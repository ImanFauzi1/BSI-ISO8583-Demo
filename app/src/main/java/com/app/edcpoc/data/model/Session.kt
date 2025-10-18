package com.app.edcpoc.data.model

data class Session(
    val id: String,
    val userId: String,
    val username: String,
    val role: UserRole,
    val startTime: Long,
    val endTime: Long? = null,
    val isActive: Boolean = true
)

data class DailySession(
    val id: String,
    val date: String, // YYYY-MM-DD format
    val startTime: Long?,
    val endTime: Long?,
    val startedBy: String?, // User ID who started the session
    val endedBy: String?, // User ID who ended the session
    val isActive: Boolean = false,
    val transactionCount: Int = 0
)

data class SessionRequest(
    val action: SessionAction,
    val reason: String? = null
)

enum class SessionAction {
    START_DATE,
    CLOSE_DATE
}
