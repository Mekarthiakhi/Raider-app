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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDriverCompanionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var phoneOrEmail by remember { mutableStateOf("+91 98765 43210") }
    var password by remember { mutableStateOf("fleet@2026") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // OTP Flow State
    var isOtpMode by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

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
                contentDescription = "Fleet Manager",
                tint = PrimaryPurple,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Fleet Manager",
            color = PrimaryPurple,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Titles
        Text(
            text = "Welcome Back",
            color = TextDark,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign in to manage your fleet",
            color = TextGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Login Toggle: Password vs Phone OTP
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
                    .background(if (!isOtpMode) PrimaryPurple else Color.Transparent)
                    .clickable { isOtpMode = false }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Password",
                    color = if (!isOtpMode) Color.White else TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (isOtpMode) PrimaryPurple else Color.Transparent)
                    .clickable { isOtpMode = true }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Phone OTP",
                    color = if (isOtpMode) Color.White else TextDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Input Fields
        OutlinedTextField(
            value = phoneOrEmail,
            onValueChange = { phoneOrEmail = it },
            label = { Text("Mobile or Email") },
            placeholder = { Text("+91 98765 43210") },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMutedGray)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (!isOtpMode) {
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextMutedGray)
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = TextMutedGray
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // OTP Field
            if (!isOtpSent) {
                Button(
                    onClick = {
                        if (phoneOrEmail.isNotBlank()) {
                            isOtpSent = true
                            Toast.makeText(context, "OTP sent to $phoneOrEmail: 5678", Toast.LENGTH_LONG).show()
                            otpCode = "5678" // Default autofill for convenience
                        } else {
                            Toast.makeText(context, "Please enter mobile number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurpleContainer),
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "Send 4-Digit OTP",
                        color = PrimaryPurple,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it },
                    label = { Text("Enter OTP Code") },
                    placeholder = { Text("5678") },
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
                        text = "OTP sent to your number",
                        color = TextGray,
                        fontSize = 11.sp
                    )
                    TextButton(onClick = {
                        Toast.makeText(context, "OTP Resent: 5678", Toast.LENGTH_SHORT).show()
                        otpCode = "5678"
                    }) {
                        Text(text = "Resend OTP", color = PrimaryPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Remember Me & Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryPurple)
                )
                Text(
                    text = "Remember me",
                    color = TextDark,
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Forgot Password?",
                color = PrimaryPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "Reset link sent to registered phone/email", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Login Action Button
        Button(
            onClick = {
                if (isOtpMode && !isOtpSent) {
                    Toast.makeText(context, "Please request OTP first", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Welcome, Akhilesh!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (isOtpMode) "Verify & Login" else "Login",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 7. Divider: "or"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
            Text(
                text = "or",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 8. Continue with Google
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Google Sign-In: Signed in as akhilesh@fleet.in", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            },
            shape = PillShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "G ",
                    color = PrimaryPurple,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Continue with Google",
                    color = TextDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 9. Driver Telemetry Companion Mode Button
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(SurfaceWhite)
                .border(1.dp, SurfaceBorder, PillShape)
                .clickable(onClick = onDriverCompanionClick)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Switch to Driver Companion Mode ›",
                color = PrimaryPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
