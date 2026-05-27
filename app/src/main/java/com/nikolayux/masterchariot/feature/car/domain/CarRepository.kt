package com.nikolayux.masterchariot.feature.car.domain

import kotlinx.coroutines.flow.Flow

interface CarRepository {
    val cars: Flow<List<Car>>
    suspend fun getCarById(id: Int): Car?
    suspend fun addCar(car: Car)
    suspend fun updateCar(car: Car)
    suspend fun deleteCar(id: Int)
    suspend fun deleteAllCars()
}