package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.Client
import com.example.data.RefillOrder
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// ==========================================
// 1. SIGN IN SCREEN (Image 5)
// ==========================================
@Composable
fun SignInScreen(
    viewModel: GasGuardViewModel,
    modifier: Modifier = Modifier
) {
    val role by viewModel.signInRole.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()
    val signInError by viewModel.signInError.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var infoDialogMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Card(
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "App Logo",
                    tint = TealMedium,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = "GAS MONITOR",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TealDark
            )
        )
        Text(
            text = "INDUSTRIAL INTEGRITY SYSTEMS",
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.5.sp,
                color = GrayDark.copy(alpha = 0.7f),
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Sign In Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Sign In / Create Account Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealExtraLight)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealDark)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SIGN IN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("CREATE ACCOUNT", color = TealDark, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Account Type Header
                Text(
                    text = "ACCOUNT TYPE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GrayDark,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Role Toggles (Homeowner vs Supplier)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Homeowner
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateSignInRole("Homeowner") },
                        shape = RoundedCornerShape(12.dp),
                        border = if (role == "Homeowner") ButtonDefaults.outlinedButtonBorder else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (role == "Homeowner") TealLight else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                contentDescription = "Homeowner",
                                tint = TealDark,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Homeowner", color = TealDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Supplier
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.updateSignInRole("Supplier") },
                        shape = RoundedCornerShape(12.dp),
                        border = if (role == "Supplier") ButtonDefaults.outlinedButtonBorder else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (role == "Supplier") TealLight else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Business,
                                contentDescription = "Supplier",
                                tint = TealDark,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Supplier", color = TealDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Email Input
                Text(
                    text = "EMAIL ADDRESS",
                    style = MaterialTheme.typography.labelSmall.copy(color = GrayDark, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.updateEmail(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = TealMedium) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrayDark,
                        unfocusedTextColor = GrayDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TealMedium,
                        unfocusedBorderColor = GrayDark.copy(alpha = 0.3f),
                        focusedLabelColor = TealMedium,
                        unfocusedLabelColor = GrayDark.copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PASSWORD",
                        style = MaterialTheme.typography.labelSmall.copy(color = GrayDark, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "FORGOT?",
                        color = BlueAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { showForgotPasswordDialog = true }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.updatePassword(it) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TealMedium) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = TealMedium
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrayDark,
                        unfocusedTextColor = GrayDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TealMedium,
                        unfocusedBorderColor = GrayDark.copy(alpha = 0.3f),
                        focusedLabelColor = TealMedium,
                        unfocusedLabelColor = GrayDark.copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )

                if (signInError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = signInError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In Button
                Button(
                    onClick = { viewModel.performSignIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Secure Sign In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Arrow", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Partners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GrayDark.copy(alpha = 0.15f))
                    Text(
                        text = " SECURE PARTNERS ",
                        fontSize = 10.sp,
                        color = GrayDark.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GrayDark.copy(alpha = 0.15f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { infoDialogMessage = "SSO Access requires your organization's identity provider to be configured (e.g. Okta, Azure AD). That isn't set up in this build yet - please sign in with email and password above." },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayDark)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = "SSO", tint = TealMedium, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SSO Access", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { infoDialogMessage = "Biometric sign-in isn't set up on this device yet. Once enabled, you'll be able to use your fingerprint or face to sign in instead of a password." },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrayDark)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = "Bio", tint = TealMedium, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Biometrics", fontSize = 11.sp)
                    }
                }
            }
        }

        // Encryption Footers
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "AES", tint = TealMedium, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AES-256 ENCRYPTION", fontSize = 9.sp, color = GrayDark.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = "GDPR", tint = TealMedium, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("GDPR COMPLIANT", fontSize = 9.sp, color = GrayDark.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Access to this system is monitored. By logging in, you agree to our Security Protocol and Terms of Use.",
            fontSize = 11.sp,
            color = GrayDark.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset your password") },
            text = { Text("Self-service password reset isn't available in this build yet. Please contact your account administrator or supplier support to reset your password.") },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Got it") }
            }
        )
    }

    if (infoDialogMessage != null) {
        AlertDialog(
            onDismissRequest = { infoDialogMessage = null },
            title = { Text("Not available yet") },
            text = { Text(infoDialogMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { infoDialogMessage = null }) { Text("Got it") }
            }
        )
    }
}

// ==========================================
// 2. HOMEOWNER DASHBOARD (Image 1, 2, 4)
// ==========================================
@Composable
fun HomeownerDashboard(
    viewModel: GasGuardViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.homeownerTab.collectAsState()
    val onboardingActive by viewModel.onboardingActive.collectAsState()

    if (onboardingActive) {
        OnboardingWizard(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = modifier,
            topBar = {
                HomeownerTopBar(viewModel = viewModel)
            },
            bottomBar = {
                HomeownerBottomBar(viewModel = viewModel, activeTab = activeTab)
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    "Home" -> HomeownerHomeTab(viewModel = viewModel)
                    "Analytics" -> HomeownerAnalyticsTab(viewModel = viewModel)
                    "Refill" -> HomeownerRefillTab(viewModel = viewModel)
                    "Chat" -> ChatTab(viewModel = viewModel)
                }

                // Global dialog overlays
                SyncSuccessDialog(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeownerTopBar(viewModel: GasGuardViewModel) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = "App Shield Logo",
                    tint = TealMedium,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SafeGas Monitor",
                    fontWeight = FontWeight.Bold,
                    color = TealDark,
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (lastSyncTime.isNotEmpty() && !isSyncing) {
                    Text(
                        text = lastSyncTime,
                        fontSize = 11.sp,
                        color = GrayDark.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.syncWithBackend() },
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("homeowner_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TealDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync Data",
                            tint = TealDark
                        )
                    }
                }
            }

            // Profile switcher button (acting as switch mode/profile switcher)
            IconButton(onClick = { viewModel.signOut() }) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out",
                    tint = TealDark
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun HomeownerBottomBar(viewModel: GasGuardViewModel, activeTab: String) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = TealDark
    ) {
        val tabs = listOf(
            Triple("Home", Icons.Filled.GridView, Icons.Outlined.GridView),
            Triple("Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
            Triple("Refill", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation),
            Triple("Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
        )

        tabs.forEach { (tab, filledIcon, outlinedIcon) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { viewModel.updateHomeownerTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (activeTab == tab) filledIcon else outlinedIcon,
                        contentDescription = tab
                    )
                },
                label = { Text(tab, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealDark,
                    selectedTextColor = TealDark,
                    indicatorColor = TealLight,
                    unselectedIconColor = GrayDark,
                    unselectedTextColor = GrayDark
                ),
                modifier = Modifier.testTag("homeowner_tab_${tab.lowercase()}")
            )
        }
    }
}

// 2A. HOMEOWNER HOME TAB (Image 2)
@Composable
fun HomeownerHomeTab(viewModel: GasGuardViewModel) {
    val lpgRemaining by viewModel.lpgRemaining.collectAsState()
    val estDays by viewModel.estimatedDays.collectAsState()
    val ppmReading by viewModel.currentPpmReading.collectAsState()
    val isLeakDetected by viewModel.isLeakDetected.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // System Secure banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLeakDetected) CoralRedBg else TealLight
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLeakDetected) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                        contentDescription = "Status Icon",
                        tint = if (isLeakDetected) CoralRed else TealMedium,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLeakDetected) "GAS LEAK ALERT!" else "SYSTEM SECURE",
                        color = if (isLeakDetected) CoralRed else TealDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Circular Gauge
        item {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 24.dp.toPx()
                    val sizeMin = size.minDimension
                    val offset = strokeWidth / 2
                    val arcSize = sizeMin - strokeWidth

                    // Gray Track Arc
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(offset, offset),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Arc
                    drawArc(
                        color = if (isLeakDetected) CoralRed else TealMedium,
                        startAngle = 135f,
                        sweepAngle = (270f * lpgRemaining).toFloat(),
                        useCenter = false,
                        topLeft = Offset(offset, offset),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(lpgRemaining * 100).toInt()}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealDark
                        )
                    )
                    Text(
                        text = "LPG REMAINING",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = GrayDark.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                    )
                }
            }
        }

        // Estimated Days Remaining Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Estimated Days Remaining",
                        color = GrayDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$estDays Days",
                        color = BlueAccent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.updateHomeownerTab("Refill") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("order_refill_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Order Refill", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Button(
                    onClick = { viewModel.startOnboarding() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("safety_check_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealLight)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, contentDescription = "Shield", tint = TealDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Safety Check / ESP32 Setup", color = TealDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // Live ESP32/MQ-2 connection status (replaces the old "Simulate Gas Leak"
                // debug trigger now that this reads real hardware over Firebase). Pairing
                // itself happens in the "Safety Check / ESP32 Setup" wizard above, not here -
                // this pill just reflects that state and, if nothing's paired yet, jumps you
                // straight into that wizard's first step.
                val sensorStatus by viewModel.sensorConnectionStatus.collectAsState()
                val pairedDeviceId by viewModel.pairedDeviceId.collectAsState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealExtraLight)
                        .let {
                            if (sensorStatus == "Not paired") it.clickable { viewModel.startOnboarding() } else it
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sensorStatus == "Live") Icons.Filled.Sensors else Icons.Filled.SensorsOff,
                        contentDescription = "Sensor link status",
                        tint = when (sensorStatus) {
                            "Live" -> TealMedium
                            "Offline" -> CoralRed
                            else -> GrayDark
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (sensorStatus) {
                                "Live" -> "MQ-2 sensor live"
                                "Offline" -> "Meter offline - check ESP32 power/WiFi"
                                "Connecting..." -> "Connecting to your meter..."
                                "Signing in..." -> "Signing in..."
                                "Not paired" -> "No gas meter paired yet - tap to set up"
                                else -> "Sensor not configured - see README"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (sensorStatus) {
                                "Live" -> TealDark
                                "Offline" -> CoralRed
                                else -> GrayDark
                            }
                        )
                        if (pairedDeviceId != null && sensorStatus != "Not paired") {
                            Text(
                                text = "Meter ID: $pairedDeviceId",
                                fontSize = 10.sp,
                                color = GrayDark.copy(alpha = 0.7f)
                            )
                        }
                    }
                    if (sensorStatus == "Not paired") {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Set up", tint = GrayDark, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Leak History / Telemetry Graph
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        Column {
                            Text("LEAK HISTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark, letterSpacing = 1.sp)
                            Text(
                                text = if (isLeakDetected) "Leak Detected!" else "No leaks detected",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLeakDetected) CoralRed else TealDark
                            )
                        }
                        Text(
                            text = "$ppmReading ppm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLeakDetected) CoralRed else TealDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Minimal visual bar graph of the last 6 real readings recorded from the
                    // ESP32/MQ-2 feed (newest on the right). Empty until the sensor is
                    // configured and has reported in at least once.
                    val recentLeaks by viewModel.latestLeaks.collectAsState()
                    val bars = recentLeaks.take(6).map { it.reading }.reversed()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (bars.isEmpty()) {
                            Text(
                                "No readings yet",
                                fontSize = 11.sp,
                                color = GrayDark.copy(alpha = 0.6f),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        } else {
                            bars.forEach { value ->
                                val barIsLeak = value >= 0.05
                                val scale = (value / 5.0).coerceIn(0.02, 1.0)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (barIsLeak) CoralRed.copy(alpha = scale.toFloat())
                                            else TealMedium.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Safety Bulletins from Backend
        item {
            val bulletins by viewModel.safetyBulletins.collectAsState()
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "OFFICIAL SAFETY BULLETINS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "FROM CLOUD BACKEND",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        letterSpacing = 0.5.sp
                    )
                }

                if (bulletins.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            "No bulletins. Click the sync button in the top bar to fetch from server.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = GrayDark
                        )
                    }
                } else {
                    bulletins.forEach { bulletin ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Campaign,
                                        contentDescription = "Alert",
                                        tint = CoralRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        bulletin.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TealDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    bulletin.body,
                                    fontSize = 13.sp,
                                    color = GrayDark,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2B. HOMEOWNER ANALYTICS TAB (Image 4)
@Composable
fun HomeownerAnalyticsTab(viewModel: GasGuardViewModel) {
    val remainingDays by viewModel.estimatedDays.collectAsState()
    val isLeakDetected by viewModel.isLeakDetected.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Refill banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "PREDICTIVE ANALYSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayDark,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Refill Recommended In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$remainingDays",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealDark
                        )
                        Text(
                            text = " Days",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TealDark,
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                    val avgDailyKg by viewModel.avgDailyConsumptionKg.collectAsState()
                    Text(
                        text = if (avgDailyKg > 0.001)
                            "Based on your average consumption of ${"%.2f".format(avgDailyKg)}kg/day."
                        else
                            "Waiting for consumption data from your ESP32 + MPXV7002DP sensor.",
                        fontSize = 12.sp,
                        color = GrayDark.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.updateHomeownerTab("Refill") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("RESERVE NOW", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Consumption Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val consumptionHistory by viewModel.consumptionHistory.collectAsState()
                    // Oldest -> newest, most recent 7 samples
                    val recentConsumption = consumptionHistory.take(7).map { it.consumptionKgPerHour }.reversed()
                    val avgKgPerDay = if (recentConsumption.isNotEmpty()) {
                        (recentConsumption.average() * 24.0)
                    } else null

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("LPG CONSUMPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark, letterSpacing = 1.sp)
                            Text(
                                text = if (avgKgPerDay != null) "${"%.2f".format(avgKgPerDay)} kg / day" else "No data yet",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw consumption line graph on Canvas from real MPXV7002DP-derived samples
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        if (recentConsumption.size >= 2) {
                            val points = recentConsumption
                            val stepX = size.width / (points.size - 1)
                            val maxVal = (points.maxOrNull() ?: 1.0).coerceAtLeast(0.01)
                            val path = Path()

                            points.forEachIndexed { i, valPoint ->
                                val x = i * stepX
                                val y = size.height - (valPoint / maxVal * size.height).toFloat()
                                if (i == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }

                            drawPath(
                                path = path,
                                color = TealMedium,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEachIndexed { i, valPoint ->
                                val x = i * stepX
                                val y = size.height - (valPoint / maxVal * size.height).toFloat()
                                drawCircle(
                                    color = TealDark,
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (recentConsumption.isEmpty()) {
                        Text(
                            "Connect your ESP32 + MPXV7002DP to see real consumption history here.",
                            fontSize = 11.sp,
                            color = GrayDark.copy(alpha = 0.7f)
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            consumptionHistory.take(7).reversed().forEach { record ->
                                Text(timeFormatter.format(java.util.Date(record.timestamp)), fontSize = 9.sp, color = GrayDark)
                            }
                        }
                    }
                }
            }
        }

        // Hardware Diagnostics
        item {
            Text(
                text = "Hardware Diagnostics",
                fontWeight = FontWeight.Bold,
                color = TealDark,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            val sensorStatus by viewModel.sensorConnectionStatus.collectAsState()
            val espHealthy = sensorStatus == "Live"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ESP32
                DiagnosticCard(
                    title = "ESP32 CORE",
                    status = when (sensorStatus) {
                        "Live" -> "Online"
                        "Offline" -> "Offline"
                        "Connecting..." -> "Connecting"
                        else -> "Not set up"
                    },
                    icon = Icons.Default.DeveloperBoard,
                    isHealthy = espHealthy,
                    modifier = Modifier.weight(1f)
                )
                // MQ-2 Sensor
                DiagnosticCard(
                    title = "MQ-2 SENSOR",
                    status = if (!espHealthy) "No data" else if (isLeakDetected) "ALERT" else "Stable",
                    icon = Icons.Default.Air,
                    isHealthy = espHealthy && !isLeakDetected,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pressure (MPXV7002DP differential pressure, used to derive flow/consumption -
                // this is NOT the cylinder's static regulator pressure)
                val pressureKpa by viewModel.currentPressureKpa.collectAsState()
                val pressureSensorLive by viewModel.sensorConnectionStatus.collectAsState()
                DiagnosticCard(
                    title = "FLOW PRESSURE",
                    status = if (pressureSensorLive == "Live") "${"%.2f".format(pressureKpa)} kPa" else "No data",
                    icon = Icons.Default.Speed,
                    isHealthy = pressureSensorLive == "Live",
                    modifier = Modifier.weight(1f)
                )
                // Uptime
                DiagnosticCard(
                    title = "UPTIME",
                    status = "14d 6h",
                    icon = Icons.Default.Timer,
                    isHealthy = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Smart Insight Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLightBg)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Insight",
                        tint = BlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SMART INSIGHT",
                            fontWeight = FontWeight.Bold,
                            color = TealDark,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Your usage peaks between 6 PM and 8 PM daily. Consider checking for optimized burner settings during this window to save 5%.",
                            fontSize = 12.sp,
                            color = GrayDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticCard(
    title: String,
    status: String,
    icon: ImageVector,
    isHealthy: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isHealthy) TealExtraLight else CoralRedBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isHealthy) TealDark else CoralRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayDark)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isHealthy) TealMedium else CoralRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isHealthy) TealDark else CoralRed)
                }
            }
        }
    }
}

// 2C. HOMEOWNER REFILL TAB (Image 1, Refill Screen)
@Composable
fun HomeownerRefillTab(viewModel: GasGuardViewModel) {
    val selectedOption by viewModel.selectedDeliveryOption.collectAsState()
    val orders by viewModel.refillOrders.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Refill Title
        item {
            Text(
                text = "Schedule Refill",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TealDark)
            )
            Text(
                text = "Configure your propane delivery for Tank #4029-A",
                color = GrayDark,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Delivery preferences
        item {
            Text("DELIVERY PREFERENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeliveryOptionCard(
                    title = "Standard Delivery",
                    desc = "Delivered within 3-5 business days.",
                    price = "$0.00",
                    isSelected = selectedOption == "Standard",
                    icon = Icons.Default.LocalShipping,
                    onClick = { viewModel.selectDeliveryOption("Standard") }
                )
                DeliveryOptionCard(
                    title = "Express Delivery",
                    desc = "Guaranteed next-day fulfillment.",
                    price = "+$15.00",
                    isSelected = selectedOption == "Express",
                    icon = Icons.Default.Bolt,
                    onClick = { viewModel.selectDeliveryOption("Express") }
                )
                DeliveryOptionCard(
                    title = "Emergency Fill",
                    desc = "Dispatch within 4 hours. Priority routing.",
                    price = "+$45.00",
                    isSelected = selectedOption == "Emergency",
                    icon = Icons.Default.Warning,
                    onClick = { viewModel.selectDeliveryOption("Emergency") }
                )
            }
        }

        // Payment Method Box
        item {
            Text("PAYMENT METHOD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TealDark)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text("VISA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("•••• 4242", fontWeight = FontWeight.Bold, color = TealDark)
                            Text("Expires 12/28", fontSize = 11.sp, color = GrayDark)
                        }
                    }
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = TealMedium)
                }
            }
        }

        // Order Summary
        item {
            Text("ORDER SUMMARY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val basePrice = 238.56
                    val surcharge = when (selectedOption) {
                        "Express" -> 15.0
                        "Emergency" -> 45.0
                        else -> 0.0
                    }
                    val inspection = 5.0
                    val total = basePrice + surcharge + inspection

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Propane (85.2 Gallons)", color = GrayDark)
                        Text("$238.56", fontWeight = FontWeight.Bold, color = TealDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Surcharge", color = GrayDark)
                        Text("$$surcharge", fontWeight = FontWeight.Bold, color = TealDark)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Safety Inspection Fee", color = GrayDark)
                        Text("$5.00", fontWeight = FontWeight.Bold, color = TealDark)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                            Text("Includes all taxes and fees", fontSize = 10.sp, color = GrayDark)
                        }
                        Text(
                            text = "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealDark
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.placeRefillOrder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("place_order_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                    ) {
                        Text("Place Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        // Refill History (from Image 1, Refill History View)
        item {
            Text("REFILL HISTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Refill Stats Card Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TealExtraLight)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL REFILLS", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                            Text("24", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealDark)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("LIFETIME VOL.", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                            Text("942g", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealDark)
                        }
                        Column {
                            Text("AVG INTERVAL", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                            Text("28d", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealDark)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SAVINGS", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                            Text("12%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealDark)
                        }
                    }

                    orders.forEach { order ->
                        RefillHistoryItem(order = order)
                    }
                }
            }
        }
    }
}

@Composable
fun RefillHistoryItem(order: RefillOrder) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(order.orderId, fontWeight = FontWeight.Bold, color = TealDark)
                Text(order.date, fontSize = 11.sp, color = GrayDark)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (order.status == "SUCCESS") TealLight else CoralRedBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = order.status,
                    color = if (order.status == "SUCCESS") TealDark else CoralRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Volume", fontSize = 11.sp, color = GrayDark)
                Text(if (order.volume > 0) "${order.volume} Gallons" else "--", fontWeight = FontWeight.SemiBold, color = TealDark)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Cost", fontSize = 11.sp, color = GrayDark)
                Text("$${order.cost}", fontWeight = FontWeight.SemiBold, color = TealDark)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = GrayDark.copy(alpha = 0.1f))
    }
}

@Composable
fun DeliveryOptionCard(
    title: String,
    desc: String,
    price: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = if (isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) TealLight else TealExtraLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TealDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = TealDark)
                Text(desc, fontSize = 11.sp, color = GrayDark)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(price, fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                if (isSelected) {
                    Icon(Icons.Filled.Check, contentDescription = "Selected", tint = TealMedium, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ==========================================
// 3. CHAT TAB (Image 1, Support Screen)
// ==========================================
@Composable
fun ChatTab(viewModel: GasGuardViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isTyping by viewModel.isAgentTyping.collectAsState()
    val textInput by viewModel.chatInput.collectAsState()
    val attachedImage by viewModel.attachedImage.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
    ) {
        // Chat Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(40.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(BlueLightBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AM", fontWeight = FontWeight.Bold, color = BlueAccent)
                    }
                    // Status badge
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("Technical Support", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ONLINE", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(" • Agent: Alex M.", color = GrayDark, fontSize = 10.sp)
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(msg = msg)
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .widthIn(max = 100.dp)
                    ) {
                        Text("Analyzing sensor image...", color = TealDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom input pane
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Image preview if attached
                if (attachedImage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealLight)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Image, contentDescription = "Attached", tint = TealDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(attachedImage!!, fontWeight = FontWeight.Bold, color = TealDark, fontSize = 12.sp)
                        }
                        IconButton(onClick = { viewModel.removeAttachedImage() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = CoralRed)
                        }
                    }
                }

                // Text field and actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera upload trigger simulation (no dead-end UI!)
                    IconButton(
                        onClick = { viewModel.attachMockImage() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(TealExtraLight)
                    ) {
                        Icon(Icons.Filled.AddAPhoto, contentDescription = "Camera", tint = TealMedium)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { viewModel.updateChatInput(it) },
                        placeholder = { Text("Type a message...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = GrayDark,
                            unfocusedTextColor = GrayDark,
                            focusedContainerColor = TealExtraLight,
                            unfocusedContainerColor = TealExtraLight,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedPlaceholderColor = GrayDark.copy(alpha = 0.5f),
                            unfocusedPlaceholderColor = GrayDark.copy(alpha = 0.5f)
                        ),
                        maxLines = 3,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            viewModel.sendChatMessage()
                            keyboard?.hide()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(TealDark)
                            .testTag("send_message_button")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage) {
    val isUser = msg.sender == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) TealDark else Color.White
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Image if present
                if (msg.imageUrl != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealMedium),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Image, contentDescription = "Sensor Attachment", tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(msg.imageUrl, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = msg.text,
                    color = if (isUser) Color.White else GrayDark,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ==========================================
// 4. ONBOARDING / SAFETY CHECK WIZARD (Image 3, Setup wizard)
// ==========================================
@Composable
fun OnboardingWizard(viewModel: GasGuardViewModel) {
    val step by viewModel.onboardingStep.collectAsState()
    val pairedDeviceId by viewModel.pairedDeviceId.collectAsState()
    val sensorStatus by viewModel.sensorConnectionStatus.collectAsState()
    val pairingError by viewModel.pairingError.collectAsState()
    val calibrationProgress by viewModel.calibrationProgress.collectAsState()
    val calibrationReading by viewModel.calibrationReading.collectAsState()
    val calibrationStatus by viewModel.calibrationStatus.collectAsState()
    val mq2Mounted by viewModel.mq2Mounted.collectAsState()
    val clampTight by viewModel.pressureClampTight.collectAsState()
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.finishOnboarding() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = TealDark)
                }
                Text("Setup Monitor", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(Icons.Filled.HelpOutline, contentDescription = "Help", tint = TealDark)
                }
            }
        }
    ) { innerPadding ->
        if (showHelpDialog) {
            val helpText = when (step) {
                1 -> "Flash your ESP32 with the firmware in this project's /firmware folder, power it on, and copy the device ID it prints to Serial the first time it boots. Paste that ID in above and tap \"Pair this device\"."
                2 -> "Calibration reads a clean-air baseline from the MQ-2 sensor. Keep the sensor away from any gas source, cleaning products, or open flames while it runs."
                3 -> "Mount the sensor within 30cm (about 12 inches) of your regulator, pointing downward, and make sure the pressure clamp is fully tightened."
                else -> "Review your setup summary, then tap Finish to start monitoring."
            }
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("Setup help") },
                text = { Text(helpText) },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) { Text("Got it") }
                }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TealExtraLight)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Progress and Step Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STEP $step: " + when (step) {
                                    1 -> "WELCOME"
                                    2 -> "CALIBRATION"
                                    3 -> "PLACEMENT & MOUNTING"
                                    else -> "SYSTEM TEST"
                                },
                                fontWeight = FontWeight.Bold,
                                color = TealMedium,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "$step of 4",
                                fontWeight = FontWeight.Bold,
                                color = GrayDark.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                        // Visual Step line
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 1..4) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (i <= step) TealMedium else Color.LightGray.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }

                    // Step Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (step) {
                            1 -> { // Connect / pair your ESP32
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Router, contentDescription = "Router", tint = TealDark, modifier = Modifier.size(54.dp))
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text("Connect Your ESP32", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TealDark, textAlign = TextAlign.Center)
                                Text(
                                    text = "Flash your ESP32 with the firmware from the project's /firmware folder, power it on, then enter the device ID it prints to Serial on first boot.",
                                    fontSize = 14.sp,
                                    color = GrayDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                if (pairedDeviceId == null) {
                                    var deviceIdInput by remember { mutableStateOf("") }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = deviceIdInput,
                                            onValueChange = { deviceIdInput = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Device ID") },
                                            placeholder = { Text("e.g. gasguard-a1b2c3", fontSize = 12.sp) },
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { viewModel.pairDevice(deviceIdInput) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                                        ) {
                                            Text("Pair this device", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        if (pairingError != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(pairingError!!, fontSize = 11.sp, color = CoralRed)
                                        }
                                        if (sensorStatus == "Not configured") {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                "Cloud sync isn't set up yet - see the README's Firebase section before pairing.",
                                                fontSize = 11.sp,
                                                color = GrayDark
                                            )
                                        }
                                    }
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = TealExtraLight)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (sensorStatus == "Live") Icons.Filled.CheckCircle else Icons.Filled.Sync,
                                                contentDescription = null,
                                                tint = if (sensorStatus == "Live") TealMedium else GrayDark
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = when (sensorStatus) {
                                                        "Live" -> "Paired and receiving live readings!"
                                                        "Offline" -> "Paired - waiting for your ESP32 to come online"
                                                        else -> "Paired - connecting..."
                                                    },
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TealDark
                                                )
                                                Text("Device ID: $pairedDeviceId", fontSize = 11.sp, color = GrayDark)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = { viewModel.unpairDevice() }) {
                                        Text("Pair a different device", fontSize = 12.sp, color = TealDark)
                                    }
                                }
                            }
                            2 -> { // Calibrate Sensors
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Build, contentDescription = "Calibrate", tint = TealDark, modifier = Modifier.size(48.dp))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Calibrate Sensors", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TealDark, textAlign = TextAlign.Center)
                                Text(
                                    text = "Please wait while we initialize the MQ-2 gas sensor and synchronize the pressure valves for maximum accuracy.",
                                    fontSize = 13.sp,
                                    color = GrayDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // MQ-2 Calibrating Item
                                SensorCalibrateProgressItem(
                                    title = "MQ-2 SENSOR",
                                    status = calibrationStatus,
                                    progress = calibrationProgress,
                                    detail = "Reading: $calibrationReading ppm   Target: < 0.05 ppm"
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Pressure Valve
                                SensorCalibrateProgressItem(
                                    title = "PRESSURE VALVE",
                                    status = if (calibrationProgress == 1f) "Synced" else "Waiting",
                                    progress = if (calibrationProgress == 1f) 1f else 0f,
                                    detail = "Pressure clamp synchronization status"
                                )
                            }
                            3 -> { // Placement & Mounting
                                Text(
                                    text = "Placement & Mounting",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealDark,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Follow the technical schematic below to ensure precise sensor orientation for optimal leak detection.",
                                    fontSize = 13.sp,
                                    color = GrayDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                                )

                                // Checkbox list
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TealExtraLight)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = mq2Mounted,
                                            onCheckedChange = { viewModel.toggleMq2Mounted(it) },
                                            colors = CheckboxDefaults.colors(checkedColor = TealDark)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "MQ-2 sensor is securely mounted within 30cm of regulator and facing down",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TealDark
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(TealExtraLight)
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = clampTight,
                                            onCheckedChange = { viewModel.togglePressureClamp(it) },
                                            colors = CheckboxDefaults.colors(checkedColor = TealDark)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Pressure sensor clamp is tight and showing green LED indicator",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TealDark
                                        )
                                    }
                                }
                            }
                            else -> { // System Test
                                val livePpm by viewModel.currentPpmReading.collectAsState()
                                val livePressure by viewModel.currentPressureKpa.collectAsState()
                                val isLive = sensorStatus == "Live"

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = if (isLive) TealLight else Color.LightGray.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = if (isLive) Icons.Filled.CheckCircle else Icons.Filled.Sync,
                                            contentDescription = "Status",
                                            tint = TealDark,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (isLive) "All Systems Go" else "Waiting for your ESP32",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TealDark
                                        )
                                        Text(
                                            text = if (isLive) "Your device is live and reporting real readings."
                                                   else "Make sure it's powered on and connected to WiFi - readings below will update once it checks in.",
                                            fontSize = 12.sp,
                                            color = TealDark,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Readings list - real values once the ESP32 is live, otherwise
                                // an honest "waiting" placeholder instead of a fake number.
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    ReadingMetricRow(
                                        label = "MQ-2 SENSOR READING",
                                        value = if (isLive) "$livePpm PPM" else "-",
                                        isGood = isLive && livePpm < 0.05
                                    )
                                    ReadingMetricRow(
                                        label = "PRESSURE SENSOR DATA",
                                        value = if (isLive) "$livePressure kPa" else "-",
                                        isGood = isLive
                                    )
                                }
                            }
                        }
                    }

                    // Navigation buttons inside Card footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (step > 1) {
                            OutlinedButton(
                                onClick = { viewModel.prevOnboardingStep() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Back", color = TealDark, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                if (step < 4) {
                                    viewModel.nextOnboardingStep()
                                } else {
                                    viewModel.finishOnboarding()
                                }
                            },
                            enabled = when (step) {
                                1 -> pairedDeviceId != null
                                2 -> calibrationStatus == "Done"
                                3 -> mq2Mounted && clampTight
                                else -> true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                        ) {
                            Text(
                                text = if (step < 4) "Next" else "Go to Dashboard",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorCalibrateProgressItem(
    title: String,
    status: String,
    progress: Float,
    detail: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TealExtraLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Bold, color = TealDark, fontSize = 11.sp)
                Text(status, fontWeight = FontWeight.Bold, color = if (status == "Done" || status == "Synced") TealMedium else BlueAccent, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TealDark,
                trackColor = Color.LightGray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(detail, fontSize = 11.sp, color = GrayDark)
        }
    }
}

@Composable
fun ReadingMetricRow(
    label: String,
    value: String,
    isGood: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TealExtraLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayDark)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TealDark, fontFamily = FontFamily.Monospace)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isGood) TealLight else CoralRedBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGood) "OPTIMAL" else "CHECK",
                    color = if (isGood) TealDark else CoralRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ==========================================
// 5. SUPPLIER DASHBOARD (Image 6, 7, 8, 9)
// ==========================================
@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = GrayDark, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = GrayDark)
    }
}

@Composable
fun SupplierDashboard(
    viewModel: GasGuardViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.supplierTab.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            SupplierTopBar(viewModel = viewModel)
        },
        bottomBar = {
            SupplierBottomBar(viewModel = viewModel, activeTab = activeTab)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Dashboard" -> SupplierDashboardTab(viewModel = viewModel)
                "Customers" -> SupplierCustomersTab(viewModel = viewModel)
                "Dispatch" -> SupplierDispatchTab(viewModel = viewModel)
                "Chat" -> ChatTab(viewModel = viewModel)
            }

            // Global dialog overlays
            SyncSuccessDialog(viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierTopBar(viewModel: GasGuardViewModel) {
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Business,
                    contentDescription = "Supplier Shield Logo",
                    tint = TealMedium,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VigilantGas",
                    fontWeight = FontWeight.Bold,
                    color = TealDark,
                    fontSize = 20.sp
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                if (lastSyncTime.isNotEmpty() && !isSyncing) {
                    Text(
                        text = lastSyncTime,
                        fontSize = 11.sp,
                        color = GrayDark.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                IconButton(
                    onClick = { viewModel.syncWithBackend() },
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("supplier_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TealDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Sync Data",
                            tint = TealDark
                        )
                    }
                }
            }

            IconButton(onClick = { viewModel.signOut() }) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Sign Out",
                    tint = TealDark
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

@Composable
fun SupplierBottomBar(viewModel: GasGuardViewModel, activeTab: String) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = TealDark
    ) {
        val tabs = listOf(
            Triple("Dashboard", Icons.Filled.SpaceDashboard, Icons.Outlined.SpaceDashboard),
            Triple("Customers", Icons.Filled.People, Icons.Outlined.People),
            Triple("Dispatch", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping),
            Triple("Chat", Icons.Filled.Chat, Icons.Outlined.Chat)
        )

        tabs.forEach { (tab, filledIcon, outlinedIcon) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { viewModel.updateSupplierTab(tab) },
                icon = {
                    Icon(
                        imageVector = if (activeTab == tab) filledIcon else outlinedIcon,
                        contentDescription = tab
                    )
                },
                label = { Text(tab, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TealDark,
                    selectedTextColor = TealDark,
                    indicatorColor = TealLight,
                    unselectedIconColor = GrayDark,
                    unselectedTextColor = GrayDark
                ),
                modifier = Modifier.testTag("supplier_tab_${tab.lowercase()}")
            )
        }
    }
}

// 5A. SUPPLIER DASHBOARD TAB (Operations & Revenue Views)
@Composable
fun SupplierDashboardTab(viewModel: GasGuardViewModel) {
    val subTab by viewModel.supplierDashboardSubTab.collectAsState()
    val dispatchesCount by viewModel.dispatchesCount.collectAsState()
    val pendingRefills by viewModel.pendingRefillsCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
    ) {
        // Toggle SubTab (Operations vs Revenue)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "Operations") TealDark else Color.Transparent)
                    .clickable { viewModel.updateSupplierDashboardSubTab("Operations") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "OPERATIONS",
                    color = if (subTab == "Operations") Color.White else TealDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (subTab == "Revenue") TealDark else Color.Transparent)
                    .clickable { viewModel.updateSupplierDashboardSubTab("Revenue") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "REVENUE OVERVIEW",
                    color = if (subTab == "Revenue") Color.White else TealDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        if (subTab == "Operations") {
            // Operations View (Image 8)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SupplierMetricCard(
                            title = "ACTIVE DISPATCHES",
                            value = "$dispatchesCount",
                            subtitle = "+2 from yesterday",
                            icon = Icons.Default.LocalShipping,
                            modifier = Modifier.weight(1f)
                        )
                        SupplierMetricCard(
                            title = "PENDING REFILLS",
                            value = "0$pendingRefills",
                            subtitle = "Scheduled today",
                            icon = Icons.Default.AccessTime,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Critical alerts
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CoralRedBg),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("CRITICAL ALERTS", fontSize = 10.sp, color = CoralRed, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("03 Units Under 10%", fontSize = 18.sp, color = CoralRed, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CoralRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = "Warning", tint = CoralRed)
                            }
                        }
                    }
                }

                // Urgent Refills Title
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Urgent Refill Requests", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                        Text("VIEW ALL", fontSize = 11.sp, color = BlueAccent, fontWeight = FontWeight.Bold)
                    }
                }

                // Urgent list
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        UrgentRefillItem(title = "Industrial North Hub", desc = "Sector 7-B, Port District", valText = "4.2%", isCritical = true)
                        UrgentRefillItem(title = "Summit Apartments", desc = "442 Westview Dr.", valText = "8.5%", isCritical = true)
                        UrgentRefillItem(title = "Res. ID: #8892", desc = "Oak Ridge Estates", valText = "9.1%", isCritical = true)
                    }
                }

                // Nearby assets mock
                item {
                    Text("Nearby Assets", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Map brush background representing assets
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawRect(color = Color.LightGray.copy(alpha = 0.3f))
                                // Draw map roads simulation
                                drawLine(Color.White, Offset(0f, 100f), Offset(size.width, 100f), strokeWidth = 8f)
                                drawLine(Color.White, Offset(200f, 0f), Offset(200f, size.height), strokeWidth = 8f)
                                drawCircle(TealMedium, radius = 10f, center = Offset(200f, 100f))
                                drawCircle(CoralRed, radius = 8f, center = Offset(300f, 120f))
                            }

                            Button(
                                onClick = { viewModel.updateSupplierTab("Dispatch") },
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Map, contentDescription = "Map")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Live View")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Revenue View (Image 9)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Revenue Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("REVENUE OVERVIEW", fontSize = 10.sp, color = GrayDark, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("$142,850.00", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, color = TealDark))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TrendingUp, contentDescription = "Trend", tint = Color.Green, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+12.5%", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Text("Total Gross Revenue (Past 30 Days)", color = GrayDark, fontSize = 12.sp)
                        }
                    }
                }

                // Chart card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("WEEKLY PERFORMANCE", fontSize = 10.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TealExtraLight)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TealDark)
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("7D", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                    Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                                        Text("30D", fontSize = 10.sp, color = GrayDark)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Chart Canvas
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                val vals = listOf(10000.0, 15000.0, 12000.0, 22000.0, 18000.0, 28000.0, 24000.0)
                                val maxV = 30000.0
                                val stepX = size.width / (vals.size - 1)
                                val path = Path()

                                vals.forEachIndexed { i, value ->
                                    val x = i * stepX
                                    val y = size.height - (value / maxV * size.height).toFloat()
                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(path, TealMedium, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                                vals.forEachIndexed { i, value ->
                                    val x = i * stepX
                                    val y = size.height - (value / maxV * size.height).toFloat()
                                    drawCircle(TealDark, radius = 4.dp.toPx(), center = Offset(x, y))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                                days.forEach { day ->
                                    Text(day, fontSize = 9.sp, color = GrayDark)
                                }
                            }
                        }
                    }
                }

                // Sectors and pending invoices
                item {
                    Text("Top Performing Sectors", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SectorProgressItem(label = "Industrial Manufacturing", valueText = "$68.2k", progress = 0.7f)
                            SectorProgressItem(label = "Residential Smart-Home", valueText = "$42.1k", progress = 0.5f)
                            SectorProgressItem(label = "Logistics & Transport", valueText = "$32.5k", progress = 0.35f)
                        }
                    }
                }

                item {
                    Text("Pending Invoices", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PendingInvoiceItem(id = "INV-98231", due = "DUE IN 2 DAYS", amount = "$4,250.00", status = "WAITING")
                        PendingInvoiceItem(id = "INV-98230", due = "DUE IN 5 DAYS", amount = "$2,100.00", status = "REVIEW")
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrayDark)
                Icon(imageVector = icon, contentDescription = title, tint = TealMedium, modifier = Modifier.size(16.dp))
            }
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TealDark, modifier = Modifier.padding(top = 4.dp))
            Text(subtitle, fontSize = 10.sp, color = GrayDark.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun UrgentRefillItem(
    title: String,
    desc: String,
    valText: String,
    isCritical: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealExtraLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Store, contentDescription = "Hub", tint = TealDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = TealDark)
                    Text(desc, fontSize = 11.sp, color = GrayDark)
                }
            }
            Text(valText, fontWeight = FontWeight.Bold, color = if (isCritical) CoralRed else TealDark, fontSize = 15.sp)
        }
    }
}

@Composable
fun SectorProgressItem(label: String, valueText: String, progress: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold, color = TealDark, fontSize = 12.sp)
            Text(valueText, fontWeight = FontWeight.Bold, color = TealDark, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = TealMedium,
            trackColor = TealExtraLight
        )
    }
}

@Composable
fun PendingInvoiceItem(id: String, due: String, amount: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Receipt, contentDescription = "Invoice", tint = TealMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(id, fontWeight = FontWeight.Bold, color = TealDark)
                    Text(due, fontSize = 10.sp, color = GrayDark)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(amount, fontWeight = FontWeight.Bold, color = TealDark)
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (status == "WAITING") GoldAlertBg else BlueLightBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(status, color = if (status == "WAITING") GoldAlert else BlueAccent, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }
    }
}

// 5B. SUPPLIER CUSTOMERS TAB (Image 7)
@Composable
fun SupplierCustomersTab(viewModel: GasGuardViewModel) {
    val clientsList by viewModel.clients.collectAsState()
    val searchQuery by viewModel.customerSearchQuery.collectAsState()
    val filterType by viewModel.customerTypeFilter.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    val allOrders by viewModel.refillOrders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search customers, ID, or site...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TealMedium) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GrayDark,
                unfocusedTextColor = GrayDark,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = TealMedium,
                unfocusedBorderColor = Color.Transparent,
                focusedPlaceholderColor = GrayDark.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = GrayDark.copy(alpha = 0.5f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf("ALL", "RESIDENTIAL", "INDUSTRIAL")
            filterOptions.forEach { opt ->
                val isSelected = filterType == opt
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) TealDark else Color.White)
                        .clickable { viewModel.updateTypeFilter(opt) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        opt,
                        color = if (isSelected) Color.White else TealDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Statistics Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ACTIVE CLIENTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrayDark)
                    Text("${clientsList.size * 320}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TealDark)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = CoralRedBg)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("REFILLS REQ.", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("18", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.NotificationImportant, contentDescription = "Req", tint = CoralRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Client directory label and floating trigger dialog
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CLIENT DIRECTORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrayDark, letterSpacing = 1.sp)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealMedium),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("ADD", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customer List
        val filteredList = clientsList.filter {
            (filterType == "ALL" || it.type == filterType) &&
                    (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { client ->
                ClientCardItem(
                    client = client,
                    onScheduleRefill = { viewModel.scheduleRefillForClient(client) },
                    onClick = { selectedClient = client }
                )
            }
        }
    }

    // Customer detail card - name, contact info, and full refill history for this client
    selectedClient?.let { client ->
        val clientOrders = allOrders.filter { it.clientId == client.id }
        AlertDialog(
            onDismissRequest = { selectedClient = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealExtraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (client.type == "INDUSTRIAL") Icons.Default.Factory else Icons.Default.Home,
                            contentDescription = client.type,
                            tint = TealDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(client.name, fontWeight = FontWeight.Bold, color = TealDark, fontSize = 16.sp)
                        Text(client.type, fontSize = 10.sp, color = GrayDark)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    // Contact & status details
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailRow(icon = Icons.Filled.Place, label = client.address)
                        DetailRow(icon = Icons.Filled.Phone, label = client.phone)
                        DetailRow(icon = Icons.Filled.LocalGasStation, label = "${(client.fuelLevel * 100).toInt()}% fuel remaining")
                        DetailRow(icon = Icons.Filled.CalendarToday, label = "Last delivery: ${client.lastDelivery}")
                        DetailRow(icon = Icons.Filled.Schedule, label = "Est. runout: ${client.estRunout}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "REFILL HISTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrayDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (clientOrders.isEmpty()) {
                        Text(
                            "No refills recorded yet for this customer.",
                            fontSize = 12.sp,
                            color = GrayDark.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(clientOrders) { order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TealExtraLight)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(order.orderId, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealDark)
                                        Text(order.date, fontSize = 10.sp, color = GrayDark)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            if (order.volume > 0) "${order.volume} gal" else "-",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TealDark
                                        )
                                        Text(
                                            order.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (order.status) {
                                                "SUCCESS" -> TealMedium
                                                "CANCELED" -> CoralRed
                                                else -> BlueAccent
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedClient = null }) { Text("Close") }
            }
        )
    }

    // Add customer dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("RESIDENTIAL") }
        var fuel by remember { mutableStateOf("0.5") }
        var phone by remember { mutableStateOf("+1-555-0100") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Customer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GrayDark,
                        unfocusedTextColor = GrayDark,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TealMedium,
                        unfocusedBorderColor = GrayDark.copy(alpha = 0.3f),
                        focusedLabelColor = TealMedium,
                        unfocusedLabelColor = GrayDark.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Customer Name") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fuel,
                        onValueChange = { fuel = it },
                        label = { Text("Fuel Level (0.0 to 1.0)") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = type == "RESIDENTIAL", onClick = { type = "RESIDENTIAL" })
                            Text("Residential")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = type == "INDUSTRIAL", onClick = { type = "INDUSTRIAL" })
                            Text("Industrial")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark),
                    onClick = {
                        val parsedFuel = fuel.toDoubleOrNull() ?: 0.5
                        viewModel.addNewClient(name, address, type, parsedFuel, phone)
                        showAddDialog = false
                    }
                ) {
                    Text("Add Client")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ClientCardItem(client: Client, onScheduleRefill: () -> Unit, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealExtraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (client.type == "INDUSTRIAL") Icons.Default.Factory else Icons.Default.Home,
                            contentDescription = client.type,
                            tint = TealDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(client.name, fontWeight = FontWeight.Bold, color = TealDark)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, contentDescription = "Loc", tint = GrayDark, modifier = Modifier.size(10.dp))
                            Text(client.address, fontSize = 10.sp, color = GrayDark)
                        }
                    }
                }

                // Fuel tag
                val fuelPercent = (client.fuelLevel * 100).toInt()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (client.fuelLevel < 0.25) CoralRedBg else TealLight)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "$fuelPercent% FUEL",
                        color = if (client.fuelLevel < 0.25) CoralRed else TealDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If fuel level is critically low, show Schedule button
                if (client.fuelLevel < 0.5) {
                    Button(
                        onClick = onScheduleRefill,
                        colors = ButtonDefaults.buttonColors(containerColor = TealMedium),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = "Refill", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCHEDULE REFILL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Normal Client Details
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("LAST DELIVERY", fontSize = 9.sp, color = GrayDark)
                            Text(client.lastDelivery, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealDark)
                        }
                        Column {
                            Text("EST. RUNOUT", fontSize = 9.sp, color = GrayDark)
                            Text(client.estRunout, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TealDark)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            if (client.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealExtraLight)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = "Call", tint = TealMedium, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = {
                            if (client.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${client.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealExtraLight)
                    ) {
                        Icon(Icons.Filled.Chat, contentDescription = "Message", tint = TealMedium, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// 5C. SUPPLIER DISPATCH TAB (Fleet Tracking & Distribution Hub views)
@Composable
fun SupplierDispatchTab(viewModel: GasGuardViewModel) {
    var showAllQueueDialog by remember { mutableStateOf(false) }

    val queueItems = listOf(
        QueueItemData("Westside Industrial Complex", "Refill • 4,500L Liquefied Petroleum", "Driver: Marcus T.", "ETA 12m", "EN ROUTE", BlueAccent),
        QueueItemData("Riverside Housing Estate", "Refill • 2,100L Natural Gas", "Dispatcher: Sarah K.", "Urgent", "LOW LEVEL", CoralRed),
        QueueItemData("Downtown Medical Center", "Refill • 6,000L Liquefied Petroleum", "Driver: Priya N.", "ETA 34m", "SCHEDULED", TealMedium),
        QueueItemData("Lakeside Apartments Block C", "Refill • 1,800L Natural Gas", "Dispatcher: Omar F.", "ETA 51m", "SCHEDULED", TealMedium)
    )

    // Show live fleet map tracking and priority queue
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TealExtraLight)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "LIVE FLEET TRACKING",
                fontWeight = FontWeight.Bold,
                color = TealDark,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            )
        }

        // Map mockup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = BlueLightBg)
                        // Grid layout representing Chicago roads
                        drawLine(Color.White, Offset(0f, 150f), Offset(size.width, 150f), strokeWidth = 10f)
                        drawLine(Color.White, Offset(150f, 0f), Offset(150f, size.height), strokeWidth = 10f)
                        drawLine(Color.White, Offset(300f, 0f), Offset(300f, size.height), strokeWidth = 10f)

                        // Trucks as circle indicators
                        drawCircle(TealDark, radius = 12f, center = Offset(150f, 150f))
                        drawCircle(GoldAlert, radius = 12f, center = Offset(300f, 100f))
                    }

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("NEAREST ASSET", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                            Text("Truck #402 • 1.2 mi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealDark)
                        }
                    }
                }
            }
        }

        // Summary Metric Rows
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACTIVE DISPATCH", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                        Text("14", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TealDark)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PENDING REFILLS", fontSize = 9.sp, color = GrayDark, fontWeight = FontWeight.Bold)
                        Text("08", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TealDark)
                    }
                }
            }
        }

        // Priority Queue Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PRIORITY QUEUE", fontWeight = FontWeight.Bold, color = TealDark, fontSize = 11.sp, letterSpacing = 1.sp)
                Text(
                    "View All",
                    fontSize = 11.sp,
                    color = BlueAccent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAllQueueDialog = true }
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                queueItems.take(2).forEach { q ->
                    QueueItem(
                        title = q.title,
                        detail = q.detail,
                        driver = q.driver,
                        time = q.time,
                        status = q.status,
                        statusColor = q.statusColor
                    )
                }
            }
        }
    }

    if (showAllQueueDialog) {
        AlertDialog(
            onDismissRequest = { showAllQueueDialog = false },
            title = { Text("All Priority Queue Items") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    queueItems.forEach { q ->
                        QueueItem(
                            title = q.title,
                            detail = q.detail,
                            driver = q.driver,
                            time = q.time,
                            status = q.status,
                            statusColor = q.statusColor
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllQueueDialog = false }) { Text("Close") }
            }
        )
    }
}

data class QueueItemData(
    val title: String,
    val detail: String,
    val driver: String,
    val time: String,
    val status: String,
    val statusColor: Color
)

@Composable
fun QueueItem(
    title: String,
    detail: String,
    driver: String,
    time: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = TealDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(status, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(detail, fontSize = 12.sp, color = GrayDark)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TealExtraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(driver.firstOrNull()?.toString() ?: "D", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(driver, fontSize = 11.sp, color = GrayDark)
                }
                Text(time, fontWeight = FontWeight.Bold, color = if (time == "Urgent") CoralRed else TealDark, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SyncSuccessDialog(viewModel: GasGuardViewModel) {
    val showSyncDialog by viewModel.showSyncSuccessDialog.collectAsState()
    val syncCounts by viewModel.syncRecordCounts.collectAsState()

    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSyncSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = "Success",
                        tint = TealDark,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cloud Sync Complete",
                        color = TealDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Local database was successfully synchronized with the Gas Monitor secure cloud backend on Google Firebase Firestore.",
                        color = GrayDark,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Synchronized Records Backup Summary:",
                        fontWeight = FontWeight.Bold,
                        color = TealDark,
                        fontSize = 12.sp
                    )
                    
                    // Detail items
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Supplier Clients:", fontSize = 12.sp, color = GrayDark)
                        Text("${syncCounts["clients"] ?: 0} records", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Refill Order Logs:", fontSize = 12.sp, color = GrayDark)
                        Text("${syncCounts["orders"] ?: 0} records", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tech Support Messages:", fontSize = 12.sp, color = GrayDark)
                        Text("${syncCounts["messages"] ?: 0} records", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gas Leak Telemetry Logs:", fontSize = 12.sp, color = GrayDark)
                        Text("${syncCounts["leaks"] ?: 0} records", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TealDark)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealLight)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Synchronized live safety bulletins from the remote Firebase Firestore collection.",
                            fontSize = 11.sp,
                            color = TealDark,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSyncSuccessDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = TealDark)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
