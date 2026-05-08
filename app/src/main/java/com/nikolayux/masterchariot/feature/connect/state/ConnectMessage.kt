package com.nikolayux.masterchariot.feature.connect.state

import android.bluetooth.BluetoothDevice

interface ConnectMessage {
    data object ToggleBluetooth : ConnectMessage
    data class ConnectToDevice(val device: BluetoothDevice) : ConnectMessage
    data object StartDiscovery : ConnectMessage
    data class SendData(val data: ByteArray) : ConnectMessage {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SendData

            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
    data object Disconnect : ConnectMessage
    data object BluetoothEnabled : ConnectMessage
    data object BluetoothEnableDenied : ConnectMessage
//    data object BluetoothNotSupported : ConnectMessage

    data class SelectConnectionType(val type: ConnectionType) : ConnectMessage
//    data object RequestBluetoothEnableManually : ConnectMessage
//    data object ConnectWifi : ConnectMessage
//    data object ConnectBle : ConnectMessage


}