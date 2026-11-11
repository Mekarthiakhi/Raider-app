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
import com.fleettrack.app.ui.components.SearchableDropdownOption
import com.fleettrack.app.ui.components.SearchableDropdownPill
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
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
    val fleetVehicles by viewModel.vehicles.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    currencyFormat.maximumFractionDigits = 0

    var selectedTimeFilter by remember { mutableStateOf("All Time") }
    var selectedCarFilter by remember { mutableStateOf("All Cars") }

    val timeOptions = remember {
        listOf(
            SearchableDropdownOption("All Time", "All Time", "Show all recorded trips"),
            SearchableDropdownOption("Today", "Today", "Trips completed today"),
            SearchableDropdownOption("Yesterday", "Yesterday", "Previous day trips"),
            SearchableDropdownOption("This Week", "This Week", "Last 7 days"),
            SearchableDropdownOption("This Month", "This Month", "Last 30 days")
        )
    }

    val carOptions = remember(fleetVehicles, trips) {
        val uniqueCars = linkedMapOf<String, String?>()
        fleetVehicles.forEach { v -> uniqueCars[v.regNo] = v.nickname }
        trips.forEach { t -> uniqueCars.putIfAbsent(t.vehicleRegNo, t.vehicleModel) }
        listOf(SearchableDropdownOption("All Cars", "All Cars", "${uniqueCars.size} fleet vehicles")) +
            uniqueCars.entries.map { (regNo, model) ->
                SearchableDropdownOption(
                    value = regNo,
                    label = regNo,
                    subtitle = model
                )
            }
    }

    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    val todayDate = now.toLocalDateTime(tz).date
    val yesterdayDate = (now - 1.days).toLocalDateTime(tz).date
    val weekStartDate = (now - 6.days).toLocalDateTime(tz).date
    val monthStartDate = (now - 29.days).toLocalDateTime(tz).date

    val filteredTrips = remember(trips, selectedTimeFilter, selectedCarFilter) {
        val selectedCarNorm = selectedCarFilter.replace(" ", "").uppercase()
        trips.filter { item ->
            val regNorm = item.vehicleRegNo.replace(" ", "").uppercase()
            val carMatches = selectedCarFilter == "All Cars" ||
                regNorm == selectedCarNorm ||
                item.vehicleRegNo.contains(selectedCarFilter, ignoreCase = true)
            val rideDate = item.ride.acceptedAt.toLocalDateTime(tz).date
            val timeMatches = when (selectedTimeFilter) {
                "Today" -> rideDate == todayDate
                "Yesterday" -> rideDate == yesterdayDate
                "This Week" -> rideDate >= weekStartDate
                "This Month" -> rideDate >= monthStartDate
                else -> true
            }
            carMatches && timeMatches
        }
    }

    val totalFare = filteredTrips.sumOf { it.ride.fare ?: 0.0 }
    val totalKm = filteredTrips.sumOf { it.ride.distanceKm ?: 0.0 }

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

        // 2. Searchable Dropdown Filter Pills: Timeframe ⌄ | Car ⌄
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SearchableDropdownPill(
                label = selectedTimeFilter,
                selectedValue = selectedTimeFilter,
                options = timeOptions,
                dialogTitle = "Filter Trips by Date",
                searchPlaceholder = "Search Today, Week, Month...",
                onOptionSelected = { selectedTimeFilter = it.value },
                modifier = Modifier.weight(1f)
            )
            SearchableDropdownPill(
                label = selectedCarFilter,
                selectedValue = selectedCarFilter,
                options = carOptions,
                dialogTitle = "Filter Trips by Vehicle",
                searchPlaceholder = "Search car plate or model...",
                onOptionSelected = { selectedCarFilter = it.value },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Quick Stats Header Card (dynamic for filtered trips)
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
                        text = "${filteredTrips.size}",
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
                        text = "${String.format(Locale.US, "%.0f", totalKm)} km",
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
                        text = currencyFormat.format(totalFare),
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

        // 4. Active filter status + Reset shortcut
        if (selectedTimeFilter != "All Time" || selectedCarFilter != "All Cars") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredTrips.size} trips • $selectedTimeFilter • $selectedCarFilter",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Reset",
                    color = PrimaryPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            selectedTimeFilter = "All Time"
                            selectedCarFilter = "All Cars"
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 5. Trip Item Cards
        if (filteredTrips.isEmpty()) {
            EmptyState(
                title = "No trips for $selectedCarFilter ($selectedTimeFilter)",
                subtitle = "Try switching to All Time or selecting All Cars above."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredTrips, key = { it.ride.id }) { item ->
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
                        text = "${formatTripDayTime(trip.ride.acceptedAt)} • ${trip.vehicleRegNo}",
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

private fun formatTripDayTime(instant: Instant): String {
    val now = Clock.System.now()
    val tz = TimeZone.currentSystemDefault()
    val todayDate = now.toLocalDateTime(tz).date
    val yesterdayDate = (now - 1.days).toLocalDateTime(tz).date
    val local = instant.toLocalDateTime(tz)
    val hour24 = local.hour
    val amPm = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val timeStr = String.format(Locale.ENGLISH, "%02d:%02d %s", hour12, local.minute, amPm)
    val dayStr = when (local.date) {
        todayDate -> "Today"
        yesterdayDate -> "Yesterday"
        else -> {
            val monthShort = local.month.name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
            String.format(Locale.ENGLISH, "%02d %s", local.dayOfMonth, monthShort)
        }
    }
    return "$dayStr, $timeStr"
}
