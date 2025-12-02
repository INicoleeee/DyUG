package com.example.dydemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dydemo.data.repository.AppRepository
import com.example.dydemo.domain.model.CardInteractionState
import com.example.dydemo.domain.model.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Conversation>>(emptyList())
    val searchResults: StateFlow<List<Conversation>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        _searchResults.value = emptyList()
                        return@flatMapLatest flowOf(emptyList<Conversation>())
                    }
                    _isSearching.value = true
                    try {
                        flowOf(repository.searchConversations(query))
                    } catch (e: Exception) {
                        flowOf(emptyList<Conversation>())
                    }
                }
                .collect { results ->
                    _searchResults.value = results
                    _isSearching.value = false
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * 新增：处理来自搜索结果列表的卡片点击
     */
    fun handleCardActionFromSearch(messageId: Long) {
        viewModelScope.launch {
            repository.updateCardInteraction(messageId, CardInteractionState.CONFIRMED)
            // 重新执行搜索以刷新结果
            val currentQuery = _searchQuery.value
            if (currentQuery.isNotBlank()) {
                _searchResults.value = repository.searchConversations(currentQuery)
            }
        }
    }
}
