package com.sololeveling.system.presentation.welcome

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.sololeveling.system.data.local.SystemPreferences
import com.sololeveling.system.domain.repository.AuthRepository
import com.sololeveling.system.domain.repository.PlayerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val systemPreferences: SystemPreferences,
    private val playerRepository: PlayerRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _event = MutableSharedFlow<WelcomeEvent>(replay = 1)
    val event: SharedFlow<WelcomeEvent> = _event.asSharedFlow()

    init {
        viewModelScope.launch {
            authRepository.authState.collect { user ->
                if (user != null) {
                    navigateToApp()
                }
            }
        }

        viewModelScope.launch {
            systemPreferences.welcomeShown.collect { shown ->
                if (shown) {
                    navigateToApp()
                }
            }
        }
    }

    private suspend fun navigateToApp() {
        val hasPlayer = playerRepository.getPlayer().firstOrNull() != null
        _event.emit(
            if (hasPlayer) WelcomeEvent.NavigateToCommandCenter
            else WelcomeEvent.NavigateToAwakening
        )
    }

    fun onSignInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val intent = authRepository.getGoogleSignInIntent()
                _event.emit(WelcomeEvent.LaunchSignIn(intent))
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to start sign in"
                _isLoading.value = false
            }
        }
    }

    suspend fun onSignInResult(data: Intent?): Boolean {
        return try {
            val result = authRepository.handleSignInResult(data)
            if (result.isSuccess) {
                systemPreferences.setWelcomeShown(true)
                _event.emit(WelcomeEvent.SignInSuccess)
                true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign in failed"
                false
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Sign in failed"
            false
        } finally {
            _isLoading.value = false
        }
    }

    fun onContinueWithoutSignIn() {
        viewModelScope.launch {
            systemPreferences.setWelcomeShown(true)
            navigateToApp()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    sealed class WelcomeEvent {
        data class LaunchSignIn(val intent: Intent) : WelcomeEvent()
        object SignInSuccess : WelcomeEvent()
        object NavigateToAwakening : WelcomeEvent()
        object NavigateToCommandCenter : WelcomeEvent()
    }
}
