package com.nikolayux.masterchariot.feature.car.list.state

sealed interface CarListMessage {
    data object AddCar : CarListMessage
    data class Edit(val car: CarUiModel) : CarListMessage
    data class Delete(val id: Int) : CarListMessage
    data object DeleteAll : CarListMessage
    data class NameChanged(val name: String) : CarListMessage
    data class MileageChanged(val mileage: Int) : CarListMessage
    data class IntervalChanged(val interval: Int) : CarListMessage
    data class MeasureChanged(val measure: Boolean) : CarListMessage
    data object SaveNewCar : CarListMessage
    data object UpdateCar : CarListMessage
    data object DismissAddCarDialog : CarListMessage
    data class LastServiceMileageChanged(val mileage: Int) : CarListMessage
    data class SelectCar(val id: Int) : CarListMessage
    data class UnknownVinDetected(val vin: String) : CarListMessage
    data object DismissUnknownVinDialog : CarListMessage
    data object CreateCarFromVin : CarListMessage
    data object ToggleNotifications : CarListMessage
}