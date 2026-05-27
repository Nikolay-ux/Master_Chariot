package com.nikolayux.masterchariot.data.obd2

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.github.eltonvs.obd.command.ObdCommand
import com.github.eltonvs.obd.command.ObdRawResponse
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout


/**
На данный момент работают команды: для сброса адаптера и для выключения эхо режима (строки 86, 96)
Затем начинается выполнение следующей команды, данные в ответ не приходят, поток программы блокируется - это проблема.
Так как данные не придут программа стоит и никакой ошибки не происходит.
Еще нюанс НЕ работает взаимодействие с реализованной библиотекой kotlin-obd-api.
 Если со сканера не приходят и данные об инициализации, это тоже плохо, как раз они приходят нестабильно, думаю тут дело в сканере, не хочет часто отправлять команды и перегружается, для него это нормально
 Нужно смотреть на реализацию метода sendRawCommand, так как в нем располагается ключевая часть моей программы - корректные отправка и получение данных.
 Проблема заключается в нестабильности работы этого метода, но тут опять же можно спереть проблему на сканер, предположим, что он нестабильно отправляет команды и перегружается от частых запросов
 нейронка мне писала о такой проблеме "китайских" аналогов оригинального адаптера ELM327, которые в свою очередь не производятся. Производство завершилось кажется в 2022 году, компания закрылась.
 С большой вероятностью могу сказать, что подключение по Bluetooth выполняется корректно, и за него беспокоиться не стоит. Самая большая проблема я полагаю находится в этом файле.
 *
 */


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
            Log.d("Obd2Service", "Подписка на socketReady")
            bluetoothService.socketReady.collect { socket ->
                Log.d("Obd2Service", "Сокет получен, вызываю startObdConnection")
                startObdConnection(socket)
                Log.d("Obd2Service", "После startObdConnection, вызываю startPolling")
                startPolling(socket)
                Log.d("Obd2Service", "startPolling завершён")
            }
        }
        scope.launch {
            bluetoothService.connectionState.collect { status ->
                if (status != ConnectionStatus.Connected) {
                    stopPolling()
                    closeObdConnection()
                }
            }
        }
    }

    private suspend fun startObdConnection(socket: BluetoothSocket) {
//        val socket = bluetoothService.getConnectedSocket() ?: return
        val inputStream = socket.inputStream
        val outputStream = socket.outputStream
//        obdConnection = ObdDeviceConnection(inputStream, outputStream)
//        Log.d("Obd2Service", "obdConnection created: $obdConnection")
        Log.e("Connection", "Init connection")
        try {
            Log.d("Obd2Service", "Start execute AT Z")
            val response = sendRawCommand(socket, "AT Z")
//            val response = obdConnection?.run(ResetAdapterCommand())
            if (response.isNullOrEmpty()) {
                Log.e("Obd2Service", "AT Z не удался")
                return
            }
            Log.d("Obd2Service", "AT Z response: $response")
//            delay(1000)


            val echoResp = sendRawCommand(socket, "AT E0")
            if (echoResp.isNullOrEmpty()) {
                Log.e("Obd2Service", "AT E0 не удался")
                return
            }
//            delay(1000)


//            val lfResp = sendRawCommand(socket, "AT L0")
//            if (lfResp.isNullOrEmpty()) Log.e("Obd2Service", "AT L0 не удался")
//
//            delay(1000)
//
//            val headersResp = sendRawCommand(socket, "AT H0")
//            if (headersResp.isNullOrEmpty()) Log.e("Obd2Service", "AT H0 не удался")

//            delay(1000)

            val protoResp = sendRawCommand(socket, "AT SP 0")
            if (protoResp.isNullOrEmpty()) {
                Log.e("Obd2Service", "AT SP 0 не удался")
                return
            }

//            delay(1000)

            val supportedPids = sendRawCommand(socket, "01 00")
            if (supportedPids.isNullOrEmpty()) {
                Log.e("Obd2Service", "01 00 не удался")
                return
            }
            Log.d("Obd2Service", "01 00 ответ: $supportedPids")

//            var response = sendRawCommand("AT Z")
//            Log.d("Obd2Service", "AT Z response: $response")
//            delay(1000)
//            response = sendRawCommand("AT E0")
//            Log.d("Obd2Service", "AT E0 response: $response")
//            delay(500)
//            response = sendRawCommand("AT L0")
//            Log.d("Obd2Service", "AT L0 response: $response")
//            delay(500)
//            response = sendRawCommand("AT SP 0")
//            Log.d("Obd2Service", "AT SP 0 response: $response")
//            delay(500)
        } catch (e: Exception) {
            Log.e("Obd2Service", "Ошибка инициализации", e)
        }
//        withContext(Dispatchers.IO) {
//            obdConnection?.let { conn ->
//                try {
//                    Log.d("Connection", "Init connection")
//                    conn.run(ResetAdapterCommand())
//                    delay(1000)
//                    conn.run(SetEchoCommand(Switcher.OFF))
//                    delay(1000)
//                    conn.run(SetLineFeedCommand(Switcher.OFF))
//                    delay(1000)
//                    conn.run(SetHeadersCommand(Switcher.OFF))
//                    delay(1000)
//
//                } catch (e: Exception) {
//                    Log.e("Obd2Service", "Init error: ${e.message}")
//                }
//            }
//        }
//        Log.d("Obd2Service", "startObdConnection конец")
    }

    private suspend fun readRpm() {
        val result = withContext(Dispatchers.IO) {
            Log.d("Read", "Execute command")
            obdConnection?.run(RPMCommand())
        }
        Log.d("Read", "Result $result")

        result?.let {
            _rpm.value = it.value.toIntOrNull() ?: 0
        }
    }

    private suspend fun readSpeed() {
//        val result = withContext(Dispatchers.IO) {
//            obdConnection?.run(SpeedCommand())
//        }
//        result?.let {
//            _speed.value = it.value.toIntOrNull() ?: 0
//        }
        obdConnection?.let { conn ->
            try {
                val response = conn.run(RawAtCommand("AT"))
                Log.d("Obd2Service", "AT response: ${response.value}")
                if (response.value.contains("OK")) {
                    Log.d("Obd2Service", "ELM327 отвечает на AT")
                } else {
                    Log.e("Obd2Service", "ELM327 не отвечает на AT")
                }
            } catch (e: Exception) {
                Log.e("Obd2Service", "Ошибка при отправке AT", e)
            }
        } ?: Log.e("Obd2Service", "obdConnection = null, невозможно отправить AT")
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
    private suspend fun sendRawCommand(socket: BluetoothSocket, cmd: String): String? = withContext(Dispatchers.IO) {
        val outputStream = socket.outputStream
        val inputStream = socket.inputStream

        try {
            Log.d("Obd2Service", "Попытка выполнить команду")
            if (inputStream.available() > 0) {
                val garbage = ByteArray(inputStream.available())
                inputStream.read(garbage)
                Log.d("Obd2Service", "Очищен мусор из буфера перед командой $cmd: ${String(garbage)}")
            }

            val cmdWithCr = if (cmd.endsWith("\r")) cmd else "$cmd\r"
            outputStream.write(cmdWithCr.toByteArray(Charsets.US_ASCII))
            outputStream.flush()

            val responseBuilder = StringBuilder()

            try {
                withTimeout(5000) {
                    while (true) {
                        Log.d("Obd2Service", "Считывание ответа от сканера")
                        val c = inputStream.read()
                        if (c == -1) {
                            Log.e("Obd2Service", "Поток Bluetooth сокета закрылся во время чтения")
                            break
                        }

                        val char = c.toChar()
                        responseBuilder.append(char)

                        if (char == '>') {
                            break // Ответ от ELM327 полностью получен
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("Obd2Service", "Таймаут! Адаптер не ответил на команду: $cmd. Получено частично: '${responseBuilder.toString()}'")
                return@withContext null
            }

            val fullResponse = responseBuilder.toString().trim()
            Log.d("Obd2Service", "Raw response for $cmd: $fullResponse")

            return@withContext fullResponse.removeSuffix(">").trim()

        } catch (e: Exception) {
            Log.e("Obd2Service", "Ошибка выполнения команды $cmd", e)
            null
        }
    }



    private suspend fun readPid(socket: BluetoothSocket, pid: String): Int? {
        val response = sendRawCommand(socket,pid) ?: return null
        Log.d("Obd2Service", "PID $pid response: $response")
        val hexParts = response.split(" ").filter { it.matches(Regex("[0-9A-Fa-f]{2}")) }
        return when (pid) {
            "010C" -> {
                if (hexParts.size >= 4) {
                    val high = hexParts[2].toInt(16)
                    val low = hexParts[3].toInt(16)
                    (high * 256 + low) / 4
                } else null
            }
            "010D" -> {
                if (hexParts.size >= 3) {
                    hexParts[2].toInt(16)
                } else null
            }
            else -> null
        }
    }
    private fun startPolling(socket: BluetoothSocket) {
        Log.d("Obd2Service", "is polling $isPolling")
        if (isPolling) return
        isPolling = true
        scope.launch {
            Log.d("Obd2Service", "state ${bluetoothService.connectionState.value}")
            while (isPolling && bluetoothService.connectionState.value == ConnectionStatus.Connected) {
                val rpm = readPid(socket, "010C")
                if (rpm != null) {
                    _rpm.value = rpm
                    Log.d("Obd2Service", "RPM: $rpm")
                }
                delay(500)
                val speed = readPid(socket, "010D")
                if (speed != null) {
                    _speed.value = speed
                    Log.d("Obd2Service", "Speed: $speed")
                }
                delay(500)
            }
        }
    }
}

class RawAtCommand(
    private val fullCommand: String,
    override val tag: String = fullCommand,
    override val name: String = fullCommand
) : ObdCommand() {
    override val mode: String = if (fullCommand.contains(' ')) {
        fullCommand.substringBefore(' ')
    } else {
        fullCommand
    }
    override val pid: String = if (fullCommand.contains(' ')) {
        fullCommand.substringAfter(' ')
    } else {
        ""
    }

    override val handler: (ObdRawResponse) -> String = { raw ->
        Log.d("RawAtCommand", "CMD: $fullCommand, RESP: ${raw.value}")
        raw.value
    }
}

class RawObdCommand(
    private val command: String, // например "01 00"
    override val tag: String = command,
    override val name: String = command
) : ObdCommand() {
    override val mode: String = command.substringBefore(' ')
    override val pid: String = command.substringAfter(' ')
    override val handler: (ObdRawResponse) -> String = { raw ->
        Log.d("RawObdCommand", "CMD: $command, RESP: ${raw.value}")
        raw.value
    }
}