package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatEngine
import com.example.data.UserRole

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginView(onLoginSuccess: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Phone, 1: Email, 2: Google, 3: Facebook, 4: Guest
    
    // Form Inputs
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var adminCode by remember { mutableStateOf("") }
    var isAdminMode by remember { mutableStateOf(false) }

    var errorText by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    val tabsList = listOf("Phone OTP", "Email PIN", "Google", "Facebook", "Guest")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "NETWORK SECURE",
                    color = Color(0xFF00FF00),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HACKER BHAI X ALONE",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Central Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                // Horizontal scroll bar for login options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    tabsList.forEachIndexed { index, name ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFF00FF00).copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF00FF00) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedTab = index
                                    errorText = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.split(" ")[0],
                                color = if (isSelected) Color(0xFF00FF00) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(bottom = 16.dp))

                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() with slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "tabAnim"
                ) { targetTab ->
                    when (targetTab) {
                        0 -> { // Phone OTP
                            Column {
                                Text(
                                    text = "PHONE NUMBER HANDSHAKE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Enter Mobile Phone", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth().testTag("phone_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FF00),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedLabelColor = Color(0xFF00FF00),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                if (isOtpSent) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { otpCode = it },
                                        label = { Text("6-Digit OTP Code", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                                        modifier = Modifier.fillMaxWidth().testTag("otp_input"),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00FF00),
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        1 -> { // Email PIN
                            Column {
                                Text(
                                    text = "SECURE EMAIL HANDSHAKE",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Local Database Email", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FF00),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Encryption Password", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00FF00),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                        }
                        2 -> { // Google Login
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AlternateEmail,
                                    contentDescription = "Google Access",
                                    tint = Color(0xFF00FF00),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Ready to decrypt with secure Google account.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                        3 -> { // Facebook Login
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Facebook Access",
                                    tint = Color(0xFF00FF00),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Inject secure hash from Facebook Identity Provider.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                        4 -> { // Guest Login
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Guest",
                                    tint = Color(0xFF00FF00),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Immediate ephemeral session (Ideal for immediate preview testing)",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "☠️ ERROR: $it",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Admin Login Override
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAdminMode = !isAdminMode }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isAdminMode,
                        onCheckedChange = { isAdminMode = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF00FF00))
                    )
                    Text(
                        text = "Access System Operator Terminal",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (isAdminMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = adminCode,
                        onValueChange = { adminCode = it },
                        label = { Text("Operator Code (Try: ADMIN)", fontFamily = FontFamily.Monospace, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00FF00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                val buttonText = when {
                    selectedTab == 0 && !isOtpSent -> "GET HANDSHAKE OTP"
                    selectedTab == 0 && isOtpSent -> "VERIFY HANDSHAKE"
                    selectedTab == 1 -> "VERIFY SYNC PASS"
                    selectedTab == 2 -> "GOOGLE AUTHORIZATION"
                    selectedTab == 3 -> "FACEBOOK HARNESS"
                    else -> "LAUNCH CODER ACCESS"
                }

                Button(
                    onClick = {
                        errorText = null
                        isLoggingIn = true
                        
                        // Verification conditions
                        if (isAdminMode && adminCode.uppercase() != "ADMIN") {
                            errorText = "INVALID SYSTEM OPERATOR DECRYPT CODE"
                            isLoggingIn = false
                            return@Button
                        }

                        val resolvedRole = if (isAdminMode) UserRole.ADMIN else UserRole.USER

                        when (selectedTab) {
                            0 -> { // Phone OTP
                                if (phoneNumber.isEmpty()) {
                                    errorText = "Handshake phone target cannot be empty"
                                    isLoggingIn = false
                                } else if (!isOtpSent) {
                                    isOtpSent = true
                                    otpCode = "123456" // autofill for easier interaction logs
                                    isLoggingIn = false
                                } else {
                                    // Verify OTP
                                    if (otpCode != "123456") {
                                        errorText = "Cryptographic signature mismatch (Expected 123456)"
                                        isLoggingIn = false
                                    } else {
                                        ChatEngine.login("Phone", phoneNumber, role = resolvedRole)
                                        onLoginSuccess()
                                    }
                                }
                            }
                            1 -> { // Email
                                if (email.isEmpty() || password.isEmpty()) {
                                    errorText = "Authentication parameters cannot be blank"
                                    isLoggingIn = false
                                } else {
                                    ChatEngine.login("Email", email, email, role = resolvedRole)
                                    onLoginSuccess()
                                }
                            }
                            2 -> { // Google Identity simulation
                                ChatEngine.login("Google", "G_Partner_" + (100..999).random(), role = resolvedRole)
                                onLoginSuccess()
                            }
                            3 -> { // Facebook simulation
                                ChatEngine.login("Facebook", "FB_Connect_" + (100..999).random(), role = resolvedRole)
                                onLoginSuccess()
                            }
                            4 -> { // Guest
                                ChatEngine.login("Guest", "GuestBhai", role = resolvedRole)
                                onLoginSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }

            // Footer info
            Text(
                text = "Secure local handshaking is active. Fully dual-linked with Firestore protocols.",
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
