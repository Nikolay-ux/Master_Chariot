package com.nikolayux.masterchariot.feature.functions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.data.obd2.Obd2Service
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FunctionViewModel @Inject constructor(
    private val obd2Service: Obd2Service,
    private val bluetoothService: BluetoothService,
    private val repository: CarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FunctionState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            obd2Service.speed.collect { speed ->
                _state.value = _state.value.copy(
                    speed = speed
                )
            }
        }
        viewModelScope.launch {
            obd2Service.rpm.collect { rpm ->
                _state.value = _state.value.copy(
                    rpm = rpm
                )
            }
        }
        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                val connected = status == ConnectionStatus.Connected
                _state.update {
                    it.copy(
                        isConnected = connected
                    )
                }
                if (connected) {
                    loadDtcCodes()
                }
            }
        }
        viewModelScope.launch {
            repository.getSelectedCarFlow()
                .collect { car ->
                    _state.update {
                        it.copy(
                            isUsingMiles = car?.isUsingMiles ?: false,
                            selectedCarName = car?.name,
                            untilService = car?.kmUntilMaintenance ?: 0
                        )
                    }
                }
        }
    }

    fun loadDtcCodes() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoadingDtc = true
                )
            }
            val codes = obd2Service.readDiagnosticTroubleCodes() ?: emptyList()
            _state.update {
                it.copy(
                    dtcCodes = codes,
                    isLoadingDtc = false
                )
            }
        }
    }


}