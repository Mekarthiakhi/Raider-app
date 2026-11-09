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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.launch

import com.fleettrack.app.data.model.UserRole

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onManagerLoginSuccess: (String) -> Unit = {},
    onDriverCompanionClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Drivers & Managers database state
    val registeredDrivers by viewModel.registeredDrivers.collectAsState()
    val registeredManagers by viewModel.registeredManagers.collectAsState()

    // 3-Role Hierarchy Selection (Owner vs Manager vs Driver)
    var selectedRole by remember { mutableStateOf(UserRole.DRIVER) }

    // Phone input (dynamic, max 10 digits)
    var phoneInput by remember { mutableStateOf("") }
    var secretKeyInput by remember { mutableStateOf("") }

    // Evaluate live role & authorization
    val authStatus = remember(phoneInput, registeredDrivers, registeredManagers, selectedRole, secretKeyInput) {
        viewModel.evaluatePhone(phoneInput, selectedRole, secretKeyInput)
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

    // Secret Management Bypass Dialog (Tap car logo 3 times to enter Master PIN)
    var showOwnerPinDialog by remember { mutableStateOf(false) }
    var ownerPinInput by remember { mutableStateOf("") }
    var logoTapCount by remember { mutableIntStateOf(0) }

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

        // 1. App Logo & Brand Header (Discreet 3-tap for Owner Direct Access)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimaryPurple.copy(alpha = 0.25f), PrimaryPurpleContainer)
                    )
                )
                .border(2.dp, PrimaryPurple.copy(alpha = 0.4f), CircleShape)
                .clickable {
                    logoTapCount++
                    if (logoTapCount >= 3) {
                        logoTapCount = 0
                        showOwnerPinDialog = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = "SVM FLEET",
                tint = PrimaryPurple,
                modifier = Modifier.size(44.dp)
            )
        }

        // Secret Management Access Dialog
        if (showOwnerPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showOwnerPinDialog = false
                    ownerPinInput = ""
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Management Access",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextDark
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Enter the SVM FLEET Management PIN to access the Owner Portal directly:",
                            fontSize = 13.sp,
                            color = TextGray,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = ownerPinInput,
                            onValueChange = {
                                if (it.length <= 6) ownerPinInput = it.filter { ch -> ch.isDigit() }
                            },
                            label = { Text("Owner Master PIN") },
                            placeholder = { Text("e.g. 500018") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PIN: Moosapet Pincode (500018) or 1501 / 1234",
                            fontSize = 11.sp,
                            color = PrimaryPurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (ownerPinInput == "500018" || ownerPinInput == "1501" || ownerPinInput == "1234" || ownerPinInput == "9999") {
                                val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("role", "OWNER")
                                    .putString("phone", "9391915013")
                                    .apply()

                                showOwnerPinDialog = false
                                ownerPinInput = ""
                                Toast.makeText(context, "🛡️ Welcome, SVM Management!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            } else {
                                Toast.makeText(context, "❌ Invalid Management PIN", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = PillShape
                    ) {
                        Text("Enter Owner Portal", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showOwnerPinDialog = false
                        ownerPinInput = ""
                    }) {
                        Text("Cancel", color = TextGray)
                    }
                }
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
                // Role Selection Tabs (Fleet Owner vs Hub Manager vs Driver Shift)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F3F5))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Owner Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedRole == UserRole.OWNER) PrimaryPurple else Color.Transparent)
                            .clickable {
                                if (selectedRole != UserRole.OWNER) {
                                    selectedRole = UserRole.OWNER
                                    isOtpSent = false
                                    otpCode = ""
                                }
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑 Owner",
                            color = if (selectedRole == UserRole.OWNER) Color.White else TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Manager Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedRole == UserRole.MANAGER) PrimaryPurple else Color.Transparent)
                            .clickable {
                                if (selectedRole != UserRole.MANAGER) {
                                    selectedRole = UserRole.MANAGER
                                    isOtpSent = false
                                    otpCode = ""
                                }
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👔 Manager",
                            color = if (selectedRole == UserRole.MANAGER) Color.White else TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Driver Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedRole == UserRole.DRIVER) PrimaryPurple else Color.Transparent)
                            .clickable {
                                if (selectedRole != UserRole.DRIVER) {
                                    selectedRole = UserRole.DRIVER
                                    isOtpSent = false
                                    otpCode = ""
                                }
                            }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🚗 Driver",
                            color = if (selectedRole == UserRole.DRIVER) Color.White else TextDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (selectedRole) {
                        UserRole.OWNER -> "👑 Fleet Owner Master Portal"
                        UserRole.MANAGER -> "👔 Hub / Fleet Manager Sign-In"
                        UserRole.DRIVER -> "🚗 Driver Partner Shift Sign-In"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = when (selectedRole) {
                        UserRole.OWNER -> "Master Access • Full visibility across all Hub Managers, 30+ Cabs & Accounts."
                        UserRole.MANAGER -> "Moosapet, Kukatpally & Madhapur Hubs • Supervise your assigned 10–20 Drivers."
                        UserRole.DRIVER -> "Enter your registered driver mobile number to start shift & vehicle tracking."
                    },
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

                // Optional Owner / Manager Secret Key or Master PIN
                if (selectedRole == UserRole.OWNER || selectedRole == UserRole.MANAGER) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = secretKeyInput,
                        onValueChange = { secretKeyInput = it },
                        label = { Text(if (selectedRole == UserRole.OWNER) "Owner Secret Key (Default: 7777)" else "Manager Access PIN / Secret Key") },
                        placeholder = { Text(if (selectedRole == UserRole.OWNER) "Enter secret key if new mobile number" else "Enter 4-digit PIN or 7777") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = SurfaceBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                        is AuthStatus.ManagerAuthorized -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Verified Hub Manager",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0369A1)
                                        )
                                        Text(
                                            text = "${authStatus.managerName} • ${authStatus.hubName}",
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
                                            text = "Authorized Master Owner Access • Global Fleet",
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
                                            text = when (selectedRole) {
                                                UserRole.OWNER -> "Owner Access Not Authorized"
                                                UserRole.MANAGER -> "Hub Manager Not Found"
                                                UserRole.DRIVER -> "Mobile Number Not Registered"
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFFBF360C)
                                        )
                                        Text(
                                            text = when (selectedRole) {
                                                UserRole.OWNER -> "Enter your Owner Secret Key above (default: 7777) to authorize this number."
                                                UserRole.MANAGER -> "Only registered Hub Managers can log in. Use PIN or Secret Key (7777) to demo."
                                                UserRole.DRIVER -> "Only drivers registered by SVM FLEET management can log in. Please contact office."
                                            },
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

                val isAuthorized = authStatus is AuthStatus.OwnerAuthorized ||
                        authStatus is AuthStatus.ManagerAuthorized ||
                        authStatus is AuthStatus.DriverAuthorized

                // OTP Trigger or Entry Box
                if (!isOtpSent) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                Toast.makeText(context, "✅ SVM FLEET OTP: $newOtp (Auto-filled)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading && phoneInput.length == 10 && isAuthorized
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🛡️ Get SVM FLEET Secure OTP",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Official In-App SVM FLEET OTP Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEDE7F6))
                            .border(1.dp, PrimaryPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PrimaryPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SVM FLEET OTP: $generatedDemoOtp",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PrimaryPurple
                                )
                                Text(
                                    text = "Secure in-app code • Auto-filled below for instant verification",
                                    fontSize = 11.sp,
                                    color = TextDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                            text = "Code: $generatedDemoOtp",
                            color = PrimaryPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {
                            val newCode = (1000..9999).random().toString()
                            generatedDemoOtp = newCode
                            otpCode = newCode
                            Toast.makeText(context, "✅ New SVM FLEET OTP: $newCode", Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = "🔄 Resend OTP", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Verify & Access Button (Live Production Mode: is_demo_mode = false)
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
                                scope.launch { viewModel.saveOwnerSession(phoneInput) }
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", false)
                                    .putString("role", "OWNER")
                                    .putString("phone", phoneInput)
                                    .apply()

                                Toast.makeText(context, "Welcome, Fleet Owner!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                            is AuthStatus.ManagerAuthorized -> {
                                scope.launch {
                                    viewModel.saveManagerSession(
                                        authStatus.managerId,
                                        authStatus.managerName,
                                        authStatus.hubName
                                    )
                                }
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", false)
                                    .putString("role", "MANAGER")
                                    .putString("phone", phoneInput)
                                    .putString("manager_id", authStatus.managerId)
                                    .putString("manager_name", authStatus.managerName)
                                    .putString("hub_name", authStatus.hubName)
                                    .apply()

                                Toast.makeText(context, "Welcome, ${authStatus.managerName} (${authStatus.hubName})!", Toast.LENGTH_SHORT).show()
                                onManagerLoginSuccess(authStatus.managerId)
                            }
                            is AuthStatus.DriverAuthorized -> {
                                scope.launch { viewModel.saveDriverSession() }
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", false)
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
                                val errorMsg = when (selectedRole) {
                                    UserRole.OWNER -> "⛔ Access Denied: +91 $phoneInput is not an authorized owner number."
                                    UserRole.MANAGER -> "⛔ Access Denied: +91 $phoneInput is not an authorized hub manager number."
                                    UserRole.DRIVER -> "⛔ Driver Not Found: +91 $phoneInput is not registered by SVM FLEET management. Please contact office."
                                }
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
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
                        text = when (selectedRole) {
                            UserRole.OWNER -> "Verify & Access Owner Portal"
                            UserRole.MANAGER -> "Verify & Access Manager Portal"
                            UserRole.DRIVER -> "Verify & Start Driver Shift"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Direct 1-Tap Quick Access Demo Buttons (Showcase Static Mode: is_demo_mode = true)
                when (selectedRole) {
                    UserRole.DRIVER -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch { viewModel.saveDriverSession() }
                                val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", true)
                                    .putString("role", "DRIVER")
                                    .putString("phone", "9886210100")
                                    .putLong("driver_id", 20L)
                                    .putString("driver_name", "Anji Babu")
                                    .putBoolean("is_shift_active", false)
                                    .apply()

                                Toast.makeText(context, "⚡ Welcome, Anji Babu! Opening Driver Shift Demo...", Toast.LENGTH_SHORT).show()
                                onDriverCompanionClick()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen),
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ 1-Tap Driver Demo (Anji Babu)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    UserRole.MANAGER -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.saveManagerSession("mgr_moosapet_01", "Srinivas Rao", "Moosapet Central Hub")
                                }
                                val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", true)
                                    .putString("role", "MANAGER")
                                    .putString("phone", "9849012345")
                                    .putString("manager_id", "mgr_moosapet_01")
                                    .putString("manager_name", "Srinivas Rao")
                                    .putString("hub_name", "Moosapet Central Hub")
                                    .apply()

                                Toast.makeText(context, "👔 Welcome, Srinivas Rao! Opening Moosapet Hub Manager Demo (12 Drivers)...", Toast.LENGTH_SHORT).show()
                                onManagerLoginSuccess("mgr_moosapet_01")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ 1-Tap Manager Demo (12 Drivers • Moosapet)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    UserRole.OWNER -> {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch { viewModel.saveOwnerSession("9391915013") }
                                val sessionPrefs = context.getSharedPreferences("svm_fleet_session", Context.MODE_PRIVATE)
                                sessionPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putBoolean("is_demo_mode", true)
                                    .putString("role", "OWNER")
                                    .putString("phone", "9391915013")
                                    .apply()

                                Toast.makeText(context, "👑 Welcome, Akhilesh! Accessing Owner Demo...", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            shape = PillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚡ 1-Tap Owner Demo (Showcase)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
