package com.sololeveling.system.presentation.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sololeveling.system.domain.model.LeaderboardEntry
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.LeaderboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    val authRepository: AuthRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val entries: StateFlow<List<LeaderboardEntry>> = _entries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collectLatest { user ->
                _isAuthenticated.value = user != null
                if (user == null) {
                    _errorMessage.value = "Sign in to view the global leaderboard."
                }
            }
        }

        viewModelScope.launch {
            leaderboardRepository.getLeaderboard()
                .catch { e ->
                    _errorMessage.value = e.message ?: "Failed to load leaderboard."
                    _isLoading.value = false
                }
                .collectLatest { list ->
                    _entries.value = list
                    _errorMessage.value = null
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
