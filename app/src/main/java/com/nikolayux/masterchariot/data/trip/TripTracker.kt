package com.nikolayux.masterchariot.data.trip

import com.nikolayux.masterchariot.data.notification.MaintenanceNotifier
import com.nikolayux.masterchariot.data.obd2.Obd2Service
import com.nikolayux.masterchariot.feature.car.domain.CarRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class TripTracker @Inject constructor(
    private val obd2Service: Obd2Service,
    private val carRepository: CarRepository,
    private val maintenanceNotifier: MaintenanceNotifier
) {

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private val _tripState =
        MutableStateFlow(TripState())

    val tripState =
        _tripState.asStateFlow()

    private var trackingJob: Job? = null

    private var tripDistanceKm = 0.0
    private var tripTimeSeconds = 0L
    private var fuelConsumedLiters = 0.0

    private var currentSpeed = 0
    private var currentRpm = 0
    private var currentTemperature = 0
    private var currentLoad = 0

    private var currentMaf = 0f

    private var currentFuelConsumption = 0f

    fun start() {

        if (trackingJob?.isActive == true) {
            return
        }

        tripDistanceKm = 0.0
        tripTimeSeconds = 0
        fuelConsumedLiters = 0.0

        trackingJob = scope.launch {

            launch {
                obd2Service.speed.collect {
                    currentSpeed = it
                }
            }

            launch {
                obd2Service.rpm.collect {
                    currentRpm = it
                }
            }

            launch {
                obd2Service.coolantTemp.collect {
                    currentTemperature = it
                }
            }

            launch {
                obd2Service.engineLoad.collect {
                    currentLoad = it
                }
            }

            launch {
                obd2Service.maf.collect {
                    currentMaf = it
                }
            }

            launch {
                obd2Service.fuelConsumption.collect {
                    currentFuelConsumption = it
                }
            }

            while (true) {

                delay(1000.milliseconds)

                if (currentSpeed > 0) {
                    tripTimeSeconds++
                }

                tripDistanceKm += currentSpeed / 3600.0

                val averageSpeed =
                    if (tripTimeSeconds == 0L) {
                        0.0
                    } else {
                        tripDistanceKm /
                                (tripTimeSeconds / 3600.0)
                    }

                val fuelRateLitersPerHour =
                    (currentMaf * 3600f) /
                            (14.7f * 745f)

                fuelConsumedLiters +=
                    fuelRateLitersPerHour / 3600.0

                _tripState.value =
                    TripState(
                        distanceKm = tripDistanceKm,
                        durationSeconds = tripTimeSeconds,

                        averageSpeed = averageSpeed,

                        currentFuelConsumption =
                            currentFuelConsumption.toDouble(),

                        rpm = currentRpm,

                        coolantTemperature =
                            currentTemperature,

                        engineLoad =
                            currentLoad,

                        fuelConsumedLiters =
                            fuelConsumedLiters
                    )
            }
        }
    }

    suspend fun stop() {
        trackingJob?.cancel()
        saveMileage()
        trackingJob = null
    }

    private suspend fun saveMileage() {

        val selectedCar =
            carRepository.getSelectedCar()
                ?: return

        val additionalMileage =
            tripDistanceKm.roundToInt()

        if (additionalMileage <= 0) {
            return
        }

        val previousRemainingKm = selectedCar.serviceInterval -
                (selectedCar.mileage - selectedCar.lastServiceMileage)
        val updatedCar = selectedCar.copy(
            mileage = selectedCar.mileage + additionalMileage
        )
        val currentRemainingKm = updatedCar.serviceInterval -
                (updatedCar.mileage - updatedCar.lastServiceMileage)

        carRepository.updateCar(updatedCar)
        maintenanceNotifier.notifyIfNeeded(
            previousRemainingKm = previousRemainingKm,
            currentRemainingKm = currentRemainingKm
        )
    }

    fun getTripDistance(): Double {
        return tripDistanceKm
    }

    fun getTripTimeSeconds(): Long {
        return tripTimeSeconds
    }

    fun release() {
        scope.cancel()
    }
}