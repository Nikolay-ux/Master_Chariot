package com.nikolayux.masterchariot.feature.connect.state

import android.bluetooth.BluetoothDevice

data class ConnectState(
    val isBluetoothEnabled: Boolean = false,
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected,
    val lastReceivedData: ByteArray? = null,
    val errorMessage: String? = null,
    val isBluetoothEnableRequested: Boolean = false,
    val isLoading: Boolean = false,
    val connectingDeviceAddress: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectState

        if (isBluetoothEnabled != other.isBluetoothEnabled) return false
        if (isBluetoothEnableRequested != other.isBluetoothEnableRequested) return false
        if (isLoading != other.isLoading) return false
        if (discoveredDevices != other.discoveredDevices) return false
        if (connectionStatus != other.connectionStatus) return false
        if (!lastReceivedData.contentEquals(other.lastReceivedData)) return false
        if (errorMessage != other.errorMessage) return false
        if (connectingDeviceAddress != other.connectingDeviceAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isBluetoothEnabled.hashCode()
        result = 31 * result + isBluetoothEnableRequested.hashCode()
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + discoveredDevices.hashCode()
        result = 31 * result + connectionStatus.hashCode()
        result = 31 * result + (lastReceivedData?.contentHashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + (connectingDeviceAddress?.hashCode() ?: 0)
        return result
    }
}