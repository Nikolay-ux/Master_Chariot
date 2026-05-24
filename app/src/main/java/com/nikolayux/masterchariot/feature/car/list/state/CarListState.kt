package com.nikolayux.masterchariot.feature.car.list.state

import com.nikolayux.masterchariot.feature.car.list.state.CarUiModel

data class CarListState(
    val cars: List<CarUiModel> = emptyList(),
    val addNewCarState: AddNewCarState? = null
)

data class AddNewCarState(
    val name: String = "",
    val mileage: Int = 0,
    val serviceInterval: Int = 7000,
    val isUsingMiles: Boolean = false,
    val isSaving: Boolean = false
)