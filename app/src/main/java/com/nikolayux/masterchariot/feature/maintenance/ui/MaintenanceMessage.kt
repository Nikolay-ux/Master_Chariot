package com.nikolayux.masterchariot.feature.maintenance.ui

sealed interface MaintenanceMessage {
    data class CurrentMileageChanged(val value: String) : MaintenanceMessage
    data object SyncMileageClicked : MaintenanceMessage
    data class ServiceMileageChanged(val value: String) : MaintenanceMessage
    data object CompleteServiceClicked : MaintenanceMessage
    data class ActionSelected(val value: String) : MaintenanceMessage
    data class CustomActionChanged(val value: String) : MaintenanceMessage
    data class ActionMileageChanged(val value: String) : MaintenanceMessage
    data object AddRecordClicked : MaintenanceMessage
    data class DeleteRecordClicked(val id: Int) : MaintenanceMessage
    data object MessageShown : MaintenanceMessage
}
