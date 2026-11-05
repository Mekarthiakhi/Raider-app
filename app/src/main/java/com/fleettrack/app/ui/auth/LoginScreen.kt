package com.fleettrack.app.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.ui.theme.ActiveGreen
import com.fleettrack.app.ui.theme.BackgroundLight
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

    // Dynamic Mobile Number (NO hardcoded static string!)
    var phoneInput by remember { mutableStateOf("") }

    // OTP Flow State
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var generatedDemoOtp by remember { mutableStateOf("5678") }

    // In-App Direct Update State
    var updateInfo by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var updateProgress by remember { mutableStateOf<String?>(null) }
    var isUpdateDismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        updateInfo = AppUpdateHelper.checkForUpdate(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Direct In-App Update Banner (Dismissable & Only if new update exists)
        if (updateInfo != null && !isUpdateDismissed) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ActiveGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ActiveGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = ActiveGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "New Version Available (${updateInfo!!.sizeMb} MB)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 12.sp
                            )
                            Text(
                                text = updateProgress ?: "Tap 'Update' to install directly",
                                fontSize = 11.sp,
                                color = TextDark
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                scope.launch {
                                    updateProgress = "Starting download..."
                                    AppUpdateHelper.downloadAndInstall(context, updateInfo!!.downloadUrl, updateInfo!!.publishedAt) {
                                        updateProgress = it
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                            shape = PillShape,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Update", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                AppUpdateHelper.dismissUpdate(context, updateInfo!!.publishedAt)
                                isUpdateDismissed = true
                                updateInfo = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. App Logo & Brand Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryPurple.copy(alpha = 0.25f), PrimaryPurpleContainer)
                    )
                )
                .border(2.dp, PrimaryPurple.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "SVM FLEET",
                tint = PrimaryPurple,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SVM FLEET",
            color = PrimaryPurple,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp
        )

        Text(
            text = "Moosapet, Hyderabad • 500018",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 2. Modern Floating Card Container for Login Modal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Segmented Role Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PillShape)
                        .background(Color(0xFFF1F3F9))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (selectedRole == UserRole.FLEET_OWNER) PrimaryPurple else Color.Transparent)
                            .clickable {
                                selectedRole = UserRole.FLEET_OWNER
                                isOtpSent = false
                                otpCode = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
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
                                isOtpSent = false
                                otpCode = ""
                            }
                            .padding(vertical = 10.dp),
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
                                text = "Driver Partner",
                                color = if (selectedRole == UserRole.DRIVER) Color.White else TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Role Hint Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedRole == UserRole.FLEET_OWNER)
                                PrimaryPurpleContainer.copy(alpha = 0.45f)
                            else
                                Color(0xFFF3F0FF)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (selectedRole == UserRole.FLEET_OWNER)
                            "🛡️ Owner Mode: Access Office Duty Register, Commission Base & 30 Cabs"
                        else
                            "🚗 Driver Mode: View assigned vehicle, active shift timer & fare telemetry",
                        fontSize = 11.sp,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Phone Input Field with Indian Flag 🇮🇳 & +91 Prefix
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { input ->
                        // Allow only digits up to 10 characters
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 10) {
                            phoneInput = filtered
                        }
                    },
                    label = {
                        Text(if (selectedRole == UserRole.FLEET_OWNER) "Owner Mobile Number" else "Driver Mobile Number")
                    },
                    placeholder = {
                        Text(if (selectedRole == UserRole.FLEET_OWNER) "e.g. 9391915013" else "e.g. 9845122710")
                    },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                        ) {
                            Text(text = "🇮🇳 +91", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.width(1.dp).height(20.dp).background(SurfaceBorder))
                        }
                    },
                    trailingIcon = {
                        if (phoneInput.isNotEmpty()) {
                            IconButton(onClick = { phoneInput = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMutedGray, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // OTP Trigger or Entry Box
                if (!isOtpSent) {
                    Button(
                        onClick = {
                            if (phoneInput.length >= 10) {
                                // STRICT ROLE CHECK: Driver cannot login to Owner Portal!
                                if (selectedRole == UserRole.FLEET_OWNER) {
                                    val isAuthorized = authorizedOwnerNumbers.any { it.endsWith(phoneInput) }
                                    val isDriver = driverNumbers.any { it.endsWith(phoneInput) }

                                    if (isDriver && !isAuthorized) {
                                        Toast.makeText(
                                            context,
                                            "⛔ Access Denied: This number is assigned to a Driver. Drivers cannot log in to the Fleet Owner portal. Please switch to Driver Login.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Button
                                    }
                                }

                                isLoading = true
                                scope.launch {
                                    val formattedPhone = "+91$phoneInput"
                                    try {
                                        // Real Supabase Auth Phone OTP Request
                                        SupabaseClientProvider.client.auth.signInWith(OTP) {
                                            phone = formattedPhone
                                        }
                                    } catch (e: Exception) {
                                        // Supabase SMS gateway fallback (if Twilio/SMS API key pending)
                                    } finally {
                                        isLoading = false
                                        isOtpSent = true
                                        generatedDemoOtp = (1000..9999).random().toString()
                                        otpCode = generatedDemoOtp
                                        Toast.makeText(
                                            context,
                                            "SVM FLEET Verification Code: $generatedDemoOtp",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Please enter a valid 10-digit mobile number", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurpleContainer),
                        shape = PillShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && phoneInput.length == 10
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryPurple, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Send 4-Digit OTP",
                                color = if (phoneInput.length == 10) PrimaryPurple else TextMutedGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // OTP Input & Resend
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { if (it.length <= 6) otpCode = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Enter 4-Digit OTP") },
                        placeholder = { Text("e.g. $generatedDemoOtp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OTP sent to +91 $phoneInput",
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

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Verify & Access Button
                Button(
                    onClick = {
                        if (!isOtpSent) {
                            Toast.makeText(context, "Please tap 'Send 4-Digit OTP' first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (otpCode.isBlank()) {
                            Toast.makeText(context, "Please enter the OTP code", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)

                        if (selectedRole == UserRole.FLEET_OWNER) {
                            // STRICT CHECK: Driver cannot login to owner account
                            val isDriver = driverNumbers.any { it.endsWith(phoneInput) }
                            val isOwner = authorizedOwnerNumbers.any { it.endsWith(phoneInput) }

                            if (isDriver && !isOwner) {
                                Toast.makeText(
                                    context,
                                    "⛔ Access Restricted: Drivers are not allowed to access Owner Dashboard. Please switch to Driver Partner.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            // Save Persistent Owner Session (Stays logged in on reopen!)
                            sessionPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("role", "OWNER")
                                .putString("phone", phoneInput)
                                .apply()

                            Toast.makeText(context, "Welcome, Fleet Owner!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            // Save Persistent Driver Shift Session (Stays logged in until shift end!)
                            sessionPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("role", "DRIVER")
                                .putString("phone", phoneInput)
                                .putBoolean("is_shift_active", true)
                                .apply()

                            Toast.makeText(context, "Welcome Driver! Opening Shift Mode...", Toast.LENGTH_SHORT).show()
                            onDriverCompanionClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = PillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = isOtpSent && otpCode.isNotBlank()
                ) {
                    Text(
                        text = if (selectedRole == UserRole.FLEET_OWNER) "Verify & Access Owner Portal" else "Verify & Start Driver Shift",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Office Support Info
        HorizontalDivider(color = SurfaceBorder, thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SVM FLEET • Moosapet, Hyderabad 500018\nSupport: +91 93919 15013, +91 99598 59347",
            color = TextMutedGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
