package com.example.dydemo.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.dydemo.data.local.database.MessageDao
import com.example.dydemo.data.local.database.UserDao
import com.example.dydemo.domain.mapper.MessageMapper.toMessage
import com.example.dydemo.domain.mapper.UserMapper.toUser
import com.example.dydemo.domain.model.Conversation
import java.io.IOException
import javax.inject.Inject

// 定义分页加载的起始页码（数据库偏移量通常从0开始）
private const val STARTING_PAGE_INDEX = 0

/**
 * Paging 3 的核心组件，负责从本地数据库按需加载分页数据。
 *
 * @param userDao 用于访问用户数据的 DAO。
 * @param messageDao 用于访问消息数据的 DAO。
 */
class ConversationPagingSource @Inject constructor(
    private val userDao: UserDao,
    private val messageDao: MessageDao
) : PagingSource<Int, Conversation>() {

    /**
     * 当需要加载新数据时，Paging 库会调用此方法。
     *
     * @param params 包含有关加载操作的信息，如要加载的页码 (key) 和加载数量 (loadSize)。
     * @return 返回一个 LoadResult 对象，可以是成功加载的 LoadResult.Page，也可以是加载失败的 LoadResult.Error。
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Conversation> {
        // 确定当前要加载的页码。如果是首次加载 (params.key == null)，则从起始页码开始。
        val page = params.key ?: STARTING_PAGE_INDEX
        // 根据页码和每页数量计算数据库查询的偏移量
        val offset = page * params.loadSize

        return try {
            // 1. 从 UserDao 分页查询用户实体
            val userEntities = userDao.getUsersForPaging(limit = params.loadSize, offset = offset)

            // 2. 将用户实体列表转换为会话（Conversation）列表
            val conversations = userEntities.map { userEntity ->
                // a. 为每个用户查找最新的消息
                val latestMessageEntity = messageDao.getLatestMessageForUser(userEntity.id)
                // b. 为每个用户查找未读消息数
                val unreadCount = messageDao.getUnreadCountForUser(userEntity.id)

                // c. 组合成 Conversation 领域模型
                Conversation(
                    user = userEntity.toUser(),
                    latestMessage = latestMessageEntity?.toMessage(),
                    unreadCount = unreadCount
                )
            }

            // 3. 构建并返回 LoadResult.Page
            LoadResult.Page(
                data = conversations,
                // 如果是第一页，则没有上一页
                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                // 如果返回的数据列表为空，说明没有更多数据了，下一页的 key 为 null
                nextKey = if (conversations.isEmpty()) null else page + 1
            )
        } catch (e: IOException) {
            // IOException 表示可能存在 I/O 问题
            LoadResult.Error(e)
        } catch (e: Exception) {
            // 捕获其他可能的异常
            LoadResult.Error(e)
        }
    }

    /**
     * 用于在数据刷新或失效后，确定从哪一页开始重新加载。
     *
     * @param state 包含有关已加载页面和最近访问位置的信息。
     * @return 返回一个新的刷新键 (页码)，Paging 库将用它来开始新的加载。
     */
    override fun getRefreshKey(state: PagingState<Int, Conversation>): Int? {
        // 尝试从最近的锚点位置（例如屏幕上最后显示的位置）找到对应的页码
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
