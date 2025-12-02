package com.example.dydemo.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dydemo.data.repository.AppRepository
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: Int = savedStateHandle.get<Int>("userId")!!

    val messages: StateFlow<List<Message>> = repository.getChatMessages(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.markMessagesAsRead(userId)
        }
    }

    fun updateCardInteraction(messageId: Long, state: CardInteractionState) {
        viewModelScope.launch {
            repository.updateCardInteraction(messageId, state)
        }
    }
}
