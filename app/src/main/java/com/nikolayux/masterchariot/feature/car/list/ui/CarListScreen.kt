package com.nikolayux.masterchariot.feature.car.list.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
    modifier: Modifier,
    contentPadding: PaddingValues = PaddingValues.Zero,
    viewModel: CarViewModel = viewModel<CarViewModel>(),
    navController: NavController = rememberNavController(),
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
    val layoutDirection = LocalLayoutDirection.current
    var showDialog by remember { mutableStateOf(false) }

    val combinedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection),
        end = contentPadding.calculateEndPadding(layoutDirection),
        top = contentPadding.calculateTopPadding(),
        bottom = contentPadding.calculateBottomPadding(),
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = combinedPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        items(items = state.cars, key = { it.id }) { car ->
            CarCard(
                modifier = Modifier.animateItem(),
                car = car,
                editCarClicked = { CarListMessage.Edit(car) },
                deleteCarClicked = { CarListMessage.Delete(car.id) },
                deleteAllCarClicked = { CarListMessage.DeleteAll },
            )
        }
    }

    FloatingActionButton(
        onClick = { onEvent(CarListMessage.AddCar) },
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = null,
        )
    }
    state.addNewCarState?.let { addNewCarState ->
        AddCarDialog(
            addNewCarState,
            onDismissRequest = { onEvent(CarListMessage.DismissAddCarDialog) },
            onConfirm = { onEvent(CarListMessage.SaveNewCar) },
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
    onConfirm: () -> Unit = {},
    onEditName: (String) -> Unit = {},
    onEditMileage: (Int) -> Unit = {},
    onEditInterval: (Int) -> Unit = {},
    onEditMeasure: (Boolean) -> Unit = {},
) {
    val options = listOf("км", "мили")
    var selectedIndex = 0
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column {
                TODO("Добавить строковые ресурсы")
                Text("Название")
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onEditName(it) }
                )
                Text("Пробег")
                OutlinedTextField(
                    value = if (state.mileage == 0) "" else state.mileage.toString(),
                    onValueChange = { onEditMileage(it.toIntOrNull() ?: 0) }
                )
                Text("Интервал ТО")
                OutlinedTextField(
                    value = state.serviceInterval.toString(),
                    onValueChange = { onEditInterval(it.toIntOrNull() ?: 0) }
                )
                Text("Мера расстояния")
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
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { onConfirm() },
//                        enabled = textState.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//fun AddCarDialogPreview() {
//    Surface {
//        AddCarDialog(
//            onDismissRequest = {},
//            onConfirm = {}
//        )
//    }
//}