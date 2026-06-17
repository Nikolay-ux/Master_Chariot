package com.nikolayux.masterchariot.feature.car.list.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.data.notification.NotificationSettings
import com.nikolayux.masterchariot.data.obd2.Obd2Service
import com.nikolayux.masterchariot.feature.car.domain.Car
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import com.nikolayux.masterchariot.feature.car.list.state.AddNewCarState
import com.nikolayux.masterchariot.feature.car.list.state.CarListMessage
import com.nikolayux.masterchariot.feature.car.list.state.CarListState
import com.nikolayux.masterchariot.feature.car.list.state.CarUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CarViewModel @Inject constructor(
    private val repository: CarRepository,
    private val obd2Service: Obd2Service,
    private val notificationSettings: NotificationSettings
) : ViewModel() {

    var state by mutableStateOf(CarListState())
        private set

    init {
        viewModelScope.launch {
            repository.cars.collect {
                state = state.copy(cars = it.map(CarUiModel.Companion::fromDomain))
            }
            obd2Service.unknownVin.collect { vin ->
                if (!vin.isNullOrBlank()) {
                    state = state.copy(
                        unknownVin = vin
                    )
                }

            }
        }
        viewModelScope.launch {
            notificationSettings.maintenanceNotificationsEnabled.collect { enabled ->
                state = state.copy(notificationsEnabled = enabled)
            }
        }
    }

    fun action(message: CarListMessage) {
        when (message) {
            is CarListMessage.AddCar -> {
                state = state.copy(addNewCarState = AddNewCarState())
            }

            is CarListMessage.DismissAddCarDialog -> {
                state = state.copy(addNewCarState = null)
            }

            is CarListMessage.Edit -> {
                state = state.copy(
                    addNewCarState = AddNewCarState(
                        id = message.car.id,
                        name = message.car.name,
                        mileage = message.car.mileage,
                        serviceInterval = message.car.serviceInterval,
                        isUsingMiles = message.car.isUsingMiles,
                        isEdit = true,
                        lastServiceMileage = message.car.lastServiceMileage,
                    )
                )
            }

            is CarListMessage.Delete -> viewModelScope.launch {
                repository.deleteCar(message.id)
            }

            is CarListMessage.DeleteAll -> viewModelScope.launch {
                repository.deleteAllCars()
            }

            is CarListMessage.IntervalChanged -> {
                state =
                    state.copy(addNewCarState = state.addNewCarState?.copy(serviceInterval = message.interval))
            }

            is CarListMessage.MeasureChanged -> {
                state =
                    state.copy(addNewCarState = state.addNewCarState?.copy(isUsingMiles = message.measure))
            }

            is CarListMessage.MileageChanged -> {
                state =
                    state.copy(addNewCarState = state.addNewCarState?.copy(mileage = message.mileage))
            }

            is CarListMessage.NameChanged -> {
                state = state.copy(addNewCarState = state.addNewCarState?.copy(name = message.name))
            }

            is CarListMessage.SaveNewCar -> {
                saveCar()
            }

            is CarListMessage.UpdateCar -> {
                updateCar()
            }

            is CarListMessage.LastServiceMileageChanged -> {
                state = state.copy(
                    addNewCarState = state.addNewCarState?.copy(
                        lastServiceMileage = message.mileage
                    )
                )
            }
            is CarListMessage.SelectCar -> {
                viewModelScope.launch {
                    repository.selectCar(message.id)
                }
            }

            CarListMessage.CreateCarFromVin -> {
                val vin = state.unknownVin ?: return

                state = state.copy(
                    unknownVin = null,
                    addNewCarState = AddNewCarState(
                        vin = vin
                    )
                )
            }
            CarListMessage.DismissUnknownVinDialog -> {
                state = state.copy(
                    unknownVin = null
                )
            }
            is CarListMessage.UnknownVinDetected -> {
                state = state.copy(
                    unknownVin = message.vin
                )
            }
            CarListMessage.ToggleNotifications -> {
                notificationSettings.setMaintenanceNotificationsEnabled(!state.notificationsEnabled)
            }
        }
    }

    fun toCar(car: CarUiModel) = Car(
        id = car.id,
        name = car.name,
        mileage = car.mileage,
        serviceInterval = car.serviceInterval,
        isUsingMiles = car.isUsingMiles,
        lastServiceMileage = car.lastServiceMileage,
        vin = car.vin,
        isSelected = car.isSelected
    )

    fun saveCar() {
        val newData = state.addNewCarState ?: return
        viewModelScope.launch {
            state = state.copy(addNewCarState = newData.copy(isSaving = true))
            val car = CarUiModel(
                name = state.addNewCarState?.name ?: "",
                mileage = state.addNewCarState?.mileage ?: 0,
                serviceInterval = state.addNewCarState?.serviceInterval ?: 7000,
                isUsingMiles = state.addNewCarState?.isUsingMiles ?: false,
                lastServiceMileage = state.addNewCarState?.lastServiceMileage ?: 0,
                vin = state.addNewCarState?.vin
            )
            repository.addCar(toCar(car))
            state = state.copy(addNewCarState = null)
        }
    }

    fun updateCar() {
        val newData = state.addNewCarState ?: return
        viewModelScope.launch {
            state = state.copy(addNewCarState = newData.copy(isSaving = true))
            val car = CarUiModel(
                id = state.addNewCarState?.id ?: 0,
                name = state.addNewCarState?.name ?: "",
                mileage = state.addNewCarState?.mileage ?: 0,
                serviceInterval = state.addNewCarState?.serviceInterval ?: 7000,
                isUsingMiles = state.addNewCarState?.isUsingMiles ?: false,
                lastServiceMileage = state.addNewCarState?.lastServiceMileage ?: 0
            )
            repository.updateCar(toCar(car))
            state = state.copy(addNewCarState = null)
        }
    }
}