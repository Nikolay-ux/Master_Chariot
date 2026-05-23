package com.nikolayux.masterchariot.feature.car.domain

data class Car(
    val id: Int = 0,
    val name: String,
    val mileage: Int,
    val serviceInterval: Int,
    val isUsingMiles: Boolean
)