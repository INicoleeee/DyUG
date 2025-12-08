#  Douyin Message Demo: 消息列表与聊天模块

一个基于现代 Android 技术栈（MVVM、Compose、Room、Paging）构建的抖音风格消息应用模块。

## 技术栈

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

##  主要功能

*   **智能消息列表**：
    *   **实时刷新与排序**：新消息到达时，列表自动刷新，并将该会话实时排序到顶部（置顶用户内部同样遵循此规则），并带有流畅的滚动动画。
    *   **丰富的数据展示**：每个会话单元都清晰展示头像、昵称/备注、最新消息摘要、格式化的时间（如“刚刚”、“昨天 10:20”）以及未读数角标。
    *   **无网/弱网占位符**：当用户头像加载失败或正在加载时，显示由用户昵称后三位和随机背景色组成的占位符，提升了视觉体验的健壮性。
    *   **动态的 UI 响应**：列表项的 UI 根据最新消息的类型（文本、图片、卡片）动态调整，例如图片消息会显示缩略图，卡片消息会显示可交互的按钮。

*   **沉浸式聊天界面**：
    *   **多类型消息气泡**：支持纯文本、图片（自动匹配高度）和交互式卡片三种消息类型，每种都有独特的视觉展现，并附带时间戳。
    *   **全局状态同步**：卡片消息的交互状态（如“领取红包”变为“已确认”）在消息列表和聊天页之间完全同步，保证了数据的一致性。
    *   **持久化交互**：用户的置顶偏好、备注名、卡片交互状态，以及消息的已读状态，都会被完整地持久化到本地数据库。

*   **高性能的搜索功能**：
    *   **多维度精确搜索**：支持通过用户昵称、备注或消息内容进行搜索。
    *   **多结果展示**：如果一个用户的多条消息都匹配搜索词，该用户将在结果列表中出现多次，每次都对应一条命中的消息。
    *   **关键词高亮**：在搜索结果中，所有匹配的关键词都会被高亮显示，让用户可以一目了然地看到匹配原因。

*   **模拟后台服务**：
    *   内置一个模拟的消息分发中心，会定时向随机用户发送新消息，用于测试和展示应用的实时响应能力。

##  关键设计思路

**1. 功能：** 列表实时刷新与自动排序

*   **设计思路：** 采用“单一数据源”原则，将 Room 数据库作为唯一可信的数据中心。UI 层完全由响应式数据流驱动，而非手动调用接口刷新。新消息的到来只需要更新数据库，UI 会自动响应这些变化。
*   **实现过程：**
    1.  在 `UserEntity` 中维护一个 `lastMessageTimestamp` 字段作为排序依据。
    2.  `AppRepository` 中提供 `insertNewMessage` 方法，在 `@Transaction` 中同时插入新消息并更新对应用户的 `lastMessageTimestamp`，保证数据原子性。
    3.  `UserDao` 提供由 Room 原生支持的 `PagingSource`，该 `PagingSource` 会自动观察 `users` 表的变化。当时间戳更新时，`PagingSource` 自动失效，触发 Paging 3 刷新数据流。
    4.  UI 层 (`MessageListScreen`) 订阅此 Paging 3 数据流，通过 `snapshotFlow` 监听列表首项的变化，并在变化时自动执行 `animateScrollToItem(0)` 实现滚动。

**2. 功能：** 精准的搜索结果展示

*   **设计思路：** 搜索功能的核心是“结果必须精确匹配用户的搜索意图”。如果用户搜索的是消息内容，结果就应该展示那条被命中的消息，而不是该用户的其他消息。这意味着搜索结果的最小单位是“消息”，而非“用户”。
*   **实现过程：**
    1.  重构 `AppRepository` 中的 `searchConversations` 方法。
    2.  **消息内容匹配优先：** 先调用 `MessageDao.searchMessagesByContent()` 找出所有内容匹配的消息实体。
    3.  为**每一条**被命中的消息实体，都创建一个对应的 `Conversation` 对象，并将这条消息作为 `latestMessage` 字段。这确保了一个用户如果有多条消息被命中，会在结果列表中出现多次。
    4.  **用户名称匹配为辅：** 再调用 `UserDao.searchUsersByNameOrRemark()` 找出名称匹配的用户，并过滤掉那些已经因消息匹配而出现的用户，避免重复。
    5.  合并两个结果列表，最终实现精确的多结果展示。

**3. 功能：** UI 状态的封装与复用 (头像占位符)

*   **设计思路：** 将复杂的 UI 状态逻辑（如图片加载中、加载失败、加载成功）封装在一个独立的、可复用的组件中，使上层调用者保持简洁，并确保 UI 表现的一致性。
*   **实现过程：**
    1.  创建 `AvatarUtils.kt`，提供一个 `rememberUserAvatarColor` 函数。它能根据固定的用户 ID，从一个预设颜色列表中，返回一个稳定、不随重组变化的颜色。
    2.  创建 `UserAvatar` Composable 组件，内部使用 Coil 的 `AsyncImage` 并通过 `onState` 回调监听其加载状态。
    3.  当状态为 `Loading` 或 `Error` 时，`UserAvatar` 显示一个由 `rememberUserAvatarColor` 决定的背景色和用户昵称后三位组成的占位符。
    4.  在 `ConversationListItem` 和 `UserActionBottomSheet` 中，用 `<UserAvatar user={...} />` 替换掉原先复杂的 `AsyncImage` 调用，实现了逻辑的封装和 UI 的统一。

**4. 功能：** 跨页面的数据同步 (未读角标消失)

*   **设计思路：** 当在一个页面（`ChatScreen`）上的操作改变了数据（消息变为已读），返回到上一个页面（`MessageListScreen`）时，UI 必须能反映这个变化。最直接可靠的方式是让 `MessageListScreen` 在重新变为活跃状态时，强制刷新一次数据。
*   **实现过程：**
    1.  在 `ChatViewModel` 的 `init` 块中，调用 `repository.markMessagesAsRead()`，这会更新数据库。
    2.  在 `MessageListScreen` 中，使用 `DisposableEffect` 和 `LifecycleEventObserver` 来观察其生命周期。
    3.  当监听到 `Lifecycle.Event.ON_RESUME` 事件时（即页面从后台回到前台），手动调用 `pagingItems.refresh()`。
    4.  `refresh()` 会强制 Paging 3 从数据源（Room 的 PagingSource）重新加载数据，此时它会获取到最新的“未读数为 0”的状态，从而正确地移除未读角标。

**5. 功能：** 全局状态同步 (交互卡片)

*   **设计思路：** 对于同一个数据状态（如卡片是否被确认），无论用户在哪个界面进行操作，其结果都应该被持久化，并在所有相关界面上得到一致的展现。这同样遵循“单一数据源”原则。
*   **实现过程：**
    1.  在 `MessageEntity` 中添加 `cardInteractionState` 字段来持久化卡片状态。
    2.  **内部同步：** 在 `ChatScreen` 的 `CardMessageBubble` 中，当用户点击按钮时，直接调用 `ChatViewModel` 的方法更新数据库中的 `cardInteractionState`。
    3.  **外部同步：** 在 `ConversationListItem` 的卡片按钮被点击时，也调用 `MessageListViewModel` 的方法，执行同样的数据更新操作。
    4.  由于 `ConversationListItem` 和 `CardMessageBubble` 的 UI 都依赖于从数据库读取的 `cardInteractionState`，因此任何一方的修改都会自动反映在另一方，实现了全局状态的同步。

# 整体项目架构
```
com.example.dydemo/
├── di/                              // 依赖注入模块 (Hilt)
│   ├── AppModule.kt                 // 提供全局单例
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
