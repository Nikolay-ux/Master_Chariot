package com.nikolayux.masterchariot.feature.maintenance.data

import com.nikolayux.masterchariot.data.local.MaintenanceRecordDao
import com.nikolayux.masterchariot.data.local.MaintenanceRecordEntity
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRecord
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MaintenanceRepositoryImpl(
    private val dao: MaintenanceRecordDao
) : MaintenanceRepository {
    override fun getRecordsByCar(carId: Int): Flow<List<MaintenanceRecord>> {
        return dao.getRecordsByCar(carId).map { records ->
            records.map(MaintenanceRecordEntity::toDomain)
        }
    }

    override suspend fun addRecord(record: MaintenanceRecord) {
        dao.insert(MaintenanceRecordEntity.fromDomain(record))
    }

    override suspend fun deleteRecord(recordId: Int) {
        dao.deleteById(recordId)
    }
}
