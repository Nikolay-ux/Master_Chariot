package com.nikolayux.masterchariot.feature.car.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.state.AddNewCarState
import com.nikolayux.masterchariot.feature.car.list.state.CarListMessage
import com.nikolayux.masterchariot.feature.car.list.state.CarListState
import com.nikolayux.masterchariot.feature.car.list.viewmodel.CarViewModel

/**
 * На экране мы должны разместить список автомобилей с использованием реализованных функций
 * для работы с базой данных
 */
@Composable
fun CarListScreenRoute(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    viewModel: CarViewModel = hiltViewModel(),
//    navController: NavController = rememberNavController(),
    listState: LazyListState = rememberLazyListState()
) {
//    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
//    val context = LocalContext.current
    CarListScreen(
        viewModel.state,
        modifier,
        contentPadding,
        viewModel::action,
        listState
    )
}

@Composable
private fun CarListScreen(
    state: CarListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onEvent: (CarListMessage) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
//    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
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
        FloatingActionButton(
            onClick = { onEvent(CarListMessage.AddCar) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp
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
            onUpdate = { onEvent(CarListMessage.UpdateCar)},
            onEditName = { onEvent(CarListMessage.NameChanged(it)) },
            onEditMileage = { onEvent(CarListMessage.MileageChanged(it)) },
            onEditInterval = { onEvent(CarListMessage.IntervalChanged(it)) },
            onEditMeasure = { onEvent(CarListMessage.MeasureChanged(it)) }
        )
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