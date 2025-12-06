package com.example.dydemo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.dydemo.domain.model.Conversation
import com.example.dydemo.domain.model.User
import com.example.dydemo.ui.components.ConversationListItem
import com.example.dydemo.ui.components.RemarkEditDialog
import com.example.dydemo.ui.components.UserActionBottomSheet
import com.example.dydemo.viewmodel.MessageListViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    onNavigateToChat: (Int) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: MessageListViewModel = hiltViewModel()
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()
    val isRefreshing = conversations.loadState.refresh is LoadState.Loading
    val listState = rememberLazyListState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showRemarkDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(conversations) {
        snapshotFlow { conversations.itemSnapshotList.firstOrNull() }
            .distinctUntilChanged()
            .filter { it != null }
            .collect { 
                listState.animateScrollToItem(0)
            }
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
                listState = listState,
                conversations = conversations,
                isRefreshing = isRefreshing,
                onRefresh = { conversations.refresh() },
                onItemClick = onNavigateToChat,
                onAvatarClick = { user ->
                    selectedUser = user
                    showBottomSheet = true
                },
                onCardActionClick = { viewModel.handleCardActionFromList(it) }
            )
        }
    }

    if (showBottomSheet && selectedUser != null) {
        UserActionBottomSheet(
            user = selectedUser!!,
            onDismiss = { showBottomSheet = false },
            onSetPinned = { isPinned -> viewModel.setPinned(selectedUser!!.id, isPinned) },
            onEditRemark = { showBottomSheet = false; showRemarkDialog = true }
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
            onConfirm = { viewModel.updateRemark(selectedUser!!.id, currentRemark); showRemarkDialog = false }
        )
    }
}

@Composable
private fun SearchBarPlaceholder(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)) // <-- 修改背景色
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp), // <-- 修改 Padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "搜索",
            tint = MaterialTheme.colorScheme.onSurface // <-- 修改图标颜色
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("搜索", color = MaterialTheme.colorScheme.onSurface) // <-- 修改文本颜色
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageListContent(
    listState: LazyListState,
    conversations: LazyPagingItems<Conversation>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onItemClick: (Int) -> Unit,
    onAvatarClick: (User) -> Unit,
    onCardActionClick: (messageId: Long) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            if (conversations.loadState.refresh is LoadState.Loading && conversations.itemCount < 1) {
                item { Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }

            items(conversations.itemCount) { index ->
                conversations[index]?.let { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onItemClick = onItemClick,
                        onAvatarClick = { onAvatarClick(conversation.user) },
                        onCardActionClick = onCardActionClick
                    )
                }
            }

            if (conversations.loadState.append is LoadState.Loading) {
                item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
        }
    }
}
