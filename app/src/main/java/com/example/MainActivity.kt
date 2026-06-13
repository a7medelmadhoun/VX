package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                VXApp()
            }
        }
    }
}

@Composable
fun VXApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val subKey by viewModel.subscriptionKey.collectAsState()
    val isValidating by viewModel.isValidatingKey.collectAsState()
    val keyError by viewModel.keyError.collectAsState()

    var showSplash by remember { mutableStateOf(true) }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val permissionState = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        if (permissionState != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                Log.d("VX_DEBUG", "POST_NOTIFICATIONS granted: $isGranted")
            }
            LaunchedEffect(Unit) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

        if (showSplash) {
        SplashScreen { showSplash = false }
    } else {
        if (subKey == null || !subKey!!.isValid) {
            val promoCode by viewModel.promoCode.collectAsState()
            val currentPrice by viewModel.currentPrice.collectAsState()
            val isRetrievingKey by viewModel.isRetrievingKey.collectAsState()
            val keyAlertMessage by viewModel.keyAlertMessage.collectAsState()
            val purchasedKeysList by viewModel.purchasedKeysList.collectAsState()
            val isFetchingPurchasedKeys by viewModel.isFetchingPurchasedKeys.collectAsState()
            val isGeneratingPaymentUrl by viewModel.isGeneratingPaymentUrl.collectAsState()
            
            AccessScreen(
                onKeyValidated = { viewModel.validateKey(it) },
                isValidating = isValidating,
                errorMessage = keyError,
                promoCode = promoCode,
                currentPrice = currentPrice,
                onApplyPromo = { viewModel.applyPromoCode(it) },
                isRetrievingKey = isRetrievingKey,
                keyAlertMessage = keyAlertMessage,
                onRetrieveLatestKey = { email, callback -> viewModel.fetchAndAutofillLatestKey(email, callback) },
                purchasedKeysList = purchasedKeysList,
                isFetchingPurchasedKeys = isFetchingPurchasedKeys,
                onRetrieveAllKeys = { viewModel.retrieveAllKeysForEmail(it) },
                isGeneratingPaymentUrl = isGeneratingPaymentUrl,
                onPurchaseKey = { email, callback -> viewModel.generateNowPaymentInvoice(email, callback) }
            )
        } else {
            MainScaffold(viewModel)
        }
    }
}

@Composable
fun MainScaffold(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentDestination?.route == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "VIP Signals") },
                    label = { 
                        Text(
                            "SIGNALS", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentDestination?.route == "stats",
                    onClick = {
                        navController.navigate("stats") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Results") },
                    label = { 
                        Text(
                            "RESULTS", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentDestination?.route == "chat",
                    onClick = {
                        navController.navigate("chat") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "AI Assistant") },
                    label = { 
                        Text(
                            "AI CHAT", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentDestination?.route == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Profile") },
                    label = { 
                        Text(
                            "PROFILE", 
                            style = MaterialTheme.typography.labelSmall, 
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ) 
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                val recs by viewModel.recommendations.collectAsState()
                val loading by viewModel.isLoadingRecs.collectAsState()
                val selectedId by viewModel.selectedRecommendationId.collectAsState()
                
                DashboardScreen(
                    recommendations = recs,
                    isLoading = loading,
                    selectedId = selectedId,
                    onRefresh = { viewModel.loadRecommendations() },
                    onSelect = { viewModel.selectRecommendation(it) },
                    onChatAbout = { rec ->
                        viewModel.onChatWithRecommendation(rec)
                        navController.navigate("chat") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("stats") {
                val stats by viewModel.dailyStats.collectAsState()
                StatsScreen(
                    stats = stats,
                    onIncrement = { viewModel.incrementDailySuccess() }
                )
            }
            composable("chat") {
                val msgs by viewModel.chatMessages.collectAsState()
                val sending by viewModel.isSendingChat.collectAsState()
                val selectedRec by viewModel.selectedRecommendation.collectAsState()
                ChatScreen(
                    messages = msgs,
                    selectedRecommendation = selectedRec,
                    onSendMessage = { viewModel.sendMessage(it) },
                    isSending = sending,
                    onBack = { navController.navigateUp() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}
