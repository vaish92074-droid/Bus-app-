package com.example.smartbus.presentation.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbus.data.model.*
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.smartbus.presentation.common.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBusManagementScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val drivers by repository.drivers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingBus by remember { mutableStateOf<Bus?>(null) }

    var busNumber by remember { mutableStateOf("") }
    var regNumber by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("45") }
    var selectedRouteId by remember { mutableStateOf("") }
    var selectedDriverId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bus Fleet Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBus = null
                    busNumber = "BUS-0${buses.size + 1}"
                    regNumber = "KL-48-G-${1000 + buses.size}"
                    capacity = "48"
                    selectedRouteId = routes.firstOrNull()?.routeId ?: ""
                    selectedDriverId = drivers.firstOrNull()?.driverId ?: ""
                    showAddDialog = true
                },
                modifier = Modifier.testTag("fab_add_bus")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bus")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Registered Campus Buses (${buses.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(buses) { bus ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(bus.busNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Reg: ${bus.registrationNumber} • ${bus.capacity} seats", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusBadge(status = bus.status, delayMinutes = bus.delayMinutes)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Route: ${bus.routeName}", style = MaterialTheme.typography.bodySmall)
                        Text("Driver: ${bus.driverName.ifEmpty { "Unassigned" }}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    editingBus = bus
                                    busNumber = bus.busNumber
                                    regNumber = bus.registrationNumber
                                    capacity = bus.capacity.toString()
                                    selectedRouteId = bus.routeId
                                    selectedDriverId = bus.driverId
                                    showAddDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            TextButton(
                                onClick = {
                                    repository.deactivateBus(bus.busId)
                                    Toast.makeText(context, "${bus.busNumber} marked inactive/offline", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Deactivate", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingBus == null) "Register New Bus" else "Update Bus") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = busNumber,
                        onValueChange = { busNumber = it },
                        label = { Text("Bus Name / Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regNumber,
                        onValueChange = { regNumber = it },
                        label = { Text("Registration Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Passenger Capacity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val route = routes.find { it.routeId == selectedRouteId } ?: routes.firstOrNull()
                        val driver = drivers.find { it.driverId == selectedDriverId } ?: drivers.firstOrNull()

                        if (editingBus == null) {
                            val newBus = Bus(
                                busId = "bus_${System.currentTimeMillis() % 10000}",
                                busNumber = busNumber,
                                registrationNumber = regNumber,
                                capacity = capacity.toIntOrNull() ?: 45,
                                routeId = route?.routeId ?: "",
                                routeName = route?.routeName ?: "CAS Campus Line",
                                driverId = driver?.driverId ?: "",
                                driverName = driver?.name ?: "Suresh Kumar",
                                status = BusStatus.ACTIVE,
                                active = true,
                                nextStopName = route?.stops?.firstOrNull()?.stopName ?: "Campus"
                            )
                            repository.addBus(newBus)
                            Toast.makeText(context, "Bus registered successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            val updated = editingBus!!.copy(
                                busNumber = busNumber,
                                registrationNumber = regNumber,
                                capacity = capacity.toIntOrNull() ?: 45
                            )
                            repository.updateBus(updated)
                            Toast.makeText(context, "Bus updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDriverManagementScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val drivers by repository.drivers.collectAsState()
    val buses by repository.buses.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Driver Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    name = "Vipin Chandran"
                    phone = "+91 94472 88771"
                    empId = "DRV-${105 + drivers.size}"
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Driver")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Enrolled Fleet Drivers (${drivers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(drivers) { driver ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(driver.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Emp ID: ${driver.employeeId} • Phone: ${driver.phone}", style = MaterialTheme.typography.bodySmall)
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(if (driver.active) "Active" else "Inactive") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Assigned Vehicle: ${driver.assignedBusNumber}", style = MaterialTheme.typography.bodySmall)
                        Text("Punctuality Score: ${driver.punctualityScore}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    repository.deactivateDriver(driver.driverId)
                                    Toast.makeText(context, "Driver status toggled", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Toggle Active")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enroll New Driver") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Driver Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = empId,
                        onValueChange = { empId = it },
                        label = { Text("Employee ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newDriver = Driver(
                            driverId = "drv_${System.currentTimeMillis() % 1000}",
                            userId = "usr_drv_${System.currentTimeMillis() % 1000}",
                            name = name,
                            phone = phone,
                            email = "${name.lowercase().replace(" ", "")}@smartbus.cas",
                            employeeId = empId,
                            assignedBusId = "bus_01",
                            assignedBusNumber = "BUS-01 (CAS Express)",
                            active = true
                        )
                        repository.addDriver(newDriver)
                        Toast.makeText(context, "Driver enrolled successfully", Toast.LENGTH_SHORT).show()
                        showDialog = false
                    }
                ) {
                    Text("Enroll")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRouteManagementScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val routes by repository.routes.collectAsState()
    var selectedRoute by remember { mutableStateOf<BusRoute?>(null) }
    var geofenceRadiusText by remember { mutableStateOf("50") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Route & Stop Geofence Editor") },
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
            item {
                Text(
                    text = "Configured College Bus Corridors (${routes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any route to view and configure stop sequence and geofence radius.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(routes) { route ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRoute = route },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(route.routeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${route.source} → ${route.destination}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${route.stops.size} stops • ${route.distanceKm} km • Default Geofence: 50m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (selectedRoute != null) {
        AlertDialog(
            onDismissRequest = { selectedRoute = null },
            title = { Text("Configure: ${selectedRoute!!.routeName.take(24)}") },
            text = {
                Column {
                    Text("Total Geofenced Stops: ${selectedRoute!!.stops.size}")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = geofenceRadiusText,
                        onValueChange = { geofenceRadiusText = it },
                        label = { Text("Geofence Radius (meters)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Default radius is 50m. When a tracked bus GPS enters this radius, arrival is stamped.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val radius = geofenceRadiusText.toDoubleOrNull() ?: 50.0
                        val updatedStops = selectedRoute!!.stops.map { it.copy(geofenceRadiusMeters = radius) }
                        val updatedRoute = selectedRoute!!.copy(stops = updatedStops)
                        repository.updateRoute(updatedRoute)
                        Toast.makeText(context, "Geofence radius set to ${radius.toInt()}m for all stops in route", Toast.LENGTH_SHORT).show()
                        selectedRoute = null
                    }
                ) {
                    Text("Save Radius")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRoute = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScheduleManagementScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val schedules by repository.schedules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Schedule Management") },
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
            item {
                Text(
                    text = "Active Transit Duty Schedules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(schedules) { schedule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(schedule.busNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            AssistChip(
                                onClick = {},
                                label = { Text(if (schedule.cancelled) "CANCELLED" else "Departure: ${schedule.departureTime}") }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Route: ${schedule.routeName}", style = MaterialTheme.typography.bodySmall)
                        Text("Operating Days: ${schedule.operatingDays}", style = MaterialTheme.typography.bodySmall)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    repository.cancelSchedule(schedule.scheduleId)
                                    Toast.makeText(context, "Schedule marked CANCELLED and alerts dispatched.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Cancel Run", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
