package com.nikolayux.masterchariot.feature.car.list.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.data.local.AppDatabase
import com.nikolayux.masterchariot.feature.car.data.CarRepositoryImpl
import com.nikolayux.masterchariot.feature.car.domain.Car
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import com.nikolayux.masterchariot.feature.car.list.state.AddNewCarState
//import com.nikolayux.masterchariot.feature.car.list.state.CarListEffect
import com.nikolayux.masterchariot.feature.car.list.state.CarListMessage
import com.nikolayux.masterchariot.feature.car.list.state.CarListState
import com.nikolayux.masterchariot.feature.car.list.state.CarUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CarViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository: CarRepository =
        CarRepositoryImpl(AppDatabase.getInstance(application).carDao)

    var state by mutableStateOf(CarListState())
        private set

    init {
        viewModelScope.launch {
            repository.cars.collect {
                state = state.copy(cars = it.map(CarUiModel.Companion::fromDomain))
            }
        }
    }

//    private val _effects = MutableSharedFlow<CarListEffect>(extraBufferCapacity = 1)
//    val effects = _effects.asSharedFlow()

    fun action(message: CarListMessage) {
        when (message) {
            is CarListMessage.AddCar -> {
                state = state.copy(addNewCarState = AddNewCarState())
            }

            is CarListMessage.DismissAddCarDialog -> {
                state = state.copy(addNewCarState = null)
            }

            is CarListMessage.Edit -> updateCar(message.car)
            is CarListMessage.Delete -> viewModelScope.launch {
                repository.deleteCar(message.id)
            }

            CarListMessage.DeleteAll -> viewModelScope.launch {
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

            CarListMessage.SaveNewCar -> {
                val newData = state.addNewCarState ?: return
                viewModelScope.launch {
                    state = state.copy(addNewCarState = newData.copy(isSaving = true))
                    val car = CarUiModel(
                        name = state.addNewCarState?.name ?: "",
                        mileage = state.addNewCarState?.mileage ?: 0,
                        serviceInterval = state.addNewCarState?.serviceInterval ?: 7000,
                        isUsingMiles = state.addNewCarState?.isUsingMiles ?: false
                    )
                    repository.addCar(toCar(car))
                    state = state.copy(addNewCarState = null)
                }
            }
        }
    }

    fun toCar(car: CarUiModel) = Car(
        name = car.name,
        mileage = car.mileage,
        serviceInterval = car.serviceInterval,
        isUsingMiles = car.isUsingMiles
    )

    fun updateCar(car: CarUiModel) {
        viewModelScope.launch {
            val newCar = Car(
                id = car.id,
                name = car.name,
                mileage = car.mileage,
                serviceInterval = car.serviceInterval,
                isUsingMiles = car.isUsingMiles
            )
            repository.updateCar(newCar)
        }
    }


}