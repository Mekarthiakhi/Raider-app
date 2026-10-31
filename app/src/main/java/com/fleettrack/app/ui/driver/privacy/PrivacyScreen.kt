package com.fleettrack.app.ui.driver.privacy

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.AlertRed
import com.fleettrack.app.ui.theme.BackgroundDark
import com.fleettrack.app.ui.theme.EmeraldAccent
import com.fleettrack.app.ui.theme.TextMuted
import com.fleettrack.app.ui.theme.TextPrimary
import com.fleettrack.app.ui.theme.TextSecondary
import com.fleettrack.app.ui.theme.TileShape

@Composable
fun PrivacyScreen(
    modifier: Modifier = Modifier
) {
    var hasConsented by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                tint = EmeraldAccent
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Driver Privacy & Consent",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "India DPDP Act 2023 Compliant",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Consent status card
        GlassCard(
            shape = TileShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Telemetry Consent",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (hasConsented) "Active: Fleet owner receives trip fare & distance" else "Revoked: No telemetry transmitted",
                        color = if (hasConsented) EmeraldAccent else AlertRed,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = hasConsented,
                    onCheckedChange = { hasConsented = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldAccent,
                        checkedTrackColor = BackgroundDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "What is monitored?",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrivacyFactCard(
            title = "Ride App Screens Only",
            description = "The accessibility service activates exclusively when Rapido Captain, Uber Driver, or Ola Driver is in the foreground.",
            icon = Icons.Default.Shield
        )

        Spacer(modifier = Modifier.height(10.dp))

        PrivacyFactCard(
            title = "Trip Telemetry Stored",
            description = "Only ride fare, distance in km, trip duration, pickup/drop area names, and timestamps are recorded for fleet accounting.",
            icon = Icons.Default.Lock
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "What is NEVER accessed?",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(
            shape = TileShape,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                listOf(
                    "❌ No phone calls or WhatsApp messages",
                    "❌ No contacts or address book",
                    "❌ No SMS or OTP reading",
                    "❌ No GPS location tracking outside ride apps",
                    "❌ No photos, camera or gallery access",
                    "❌ No automated clicks or simulated touches"
                ).forEach { item ->
                    Text(
                        text = item,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun PrivacyFactCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    GlassCard(
        shape = TileShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldAccent,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

