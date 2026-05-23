package com.nikolayux.masterchariot.feature.car.ui

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
                state = state.copy(cars = it.map(CarUiModel::fromDomain))
            }
        }
    }

//    private val _effects = MutableSharedFlow<CarListEffect>(extraBufferCapacity = 1)
//    val effects = _effects.asSharedFlow()

    fun action(message: CarListMessage) {
        when (message) {
            is CarListMessage.AddCar -> addCar(message.car)
            is CarListMessage.Edit -> updateCar(message.car)
            is CarListMessage.Delete -> viewModelScope.launch {
                repository.deleteCar(message.id)
            }

            CarListMessage.DeleteAll -> viewModelScope.launch {
                repository.deleteAllCars()
            }
        }
    }

    fun addCar(car: CarUiModel) {
        viewModelScope.launch {
            val newCar = Car(
                name = car.name,
                mileage = car.mileage,
                serviceInterval = car.serviceInterval,
                isUsingMiles = car.isUsingMiles
            )
            repository.addCar(newCar)
        }
    }

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