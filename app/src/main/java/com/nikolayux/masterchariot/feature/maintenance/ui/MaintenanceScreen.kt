package com.nikolayux.masterchariot.feature.maintenance.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.maintenance.domain.MaintenanceRecord
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun MaintenanceScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    MaintenanceScreen(
        state = state,
        onMessage = viewModel::onMessage,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    state: MaintenanceState,
    onMessage: (MaintenanceMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current

    LaunchedEffect(state.message) {
        if (state.message != null) {
            onMessage(MaintenanceMessage.MessageShown)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.maintenance_screen_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
            item {
                MaintenanceOverviewCard(state = state)
            }
            item {
                MileageSyncCard(
                    state = state,
                    onMessage = onMessage
                )
            }
            item {
                CompleteServiceCard(
                    state = state,
                    onMessage = onMessage
                )
            }
            item {
                AddMaintenanceRecordCard(
                    state = state,
                    onMessage = onMessage
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(R.string.maintenance_records_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            if (state.records.isEmpty()) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        text = stringResource(R.string.maintenance_records_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                items(state.records, key = { it.id }) { record ->
                    MaintenanceRecordCard(
                        record = record,
                        onDelete = { onMessage(MaintenanceMessage.DeleteRecordClicked(record.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MaintenanceOverviewCard(
    state: MaintenanceState,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = state.carName.ifBlank { stringResource(R.string.maintenance_no_selected_car) },
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.maintenance_current_mileage_value, state.currentMileage),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.maintenance_until_service_value, state.kmUntilMaintenance),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MileageSyncCard(
    state: MaintenanceState,
    onMessage: (MaintenanceMessage) -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maintenance_sync_mileage_title),
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.currentMileageInput,
                onValueChange = { onMessage(MaintenanceMessage.CurrentMileageChanged(it)) },
                label = { Text(stringResource(R.string.dialog_car_mileage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            Button(
                onClick = { onMessage(MaintenanceMessage.SyncMileageClicked) },
                enabled = !state.isSaving && state.carId != null
            ) {
                Icon(Icons.Default.Sync, null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.maintenance_sync_mileage_button)
                )
            }
        }
    }
}

@Composable
private fun CompleteServiceCard(
    state: MaintenanceState,
    onMessage: (MaintenanceMessage) -> Unit,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maintenance_complete_service_title),
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.serviceMileageInput,
                onValueChange = { onMessage(MaintenanceMessage.ServiceMileageChanged(it)) },
                label = { Text(stringResource(R.string.maintenance_service_mileage_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            Button(
                onClick = { onMessage(MaintenanceMessage.CompleteServiceClicked) },
                enabled = !state.isSaving && state.carId != null
            ) {
                Icon(Icons.Default.Done, null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.maintenance_complete_service_button)
                )
            }
        }
    }
}

@Composable
private fun AddMaintenanceRecordCard(
    state: MaintenanceState,
    onMessage: (MaintenanceMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = popularMaintenanceActions()
    val isOtherSelected = state.selectedAction == MaintenanceViewModel.OTHER_ACTION

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.maintenance_add_record_title),
                style = MaterialTheme.typography.headlineMedium
            )
            FlowRowCompat {
                actions.forEach { action ->
                    AssistChip(
                        onClick = { onMessage(MaintenanceMessage.ActionSelected(action)) },
                        label = { Text(action) },
                        leadingIcon = if (state.selectedAction == action) {
                            { Icon(Icons.Default.Done, null) }
                        } else null
                    )
                }
                AssistChip(
                    onClick = { onMessage(MaintenanceMessage.ActionSelected(MaintenanceViewModel.OTHER_ACTION)) },
                    label = { Text(stringResource(R.string.maintenance_action_other)) },
                    leadingIcon = if (isOtherSelected) {
                        { Icon(Icons.Default.Done, null) }
                    } else null
                )
            }
            if (isOtherSelected) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.customActionInput,
                    onValueChange = { onMessage(MaintenanceMessage.CustomActionChanged(it)) },
                    label = { Text(stringResource(R.string.maintenance_custom_action_label)) },
                    singleLine = true
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.actionMileageInput,
                onValueChange = { onMessage(MaintenanceMessage.ActionMileageChanged(it)) },
                label = { Text(stringResource(R.string.maintenance_action_mileage_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(50)
            )
            Button(
                onClick = { onMessage(MaintenanceMessage.AddRecordClicked) },
                enabled = !state.isSaving && state.carId != null
            ) {
                Icon(Icons.Default.Build, null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.maintenance_add_record_button)
                )
            }
        }
    }
}

@Composable
private fun FlowRowCompat(content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun popularMaintenanceActions(): List<String> = listOf(
    stringResource(R.string.maintenance_action_oil),
    stringResource(R.string.maintenance_action_oil_filter),
    stringResource(R.string.maintenance_action_air_filter),
    stringResource(R.string.maintenance_action_cabin_filter),
    stringResource(R.string.maintenance_action_spark_plugs),
    stringResource(R.string.maintenance_action_brake_pads),
    stringResource(R.string.maintenance_action_brake_fluid),
    stringResource(R.string.maintenance_action_coolant),
    stringResource(R.string.maintenance_action_ball_joint)
)

@Composable
private fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = record.action,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.maintenance_record_mileage_value, record.mileage),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}

@Preview(locale = "ru")
@Composable
private fun MaintenanceScreenPreview() {
    MasterChariotTheme {
        MaintenanceScreen(
            state = MaintenanceState(
                carId = 1,
                carName = "Toyota Camry",
                currentMileage = 124000,
                serviceInterval = 10000,
                lastServiceMileage = 118000,
                currentMileageInput = "124000",
                serviceMileageInput = "124000",
                actionMileageInput = "124000",
                records = listOf(
                    MaintenanceRecord(carId = 1, action = "Замена масла", mileage = 118000),
                    MaintenanceRecord(carId = 1, action = "Замена шаровой опоры", mileage = 123500)
                )
            ),
            onMessage = {}
        )
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MaintenanceScreenDarkPreview() {
    MasterChariotTheme {
        MaintenanceScreen(
            state = MaintenanceState(
                carId = 1,
                carName = "Toyota Camry",
                currentMileage = 124000,
                serviceInterval = 10000,
                lastServiceMileage = 118000,
                currentMileageInput = "124000",
                serviceMileageInput = "124000",
                actionMileageInput = "124000",
                records = listOf(
                    MaintenanceRecord(carId = 1, action = "Замена масла", mileage = 118000),
                    MaintenanceRecord(carId = 1, action = "Замена шаровой опоры", mileage = 123500)
                )
            ),
            onMessage = {}
        )
    }
}