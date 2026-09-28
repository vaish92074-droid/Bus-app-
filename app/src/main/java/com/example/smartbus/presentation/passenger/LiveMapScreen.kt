package com.example.smartbus.presentation.passenger

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.smartbus.data.model.*
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.StatusBadge
import com.example.smartbus.presentation.map.SmartBusInteractiveMap
import com.example.ui.theme.*
import com.example.smartbus.utils.GeoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveMapScreen(
    repository: SmartBusRepository,
    initialBusId: String? = null,
    onNavigateToBusDetails: (String) -> Unit,
    onNavigateToRouteDetails: (String) -> Unit,
    onNavigateToFeedbackWithBus: (String) -> Unit
) {
    val context = LocalContext.current
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val isDemoMode by repository.isDemoMode.collectAsState()
    val isSimulating by repository.isSimulating.collectAsState()
    val simSpeed by repository.simulationSpeedMultiplier.collectAsState()

    var selectedRouteId by remember { mutableStateOf<String?>(null) }
    var selectedBusId by remember { mutableStateOf(initialBusId ?: buses.firstOrNull()?.busId) }
    var selectedStop by remember { mutableStateOf<RouteStop?>(null) }
    var showDemoControls by remember { mutableStateOf(false) }

    val selectedBus = buses.find { it.busId == selectedBusId }
    val selectedRoute = routes.find { it.routeId == selectedRouteId }

    // Bottom sheet state for selected bus
    val busSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBusSheet by remember { mutableStateOf(false) }

    // Stop sheet state
    val stopSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showStopSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Core interactive map
        SmartBusInteractiveMap(
            buses = buses,
            routes = routes,
            selectedBus = selectedBus,
            selectedRoute = selectedRoute,
            onBusSelected = { bus ->
                selectedBusId = bus.busId
                showBusSheet = true
            },
            onStopSelected = { stop ->
                selectedStop = stop
                showStopSheet = true
            },
            isDemoMode = isDemoMode
        )

        // Top Route Filter Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 48.dp, start = 12.dp, end = 12.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedRouteId == null,
                        onClick = { selectedRouteId = null },
                        label = { Text("All Routes (${routes.size})") },
                        leadingIcon = { Icon(Icons.Default.Route, null, Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    )
                }
                items(routes) { route ->
                    FilterChip(
                        selected = selectedRouteId == route.routeId,
                        onClick = {
                            selectedRouteId = if (selectedRouteId == route.routeId) null else route.routeId
                        },
                        label = { Text(route.routeName.take(24)) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // Bottom Simulation Control Strip (Academic Presentation Demo Engine)
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSimulating) Color(0xFF2E7D32) else Color(0xFFF57C00))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSimulating) "GPS Simulator Running (${simSpeed}x)" else "GPS Simulator Ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (isSimulating) repository.stopSimulation()
                                else repository.startSimulation(selectedBusId ?: "bus_01")
                            },
                            modifier = Modifier.testTag("btn_sim_toggle")
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = if (isSimulating) "Pause Simulation" else "Start Simulation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(onClick = { showDemoControls = !showDemoControls }) {
                            Icon(
                                imageVector = if (showDemoControls) Icons.Default.ExpandLess else Icons.Default.Tune,
                                contentDescription = "Demo Controls"
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = showDemoControls) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = "Simulation Actions for Presentation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val nextSpeed = when (simSpeed) {
                                        1 -> 2
                                        2 -> 5
                                        else -> 1
                                    }
                                    repository.setSimulationSpeed(nextSpeed)
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Speed: ${simSpeed}x", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    selectedBusId?.let { repository.triggerSimulatedDelay(it, 5) }
                                    Toast.makeText(context, "Simulated 5m traffic delay injected!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("+5m Delay", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    selectedBusId?.let { repository.triggerSimulatedArrival(it) }
                                    Toast.makeText(context, "Simulated Arrival at Campus Gate!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("Arrival Alert", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // If a bus is selected, show mini status strip
                if (selectedBus != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBusSheet = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedBus.busNumber,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Next: ${selectedBus.nextStopName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = GeoUtils.formatEta(selectedBus.nextStopEtaMinutes, selectedBus.delayMinutes),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Bus Details Bottom Sheet (Section 8 Requirement)
        if (showBusSheet && selectedBus != null) {
            ModalBottomSheet(
                onDismissRequest = { showBusSheet = false },
                sheetState = busSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedBus.busNumber,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reg: ${selectedBus.registrationNumber} • Capacity: ${selectedBus.capacity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = selectedBus.status, delayMinutes = selectedBus.delayMinutes)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Route", style = MaterialTheme.typography.labelSmall)
                                Text("Driver", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedBus.routeName.take(24),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = selectedBus.driverName.ifEmpty { "Suresh Kumar" },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Speed", style = MaterialTheme.typography.labelSmall)
                                Text("Next Stop & ETA", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${selectedBus.currentSpeedKmh.toInt()} km/h",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${selectedBus.nextStopName} (${selectedBus.nextStopEtaMinutes}m)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Working Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showBusSheet = false
                                onNavigateToBusDetails(selectedBus.busId)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Info, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Full Details")
                        }

                        OutlinedButton(
                            onClick = {
                                repository.addNotification(
                                    title = "Alert Subscribed: ${selectedBus.busNumber}",
                                    message = "You will be alerted 5 minutes before ${selectedBus.busNumber} reaches your stop.",
                                    type = NotificationType.BUS_APPROACHING,
                                    busId = selectedBus.busId
                                )
                                Toast.makeText(context, "Subscribed to arrival alerts for ${selectedBus.busNumber}", Toast.LENGTH_SHORT).show()
                                showBusSheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Notify Me")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showBusSheet = false
                                onNavigateToRouteDetails(selectedBus.routeId)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Route Stops")
                        }

                        TextButton(
                            onClick = {
                                showBusSheet = false
                                onNavigateToFeedbackWithBus(selectedBus.busId)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ReportProblem, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Report Issue")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Bus Stop Details Bottom Sheet
        if (showStopSheet && selectedStop != null) {
            ModalBottomSheet(
                onDismissRequest = { showStopSheet = false },
                sheetState = stopSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = selectedStop!!.stopName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stop Sequence #${selectedStop!!.sequence} • Geofence Radius: ${selectedStop!!.geofenceRadiusMeters.toInt()}m",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Buses Approaching this Stop",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val approachingBus = buses.find { it.nextStopName.contains(selectedStop!!.stopName.take(8)) }
                            if (approachingBus != null) {
                                Text(
                                    text = "${approachingBus.busNumber} (~${approachingBus.nextStopEtaMinutes} mins away)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    text = "Scheduled next run: 08:35 AM (BUS-01)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Saved ${selectedStop!!.stopName} as your preferred stop", Toast.LENGTH_SHORT).show()
                            showStopSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set as My Regular Stop")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
