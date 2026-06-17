package com.nikolayux.masterchariot.feature.car.list.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.state.CarUiModel
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import com.nikolayux.masterchariot.ui.theme.Red

@Composable
fun CarCard(
    modifier: Modifier = Modifier,
    car: CarUiModel,
    editCarClicked: () -> Unit = {},
    deleteCarClicked: () -> Unit = {},
    selectCarClicked: () -> Unit = {},
) {
    var detailsExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var deleteDialogVisible by remember { mutableStateOf(false) }

    val containerColor = if (car.isSelected) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        onClick = selectCarClicked
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.car_card_mileage_value, car.mileage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = { detailsExpanded = !detailsExpanded }
                ) {
                    Icon(
                        imageVector = if (detailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.car_menu_edit),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.primary,
                                leadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            onClick = {
                                editCarClicked()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.car_menu_delete),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Red,
                                leadingIconColor = Red
                            ),
                            onClick = {
                                menuExpanded = false
                                deleteDialogVisible = true
                            }
                        )
                    }
                }
            }

            if (detailsExpanded) {
                CarDetailsContent(car = car)
            }
        }
    }

    if (deleteDialogVisible) {
        DeleteCarConfirmDialog(
            onConfirm = {
                deleteDialogVisible = false
                deleteCarClicked()
            },
            onDismiss = { deleteDialogVisible = false }
        )
    }
}

@Composable
private fun DeleteCarConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.car_delete_confirm_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirm
                    ) {
                        Text(
                            text = stringResource(R.string.car_delete_confirm_yes),
                            color = Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(R.string.car_delete_confirm_no),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarDetailsContent(
    car: CarUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CarInfoRow(
            title = stringResource(R.string.menu_car_name),
            value = car.name
        )
        CarInfoRow(
            title = stringResource(R.string.menu_car_mileage),
            value = stringResource(R.string.car_card_mileage_value, car.mileage)
        )
        CarInfoRow(
            title = stringResource(R.string.menu_car_interval),
            value = stringResource(R.string.car_card_mileage_value, car.serviceInterval)
        )
        CarInfoRow(
            title = stringResource(R.string.until_maintenance),
            value = stringResource(R.string.car_card_mileage_value, car.kmUntilMaintenance)
        )
        CarInfoRow(
            title = stringResource(R.string.menu_car_measure),
            value = if (car.isUsingMiles) {
                stringResource(R.string.car_measure_miles)
            } else {
                stringResource(R.string.car_measure_km)
            }
        )
        CarInfoRow(
            title = stringResource(R.string.car_vin_label),
            value = car.vin?.takeIf { it.isNotBlank() } ?: stringResource(R.string.unknown)
        )
    }
}

@Composable
private fun CarInfoRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            modifier = Modifier.weight(1.2f),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(locale = "ru")
@Composable
private fun CarCardPreview() {
    MasterChariotTheme {
        CarCard(
            car = CarUiModel(
                id = 1,
                name = "Матиз",
                mileage = 77777,
                serviceInterval = 10000,
                kmUntilMaintenance = 2500,
                vin = "XTA210990Y1234567",
                isSelected = true
            ),
        )
    }
}
