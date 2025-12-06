package com.example.dydemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.dydemo.data.repository.AppRepository
import com.example.dydemo.di.MessageDispatcher
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private val repository: AppRepository,
    private val dispatcher: MessageDispatcher // <-- 注入分发器
) : ViewModel() {

    val conversations: Flow<PagingData<Conversation>> = repository.getConversations().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
        }
        // 启动消息分发
        dispatcher.start()
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

    fun handleCardActionFromList(messageId: Long) {
        viewModelScope.launch {
            repository.updateCardInteraction(messageId, CardInteractionState.CONFIRMED)
        }
    }

    // 在 ViewModel 销毁时停止分发器，防止内存泄漏
    override fun onCleared() {
        super.onCleared()
        dispatcher.stop()
    }
}
