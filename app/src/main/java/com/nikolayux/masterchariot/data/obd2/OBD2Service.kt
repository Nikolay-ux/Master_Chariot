package com.nikolayux.masterchariot.data.obd2

import android.util.Log
import com.github.eltonvs.obd.command.Switcher
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SetEchoCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class Obd2Service @Inject constructor(
    private val bluetoothService: BluetoothService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isPolling = false
    private var obdConnection: ObdDeviceConnection? = null

    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed.asStateFlow()

    private val _rpm = MutableStateFlow(0)
    val rpm: StateFlow<Int> = _rpm.asStateFlow()

    init {
        scope.launch {
            bluetoothService.connectionState.collect { status ->
                when (status) {
                    ConnectionStatus.Connected -> {
                        startObdConnection()
                        startPolling()
                    }
                    else -> {
                        stopPolling()
                        closeObdConnection()
                    }
                }
            }
        }
    }

    private suspend fun startObdConnection() {
        val socket = bluetoothService.getConnectedSocket() ?: return
        val inputStream = socket.inputStream
        val outputStream = socket.outputStream
        obdConnection = ObdDeviceConnection(inputStream, outputStream)
        withContext(Dispatchers.IO) {
            obdConnection?.let { conn ->
                try {
                    conn.run(ResetAdapterCommand())
                    conn.run(SetEchoCommand(Switcher.OFF))
                } catch (e: Exception) {
                    Log.e("Obd2Service", "Init error: ${e.message}")
                }
            }
        }
    }

    private suspend fun startPolling() {
        if (isPolling) return
        isPolling = true
        scope.launch {
            while (isPolling && bluetoothService.connectionState.value == ConnectionStatus.Connected) {
                try {
                    readRpm()
                    delay(500)
                    readSpeed()
                    delay(500)
                } catch (e: Exception) {
                    delay(1000)
                }
            }
        }
    }

    private suspend fun readRpm() {
        val result = withContext(Dispatchers.IO) {
            obdConnection?.run(RPMCommand())
        }
        result?.let {
            _rpm.value = it.value.toIntOrNull() ?: 0
        }
    }

    private suspend fun readSpeed() {
        val result = withContext(Dispatchers.IO) {
            obdConnection?.run(SpeedCommand())
        }
        result?.let {
            _speed.value = it.value.toIntOrNull() ?: 0
        }
    }

    suspend fun readDiagnosticTroubleCodes(): List<String>? = withContext(Dispatchers.IO) {
        try {
            obdConnection?.let { connection ->
                val response = connection.run(TroubleCodesCommand())
                val codes = response.value.split(" ").filter { it.isNotBlank() }
                return@withContext codes.ifEmpty { null }
            } ?: return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    fun stopPolling() {
        isPolling = false
    }

    private fun closeObdConnection() {
        obdConnection = null
    }
}