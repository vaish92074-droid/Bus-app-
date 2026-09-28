package com.example.smartbus.presentation.passenger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbus.data.model.Bus
import com.example.smartbus.data.model.BusRoute
import com.example.smartbus.data.model.RouteStop
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToBus: (String) -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()

    val filteredBuses = remember(searchQuery, buses) {
        if (searchQuery.isBlank()) emptyList()
        else buses.filter {
            it.busNumber.contains(searchQuery, ignoreCase = true) ||
            it.registrationNumber.contains(searchQuery, ignoreCase = true) ||
            it.routeName.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredRoutes = remember(searchQuery, routes) {
        if (searchQuery.isBlank()) emptyList()
        else routes.filter {
            it.routeName.contains(searchQuery, ignoreCase = true) ||
            it.source.contains(searchQuery, ignoreCase = true) ||
            it.destination.contains(searchQuery, ignoreCase = true) ||
            it.stops.any { stop -> stop.stopName.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search bus, stop, or route...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, null)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("search_text_input"),
                        shape = RoundedCornerShape(24.dp)
                    )
                },
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
            if (searchQuery.isBlank()) {
                item {
                    Text(
                        text = "Quick Searches",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BUS-01", "Cherakkuzhi", "Chelakkara", "Thiruvilwamala", "Pampady").forEach { suggestion ->
                            AssistChip(
                                onClick = { searchQuery = suggestion },
                                label = { Text(suggestion) },
                                leadingIcon = { Icon(Icons.Default.History, null, Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            } else {
                if (filteredBuses.isEmpty() && filteredRoutes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No matching buses, stops, or routes found for '$searchQuery'",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (filteredBuses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Matching Buses (${filteredBuses.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(filteredBuses) { bus ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToBus(bus.busId) }
                                .testTag("search_result_bus_${bus.busId}"),
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
                                    Text(bus.busNumber, fontWeight = FontWeight.Bold)
                                    Text(bus.routeName, style = MaterialTheme.typography.bodySmall)
                                    Text("Next: ${bus.nextStopName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                StatusBadge(status = bus.status, delayMinutes = bus.delayMinutes)
                            }
                        }
                    }
                }

                if (filteredRoutes.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Matching Routes & Stops (${filteredRoutes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(filteredRoutes) { route ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToRoute(route.routeId) }
                                .testTag("search_result_route_${route.routeId}"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(route.routeName, fontWeight = FontWeight.Bold)
                                Text("${route.source} → ${route.destination}", style = MaterialTheme.typography.bodySmall)
                                Text("${route.stops.size} stops • ${route.distanceKm} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToBus: (String) -> Unit
) {
    val schedules by repository.schedules.collectAsState()
    val routes by repository.routes.collectAsState()
    var selectedRouteFilter by remember { mutableStateOf<String?>(null) }

    val displayedSchedules = if (selectedRouteFilter == null) schedules else schedules.filter { it.routeId == selectedRouteFilter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Bus Timetable") },
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
                Text(
                    text = "Daily Campus Schedule (8:00 AM – 5:00 PM)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scheduled times, live delay adherence, and stop arrival stamps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Route filter chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedRouteFilter == null,
                            onClick = { selectedRouteFilter = null },
                            label = { Text("All Routes") }
                        )
                    }
                    items(routes) { route ->
                        FilterChip(
                            selected = selectedRouteFilter == route.routeId,
                            onClick = { selectedRouteFilter = if (selectedRouteFilter == route.routeId) null else route.routeId },
                            label = { Text(route.source) }
                        )
                    }
                }
            }

            items(displayedSchedules) { schedule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = schedule.busNumber,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = schedule.routeName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AssistChip(
                                onClick = { onNavigateToBus(schedule.busId) },
                                label = { Text("Departure: ${schedule.departureTime}") },
                                leadingIcon = { Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Stops & Arrival Schedule",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        schedule.stopTimes.forEach { stopTime ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.RadioButtonChecked, null, Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stopTime.stopName, fontSize = 13.sp)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stopTime.scheduledTime,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    if (stopTime.actualArrivalTime != null) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "(${stopTime.actualArrivalTime})",
                                            color = StatusOnTime,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
