package com.example.smartbus.presentation.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbus.data.model.Bus
import com.example.smartbus.data.model.BusStatus
import com.example.smartbus.data.model.FeedbackItem
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.BusCard
import com.example.smartbus.presentation.common.MetricCard
import com.example.smartbus.presentation.common.StatusBadge
import com.example.smartbus.presentation.map.SmartBusInteractiveMap
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    repository: SmartBusRepository,
    onNavigateToLiveMonitoring: () -> Unit,
    onNavigateToBusManagement: () -> Unit,
    onNavigateToDriverManagement: () -> Unit,
    onNavigateToRouteManagement: () -> Unit,
    onNavigateToScheduleManagement: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onLogout: () -> Unit
) {
    val buses by repository.buses.collectAsState()
    val drivers by repository.drivers.collectAsState()
    val routes by repository.routes.collectAsState()
    val schedules by repository.schedules.collectAsState()
    val activeTrips by repository.activeTrips.collectAsState()
    val feedbackList by repository.feedbackList.collectAsState()

    val totalBuses = buses.size
    val activeBuses = buses.count { it.status == BusStatus.IN_TRANSIT || it.status == BusStatus.ACTIVE }
    val offlineBuses = buses.count { it.status == BusStatus.OFFLINE }
    val delayedBuses = buses.count { it.delayMinutes > 0 }
    val onTimePercentage = if (activeBuses > 0) ((activeBuses - delayedBuses) * 100 / activeBuses) else 95

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fleet Command Center", fontWeight = FontWeight.Bold)
                        Text(
                            "CAS Chelakkara Transport Office",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Speed, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Transit System Adherence",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "$onTimePercentage% Punctuality Score",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$activeBuses buses tracking • ${feedbackList.count { it.status == "PENDING" }} unresolved grievances",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Key Operations Metrics Grid
            item {
                Text(
                    text = "Fleet Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Fleet",
                        value = "$totalBuses",
                        subtitle = "$activeBuses Active",
                        icon = Icons.Default.DirectionsBus,
                        color = SmartBusPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Drivers",
                        value = "${drivers.size}",
                        subtitle = "${drivers.count { it.active }} On Duty",
                        icon = Icons.Default.Badge,
                        color = Color(0xFF007A99),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Delayed",
                        value = "$delayedBuses",
                        subtitle = "Avg +4m",
                        icon = Icons.Default.Warning,
                        color = if (delayedBuses > 0) StatusDelayed else StatusOnTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Management Navigation Hub
            item {
                Text(
                    text = "System Administration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminNavRow(
                        title = "Live Fleet Map Monitor",
                        subtitle = "Watch all active buses moving in real time with bearings",
                        icon = Icons.Default.Map,
                        color = SmartBusPrimary,
                        onClick = onNavigateToLiveMonitoring
                    )
                    AdminNavRow(
                        title = "Bus Fleet Management",
                        subtitle = "Add, edit, assign drivers, or deactivate buses (${buses.size})",
                        icon = Icons.Default.DirectionsBusFilled,
                        color = Color(0xFF007A99),
                        onClick = onNavigateToBusManagement
                    )
                    AdminNavRow(
                        title = "Driver Registry & Rosters",
                        subtitle = "Manage drivers, licenses, and performance ratings (${drivers.size})",
                        icon = Icons.Default.PersonSearch,
                        color = Color(0xFF00897B),
                        onClick = onNavigateToDriverManagement
                    )
                    AdminNavRow(
                        title = "Route & Geofence Stops",
                        subtitle = "Configure route paths, stop geofence radii (50m), distances",
                        icon = Icons.Default.AltRoute,
                        color = Color(0xFF5E35B1),
                        onClick = onNavigateToRouteManagement
                    )
                    AdminNavRow(
                        title = "Timetable & Schedules",
                        subtitle = "Morning and evening trip schedules (8:00 AM – 5:00 PM)",
                        icon = Icons.Default.Schedule,
                        color = Color(0xFFD81B60),
                        onClick = onNavigateToScheduleManagement
                    )
                    AdminNavRow(
                        title = "Punctuality Reports & Analytics",
                        subtitle = "Delay hotspots, driver scorecards, route statistics",
                        icon = Icons.Default.Analytics,
                        color = Color(0xFFE65100),
                        onClick = onNavigateToReports
                    )
                    AdminNavRow(
                        title = "Student Grievance & Feedback",
                        subtitle = "Review and resolve submitted student issues (${feedbackList.size})",
                        icon = Icons.Default.Feedback,
                        color = Color(0xFF43A047),
                        onClick = onNavigateToFeedback
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AdminNavRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLiveMonitoringScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToBusDetails: (String) -> Unit
) {
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    var selectedBusId by remember { mutableStateOf<String?>(buses.firstOrNull()?.busId) }
    val selectedBus = buses.find { it.busId == selectedBusId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fleet Live Monitoring") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SmartBusInteractiveMap(
                buses = buses,
                routes = routes,
                selectedBus = selectedBus,
                selectedRoute = null,
                onBusSelected = { selectedBusId = it.busId },
                onStopSelected = {}
            )

            // Bottom Selected Bus Info Card
            if (selectedBus != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(selectedBus.busNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Driver: ${selectedBus.driverName.ifEmpty { "Suresh Kumar" }}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(status = selectedBus.status, delayMinutes = selectedBus.delayMinutes)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Speed: ${selectedBus.currentSpeedKmh.toInt()} km/h", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Next: ${selectedBus.nextStopName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onNavigateToBusDetails(selectedBus.busId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Bus Management")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val buses by repository.buses.collectAsState()
    val drivers by repository.drivers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Adherence Reports") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Punctuality Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "1. Punctuality & Schedule Adherence",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("On-Time Rate", style = MaterialTheme.typography.labelSmall)
                                Text("94.2%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StatusOnTime)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Late Rate", style = MaterialTheme.typography.labelSmall)
                                Text("5.8%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StatusDelayed)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg Delay", style = MaterialTheme.typography.labelSmall)
                                Text("3.4 mins", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SmartBusPrimary)
                            }
                        }
                    }
                }
            }

            // Delay Hotspots
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "2. Delay Hotspots (Top Bottlenecks)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            Triple("Cherakkuzhi Center", "Thiruvilwamala Route", "6 incidents (Avg +5m)"),
                            Triple("Pampady Junction", "Thiruvilwamala Route", "4 incidents (Avg +4m)"),
                            Triple("Pazhayannur Temple Jn", "Wadakkanchery Route", "3 incidents (Avg +3m)")
                        ).forEach { (stop, route, stat) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(stop, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(route, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(stat, fontWeight = FontWeight.Bold, color = StatusDelayed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Driver Scorecards
            item {
                Text(
                    text = "3. Driver Performance Scorecards",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(drivers) { driver ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(driver.name, fontWeight = FontWeight.Bold)
                            Text("Trips Completed: ${driver.completedTrips}", style = MaterialTheme.typography.bodySmall)
                            Text("Bus: ${driver.assignedBusNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${driver.punctualityScore}% Score",
                                fontWeight = FontWeight.Bold,
                                color = if (driver.punctualityScore >= 90) StatusOnTime else StatusDelayed
                            )
                            Text("GPS Reliability: 99%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val feedbackList by repository.feedbackList.collectAsState()
    var selectedFeedback by remember { mutableStateOf<FeedbackItem?>(null) }
    var replyText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passenger Grievances & Feedback") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(feedbackList) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedFeedback = item
                            replyText = item.adminReply
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.userName} (${item.category})",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(item.status) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.message, style = MaterialTheme.typography.bodyMedium)

                        if (item.adminReply.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Admin Note: ${item.adminReply}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedFeedback != null) {
        AlertDialog(
            onDismissRequest = { selectedFeedback = null },
            title = { Text("Resolve Grievance: ${selectedFeedback!!.category}") },
            text = {
                Column {
                    Text(selectedFeedback!!.message, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Official Response") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateFeedbackStatus(selectedFeedback!!.id, "RESOLVED", replyText)
                        Toast.makeText(context, "Grievance marked RESOLVED with reply dispatched", Toast.LENGTH_SHORT).show()
                        selectedFeedback = null
                    }
                ) {
                    Text("Mark Resolved")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFeedback = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
