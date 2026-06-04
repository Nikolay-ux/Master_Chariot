package com.nikolayux.masterchariot.feature.car.list.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.state.CarUiModel
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    car: CarUiModel,
    editCarClicked: () -> Unit = {},
    deleteCarClicked: () -> Unit = {},
    selectCarClicked: () -> Unit = {},
) {
    Card(modifier = modifier.fillMaxWidth()
        .border(
            width = if (car.isSelected) 2.dp else 0.dp,
            color = if (car.isSelected)
                Color(0xFF4CAF50)
            else
                Color.Transparent,
            shape = MaterialTheme.shapes.medium
        ),
        onClick = selectCarClicked) {
        var expandedData by remember { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }
        Column(modifier.padding(top = 12.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1F)) {
                    Text(
                        text = car.name,
                    )
                }

                IconButton({
                    expandedData = true
                    expanded = false
                }) {
                    Icon(Icons.Default.ArrowDropDown, null)

                    DropdownMenu(
                        modifier = modifier.fillMaxWidth(),
                        expanded = expandedData,
                        onDismissRequest = { expandedData = false }) {
                        Card(modifier = modifier
                            .fillMaxWidth()
                        ) {
                            Text(
                                stringResource(R.string.menu_car_name) +
                                        ": " +
                                        car.name,
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp, bottom = 5.dp, start = 16.dp, end = 16.dp)
                            )
                            Text(
                                stringResource(R.string.menu_car_mileage) +
                                        ": " +
                                        car.mileage.toString(),
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp, bottom = 5.dp, start = 16.dp, end = 16.dp)
                            )
                            Text(
                                stringResource(R.string.menu_car_interval) +
                                        ": " +
                                        car.serviceInterval.toString(),
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp, bottom = 5.dp, start = 16.dp, end = 16.dp)
                            )
                            Text(
                                stringResource(R.string.menu_car_measure) + ": " + if (car.isUsingMiles) {
                                    stringResource(R.string.car_measure_miles)
                                } else {
                                    stringResource(R.string.car_measure_km)
                                },
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(top = 5.dp, bottom = 5.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                    }
                }

                IconButton({
                    expanded = true
                    expandedData = false
                }) {
                    Icon(Icons.Default.Edit, null)
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.car_menu_edit)) },
                            onClick = {
                                editCarClicked()
                                expanded = false
                            })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.car_menu_delete)) },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.Red
                            ),
                            onClick = {
                                deleteCarClicked()
                                expanded = false
                            })

                    }
                }
                Box(modifier = modifier.size(20.dp)) {
                    if (car.isSelected) {
                        Icon(Icons.Default.Check, null)
                    }
                }

            }

        }
    }
}

@Preview
@Composable
private fun CarCardPreview() {
    MasterChariotTheme {
        CarCard(
            car = CarUiModel(
                id = 1,
                name = "Матиз",
                mileage = 77777
            ),
        )
    }
}