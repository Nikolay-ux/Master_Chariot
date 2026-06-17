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
import android.util.Log
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

    private val _pairingCompleted = MutableSharedFlow<BluetoothDevice>()
    val pairingCompleted: SharedFlow<BluetoothDevice> = _pairingCompleted.asSharedFlow()

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothManager?.adapter
    }

    private val _btState = MutableStateFlow(bluetoothAdapter?.isEnabled)
    val btState: StateFlow<Boolean?> = _btState.asStateFlow()

    private var connectThread: ConnectThread? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _socketReady = MutableSharedFlow<BluetoothSocket>(replay = 1)
    val socketReady: SharedFlow<BluetoothSocket> = _socketReady.asSharedFlow()


    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val isEnabled = when (state) {
                    BluetoothAdapter.STATE_ON -> true
                    else -> false
                }
                _btState.value = isEnabled
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                if (device != null && bondState == BluetoothDevice.BOND_BONDED) {
                    serviceScope.launch {
                        _pairingCompleted.emit(device)
                    }
                }
            }
        }
    }

    private val discoveryReceiver = object : BroadcastReceiver() {
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
        val filterState = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val filterDiscovery = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(discoveryReceiver, filterDiscovery)
        val filterBond = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filterState, Context.RECEIVER_NOT_EXPORTED)
        context.registerReceiver(bondStateReceiver, filterBond, Context.RECEIVER_NOT_EXPORTED)
    }

    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun enableBluetooth(): Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun pair(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        }
    }

    fun startConnectThread(device: BluetoothDevice) {
        connectThread = ConnectThread(device).apply { start() }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        if (connectionState.value == ConnectionStatus.Connecting) return
        _connectionState.value = ConnectionStatus.Connecting
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            Log.d("Bluetooth service", "ну по идее должно работать сопряжение")
            pair(device)
        } else {
            Log.d("Bluetooth service", "вот это подключение сразу" + device.bondState)
            startConnectThread(device)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun disconnect() {
        serviceScope.launch {
            stopDiscovery()
            connectThread?.cancel()
            _connectionState.value = ConnectionStatus.Disconnected
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun release() {
        disconnect()
        context.unregisterReceiver(bluetoothStateReceiver)
        context.unregisterReceiver(discoveryReceiver)
        context.unregisterReceiver(bondStateReceiver)
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
                    serviceScope.launch {
                        _socketReady.emit(it)
                    }
                } catch (e: IOException) {
                    _connectionState.value = ConnectionStatus.Error
                    serviceScope.launch { _events.send(BluetoothEvent.Error("Connection failed: ${e.message}")) }
                    try {
                        it.close()
                    } catch (ex: IOException) { /* ignore */
                        ex.message?.let { msg -> Log.e("BluetoothService", msg) }
                    }
                }
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) { /* ignore */
                e.message?.let { msg -> Log.e("BluetoothService", msg) }
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