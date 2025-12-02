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

*   **消息列表页**：
    *   支持下拉刷新和上拉加载更多的分页会话列表。
    *   会话按用户聚合，展示最新消息摘要、时间和未读数。
    *   根据最新消息类型（文本、图片、卡片）动态调整列表项 UI。
    *   支持用户置顶，置顶会话始终显示在列表顶部。
    *   支持为用户设置备注名。
*   **聊天页**：
    *   展示与单个用户的完整聊天记录。
    *   支持三种消息类型：纯文本、图片（动态高度）、交互式卡片。
    *   交互式卡片的状态（确认/取消）会被持久化。
*   **搜索功能**：
    *   支持通过用户昵称或消息内容进行搜索。
    *   搜索结果中高亮匹配的关键词。

# 整体项目架构
```
com.example.dydemo/
├── di/                              // 依赖注入 (Hilt)
│   ├── CoilModule.kt                // 提供自定义的 Coil ImageLoader
│   ├── AppModule.kt                 // 注入 Repository 等实例
│   └── DatabaseModule.kt            // 注入数据库、DAO 等依赖
|
├── data/                            // 数据层：负责数据的获取、存储和管理
│   ├── local/                       // 本地数据源 (Room)
│   │   ├── database/
│   │   │   ├── AppDatabase.kt       // Room 数据库配置
│   │   │   ├── UserDao.kt           // 用户数据访问对象
│   │   │   └── MessageDao.kt        // 消息数据访问对象 (新)
│   │   └── entity/                  // 数据库实体
│   │       ├── UserEntity.kt        // 用户表结构 (已重构)
│   │       └── MessageEntity.kt     // 消息表结构 (新)
│   ├── paging/                      // Paging 3 数据源
│   │   └── ConversationPagingSource.kt // 从本地数据库分页加载会话 (新)
│   ├── repository/                  // 数据仓库
│   │   └── AppRepository.kt         // 统一数据入口，协调本地数据源 (新)
│   └── source/                      // 初始数据源
│       └── messages.json            // 提供初始用户和消息的模拟数据 (新)
|
├── domain/                          // 领域层：核心业务模型和转换逻辑
│   ├── model/                       // 领域模型
│   │   ├── User.kt                  // 用户模型 (已重构)
│   │   ├── Message.kt               // 消息模型 (新, Sealed Class)
│   │   └── Conversation.kt          // 会话模型，用于主列表 (新)
│   └── mapper/                      // 数据转换器
│       ├── UserMapper.kt            // Entity <-> Model 转换 (已重构)
│       └── MessageMapper.kt         // Entity <-> Model 转换 (新)
|
├── viewmodel/                       // ViewModel 层
│   ├── MessageListViewModel.kt      // 消息列表 ViewModel (新)
│   └── ChatViewModel.kt             // 聊天界面 ViewModel (新)
|
└── ui/                              // UI 层 (Jetpack Compose)
├── screens/                     // 应用的主要屏幕
│   ├── MessageListScreen.kt     // 消息列表主屏幕 (新)
│   └── ChatScreen.kt            // 聊天详情屏幕 (新)
├── components/                  // 可复用的 UI 组件
│   ├── ConversationListItem.kt  // 会话列表项 (新)
│   ├── UserActionBottomSheet.kt // 用户操作弹窗 (已重构)
│   ├── MessageBubbles.kt        // 包含文本、图片、卡片消息气泡 (新)
│   └── SearchBar.kt             // 搜索栏组件 (新)
└── utils/                       // UI 相关的工具类
└── TimeUtils.kt             // 时间格式化工具 (新)
```