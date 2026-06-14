package com.nikolayux.masterchariot.feature.functions.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.Navigation
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.ui.theme.Black
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import com.nikolayux.masterchariot.ui.theme.White

private val CompactScreenPadding = 12.dp
private val ExpandedScreenPadding = 20.dp
private val CompactSpacing = 12.dp
private val ExpandedSpacing = 16.dp
private val ConnectionBannerHeight = 48.dp
private val ButtonHeight = 56.dp
private val SettingsButtonWidth = 64.dp
private val DtcRefreshButtonWidth = 52.dp
private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun FunctionScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: FunctionViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val state by viewModel.state.collectAsState()

    FunctionScreen(
        modifier = modifier,
        state = state,
        onRefreshDtc = viewModel::loadDtcCodes,
        navController = navController
    )
}

@Composable
fun FunctionScreen(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Scaffold { contentPadding ->
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            val isWide = maxWidth >= 600.dp
            val isShort = maxHeight < 560.dp
            val screenPadding = if (isWide) ExpandedScreenPadding else CompactScreenPadding
            val spacing = if (isWide) ExpandedSpacing else CompactSpacing

            if (isShort) {
                FunctionScrollableContent(
                    state = state,
                    onRefreshDtc = onRefreshDtc,
                    navController = navController,
                    screenPadding = screenPadding,
                    spacing = spacing,
                    isWide = isWide
                )
            } else {
                FunctionFullScreenContent(
                    state = state,
                    onRefreshDtc = onRefreshDtc,
                    navController = navController,
                    screenPadding = screenPadding,
                    spacing = spacing,
                    isWide = isWide
                )
            }
        }
    }
}

@Composable
private fun FunctionFullScreenContent(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    navController: NavController,
    screenPadding: Dp,
    spacing: Dp,
    isWide: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!state.isConnected) {
            ConnectionBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ConnectionBannerHeight)
            )
        }

        SpeedPanel(
            state = state,
            onRefreshDtc = onRefreshDtc,
            navController = navController,
            isWide = isWide,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )

        BottomPanel(
            state = state,
            navController = navController,
            isWide = isWide,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
        )
    }
}

@Composable
private fun FunctionScrollableContent(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    navController: NavController,
    screenPadding: Dp,
    spacing: Dp,
    isWide: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(screenPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!state.isConnected) {
            ConnectionBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ConnectionBannerHeight)
            )
        }

        SpeedPanel(
            state = state,
            onRefreshDtc = onRefreshDtc,
            navController = navController,
            isWide = isWide,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isWide) 260.dp else 320.dp)
        )

        BottomPanel(
            state = state,
            navController = navController,
            isWide = isWide,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isWide) 180.dp else 220.dp)
        )
    }
}

@Composable
private fun ConnectionBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.ui_not_connected),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SpeedPanel(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    navController: NavController,
    isWide: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SpeedValue(
                    state = state,
                    modifier = Modifier.weight(1.2f)
                )

                MetricActions(
                    state = state,
                    onRefreshDtc = onRefreshDtc,
                    navController = navController,
                    modifier = Modifier.weight(1f),
                    vertical = true
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                SpeedValue(
                    state = state,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                MetricActions(
                    state = state,
                    onRefreshDtc = onRefreshDtc,
                    navController = navController,
                    modifier = Modifier.fillMaxWidth(),
                    vertical = false
                )
            }
        }
    }
}

@Composable
private fun SpeedValue(
    state: FunctionState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.speed.toString(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge,
            maxLines = 1
        )
        Text(
            text = if (state.isUsingMiles) {
                stringResource(R.string.car_speed_miles)
            } else {
                stringResource(R.string.car_speed_km)
            },
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
private fun MetricActions(
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    vertical: Boolean
) {
    if (vertical) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            RpmButton(
                rpm = state.rpm,
                onClick = { navController.navigate(Navigation.InstrumentPanel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ButtonHeight)
            )
            DtcButton(
                count = state.dtcCodes.size,
                isLoading = state.isLoadingDtc,
                onOpen = { navController.navigate(Navigation.DtcErrors) },
                onRefresh = onRefreshDtc,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(ButtonHeight)
            )
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RpmButton(
                rpm = state.rpm,
                onClick = { navController.navigate(Navigation.InstrumentPanel) },
                modifier = Modifier
                    .weight(1f)
                    .height(ButtonHeight)
            )
            DtcButton(
                count = state.dtcCodes.size,
                isLoading = state.isLoadingDtc,
                onOpen = { navController.navigate(Navigation.DtcErrors) },
                onRefresh = onRefreshDtc,
                modifier = Modifier
                    .weight(1f)
                    .height(ButtonHeight)
            )
        }
    }
}

@Composable
private fun RpmButton(
    rpm: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = rpm.toString(),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1
            )
            Text(
                modifier = Modifier.padding(start = 6.dp, top = 2.dp),
                text = stringResource(R.string.car_rpm),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DtcButton(
    count: Int,
    isLoading: Boolean,
    onOpen: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .clickable(onClick = onOpen)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.padding(start = 6.dp, top = 2.dp),
                    text = stringResource(R.string.dtc),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(vertical = 10.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outline
                ) {}
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .width(DtcRefreshButtonWidth)
                    .fillMaxHeight(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
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
    }
}

@Composable
private fun BottomPanel(
    state: FunctionState,
    navController: NavController,
    isWide: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ServiceButton(
                    state = state,
                    onClick = { navController.navigate(Navigation.InstrumentPanel) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                ConnectAndSettingsRow(
                    state = state,
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                ServiceButton(
                    state = state,
                    onClick = { navController.navigate(Navigation.InstrumentPanel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                ConnectAndSettingsRow(
                    state = state,
                    navController = navController,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ServiceButton(
    state: FunctionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.until_maintenance),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.untilService.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 1
                )
                Text(
                    text = if (state.isUsingMiles) {
                        stringResource(R.string.car_measure_miles)
                    } else {
                        stringResource(R.string.car_measure_km)
                    },
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ConnectAndSettingsRow(
    state: FunctionState,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(ButtonHeight),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { navController.navigate(Navigation.Connect) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (state.isConnected) {
                    Icon(
                        imageVector = Icons.Default.BluetoothConnected,
                        contentDescription = null
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null
                    )
                }

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = if (state.isConnected) {
                        stringResource(R.string.connected)
                    } else {
                        stringResource(
                            R.string.connect
                        )
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            modifier = Modifier
                .height(ButtonHeight)
                .width(SettingsButtonWidth),
            onClick = { navController.navigate(Navigation.Settings) },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Black,
                contentColor = White,
                disabledContainerColor = Black,
                disabledContentColor = White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun FunctionScreenPreviewConnected() {
    MasterChariotTheme {
        FunctionScreen(
            FunctionState(
                speed = 87,
                rpm = 2450,
                isConnected = true,
                dtcCodes = listOf(
                    "P0300",
                    "P0171"
                ),
                isLoadingDtc = false,
                isUsingMiles = true,
            ),
            onRefreshDtc = { },
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun FunctionScreenPreviewDisconnected() {
    MasterChariotTheme {
        FunctionScreen(
            FunctionState(
                speed = 87,
                rpm = 2450,
                isConnected = false,
                dtcCodes = listOf(
                    "P0300",
                    "P0171"
                ),
                isLoadingDtc = false,
                isUsingMiles = false,
            ),
            onRefreshDtc = { },
        )
    }
}

@Preview(widthDp = 840, heightDp = 420)
@Composable
private fun FunctionScreenPreviewWideShort() {
    MasterChariotTheme {
        FunctionScreen(
            FunctionState(
                speed = 124,
                rpm = 3200,
                isConnected = false,
                dtcCodes = emptyList(),
                isLoadingDtc = true,
                isUsingMiles = false,
            ),
            onRefreshDtc = { },
        )
    }
}
