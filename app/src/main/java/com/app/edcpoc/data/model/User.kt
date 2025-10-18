package com.app.edcpoc.data.model

data class User(
    val id: String,
    val username: String,
    val name: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val lastLogin: Long? = null
)

enum class UserRole {
    SUPERVISOR,
    OPERATOR,
    TELLER
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val user: User?,
    val message: String
)
