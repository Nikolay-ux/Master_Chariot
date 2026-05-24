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
    data object DismissAddCarDialog: CarListMessage
}