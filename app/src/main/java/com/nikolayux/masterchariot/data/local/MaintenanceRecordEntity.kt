package com.nikolayux.masterchariot.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRecord

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carId")]
)
data class MaintenanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    val id: Int = 0,
    @ColumnInfo("carId")
    val carId: Int,
    @ColumnInfo("action")
    val action: String,
    @ColumnInfo("mileage")
    val mileage: Int,
    @ColumnInfo("createdAt")
    val createdAt: Long
) {
    fun toDomain() = MaintenanceRecord(
        id = id,
        carId = carId,
        action = action,
        mileage = mileage,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(record: MaintenanceRecord) = MaintenanceRecordEntity(
            id = record.id,
            carId = record.carId,
            action = record.action,
            mileage = record.mileage,
            createdAt = record.createdAt
        )
    }
}
