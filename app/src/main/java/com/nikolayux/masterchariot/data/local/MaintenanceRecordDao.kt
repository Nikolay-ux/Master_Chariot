package com.nikolayux.masterchariot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Query("SELECT * FROM maintenance_records WHERE carId = :carId ORDER BY mileage DESC, createdAt DESC")
    fun getRecordsByCar(carId: Int): Flow<List<MaintenanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MaintenanceRecordEntity): Long

    @Query("DELETE FROM maintenance_records WHERE id = :recordId")
    suspend fun deleteById(recordId: Int)
}
