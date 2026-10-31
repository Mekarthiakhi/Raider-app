package com.fleettrack.app.ui.owner.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.local.entity.RideEntity
import com.fleettrack.app.data.model.Confidence
import com.fleettrack.app.ui.components.ConfidenceBadge
import com.fleettrack.app.ui.components.EmptyState
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.components.PlatformChip
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EarningsGold
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LiveFeedScreen(
    viewModel: LiveFeedViewModel,
    onRideClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val rides by viewModel.recentRides.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Ride Stream",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Realtime telemetry from all 30 cars",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (rides.isEmpty()) {
            EmptyState(
                title = "No rides recorded yet",
                subtitle = "When companion devices detect rides on Rapido, Uber, or Ola, they stream here instantly."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(rides, key = { it.id }) { ride ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -20 })
                    ) {
                        RideFeedItem(
                            ride = ride,
                            onClick = { onRideClick(ride.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideFeedItem(
    ride: RideEntity,
    onClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    GlassCard(
        onClick = onClick,
        shape = TileShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformChip(platform = ride.platform)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Car #${ride.vehicleId}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ConfidenceBadge(confidence = Confidence(ride.confidence))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = ride.fare?.let { currencyFormat.format(it) } ?: "Fare pending",
                        color = EarningsGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${ride.distanceKm ?: 0.0} km • ${ride.durationMin?.toInt() ?: 0} min",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ride.status.name,
                        color = when (ride.status.name) {
                            "COMPLETED" -> EmeraldAccent
                            "CANCELLED" -> AlertRed
                            else -> TextSecondary
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (ride.needsReview) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Needs Review",
                            color = AlertRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

