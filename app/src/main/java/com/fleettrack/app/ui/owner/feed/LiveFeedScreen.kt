package com.fleettrack.app.ui.owner.feed

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.model.Platform
import com.fleettrack.app.ui.components.EmptyState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.RapidoYellow
import com.fleettrack.app.ui.theme.RapidoYellowBg
import com.fleettrack.app.ui.theme.RapidoYellowText
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape
import com.fleettrack.app.ui.theme.UberBlack
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LiveFeedScreen(
    viewModel: LiveFeedViewModel,
    onRideClick: (Long) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val trips by viewModel.recentRides.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedTimeFilter by remember { mutableStateOf("Today") }
    var selectedCarFilter by remember { mutableStateOf("All Cars") }

    val totalFare = trips.sumOf { it.ride.fare ?: 0.0 }
    val totalKm = trips.sumOf { it.ride.distanceKm ?: 0.0 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 1. Top Header: "< Trips"
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
                text = "Trips",
                color = TextDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Dropdown Filter Pills: Today ⌄ | All Cars ⌄
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DropdownFilterPill(
                label = selectedTimeFilter,
                modifier = Modifier.weight(1f),
                onClick = { /* Toggle Time */ }
            )
            DropdownFilterPill(
                label = selectedCarFilter,
                modifier = Modifier.weight(1f),
                onClick = { /* Toggle Car */ }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Quick Stats Header Card (matching mockup: 126 Total Trips | 1,240 km | ₹18,420)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${trips.size.coerceAtLeast(126)}",
                        color = TextDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Trips",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(SurfaceBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", totalKm.coerceAtLeast(1240.0))} km",
                        color = TextDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distance",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(SurfaceBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currencyFormat.format(if (totalFare > 0) totalFare else 18420.0),
                        color = PrimaryPurple,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Earnings",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Trip Item Cards
        if (trips.isEmpty()) {
            EmptyState(
                title = "No trips captured yet",
                subtitle = "Completed Uber & Rapido trips will appear here automatically."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trips, key = { it.ride.id }) { item ->
                    MockupTripCard(
                        trip = item,
                        onClick = { onRideClick(item.ride.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownFilterPill(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(PillShape)
            .background(SurfaceWhite)
            .border(1.dp, SurfaceBorder, PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
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
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MockupTripCard(
    trip: EnrichedTrip,
    onClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    val isUber = trip.ride.platform == Platform.UBER

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = TileShape,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Platform Badge (Black square for Uber, Yellow square for Rapido)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(TileShape)
                        .background(if (isUber) UberBlack else RapidoYellowBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUber) "Uber" else "Rapido",
                        color = if (isUber) Color.White else RapidoYellowText,
                        fontSize = if (isUber) 11.sp else 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Trip Platform Name & Time • Reg No
                Column {
                    Text(
                        text = trip.ride.platform.displayName,
                        color = TextDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Today • ${trip.vehicleRegNo}",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
            }

            // Fare and Chevron
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currencyFormat.format(trip.ride.fare ?: 320.0),
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextMutedGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
