package com.example.smartbus.data.repository

import android.content.Context
import android.util.Log
import com.example.smartbus.data.model.*
import com.example.smartbus.data.sample.SampleDataProvider
import com.example.smartbus.utils.GeoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SmartBusRepository private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Current Session
    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            userId = "usr_student_01",
            name = "CAS Chelakkara Student",
            email = "student@caschelakkara.edu.in",
            phone = "+91 94470 11223",
            role = UserRole.PASSENGER
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    // Bus Fleet State
    private val _buses = MutableStateFlow<List<Bus>>(SampleDataProvider.BUSES)
    val buses: StateFlow<List<Bus>> = _buses.asStateFlow()

    // Drivers State
    private val _drivers = MutableStateFlow<List<Driver>>(SampleDataProvider.DRIVERS)
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    // Routes State
    private val _routes = MutableStateFlow<List<BusRoute>>(SampleDataProvider.ROUTES)
    val routes: StateFlow<List<BusRoute>> = _routes.asStateFlow()

    // Schedules State
    private val _schedules = MutableStateFlow<List<Schedule>>(SampleDataProvider.SCHEDULES)
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    // Active Trips
    private val _activeTrips = MutableStateFlow<Map<String, Trip>>(emptyMap())
    val activeTrips: StateFlow<Map<String, Trip>> = _activeTrips.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(SampleDataProvider.INITIAL_NOTIFICATIONS)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Feedback
    private val _feedbackList = MutableStateFlow<List<FeedbackItem>>(SampleDataProvider.INITIAL_FEEDBACK)
    val feedbackList: StateFlow<List<FeedbackItem>> = _feedbackList.asStateFlow()

    // Favorites
    private val _favoriteRoutes = MutableStateFlow<Set<String>>(setOf("route_1"))
    val favoriteRoutes: StateFlow<Set<String>> = _favoriteRoutes.asStateFlow()

    private val _favoriteBuses = MutableStateFlow<Set<String>>(setOf("bus_01"))
    val favoriteBuses: StateFlow<Set<String>> = _favoriteBuses.asStateFlow()

    // Demo Mode & Simulation
    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simulationSpeedMultiplier = MutableStateFlow(2)
    val simulationSpeedMultiplier: StateFlow<Int> = _simulationSpeedMultiplier.asStateFlow()

    private var simulationJob: Job? = null

    // Safe Firebase References
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    init {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            Log.d("SmartBusRepo", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.w("SmartBusRepo", "Firebase not initialized, running in robust local mode: ${e.message}")
        }

        // Initialize active trip for BUS-01
        val initialTrip = Trip(
            tripId = "trip_demo_1",
            busId = "bus_01",
            busNumber = "BUS-01 (CAS Express)",
            driverId = "drv_01",
            driverName = "Suresh Kumar",
            routeId = "route_1",
            routeName = "Thiruvilwamala – Cherakkuzhi – CAS Chelakkara",
            startedAt = System.currentTimeMillis() - 1200000,
            status = TripStatus.IN_PROGRESS,
            currentStopIndex = 4,
            delayMinutes = 0,
            currentLatitude = 10.7018,
            currentLongitude = 76.3800,
            currentSpeedKmh = 38.0,
            gpsActive = true
        )
        _activeTrips.value = mapOf("bus_01" to initialTrip)
    }

    // --- Authentication ---

    fun login(email: String, role: UserRole, name: String? = null) {
        val resolvedName = name ?: when (role) {
            UserRole.PASSENGER -> "CAS Chelakkara Student"
            UserRole.DRIVER -> "Suresh Kumar (Driver)"
            UserRole.ADMIN -> "Transport Officer (Admin)"
        }
        val user = UserProfile(
            userId = "usr_${System.currentTimeMillis() % 10000}",
            name = resolvedName,
            email = email,
            phone = "+91 94471 00000",
            role = role
        )
        _currentUser.value = user
    }

    fun register(name: String, email: String, phone: String, role: UserRole) {
        val user = UserProfile(
            userId = "usr_${System.currentTimeMillis() % 10000}",
            name = name,
            email = email,
            phone = phone,
            role = role
        )
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Ignore
        }
    }

    // --- Driver Trip & Live Tracking ---

    fun startTrip(busId: String, routeId: String, driverId: String): Trip {
        val bus = _buses.value.find { it.busId == busId } ?: SampleDataProvider.BUSES[0]
        val route = _routes.value.find { it.routeId == routeId } ?: SampleDataProvider.ROUTE_1
        val driver = _drivers.value.find { it.driverId == driverId } ?: SampleDataProvider.DRIVERS[0]

        val firstStop = route.stops.firstOrNull()
        val startLat = firstStop?.latitude ?: 10.6974
        val startLon = firstStop?.longitude ?: 76.4385

        val tripId = "trip_${System.currentTimeMillis()}"
        val newTrip = Trip(
            tripId = tripId,
            busId = bus.busId,
            busNumber = bus.busNumber,
            driverId = driver.driverId,
            driverName = driver.name,
            routeId = route.routeId,
            routeName = route.routeName,
            startedAt = System.currentTimeMillis(),
            status = TripStatus.IN_PROGRESS,
            currentStopIndex = 0,
            delayMinutes = 0,
            currentLatitude = startLat,
            currentLongitude = startLon,
            currentSpeedKmh = 25.0,
            gpsActive = true,
            lastUpdateTimestamp = System.currentTimeMillis()
        )

        _activeTrips.update { it + (bus.busId to newTrip) }

        // Update bus record
        updateBusState(bus.busId) {
            it.copy(
                status = BusStatus.IN_TRANSIT,
                latitude = startLat,
                longitude = startLon,
                currentSpeedKmh = 25.0,
                nextStopName = route.stops.getOrNull(1)?.stopName ?: (firstStop?.stopName ?: "Destination"),
                nextStopEtaMinutes = 4,
                delayMinutes = 0,
                lastUpdated = System.currentTimeMillis(),
                currentTripId = tripId
            )
        }

        // Add notification
        addNotification(
            title = "Trip Started: ${bus.busNumber}",
            message = "Trip on route ${route.routeName} is now active.",
            type = NotificationType.TRIP_STARTED,
            busId = bus.busId,
            routeId = route.routeId,
            tripId = tripId
        )

        return newTrip
    }

    fun updateTripLocation(
        busId: String,
        latitude: Double,
        longitude: Double,
        speedKmh: Double,
        bearing: Float
    ) {
        val currentTrip = _activeTrips.value[busId] ?: return
        val route = _routes.value.find { it.routeId == currentTrip.routeId } ?: SampleDataProvider.ROUTE_1

        // Evaluate geofence arrival against upcoming stops
        var nextStopIndex = currentTrip.currentStopIndex
        val upcomingStop = route.stops.getOrNull(nextStopIndex)
        var stopEta = 5
        var delay = currentTrip.delayMinutes

        if (upcomingStop != null) {
            val distToStop = GeoUtils.calculateDistanceMeters(
                latitude, longitude,
                upcomingStop.latitude, upcomingStop.longitude
            )
            val isInside = distToStop <= upcomingStop.geofenceRadiusMeters

            stopEta = GeoUtils.calculateEtaMinutes(distToStop, speedKmh)

            if (isInside) {
                // Arrived at stop
                addNotification(
                    title = "Bus Arrived: ${upcomingStop.stopName}",
                    message = "${currentTrip.busNumber} has arrived at ${upcomingStop.stopName}.",
                    type = NotificationType.BUS_ARRIVED,
                    busId = busId,
                    routeId = currentTrip.routeId,
                    tripId = currentTrip.tripId
                )

                if (nextStopIndex + 1 < route.stops.size) {
                    nextStopIndex++
                }
            } else if (distToStop in 200.0..1000.0 && stopEta <= 3) {
                // Approaching
                addNotification(
                    title = "Approaching ${upcomingStop.stopName}",
                    message = "${currentTrip.busNumber} will arrive at ${upcomingStop.stopName} in ~$stopEta mins.",
                    type = NotificationType.BUS_APPROACHING,
                    busId = busId,
                    routeId = currentTrip.routeId,
                    tripId = currentTrip.tripId
                )
            }
        }

        val updatedTrip = currentTrip.copy(
            currentLatitude = latitude,
            currentLongitude = longitude,
            currentSpeedKmh = speedKmh,
            currentStopIndex = nextStopIndex,
            lastUpdateTimestamp = System.currentTimeMillis()
        )

        _activeTrips.update { it + (busId to updatedTrip) }

        updateBusState(busId) {
            it.copy(
                latitude = latitude,
                longitude = longitude,
                currentSpeedKmh = speedKmh,
                bearing = bearing,
                nextStopName = route.stops.getOrNull(nextStopIndex)?.stopName ?: "Destination",
                nextStopEtaMinutes = stopEta,
                delayMinutes = delay,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun endTrip(busId: String) {
        val trip = _activeTrips.value[busId]
        if (trip != null) {
            _activeTrips.update { it - busId }
            updateBusState(busId) {
                it.copy(
                    status = BusStatus.ACTIVE,
                    currentSpeedKmh = 0.0,
                    currentTripId = "",
                    nextStopName = "Completed",
                    nextStopEtaMinutes = 0,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            addNotification(
                title = "Trip Completed: ${trip.busNumber}",
                message = "The scheduled run on ${trip.routeName} has completed successfully.",
                type = NotificationType.ADMIN_ANNOUNCEMENT,
                busId = busId,
                routeId = trip.routeId
            )
        }
    }

    // --- Simulation / Demo Mode Engine ---

    fun toggleDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        if (!enabled) {
            stopSimulation()
        }
    }

    fun setSimulationSpeed(multiplier: Int) {
        _simulationSpeedMultiplier.value = multiplier
    }

    fun startSimulation(busId: String = "bus_01") {
        if (_isSimulating.value) return
        _isSimulating.value = true

        val bus = _buses.value.find { it.busId == busId } ?: SampleDataProvider.BUSES[0]
        val route = _routes.value.find { it.routeId == bus.routeId } ?: SampleDataProvider.ROUTE_1

        simulationJob?.cancel()
        simulationJob = scope.launch {
            var stepIndex = 0
            val pathCoords = mutableListOf<Pair<Double, Double>>()

            // Generate intermediate interpolated points between stops for smooth animation
            for (i in 0 until route.stops.size - 1) {
                val s1 = route.stops[i]
                val s2 = route.stops[i + 1]
                val intermediateSteps = 6
                for (s in 0 until intermediateSteps) {
                    val factor = s.toDouble() / intermediateSteps
                    val lat = s1.latitude + (s2.latitude - s1.latitude) * factor
                    val lon = s1.longitude + (s2.longitude - s1.longitude) * factor
                    pathCoords.add(Pair(lat, lon))
                }
            }
            val lastStop = route.stops.last()
            pathCoords.add(Pair(lastStop.latitude, lastStop.longitude))

            while (isActive && _isSimulating.value) {
                val current = pathCoords[stepIndex]
                val next = pathCoords[(stepIndex + 1) % pathCoords.size]

                val bearing = GeoUtils.calculateBearing(current.first, current.second, next.first, next.second)
                val simulatedSpeed = 36.0 + (stepIndex % 4) * 3.0

                updateTripLocation(
                    busId = busId,
                    latitude = current.first,
                    longitude = current.second,
                    speedKmh = simulatedSpeed,
                    bearing = bearing
                )

                stepIndex = (stepIndex + 1) % pathCoords.size
                val delayTime = (3000L / _simulationSpeedMultiplier.value.coerceAtLeast(1))
                delay(delayTime)
            }
        }
    }

    fun stopSimulation() {
        _isSimulating.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    fun triggerSimulatedDelay(busId: String, delayMinutes: Int = 6) {
        updateBusState(busId) {
            it.copy(
                delayMinutes = delayMinutes,
                status = if (delayMinutes > 0) BusStatus.DELAYED else BusStatus.IN_TRANSIT
            )
        }
        addNotification(
            title = "Delay Alert: $busId",
            message = "Bus schedule delayed by $delayMinutes mins due to traffic congestion near Cherakkuzhi.",
            type = NotificationType.BUS_DELAYED,
            busId = busId
        )
    }

    fun triggerSimulatedArrival(busId: String) {
        val bus = _buses.value.find { it.busId == busId } ?: return
        addNotification(
            title = "Arriving at CAS Campus Gate",
            message = "${bus.busNumber} is arriving at CAS Chelakkara Campus Gate right now.",
            type = NotificationType.BUS_ARRIVED,
            busId = busId
        )
        updateBusState(busId) {
            it.copy(nextStopEtaMinutes = 0, currentSpeedKmh = 10.0)
        }
    }

    // --- Admin CRUD Operations ---

    fun addBus(bus: Bus) {
        _buses.update { it + bus }
    }

    fun updateBus(bus: Bus) {
        _buses.update { list ->
            list.map { if (it.busId == bus.busId) bus else it }
        }
    }

    fun deactivateBus(busId: String) {
        _buses.update { list ->
            list.map { if (it.busId == busId) it.copy(active = false, status = BusStatus.OFFLINE) else it }
        }
    }

    fun addDriver(driver: Driver) {
        _drivers.update { it + driver }
    }

    fun updateDriver(driver: Driver) {
        _drivers.update { list ->
            list.map { if (it.driverId == driver.driverId) driver else it }
        }
    }

    fun deactivateDriver(driverId: String) {
        _drivers.update { list ->
            list.map { if (it.driverId == driverId) it.copy(active = false) else it }
        }
    }

    fun addRoute(route: BusRoute) {
        _routes.update { it + route }
    }

    fun updateRoute(route: BusRoute) {
        _routes.update { list ->
            list.map { if (it.routeId == route.routeId) route else it }
        }
    }

    fun addSchedule(schedule: Schedule) {
        _schedules.update { it + schedule }
    }

    fun updateSchedule(schedule: Schedule) {
        _schedules.update { list ->
            list.map { if (it.scheduleId == schedule.scheduleId) schedule else it }
        }
    }

    fun cancelSchedule(scheduleId: String) {
        _schedules.update { list ->
            list.map { if (it.scheduleId == scheduleId) it.copy(cancelled = true) else it }
        }
    }

    // --- Feedback ---

    fun submitFeedback(
        category: String,
        message: String,
        busId: String = "",
        routeId: String = ""
    ) {
        val user = _currentUser.value
        val item = FeedbackItem(
            id = "fb_${System.currentTimeMillis()}",
            userId = user?.userId ?: "guest",
            userName = user?.name ?: "Anonymous Student",
            userEmail = user?.email ?: "student@caschelakkara.edu.in",
            category = category,
            message = message,
            busId = busId,
            routeId = routeId,
            createdAt = System.currentTimeMillis()
        )
        _feedbackList.update { listOf(item) + it }
    }

    fun updateFeedbackStatus(feedbackId: String, status: String, reply: String = "") {
        _feedbackList.update { list ->
            list.map {
                if (it.id == feedbackId) it.copy(status = status, adminReply = reply) else it
            }
        }
    }

    // --- Favorites ---

    fun toggleFavoriteRoute(routeId: String) {
        _favoriteRoutes.update { current ->
            if (current.contains(routeId)) current - routeId else current + routeId
        }
    }

    fun toggleFavoriteBus(busId: String) {
        _favoriteBuses.update { current ->
            if (current.contains(busId)) current - busId else current + busId
        }
    }

    // --- Notifications ---

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        busId: String? = null,
        routeId: String? = null,
        tripId: String? = null
    ) {
        val item = NotificationItem(
            id = "notif_${System.currentTimeMillis()}",
            userId = "all",
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            read = false,
            busId = busId,
            routeId = routeId,
            tripId = tripId
        )
        _notifications.update { listOf(item) + it }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(read = true) else it }
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.update { list ->
            list.map { it.copy(read = true) }
        }
    }

    private fun updateBusState(busId: String, block: (Bus) -> Bus) {
        _buses.update { list ->
            list.map { if (it.busId == busId) block(it) else it }
        }
    }

    companion object {
        @Volatile
        private var instance: SmartBusRepository? = null

        fun getInstance(context: Context): SmartBusRepository {
            return instance ?: synchronized(this) {
                instance ?: SmartBusRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
