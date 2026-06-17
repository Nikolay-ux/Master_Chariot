package com.nikolayux.masterchariot.feature.maintenance.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.domain.Car
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRecord
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val maintenanceRepository: MaintenanceRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(MaintenanceState())
    val state = _state.asStateFlow()

    private var selectedCar: Car? = null

    init {
        viewModelScope.launch {
            carRepository.getSelectedCarFlow().collect { car ->
                selectedCar = car
                _state.update { state ->
                    if (car == null) {
                        MaintenanceState(message = state.message)
                    } else {
                        state.copy(
                            carId = car.id,
                            carName = car.name,
                            currentMileage = car.mileage,
                            lastServiceMileage = car.lastServiceMileage,
                            serviceInterval = car.serviceInterval,
                            currentMileageInput = car.mileage.toString(),
                            serviceMileageInput = car.mileage.toString(),
                            actionMileageInput = car.mileage.toString()
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            carRepository.getSelectedCarFlow()
                .flatMapLatest { car ->
                    car?.let { maintenanceRepository.getRecordsByCar(it.id) } ?: flowOf(emptyList())
                }
                .collect { records ->
                    _state.update { it.copy(records = records) }
                }
        }
    }

    fun onMessage(message: MaintenanceMessage) {
        when (message) {
            is MaintenanceMessage.CurrentMileageChanged -> {
                _state.update { it.copy(currentMileageInput = message.value.onlyDigits()) }
            }
            MaintenanceMessage.SyncMileageClicked -> syncMileage()
            is MaintenanceMessage.ServiceMileageChanged -> {
                _state.update { it.copy(serviceMileageInput = message.value.onlyDigits()) }
            }
            MaintenanceMessage.CompleteServiceClicked -> completeService()
            is MaintenanceMessage.ActionSelected -> {
                _state.update { it.copy(selectedAction = message.value) }
            }
            is MaintenanceMessage.CustomActionChanged -> {
                _state.update { it.copy(customActionInput = message.value) }
            }
            is MaintenanceMessage.ActionMileageChanged -> {
                _state.update { it.copy(actionMileageInput = message.value.onlyDigits()) }
            }
            MaintenanceMessage.AddRecordClicked -> addMaintenanceRecord()
            is MaintenanceMessage.DeleteRecordClicked -> deleteRecord(message.id)
            MaintenanceMessage.MessageShown -> {
                _state.update { it.copy(message = null) }
            }
        }
    }

    private fun syncMileage() {
        val car = selectedCar ?: return
        val mileage = _state.value.currentMileageInput.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            carRepository.updateCar(car.copy(mileage = mileage))
            _state.update { it.copy(isSaving = false, message = R.string.maintenance_message_mileage_updated) }
        }
    }

    private fun completeService() {
        val car = selectedCar ?: return
        val mileage = _state.value.serviceMileageInput.toIntOrNull() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            carRepository.updateCar(
                car.copy(
                    mileage = maxOf(car.mileage, mileage),
                    lastServiceMileage = mileage
                )
            )
            maintenanceRepository.addRecord(
                MaintenanceRecord(
                    carId = car.id,
                    action = context.getString(R.string.maintenance_action_oil),
                    mileage = mileage
                )
            )
            _state.update { it.copy(isSaving = false, message = R.string.maintenance_message_service_completed) }
        }
    }

    private fun addMaintenanceRecord() {
        val state = _state.value
        val carId = state.carId ?: return
        val action = if (state.selectedAction == OTHER_ACTION) {
            state.customActionInput.trim()
        } else {
            state.selectedAction.orEmpty().trim()
        }
        val mileage = state.actionMileageInput.toIntOrNull()

        if (action.isBlank() || mileage == null) {
            _state.update { it.copy(message = R.string.maintenance_message_fill_action_and_mileage) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            maintenanceRepository.addRecord(
                MaintenanceRecord(
                    carId = carId,
                    action = action,
                    mileage = mileage
                )
            )
            _state.update {
                it.copy(
                    isSaving = false,
                    selectedAction = null,
                    customActionInput = "",
                    actionMileageInput = it.currentMileage.toString(),
                    message = R.string.maintenance_message_record_added
                )
            }
        }
    }

    private fun deleteRecord(id: Int) {
        viewModelScope.launch {
            maintenanceRepository.deleteRecord(id)
        }
    }

    private fun String.onlyDigits(): String = filter(Char::isDigit)

    companion object {
        const val OTHER_ACTION = "__other__"
    }
}
