package com.fleettrack.app.ui.owner.expenses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val percentage: Int,
    val color: Color
)

@Composable
fun ExpensesScreen(
    onBackClick: (() -> Unit)? = null,
    onNavigateToAddFuel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedMonth by remember { mutableStateOf("Oct 2026") }
    var showAddFuelDialog by remember { mutableStateOf(false) }

    var fuelAmount by remember { mutableStateOf(72680.0) }
    var driverPaymentAmount by remember { mutableStateOf(120000.0) }
    var maintenanceAmount by remember { mutableStateOf(18000.0) }
    var otherAmount by remember { mutableStateOf(10040.0) }

    val totalExpenses = fuelAmount + driverPaymentAmount + maintenanceAmount + otherAmount

    val categories = listOf(
        ExpenseCategory("Fuel", fuelAmount, ((fuelAmount / totalExpenses) * 100).toInt(), Color(0xFF007AFF)),
        ExpenseCategory("Driver Payments", driverPaymentAmount, ((driverPaymentAmount / totalExpenses) * 100).toInt(), PrimaryPurple),
        ExpenseCategory("Maintenance", maintenanceAmount, ((maintenanceAmount / totalExpenses) * 100).toInt(), Color(0xFFFF9500)),
        ExpenseCategory("Other", otherAmount, ((otherAmount / totalExpenses) * 100).toInt(), Color(0xFF34C759))
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (onNavigateToAddFuel != null) {
                        onNavigateToAddFuel()
                    } else {
                        showAddFuelDialog = true
                    }
                },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = PillShape,
                modifier = Modifier.padding(bottom = 70.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Fuel Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header: "< Expenses"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextDark
                                )
                            }
                        }
                        Text(
                            text = "Expenses",
                            color = TextDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Month Selector Dropdown Pill
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SurfaceWhite)
                            .border(1.dp, SurfaceBorder, PillShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedMonth,
                                color = TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Month",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Donut Chart Card (Matching Mockup Sheet 2)
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Donut Chart Canvas
                        Box(
                            modifier = Modifier.size(190.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                val strokeWidth = 32f

                                for (cat in categories) {
                                    val sweep = (cat.amount / totalExpenses).toFloat() * 360f
                                    drawArc(
                                        color = cat.color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep - 2f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }

                            // Center Total Text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = currencyFormat.format(totalExpenses),
                                    color = TextDark,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Total Expenses",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Category Breakdown List
            item {
                Text(
                    text = "Category Breakdown",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        categories.forEachIndexed { index, cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(cat.color)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = cat.name,
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currencyFormat.format(cat.amount),
                                        color = TextDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${cat.percentage}%)",
                                        color = TextMutedGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (index < categories.size - 1) {
                                androidx.compose.material3.HorizontalDivider(
                                    color = SurfaceBorder,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }

            // 4. Recent Fuel & Operational Transactions
            item {
                Text(
                    text = "Recent Fuel Log",
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FuelTransactionTile(
                        carReg = "TS09 AB 1234",
                        date = "02 Nov 2026",
                        liters = "25.40 L (Petrol)",
                        odo = "42,340 km",
                        amount = 2840.0
                    )
                    FuelTransactionTile(
                        carReg = "TS09 CD 5678",
                        date = "01 Nov 2026",
                        liters = "18.20 kg (CNG)",
                        odo = "58,120 km",
                        amount = 1640.0
                    )
                    FuelTransactionTile(
                        carReg = "TS09 EF 9012",
                        date = "31 Oct 2026",
                        liters = "22.00 L (Petrol)",
                        odo = "31,850 km",
                        amount = 2460.0
                    )
                }
            }
        }
    }

    if (showAddFuelDialog) {
        AddFuelExpenseDialog(
            onDismiss = { showAddFuelDialog = false },
            onSave = { car, date, liters, amount, odo ->
                fuelAmount += amount
                showAddFuelDialog = false
            }
        )
    }
}

@Composable
private fun FuelTransactionTile(
    carReg: String,
    date: String,
    liters: String,
    odo: String,
    amount: Double
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = TileShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = carReg,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date,
                        color = TextMutedGray,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$liters • Odo: $odo",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "- " + currencyFormat.format(amount),
                color = Color(0xFFE53935),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddFuelExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (car: String, date: String, liters: Double, amount: Double, odo: String) -> Unit
) {
    var car by remember { mutableStateOf("TS09 AB 1234") }
    var date by remember { mutableStateOf("02 Nov 2026") }
    var liters by remember { mutableStateOf("25.40") }
    var amount by remember { mutableStateOf("2840") }
    var odometer by remember { mutableStateOf("42340") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Fuel Expense",
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = car,
                    onValueChange = { car = it.uppercase() },
                    label = { Text("Car Registration") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = liters,
                        onValueChange = { liters = it },
                        label = { Text("Fuel (Liters)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("Odometer (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedLiters = liters.toDoubleOrNull() ?: 0.0
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    onSave(car, date, parsedLiters, parsedAmount, odometer)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}
