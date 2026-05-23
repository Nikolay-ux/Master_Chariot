package com.nikolayux.masterchariot.feature.car.ui

sealed interface CarListMessage {
    data class AddCar(val car: CarUiModel) : CarListMessage
    data class Edit(val car: CarUiModel) : CarListMessage
    data class Delete(val id: Int) : CarListMessage
    data object DeleteAll : CarListMessage
}