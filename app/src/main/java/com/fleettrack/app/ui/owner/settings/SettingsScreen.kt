package com.fleettrack.app.ui.owner.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.ThemeManager
import com.fleettrack.app.util.AppUpdateHelper
import com.fleettrack.app.util.BatteryOptimizationHelper
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackClick: (() -> Unit)? = null,
    onSwitchRoleClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onNodeInspectorClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessionPrefs = remember { context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE) }
    var currentSecretKey by remember { mutableStateOf(sessionPrefs.getString("owner_secret_key", "7777") ?: "7777") }
    var showChangeKeyDialog by remember { mutableStateOf(false) }
    val isDarkTheme = ThemeManager.isDarkTheme

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
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
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Column {
                    Text(
                        text = "Settings",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fleet preferences & device configuration",
                        color = TextGray,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 1. Account & Organization Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = CardShape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ACCOUNT & FLEET",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SettingItemRow(
                        icon = Icons.Default.Person,
                        label = "Switch Role (Owner / Manager / Driver)",
                        onClick = onSwitchRoleClick
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Group,
                        label = "Manage Drivers & Permissions",
                        onClick = { Toast.makeText(context, "Open Team / Drivers from Home or More tab", Toast.LENGTH_SHORT).show() }
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Extension,
                        label = "Platform Integrations (Uber, Rapido, Ola)",
                        onClick = { Toast.makeText(context, "Uber, Rapido & Ola accessibility capture ready", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }

        // Owner Security & Master Secret Key Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = CardShape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "OWNER SECURITY & ACCESS KEY",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showChangeKeyDialog = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secret Key",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Owner Secret Key / Master PIN",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Key: $currentSecretKey • Tap to change",
                                    color = PrimaryPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Change Key",
                            tint = TextMutedGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Person,
                        label = "Authorized: +91 93919 15013 (Akhilesh), +91 99598 59347",
                        onClick = {
                            Toast.makeText(context, "These mobile numbers can log into Owner Mode directly", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // 2. Telemetry, Battery & Cloud Sync Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = CardShape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "TELEMETRY & SYSTEM HEALTH",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 24/7 Battery Optimization Item
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Battery Optimization",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "24/7 Background Telemetry",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
                                        "Unrestricted • Tracking active 24/7"
                                    else
                                        "Tap to enable unrestricted battery",
                                    color = if (BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
                                        ActiveGreen
                                    else
                                        TextMutedGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Configure Battery",
                            tint = TextMutedGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    // Debug Node Inspector
                    SettingItemRow(
                        icon = Icons.Default.BugReport,
                        label = "Debug Node Inspector & Live Dumper",
                        onClick = onNodeInspectorClick
                    )

                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    // Supabase Cloud Sync Status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Cloud sync is live with Supabase Realtime", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ActiveGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Supabase Cloud Sync",
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Supabase Cloud Sync",
                                    color = TextDark,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Realtime telemetry & rides active",
                                    color = ActiveGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(ActiveGreenBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Live",
                                color = ActiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Notifications,
                        label = "Alerts & Offline Vehicle Thresholds",
                        onClick = { }
                    )
                }
            }
        }

        // 3. App Preferences
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), shape = CardShape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PREFERENCES",
                        color = TextMutedGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Dark Theme",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Dark Mode",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { ThemeManager.setDarkTheme(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    val scope = rememberCoroutineScope()
                    SettingItemRow(
                        icon = Icons.Default.SystemUpdate,
                        label = "Check for In-App Updates (Direct Install)",
                        onClick = {
                            scope.launch {
                                android.widget.Toast.makeText(context, "Checking for latest SVM FLEET release...", android.widget.Toast.LENGTH_SHORT).show()
                                val update = AppUpdateHelper.checkForUpdate(context)
                                if (update != null) {
                                    android.widget.Toast.makeText(context, "Downloading update (${update.sizeMb} MB)...", android.widget.Toast.LENGTH_LONG).show()
                                    AppUpdateHelper.downloadAndInstall(context, update.downloadUrl, update.publishedAt) { status ->
                                        // update progress
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "SVM FLEET is already up to date!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Info,
                        label = "About App (SVM FLEET v${com.fleettrack.app.BuildConfig.VERSION_NAME})",
                        onClick = { Toast.makeText(context, "SVM FLEET Build ${com.fleettrack.app.BuildConfig.VERSION_CODE}", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }

        // 4. Logout Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogoutClick,
                shape = CardShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(InactiveRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = InactiveRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Logout",
                        color = InactiveRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showChangeKeyDialog) {
        var newKeyInput by remember { mutableStateOf("") }
        var confirmKeyInput by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangeKeyDialog = false },
            title = {
                Text(text = "Change Owner Secret Key", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This secret key allows logging into Owner Mode from any mobile number (Current: $currentSecretKey).",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                    OutlinedTextField(
                        value = newKeyInput,
                        onValueChange = { newKeyInput = it },
                        label = { Text("New Secret Key (e.g. 4-6 digits)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmKeyInput,
                        onValueChange = { confirmKeyInput = it },
                        label = { Text("Confirm New Key") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            color = InactiveRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newKeyInput.isBlank() || newKeyInput.length < 4) {
                            errorMsg = "Key must be at least 4 digits/characters"
                            return@Button
                        }
                        if (newKeyInput.trim() != confirmKeyInput.trim()) {
                            errorMsg = "New key and confirmation do not match"
                            return@Button
                        }
                        val key = newKeyInput.trim()
                        sessionPrefs.edit().putString("owner_secret_key", key).apply()
                        currentSecretKey = key
                        showChangeKeyDialog = false
                        Toast.makeText(context, "✅ Owner Secret Key changed to: $key", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = PillShape
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeKeyDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            }
        )
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
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
                    imageVector = icon,
                    contentDescription = label,
                    tint = PrimaryPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                color = TextDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Open $label",
            tint = TextMutedGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
