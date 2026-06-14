package com.nikolayux.masterchariot.feature.instrument

import androidx.compose.runtime.Immutable

@Immutable
data class InstrumentPanelState(
    val speed: Int = 0,
    val rpm: Int = 0,
    val coolantTemperature: Int = 0,
    val engineLoad: Int = 0,
    val fuelConsumption: Float = 0f,
    val isConnected: Boolean = false,
    val isUsingMiles: Boolean = false
)
