package com.fleettrack.app.ui.owner.reports

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.EarningsText
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.StatItem
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val dailySummary by viewModel.dailySummary.collectAsState()
    val weeklySummary by viewModel.weeklySummary.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()

    val currentSummary = when (selectedPeriod) {
        ReportPeriod.DAILY -> dailySummary
        ReportPeriod.WEEKLY -> weeklySummary
        ReportPeriod.MONTHLY -> monthlySummary
    }

    val periods = listOf(ReportPeriod.DAILY, ReportPeriod.WEEKLY, ReportPeriod.MONTHLY)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Analytics & Reports",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Reconcile earnings and export tax records",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(
            selectedTabIndex = periods.indexOf(selectedPeriod),
            containerColor = SurfaceDark,
            contentColor = EmeraldAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[periods.indexOf(selectedPeriod)]),
                    color = EmeraldAccent
                )
            }
        ) {
            periods.forEach { period ->
                Tab(
                    selected = selectedPeriod == period,
                    onClick = { viewModel.selectPeriod(period) },
                    text = {
                        Text(
                            text = period.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Total Period Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "GROSS TRACKED FARE",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                EarningsText(amount = currentSummary.totalFare)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(label = "Total Rides", value = "${currentSummary.rideCount}")
                    StatItem(
                        label = "Distance",
                        value = "${String.format(Locale.US, "%.1f", currentSummary.totalKm)} km"
                    )
                    StatItem(
                        label = "Avg Fare/Km",
                        value = "₹${String.format(Locale.US, "%.1f", currentSummary.avgFarePerKm)}",
                        valueColor = EmeraldAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reconciliation Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Reconciliation vs Platform Statement",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tracked trip fare is pre-commission. Compare against Rapido, Uber and Ola weekly payouts.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Export Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onExportCsv,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV")
            }

            Button(
                onClick = onExportPdf,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = BackgroundDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

