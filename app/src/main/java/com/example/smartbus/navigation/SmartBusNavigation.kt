package com.example.smartbus.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.smartbus.data.model.UserRole
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.admin.*
import com.example.smartbus.presentation.auth.*
import com.example.smartbus.presentation.driver.DriverDashboardScreen
import com.example.smartbus.presentation.driver.DriverScheduleScreen
import com.example.smartbus.presentation.passenger.*
import kotlinx.coroutines.delay

object NavDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"

    // Passenger
    const val PASSENGER_MAIN = "passenger_main"
    const val SEARCH = "search"
    const val BUS_DETAILS = "bus_details/{busId}"
    const val ROUTE_DETAILS = "route_details/{routeId}"
    const val TIMETABLE = "timetable"
    const val FEEDBACK = "feedback?busId={busId}"

    // Driver
    const val DRIVER_DASHBOARD = "driver_dashboard"
    const val DRIVER_SCHEDULE = "driver_schedule"

    // Admin
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_LIVE_MONITORING = "admin_live_monitoring"
    const val ADMIN_BUS_MANAGEMENT = "admin_bus_management"
    const val ADMIN_DRIVER_MANAGEMENT = "admin_driver_management"
    const val ADMIN_ROUTE_MANAGEMENT = "admin_route_management"
    const val ADMIN_SCHEDULE_MANAGEMENT = "admin_schedule_management"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_FEEDBACK = "admin_feedback"
}

@Composable
fun SmartBusNavGraph(
    navController: NavHostController,
    repository: SmartBusRepository
) {
    val currentUser by repository.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavDestinations.SPLASH
    ) {
        // Splash Screen
        composable(NavDestinations.SPLASH) {
            SplashScreen(
                onTimeout = {
                    val user = currentUser
                    if (user != null) {
                        when (user.role) {
                            UserRole.PASSENGER -> navController.navigate(NavDestinations.PASSENGER_MAIN) {
                                popUpTo(NavDestinations.SPLASH) { inclusive = true }
                            }
                            UserRole.DRIVER -> navController.navigate(NavDestinations.DRIVER_DASHBOARD) {
                                popUpTo(NavDestinations.SPLASH) { inclusive = true }
                            }
                            UserRole.ADMIN -> navController.navigate(NavDestinations.ADMIN_DASHBOARD) {
                                popUpTo(NavDestinations.SPLASH) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(NavDestinations.LOGIN) {
                            popUpTo(NavDestinations.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Auth
        composable(NavDestinations.LOGIN) {
            LoginScreen(
                repository = repository,
                onLoginSuccess = {
                    val user = repository.currentUser.value
                    val target = when (user?.role) {
                        UserRole.PASSENGER -> NavDestinations.PASSENGER_MAIN
                        UserRole.DRIVER -> NavDestinations.DRIVER_DASHBOARD
                        UserRole.ADMIN -> NavDestinations.ADMIN_DASHBOARD
                        null -> NavDestinations.PASSENGER_MAIN
                    }
                    navController.navigate(target) {
                        popUpTo(NavDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(NavDestinations.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(NavDestinations.FORGOT_PASSWORD) }
            )
        }

        composable(NavDestinations.REGISTER) {
            RegisterScreen(
                repository = repository,
                onRegisterSuccess = {
                    val user = repository.currentUser.value
                    val target = when (user?.role) {
                        UserRole.PASSENGER -> NavDestinations.PASSENGER_MAIN
                        UserRole.DRIVER -> NavDestinations.DRIVER_DASHBOARD
                        UserRole.ADMIN -> NavDestinations.ADMIN_DASHBOARD
                        null -> NavDestinations.PASSENGER_MAIN
                    }
                    navController.navigate(target) {
                        popUpTo(NavDestinations.REGISTER) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Passenger Main (Bottom Navigation Scaffold)
        composable(NavDestinations.PASSENGER_MAIN) {
            PassengerMainContainer(
                repository = repository,
                onNavigateToSearch = { navController.navigate(NavDestinations.SEARCH) },
                onNavigateToBusDetails = { busId -> navController.navigate("bus_details/$busId") },
                onNavigateToRouteDetails = { routeId -> navController.navigate("route_details/$routeId") },
                onNavigateToTimetable = { navController.navigate(NavDestinations.TIMETABLE) },
                onNavigateToFeedback = { busId -> navController.navigate("feedback?busId=${busId ?: ""}") },
                onLogout = {
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.PASSENGER_MAIN) { inclusive = true }
                    }
                },
                onRoleChanged = {
                    val role = repository.currentUser.value?.role
                    when (role) {
                        UserRole.DRIVER -> navController.navigate(NavDestinations.DRIVER_DASHBOARD) {
                            popUpTo(NavDestinations.PASSENGER_MAIN) { inclusive = true }
                        }
                        UserRole.ADMIN -> navController.navigate(NavDestinations.ADMIN_DASHBOARD) {
                            popUpTo(NavDestinations.PASSENGER_MAIN) { inclusive = true }
                        }
                        else -> {}
                    }
                }
            )
        }

        // Passenger Sub-destinations
        composable(NavDestinations.SEARCH) {
            SearchScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBus = { busId -> navController.navigate("bus_details/$busId") },
                onNavigateToRoute = { routeId -> navController.navigate("route_details/$routeId") }
            )
        }

        composable(
            route = NavDestinations.BUS_DETAILS,
            arguments = listOf(navArgument("busId") { type = NavType.StringType })
        ) { backStack ->
            val busId = backStack.arguments?.getString("busId") ?: ""
            BusDetailScreen(
                busId = busId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { targetBusId ->
                    navController.navigate(NavDestinations.PASSENGER_MAIN)
                },
                onNavigateToRoute = { routeId -> navController.navigate("route_details/$routeId") },
                onNavigateToFeedback = { targetBusId -> navController.navigate("feedback?busId=$targetBusId") }
            )
        }

        composable(
            route = NavDestinations.ROUTE_DETAILS,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStack ->
            val routeId = backStack.arguments?.getString("routeId") ?: ""
            RouteDetailScreen(
                routeId = routeId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { navController.navigate(NavDestinations.PASSENGER_MAIN) },
                onNavigateToBus = { busId -> navController.navigate("bus_details/$busId") }
            )
        }

        composable(NavDestinations.TIMETABLE) {
            TimetableScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBus = { busId -> navController.navigate("bus_details/$busId") }
            )
        }

        composable(
            route = NavDestinations.FEEDBACK,
            arguments = listOf(navArgument("busId") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStack ->
            val busId = backStack.arguments?.getString("busId") ?: ""
            FeedbackScreen(
                repository = repository,
                initialBusId = busId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Driver Portal
        composable(NavDestinations.DRIVER_DASHBOARD) {
            DriverDashboardScreen(
                repository = repository,
                onNavigateToRoute = { routeId -> navController.navigate("route_details/$routeId") },
                onNavigateToSchedule = { navController.navigate(NavDestinations.DRIVER_SCHEDULE) },
                onNavigateToAlerts = { navController.navigate(NavDestinations.TIMETABLE) },
                onLogout = {
                    repository.logout()
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.DRIVER_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(NavDestinations.DRIVER_SCHEDULE) {
            DriverScheduleScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin Portal
        composable(NavDestinations.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                repository = repository,
                onNavigateToLiveMonitoring = { navController.navigate(NavDestinations.ADMIN_LIVE_MONITORING) },
                onNavigateToBusManagement = { navController.navigate(NavDestinations.ADMIN_BUS_MANAGEMENT) },
                onNavigateToDriverManagement = { navController.navigate(NavDestinations.ADMIN_DRIVER_MANAGEMENT) },
                onNavigateToRouteManagement = { navController.navigate(NavDestinations.ADMIN_ROUTE_MANAGEMENT) },
                onNavigateToScheduleManagement = { navController.navigate(NavDestinations.ADMIN_SCHEDULE_MANAGEMENT) },
                onNavigateToReports = { navController.navigate(NavDestinations.ADMIN_REPORTS) },
                onNavigateToFeedback = { navController.navigate(NavDestinations.ADMIN_FEEDBACK) },
                onLogout = {
                    repository.logout()
                    navController.navigate(NavDestinations.LOGIN) {
                        popUpTo(NavDestinations.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(NavDestinations.ADMIN_LIVE_MONITORING) {
            AdminLiveMonitoringScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBusDetails = { busId -> navController.navigate("bus_details/$busId") }
            )
        }

        composable(NavDestinations.ADMIN_BUS_MANAGEMENT) {
            AdminBusManagementScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ADMIN_DRIVER_MANAGEMENT) {
            AdminDriverManagementScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ADMIN_ROUTE_MANAGEMENT) {
            AdminRouteManagementScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ADMIN_SCHEDULE_MANAGEMENT) {
            AdminScheduleManagementScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ADMIN_REPORTS) {
            AdminReportsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavDestinations.ADMIN_FEEDBACK) {
            AdminFeedbackScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = "SmartBus",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "SmartBus",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "CAS Chelakkara GPS Transit System",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        }
    }
}

enum class PassengerTab(val label: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "tab_home"),
    MAP("Live Map", Icons.Default.Map, "tab_live_map"),
    ROUTES("Routes", Icons.Default.AltRoute, "tab_routes"),
    ALERTS("Alerts", Icons.Default.Notifications, "tab_alerts"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

@Composable
fun PassengerMainContainer(
    repository: SmartBusRepository,
    onNavigateToSearch: () -> Unit,
    onNavigateToBusDetails: (String) -> Unit,
    onNavigateToRouteDetails: (String) -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToFeedback: (String?) -> Unit,
    onLogout: () -> Unit,
    onRoleChanged: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(PassengerTab.HOME) }
    var mapInitialBusId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                PassengerTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                PassengerTab.HOME -> PassengerHomeScreen(
                    repository = repository,
                    onNavigateToMap = { busId ->
                        mapInitialBusId = busId
                        selectedTab = PassengerTab.MAP
                    },
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToRoutes = { selectedTab = PassengerTab.ROUTES },
                    onNavigateToTimetable = onNavigateToTimetable,
                    onNavigateToBusDetails = onNavigateToBusDetails,
                    onNavigateToAlerts = { selectedTab = PassengerTab.ALERTS },
                    onNavigateToFeedback = { onNavigateToFeedback(null) }
                )
                PassengerTab.MAP -> LiveMapScreen(
                    repository = repository,
                    initialBusId = mapInitialBusId,
                    onNavigateToBusDetails = onNavigateToBusDetails,
                    onNavigateToRouteDetails = onNavigateToRouteDetails,
                    onNavigateToFeedbackWithBus = { busId -> onNavigateToFeedback(busId) }
                )
                PassengerTab.ROUTES -> RoutesScreen(
                    repository = repository,
                    onNavigateToRouteDetail = onNavigateToRouteDetails,
                    onNavigateToMapWithRoute = {
                        selectedTab = PassengerTab.MAP
                    }
                )
                PassengerTab.ALERTS -> AlertsScreen(
                    repository = repository,
                    onNavigateBack = { selectedTab = PassengerTab.HOME },
                    onNavigateToBus = onNavigateToBusDetails
                )
                PassengerTab.PROFILE -> ProfileAndSettingsScreen(
                    repository = repository,
                    onLogout = onLogout,
                    onRoleChanged = onRoleChanged
                )
            }
        }
    }
}
