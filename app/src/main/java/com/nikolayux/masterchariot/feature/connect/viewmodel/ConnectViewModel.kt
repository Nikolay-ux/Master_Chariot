package com.nikolayux.masterchariot.feature.connect.viewmodel

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.feature.connect.state.ConnectEffect
import com.nikolayux.masterchariot.feature.connect.state.ConnectMessage
import com.nikolayux.masterchariot.feature.connect.state.ConnectState
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import com.nikolayux.masterchariot.feature.connect.state.ConnectionType
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val bluetoothService: BluetoothService
) : ViewModel() {
    private val _state = MutableStateFlow(ConnectState())
    val state: StateFlow<ConnectState> = _state.asStateFlow()

    private val _effect = Channel<ConnectEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                _state.update {
                    it.copy(
                        connectionStatus = status,
                        connectingDeviceAddress =
                            if (status in listOf(
                                    ConnectionStatus.Connected,
                                    ConnectionStatus.Disconnected,
                                    ConnectionStatus.Error)
                                ) {
                                null
                            } else {
                                it.connectingDeviceAddress
                            }
                    )
                }
                if (status == ConnectionStatus.Connected) {
                    _effect.send(ConnectEffect.Connected)
                }
            }
        }

        viewModelScope.launch {
            bluetoothService.discoveredDevices.collect { devices ->
                _state.update { it.copy(isLoading = false, discoveredDevices = devices) }
            }
        }

        viewModelScope.launch {
            bluetoothService.receivedData.collect { data ->
                _state.update { it.copy(lastReceivedData = data) }
            }
        }

        viewModelScope.launch {
            bluetoothService.btState.collect { isEnable ->
                _state.update { it.copy(isBluetoothEnabled = isEnable == true) }
            }
        }

//        viewModelScope.launch {
//            bluetoothService.pairingRequest.collect { device ->
//                pendingPairingDevice = device
//                _effect.send(ConnectEffect.RequestPairingPin(device))
//            }
//        }

        viewModelScope.launch {
            bluetoothService.pairingCompleted.collect { device ->
                _state.update { it.copy(connectingDeviceAddress = device.address) }
//                bluetoothService.proceedWithConnectionAfterPairing(device)
                bluetoothService.startConnectThread(device)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun onIntent(message: ConnectMessage) {
        viewModelScope.launch {
            when (message) {
                is ConnectMessage.ToggleBluetooth -> toggleBluetooth()
                is ConnectMessage.StartDiscovery -> startDiscovery()
                is ConnectMessage.ConnectToDevice -> connectToDevice(message.device)
//                is ConnectMessage.SendData -> sendData(message.data)
                is ConnectMessage.Disconnect -> disconnect()
                is ConnectMessage.BluetoothEnabled -> {
                    _state.update {
                        it.copy(
                            isBluetoothEnabled = true,
                            isBluetoothEnableRequested = false
                        )
                    }
                    onIntent(ConnectMessage.StartDiscovery)
                }
                is ConnectMessage.BluetoothEnableDenied -> {
                    _state.update { it.copy(isBluetoothEnableRequested = false) }
                    _effect.send(
                        ConnectEffect.ShowToast(R.string.bt_denied)
                    )
                }
                is ConnectMessage.SelectConnectionType -> selectConnectionType(message.type)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    private suspend fun selectConnectionType(type: ConnectionType) {
        _state.update { it.copy(selectedConnectionType = type) }
        when (type) {
            ConnectionType.Wifi -> {
                _effect.send(ConnectEffect.ShowToast(R.string.wifi_not_implemented))
            }
            ConnectionType.Bluetooth -> {
                if (!bluetoothService.isBluetoothEnabled()) {
                    _state.update { it.copy(isBluetoothEnableRequested = true) }
                    onIntent(ConnectMessage.ToggleBluetooth)
                } else {
                    _state.update { it.copy(isBluetoothEnabled = true) }
                    _state.update { it.copy(isBluetoothEnableRequested = false) }
                    onIntent(ConnectMessage.StartDiscovery)
                }
            }
            ConnectionType.BluetoothLe -> {
                _effect.send(ConnectEffect.ShowToast(R.string.ble_not_implemented))
            }
        }
    }

    private suspend fun toggleBluetooth() {
        if (!bluetoothService.isBluetoothSupported()) {
            _effect.send(ConnectEffect.ShowToast(R.string.bt_not_supported))
            return
        }
        if (!bluetoothService.isBluetoothEnabled()) {
            val intent = try {
                bluetoothService.enableBluetooth()
            } catch (e: IllegalStateException) {
                e.message?.let { Log.e("ConnectViewModel", it) }
                return
            }
            _effect.send(ConnectEffect.RequestBluetoothEnable(intent))
//            if (bluetoothService.isBluetoothEnabled()) {
//                _state.update { it.copy(isBluetoothEnabled = true) }
//            }
        } else {
            _state.update { it.copy(isBluetoothEnabled = true) }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startDiscovery() {
        if (bluetoothService.isBluetoothEnabled()) {
            _state.update { it.copy(isLoading = true, discoveredDevices = emptyList()) }
            bluetoothService.startDiscovery()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice(device: BluetoothDevice) {
        if (_state.value.connectionStatus == ConnectionStatus.Connecting) return
        _state.update { it.copy(connectingDeviceAddress = device.address) }
        Log.d("ConnectViewModel", "здесь попытка подключения")
        bluetoothService.connectToDevice(device)
    }

//    private fun sendData(data: ByteArray) {
//        bluetoothService.sendData(data)
//    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun disconnect() {
        bluetoothService.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onCleared() {
        super.onCleared()
//        bluetoothService.release()
    }

}