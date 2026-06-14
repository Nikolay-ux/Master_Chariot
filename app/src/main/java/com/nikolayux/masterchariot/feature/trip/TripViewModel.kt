package com.nikolayux.masterchariot.feature.trip

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikolayux.masterchariot.data.bluetooth.BluetoothService
import com.nikolayux.masterchariot.data.trip.TripTracker
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripTracker: TripTracker,
    private val bluetoothService: BluetoothService
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            TripScreenState()
        )

    val state =
        _state.asStateFlow()

    init {

        viewModelScope.launch {

            tripTracker.tripState.collect { trip ->

                _state.update { currentState ->
                    currentState.copy(

                        distanceKm =
                            String.format(
                                "%.2f",
                                trip.distanceKm
                            ),

                        duration =
                            formatDuration(
                                trip.durationSeconds
                            ),

                        averageSpeed =
                            String.format(
                                "%.1f",
                                trip.averageSpeed
                            ),

                        currentFuelConsumption =
                            String.format(
                                "%.1f",
                                trip.currentFuelConsumption
                            ),

                        fuelConsumedLiters =
                            String.format(
                                "%.2f",
                                trip.fuelConsumedLiters
                            )
                    )
                }
            }
        }

        viewModelScope.launch {
            bluetoothService.connectionState.collect { status ->
                _state.update { it.copy(isConnected = status == ConnectionStatus.Connected) }
            }
        }
    }

    private fun formatDuration(
        seconds: Long
    ): String {

        val hours =
            seconds / 3600

        val minutes =
            (seconds % 3600) / 60

        val secs =
            seconds % 60

        return "%02d:%02d:%02d".format(
            hours,
            minutes,
            secs
        )
    }
}