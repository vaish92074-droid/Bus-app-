package com.example.smartbus.presentation.passenger

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
import com.example.smartbus.data.model.BusRoute
import com.example.smartbus.data.model.BusStatus
import com.example.smartbus.data.model.NotificationType
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.StatusBadge
import com.example.smartbus.presentation.common.StopTimelineView
import com.example.ui.theme.*
import com.example.smartbus.utils.GeoUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(
    repository: SmartBusRepository,
    onNavigateToRouteDetail: (String) -> Unit,
    onNavigateToMapWithRoute: (String) -> Unit
) {
    val routes by repository.routes.collectAsState()
    val buses by repository.buses.collectAsState()
    val favoriteRoutes by repository.favoriteRoutes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Bus Routes") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Operational Lines (CAS Chelakkara)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connecting Thiruvilwamala, Cherakkuzhi, Pazhayannur & Chelakkara campus",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(routes) { route ->
                val activeBusesCount = buses.count { it.routeId == route.routeId && it.status == BusStatus.IN_TRANSIT }
                val isFav = favoriteRoutes.contains(route.routeId)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRouteDetail(route.routeId) }
                        .testTag("route_card_${route.routeId}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = route.routeName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { repository.toggleFavoriteRoute(route.routeId) }
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TripOrigin, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(route.source, style = MaterialTheme.typography.bodySmall)
                            Icon(Icons.Default.ArrowForward, null, Modifier.size(12.dp))
                            Icon(Icons.Default.Place, null, Modifier.size(14.dp), tint = Color(0xFFE65100))
                            Text(route.destination, style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column {
                                    Text("Distance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${route.distanceKm} km", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Stops", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${route.stops.size} stops", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Est. Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${route.estimatedDurationMinutes} mins", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }

                            FilledTonalButton(
                                onClick = { onNavigateToMapWithRoute(route.routeId) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Map, null, Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Map", fontSize = 12.sp)
                            }
                        }
                    }
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
fun RouteDetailScreen(
    routeId: String,
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToBus: (String) -> Unit
) {
    val routes by repository.routes.collectAsState()
    val buses by repository.buses.collectAsState()
    val route = routes.find { it.routeId == routeId } ?: routes.firstOrNull()
    val activeBusesOnRoute = buses.filter { it.routeId == route?.routeId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route?.routeName ?: "Route Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToMap) {
                        Icon(Icons.Default.Map, contentDescription = "View on Map")
                    }
                }
            )
        }
    ) { padding ->
        if (route == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Route not found") }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overview card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = route.routeName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Total: ${route.distanceKm} km", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Duration: ~${route.estimatedDurationMinutes} mins", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Stops: ${route.stops.size}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }

            // Buses running on this route
            item {
                Text(
                    text = "Buses Running on this Route (${activeBusesOnRoute.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(activeBusesOnRoute) { bus ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToBus(bus.busId) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(bus.busNumber, fontWeight = FontWeight.Bold)
                            Text("Next Stop: ${bus.nextStopName}", style = MaterialTheme.typography.bodySmall)
                        }
                        StatusBadge(status = bus.status, delayMinutes = bus.delayMinutes)
                    }
                }
            }

            // Ordered Stop Timeline
            item {
                Text(
                    text = "Route Progression & Geofenced Stops",
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
                        stops = route.stops,
                        currentStopIndex = 4, // Mid-route illustration
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusDetailScreen(
    busId: String,
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToMap: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val favoriteBuses by repository.favoriteBuses.collectAsState()

    val bus = buses.find { it.busId == busId } ?: buses.firstOrNull()
    val route = routes.find { it.routeId == bus?.routeId }
    val isFav = favoriteBuses.contains(bus?.busId)

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bus?.busNumber ?: "Bus Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            bus?.let { repository.toggleFavoriteBus(it.busId) }
                        }
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (bus == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Bus details unavailable") }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bus Hero Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bus.busNumber,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Registration: ${bus.registrationNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = bus.status, delayMinutes = bus.delayMinutes)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Assigned Route: ${bus.routeName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Driver: ${bus.driverName.ifEmpty { "Suresh Kumar (CAS Fleet)" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Current Speed", style = MaterialTheme.typography.labelSmall)
                            Text("${bus.currentSpeedKmh.toInt()} km/h", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SmartBusPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Next Stop ETA", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = GeoUtils.formatEta(bus.nextStopEtaMinutes, bus.delayMinutes),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (bus.delayMinutes > 0) StatusDelayed else StatusOnTime
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Capacity", style = MaterialTheme.typography.labelSmall)
                            Text("${bus.capacity} seats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Quick Operational Action Buttons (Section 15 Requirement)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigateToMap(bus.busId) },
                    modifier = Modifier.weight(1f).testTag("bus_detail_track_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Live Map")
                }

                OutlinedButton(
                    onClick = {
                        repository.addNotification(
                            title = "Subscribed: ${bus.busNumber}",
                            message = "Arrival alert set for ${bus.busNumber}. You'll be notified 5m prior to stop arrival.",
                            type = NotificationType.BUS_APPROACHING,
                            busId = bus.busId
                        )
                        Toast.makeText(context, "Arrival alert set for ${bus.busNumber}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Notify Me")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (route != null) {
                    OutlinedButton(
                        onClick = { onNavigateToRoute(route.routeId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Route")
                    }
                }

                TextButton(
                    onClick = { onNavigateToFeedback(bus.busId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ReportProblem, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Report Issue")
                }
            }

            // Next Stop & Live Progress
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Upcoming Stop",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = bus.nextStopName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Estimated arrival: ~${bus.nextStopEtaMinutes} minutes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (route != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Remaining Stops Timeline",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StopTimelineView(
                            stops = route.stops,
                            currentStopIndex = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
