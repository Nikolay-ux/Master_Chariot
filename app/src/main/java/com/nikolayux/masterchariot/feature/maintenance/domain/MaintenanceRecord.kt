package com.nikolayux.masterchariot.feature.maintenance.domain

data class MaintenanceRecord(
    val id: Int = 0,
    val carId: Int,
    val action: String,
    val mileage: Int,
    val createdAt: Long = System.currentTimeMillis()
)
