package com.fleettrack.app.ui.owner.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.SurfaceBorderDark
import com.fleettrack.app.ui.theme.SurfaceDark
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun ReviewQueueScreen(
    viewModel: ReviewQueueViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviewRides by viewModel.reviewRides.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Review Queue",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${reviewRides.size} items require captain approval",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reviewRides.isEmpty()) {
            EmptyState(
                title = "All clear!",
                subtitle = "No rides flagged with low confidence or missing values."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(reviewRides, key = { it.id }) { ride ->
                    ReviewRideCard(
                        ride = ride,
                        onApprove = { fare, km, note ->
                            viewModel.resolveRide(ride.id, fare, km, note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewRideCard(
    ride: RideEntity,
    onApprove: (fare: Double?, distanceKm: Double?, note: String?) -> Unit
) {
    var fareInput by remember { mutableStateOf(ride.fare?.toString() ?: "") }
    var kmInput by remember { mutableStateOf(ride.distanceKm?.toString() ?: "") }
    var noteInput by remember { mutableStateOf(ride.reviewNote ?: "") }

    GlassCard(
        shape = TileShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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
                        fontWeight = FontWeight.Bold
                    )
                }
                ConfidenceBadge(confidence = Confidence(ride.confidence))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Raw extracted OCR / Node snippet if present
            if (!ride.rawExtracted.isNullOrBlank()) {
                Text(
                    text = "OCR / Node Text: ${ride.rawExtracted}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fareInput,
                    onValueChange = { fareInput = it },
                    label = { Text("Fare (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = SurfaceBorderDark
                    )
                )

                OutlinedTextField(
                    value = kmInput,
                    onValueChange = { kmInput = it },
                    label = { Text("Km") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = SurfaceBorderDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Correction Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = EmeraldAccent,
                    unfocusedBorderColor = SurfaceBorderDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val parsedFare = fareInput.toDoubleOrNull()
                    val parsedKm = kmInput.toDoubleOrNull()
                    onApprove(parsedFare, parsedKm, noteInput.ifBlank { "Approved manually" })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BackgroundDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Approve Ride", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

