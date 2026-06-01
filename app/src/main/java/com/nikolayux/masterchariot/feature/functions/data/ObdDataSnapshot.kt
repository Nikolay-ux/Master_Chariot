package com.nikolayux.masterchariot.feature.functions.data

data class ObdDataSnapshot(
    val speed: Float? = null,
    val rpm: Float? = null,
    val coolantTemp: Float? = null,
    val engineLoad: Float? = null,
    val intakePressure: Float? = null,
    val maf: Float? = null,
    val throttlePos: Float? = null,
    val fuelLevel: Float? = null,
    val runtime: Float? = null,
    val dtc: List<String> = emptyList()
)
