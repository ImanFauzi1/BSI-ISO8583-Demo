package com.app.edcpoc.data.repository

import com.app.edcpoc.data.model.*
import kotlinx.coroutines.delay

class PinRepository {
    
    private val pins = mutableListOf<Pin>()
    private val pinRequests = mutableListOf<PinRequest>()
    
    suspend fun createPin(cardNumber: String, pinValue: String, createdBy: String): PinResponse {
        // Simulate network delay
        delay(1500)
        
        // Check if PIN already exists for this card
        val existingPin = pins.find { it.cardNumber == cardNumber && it.status == PinStatus.ACTIVE }
        if (existingPin != null) {
            return PinResponse(false, "PIN already exists for this card", null)
        }
        
        val pin = Pin(
            id = System.currentTimeMillis().toString(),
            cardNumber = cardNumber,
            pinValue = encryptPin(pinValue), // In real app, use proper encryption
            createdAt = System.currentTimeMillis(),
            createdBy = createdBy,
            status = PinStatus.ACTIVE
        )
        
        pins.add(pin)
        return PinResponse(true, "PIN created successfully", pin)
    }
    
    suspend fun changePin(cardNumber: String, oldPin: String, newPin: String, changedBy: String): PinResponse {
        // Simulate network delay
        delay(1500)
        
        val existingPin = pins.find { it.cardNumber == cardNumber && it.status == PinStatus.ACTIVE }
        if (existingPin == null) {
            return PinResponse(false, "No active PIN found for this card", null)
        }
        
        // Verify old PIN
        if (existingPin.pinValue != encryptPin(oldPin)) {
            return PinResponse(false, "Invalid old PIN", null)
        }
        
        // Update PIN
        val updatedPin = existingPin.copy(
            pinValue = encryptPin(newPin),
            lastUsed = System.currentTimeMillis()
        )
        
        val index = pins.indexOf(existingPin)
        pins[index] = updatedPin
        
        return PinResponse(true, "PIN changed successfully", updatedPin)
    }
    
    suspend fun reissuePin(cardNumber: String, reason: String, requestedBy: String): PinResponse {
        // Simulate network delay
        delay(1500)
        
        // Block existing PIN
        val existingPin = pins.find { it.cardNumber == cardNumber && it.status == PinStatus.ACTIVE }
        if (existingPin != null) {
            val index = pins.indexOf(existingPin)
            pins[index] = existingPin.copy(status = PinStatus.BLOCKED)
        }
        
        // Create new PIN request
        val request = PinRequest(
            cardNumber = cardNumber,
            pinType = PinType.REISSUE,
            reason = reason,
            requestedBy = requestedBy
        )
        pinRequests.add(request)
        
        return PinResponse(true, "PIN reissue request submitted for approval", null)
    }
    
    suspend fun verifyPin(cardNumber: String, pinValue: String, verifiedBy: String): PinResponse {
        // Simulate network delay
        delay(1000)
        
        val pin = pins.find { it.cardNumber == cardNumber && it.status == PinStatus.ACTIVE }
        if (pin == null) {
            return PinResponse(false, "No active PIN found for this card", null)
        }
        
        val isValid = pin.pinValue == encryptPin(pinValue)
        
        if (isValid) {
            // Update last used time
            val index = pins.indexOf(pin)
            pins[index] = pin.copy(lastUsed = System.currentTimeMillis())
        }
        
        return PinResponse(isValid, if (isValid) "PIN verified successfully" else "Invalid PIN", pin)
    }
    
    suspend fun approvePinRequest(requestId: String, approvedBy: String, newPin: String): PinResponse {
        // Simulate network delay
        delay(1000)
        
        val request = pinRequests.find { it.cardNumber == requestId }
        if (request == null) {
            return PinResponse(false, "PIN request not found", null)
        }
        
        // Create new PIN
        val pin = Pin(
            id = System.currentTimeMillis().toString(),
            cardNumber = request.cardNumber,
            pinValue = encryptPin(newPin),
            createdAt = System.currentTimeMillis(),
            createdBy = approvedBy,
            status = PinStatus.ACTIVE
        )
        
        pins.add(pin)
        pinRequests.remove(request)
        
        return PinResponse(true, "PIN request approved and PIN created", pin)
    }
    
    fun getPendingPinRequests(): List<PinRequest> = pinRequests.toList()
    fun getPinsForCard(cardNumber: String): List<Pin> = pins.filter { it.cardNumber == cardNumber }
    
    private fun encryptPin(pin: String): String {
        // Simple encryption for demo - in real app use proper encryption
        return pin.reversed() + "encrypted"
    }
}
