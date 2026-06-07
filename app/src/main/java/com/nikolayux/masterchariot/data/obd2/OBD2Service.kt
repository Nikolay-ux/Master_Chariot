package com.nikolayux.masterchariot.data.obd2

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.github.eltonvs.obd.command.Switcher
import com.github.eltonvs.obd.command.at.ResetAdapterCommand
import com.github.eltonvs.obd.command.at.SetEchoCommand
import com.github.eltonvs.obd.command.at.SetHeadersCommand
import com.github.eltonvs.obd.command.at.SetLineFeedCommand
import com.github.eltonvs.obd.command.control.TroubleCodesCommand
import com.github.eltonvs.obd.command.control.VINCommand
import com.github.eltonvs.obd.command.engine.LoadCommand
import com.github.eltonvs.obd.command.engine.MassAirFlowCommand
import com.github.eltonvs.obd.command.engine.RPMCommand
import com.github.eltonvs.obd.command.engine.SpeedCommand
import com.github.eltonvs.obd.command.temperature.EngineCoolantTemperatureCommand
import com.github.eltonvs.obd.connection.ObdDeviceConnection
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
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
    private val bluetoothService: BluetoothService,
    private val carRepository: CarRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isPolling = false
    private var obdConnection: ObdDeviceConnection? = null

    private val _speed = MutableStateFlow(0)
    val speed: StateFlow<Int> = _speed.asStateFlow()

    private val _rpm = MutableStateFlow(0)
    val rpm: StateFlow<Int> = _rpm.asStateFlow()

    private val _vin = MutableStateFlow<String?>(null)
    val vin: StateFlow<String?> = _vin.asStateFlow()

    private val _unknownVin = MutableStateFlow<String?>(null)
    val unknownVin: StateFlow<String?> = _unknownVin.asStateFlow()

    private val _coolantTemp = MutableStateFlow(0)
    val coolantTemp: StateFlow<Int> = _coolantTemp.asStateFlow()

    private val _engineLoad = MutableStateFlow(0)
    val engineLoad: StateFlow<Int> = _engineLoad.asStateFlow()

    private val _maf = MutableStateFlow(0f)
    val maf: StateFlow<Float> = _maf.asStateFlow()

    private val _fuelConsumption = MutableStateFlow(0f)
    val fuelConsumption: StateFlow<Float> = _fuelConsumption.asStateFlow()

    init {
        scope.launch {
            bluetoothService.socketReady.collect { socket ->

                val success = startObdConnection(socket)

                if (success) {
                    startPolling()
                }
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

    private suspend fun startObdConnection(
        socket: BluetoothSocket
    ): Boolean = withContext(Dispatchers.IO) {

        return@withContext try {

            obdConnection = ObdDeviceConnection(
                socket.inputStream, socket.outputStream
            )

            val conn = obdConnection ?: return@withContext false

            Log.d("OBD", "=== START INIT ===")

            conn.run(ResetAdapterCommand())
            Log.d("OBD", "AT Z OK")

            delay(2000)

            conn.run(SetEchoCommand(Switcher.OFF))
            Log.d("OBD", "AT E0 OK")

            delay(300)

            conn.run(SetLineFeedCommand(Switcher.OFF))
            Log.d("OBD", "AT L0 OK")

            delay(300)

            conn.run(SetHeadersCommand(Switcher.OFF))
            Log.d("OBD", "AT H0 OK")

            delay(300)

            readVin()

//            val supported = conn.run(AvailablePIDsCommand(
//                range = AvailablePIDsCommand.AvailablePIDsRanges.PIDS_01_TO_20
//            ))

//            Log.d(
//                "OBD",
//                "Supported PIDs: ${supported}"
//            )

            Log.d("OBD", "=== INIT SUCCESS ===")
            true

        } catch (e: Exception) {
            Log.e("OBD", "Initialization failed", e)
            false
        }
    }

    private suspend fun readRpm() {
        try {
            val result = withContext(Dispatchers.IO) {
                obdConnection?.run(RPMCommand())
//                obdConnection?.run(RawObdCommand("01 0C"))
            }
            Log.d("OBD", "RPM response = $result")
//            val raw = result?.value ?: return
            val rpmValue = parseRpm(result?.rawResponse?.value ?: "")
            if (rpmValue != null) {
                _rpm.value = rpmValue
            }
            Log.d("OBD", "Parsed RPM = ${_rpm.value}")
        } catch (e: Exception) {
            Log.e("OBD", "RPM read error", e)
        }
    }

    private suspend fun readSpeed() {
        try {
            val result = withContext(Dispatchers.IO) {
//                obdConnection?.run(RawObdCommand("01 0D"))
                obdConnection?.run(SpeedCommand())
            }
            Log.d("OBD", "Speed response = $result")

//            val raw = result?.value ?: return

            val speedValue = parseSpeed(result?.rawResponse?.value ?: "")

            if (speedValue != null) {
                _speed.value = speedValue
            }
            Log.d("OBD", "Parsed Speed = ${_speed.value}")

        } catch (e: Exception) {
            Log.e("OBD", "Speed read error", e)
        }
    }

    private fun parseRpm(raw: String): Int? {
        val match = Regex("41\\s+0C\\s+([0-9A-F]{2})\\s+([0-9A-F]{2})").find(raw)
        if (match != null) {
            val a = match.groupValues[1].toInt(16)
            val b = match.groupValues[2].toInt(16)
            return ((a * 256) + b) / 4
        }
        return null
    }

    private fun parseSpeed(raw: String): Int? {
        val match = Regex("41\\s+0D\\s+([0-9A-F]{2})").find(raw)
        return match?.groupValues?.get(1)?.toInt(16)
    }

    private suspend fun readCoolantTemp() {
        try {
            val result = withContext(Dispatchers.IO) {
                obdConnection?.run(EngineCoolantTemperatureCommand())
            }
            Log.d("OBD", "Coolant response = $result")
            val value =
                parseCoolantTemp(result?.rawResponse?.value ?: "")
            value?.let {
                _coolantTemp.value = it
            }
        } catch (e: Exception) {
            Log.e("OBD", "Coolant read error", e)
        }
    }

    private fun parseCoolantTemp(raw: String): Int? {
        val match = Regex("41\\s+05\\s+([0-9A-F]{2})").find(raw)
        if (match != null) {
            val a = match.groupValues[1].toInt(16)
            return a - 40
        }
        return null
    }

    private suspend fun readEngineLoad() {
        try {
            val result = withContext(Dispatchers.IO) {
                obdConnection?.run(LoadCommand())
            }
            val value =
                parseEngineLoad(
                    result?.rawResponse?.value ?: ""
                )
            value?.let {
                _engineLoad.value = it
            }
        } catch (e: Exception) {
            Log.e("OBD", "Engine load error", e)
        }
    }

    private fun parseEngineLoad(raw: String): Int? {
        val match = Regex("41\\s+04\\s+([0-9A-F]{2})").find(raw)
        if (match != null) {
            val a = match.groupValues[1].toInt(16)
            return (a * 100f / 255f).toInt()
        }
        return null
    }

    private suspend fun readMaf() {

        try {

            val result = withContext(Dispatchers.IO) {
                obdConnection?.run(MassAirFlowCommand())
            }

            val mafValue = parseMaf(
                result?.rawResponse?.value ?: ""
            )

            if (mafValue != null) {

                _maf.value = mafValue

                _fuelConsumption.value =
                    calculateFuelConsumption(
                        maf = mafValue,
                        speed = _speed.value
                    )
            }

        } catch (e: Exception) {
            Log.e("OBD", "MAF read error", e)
        }
    }

    private fun parseMaf(raw: String): Float? {
        val match = Regex("41\\s+10\\s+([0-9A-F]{2})\\s+([0-9A-F]{2})").find(raw)

        if (match != null) {
            val a = match.groupValues[1].toInt(16)
            val b = match.groupValues[2].toInt(16)
            return ((a * 256) + b) / 100f
        }
        return null
    }

    private fun calculateFuelConsumption(
        maf: Float,
        speed: Int
    ): Float {

        if (maf <= 0f || speed <= 0) {
            return 0f
        }

        val fuelRateLitersPerHour =
            (maf * 3600f) / (14.7f * 745f)

        return (fuelRateLitersPerHour / speed) * 100f
    }

    private fun updateFuelConsumption() {

        val maf = _maf.value
        val speed = _speed.value

        _fuelConsumption.value =
            calculateFuelConsumption(
                maf = maf,
                speed = speed
            )
    }

    private suspend fun readVin() {
        try {
            val result = withContext(Dispatchers.IO) {
                obdConnection?.run(VINCommand())
            }
            Log.d("OBD", "VIN response = $result")

            val vin = result?.value?.trim()
            if (!vin.isNullOrBlank()) {
                _vin.value = vin
                processVin(vin)
                Log.d("OBD", "VIN = $vin")
            }
        } catch (e: Exception) {
            Log.e("OBD", "VIN read error", e)
        }
    }

    private suspend fun processVin(vin: String) {
        val car = carRepository.getCarByVin(vin)
        if (car != null) {
            carRepository.selectCar(car.id)
            Log.d("OBD", "Car found: ${car.name}")
        } else {
            _unknownVin.value = vin
            Log.d("OBD", "Unknown VIN: $vin")
        }
    }

    suspend fun readDiagnosticTroubleCodes(): List<String>? = withContext(Dispatchers.IO) {
        try {
            obdConnection?.let { connection ->
                val response = connection.run(TroubleCodesCommand())
                val codes = response.value.split(" ").filter { it.isNotBlank() }
                return@withContext codes.ifEmpty { null }
            }
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

    private fun startPolling() {
        if (isPolling) return
        isPolling = true

        scope.launch {

            while (isPolling && bluetoothService.connectionState.value == ConnectionStatus.Connected) {
                readRpm()
                readSpeed()
                readCoolantTemp()
                readEngineLoad()
                readMaf()
                updateFuelConsumption()
            }
        }
    }
}
