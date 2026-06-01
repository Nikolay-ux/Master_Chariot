package com.nikolayux.masterchariot.feature.functions.data

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.chaquo.python.PyObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * Репозиторий для работы с OBD2 адаптером через Python библиотеку obd.
 * Использует Chaquopy для вызова функций из obd_reader.py.
 */
class ObdRepository(private val context: Context) : Closeable {

    private val pythonModule: PyObject by lazy {
        // Инициализируем Python при первом обращении
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
        Python.getInstance().getModule("obd_reader")
    }

    // ======================== Управление подключением ========================

    /**
     * Подключиться к OBD2 адаптеру.
     * @param port опциональное имя порта (например, "COM3" или "/dev/ttyUSB0"). Если null — автоматический поиск.
     * @return true при успешном подключении
     */
    suspend fun connect(port: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = pythonModule.callAttr("connect", port)
            result.toBoolean()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Закрыть соединение с адаптером.
     */
    suspend fun disconnect(): Boolean = withContext(Dispatchers.IO) {
        try {
            pythonModule.callAttr("disconnect")
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Проверить, активно ли соединение.
     */
    suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            pythonModule.callAttr("is_connected").toBoolean()
        } catch (e: Exception) {
            false
        }
    }

    // ======================== Отдельные параметры ========================

    suspend fun getSpeed(): Float? = getFloatParam("get_speed")
    suspend fun getRpm(): Float? = getFloatParam("get_rpm")
    suspend fun getCoolantTemp(): Float? = getFloatParam("get_coolant_temp")
    suspend fun getEngineLoad(): Float? = getFloatParam("get_engine_load")
    suspend fun getIntakePressure(): Float? = getFloatParam("get_intake_pressure")
    suspend fun getMaf(): Float? = getFloatParam("get_maf")
    suspend fun getThrottlePos(): Float? = getFloatParam("get_throttle_pos")
    suspend fun getFuelLevel(): Float? = getFloatParam("get_fuel_level")
    suspend fun getRuntime(): Float? = getFloatParam("get_runtime")  // в секундах

    /**
     * Получить список кодов неисправностей (DTC).
     * @return список строк, например ["P0101", "P0300"] или пустой список, если ошибок нет.
     */
    suspend fun getDtcList(): List<String> = withContext(Dispatchers.IO) {
        try {
            val pyList = pythonModule.callAttr("get_dtc_list")
            if (pyList == null) return@withContext emptyList()
            // Преобразуем PyObject в List<String>
            pyList.asList().map { it.toString() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Вспомогательная функция для получения float-значений
    private suspend fun getFloatParam(funcName: String): Float? = withContext(Dispatchers.IO) {
        try {
            val result = pythonModule.callAttr(funcName)
            result?.toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ======================== Получение всех данных за раз ========================

    /**
     * Получить снимок всех поддерживаемых параметров (включая DTC).
     * @return ObdDataSnapshot или null при ошибке
     */
    suspend fun getAllData(): ObdDataSnapshot? = withContext(Dispatchers.IO) {
        try {
            val pyDict = pythonModule.callAttr("get_all_data") ?: return@withContext null

            ObdDataSnapshot(
                speed = pyDict["speed"]?.toDouble()?.toFloat(),
                rpm = pyDict["rpm"]?.toDouble()?.toFloat(),
                coolantTemp = pyDict["coolant_temp"]?.toDouble()?.toFloat(),
                engineLoad = pyDict["engine_load"]?.toDouble()?.toFloat(),
                intakePressure = pyDict["intake_pressure"]?.toDouble()?.toFloat(),
                maf = pyDict["maf"]?.toDouble()?.toFloat(),
                throttlePos = pyDict["throttle_pos"]?.toDouble()?.toFloat(),
                fuelLevel = pyDict["fuel_level"]?.toDouble()?.toFloat(),
                runtime = pyDict["runtime"]?.toDouble()?.toFloat(),
                dtc = pyDict["dtc"]?.asList()?.mapNotNull { it.toString() } ?: emptyList()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Поток данных, обновляющийся с заданным интервалом.
     * @param intervalMs интервал опроса в миллисекундах
     * @return Flow<ObdDataSnapshot>, который бесконечно эмитит данные.
     *         При ошибке соединения эмитит null или можно добавить обработку.
     */
    fun pollData(intervalMs: Long = 1000): Flow<ObdDataSnapshot?> = flow {
        while (true) {
            val snapshot = getAllData()
            emit(snapshot)
            kotlinx.coroutines.delay(intervalMs)
        }
    }

    /**
     * Закрыть репозиторий (вызывается, например, в onCleared() ViewModel).
     */
    override fun close() {
        // Принудительно отключаемся, если соединение активно
        // Можно вызвать disconnect, но это suspend, поэтому лучше в корутине
        // Оставим пользователю самому вызывать disconnect перед закрытием
    }
}
