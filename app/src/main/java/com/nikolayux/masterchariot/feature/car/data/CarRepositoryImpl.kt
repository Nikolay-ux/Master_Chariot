package com.nikolayux.masterchariot.feature.car.data

import com.nikolayux.masterchariot.data.local.CarDao
import com.nikolayux.masterchariot.data.local.CarEntity
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import kotlinx.coroutines.flow.Flow
import com.nikolayux.masterchariot.feature.car.domain.Car
import kotlinx.coroutines.flow.map

class CarRepositoryImpl(
    private val carDao: CarDao
) : CarRepository {

    override fun getAllCars(): Flow<List<Car>> {
        return carDao.getAllCars().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCarById(id: Int): Car? {
        return carDao.getCarById(id)?.toDomain()
    }

    override suspend fun addCar(car: Car) {
        carDao.insert(car.toEntity())
    }

    override suspend fun updateCar(car: Car) {
        carDao.update(car.toEntity())
    }

    override suspend fun deleteCar(id: Int) {
        carDao.deleteById(id)
    }

    override suspend fun deleteAllCars() {
        carDao.deleteAll()
    }

    private fun CarEntity.toDomain(): Car = Car(
        id = id,
        name = name,
        mileage = mileage,
        serviceInterval = serviceInterval,
        isUsingMiles = isUsingMiles
    )

    private fun Car.toEntity(): CarEntity = CarEntity(
        id = id,
        name = name,
        mileage = mileage,
        serviceInterval = serviceInterval,
        isUsingMiles = isUsingMiles
    )
}