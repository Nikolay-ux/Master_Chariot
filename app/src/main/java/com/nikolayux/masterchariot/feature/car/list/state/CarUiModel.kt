package com.nikolayux.masterchariot.feature.car.list.state

import com.nikolayux.masterchariot.feature.car.domain.Car

data class CarUiModel(
    val id: Int = 0,
    val name: String = "",
    val mileage: Int = 0,
    val serviceInterval: Int = 7000,
    val isUsingMiles: Boolean = false,
) {
    companion object {
        fun fromDomain(car: Car) = with(car) {
            CarUiModel(
                id = id,
                name = name,
                mileage = mileage,
                serviceInterval = serviceInterval,
                isUsingMiles = isUsingMiles
            )
        }
    }
}