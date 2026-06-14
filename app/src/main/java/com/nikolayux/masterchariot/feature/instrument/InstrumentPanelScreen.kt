package com.nikolayux.masterchariot.feature.instrument

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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.SettingsInputComponent
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import java.util.Locale

@Composable
fun InstrumentPanelScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: InstrumentPanelViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    InstrumentPanelScreen(
        state = state,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentPanelScreen(
    state: InstrumentPanelState,
    modifier: Modifier = Modifier
) {
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.instrument_panel)) },
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
            if (!state.isConnected) {
                item {
                    InstrumentStatusCard()
                }
            }
            item {
                InstrumentMainSpeedCard(state = state)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InstrumentMetricCard(
                        title = stringResource(R.string.instrument_rpm),
                        value = state.rpm.toString(),
                        unit = stringResource(R.string.car_rpm),
                        icon = Icons.Outlined.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    InstrumentMetricCard(
                        title = stringResource(R.string.instrument_engine_load),
                        value = state.engineLoad.toString(),
                        unit = "%",
                        icon = Icons.Default.Whatshot,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InstrumentMetricCard(
                        title = stringResource(R.string.instrument_coolant_temp),
                        value = state.coolantTemperature.toString(),
                        unit = "°C",
                        icon = Icons.Default.Thermostat,
                        modifier = Modifier.weight(1f)
                    )
                    InstrumentMetricCard(
                        title = stringResource(R.string.instrument_fuel_consumption),
                        value = String.format(Locale.US, "%.1f", state.fuelConsumption),
                        unit = stringResource(R.string.trip_fuel_consumption_unit),
                        icon = Icons.Default.LocalGasStation,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InstrumentStatusCard(
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
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun InstrumentMainSpeedCard(
    state: InstrumentPanelState,
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
            Text(
                text = state.speed.toString(),
                style = MaterialTheme.typography.displayLarge,
                maxLines = 1
            )
            Text(
                text = if (state.isUsingMiles) {
                    stringResource(R.string.car_speed_miles)
                } else {
                    stringResource(R.string.car_speed_km)
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InstrumentMetricCard(
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
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun InstrumentPanelScreenPreview() {
    MasterChariotTheme {
        InstrumentPanelScreen(
            InstrumentPanelState(
                speed = 87,
                rpm = 2450,
                coolantTemperature = 92,
                engineLoad = 38,
                fuelConsumption = 8.7f,
                isConnected = true
            )
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun InstrumentPanelScreenDarkPreview() {
    MasterChariotTheme {
        InstrumentPanelScreen(
            InstrumentPanelState(
                speed = 87,
                rpm = 2450,
                coolantTemperature = 92,
                engineLoad = 38,
                fuelConsumption = 8.7f,
                isConnected = false
            )
        )
    }
}