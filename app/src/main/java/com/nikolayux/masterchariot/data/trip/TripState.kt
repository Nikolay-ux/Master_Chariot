package com.nikolayux.masterchariot.data.trip

data class TripState(
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val averageSpeed: Double = 0.0,
    val currentFuelConsumption: Double = 0.0,
    val rpm: Int = 0,
    val coolantTemperature: Int = 0,
    val engineLoad: Int = 0,
    val fuelConsumedLiters: Double = 0.0
)
