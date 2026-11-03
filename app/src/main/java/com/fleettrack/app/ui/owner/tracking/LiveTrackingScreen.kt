package com.fleettrack.app.ui.owner.tracking

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

data class LiveCarMarker(
    val regNo: String,
    val model: String,
    val driverName: String,
    val driverPhone: String,
    val locationName: String,
    val distanceAway: String,
    val speedKmh: Int,
    val isOnTrip: Boolean,
    val currentPlatform: String,
    val currentFare: Double,
    val xRatio: Float,
    val yRatio: Float
)

@Composable
fun LiveTrackingScreen(
    onBackClick: () -> Unit,
    onVehicleClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val fleetVehicles = remember {
        listOf(
            LiveCarMarker("TS09 AB 1234", "Hyundai Aura", "Ravi Kumar", "+919876543210", "Banjara Hills, Hyderabad", "8.4 km away", 42, true, "Uber Go", 320.0, 0.52f, 0.48f),
            LiveCarMarker("TS09 CD 5678", "Maruti Dzire", "Suresh Reddy", "+919876543211", "Jubilee Hills, Rd 36", "3.2 km away", 38, true, "Uber Premier", 410.0, 0.32f, 0.35f),
            LiveCarMarker("TS09 EF 9012", "Honda Amaze", "Imran Ali", "+919876543212", "Hitec City / Cyber Towers", "12.1 km away", 55, true, "Rapido Cab", 280.0, 0.74f, 0.28f),
            LiveCarMarker("TS09 GH 3456", "Tata Tigor", "Mahesh B", "+919876543213", "Gachibowli Stadium", "15.0 km away", 0, false, "Idle (Shift Change)", 0.0, 0.82f, 0.65f),
            LiveCarMarker("TS09 IJ 7890", "Hyundai Aura", "Ramesh Patel", "+919876543214", "Begumpet Airport Rd", "6.7 km away", 48, true, "Uber Go", 360.0, 0.28f, 0.72f),
            LiveCarMarker("TS09 KL 2345", "Maruti Dzire", "Kiran V", "+919876543215", "Madhapur Metro", "9.5 km away", 36, true, "Uber Go", 240.0, 0.60f, 0.58f)
        )
    }

    var selectedCar by remember { mutableStateOf(fleetVehicles[0]) }

    // Pulsing radar effect on the selected vehicle
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val radarRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 46f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRadius"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F8))
    ) {
        // 1. Interactive Stylized Map Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background terrain / city block fills
            drawRect(color = Color(0xFFE9ECF2), size = size)

            // Green park zones (KBR National Park / botanical garden representation)
            drawCircle(
                color = Color(0xFFD6EAD7),
                radius = width * 0.24f,
                center = Offset(width * 0.40f, height * 0.42f)
            )
            drawCircle(
                color = Color(0xFFD6EAD7),
                radius = width * 0.16f,
                center = Offset(width * 0.78f, height * 0.22f)
            )

            // Hyderabad Lakes (Hussain Sagar / Durgam Cheruvu)
            val lakePath = Path().apply {
                moveTo(width * 0.15f, height * 0.70f)
                cubicTo(width * 0.22f, height * 0.65f, width * 0.35f, height * 0.68f, width * 0.38f, height * 0.78f)
                cubicTo(width * 0.35f, height * 0.88f, width * 0.20f, height * 0.86f, width * 0.15f, height * 0.70f)
                close()
            }
            drawPath(lakePath, color = Color(0xFFC7DCF5))

            // Main Highway & Arterial Road Grid (White + Gray stroke)
            val roadLines = listOf(
                Pair(Offset(0f, height * 0.35f), Offset(width, height * 0.35f)), // Inner Ring Road
                Pair(Offset(0f, height * 0.60f), Offset(width, height * 0.60f)),
                Pair(Offset(width * 0.30f, 0f), Offset(width * 0.30f, height)),
                Pair(Offset(width * 0.68f, 0f), Offset(width * 0.68f, height)),
                Pair(Offset(0f, height * 0.15f), Offset(width, height * 0.85f)), // Diagonal Expressway
                Pair(Offset(width * 0.10f, height), Offset(width * 0.90f, 0f))
            )

            for ((start, end) in roadLines) {
                // Road casing
                drawLine(
                    color = Color(0xFFD8DDE6),
                    start = start,
                    end = end,
                    strokeWidth = 14f,
                    cap = StrokeCap.Round
                )
                // Road center
                drawLine(
                    color = Color.White,
                    start = start,
                    end = end,
                    strokeWidth = 9f,
                    cap = StrokeCap.Round
                )
            }

            // Route Trail for Selected Vehicle
            val trailPath = Path().apply {
                moveTo(selectedCar.xRatio * width - 60f, selectedCar.yRatio * height + 70f)
                lineTo(selectedCar.xRatio * width - 20f, selectedCar.yRatio * height + 25f)
                lineTo(selectedCar.xRatio * width, selectedCar.yRatio * height)
            }
            drawPath(
                trailPath,
                color = PrimaryPurple.copy(alpha = 0.5f),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // Radar pulse around selected car
            drawCircle(
                color = PrimaryPurple.copy(alpha = radarAlpha),
                radius = radarRadius,
                center = Offset(selectedCar.xRatio * width, selectedCar.yRatio * height)
            )
        }

        // 2. Interactive Vehicle Markers Overlay
        fleetVehicles.forEach { car ->
            val isSelected = car.regNo == selectedCar.regNo
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = (car.xRatio * 320).dp,
                        top = (car.yRatio * 520).dp
                    )
                    .clickable { selectedCar = car },
                contentAlignment = Alignment.Center
            ) {
                // Marker Pin
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 38.dp else 30.dp)
                        .clip(CircleShape)
                        .background(if (car.isOnTrip) ActiveGreen else InactiveRed)
                        .border(
                            width = if (isSelected) 3.dp else 2.dp,
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = car.regNo,
                        tint = Color.White,
                        modifier = Modifier.size(if (isSelected) 22.dp else 16.dp)
                    )
                }
            }
        }

        // 3. Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Live Tracking",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "28 Active • 2 Idle in Hyderabad",
                        color = ActiveGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map Layers",
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                        .clickable {
                            // Cycle to next active car
                            val nextIndex = (fleetVehicles.indexOf(selectedCar) + 1) % fleetVehicles.size
                            selectedCar = fleetVehicles[nextIndex]
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Re-center",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. Quick Car Selector Carousel
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 145.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fleetVehicles, key = { it.regNo }) { car ->
                val isSelected = car.regNo == selectedCar.regNo
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (isSelected) PrimaryPurple else SurfaceWhite)
                        .border(1.dp, if (isSelected) PrimaryPurple else SurfaceBorder, PillShape)
                        .clickable { selectedCar = car }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (car.isOnTrip) ActiveGreen else InactiveRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = car.regNo,
                            color = if (isSelected) Color.White else TextDark,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 5. Bottom Floating Vehicle Status Card (Matching Mockup Sheet 2 Row 2 Screen 6)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Vehicle Icon / Thumbnail
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryPurpleContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedCar.regNo,
                                    color = TextDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(if (selectedCar.isOnTrip) ActiveGreenBg else InactiveRedBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (selectedCar.isOnTrip) "On Trip" else "Idle",
                                        color = if (selectedCar.isOnTrip) ActiveGreen else InactiveRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${if (selectedCar.isOnTrip) "On Trip • " else ""}${selectedCar.distanceAway}",
                                color = if (selectedCar.isOnTrip) ActiveGreen else TextGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = selectedCar.locationName,
                                color = TextMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action Buttons (Call / Route Navigate)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Call Driver
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurpleContainer)
                                .clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${selectedCar.driverPhone}"))
                                    context.startActivity(dialIntent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Driver",
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Navigation Action
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryPurple)
                                .clickable {
                                    onVehicleClick?.invoke(selectedCar.regNo)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Vehicle Details",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Trip metrics: Driver name & current speed / fare
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF7F8FC))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Driver: ${selectedCar.driverName}",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (selectedCar.isOnTrip) "${selectedCar.speedKmh} km/h • ₹${selectedCar.currentFare.toInt()}" else "Parked",
                        color = PrimaryPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
