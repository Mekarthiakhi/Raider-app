package com.fleettrack.app.ui.owner.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.ui.components.GlassCard
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.CardShape
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.InactiveRedBg
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun SettingsScreen(
    onBackClick: (() -> Unit)? = null,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDarkTheme by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Header: "< Settings"
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
                    text = "Settings",
                    color = TextDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. Settings Group Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    SettingItemRow(
                        icon = Icons.Default.Person,
                        label = "My Profile",
                        onClick = { }
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Group,
                        label = "Manage Team & Roles",
                        onClick = { }
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Extension,
                        label = "Integrations (Uber, Rapido, GPS)",
                        onClick = { }
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Notifications,
                        label = "Alerts & Reminders",
                        onClick = { }
                    )
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    // Theme Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = "Theme",
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Theme (Light / Dark)",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { isDarkTheme = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryPurple
                            )
                        )
                    }
                    HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                    SettingItemRow(
                        icon = Icons.Default.Info,
                        label = "About App (v1.0.4)",
                        onClick = { }
                    )
                }
            }
        }

        // 3. Logout Card
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

