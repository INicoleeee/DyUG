package com.example.dydemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.dydemo.domain.model.Conversation
import com.example.dydemo.domain.model.User
import com.example.dydemo.ui.components.ConversationListItem
import com.example.dydemo.ui.components.RemarkEditDialog
import com.example.dydemo.ui.components.UserActionBottomSheet
import com.example.dydemo.viewmodel.MessageListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    onNavigateToChat: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: MessageListViewModel = hiltViewModel()
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    val isRefreshing = conversations.loadState.refresh is LoadState.Loading

    var showBottomSheet by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                conversations.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("消息") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBarPlaceholder(onClick = onNavigateToSearch)

            MessageListContent(
                conversations = conversations,
                isRefreshing = isRefreshing,
                onRefresh = { conversations.refresh() },
                onItemClick = onNavigateToChat,
                onAvatarClick = { user ->
                    selectedUser = user
                    showBottomSheet = true
                },
                // 链接卡片点击事件
                onCardActionClick = {
                    viewModel.handleCardActionFromList(it)
                    conversations.refresh()
                }
            )
        }
    }

    if (showBottomSheet && selectedUser != null) {
        UserActionBottomSheet(
            user = selectedUser!!,
            onDismiss = { showBottomSheet = false },
            onSetPinned = { isPinned ->
                viewModel.setPinned(selectedUser!!.id, isPinned)
                conversations.refresh()
                showBottomSheet = false
            },
            onEditRemark = {
                showBottomSheet = false
                showRemarkDialog = true
            }
        )
    }

    if (showRemarkDialog && selectedUser != null) {
        var currentRemark by remember { mutableStateOf(selectedUser?.customRemark ?: "") }

        RemarkEditDialog(
            userNickname = selectedUser!!.nickname,
            initialRemark = selectedUser?.customRemark ?: "",
            onInputChange = { newText -> currentRemark = newText },
            onClearInput = { currentRemark = "" },
            onDismiss = { showRemarkDialog = false },
            onConfirm = {
                viewModel.updateRemark(selectedUser!!.id, currentRemark)
                showRemarkDialog = false
                conversations.refresh()
            }
        )
    }
}

@Composable
private fun SearchBarPlaceholder(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = "搜索", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text("搜索", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageListContent(
    conversations: LazyPagingItems<Conversation>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (Int) -> Unit,
    onAvatarClick: (User) -> Unit,
    onCardActionClick: (messageId: Long) -> Unit // <-- 接收回调
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (conversations.loadState.refresh is LoadState.Loading) {
                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }

            items(conversations.itemCount) { index ->
                conversations[index]?.let { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onItemClick = onItemClick,
                        onAvatarClick = { onAvatarClick(conversation.user) },
                        onCardActionClick = onCardActionClick // <-- 传递回调
                    )
                }
            }

            if (conversations.loadState.append is LoadState.Loading) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
        }
    }
}
