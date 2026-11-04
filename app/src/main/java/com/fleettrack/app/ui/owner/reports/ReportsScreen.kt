package com.fleettrack.app.ui.owner.reports

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.ActiveGreenBg
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.InactiveRedBg
import com.fleettrack.app.ui.theme.MetricPurpleBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleChartGradient
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedWeekIndex by viewModel.selectedWeekIndex.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()

    val currentWeekPnL = viewModel.weeklyPnLStatements.getOrElse(selectedWeekIndex) {
        viewModel.weeklyPnLStatements.first()
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    val displayEarnings = when (selectedPeriod) {
        ReportPeriod.DAILY -> if (dailySummary.totalFare > 0) dailySummary.totalFare else 18420.0
        ReportPeriod.WEEKLY -> currentWeekPnL.totalGrossRevenue
        ReportPeriod.MONTHLY -> if (monthlySummary.totalFare > 0) monthlySummary.totalFare else 542180.0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: "< Earnings & P&L"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    text = "Weekly P&L & Earnings",
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Main Filter Pills: Today | Week | Month
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite, PillShape)
                    .border(1.dp, SurfaceBorder, PillShape)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    Pair("Today", ReportPeriod.DAILY),
                    Pair("Week (P&L)", ReportPeriod.WEEKLY),
                    Pair("Month", ReportPeriod.MONTHLY)
                ).forEach { (label, period) ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isSelected) PrimaryPurple else Color.Transparent)
                            .clickable { viewModel.selectPeriod(period) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextGray,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. If "Week" is chosen: Week-by-Week Horizontal Selector
        if (selectedPeriod == ReportPeriod.WEEKLY) {
            item {
                Column {
                    Text(
                        text = "SELECT WEEK",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(viewModel.weeklyPnLStatements) { index, statement ->
                            val isSelected = selectedWeekIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryPurpleContainer else SurfaceWhite)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) PrimaryPurple else SurfaceBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectWeek(index) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = statement.weekLabel,
                                        color = if (isSelected) PrimaryPurple else TextDark,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Text(
                                        text = statement.dateRange,
                                        color = if (isSelected) PrimaryPurple.copy(alpha = 0.8f) else TextMutedGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Total Earnings & Net Profit / Loss Banner Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val subtitle = when (selectedPeriod) {
                                ReportPeriod.DAILY -> "Total Gross (Today)"
                                ReportPeriod.WEEKLY -> "Gross Revenue (${currentWeekPnL.dateRange})"
                                ReportPeriod.MONTHLY -> "Total Gross (October)"
                            }
                            Text(
                                text = subtitle,
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormat.format(displayEarnings),
                                color = TextDark,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Growth or P&L Status Tag
                        if (selectedPeriod == ReportPeriod.WEEKLY) {
                            val isProfitable = currentWeekPnL.isProfit
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (isProfitable) ActiveGreenBg else InactiveRedBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isProfitable) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isProfitable) ActiveGreen else InactiveRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isProfitable) "+${currentWeekPnL.profitMarginPercent}% Profit" else "${currentWeekPnL.profitMarginPercent}% Loss",
                                        color = if (isProfitable) ActiveGreen else InactiveRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(ActiveGreenBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "▲ +14%",
                                    color = ActiveGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Purple Area Chart Curve
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val fillPath = Path()
                            val width = size.width
                            val height = size.height

                            val points = if (selectedPeriod == ReportPeriod.WEEKLY && !currentWeekPnL.isProfit) {
                                listOf(
                                    Pair(0f, 0.4f),
                                    Pair(0.2f, 0.35f),
                                    Pair(0.4f, 0.55f),
                                    Pair(0.6f, 0.70f),
                                    Pair(0.8f, 0.85f),
                                    Pair(1f, 0.90f) // dip down
                                )
                            } else {
                                listOf(
                                    Pair(0f, 0.8f),
                                    Pair(0.2f, 0.65f),
                                    Pair(0.4f, 0.7f),
                                    Pair(0.6f, 0.45f),
                                    Pair(0.8f, 0.5f),
                                    Pair(1f, 0.2f)
                                )
                            }

                            fillPath.moveTo(0f, height)
                            points.forEachIndexed { index, (xRatio, yRatio) ->
                                val x = xRatio * width
                                val y = yRatio * height
                                if (index == 0) {
                                    path.moveTo(x, y)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }
                            }
                            fillPath.lineTo(width, height)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = PurpleChartGradient
                            )

                            drawPath(
                                path = path,
                                color = if (selectedPeriod == ReportPeriod.WEEKLY && !currentWeekPnL.isProfit) InactiveRed else PrimaryPurple,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // 5. Weekly Profit & Loss Statement (or Daily/Monthly Breakdown)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (selectedPeriod == ReportPeriod.WEEKLY) "WEEKLY PROFIT & LOSS STATEMENT" else "FINANCIAL BREAKDOWN",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    val (uberVal, rapidoVal, driverVal, fuelVal, maintVal, tollVal, netVal) = when (selectedPeriod) {
                        ReportPeriod.WEEKLY -> Tuple7(
                            currentWeekPnL.uberGross,
                            currentWeekPnL.rapidoGross,
                            currentWeekPnL.driverPayouts,
                            currentWeekPnL.fuelExpenses,
                            currentWeekPnL.maintenanceExpenses,
                            currentWeekPnL.otherTolls,
                            currentWeekPnL.netProfitOrLoss
                        )
                        ReportPeriod.DAILY -> Tuple7(10240.0, 8180.0, 9200.0, 3100.0, 800.0, 450.0, 4870.0)
                        ReportPeriod.MONTHLY -> Tuple7(312400.0, 229780.0, 271000.0, 118400.0, 36000.0, 14200.0, 102580.0)
                    }

                    BreakdownRow(
                        label = "Uber Inflow (+)",
                        amount = currencyFormat.format(uberVal),
                        amountColor = TextDark
                    )

                    BreakdownRow(
                        label = "Rapido Inflow (+)",
                        amount = currencyFormat.format(rapidoVal),
                        amountColor = TextDark
                    )

                    BreakdownRow(
                        label = "Driver Payouts (-)",
                        amount = "- ${currencyFormat.format(driverVal)}",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Fuel Expenses (-)",
                        amount = "- ${currencyFormat.format(fuelVal)}",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Maintenance & Repairs (-)",
                        amount = "- ${currencyFormat.format(maintVal)}",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Tolls & Other (-)",
                        amount = "- ${currencyFormat.format(tollVal)}",
                        amountColor = InactiveRed
                    )

                    HorizontalDivider(color = SurfaceBorder)

                    // Net Result Row
                    val isProfit = netVal >= 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isProfit) "Net Profit" else "Net Loss",
                                color = TextDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedPeriod == ReportPeriod.WEEKLY) {
                                Text(
                                    text = if (isProfit) "Surplus after driver & car costs" else "Expenses exceeded weekly gross",
                                    color = TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = if (isProfit) currencyFormat.format(netVal) else "- ${currencyFormat.format(Math.abs(netVal))}",
                            color = if (isProfit) ActiveGreen else InactiveRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Car Profitability Highlight (Weekly)
        if (selectedPeriod == ReportPeriod.WEEKLY) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Earner Car
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = TileShape
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Top Profit Car", color = TextGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentWeekPnL.topProfitableCar,
                                color = ActiveGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Loss or Repair Car
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = TileShape
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "High Expense Car", color = TextGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentWeekPnL.lossOrMaintenanceCar,
                                color = InactiveRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 7. Export Report Button
        item {
            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                shape = PillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Weekly Statement (PDF / Excel)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

private data class Tuple7<A, B, C, D, E, F, G>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G
)

@Composable
private fun BreakdownRow(
    label: String,
    amount: String,
    amountColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = amount,
            color = amountColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
