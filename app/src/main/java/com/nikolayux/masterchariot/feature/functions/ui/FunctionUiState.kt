package com.nikolayux.masterchariot.feature.functions.ui

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class FunctionUiState (
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val rpm: Float? = null,
    val speed: Float? = null,
    val coolantTemp: Float? = null,
    val engineLoad: Float? = null,
    val dtcCount: Int = 0,
    val lastUpdate: Long = 0,
    val error: String? = null
) : Parcelable