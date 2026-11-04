package com.fleettrack.app.ui.driver.setup

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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun SetupWizardScreen(
    viewModel: SetupWizardViewModel,
    onSetupComplete: () -> Unit,
    onInspectNodesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Companion Device Setup",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Follow these 4 steps once on each car phone",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Step 1: Accessibility Service
        SetupStepCard(
            stepNumber = 1,
            title = "Ride Tracking Accessibility",
            description = "Enables companion to read trip completion fares on Rapido, Uber and Ola.",
            isCompleted = state.isAccessibilityEnabled,
            buttonText = "Enable Accessibility",
            onAction = { viewModel.openAccessibilitySettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 2: Android 13+ Restricted Settings
        SetupStepCard(
            stepNumber = 2,
            title = "Allow Restricted Settings (Android 13+)",
            description = "If Accessibility is greyed out, tap App info > 3-dots menu > Allow restricted settings.",
            isCompleted = state.isRestrictedSettingsAllowed,
            buttonText = "Open App Info",
            onAction = { viewModel.openAppSettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 3: Unrestricted Battery
        SetupStepCard(
            stepNumber = 3,
            title = "Battery Unrestricted",
            description = "Prevents Android from killing background telemetry when phone is charging in car.",
            isCompleted = state.isBatteryOptimizationIgnored,
            buttonText = "Allow Unrestricted",
            onAction = { viewModel.openBatterySettings(context) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 4: OEM Auto-start
        SetupStepCard(
            stepNumber = 4,
            title = "OEM Auto-Start / Keep Alive",
            description = "On Xiaomi, Vivo, Oppo or Samsung, enable Auto-start and lock app in recents.",
            isCompleted = state.isOemAutoStartConfigured,
            buttonText = "Allow in Settings",
            onAction = { viewModel.openAppSettings(context) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSetupComplete,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
        ) {
            Text(
                text = "Continue to Driver Dashboard",
                color = BackgroundDark,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (onInspectNodesClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onInspectNodesClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = EmeraldAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Debug Node Inspector / View Tree",
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SetupStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    isCompleted: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
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
                Text(
                    text = "STEP $stepNumber",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isCompleted) EmeraldAccent else AlertRed
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "READY" else "ACTION REQUIRED",
                        color = if (isCompleted) EmeraldAccent else AlertRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = TextMuted,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Text(buttonText)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}
