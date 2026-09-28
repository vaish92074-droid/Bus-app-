package com.example.smartbus.presentation.driver

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartbus.data.model.BusStatus
import com.example.smartbus.data.model.Trip
import com.example.smartbus.data.model.TripStatus
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.MetricCard
import com.example.smartbus.presentation.common.StatusBadge
import com.example.smartbus.presentation.common.StopTimelineView
import com.example.ui.theme.*
import com.example.smartbus.utils.GeoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    repository: SmartBusRepository,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val activeTrips by repository.activeTrips.collectAsState()

    // Default assigned driver details
    val assignedBus = buses.firstOrNull { it.busId == "bus_01" } ?: buses.first()
    val assignedRoute = routes.firstOrNull { it.routeId == assignedBus.routeId } ?: routes.first()
    val currentTrip = activeTrips[assignedBus.busId]

    var gpsPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        gpsPermissionGranted = fineGranted || coarseGranted

        if (gpsPermissionGranted) {
            repository.startTrip(assignedBus.busId, assignedRoute.routeId, "drv_01")
            Toast.makeText(context, "Location permission granted. Trip started!", Toast.LENGTH_SHORT).show()
        } else {
            // Inform driver about simulation fallback
            repository.startTrip(assignedBus.busId, assignedRoute.routeId, "drv_01")
            Toast.makeText(context, "Running in Academic GPS Simulation Mode", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Driver Operations Console", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Driver: ${currentUser?.name ?: "Suresh Kumar"} • ${assignedBus.busNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAlerts) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                    }
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
            // Current Trip Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentTrip != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (currentTrip != null) Color(0xFF2E7D32) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentTrip != null) "TRIP IN PROGRESS" else "NO ACTIVE TRIP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (currentTrip != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(
                                status = if (currentTrip != null) BusStatus.IN_TRANSIT else BusStatus.OFFLINE,
                                delayMinutes = currentTrip?.delayMinutes ?: 0
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = assignedRoute.routeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (currentTrip != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Assigned Vehicle: ${assignedBus.busNumber} (${assignedBus.registrationNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (currentTrip != null) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Big Trip Control Button
                        if (currentTrip == null) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("btn_driver_start_trip"),
                                colors = ButtonDefaults.buttonColors(containerColor = SmartBusPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text("START TRIP & SHARE GPS", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        repository.endTrip(assignedBus.busId)
                                        Toast.makeText(context, "Trip successfully ended and saved.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("btn_driver_end_trip"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Stop, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("END TRIP")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val newDelay = if (currentTrip.delayMinutes == 0) 5 else 0
                                        repository.triggerSimulatedDelay(assignedBus.busId, newDelay)
                                        Toast.makeText(context, if (newDelay > 0) "Marked delayed by 5 mins" else "Cleared delay", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(if (currentTrip.delayMinutes == 0) "Report Delay (+5m)" else "Clear Delay")
                                }
                            }
                        }
                    }
                }
            }

            // Live Telemetry Grid
            item {
                Text(
                    text = "Live GPS Telemetry",
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
                        title = "Speed",
                        value = "${assignedBus.currentSpeedKmh.toInt()} km/h",
                        icon = Icons.Default.Speed,
                        color = SmartBusPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "GPS Signal",
                        value = if (currentTrip != null) "Active" else "Standby",
                        icon = Icons.Default.GpsFixed,
                        color = if (currentTrip != null) StatusOnTime else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Delay Adherence",
                        value = if (currentTrip?.delayMinutes ?: 0 > 0) "+${currentTrip!!.delayMinutes}m" else "0m",
                        icon = Icons.Default.Schedule,
                        color = if (currentTrip?.delayMinutes ?: 0 > 0) StatusDelayed else StatusOnTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Next Stop Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Next Geofenced Stop",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = assignedBus.nextStopName.ifEmpty { "Cherakkuzhi Center" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "~${assignedBus.nextStopEtaMinutes} mins",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusOnTime
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Geofence auto-detects arrival within 50 meters and records official timestamp.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Operational Shortcuts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onNavigateToRoute(assignedRoute.routeId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AltRoute, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("View Route")
                    }

                    FilledTonalButton(
                        onClick = onNavigateToSchedule,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("My Schedule")
                    }
                }
            }

            // Stop progression
            item {
                Text(
                    text = "Today's Stop Progression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    StopTimelineView(
                        stops = assignedRoute.stops,
                        currentStopIndex = currentTrip?.currentStopIndex ?: 0,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScheduleScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val schedules by repository.schedules.collectAsState()
    val driverSchedule = schedules.firstOrNull { it.busId == "bus_01" } ?: schedules.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Duty Roster") },
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Today's Assigned Duty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${driverSchedule?.busNumber} • Departure: ${driverSchedule?.departureTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Route: ${driverSchedule?.routeName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Scheduled Stops & Target Timings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            driverSchedule?.stopTimes?.let { stopTimes ->
                items(stopTimes) { stopTime ->
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
                                Text(stopTime.stopName, fontWeight = FontWeight.SemiBold)
                                Text("Sequence #${stopTime.sequence}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = stopTime.scheduledTime,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
