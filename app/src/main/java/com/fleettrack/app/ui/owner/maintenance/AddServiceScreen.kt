package com.fleettrack.app.ui.owner.maintenance

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    onBackClick: () -> Unit,
    onServiceSaved: (regNo: String, serviceType: String, amount: Double) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val carList = listOf("TS09 AB 1234", "TS09 CD 5678", "TS09 EF 9012", "TS09 GH 3456", "TS09 IJ 7890")
    var selectedCar by remember { mutableStateOf(carList[0]) }
    var isCarDropdownExpanded by remember { mutableStateOf(false) }

    val serviceTypes = listOf("Regular Service", "Oil & Filter Change", "Brake Pad Inspection", "Tire Replacement", "AC Service", "Battery Replacement")
    var selectedServiceType by remember { mutableStateOf(serviceTypes[0]) }
    var isServiceDropdownExpanded by remember { mutableStateOf(false) }

    var serviceDate by remember { mutableStateOf("05 Nov 2026") }
    var serviceAmount by remember { mutableStateOf("4500") }
    var notes by remember { mutableStateOf("Oil change, filter, general check") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Header: "< Add Service"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Service",
                color = TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Car Selector Dropdown
                Text(
                    text = "Car *",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = isCarDropdownExpanded,
                    onExpandedChange = { isCarDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCar,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCarDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isCarDropdownExpanded,
                        onDismissRequest = { isCarDropdownExpanded = false }
                    ) {
                        carList.forEach { car ->
                            DropdownMenuItem(
                                text = { Text(car, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedCar = car
                                    isCarDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Service Type Dropdown
                Text(
                    text = "Service Type *",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                ExposedDropdownMenuBox(
                    expanded = isServiceDropdownExpanded,
                    onExpandedChange = { isServiceDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedServiceType,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isServiceDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = isServiceDropdownExpanded,
                        onDismissRequest = { isServiceDropdownExpanded = false }
                    ) {
                        serviceTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, fontWeight = FontWeight.Medium) },
                                onClick = {
                                    selectedServiceType = type
                                    isServiceDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date
                Text(
                    text = "Date *",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = serviceDate,
                    onValueChange = { serviceDate = it },
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = PrimaryPurple)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Amount (₹)
                Text(
                    text = "Amount (₹) *",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = serviceAmount,
                    onValueChange = { serviceAmount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Notes
                Text(
                    text = "Notes",
                    color = TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("e.g. Oil change, filter, general check") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Primary Save Button
                Button(
                    onClick = {
                        val amount = serviceAmount.toDoubleOrNull() ?: 4500.0
                        onServiceSaved(selectedCar, selectedServiceType, amount)
                        Toast.makeText(context, "Service $selectedServiceType saved for $selectedCar", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = PillShape
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
