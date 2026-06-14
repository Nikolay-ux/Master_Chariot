package com.nikolayux.masterchariot.feature.maintenance.ui

import androidx.compose.runtime.Immutable
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRecord

@Immutable
data class MaintenanceState(
    val carId: Int? = null,
    val carName: String = "",
    val currentMileage: Int = 0,
    val lastServiceMileage: Int = 0,
    val serviceInterval: Int = 0,
    val currentMileageInput: String = "",
    val serviceMileageInput: String = "",
    val selectedAction: String? = null,
    val customActionInput: String = "",
    val actionMileageInput: String = "",
    val records: List<MaintenanceRecord> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null
) {
    val kmUntilMaintenance: Int
        get() = maxOf(0, serviceInterval - (currentMileage - lastServiceMileage))
}
