package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.*
import kotlinx.coroutines.delay

class SessionRepository {
    
    private var currentDailySession: DailySession? = null
    private var currentUserSession: Session? = null
    
    suspend fun startDate(userId: String): Boolean {
        // Simulate network delay
        delay(1000)
        
        if (currentDailySession?.isActive == true) {
            return false // Session already active
        }
        
        currentDailySession = DailySession(
            id = System.currentTimeMillis().toString(),
            date = getCurrentDate(),
            startTime = System.currentTimeMillis(),
            endTime = null,
            startedBy = userId,
            endedBy = null,
            isActive = true,
            transactionCount = 0
        )
        
        return true
    }
    
    suspend fun closeDate(userId: String): Boolean {
        // Simulate network delay
        delay(1000)
        
        val session = currentDailySession ?: return false
        
        if (!session.isActive) {
            return false // No active session to close
        }
        
        currentDailySession = session.copy(
            endTime = System.currentTimeMillis(),
            endedBy = userId,
            isActive = false
        )
        
        return true
    }
    
    suspend fun logon(userId: String, username: String, role: UserRole): Boolean {
        // Simulate network delay
        delay(500)
        
        if (currentUserSession?.isActive == true) {
            return false // User already logged in
        }
        
        currentUserSession = Session(
            id = System.currentTimeMillis().toString(),
            userId = userId,
            username = username,
            role = role,
            startTime = System.currentTimeMillis(),
            isActive = true
        )
        
        return true
    }
    
    suspend fun logoff(): Boolean {
        // Simulate network delay
        delay(500)
        
        val session = currentUserSession ?: return false
        
        currentUserSession = session.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )
        
        return true
    }
    
    fun getCurrentDailySession(): DailySession? = currentDailySession
    fun getCurrentUserSession(): Session? = currentUserSession
    fun isDailySessionActive(): Boolean = currentDailySession?.isActive == true
    fun isUserLoggedIn(): Boolean = currentUserSession?.isActive == true
    
    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
