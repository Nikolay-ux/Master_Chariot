package com.nikolayux.masterchariot.feature.connect.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.connect.state.ConnectEffect
import com.nikolayux.masterchariot.feature.connect.state.ConnectMessage
import com.nikolayux.masterchariot.feature.connect.state.ConnectState
import com.nikolayux.masterchariot.feature.connect.state.ConnectionStatus
import com.nikolayux.masterchariot.feature.connect.state.ConnectionType
import com.nikolayux.masterchariot.feature.connect.viewmodel.ConnectViewModel
import kotlinx.coroutines.flow.StateFlow


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@RequiresApi(Build.VERSION_CODES.S)
@RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConnectScreenRoute(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: ConnectViewModel = hiltViewModel(),
) {
    val context = LocalContext.current


    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onIntent(ConnectMessage.BluetoothEnabled)
        } else {
            viewModel.onIntent(ConnectMessage.BluetoothEnableDenied)
        }
    }

    val permissions = listOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )


    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ConnectEffect.Connected -> {
                    navController.popBackStack()
                }
                is ConnectEffect.ShowToast -> {
                    Toast.makeText(context, effect.messageResId, Toast.LENGTH_SHORT).show()
                }
                is ConnectEffect.RequestBluetoothEnable -> {
                    bluetoothEnableLauncher.launch(effect.intent)
                }
            }
        }
    }

    val permissionsState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    if (!permissionsState.allPermissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.bt_need_permissions))
            Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                Text(stringResource(R.string.bt_grant_permissions))
            }
        }
        return
    }

    ConnectScreen(viewModel.state, modifier, viewModel::onIntent)
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectScreen(
    state: StateFlow<ConnectState>,
    modifier: Modifier = Modifier,
    onIntent: (ConnectMessage) -> Unit = {},
) {
    val currentState by state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ConnectionType.entries.forEach { connectionType ->
                ConnectionTypeChip(
                    type = connectionType,
                    isSelected = currentState.selectedConnectionType == connectionType,
                    onClick = { onIntent(ConnectMessage.SelectConnectionType(connectionType)) }
                )
            }
        }
        when (currentState.selectedConnectionType) {
            ConnectionType.Wifi -> {
                Text(stringResource(R.string.wifi_not_implemented))
            }

            ConnectionType.Bluetooth -> {
                BluetoothContent(
                    state = currentState, onIntent = onIntent
                )
            }

            ConnectionType.BluetoothLe -> {
                Text(stringResource(R.string.ble_not_implemented))
            }
        }
    }
}

@Composable
fun ConnectionTypeChip(
    type: ConnectionType, isSelected: Boolean, onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(type.name) },
        modifier = Modifier,
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null)
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun BluetoothContent(
    state: ConnectState, onIntent: (ConnectMessage) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (state.isBluetoothEnabled) {
                    stringResource(R.string.bt_enabled)
                } else stringResource(R.string.bt_disabled),
                color = if (state.isBluetoothEnabled) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.error
            )
            if (!state.isBluetoothEnabled && !state.isBluetoothEnableRequested) {
                Button(onClick = { onIntent(ConnectMessage.ToggleBluetooth) }) {
                    Text(stringResource(R.string.bt_enable))
                }
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (state.isBluetoothEnabled) {
            if (state.discoveredDevices.isEmpty() && !state.isLoading) {
                Text(stringResource(R.string.bt_not_found))
                Button(onClick = { onIntent(ConnectMessage.StartDiscovery) }) {
                    Text(stringResource(R.string.bt_search))
                }
            } else {
                LazyColumn {
                    items(state.discoveredDevices) { device ->
                        DeviceItem(
                            device = device,
                            isConnecting = state.connectionStatus == ConnectionStatus.Connecting
                                    && state.connectingDeviceAddress == device.address,
                            onClick = { onIntent(ConnectMessage.ConnectToDevice(device)) }
                            )
                    }
                }
            }
        } else if (state.isBluetoothEnableRequested) {
            Text(stringResource(R.string.bt_wait))
        }
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun DeviceItem(
    device: BluetoothDevice,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent =  { Text(device.name ?: device.address) },
        supportingContent = { Text(device.address) },
        trailingContent = {
            if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon (Icons.Default.Bluetooth, contentDescription = null)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
