package com.example.dydemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dydemo.data.repository.AppRepository
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val conversations: Flow<PagingData<Conversation>> = repository.getConversations().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
    }

    fun setPinned(userId: Int, isPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinnedStatus(userId, isPinned)
        }
    }

    fun updateRemark(userId: Int, remark: String?) {
        viewModelScope.launch {
            repository.updateRemark(userId, remark)
        }
    }

    /**
     * 新增：处理来自消息列表的卡片点击事件
     */
    fun handleCardActionFromList(messageId: Long) {
        viewModelScope.launch {
            // 默认列表上的操作为“确认”
            repository.updateCardInteraction(messageId, CardInteractionState.CONFIRMED)
        }
    }
}
