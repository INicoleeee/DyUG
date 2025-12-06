# 🚀 Douyin Message Demo: 消息列表与聊天模块

一个基于现代 Android 技术栈（MVVM、Compose、Room、Paging）构建的抖音风格消息应用模块。

## ⚙️ 技术栈

| 类别 | 技术 | 备注 |
|:---|:---|:---|
| **语言** | Kotlin | |
| **UI 框架** | Jetpack Compose | 声明式 UI，用于构建高性能、动态的界面。 |
| **架构** | MVVM (Repository Pattern) | 清晰的关注点分离，易于维护和测试。 |
| **分页加载** | Jetpack Paging 3 | 从本地数据库高效加载和展示大规模会话列表。 |
| **数据库** | Room Persistence Library | 用于持久化用户信息、消息数据和交互状态。 |
| **异步处理** | Kotlin Coroutines & Flow | 用于处理数据库 I/O 和 UI 状态的实时更新。 |
| **依赖注入** | Hilt (Dagger) | |
| **图片加载** | Coil | 专为 Jetpack Compose 设计的图片加载库。 |

## ✨ 主要功能

*   **消息列表**：
    *   **实时刷新与排序**：新消息到达时，列表自动刷新，并将该会话实时排序到顶部（置顶用户内部同样遵循此规则），并带有流畅的滚动动画。
    *   **丰富的数据展示**：每个会话单元都清晰展示头像、昵称/备注、最新消息摘要、格式化的时间（如“刚刚”、“昨天 10:20”）以及未读数角标。
    *   **无网/弱网占位符**：当用户头像加载失败或正在加载时，显示由用户昵称后三位和随机背景色组成的占位符，提升了视觉体验的健壮性。
    *   **动态的 UI 响应**：列表项的 UI 根据最新消息的类型（文本、图片、卡片）动态调整，例如图片消息会显示缩略图，卡片消息会显示可交互的按钮。

*   **聊天界面**：
    *   **多类型消息气泡**：支持纯文本、图片（自动匹配高度）和交互式卡片三种消息类型，每种都有独特的视觉展现，并附带时间戳。
    *   **全局状态同步**：卡片消息的交互状态（如“领取红包”变为“已确认”）在消息列表和聊天页之间完全同步，保证了数据的一致性。
    *   **持久化交互**：用户的置顶偏好、备注名、卡片交互状态，以及消息的已读状态，都会被完整地持久化到本地数据库。

*   **搜索功能**：
    *   **多维度精确搜索**：支持通过用户昵称、备注或消息内容进行搜索。
    *   **多结果展示**：如果一个用户的多条消息都匹配搜索词，该用户将在结果列表中出现多次，每次都对应一条命中的消息。
    *   **关键词高亮**：在搜索结果中，所有匹配的关键词都会被高亮显示，让用户可以一目了然地看到匹配原因。

*   **模拟后台服务**：
    *   内置一个模拟的消息分发中心，会定时向随机用户发送新消息，用于测试和展示应用的实时响应能力。

# 整体项目架构
```
com.example.dydemo/
├── di/                              // 依赖注入模块 (Hilt)
│   ├── CoilModule.kt                // 提供自定义的 Coil ImageLoader
│   ├── AppModule.kt                 // 提供全局单例，如 Repository
│   ├── DatabaseModule.kt            // 提供数据库及 DAO 相关依赖
│   └── MessageDispatcher.kt         // 模拟后台消息分发的服务
|
├── data/                            // 数据层：负责所有数据的获取、存储和管理
│   ├── local/                       // 本地数据源
│   │   ├── database/
│   │   │   ├── AppDatabase.kt       // Room 数据库的配置中心
│   │   │   ├── UserDao.kt           // 用户数据访问对象
│   │   │   └── MessageDao.kt        // 消息数据访问对象
│   │   └── entity/                  // 数据库实体 (表结构)
│   │       ├── UserEntity.kt        // 用户表
│   │       └── MessageEntity.kt     // 消息表
│   └── repository/                  // 数据仓库
│       └── AppRepository.kt         // 项目唯一的数据入口，封装所有数据逻辑
|
├── domain/                          // 领域层：定义核心业务模型
│   ├── model/                       // 业务模型 (在 UI 和 ViewModel 中使用)
│   │   ├── User.kt                  // 用户模型
│   │   ├── Message.kt               // 消息模型 (使用 Sealed Class 定义多类型)
│   │   └── Conversation.kt          // 会话模型 (用于消息列表)
│   └── mapper/                      // 数据转换器
│       ├── UserMapper.kt            // 在 UserEntity 和 User 之间转换
│       └── MessageMapper.kt         // 在 MessageEntity 和 Message 之间转换
|
├── viewmodel/                       // ViewModel 层：处理业务逻辑和 UI 状态
│   ├── MessageListViewModel.kt      // 消息列表页的 ViewModel
│   ├── ChatViewModel.kt             // 聊天详情页的 ViewModel
│   └── SearchViewModel.kt           // 搜索页的 ViewModel
|
└── ui/                              // UI 层 (Jetpack Compose)
    ├── screens/                     // 应用的主要屏幕
    │   ├── MessageListScreen.kt     // 消息列表主屏幕
    │   ├── ChatScreen.kt            // 聊天详情屏幕
    │   └── SearchScreen.kt          // 搜索结果屏幕
    ├── components/                  // 可复用的 UI 组件
    │   ├── ConversationListItem.kt  // 消息列表中的单个会话项
    │   ├── UserActionBottomSheet.kt // 点击头像弹出的操作菜单
    │   ├── RemarkEditDialog.kt      // 修改备注的对话框
    │   ├── MessageBubbles.kt        // 聊天界面中的消息气泡
    │   └── UserAvatar.kt            // 带占位符功能的用户头像组件
    └── utils/                       // UI 相关的工具类
        ├── AvatarUtils.kt           // 生成头像占位符颜色的工具
        └── TimeUtils.kt             // 时间格式化工具
```
