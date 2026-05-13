package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val speed by viewModel.speed.collectAsState()
    val rpm by viewModel.rpm.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val dtcCodes by viewModel.dtcCodes.collectAsState()
    val isLoadingDtc by viewModel.isLoadingDtc.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
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
                if (isConnected) {
                    Text("Скорость: $speed км/ч", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Обороты: $rpm об/мин", style = MaterialTheme.typography.headlineMedium)
                } else {
                    Text("Не подключено к сканеру", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
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
                    Text(
                        text = "Коды неисправностей (DTC)",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = { viewModel.loadDtcCodes() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                    }
                }

                if (isLoadingDtc) {
                    CircularProgressIndicator()
                } else {
                    if (dtcCodes.isEmpty()) {
                        Text("Нет активных ошибок", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn {
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
    }
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