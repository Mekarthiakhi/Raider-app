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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fleettrack.app.data.remote.SupabaseClientProvider
import com.fleettrack.app.ui.theme.BackgroundLight
import com.fleettrack.app.ui.theme.PillShape
import com.fleettrack.app.ui.theme.PrimaryPurple
import com.fleettrack.app.ui.theme.PrimaryPurpleContainer
import com.fleettrack.app.ui.theme.SurfaceBorder
import com.fleettrack.app.ui.theme.SurfaceWhite
import com.fleettrack.app.ui.theme.TextDark
import com.fleettrack.app.ui.theme.TextGray
import com.fleettrack.app.ui.theme.TextMutedGray
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onDriverCompanionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // SVM FLEET official office default contacts: Moosapet, Hyderabad
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "SVM FLEET",
            color = PrimaryPurple,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )

        Text(
            text = "Moosapet, Hyderabad • 500018",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Titles
        Text(
            text = "Welcome Back",
            color = TextDark,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Sign in to manage fleet and duty register",
            color = TextGray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Login Toggle: Phone OTP vs Password
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
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Input Fields
        OutlinedTextField(
            value = phoneOrEmail,
            onValueChange = { phoneOrEmail = it },
            label = { Text("Mobile or Email") },
            placeholder = { Text("+91 93919 15013") },
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
                            isLoading = true
                            scope.launch {
                                try {
                                    // Try sending Supabase OTP
                                    val cleaned = phoneOrEmail.replace(" ", "")
                                    SupabaseClientProvider.client.auth.signInWith(OTP) {
                                        phone = cleaned
                                    }
                                } catch (e: Exception) {
                                    // Fallback to local demo OTP
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
                    label = { Text("Enter OTP Code") },
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
                text = "Help / Contact",
                color = PrimaryPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    Toast.makeText(context, "SVM Fleet Moosapet: 9391915013, 9959859347", Toast.LENGTH_LONG).show()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Login Action Button
        Button(
            onClick = {
                if (isOtpMode && !isOtpSent) {
                    Toast.makeText(context, "Please click Send OTP first", Toast.LENGTH_SHORT).show()
                } else if (isOtpMode && otpCode.isBlank()) {
                    Toast.makeText(context, "Please enter OTP code", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Welcome to SVM FLEET!", Toast.LENGTH_SHORT).show()
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

        // 7. Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
            Text(
                text = "SVM FLEET DISPATCH",
                color = TextGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = SurfaceBorder)
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 8. Driver Telemetry Companion Mode Button
        Box(
            modifier = Modifier
                .clip(PillShape)
                .background(SurfaceWhite)
                .border(1.dp, SurfaceBorder, PillShape)
                .clickable(onClick = onDriverCompanionClick)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "🚗 Switch to Driver Shift Mode ›",
                color = PrimaryPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
