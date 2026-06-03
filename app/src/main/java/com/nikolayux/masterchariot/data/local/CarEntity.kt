package com.nikolayux.masterchariot.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nikolayux.masterchariot.feature.car.domain.Car

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("name")
    val name: String,
    @ColumnInfo("mileage")
    val mileage: Int,
    @ColumnInfo("serviceInterval")
    val serviceInterval: Int,
    @ColumnInfo("lastServiceMileage")
    val lastServiceMileage: Int = 0,
    @ColumnInfo("isUsingMiles")
    val isUsingMiles: Boolean = false,
    @ColumnInfo("vin")
    val vin: String? = null,
    @ColumnInfo("isSelected")
    val isSelected: Boolean = false
) {
    fun toDomain() = Car(
        id = id,
        name = name,
        mileage = mileage,
        serviceInterval = serviceInterval,
        lastServiceMileage = lastServiceMileage,
        isUsingMiles = isUsingMiles,
        vin = vin,
        isSelected = isSelected
    )

    companion object {
        fun fromDomain(car: Car) = CarEntity(
            id = car.id,
            name = car.name,
            mileage = car.mileage,
            serviceInterval = car.serviceInterval,
            lastServiceMileage = car.lastServiceMileage,
            isUsingMiles = car.isUsingMiles,
            vin = car.vin,
            isSelected = car.isSelected
        )
    }
}