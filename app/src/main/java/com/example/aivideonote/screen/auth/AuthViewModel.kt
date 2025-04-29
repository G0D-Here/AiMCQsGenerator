package com.example.aivideonote.screen.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aivideonote.data.remote.AuthResult
import com.example.aivideonote.data.remote.AuthUiState
import com.example.aivideonote.data.remote.FirebaseAuthRepoImpl
import com.example.aivideonote.data.remote.LoginRequest
import com.example.aivideonote.data.remote.RegisterRequest
import com.example.aivideonote.data.remote.UsernameValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuthRepoImpl,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState = _authState.asStateFlow()

    private val _usernameState =
        MutableStateFlow<UsernameValidationState>(UsernameValidationState.Idle)

    val usernameAvailabilityState = _usernameState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkUsernameAvailability(username: String) {
        viewModelScope.launch {
            _usernameState.value = UsernameValidationState.Loading
            delay(300)
            auth.checkUsernameAvailability(username).catch { e ->
                _usernameState.value =
                    UsernameValidationState.Error(e.message ?: "Username check failed")
            }.collect {
                _usernameState.value = UsernameValidationState.Available(it)
            }
        }
    }

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading

            when (val result = auth.register(request = request)) {
                is AuthResult.Failure -> {
                    _authState.value = AuthUiState.Error(result.error)
                }

                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Success(result.data)
                    apikeyToUse = result.data.key


                }
            }
        }
    }

    fun login(request: LoginRequest) {
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            when (val result = auth.login(request = request)) {
                is AuthResult.Failure -> {
                    _authState.value = AuthUiState.Error(result.error)
                }

                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Success(result.data)
                    apikeyToUse = result.data.key
                }
            }
        }
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = auth.getCurrentUser()) {
                is AuthResult.Failure -> {
//                    _authState.value = AuthUiState.Error(result.error)
                    _authState.value = AuthUiState.Idle

                }

                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Success(result.data)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            when (val result = auth.logout()) {
                is AuthResult.Failure -> {
                    _authState.value = AuthUiState.Error(result.error)
                }

                is AuthResult.Success -> {
                    _authState.value = AuthUiState.Idle
                    _usernameState.value = UsernameValidationState.Idle
                }
            }
        }
    }

    fun addMCQs(questions: String) {
        viewModelScope.launch {
            auth.addQuestionToUser(questions)
            Log.d("MCQGotAppended", "Work Done")
        }
    }


}

var apikeyToUse = ""
