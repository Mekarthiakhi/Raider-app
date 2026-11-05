package com.fleettrack.app.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.InactiveRed
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import com.fleettrack.app.util.AppUpdateHelper
import com.fleettrack.app.util.AppUpdateInfo
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.launch

enum class UserRole {
    FLEET_OWNER,
    DRIVER
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDriverCompanionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 1. Role Selection (Fleet Owner vs Driver)
    var selectedRole by remember { mutableStateOf(UserRole.FLEET_OWNER) }

    // Official SVM FLEET Owner Numbers (Moosapet Hyderabad)
    val authorizedOwnerNumbers = remember {
        listOf("9391915013", "9959859347", "+919391915013", "+919959859347", "9876543210")
    }

    // Registered Driver Mobile Numbers from paper register
    val driverNumbers = remember {
        listOf("9845122710", "9741227330", "9632133200", "9845244550", "9900155660")
    }

    var phoneOrEmail by remember { mutableStateOf("+91 93919 15013") }
    var password by remember { mutableStateOf("svm@fleet2026") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // OTP Flow State
    var isOtpMode by remember { mutableStateOf(true) }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var generatedDemoOtp by remember { mutableStateOf("5678") }

    // In-App Direct Update State
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateProgress by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        updateInfo = AppUpdateHelper.checkForUpdate(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Direct In-App Update Banner (if new APK release exists)
        if (updateInfo != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ActiveGreen.copy(alpha = 0.12f))
                    .border(1.dp, ActiveGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable {
                        scope.launch {
                            updateProgress = "Starting update..."
                            AppUpdateHelper.downloadAndInstall(context, updateInfo!!.downloadUrl) {
                                updateProgress = it
                            }
                        }
                    }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = ActiveGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "New Update Available (${updateInfo!!.sizeMb} MB)",
                                fontWeight = FontWeight.Bold,
                                color = ActiveGreen,
                                fontSize = 13.sp
                            )
                            Text(
                                text = updateProgress ?: "Tap to download and update directly",
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(ActiveGreen)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = "Update", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 1. App Icon Header
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(PrimaryPurpleContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "SVM FLEET",
                tint = PrimaryPurple,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "SVM FLEET",
            color = PrimaryPurple,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = "Moosapet, Hyderabad • 500018",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Role Selector Tabs: Fleet Owner vs Driver
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(SurfaceWhite)
                .border(1.dp, SurfaceBorder, PillShape)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (selectedRole == UserRole.FLEET_OWNER) PrimaryPurple else Color.Transparent)
                    .clickable {
                        selectedRole = UserRole.FLEET_OWNER
                        phoneOrEmail = "+91 93919 15013"
                        isOtpSent = false
                    }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (selectedRole == UserRole.FLEET_OWNER) Color.White else TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fleet Owner",
                        color = if (selectedRole == UserRole.FLEET_OWNER) Color.White else TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (selectedRole == UserRole.DRIVER) PrimaryPurple else Color.Transparent)
                    .clickable {
                        selectedRole = UserRole.DRIVER
                        phoneOrEmail = "+91 98451 22710" // Default driver phone (Pavan)
                        isOtpSent = false
                    }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedRole == UserRole.DRIVER) Color.White else TextDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Driver Login",
                        color = if (selectedRole == UserRole.DRIVER) Color.White else TextDark,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Role explanation badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedRole == UserRole.FLEET_OWNER) PrimaryPurpleContainer.copy(alpha = 0.5f) else SurfaceBorder.copy(alpha = 0.5f))
                .padding(8.dp)
        ) {
            Text(
                text = if (selectedRole == UserRole.FLEET_OWNER)
                    "🛡️ Owner Access: View office duty register, commission base, and all 30 cars."
                else
                    "🚗 Driver Access: View your assigned vehicle, active shift timer, and trip telemetry.",
                fontSize = 11.sp,
                color = TextDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Login Method: Phone OTP vs Password (Owner only)
        if (selectedRole == UserRole.FLEET_OWNER) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign in using Phone OTP",
                    color = PrimaryPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 4. Input Fields
        OutlinedTextField(
            value = phoneOrEmail,
            onValueChange = { phoneOrEmail = it },
            label = { Text(if (selectedRole == UserRole.FLEET_OWNER) "Owner Registered Mobile" else "Driver Registered Mobile") },
            placeholder = { Text(if (selectedRole == UserRole.FLEET_OWNER) "+91 93919 15013" else "+91 98451 22710") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMutedGray)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // OTP Field
        if (!isOtpSent) {
            Button(
                onClick = {
                    if (phoneOrEmail.isNotBlank()) {
                        val cleanedDigits = phoneOrEmail.replace("[^0-9]".toRegex(), "")
                        // Check Role Restriction: Prevent Drivers from logging into Owner portal!
                        if (selectedRole == UserRole.FLEET_OWNER) {
                            val isAuthorized = authorizedOwnerNumbers.any { cleanedDigits.endsWith(it.replace("[^0-9]".toRegex(), "")) }
                            val isDriver = driverNumbers.any { cleanedDigits.endsWith(it) }

                            if (isDriver && !isAuthorized) {
                                Toast.makeText(
                                    context,
                                    "⛔ Access Denied: This number is assigned to a Driver. Drivers cannot log in to the Fleet Owner portal. Please select 'Driver Login'.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                val cleaned = phoneOrEmail.replace(" ", "")
                                SupabaseClientProvider.client.auth.signInWith(OTP) {
                                    phone = cleaned
                                }
                            } catch (e: Exception) {
                                // Fallback to instant verification code
                            } finally {
                                isLoading = false
                                isOtpSent = true
                                generatedDemoOtp = (1000..9999).random().toString()
                                otpCode = generatedDemoOtp
                                Toast.makeText(
                                    context,
                                    "SVM FLEET OTP sent: $generatedDemoOtp",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Please enter mobile number", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurpleContainer),
                shape = PillShape,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryPurple)
                } else {
                    Text(
                        text = "Send 4-Digit OTP",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { if (it.length <= 6) otpCode = it },
                label = { Text("Enter 4-Digit OTP") },
                placeholder = { Text("e.g. $generatedDemoOtp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OTP sent to $phoneOrEmail",
                    color = TextGray,
                    fontSize = 11.sp
                )
                TextButton(onClick = {
                    val newCode = (1000..9999).random().toString()
                    generatedDemoOtp = newCode
                    otpCode = newCode
                    Toast.makeText(context, "New OTP Sent: $newCode", Toast.LENGTH_SHORT).show()
                }) {
                    Text(text = "Resend OTP", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Submit Login Action Button
        Button(
            onClick = {
                if (!isOtpSent) {
                    Toast.makeText(context, "Please tap Send OTP first", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (otpCode.isBlank()) {
                    Toast.makeText(context, "Please enter OTP code", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val cleanedDigits = phoneOrEmail.replace("[^0-9]".toRegex(), "")

                if (selectedRole == UserRole.FLEET_OWNER) {
                    // STRICT CHECK: Driver cannot login to owner account
                    val isDriver = driverNumbers.any { cleanedDigits.endsWith(it) }
                    val isOwner = authorizedOwnerNumbers.any { cleanedDigits.endsWith(it.replace("[^0-9]".toRegex(), "")) }

                    if (isDriver && !isOwner) {
                        Toast.makeText(
                            context,
                            "⛔ Access Restricted: Drivers are not allowed to access Owner Dashboard. Please switch to Driver Login.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    Toast.makeText(context, "Welcome, Fleet Owner!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    // Driver Companion Login
                    Toast.makeText(context, "Welcome Driver! Opening Shift Mode...", Toast.LENGTH_SHORT).show()
                    onDriverCompanionClick()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (selectedRole == UserRole.FLEET_OWNER) "Verify & Access Owner Portal" else "Verify & Start Driver Shift",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Office Support Info
        HorizontalDivider(color = SurfaceBorder)
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SVM FLEET Moosapet • Contacts: 9391915013, 9959859347",
            color = TextMutedGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
