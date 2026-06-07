package com.nikolayux.masterchariot.feature.trip

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.R
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TripScreenRoute(
    modifier: Modifier,
    viewModel: TripViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    TripScreen(modifier, viewModel.state)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    modifier: Modifier,
    state: StateFlow<TripScreenState>,
) {
    val currentState by state.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.instrument_panel))
                },
                navigationIcon = {
                    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                    IconButton(onClick = {
                        backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
//                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            TripInfoCard(
                title = "Расстояние поездки"
            ) {
                Text(
                    "${currentState.distanceKm} км"
                )
            }
            TripInfoCard(
                title = "Длительность"
            ) {
                Text(currentState.duration)
            }
            TripInfoCard(
                title = "Средняя скорость"
            ) {
                Text(
                    "${currentState.averageSpeed} км/ч"
                )
            }
            TripInfoCard(
                title = "Мгновенный расход"
            ) {
                Text(
                    "${currentState.currentFuelConsumption} л/100км"
                )
            }
            TripInfoCard(
                title = "Израсходовано топлива"
            ) {
                Text(
                    "${currentState.fuelConsumedLiters} л"
                )
            }
            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {
                TripInfoCard(
                    title = "RPM",
                    modifier = Modifier.weight(1f)
                ) {
                    Text(currentState.rpm)
                }
                TripInfoCard(
                    title = "Нагрузка",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("${currentState.engineLoad}%")
                }
            }
            TripInfoCard(
                title = "Температура ОЖ"
            ) {
                Text(
                    "${currentState.coolantTemperature} °C"
                )
            }
        }
    }
}

@Composable
fun TripInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )
            Spacer(
                Modifier.height(8.dp)
            )
            content()
        }
    }
}