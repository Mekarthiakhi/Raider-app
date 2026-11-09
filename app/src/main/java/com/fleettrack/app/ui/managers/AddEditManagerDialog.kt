package com.fleettrack.app.ui.managers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.ManagerEntity
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditManagerDialog(
    initialManager: ManagerEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, hubName: String, pinCode: String) -> Unit
) {
    var name by remember { mutableStateOf(initialManager?.name ?: "") }
    var phone by remember { mutableStateOf(initialManager?.phone ?: "") }
    var hubName by remember { mutableStateOf(initialManager?.hubName ?: "Moosapet Central Hub") }
    var pinCode by remember { mutableStateOf(initialManager?.pinCode ?: "1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickHubs = listOf(
        "Moosapet Central Hub",
        "Kukatpally Y-Junction Hub",
        "Madhapur / Hitec City Hub",
        "Gachibowli DLF Hub",
        "Secunderabad Hub"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (initialManager == null) "👔 Add Hub Fleet Manager" else "👔 Edit Fleet Manager",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Assign a dedicated manager to oversee 10–20+ cabs, drivers, and daily cash settlements.",
                    fontSize = 12.sp,
                    color = TextGray
                )

                // Manager Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Manager Full Name") },
                    placeholder = { Text("e.g. Srinivas Rao") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 10) phone = digits
                    },
                    label = { Text("Mobile Number (10 Digits)") },
                    placeholder = { Text("e.g. 9849012345") },
                    leadingIcon = {
                        Text(
                            text = "+91 ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDark,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Hub Name
                OutlinedTextField(
                    value = hubName,
                    onValueChange = { hubName = it },
                    label = { Text("Operating Hub / Office Name") },
                    placeholder = { Text("e.g. Moosapet Central Hub") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Hub Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickHubs.forEach { hub ->
                        val isSelected = hubName.equals(hub, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryPurple.copy(alpha = 0.15f) else Color(0xFFF1F3F5))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryPurple else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { hubName = hub }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = hub,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryPurple else TextDark
                            )
                        }
                    }
                }

                // PIN Code
                OutlinedTextField(
                    value = pinCode,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        if (digits.length <= 4) pinCode = digits
                    },
                    label = { Text("Manager 4-Digit Access PIN") },
                    placeholder = { Text("1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Please enter manager name."
                        return@Button
                    }
                    if (phone.length < 10) {
                        errorMessage = "Please enter valid 10-digit mobile number."
                        return@Button
                    }
                    if (hubName.isBlank()) {
                        errorMessage = "Please select or enter hub name."
                        return@Button
                    }
                    onConfirm(name.trim(), phone.trim(), hubName.trim(), if (pinCode.isBlank()) "1234" else pinCode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save Manager", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMutedGray)
            }
        }
    )
}

