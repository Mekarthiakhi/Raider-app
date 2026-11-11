package com.fleettrack.app.ui.owner.officeregister

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.DutyLogEntity
import com.fleettrack.app.ui.components.DatePickerField
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.SearchableDropdownField
import com.fleettrack.app.ui.components.SearchableDropdownOption
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.IdleOrange
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceCard
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.util.SvmCommissionCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeRegisterScreen(
    viewModel: OfficeRegisterViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showRulesCard by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Office Duty Register",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "SVM FLEET • Moosapet Hyderabad",
                            fontSize = 12.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
                    }
                },
                actions = {
                    IconButton(onClick = { showRulesCard = !showRulesCard }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Commission Base Rules",
                            tint = PrimaryPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryPurple,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Shift")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "New Shift Entry", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. KPI Summary Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OfficeKpiCard(
                        title = "Gross Earnings",
                        value = "₹${uiState.totalEarnings.toInt()}",
                        color = ActiveGreen,
                        modifier = Modifier.weight(1f)
                    )
                    OfficeKpiCard(
                        title = "Cash Collection",
                        value = "₹${uiState.totalCashCollection.toInt()}",
                        color = IdleOrange,
                        modifier = Modifier.weight(1f)
                    )
                    OfficeKpiCard(
                        title = "Total Trips",
                        value = "${uiState.totalTrips}",
                        color = PrimaryPurple,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }

            // 2. Expandable SVM FLEET Commission Rules Cheat Sheet (Image 1)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showRulesCard = !showRulesCard },
                    colors = CardDefaults.cardColors(containerColor = PrimaryPurpleContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Calculate, contentDescription = null, tint = PrimaryPurple)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SVM FLEET Driver's Commission Base",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = PrimaryPurple
                                )
                            }
                            Icon(
                                imageVector = if (showRulesCard) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PrimaryPurple
                            )
                        }

                        AnimatedVisibility(visible = showRulesCard) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = "Office: Moosapet Hyderabad, 500018 • Contacts: 9391915013, 9959859347",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // 12 Hrs Shift Table
                                Text(
                                    text = "12 Hrs SHIFT { Minimum 10 Trips }",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CommissionRuleRow("3500 Above", "40%")
                                CommissionRuleRow("2500 to 3500", "35%")
                                CommissionRuleRow("2000 to 2500", "30%")
                                CommissionRuleRow("1500 to 2000", "25%")
                                CommissionRuleRow("Below 1500", "0%")

                                Spacer(modifier = Modifier.height(10.dp))

                                // 24 Hrs Shift Table
                                Text(
                                    text = "24 Hrs SHIFT { Minimum 18 Trips }",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CommissionRuleRow("6500 Above", "40%")
                                CommissionRuleRow("5000 to 6500", "35%")
                                CommissionRuleRow("4000 to 5000", "30%")
                                CommissionRuleRow("2500 to 4000", "25%")
                                CommissionRuleRow("Below 2500", "0%")
                            }
                        }
                    }
                }
            }

            // 3. Search and Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search Driver / Vehicle / 3368") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMutedGray) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem(
                        label = "All (${uiState.dutyLogs.size})",
                        isSelected = uiState.selectedFilter == "ALL",
                        onClick = { viewModel.onFilterSelected("ALL") }
                    )
                    FilterChipItem(
                        label = "12 Hrs Shift",
                        isSelected = uiState.selectedFilter == "12_HOURS",
                        onClick = { viewModel.onFilterSelected("12_HOURS") }
                    )
                    FilterChipItem(
                        label = "24 Hrs Shift",
                        isSelected = uiState.selectedFilter == "24_HOURS",
                        onClick = { viewModel.onFilterSelected("24_HOURS") }
                    )
                    FilterChipItem(
                        label = "Open / Pending",
                        isSelected = uiState.selectedFilter == "OPEN",
                        onClick = { viewModel.onFilterSelected("OPEN") }
                    )
                }
            }

            // 4. Register Entries List (Image 2 columns)
            if (uiState.dutyLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No duty log entries match your filter.",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(uiState.dutyLogs, key = { it.id }) { log ->
                    DutyLogCard(log = log)
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog) {
        AddDutyLogDialog(
            recentLogs = uiState.dutyLogs,
            onDismiss = { showAddDialog = false },
            onConfirm = { shiftType, loginDate, loginTime, logoutDate, logoutTime, vehicleNo, driverName, driverPhone, trips, earnings, cashCollection, expenditure, drivePass, advance, paymentCash, paymentOnline, remarks ->
                viewModel.addNewDutyLog(
                    shiftType, loginDate, loginTime, logoutDate, logoutTime,
                    vehicleNo, driverName, driverPhone, trips, earnings,
                    cashCollection, expenditure, drivePass, advance,
                    paymentCash, paymentOnline, remarks
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DutyLogCard(log: DutyLogEntity) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Vehicle No, Driver, Shift Tag & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = log.vehicleNo,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = log.driverName,
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (log.status == "SETTLED") ActiveGreen.copy(alpha = 0.15f) else IdleOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = log.status,
                            color = if (log.status == "SETTLED") ActiveGreen else IdleOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (log.shiftType == "12_HOURS") "12h Shift" else "24h Shift",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Office Paper Register Log In & Log Out timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "LOG IN", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(text = "${log.loginDate} ${log.loginTime}", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "LOG OUT", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Text(text = "${log.logoutDate} ${log.logoutTime}", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Columns Matrix (Image 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RegisterMetric(title = "TRIPS", value = "${log.trips}")
                RegisterMetric(title = "EARNING'S", value = "₹${log.earnings.toInt()}", valueColor = ActiveGreen)
                RegisterMetric(title = "CASH COLL.", value = "₹${log.cashCollection.toInt()}", valueColor = IdleOrange)
                RegisterMetric(title = "COMMISSION", value = "₹${log.commissionRent.toInt()}", valueColor = PrimaryPurple)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary Office Columns: Expenditure, Drive Pass, Advance, Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Exp: ₹${log.expenditure.toInt()}  •  Pass: ₹${log.drivePass.toInt()}  •  Adv: ₹${log.advance.toInt()}",
                    fontSize = 11.sp,
                    color = TextGray
                )

                val paymentStr = when {
                    log.paymentOnline > 0 -> "Online UPI: ₹${log.paymentOnline.toInt()}"
                    log.paymentCash > 0 -> "Cash: ₹${log.paymentCash.toInt()}"
                    else -> "Pending"
                }
                Text(
                    text = paymentStr,
                    fontSize = 11.sp,
                    color = PrimaryPurple,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!log.remarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${log.remarks}",
                    fontSize = 11.sp,
                    color = TextMutedGray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
private fun RegisterMetric(
    title: String,
    value: String,
    valueColor: Color = TextDark
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CommissionRuleRow(range: String, percent: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = range, fontSize = 12.sp, color = TextDark)
        Text(text = percent, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
    }
}

@Composable
private fun OfficeKpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = TextGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(PillShape)
            .background(if (isSelected) PrimaryPurple else SurfaceWhite)
            .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AddDutyLogDialog(
    recentLogs: List<DutyLogEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (
        shiftType: SvmCommissionCalculator.ShiftType,
        loginDate: String,
        loginTime: String,
        logoutDate: String,
        logoutTime: String,
        vehicleNo: String,
        driverName: String,
        driverPhone: String,
        trips: Int,
        earnings: Double,
        cashCollection: Double,
        expenditure: Double,
        drivePass: Double,
        advance: Double,
        paymentCash: Double,
        paymentOnline: Double,
        remarks: String
    ) -> Unit
) {
    var shiftType by remember { mutableStateOf(SvmCommissionCalculator.ShiftType.SHIFT_12_HOURS) }
    var vehicleNo by remember { mutableStateOf("TS09 UB 3368") }
    var driverName by remember { mutableStateOf("Suman") }
    var driverPhone by remember { mutableStateOf("+91 93919 15013") }
    var loginDate by remember { mutableStateOf("01/11/2026") }
    var loginTime by remember { mutableStateOf("06:00 AM") }
    var logoutDate by remember { mutableStateOf("01/11/2026") }
    var logoutTime by remember { mutableStateOf("08:15 PM") }

    var tripsStr by remember { mutableStateOf("16") }
    var earningsStr by remember { mutableStateOf("2950") }
    var cashCollectionStr by remember { mutableStateOf("2503") }
    var expenditureStr by remember { mutableStateOf("200") }
    var drivePassStr by remember { mutableStateOf("0") }
    var advanceStr by remember { mutableStateOf("0") }
    var paymentCashStr by remember { mutableStateOf("0") }
    var paymentOnlineStr by remember { mutableStateOf("278") }
    var remarks by remember { mutableStateOf("") }

    val trips = tripsStr.toIntOrNull() ?: 0
    val earnings = earningsStr.toDoubleOrNull() ?: 0.0
    val cashColl = cashCollectionStr.toDoubleOrNull() ?: 0.0
    val exp = expenditureStr.toDoubleOrNull() ?: 0.0
    val pass = drivePassStr.toDoubleOrNull() ?: 0.0
    val adv = advanceStr.toDoubleOrNull() ?: 0.0

    val calc = SvmCommissionCalculator.calculate(
        shiftType = shiftType,
        trips = trips,
        grossEarnings = earnings,
        cashCollection = cashColl,
        expenditure = exp,
        drivePass = pass,
        advance = adv
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add Office Shift Entry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shift Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (shiftType == SvmCommissionCalculator.ShiftType.SHIFT_12_HOURS) PrimaryPurple else SurfaceBorder)
                            .clickable { shiftType = SvmCommissionCalculator.ShiftType.SHIFT_12_HOURS }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "12 Hrs (Min 10)", color = if (shiftType == SvmCommissionCalculator.ShiftType.SHIFT_12_HOURS) Color.White else TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (shiftType == SvmCommissionCalculator.ShiftType.SHIFT_24_HOURS) PrimaryPurple else SurfaceBorder)
                            .clickable { shiftType = SvmCommissionCalculator.ShiftType.SHIFT_24_HOURS }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "24 Hrs (Min 18)", color = if (shiftType == SvmCommissionCalculator.ShiftType.SHIFT_24_HOURS) Color.White else TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                val quickPickOptions = remember(recentLogs) {
                    val unique = linkedMapOf<String, DutyLogEntity>()
                    recentLogs.forEach { unique.putIfAbsent("${it.vehicleNo}|${it.driverName}", it) }
                    unique.values.map {
                        SearchableDropdownOption(
                            value = "${it.vehicleNo}|${it.driverName}|${it.driverPhone}",
                            label = "${it.driverName} (${it.vehicleNo})",
                            subtitle = it.driverPhone
                        )
                    }
                }
                if (quickPickOptions.isNotEmpty()) {
                    SearchableDropdownField(
                        label = "Quick Select Driver / Cab",
                        selectedValue = "$vehicleNo|$driverName|$driverPhone",
                        options = quickPickOptions,
                        onOptionSelected = { opt ->
                            val parts = opt.value.split("|")
                            if (parts.size >= 3) {
                                vehicleNo = parts[0]
                                driverName = parts[1]
                                driverPhone = parts[2]
                            }
                        },
                        dialogTitle = "Select Driver or Cab",
                        searchPlaceholder = "Search driver, phone or plate...",
                        placeholder = "Tap to search & auto-fill...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = vehicleNo,
                    onValueChange = { vehicleNo = it.uppercase() },
                    label = { Text("Vehicle No (e.g. TS09 UB 3368)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = driverName,
                    onValueChange = { driverName = it },
                    label = { Text("Driver Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        label = "Log In Date *",
                        selectedDate = loginDate,
                        onDateSelected = { loginDate = it },
                        outputPattern = "dd/MM/yyyy",
                        modifier = Modifier.weight(1f)
                    )
                    DatePickerField(
                        label = "Log Out Date *",
                        selectedDate = logoutDate,
                        onDateSelected = { logoutDate = it },
                        outputPattern = "dd/MM/yyyy",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = loginTime,
                        onValueChange = { loginTime = it },
                        label = { Text("Log In Time") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = logoutTime,
                        onValueChange = { logoutTime = it },
                        label = { Text("Log Out Time") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tripsStr,
                        onValueChange = { tripsStr = it },
                        label = { Text("Trips Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = earningsStr,
                        onValueChange = { earningsStr = it },
                        label = { Text("Earnings (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = cashCollectionStr,
                        onValueChange = { cashCollectionStr = it },
                        label = { Text("Cash Coll. (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = expenditureStr,
                        onValueChange = { expenditureStr = it },
                        label = { Text("Expenditure (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = drivePassStr,
                        onValueChange = { drivePassStr = it },
                        label = { Text("Drive Pass (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = advanceStr,
                        onValueChange = { advanceStr = it },
                        label = { Text("Advance (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                // SVM FLEET Commission Live Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryPurpleContainer)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "SVM Commission: ${calc.commissionPercentage.toInt()}% = ₹${calc.commissionAmount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            fontSize = 13.sp
                        )
                        Text(
                            text = calc.remarks,
                            fontSize = 11.sp,
                            color = TextDark
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = paymentCashStr,
                        onValueChange = { paymentCashStr = it },
                        label = { Text("Pay Cash (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = paymentOnlineStr,
                        onValueChange = { paymentOnlineStr = it },
                        label = { Text("Pay Online UPI (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        shiftType,
                        loginDate,
                        loginTime,
                        logoutDate,
                        logoutTime,
                        vehicleNo,
                        driverName,
                        driverPhone,
                        trips,
                        earnings,
                        cashColl,
                        exp,
                        pass,
                        adv,
                        paymentCashStr.toDoubleOrNull() ?: 0.0,
                        paymentOnlineStr.toDoubleOrNull() ?: 0.0,
                        remarks
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(text = "Save Entry", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = TextGray)
            }
        }
    )
}
