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

    override fun getSelectedCarFlow(): Flow<Car?> {
        return carDao.getSelectedCarFlow()
            .map { it?.toDomain() }
    }

    override suspend fun addCar(car: Car) {
        val id = carDao.insert(CarEntity.fromDomain(car)).toInt()
        carDao.clearSelection()
        carDao.selectCar(id)
    }

    override suspend fun updateCar(car: Car) {
        carDao.update(CarEntity.fromDomain(car))
    }

    override suspend fun deleteCar(id: Int) {
        carDao.deleteById(id)
    }

    override suspend fun deleteAllCars() {
        carDao.deleteAll()
    }

    override suspend fun selectCar(carId: Int) {
        carDao.clearSelection()
        carDao.selectCar(carId)
    }

    override suspend fun getSelectedCar(): Car? {
        return carDao.getSelectedCar()?.toDomain()
    }

    override suspend fun getCarByVin(vin: String): Car? {
        return carDao.getCarByVin(vin)?.toDomain()
    }
}