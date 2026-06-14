package com.nikolayux.masterchariot.feature.car.list.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.state.AddNewCarState
import com.nikolayux.masterchariot.feature.car.list.state.CarListMessage
import com.nikolayux.masterchariot.feature.car.list.state.CarListState
import com.nikolayux.masterchariot.feature.car.list.viewmodel.CarViewModel
import com.nikolayux.masterchariot.ui.theme.Black
import com.nikolayux.masterchariot.ui.theme.White

private val SettingsScreenPadding = 16.dp
private val SettingsSectionSpacing = 20.dp
private val SettingsControlHeight = 48.dp
private val NotificationsToggleWidth = 128.dp

@Composable
fun CarListScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: CarViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
    listState: LazyListState = rememberLazyListState(),
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    CarListScreen(
        state = viewModel.state,
        modifier = modifier,
        onEvent = viewModel::action,
        listState = listState,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
private fun CarListScreen(
    state: CarListState,
    modifier: Modifier = Modifier,
    onEvent: (CarListMessage) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBackClick: () -> Unit
) {
    var notificationsEnabled by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SettingsTopAppBar(onBackClick = onBackClick)
        }
    ) { contentPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = SettingsScreenPadding,
                    top = SettingsScreenPadding,
                    end = SettingsScreenPadding,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState
            ) {
                item {
                    SettingsHeader(text = stringResource(R.string.settings_my_cars))
                }

                if (state.cars.isEmpty()) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            text = stringResource(R.string.settings_cars_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    items(items = state.cars, key = { it.id }) { car ->
                        CarCard(
                            modifier = Modifier.animateItem(),
                            car = car,
                            editCarClicked = { onEvent(CarListMessage.Edit(car)) },
                            deleteCarClicked = { onEvent(CarListMessage.Delete(car.id)) },
                            selectCarClicked = { onEvent(CarListMessage.SelectCar(car.id)) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(SettingsSectionSpacing))
                    SettingsActionRow(
                        title = stringResource(R.string.settings_app_theme)
                    ) {
                        ThemeModeButton(
                            isDarkTheme = isDarkTheme,
                            onClick = onToggleTheme
                        )
                    }
                }

                item {
                    SettingsActionRow(
                        title = stringResource(R.string.settings_notifications)
                    ) {
                        NotificationsToggleButton(
                            enabled = notificationsEnabled,
                            onToggle = { notificationsEnabled = !notificationsEnabled }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { onEvent(CarListMessage.AddCar) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                )
            }
        }

        state.addNewCarState?.let { addNewCarState ->
            AddCarDialog(
                addNewCarState,
                onDismissRequest = { onEvent(CarListMessage.DismissAddCarDialog) },
                onCreate = { onEvent(CarListMessage.SaveNewCar) },
                onUpdate = { onEvent(CarListMessage.UpdateCar) },
                onEditName = { onEvent(CarListMessage.NameChanged(it)) },
                onEditMileage = { onEvent(CarListMessage.MileageChanged(it)) },
                onEditInterval = { onEvent(CarListMessage.IntervalChanged(it)) },
                onEditMeasure = { onEvent(CarListMessage.MeasureChanged(it)) }
            )
        }

        state.unknownVin?.let { vin ->
            UnknownVinDialog(
                vin = vin,
                onCreate = { onEvent(CarListMessage.CreateCarFromVin) },
                onDismiss = { onEvent(CarListMessage.DismissUnknownVinDialog) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = { Text(stringResource(R.string.tab_settings)) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun SettingsHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
private fun SettingsActionRow(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        action()
    }
}

@Composable
private fun ThemeModeButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetIsDark = !isDarkTheme
    val containerColor = if (targetIsDark) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (targetIsDark) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = modifier
            .height(SettingsControlHeight)
            .width(SettingsControlHeight),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (targetIsDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = stringResource(R.string.settings_app_theme)
            )
        }
    }
}

@Composable
private fun NotificationsToggleButton(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(NotificationsToggleWidth)
            .height(SettingsControlHeight),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onToggle)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NotificationToggleSegment(
                selected = !enabled,
                label = stringResource(R.string.settings_notifications_off),
                icon = Icons.Default.NotificationsOff,
                modifier = Modifier.weight(1.2f),
                selectedColor = Black,
                selectedContentColor = White
            )
            NotificationToggleSegment(
                selected = enabled,
                label = stringResource(R.string.settings_notifications_on),
                icon = Icons.Default.Notifications,
                modifier = Modifier.weight(1f),
                selectedColor = MaterialTheme.colorScheme.secondary,
                selectedContentColor = MaterialTheme.colorScheme.background
            )
        }
    }
}

@Composable
private fun NotificationToggleSegment(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    selectedColor: Color,
    selectedContentColor: Color,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) selectedColor else Color.Transparent
    val contentColor = if (selected) selectedContentColor else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AddCarDialog(
    state: AddNewCarState,
    onDismissRequest: () -> Unit = {},
    onCreate: () -> Unit = {},
    onUpdate: () -> Unit = {},
    onEditName: (String) -> Unit = {},
    onEditMileage: (Int) -> Unit = {},
    onEditInterval: (Int) -> Unit = {},
    onEditMeasure: (Boolean) -> Unit = {},
) {
    val options = listOf("км", "мили")
    val selectedIndex = if (state.isUsingMiles) 1 else 0
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column {
                Text(stringResource(R.string.dialog_car_name))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onEditName(it) }
                )
                Text(stringResource(R.string.dialog_car_mileage))
                OutlinedTextField(
                    value = if (state.mileage == 0) "" else state.mileage.toString(),
                    onValueChange = { onEditMileage(it.toIntOrNull() ?: 0) }
                )
                Text(stringResource(R.string.dialog_car_interval))
                OutlinedTextField(
                    value = state.serviceInterval.toString(),
                    onValueChange = { onEditInterval(it.toIntOrNull() ?: 0) }
                )
                if (!state.vin.isNullOrBlank()) {
                    Text("VIN")
                    OutlinedTextField(
                        value = state.vin,
                        onValueChange = {},
                        readOnly = true
                    )
                }
                Text(stringResource(R.string.dialog_car_measure))
                SingleChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { onEditMeasure(index == 1) },
                            selected = index == selectedIndex
                        ) {
                            Text(label)
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(R.string.dialog_car_cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { if (state.isEdit) onUpdate() else onCreate() },
                        enabled = state.name.isNotBlank()
                    ) {
                        Text(stringResource(R.string.dialog_car_save))
                    }
                }
            }
        }
    }
}

@Composable
fun UnknownVinDialog(
    vin: String,
    onCreate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Обнаружен новый автомобиль"
                )
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
                Text(
                    text = "VIN: $vin"
                )
                Spacer(
                    modifier = Modifier.height(16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Отмена")
                    }
                    TextButton(
                        onClick = onCreate
                    ) {
                        Text("Создать")
                    }
                }
            }
        }
    }
}