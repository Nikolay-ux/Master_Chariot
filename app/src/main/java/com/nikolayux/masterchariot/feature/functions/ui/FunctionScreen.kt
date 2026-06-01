package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun FunctionScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: FunctionViewModel = hiltViewModel(),
) {
    val isConnecting by viewModel.isConnecting.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val snapshot by viewModel.snapshot.collectAsState()
    val dtcCodes by viewModel.dtcCodes.collectAsState()
    val isLoadingDtc by viewModel.isLoadingDtc.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Блок статуса и управления подключением
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.connectToObd() },
                        enabled = !isConnected && !isConnecting
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Подключиться")
                    }
                    Button(
                        onClick = { viewModel.disconnectFromObd() },
                        enabled = isConnected
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Отключиться")
                    }
                }
                Spacer(Modifier.height(8.dp))
                when {
                    isConnecting -> CircularProgressIndicator()
                    isConnected -> Text("✅ Подключено", color = MaterialTheme.colorScheme.primary)
                    else -> Text("❌ Не подключено", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Основные параметры (сетка 2 колонки)
        if (snapshot != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1F),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Параметры двигателя", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ParamCard("Скорость", snapshot!!.speed, "км/ч")
                        ParamCard("Обороты", snapshot!!.rpm, "об/мин")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ParamCard("Температура ОЖ", snapshot!!.coolantTemp, "°C")
                        ParamCard("Нагрузка", snapshot!!.engineLoad, "%")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ParamCard("Давление", snapshot!!.intakePressure, "кПа")
                        ParamCard("Расход воздуха", snapshot!!.maf, "г/с")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ParamCard("Дроссель", snapshot!!.throttlePos, "%")
                        ParamCard("Топливо", snapshot!!.fuelLevel, "%")
                    }
                    Spacer(Modifier.height(8.dp))
                    ParamCardFullWidth("Время работы", snapshot!!.runtime?.let { formatRuntime(it) }, "сек")
                }
            }
        } else if (isConnected) {
            Box(Modifier
                .fillMaxWidth()
                .padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Блок DTC (коды ошибок)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔧 Коды неисправностей (DTC)", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { viewModel.loadDtcCodes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (isLoadingDtc) {
                    CircularProgressIndicator()
                } else {
                    if (dtcCodes.isEmpty()) {
                        Text("✅ Нет активных ошибок", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(dtcCodes) { code ->
                                Text(
                                    text = code,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Кнопка ручного обновления всех данных
        if (isConnected) {
            Button(
                onClick = { viewModel.refreshAllData() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Обновить все данные")
            }
        }
    }
}

@Composable
fun ParamCard(title: String, value: Float?, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = if (value != null) "${value.toInt()} $unit" else "—",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ParamCardFullWidth(title: String, valueFormatted: String?, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(
                text = valueFormatted ?: "—",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private fun formatRuntime(seconds: Float): String {
    val totalSeconds = seconds.toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, secs)
    else "%02d:%02d".format(minutes, secs)
}

@Preview
@Composable
private fun FunctionScreenPreview() {
    MasterChariotTheme {
        FunctionScreen(
            contentPadding = PaddingValues(top = 30.dp, bottom = 100.dp)
        )
    }
}