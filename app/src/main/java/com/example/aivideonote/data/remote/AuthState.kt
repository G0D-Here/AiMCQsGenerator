package com.example.aivideonote.data.remote

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Failure(val error: AuthError) : AuthResult<Nothing>()
}

sealed class AuthError {
    data object NetworkError : AuthError()
    data object InvalidCredentials : AuthError()
    data object EmailInUse : AuthError()
    data object UsernameTaken : AuthError()
    data object UserDataNotFound : AuthError()
    data object NotLoggedIn : AuthError()
    data class UnknownError(val message: String) : AuthError()
    data class ValidationError(val message: String):AuthError()
    data object WeakPassword:AuthError()
}

sealed class UsernameValidationState {
    data object Idle : UsernameValidationState()
    data object Loading : UsernameValidationState()
    data class Available(val isAvailable: Boolean) : UsernameValidationState()
    data class Error(val message: String) : UsernameValidationState()
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    class Success(val data: AuthUser) : AuthUiState()
    class Error(val error: AuthError) : AuthUiState()
}

// Extension for error messages
fun AuthError.toUserMessage(): String = when (this) {
    AuthError.EmailInUse -> "Email already in use"
    AuthError.InvalidCredentials -> "Invalid email or password"
    AuthError.NetworkError -> "Network error"
    AuthError.NotLoggedIn -> "Not logged in"
    AuthError.UserDataNotFound -> "User data not found"
    AuthError.UsernameTaken -> "Username already taken"
    AuthError.WeakPassword -> "Password too weak"
    is AuthError.UnknownError -> "Unknown error: $message"
    is AuthError.ValidationError -> "This is : $message"
}