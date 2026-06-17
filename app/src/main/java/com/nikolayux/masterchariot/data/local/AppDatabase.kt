package com.nikolayux.masterchariot.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [CarEntity::class, MaintenanceRecordEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val carDao: CarDao
    abstract val maintenanceRecordDao: MaintenanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS maintenance_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        carId INTEGER NOT NULL,
                        action TEXT NOT NULL,
                        mileage INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(carId) REFERENCES cars(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_maintenance_records_carId ON maintenance_records(carId)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_database.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}