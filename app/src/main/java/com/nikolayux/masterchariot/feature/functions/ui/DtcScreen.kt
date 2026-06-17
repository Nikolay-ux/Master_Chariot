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
    val knownTitleResId = knownDtcTitleResIds[normalizedCode]
    val system = when (normalizedCode.firstOrNull()) {
        'P' -> stringResource(R.string.dtc_system_powertrain)
        'B' -> stringResource(R.string.dtc_system_body)
        'C' -> stringResource(R.string.dtc_system_chassis)
        'U' -> stringResource(R.string.dtc_system_network)
        else -> stringResource(R.string.dtc_system_unknown)
    }
    val title = knownTitleResId?.let { stringResource(it) } ?: stringResource(R.string.dtc_unknown_title)
    val description = knownTitleResId?.let {
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

private val knownDtcTitleResIds = mapOf<String, Int>(
    "P0100" to R.string.dtc_title_p0100,
    "P0101" to R.string.dtc_title_p0101,
    "P0102" to R.string.dtc_title_p0102,
    "P0103" to R.string.dtc_title_p0103,
    "P0110" to R.string.dtc_title_p0110,
    "P0115" to R.string.dtc_title_p0115,
    "P0120" to R.string.dtc_title_p0120,
    "P0130" to R.string.dtc_title_p0130,
    "P0171" to R.string.dtc_title_p0171,
    "P0172" to R.string.dtc_title_p0172,
    "P0300" to R.string.dtc_title_p0300,
    "P0301" to R.string.dtc_title_p0301,
    "P0302" to R.string.dtc_title_p0302,
    "P0303" to R.string.dtc_title_p0303,
    "P0304" to R.string.dtc_title_p0304,
    "P0325" to R.string.dtc_title_p0325,
    "P0335" to R.string.dtc_title_p0335,
    "P0340" to R.string.dtc_title_p0340,
    "P0420" to R.string.dtc_title_p0420,
    "P0430" to R.string.dtc_title_p0430,
    "P0440" to R.string.dtc_title_p0440,
    "P0442" to R.string.dtc_title_p0442,
    "P0455" to R.string.dtc_title_p0455,
    "P0500" to R.string.dtc_title_p0500,
    "P0562" to R.string.dtc_title_p0562,
    "P0700" to R.string.dtc_title_p0700,
    "U0100" to R.string.dtc_title_u0100,
    "U0121" to R.string.dtc_title_u0121
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
