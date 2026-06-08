package com.nikolayux.masterchariot.feature.functions.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AccessibleForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.nikolayux.masterchariot.ui.theme.Black
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import com.nikolayux.masterchariot.ui.theme.White

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.isConnected) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
            ) {
                Card(
                    modifier = modifier.fillMaxSize(),
                    colors = CardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = modifier
                                .fillMaxWidth()
                        )
                        {
                            Text(
                                modifier = modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = stringResource(R.string.ui_not_connected),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        Card(
            modifier = modifier
                .weight(2.5F)
                .padding(start = 8.dp, end = 8.dp),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier.weight(1F))
                Column(
                    modifier = modifier.weight(2F),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${state.speed}",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            if (state.isUsingMiles) {
                                stringResource(R.string.car_speed_miles)
                            } else stringResource(R.string.car_speed_km),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier.weight(3F))
                    Row(
                        modifier = modifier.weight(2F)
//                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate(Navigation.InstrumentPanel) },
                            modifier = modifier
                                .weight(1F)
//                                .fillMaxSize()
                        ) {
                            Row(
                                modifier = modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "${state.rpm}",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        stringResource(R.string.car_rpm),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                            }
                        }
                        OutlinedButton(
                            onClick = onRefreshDtc,
                            modifier = modifier
                                .weight(1F)
//                                .fillMaxSize(),
                        ) {
                            if (state.isLoadingDtc) {
                                CircularProgressIndicator()
                            } else {
                                Row(
                                    modifier = modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "${state.dtcCodes.size}",
                                            style = MaterialTheme.typography.headlineSmall
                                        )
                                        Text(
                                            stringResource(R.string.dtc),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier.weight(1F))
            }
        }

        Card(
            modifier = modifier
                .padding(start = 8.dp, end = 8.dp)
                .weight(1F),
            colors = CardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(modifier = modifier
                .padding(top = 32.dp, bottom = 32.dp)) {
                Row(
                    modifier = modifier
                        .weight(2F),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { navController.navigate(Navigation.InstrumentPanel) },
//                        modifier = modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0),
                    ) {
                        Row(
                            modifier = modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.until_maintenance), style = MaterialTheme.typography.bodyMedium)
                                Row(verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${state.untilService}", style = MaterialTheme.typography.displayMedium)
                                    Text(if (state.isUsingMiles) {
                                        stringResource(R.string.car_measure_miles)
                                    } else stringResource(R.string.car_measure_km), style = MaterialTheme.typography.displayMedium)

                                }

                            }
                        }
                    }
                }
                Row(
                    modifier = modifier
                        .padding(start = 8.dp, end = 8.dp)
                        .weight(1F),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            modifier = modifier.weight(4F),
                            onClick = { navController.navigate(Navigation.Connect) },
                            shape = RoundedCornerShape(16)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.AccessibleForward, null)
//                                Icon(Icons.Default.LeakAdd, null)
                                Text(
                                    stringResource(R.string.connect),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
//                        Column(
//                            modifier = modifier.widthIn(min = 72.dp)
//                        ) {
//
//                        }
                        IconButton(
                            modifier = modifier
                                .weight(1F),
                            onClick = {
                                navController.navigate(Navigation.Settings)
                            },
                            shape = RoundedCornerShape(16),
                            colors = IconButtonColors(
                                containerColor = Black,
                                contentColor = White,
                                disabledContainerColor = Black,
                                disabledContentColor = White
                            )
                        ) {
                            Icon(
                                Icons.Outlined.Settings,
                                null
                            )
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

@Preview(uiMode = UI_MODE_NIGHT_YES)
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
                isUsingMiles = false,
            ),
            onRefreshDtc = { },
        )
    }
}