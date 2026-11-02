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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
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
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.PurpleChartGradient
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
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
    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    val currentSummary = when (selectedPeriod) {
        ReportPeriod.DAILY -> dailySummary
        ReportPeriod.WEEKLY -> weeklySummary
        ReportPeriod.MONTHLY -> monthlySummary
    }

    val displayEarnings = when (selectedPeriod) {
        ReportPeriod.DAILY -> if (dailySummary.totalFare > 0) dailySummary.totalFare else 18420.0
        ReportPeriod.WEEKLY -> if (weeklySummary.totalFare > 0) weeklySummary.totalFare else 128450.0
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
        // 1. Top Header: "< Earnings"
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
                    text = "Earnings",
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Segmented Filter Pills: Today | Week | Month
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
                    Pair("Week", ReportPeriod.WEEKLY),
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

        // 3. Hero Earnings Card with Curved Area Chart
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currencyFormat.format(displayEarnings),
                                color = TextDark,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (selectedPeriod) {
                                    ReportPeriod.DAILY -> "Total Earnings Today"
                                    ReportPeriod.WEEKLY -> "Total Earnings This Week"
                                    ReportPeriod.MONTHLY -> "Total Earnings (Oct)"
                                },
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        }

                        // Growth Badge
                        Box(
                            modifier = Modifier
                                .background(ActiveGreenBg, PillShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "▲ +14%",
                                color = ActiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Smooth Wave Line Chart Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val path = Path().apply {
                                moveTo(0f, h * 0.75f)
                                cubicTo(w * 0.25f, h * 0.85f, w * 0.35f, h * 0.45f, w * 0.55f, h * 0.55f)
                                cubicTo(w * 0.75f, h * 0.65f, w * 0.85f, h * 0.25f, w, h * 0.35f)
                            }

                            // Fill Path with Gradient
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(PrimaryPurple.copy(alpha = 0.25f), PrimaryPurple.copy(alpha = 0.0f))
                                )
                            )

                            // Stroke Line
                            drawPath(
                                path = path,
                                color = PrimaryPurple,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // 4. Financial Breakdown List Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "FINANCIAL BREAKDOWN",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    BreakdownRow(
                        label = "Uber Earnings",
                        amount = "₹5,12,340",
                        amountColor = TextDark
                    )

                    BreakdownRow(
                        label = "Rapido Earnings",
                        amount = "₹1,00,200",
                        amountColor = TextDark
                    )

                    BreakdownRow(
                        label = "Driver Payments",
                        amount = "- ₹1,20,000",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Fuel Expenses",
                        amount = "- ₹72,680",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Maintenance",
                        amount = "- ₹18,000",
                        amountColor = InactiveRed
                    )

                    BreakdownRow(
                        label = "Other Expenses",
                        amount = "- ₹10,040",
                        amountColor = InactiveRed
                    )

                    HorizontalDivider(color = SurfaceBorder)

                    // Net Profit (Highlighted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Profit",
                            color = TextDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹4,42,180",
                            color = ActiveGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Export Report Button
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
                    text = "Export Report (PDF / Excel)",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    amount: String,
    amountColor: Color = TextDark
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = amount,
            color = amountColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
