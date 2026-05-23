package com.nikolayux.masterchariot.feature.car.data

import com.nikolayux.masterchariot.data.local.CarDao
import com.nikolayux.masterchariot.data.local.CarEntity
import com.nikolayux.masterchariot.feature.car.domain.Car
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CarRepositoryImpl(
    private val carDao: CarDao
) : CarRepository {
    override val cars: Flow<List<Car>> = carDao.getAllCars().map {
        it.map(CarEntity::toDomain)
    }

    override suspend fun getCarById(id: Int): Car? {
        return carDao.getCarById(id)?.toDomain()
    }

    override suspend fun addCar(car: Car) {
        val newCar = CarEntity(
            id = 0,
            name = car.name,
            mileage = car.mileage,
            serviceInterval = car.serviceInterval,
            isUsingMiles = car.isUsingMiles
        )
        carDao.insert(newCar)
    }

    override suspend fun updateCar(car: Car) {
        val newCar = CarEntity(
            id = car.id,
            name = car.name,
            mileage = car.mileage,
            serviceInterval = car.serviceInterval,
            isUsingMiles = car.isUsingMiles
        )
        carDao.update(newCar)
    }

    override suspend fun deleteCar(id: Int) {
        carDao.deleteById(id)
    }

    override suspend fun deleteAllCars() {
        carDao.deleteAll()
    }
}