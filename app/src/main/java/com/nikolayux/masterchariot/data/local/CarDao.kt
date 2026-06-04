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

    @Query("SELECT * FROM cars WHERE isSelected = 1 LIMIT 1")
    fun getSelectedCarFlow(): Flow<CarEntity?>

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

    @Query("""
    SELECT * FROM cars
    WHERE isSelected = 1
    LIMIT 1
""")
    suspend fun getSelectedCar(): CarEntity?

    @Query("""
    UPDATE cars
    SET isSelected = 0
""")
    suspend fun clearSelection()

    @Query("UPDATE cars SET isSelected = 1 WHERE id = :carId")
    suspend fun selectCar(carId: Int)

    @Query("SELECT * FROM cars WHERE vin = :vin LIMIT 1")
    suspend fun getCarByVin(vin: String): CarEntity?
}