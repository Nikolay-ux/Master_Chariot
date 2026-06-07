package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.Navigation
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun FunctionScreenRoute(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: FunctionViewModel = hiltViewModel(),
    navController: NavController = rememberNavController()
) {
    val state by viewModel.state.collectAsState()

    FunctionScreen(
        contentPadding = contentPadding,
        modifier = modifier,
        state = state,
        onRefreshDtc = viewModel::loadDtcCodes,
        navController = navController
    )
}

@Composable
fun FunctionScreen(
    contentPadding: PaddingValues,
    state: FunctionState,
    onRefreshDtc: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController()
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (true) {
                Card(
                    modifier = modifier
                        .fillMaxSize()
                        .weight(1F),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(50),

                    ) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${state.speed}", textAlign = TextAlign.Center)
                            Text(
                                if (state.isUsingMiles) {
                                    stringResource(R.string.car_speed_miles)
                                } else stringResource(R.string.car_speed_km)
                            )
                        }

                    }
                }
                Card(
                    modifier = modifier
                        .fillMaxSize()
                        .weight(1F),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${state.rpm} ")
                            Text(stringResource(R.string.car_rpm))
                        }
                    }
                }
                Card(
                    modifier = modifier
                        .fillMaxSize()
                        .weight(1F),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center

                    ) {
                        Column(
                            modifier = modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (state.isLoadingDtc) {
                                CircularProgressIndicator()
                            } else {
                                Text(
                                    "${state.dtcCodes.size}"
                                )
                            }
                            Text(
                                text = stringResource(R.string.dtc)
                            )
                            IconButton(onClick = onRefreshDtc) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    stringResource(R.string.ui_not_connected),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .weight(1F)
        ) {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { navController.navigate(Navigation.InstrumentPanel) },
                    modifier = modifier.fillMaxSize(),
                    shape = RoundedCornerShape(0),
                ) {
                    Card(
                        modifier = modifier
                            .fillMaxSize()
                    ) {
                        Row(
                            modifier = modifier
                                .fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(R.string.until_maintenance) + " ${state.untilService}")
                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
private fun FunctionScreenPreviewConnected() {
    MasterChariotTheme {
        FunctionScreen(
            contentPadding = PaddingValues(
                top = 30.dp,
                bottom = 100.dp
            ),
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

@Preview
@Composable
private fun FunctionScreenPreviewDisconnected() {
    MasterChariotTheme {
        FunctionScreen(
            contentPadding = PaddingValues(
                top = 30.dp,
                bottom = 100.dp
            ),
            FunctionState(
                speed = 87,
                rpm = 2450,
                isConnected = false,
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