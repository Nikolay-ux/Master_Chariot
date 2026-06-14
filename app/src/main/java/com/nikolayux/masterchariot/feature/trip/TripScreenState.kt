package com.nikolayux.masterchariot.feature.trip

data class TripScreenState(
    val distanceKm: String = "0.00",
    val duration: String = "00:00:00",
    val averageSpeed: String = "0.0",
    val currentFuelConsumption: String = "0.0",
    val fuelConsumedLiters: String = "0.0",
    val isConnected: Boolean = false
)