package com.example.aivideonote.data.remote

// Requests
data class RegisterRequest(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val key:String
)

data class LoginRequest(
    val email: String,
    val password: String
)

// Response
data class AuthUser(
    val uid: String,
    val username: String,
    val name: String,
    val email: String,
    val key: String
)