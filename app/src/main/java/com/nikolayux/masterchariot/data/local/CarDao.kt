package com.nikolayux.masterchariot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY id DESC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars WHERE id = :carId")
    suspend fun getCarById(carId: Int): CarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(car: CarEntity): Long

    @Update
    suspend fun update(car: CarEntity)

    @Query("DELETE FROM cars WHERE id = :carId")
    suspend fun deleteById(carId: Int)

    @Query("DELETE FROM cars")
    suspend fun deleteAll()
}