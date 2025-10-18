package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.*
import kotlinx.coroutines.delay

class AuthRepository {
    
    // Mock data for demonstration
    private val users = listOf(
        User("1", "supervisor", "John Supervisor", UserRole.SUPERVISOR),
        User("2", "operator", "Jane Operator", UserRole.OPERATOR),
        User("3", "teller", "Bob Teller", UserRole.TELLER)
    )
    
    private val passwords = mapOf(
        "supervisor" to "super123",
        "operator" to "oper123",
        "teller" to "tell123"
    )
    
    suspend fun login(username: String, password: String): LoginResponse {
        // Simulate network delay
        delay(1000)
        
        val user = users.find { it.username == username }
        val correctPassword = passwords[username]
        
        return when {
            user == null -> LoginResponse(false, null, "User not found")
            correctPassword != password -> LoginResponse(false, null, "Invalid password")
            !user.isActive -> LoginResponse(false, null, "User account is inactive")
            else -> LoginResponse(true, user, "Login successful")
        }
    }
    
    fun getCurrentUser(): User? {
        // In real app, this would get from secure storage
        return null
    }
    
    fun logout() {
        // In real app, this would clear secure storage
    }
    
    fun hasPermission(user: User, action: String): Boolean {
        return when (user.role) {
            UserRole.SUPERVISOR -> true // Supervisor can do everything
            UserRole.OPERATOR -> when (action) {
                "start_date", "close_date" -> false
                "logon", "logoff", "create_pin_input", "reissue_pin_input", "change_pin", "verification_pin" -> true
                else -> false
            }
            UserRole.TELLER -> when (action) {
                "verification_pin", "change_pin_customer" -> true
                else -> false
            }
        }
    }
}
