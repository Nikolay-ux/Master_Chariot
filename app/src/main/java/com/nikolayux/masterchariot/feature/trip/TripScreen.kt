package com.nikolayux.masterchariot.feature.trip

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import kotlinx.coroutines.flow.MutableStateFlow
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
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text(stringResource(R.string.trip_tracker_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!currentState.isConnected) {
                item {
                    TripStatusCard()
                }
            }
            item {
                TripMainDistanceCard(state = currentState)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TripMetricCard(
                        title = stringResource(R.string.trip_duration),
                        value = currentState.duration,
                        unit = "",
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    TripMetricCard(
                        title = stringResource(R.string.trip_average_speed),
                        value = currentState.averageSpeed,
                        unit = stringResource(R.string.car_speed_km),
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TripMetricCard(
                        title = stringResource(R.string.trip_current_fuel_consumption),
                        value = currentState.currentFuelConsumption,
                        unit = stringResource(R.string.trip_fuel_consumption_unit),
                        icon = Icons.Default.LocalGasStation,
                        modifier = Modifier.weight(1f)
                    )
                    TripMetricCard(
                        title = stringResource(R.string.trip_fuel_consumed),
                        value = currentState.fuelConsumedLiters,
                        unit = stringResource(R.string.trip_fuel_liters_unit),
                        icon = Icons.Default.LocalGasStation,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TripStatusCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            text = stringResource(R.string.ui_not_connected),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun TripMainDistanceCard(
    state: TripScreenState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.trip_distance),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.distanceKm,
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.car_measure_km),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TripMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1
                )
                if (unit.isNotBlank()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun TripScreenPreview() {
    MasterChariotTheme {
        TripScreen(
            modifier = Modifier,
            state = MutableStateFlow(
                TripScreenState(
                    distanceKm = "12.45",
                    duration = "00:24:18",
                    averageSpeed = "42.1",
                    currentFuelConsumption = "8.4",
                    fuelConsumedLiters = "1.05"
                )
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun TripScreenDarkPreview() {
    MasterChariotTheme {
        TripScreen(
            modifier = Modifier,
            state = MutableStateFlow(
                TripScreenState(
                    distanceKm = "12.45",
                    duration = "00:24:18",
                    averageSpeed = "42.1",
                    currentFuelConsumption = "8.4",
                    fuelConsumedLiters = "1.05"
                )
            )
        )
    }
}
