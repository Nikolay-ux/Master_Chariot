package com.nikolayux.masterchariot.feature.connect.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.nikolayux.masterchariot.feature.connect.viewmodel.ConnectViewModel
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import kotlinx.coroutines.flow.StateFlow


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

    val bluetoothPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
    val notificationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
    }

    val bluetoothPermissionsState = rememberMultiplePermissionsState(bluetoothPermissions)

    LaunchedEffect(Unit) {
        if (!bluetoothPermissionsState.allPermissionsGranted) {
            bluetoothPermissionsState.launchMultiplePermissionRequest()
        }
    }

    if (notificationPermissions.isNotEmpty()) {
        val notificationPermissionsState = rememberMultiplePermissionsState(notificationPermissions)
        LaunchedEffect(Unit) {
            if (!notificationPermissionsState.allPermissionsGranted) {
                notificationPermissionsState.launchMultiplePermissionRequest()
            }
        }
    }

    if (!bluetoothPermissionsState.allPermissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(stringResource(R.string.bt_need_permissions))
            Button(onClick = { bluetoothPermissionsState.launchMultiplePermissionRequest() }) {
                Text(stringResource(R.string.bt_grant_permissions))
            }
        }
        return
    }

    ConnectScreen(viewModel.state, modifier, viewModel::onIntent)
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun ConnectScreen(
    state: StateFlow<ConnectState>,
    modifier: Modifier = Modifier,
    onIntent: (ConnectMessage) -> Unit = {},
) {
    val currentState by state.collectAsState()

    ConnectScreenContent(
        state = currentState,
        modifier = modifier,
        onIntent = onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
private fun ConnectScreenContent(
    state: ConnectState,
    modifier: Modifier = Modifier,
    onIntent: (ConnectMessage) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(stringResource(R.string.bt))
                },
                navigationIcon = {
                    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                    IconButton(onClick = {
                        backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (state.isBluetoothEnabled && !state.isLoading) {
                        TextButton(
                            onClick = { onIntent(ConnectMessage.StartDiscovery) }
                        ) {
                            Text(stringResource(R.string.bt_find))
                        }
                    } else if (state.isBluetoothEnabled) {
                        TextButton(
                            onClick = { onIntent(ConnectMessage.StopDiscovery) }
                        ) {
                            Text(stringResource(R.string.bt_stop_find))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BluetoothContent(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun BluetoothContent(
    state: ConnectState,
    modifier: Modifier = Modifier,
    onIntent: (ConnectMessage) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =
                        if (state.isBluetoothEnabled)
                            stringResource(R.string.bt_enabled)
                        else
                            stringResource(R.string.bt_disabled),
                    color =
                        if (state.isBluetoothEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                )
                if (
                    !state.isBluetoothEnabled &&
                    !state.isBluetoothEnableRequested
                ) {
                    Button(
                        onClick = {
                            onIntent(ConnectMessage.ToggleBluetooth)
                        }
                    ) {
                        Text(stringResource(R.string.bt_enable))
                    }
                }
            }
        }

        if (state.isBluetoothEnableRequested) {
            Text(stringResource(R.string.bt_wait))
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.isBluetoothEnabled) {
            when {
                state.isLoading && state.discoveredDevices.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.discoveredDevices.isEmpty() -> {
                    Row(modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(stringResource(R.string.bt_not_found))
                    }
                }

                else -> {
                    LazyColumn {
                        items(state.discoveredDevices) { device ->
                            DeviceItem(
                                device = device,
                                isConnecting =
                                    state.connectionStatus == ConnectionStatus.Connecting &&
                                            state.connectingDeviceAddress == device.address,
                                onClick = {
                                    onIntent(
                                        ConnectMessage.ConnectToDevice(device)
                                    )
                                }
                            )
                        }
                    }
                }
            }
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
        headlineContent = { Text(device.name ?: device.address) },
        supportingContent = { Text(device.address) },
        trailingContent = {
            if (isConnecting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.Default.Bluetooth, contentDescription = null)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(name = "Bluetooth disabled", showBackground = true)
@Composable
private fun ConnectScreenDisabledPreview() {
    MasterChariotTheme {
        ConnectScreenContent(
            state = ConnectState(isBluetoothEnabled = false),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Bluetooth search", showBackground = true)
@Composable
private fun ConnectScreenSearchingPreview() {
    MasterChariotTheme {
        ConnectScreenContent(
            state = ConnectState(
                isBluetoothEnabled = true,
                isLoading = true
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Bluetooth empty", showBackground = true)
@Composable
private fun ConnectScreenEmptyPreview() {
    MasterChariotTheme {
        ConnectScreenContent(
            state = ConnectState(isBluetoothEnabled = true),
            modifier = Modifier.fillMaxSize()
        )
    }
}
