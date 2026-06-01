package com.nikolayux.masterchariot.feature.functions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.feature.functions.data.ObdRepository
import com.nikolayux.masterchariot.feature.functions.data.ObdDataSnapshot
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FunctionViewModel(
private val repository: ObdRepository
) : ViewModel() {

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _snapshot = MutableStateFlow<ObdDataSnapshot?>(null)
    val snapshot: StateFlow<ObdDataSnapshot?> = _snapshot.asStateFlow()

    private val _dtcCodes = MutableStateFlow<List<String>>(emptyList())
    val dtcCodes: StateFlow<List<String>> = _dtcCodes.asStateFlow()

    private val _isLoadingDtc = MutableStateFlow(false)
    val isLoadingDtc: StateFlow<Boolean> = _isLoadingDtc.asStateFlow()

    private var pollingJob: Job? = null

    fun connectToObd(port: String? = null) {
        viewModelScope.launch {
            _isConnecting.value = true
            val connected = repository.connect(port)
            _isConnecting.value = false
            if (connected) {
                _isConnected.value = true
                startPolling()
                loadDtcCodes()
            } else {
                _snapshot.value = null
                _dtcCodes.value = emptyList()
            }
        }
    }

    fun disconnectFromObd() {
        pollingJob?.cancel()
        viewModelScope.launch {
            repository.disconnect()
            _isConnected.value = false
            _snapshot.value = null
            _dtcCodes.value = emptyList()
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            if (!_isConnected.value) return@launch
            val data = repository.getAllData()
            _snapshot.value = data
        }
    }

    fun loadDtcCodes() {
        viewModelScope.launch {
            _isLoadingDtc.value = true
            val codes = repository.getDtcList()
            _dtcCodes.value = codes
            _isLoadingDtc.value = false
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (_isConnected.value) {
                val data = repository.getAllData()
                _snapshot.value = data
//                delay(800)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        viewModelScope.launch {
            repository.disconnect()
        }
        repository.close()
    }
}
