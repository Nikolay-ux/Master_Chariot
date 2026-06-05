package com.nikolayux.masterchariot.feature.connect.state

import android.bluetooth.BluetoothDevice

interface ConnectMessage {
    data object ToggleBluetooth : ConnectMessage
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectMessage
    data object StartDiscovery : ConnectMessage
    data object StopDiscovery : ConnectMessage
    data object Disconnect : ConnectMessage
    data object BluetoothEnabled : ConnectMessage
    data object BluetoothEnableDenied : ConnectMessage
}