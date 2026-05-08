package com.nikolayux.masterchariot.data.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothService(private val context: Context) {
    private val _connectionState = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoveredDevices.asStateFlow()

    private val _receivedData = MutableSharedFlow<ByteArray>()
    val receivedData: SharedFlow<ByteArray> = _receivedData.asSharedFlow()

    private val _events = Channel<BluetoothEvent>()
    val events: ReceiveChannel<BluetoothEvent> = _events

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val currentList = _discoveredDevices.value.toMutableList()
                        if (!currentList.contains(it)) {
                            currentList.add(it)
                            _discoveredDevices.value = currentList
                        }
                    }
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun enableBluetooth(): Intent {
        return if (!isBluetoothEnabled()) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        } else {
            throw IllegalStateException("Bluetooth is already enabled")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery() {
        if (isBluetoothEnabled()) {
            _discoveredDevices.value = emptyList()
            bluetoothAdapter?.cancelDiscovery()
            bluetoothAdapter?.startDiscovery()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        bluetoothAdapter?.cancelDiscovery()
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (connectionState.value == ConnectionStatus.Connecting) return
        _connectionState.value = ConnectionStatus.Connecting
        connectThread = ConnectThread(device).apply { start() }
    }

    fun sendData(data: ByteArray) {
        connectedThread?.write(data)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun disconnect() {
        serviceScope.launch {
            stopDiscovery()
            connectThread?.cancel()
            connectedThread?.cancel()
            _connectionState.value = ConnectionStatus.Disconnected
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun release() {
        disconnect()
        context.unregisterReceiver(receiver)
        serviceScope.cancel()
    }
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? by lazy {
            try {
                device.createInsecureRfcommSocketToServiceRecord(SPP_UUID)
            } catch (e: IOException) {
                try {
                    device.createRfcommSocketToServiceRecord(SPP_UUID)
                } catch (e2: IOException) {
                    null
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun run() {
            bluetoothAdapter?.cancelDiscovery()
            socket?.let {
                try {
                    it.connect()
                    _connectionState.value = ConnectionStatus.Connected
                    connectedThread = ConnectedThread(it).apply { start() }
                } catch (e: IOException) {
                    _connectionState.value = ConnectionStatus.Error
                    serviceScope.launch { _events.send(BluetoothEvent.Error("Connection failed: ${e.message}")) }
                    try {
                        it.close()
                    } catch (ex: IOException) { /* ignore */
                    }
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) { /* ignore */
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream
        private val buffer = ByteArray(1024)

        @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
        override fun run() {
            while (_connectionState.value == ConnectionStatus.Connected) {
                val bytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    -1
                }
                if (bytes > 0) {
                    val data = buffer.copyOf(bytes)
                    serviceScope.launch { _receivedData.emit(data) }
                } else {
                    break
                }
            }
            disconnect()
        }

        fun write(data: ByteArray) {
            try {
                outputStream.write(data)
            } catch (e: IOException) {
                serviceScope.launch { _events.send(BluetoothEvent.Error("Send failed: ${e.message}")) }
            }
        }

        fun cancel() {
            try {
                socket.close()
            } catch (e: IOException) { /* ignore */
            }
        }
    }

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

}

sealed class BluetoothEvent {
    data class Error(val message: String) : BluetoothEvent()
}