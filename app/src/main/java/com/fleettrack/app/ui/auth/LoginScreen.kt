package com.fleettrack.app.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.fleettrack.app.util.Fast2SmsHelper
import com.fleettrack.app.util.WhatsAppHelper
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDriverCompanionClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Drivers database state
    val registeredDrivers by viewModel.registeredDrivers.collectAsState()

    // Phone input (dynamic, max 10 digits)
    var phoneInput by remember { mutableStateOf("") }

    // Evaluate live role & authorization
    val authStatus = remember(phoneInput, registeredDrivers) {
        viewModel.evaluatePhone(phoneInput)
    }

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
        // Direct In-App Update Banner (Dismissable & Only if new update genuinely exists)
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

        Spacer(modifier = Modifier.height(14.dp))

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

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(PrimaryPurpleContainer.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Commercial Fleet Partner Portal",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryPurple
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Modern Card Container for Login Modal
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
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign in with Mobile",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = "Enter your registered mobile number to receive an instant verification code.",
                    fontSize = 12.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )

                // Phone Input Field with Indian Flag 🇮🇳 & +91 Prefix
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 10) {
                            phoneInput = filtered
                            if (isOtpSent) {
                                isOtpSent = false
                                otpCode = ""
                            }
                        }
                    },
                    label = { Text("Registered Mobile Number") },
                    placeholder = { Text("e.g. 9845122710") },
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
                            IconButton(onClick = {
                                phoneInput = ""
                                isOtpSent = false
                                otpCode = ""
                            }) {
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

                // Authorization Status Feedback Card
                AnimatedVisibility(
                    visible = phoneInput.length == 10,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    when (authStatus) {
                        is AuthStatus.DriverAuthorized -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .border(1.dp, ActiveGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ActiveGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Verified Driver Partner",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1B5E20)
                                        )
                                        Text(
                                            text = "Welcome, ${authStatus.driverName}! Ready for duty check-in.",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }
                        }
                        is AuthStatus.OwnerAuthorized -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryPurpleContainer.copy(alpha = 0.45f))
                                    .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "SVM Fleet Management",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = PrimaryPurple
                                        )
                                        Text(
                                            text = "Authorized Owner Access • Moosapet Central Hub",
                                            fontSize = 11.sp,
                                            color = TextDark
                                        )
                                    }
                                }
                            }
                        }
                        is AuthStatus.Unauthorized -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Mobile Number Not Registered",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFBF360C)
                                        )
                                        Text(
                                            text = "Only drivers registered by SVM FLEET management can log in. Please contact Moosapet Office to onboard.",
                                            fontSize = 11.sp,
                                            color = TextDark,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isAuthorized = authStatus is AuthStatus.OwnerAuthorized || authStatus is AuthStatus.DriverAuthorized

                // OTP Trigger or Entry Box
                if (!isOtpSent) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Primary WhatsApp Verification Button (100% Free & Instant)
                        Button(
                            onClick = {
                                if (!isAuthorized) {
                                    Toast.makeText(
                                        context,
                                        "⛔ Unregistered Mobile: Contact SVM FLEET office to register.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@Button
                                }

                                val newOtp = (1000..9999).random().toString()
                                generatedDemoOtp = newOtp
                                otpCode = newOtp
                                isOtpSent = true

                                val isOwner = authStatus is AuthStatus.OwnerAuthorized
                                val opened = WhatsAppHelper.sendOtpViaWhatsApp(
                                    context = context,
                                    recipientPhone = phoneInput,
                                    otpCode = newOtp,
                                    isOwner = isOwner
                                )

                                if (opened) {
                                    Toast.makeText(context, "Opening WhatsApp with OTP: $newOtp", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "SVM FLEET OTP: $newOtp", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)), // WhatsApp Green
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading && phoneInput.length == 10 && isAuthorized
                        ) {
                            Text(
                                text = "💬 Send OTP via WhatsApp (Free)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Secondary Fast2SMS / Direct Instant OTP option
                        TextButton(
                            onClick = {
                                if (!isAuthorized) {
                                    Toast.makeText(
                                        context,
                                        "⛔ Mobile number is not authorized by SVM FLEET management.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@TextButton
                                }

                                isLoading = true
                                scope.launch {
                                    val newOtp = (1000..9999).random().toString()
                                    generatedDemoOtp = newOtp
                                    otpCode = newOtp

                                    val fast2smsResult = Fast2SmsHelper.sendOtp(phoneInput, newOtp)
                                    isLoading = false
                                    isOtpSent = true

                                    when (fast2smsResult) {
                                        is Fast2SmsHelper.Result.Success -> {
                                            Toast.makeText(context, "✅ SMS sent via Fast2SMS", Toast.LENGTH_LONG).show()
                                        }
                                        is Fast2SmsHelper.Result.Warning -> {
                                            Toast.makeText(context, fast2smsResult.message, Toast.LENGTH_LONG).show()
                                        }
                                        is Fast2SmsHelper.Result.Error -> {
                                            Toast.makeText(context, "SVM FLEET OTP: $newOtp", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = phoneInput.length == 10 && isAuthorized
                        ) {
                            Text(
                                text = "Or send via SMS / Instant Code",
                                color = if (isAuthorized) TextGray else TextMutedGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
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
                            text = "OTP: $generatedDemoOtp",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {
                            val newCode = (1000..9999).random().toString()
                            generatedDemoOtp = newCode
                            otpCode = newCode
                            WhatsAppHelper.sendOtpViaWhatsApp(
                                context = context,
                                recipientPhone = phoneInput,
                                otpCode = newCode,
                                isOwner = authStatus is AuthStatus.OwnerAuthorized
                            )
                            Toast.makeText(context, "Resent via WhatsApp: $newCode", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = "Resend WhatsApp", color = Color(0xFF25D366), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Verify & Access Button
                Button(
                    onClick = {
                        if (!isOtpSent) {
                            Toast.makeText(context, "Please send OTP first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (otpCode.isBlank()) {
                            Toast.makeText(context, "Please enter the OTP code", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)

                        when (authStatus) {
                            is AuthStatus.OwnerAuthorized -> {
                                // Save Persistent Owner Session
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("role", "OWNER")
                                    .putString("phone", phoneInput)
                                    .apply()

                                Toast.makeText(context, "Welcome, Fleet Owner!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                            is AuthStatus.DriverAuthorized -> {
                                // Save Persistent Driver Shift Session
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("role", "DRIVER")
                                    .putString("phone", phoneInput)
                                    .putLong("driver_id", authStatus.driverId)
                                    .putString("driver_name", authStatus.driverName)
                                    .putBoolean("is_shift_active", true)
                                    .apply()

                                Toast.makeText(context, "Welcome, ${authStatus.driverName}! Opening Shift Mode...", Toast.LENGTH_SHORT).show()
                                onDriverCompanionClick()
                            }
                            else -> {
                                Toast.makeText(
                                    context,
                                    "⛔ Unauthorized: Mobile number not registered by Fleet Owner.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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
                        text = "Verify & Enter Portal",
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
            text = "SVM FLEET • Moosapet, Hyderabad 500018\nDriver Onboarding & Support: +91 93919 15013, +91 99598 59347",
            color = TextMutedGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
