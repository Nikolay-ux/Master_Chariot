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
    @ColumnInfo("isUsingMiles")
    val isUsingMiles: Boolean = false,
) {
    fun toDomain() = Car(
        id = id,
        name = name,
        mileage = mileage,
        serviceInterval = serviceInterval,
        isUsingMiles = isUsingMiles
    )
}