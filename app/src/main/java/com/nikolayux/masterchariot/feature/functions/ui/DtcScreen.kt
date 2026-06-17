package com.nikolayux.masterchariot.feature.functions.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun DtcScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: FunctionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    DtcScreen(
        state = state,
        onRefreshDtc = viewModel::loadDtcCodes,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DtcScreen(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(stringResource(R.string.dtc_errors_title)) },
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
                },
                actions = {
                    IconButton(
                        onClick = onRefreshDtc,
                        enabled = !state.isLoadingDtc
                    ) {
                        if (state.isLoadingDtc) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(10.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.dtc_refresh)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.dtcCodes.isEmpty() && !state.isLoadingDtc) {
            EmptyDtcContent(
                isConnected = state.isConnected,
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isLoadingDtc && state.dtcCodes.isEmpty()) {
                    item {
                        LoadingDtcCard()
                    }
                }

                items(
                    items = state.dtcCodes,
                    key = { it }
                ) { code ->
                    DtcCodeCard(code = code)
                }
            }
        }
    }
}

@Composable
private fun EmptyDtcContent(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.dtc_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (isConnected) {
                stringResource(R.string.dtc_empty_description)
            } else {
                stringResource(R.string.dtc_not_connected_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingDtcCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp
            )
            Text(
                text = stringResource(R.string.dtc_loading),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DtcCodeCard(
    code: String,
    modifier: Modifier = Modifier
) {
    val info = rememberDtcInfo(code)

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = info.code,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info.system,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = info.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = info.description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun rememberDtcInfo(code: String): DtcInfo {
    val normalizedCode = code.trim().uppercase()
    val knownTitle = knownDtcTitles[normalizedCode]
    val system = when (normalizedCode.firstOrNull()) {
        'P' -> stringResource(R.string.dtc_system_powertrain)
        'B' -> stringResource(R.string.dtc_system_body)
        'C' -> stringResource(R.string.dtc_system_chassis)
        'U' -> stringResource(R.string.dtc_system_network)
        else -> stringResource(R.string.dtc_system_unknown)
    }
    val title = knownTitle ?: stringResource(R.string.dtc_unknown_title)
    val description = knownTitle?.let {
        stringResource(R.string.dtc_known_description)
    } ?: stringResource(R.string.dtc_unknown_description)

    return DtcInfo(
        code = normalizedCode.ifBlank { stringResource(R.string.unknown) },
        system = system,
        title = title,
        description = description
    )
}

private data class DtcInfo(
    val code: String,
    val system: String,
    val title: String,
    val description: String
)

private val knownDtcTitles = mapOf(
    "P0100" to "Mass or Volume Air Flow Circuit",
    "P0101" to "Mass or Volume Air Flow Circuit Range/Performance",
    "P0102" to "Mass or Volume Air Flow Circuit Low Input",
    "P0103" to "Mass or Volume Air Flow Circuit High Input",
    "P0110" to "Intake Air Temperature Sensor Circuit",
    "P0115" to "Engine Coolant Temperature Sensor Circuit",
    "P0120" to "Throttle/Pedal Position Sensor Circuit",
    "P0130" to "Oxygen Sensor Circuit Bank 1 Sensor 1",
    "P0171" to "System Too Lean Bank 1",
    "P0172" to "System Too Rich Bank 1",
    "P0300" to "Random/Multiple Cylinder Misfire Detected",
    "P0301" to "Cylinder 1 Misfire Detected",
    "P0302" to "Cylinder 2 Misfire Detected",
    "P0303" to "Cylinder 3 Misfire Detected",
    "P0304" to "Cylinder 4 Misfire Detected",
    "P0325" to "Knock Sensor Circuit Bank 1",
    "P0335" to "Crankshaft Position Sensor Circuit",
    "P0340" to "Camshaft Position Sensor Circuit Bank 1",
    "P0420" to "Catalyst System Efficiency Below Threshold Bank 1",
    "P0430" to "Catalyst System Efficiency Below Threshold Bank 2",
    "P0440" to "Evaporative Emission Control System",
    "P0442" to "Evaporative Emission Control System Leak Detected",
    "P0455" to "Evaporative Emission Control System Leak Detected Gross Leak",
    "P0500" to "Vehicle Speed Sensor",
    "P0562" to "System Voltage Low",
    "P0700" to "Transmission Control System",
    "U0100" to "Lost Communication With ECM/PCM",
    "U0121" to "Lost Communication With Anti-Lock Brake System Control Module"
)

@Preview
@Composable
private fun DtcScreenPreview() {
    MasterChariotTheme {
        DtcScreen(
            state = FunctionState(
                isConnected = true,
                dtcCodes = listOf("P0300", "P0171", "U0100")
            ),
            onRefreshDtc = {}
        )
    }
}
