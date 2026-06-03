package com.nikolayux.masterchariot.feature.car.domain

data class Car(
    val id: Int = 0,
    val name: String,
    val mileage: Int,
    val serviceInterval: Int,
    val lastServiceMileage: Int,
    val isUsingMiles: Boolean,
    val vin: String? = null,
    val isSelected: Boolean = false
) {
    val kmUntilMaintenance: Int
        get() = serviceInterval - (mileage - lastServiceMileage)
}