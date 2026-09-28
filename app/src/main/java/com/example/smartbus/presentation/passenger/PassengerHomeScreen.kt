package com.example.smartbus.presentation.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbus.data.model.Bus
import com.example.smartbus.data.model.BusStatus
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.BusCard
import com.example.smartbus.presentation.common.StatusBadge
import com.example.ui.theme.*
import com.example.smartbus.utils.GeoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassengerHomeScreen(
    repository: SmartBusRepository,
    onNavigateToMap: (String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToBusDetails: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val activeBuses = buses.filter { it.status == BusStatus.IN_TRANSIT || it.status == BusStatus.ACTIVE || it.status == BusStatus.DELAYED }
    val nextBus = activeBuses.minByOrNull { it.nextStopEtaMinutes } ?: buses.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SmartBus CAS Chelakkara",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hi, ${currentUser?.name ?: "Student"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSearch,
                        modifier = Modifier.testTag("action_search")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search buses")
                    }
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.testTag("action_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge { Text("2") }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nearest/Selected Stop Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Campus Stop",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "CAS Chelakkara Campus Gate",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Next arrival in ~${nextBus?.nextStopEtaMinutes ?: 4} mins",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Quick Actions Horizontal Row
            item {
                Text(
                    text = "Quick Services",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionItem(
                        icon = Icons.Default.Map,
                        label = "Live Map",
                        tag = "quick_live_map",
                        onClick = { onNavigateToMap(null) }
                    )
                    QuickActionItem(
                        icon = Icons.Default.AltRoute,
                        label = "Routes",
                        tag = "quick_routes",
                        onClick = onNavigateToRoutes
                    )
                    QuickActionItem(
                        icon = Icons.Default.Schedule,
                        label = "Timetable",
                        tag = "quick_timetable",
                        onClick = onNavigateToTimetable
                    )
                    QuickActionItem(
                        icon = Icons.Default.ReportProblem,
                        label = "Report",
                        tag = "quick_feedback",
                        onClick = onNavigateToFeedback
                    )
                }
            }

            // Next Available Bus Highlight Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Approaching Bus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (nextBus != null) {
                        TextButton(onClick = { onNavigateToBusDetails(nextBus.busId) }) {
                            Text("Full Details")
                        }
                    }
                }

                if (nextBus != null) {
                    BusCard(
                        bus = nextBus,
                        onClick = { onNavigateToBusDetails(nextBus.busId) },
                        onTrackClick = { onNavigateToMap(nextBus.busId) }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.DirectionsBusFilled, null, Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No buses are currently being tracked.",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Next scheduled bus: 08:15 AM",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Active Fleet Tracking List
            item {
                Text(
                    text = "Live Fleet on Routes (${activeBuses.size} Active)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(activeBuses) { bus ->
                BusCard(
                    bus = bus,
                    onClick = { onNavigateToBusDetails(bus.busId) },
                    onTrackClick = { onNavigateToMap(bus.busId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
