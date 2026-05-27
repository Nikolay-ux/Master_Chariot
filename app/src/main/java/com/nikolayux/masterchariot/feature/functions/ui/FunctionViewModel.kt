package com.nikolayux.masterchariot.feature.functions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.data.obd2.Obd2Service
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FunctionViewModel @Inject constructor(
    private val obd2Service: Obd2Service,
    private val bluetoothService: BluetoothService
) : ViewModel() {
    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed.asStateFlow()

    private val _rpm = MutableStateFlow(0)
    val rpm: StateFlow<Int> = _rpm.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _dtcCodes = MutableStateFlow<List<String>>(emptyList())
    val dtcCodes: StateFlow<List<String>> = _dtcCodes.asStateFlow()

    private val _isLoadingDtc = MutableStateFlow(false)
    val isLoadingDtc: StateFlow<Boolean> = _isLoadingDtc.asStateFlow()

    init {
        viewModelScope.launch {
            obd2Service.speed.collect { _speed.value = it }
        }
        viewModelScope.launch {
            obd2Service.rpm.collect { _rpm.value = it }
        }
        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                _isConnected.value = status == ConnectionStatus.Connected
            }
        }
        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                _isConnected.value = status == ConnectionStatus.Connected
//                if (_isConnected.value) {
//                    loadDtcCodes()
//                }
            }
        }
    }

    fun loadDtcCodes() {
        viewModelScope.launch {
            _isLoadingDtc.value = true
//            obd2Service.readDiagnosticTroubleCodes()
            _isLoadingDtc.value = false
        }
    }
}