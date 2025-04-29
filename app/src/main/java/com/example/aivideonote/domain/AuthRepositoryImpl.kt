package com.example.aivideonote.domain

import com.example.aivideonote.data.remote.AuthResult
import com.example.aivideonote.data.remote.AuthUser
import com.example.aivideonote.data.remote.LoginRequest
import com.example.aivideonote.data.remote.RegisterRequest
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(request: RegisterRequest): AuthResult<AuthUser>
    suspend fun login(request: LoginRequest): AuthResult<AuthUser>
    suspend fun getCurrentUser(): AuthResult<AuthUser>
    suspend fun logout(): AuthResult<Unit>
    fun checkUsernameAvailability(username: String): Flow<Boolean>

}
