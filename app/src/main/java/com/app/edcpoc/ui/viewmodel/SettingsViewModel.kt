package com.app.edcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.app.edcpoc.utils.Utility

class SettingsViewModel : ViewModel() {
    private val _pingStatuses = MutableStateFlow(List(6) { PingStatus.IDLE })
    val pingStatuses: StateFlow<List<PingStatus>> = _pingStatuses.asStateFlow()

    fun startPingSequence(
        gatewayUrl: String,
        gatewayPort: String,
        host: String,
        hostPort: String,
        tms: String,
        tmsPort: String
    ) {
        _pingStatuses.value = List(6) { PingStatus.IDLE }
        viewModelScope.launch(Dispatchers.IO) {
            val statuses = MutableList(6) { PingStatus.IDLE }
            // Ping ke Gateway
            statuses[0] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val gatewayPing = Utility.pingHost(gatewayUrl)
            statuses[0] = if (gatewayPing) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            // Telnet ke Gateway
            statuses[1] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val gatewayTelnet = Utility.telnetHost(gatewayUrl, gatewayPort.toIntOrNull() ?: 80)
            statuses[1] = if (gatewayTelnet) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            // Ping ke Host
            statuses[2] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val hostPing = Utility.pingHost(host)
            statuses[2] = if (hostPing) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            // Telnet ke Host
            statuses[3] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val hostTelnet = Utility.telnetHost(host, hostPort.toIntOrNull() ?: 80)
            statuses[3] = if (hostTelnet) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            // Ping ke TMS
            statuses[4] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val tmsPing = Utility.pingHost(tms)
            statuses[4] = if (tmsPing) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            // Telnet ke TMS
            statuses[5] = PingStatus.LOADING
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
            val tmsTelnet = Utility.telnetHost(tms, tmsPort.toIntOrNull() ?: 80)
            statuses[5] = if (tmsTelnet) PingStatus.SUCCESS else PingStatus.FAIL
            withContext(Dispatchers.Main) { _pingStatuses.value = statuses.toList() }
        }
    }
}

enum class PingStatus { IDLE, LOADING, SUCCESS, FAIL }
