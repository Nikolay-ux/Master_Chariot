package com.nikolayux.masterchariot.feature.maintenance.domain

import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun getRecordsByCar(carId: Int): Flow<List<MaintenanceRecord>>
    suspend fun addRecord(record: MaintenanceRecord)
    suspend fun deleteRecord(recordId: Int)
}
