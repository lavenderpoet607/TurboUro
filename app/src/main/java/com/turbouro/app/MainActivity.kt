package com.turbouro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turbouro.app.manager.AppLaunchManager
import com.turbouro.app.manager.CapabilityManager
import com.turbouro.app.manager.GameScanner
import com.turbouro.app.manager.SystemGameOptimizer
import com.turbouro.app.manager.ThermalTracker
import com.turbouro.app.storage.GameProfileStore
import com.turbouro.app.storage.SessionHistoryStore
import com.turbouro.app.storage.TurboUroPreferences
import com.turbouro.app.ui.DashboardScreen
import com.turbouro.app.ui.DiagnosticsScreen
import com.turbouro.app.ui.GameDetailScreen
import com.turbouro.app.ui.GameLibraryScreen
import com.turbouro.app.ui.MoreScreen
import com.turbouro.app.ui.OnboardingScreen
import com.turbouro.app.ui.OverlaySettingsScreen
import com.turbouro.app.ui.PerformanceScreen
import com.turbouro.app.ui.PermissionCenterScreen
import com.turbouro.app.ui.SessionDetailScreen
import com.turbouro.app.ui.SessionHistoryScreen
import com.turbouro.app.ui.SettingsScreen
import com.turbouro.app.ui.SplashScreen
import com.turbouro.app.ui.ThermalScreen
import com.turbouro.app.ui.theme.TurboUroAppTheme
import com.turbouro.app.ui.theme.TurboUroTheme
import kotlinx.coroutines.launch

sealed class AppDestination {
    data object Splash : AppDestination()
    data object Onboarding : AppDestination()
    data object Permissions : AppDestination()
    data object Home : AppDestination()
    data object Games : AppDestination()
    data object Performance : AppDestination()
    data object More : AppDestination()
    data class GameDetail(val packageName: String) : AppDestination()
    data object Thermal : AppDestination()
    data object History : AppDestination()
    data class SessionDetail(val sessionId: String) : AppDestination()
    data object Diagnostics : AppDestination()
    data object OverlaySettings : AppDestination()
    data object Settings : AppDestination()
}

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Dashboard),
    GAMES("Games", Icons.Default.SportsEsports),
    PERFORMANCE("Performa", Icons.Default.Speed),
    MORE("Lainnya", Icons.Default.MoreHoriz)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = TurboUroPreferences(this)
        val profileStore = GameProfileStore(this)
        val sessionStore = SessionHistoryStore(this)
        val optimizer = SystemGameOptimizer(this)
        val thermalTracker = ThermalTracker(this)
        val capabilityManager = CapabilityManager(this)
        val gameScanner = GameScanner(this, profileStore)
        val launchManager = AppLaunchManager(this, profileStore, sessionStore, optimizer, thermalTracker)

        setContent {
            val appSettings by prefs.appSettingsFlow.collectAsState()

            TurboUroAppTheme(selectedTheme = appSettings.theme) {
                TurboUroAppScaffold(
                    prefs = prefs,
                    profileStore = profileStore,
                    sessionStore = sessionStore,
                    optimizer = optimizer,
                    capabilityManager = capabilityManager,
                    gameScanner = gameScanner,
                    launchManager = launchManager
                )
            }
        }
    }
}

@Composable
fun TurboUroAppScaffold(
    prefs: TurboUroPreferences,
    profileStore: GameProfileStore,
    sessionStore: SessionHistoryStore,
    optimizer: SystemGameOptimizer,
    capabilityManager: CapabilityManager,
    gameScanner: GameScanner,
    launchManager: AppLaunchManager
) {
    val colors = TurboUroTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val appSettings by prefs.appSettingsFlow.collectAsState()
    var destination by remember {
        mutableStateOf<AppDestination>(AppDestination.Splash)
    }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    when (val current = destination) {
        is AppDestination.Splash -> {
            SplashScreen(
                onFinished = {
                    destination = if (appSettings.isOnboardingCompleted) {
                        AppDestination.Home
                    } else {
                        AppDestination.Onboarding
                    }
                }
            )
        }

        is AppDestination.Onboarding -> {
            OnboardingScreen(
                onComplete = {
                    prefs.updateAppSettings(appSettings.copy(isOnboardingCompleted = true))
                    destination = AppDestination.Permissions
                }
            )
        }

        is AppDestination.Permissions -> {
            PermissionCenterScreen(
                onContinue = {
                    destination = AppDestination.Home
                }
            )
        }

        is AppDestination.GameDetail -> {
            BackHandler { destination = AppDestination.Games }
            GameDetailScreen(
                packageName = current.packageName,
                profileStore = profileStore,
                sessionStore = sessionStore,
                launchManager = launchManager,
                capabilityManager = capabilityManager,
                onBack = { destination = AppDestination.Games },
                onShowMessage = showMessage
            )
        }

        is AppDestination.Thermal -> {
            BackHandler { destination = AppDestination.More }
            ThermalScreen()
        }

        is AppDestination.History -> {
            BackHandler { destination = AppDestination.More }
            SessionHistoryScreen(
                sessionStore = sessionStore,
                onNavigateToDetail = { sessId ->
                    destination = AppDestination.SessionDetail(sessId)
                },
                onShowMessage = showMessage
            )
        }

        is AppDestination.SessionDetail -> {
            BackHandler { destination = AppDestination.History }
            SessionDetailScreen(
                sessionId = current.sessionId,
                sessionStore = sessionStore,
                onBack = { destination = AppDestination.History },
                onShowMessage = showMessage
            )
        }

        is AppDestination.Diagnostics -> {
            BackHandler { destination = AppDestination.More }
            DiagnosticsScreen()
        }

        is AppDestination.OverlaySettings -> {
            BackHandler { destination = AppDestination.More }
            OverlaySettingsScreen(
                prefs = prefs,
                onShowMessage = showMessage
            )
        }

        is AppDestination.Settings -> {
            BackHandler { destination = AppDestination.More }
            SettingsScreen(
                prefs = prefs,
                sessionStore = sessionStore,
                onNavigateToDiagnostics = { destination = AppDestination.Diagnostics },
                onShowMessage = showMessage
            )
        }

        else -> {
            if (isTablet) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.backgroundPrimary)
                ) {
                    NavigationRail(
                        containerColor = colors.backgroundSecondary,
                        contentColor = colors.textPrimary,
                        modifier = Modifier
                            .fillMaxHeight()
                            .border(width = 1.dp, color = colors.border)
                    ) {
                        MainTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    destination = when (tab) {
                                        MainTab.HOME -> AppDestination.Home
                                        MainTab.GAMES -> AppDestination.Games
                                        MainTab.PERFORMANCE -> AppDestination.Performance
                                        MainTab.MORE -> AppDestination.More
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = colors.accentPrimary,
                                    unselectedIconColor = colors.textMuted,
                                    selectedTextColor = colors.accentPrimary,
                                    unselectedTextColor = colors.textMuted,
                                    indicatorColor = colors.surfaceElevated
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            containerColor = colors.backgroundPrimary
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                TabContent(
                                    tab = currentTab,
                                    launchManager = launchManager,
                                    optimizer = optimizer,
                                    profileStore = profileStore,
                                    sessionStore = sessionStore,
                                    capabilityManager = capabilityManager,
                                    gameScanner = gameScanner,
                                    prefs = prefs,
                                    onNavigateToGames = {
                                        currentTab = MainTab.GAMES
                                        destination = AppDestination.Games
                                    },
                                    onNavigateToGameDetail = { pkg ->
                                        destination = AppDestination.GameDetail(pkg)
                                    },
                                    onNavigateToThermal = { destination = AppDestination.Thermal },
                                    onNavigateToHistory = { destination = AppDestination.History },
                                    onNavigateToDiagnostics = { destination = AppDestination.Diagnostics },
                                    onNavigateToOverlaySettings = { destination = AppDestination.OverlaySettings },
                                    onNavigateToSettings = { destination = AppDestination.Settings },
                                    onNavigateToPermissions = { destination = AppDestination.Permissions },
                                    onNavigateToSessionDetail = { sessId ->
                                        destination = AppDestination.SessionDetail(sessId)
                                    },
                                    onShowMessage = showMessage
                                )
                            }
                        }
                    }
                }
            } else {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = colors.backgroundPrimary,
                    bottomBar = {
                        NavigationBar(
                            containerColor = colors.backgroundSecondary,
                            contentColor = colors.textPrimary,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 1.dp, color = colors.border)
                        ) {
                            MainTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = {
                                        currentTab = tab
                                        destination = when (tab) {
                                            MainTab.HOME -> AppDestination.Home
                                            MainTab.GAMES -> AppDestination.Games
                                            MainTab.PERFORMANCE -> AppDestination.Performance
                                            MainTab.MORE -> AppDestination.More
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = colors.accentPrimary,
                                        unselectedIconColor = colors.textSecondary,
                                        selectedTextColor = colors.accentPrimary,
                                        unselectedTextColor = colors.textSecondary,
                                        indicatorColor = colors.accentPrimary.copy(alpha = 0.16f)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        TabContent(
                            tab = currentTab,
                            launchManager = launchManager,
                            optimizer = optimizer,
                            profileStore = profileStore,
                            sessionStore = sessionStore,
                            capabilityManager = capabilityManager,
                            gameScanner = gameScanner,
                            prefs = prefs,
                            onNavigateToGames = {
                                currentTab = MainTab.GAMES
                                destination = AppDestination.Games
                            },
                            onNavigateToGameDetail = { pkg ->
                                destination = AppDestination.GameDetail(pkg)
                            },
                            onNavigateToThermal = { destination = AppDestination.Thermal },
                            onNavigateToHistory = { destination = AppDestination.History },
                            onNavigateToDiagnostics = { destination = AppDestination.Diagnostics },
                            onNavigateToOverlaySettings = { destination = AppDestination.OverlaySettings },
                            onNavigateToSettings = { destination = AppDestination.Settings },
                            onNavigateToPermissions = { destination = AppDestination.Permissions },
                            onNavigateToSessionDetail = { sessId ->
                                destination = AppDestination.SessionDetail(sessId)
                            },
                            onShowMessage = showMessage
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabContent(
    tab: MainTab,
    launchManager: AppLaunchManager,
    optimizer: SystemGameOptimizer,
    profileStore: GameProfileStore,
    sessionStore: SessionHistoryStore,
    capabilityManager: CapabilityManager,
    gameScanner: GameScanner,
    prefs: TurboUroPreferences,
    onNavigateToGames: () -> Unit,
    onNavigateToGameDetail: (String) -> Unit,
    onNavigateToThermal: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToOverlaySettings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSessionDetail: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    when (tab) {
        MainTab.HOME -> {
            DashboardScreen(
                launchManager = launchManager,
                optimizer = optimizer,
                onNavigateToGames = onNavigateToGames,
                onNavigateToThermal = onNavigateToThermal,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToSessionDetail = onNavigateToSessionDetail,
                onShowMessage = onShowMessage
            )
        }

        MainTab.GAMES -> {
            GameLibraryScreen(
                scanner = gameScanner,
                profileStore = profileStore,
                launchManager = launchManager,
                onNavigateToDetail = onNavigateToGameDetail,
                onShowMessage = onShowMessage
            )
        }

        MainTab.PERFORMANCE -> {
            PerformanceScreen(
                launchManager = launchManager
            )
        }

        MainTab.MORE -> {
            MoreScreen(
                onNavigateToThermal = onNavigateToThermal,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                onNavigateToOverlaySettings = onNavigateToOverlaySettings,
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToPermissions = onNavigateToPermissions
            )
        }
    }
}
