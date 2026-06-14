package com.nikolayux.masterchariot.feature.instrument

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
class InstrumentPanelViewModel @Inject constructor(
    private val obd2Service: Obd2Service,
    private val bluetoothService: BluetoothService,
    private val repository: CarRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InstrumentPanelState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            obd2Service.speed.collect { speed ->
                _state.update { it.copy(speed = speed) }
            }
        }
        viewModelScope.launch {
            obd2Service.rpm.collect { rpm ->
                _state.update { it.copy(rpm = rpm) }
            }
        }
        viewModelScope.launch {
            obd2Service.coolantTemp.collect { temperature ->
                _state.update { it.copy(coolantTemperature = temperature) }
            }
        }
        viewModelScope.launch {
            obd2Service.engineLoad.collect { load ->
                _state.update { it.copy(engineLoad = load) }
            }
        }
        viewModelScope.launch {
            obd2Service.fuelConsumption.collect { consumption ->
                _state.update { it.copy(fuelConsumption = consumption) }
            }
        }
        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                _state.update { it.copy(isConnected = status == ConnectionStatus.Connected) }
            }
        }
        viewModelScope.launch {
            repository.getSelectedCarFlow().collect { car ->
                _state.update { it.copy(isUsingMiles = car?.isUsingMiles ?: false) }
            }
        }
    }
}
