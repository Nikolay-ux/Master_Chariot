package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.runtime.Immutable

@Immutable
data class FunctionState(
    val speed: Int = 0,
    val rpm: Int = 0,
    val isConnected: Boolean = false,
    val dtcCodes: List<String> = emptyList(),
    val isLoadingDtc: Boolean = false,
    val isUsingMiles: Boolean = false,
    val vin: String? = null,
    val selectedCarName: String? = null,
    val untilService: Int = 7000
)