package com.fleettrack.app.ui.owner.vault

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.fleettrack.app.ui.theme.MetricPurpleBg
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.ui.theme.TileShape

data class VehicleDocument(
    val id: String,
    val title: String,
    val documentNo: String,
    val issuer: String,
    val expiryDate: String,
    val status: String,
    val isExpiringSoon: Boolean,
    val icon: ImageVector
)

@Composable
fun DocumentVaultScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val carList = listOf("TS09 AB 1234", "TS09 CD 5678", "TS09 EF 9012", "TS09 GH 3456", "TS09 IJ 7890")
    var selectedCar by remember { mutableStateOf(carList[0]) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Document uploaded successfully for $selectedCar", Toast.LENGTH_SHORT).show()
        }
    }

    val documents = remember(selectedCar) {
        listOf(
            VehicleDocument(
                id = "rc",
                title = "Registration Certificate (RC)",
                documentNo = "TS09-RC-2023-${selectedCar.takeLast(4)}",
                issuer = "RTO Hyderabad Central",
                expiryDate = "Valid Until 14 Oct 2038",
                status = "Active",
                isExpiringSoon = false,
                icon = Icons.Default.DirectionsCar
            ),
            VehicleDocument(
                id = "ins",
                title = "Commercial Vehicle Insurance",
                documentNo = "POL-ICICI-88492019",
                issuer = "ICICI Lombard General Insurance",
                expiryDate = "Expires 15 Nov 2026",
                status = "11d left",
                isExpiringSoon = true,
                icon = Icons.Default.Security
            ),
            VehicleDocument(
                id = "puc",
                title = "Pollution Under Control (PUC)",
                documentNo = "PUC-TS-2026-9021",
                issuer = "TS Transport Emission Station",
                expiryDate = "Expires 28 Nov 2026",
                status = "Valid",
                isExpiringSoon = false,
                icon = Icons.Default.CheckCircle
            ),
            VehicleDocument(
                id = "fitness",
                title = "Commercial Fitness Certificate",
                documentNo = "FC-2024-HYD-4412",
                issuer = "Joint Transport Commissioner",
                expiryDate = "Valid Until 10 Oct 2029",
                status = "Active",
                isExpiringSoon = false,
                icon = Icons.Default.Description
            ),
            VehicleDocument(
                id = "permit",
                title = "All India Tourist / State Taxi Permit",
                documentNo = "AITP-TS-09-5561",
                issuer = "State Transport Authority",
                expiryDate = "Valid Until 05 Mar 2028",
                status = "Active",
                isExpiringSoon = false,
                icon = Icons.Default.Description
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header: "< Document Vault"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Document Vault",
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RC, Insurance, PUC & Permits for your fleet",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Car Selection Chips
        item {
            Text(
                text = "Select Vehicle",
                color = TextDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(carList) { car ->
                    val isSelected = selectedCar == car
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                            .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                            .clickable { selectedCar = car }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = car,
                            color = if (isSelected) Color.White else TextDark,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. Upload Action Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { filePickerLauncher.launch("image/*") },
                shape = TileShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MetricPurpleBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = "Upload",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Upload New Document",
                                color = TextDark,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to upload scanned photo or PDF",
                                color = TextMutedGray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(PrimaryPurple)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Upload", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Digital Document Cards
        items(documents, key = { it.id }) { doc ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShape
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
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (doc.isExpiringSoon) InactiveRedBg else PrimaryPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = doc.icon,
                                    contentDescription = null,
                                    tint = if (doc.isExpiringSoon) InactiveRed else PrimaryPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = doc.title,
                                    color = TextDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = doc.documentNo,
                                    color = PrimaryPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(if (doc.isExpiringSoon) InactiveRedBg else ActiveGreenBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = doc.status,
                                color = if (doc.isExpiringSoon) InactiveRed else ActiveGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SurfaceBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = doc.issuer,
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = doc.expiryDate,
                                color = if (doc.isExpiringSoon) InactiveRed else TextDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MetricPurpleBg)
                                .clickable {
                                    Toast.makeText(context, "Opening ${doc.title} file", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = "View", tint = PrimaryPurple, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View File", color = PrimaryPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
