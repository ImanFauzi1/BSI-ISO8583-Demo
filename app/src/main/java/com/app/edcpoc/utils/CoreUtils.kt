package com.app.edcpoc.utils


object CoreUtils {
    fun generateUniqueStan(): String {
        val rnd = java.security.SecureRandom()
        val number = rnd.nextInt(1_000_000)
        return String.format("%06d", number)
    }
}