package com.example.smartbus.data.model

enum class UserRole {
    PASSENGER,
    DRIVER,
    ADMIN
}

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.PASSENGER,
    val createdAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

enum class BusStatus {
    ACTIVE,
    IN_TRANSIT,
    DELAYED,
    OFFLINE,
    MAINTENANCE
}

data class Bus(
    val busId: String = "",
    val busNumber: String = "",
    val registrationNumber: String = "",
    val capacity: Int = 45,
    val driverId: String = "",
    val driverName: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val status: BusStatus = BusStatus.OFFLINE,
    val active: Boolean = true,
    val currentSpeedKmh: Double = 0.0,
    val bearing: Float = 0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val nextStopName: String = "",
    val nextStopEtaMinutes: Int = 0,
    val delayMinutes: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val currentTripId: String = ""
)

data class Driver(
    val driverId: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val employeeId: String = "",
    val assignedBusId: String = "",
    val assignedBusNumber: String = "",
    val assignedRouteId: String = "",
    val assignedRouteName: String = "",
    val active: Boolean = true,
    val punctualityScore: Int = 94, // Percentage
    val completedTrips: Int = 120
)

data class RouteStop(
    val stopId: String = "",
    val stopName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val sequence: Int = 0,
    val geofenceRadiusMeters: Double = 50.0,
    val scheduledOffsetMinutes: Int = 0 // minutes from route origin
)

data class BusRoute(
    val routeId: String = "",
    val routeName: String = "",
    val source: String = "",
    val destination: String = "",
    val direction: String = "FORWARD", // FORWARD or RETURN
    val active: Boolean = true,
    val distanceKm: Double = 0.0,
    val estimatedDurationMinutes: Int = 0,
    val stops: List<RouteStop> = emptyList()
)

data class StopScheduleTime(
    val stopId: String = "",
    val stopName: String = "",
    val scheduledTime: String = "", // e.g. "08:15 AM"
    val actualArrivalTime: String? = null,
    val actualDepartureTime: String? = null,
    val sequence: Int = 0,
    val delayMinutes: Int = 0
)

data class Schedule(
    val scheduleId: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val busId: String = "",
    val busNumber: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val operatingDays: String = "Mon - Fri",
    val departureTime: String = "08:00 AM",
    val stopTimes: List<StopScheduleTime> = emptyList(),
    val active: Boolean = true,
    val cancelled: Boolean = false
)

enum class TripStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class Trip(
    val tripId: String = "",
    val busId: String = "",
    val busNumber: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val routeId: String = "",
    val routeName: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val status: TripStatus = TripStatus.NOT_STARTED,
    val currentStopIndex: Int = 0,
    val delayMinutes: Int = 0,
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentSpeedKmh: Double = 0.0,
    val gpsActive: Boolean = false,
    val lastUpdateTimestamp: Long = System.currentTimeMillis()
)

data class BusLocationUpdate(
    val locationId: String = "",
    val tripId: String = "",
    val busId: String = "",
    val driverId: String = "",
    val routeId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speedKmh: Double = 0.0,
    val bearing: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    BUS_APPROACHING,
    BUS_DELAYED,
    BUS_ARRIVED,
    ROUTE_CHANGED,
    SCHEDULE_CHANGED,
    TRIP_STARTED,
    TRIP_CANCELLED,
    EMERGENCY_ALERT,
    ADMIN_ANNOUNCEMENT
}

data class NotificationItem(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.BUS_APPROACHING,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val busId: String? = null,
    val routeId: String? = null,
    val tripId: String? = null
)

data class FeedbackItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val category: String = "General",
    val message: String = "",
    val busId: String = "",
    val routeId: String = "",
    val tripId: String = "",
    val status: String = "PENDING", // PENDING, REVIEWED, RESOLVED
    val adminReply: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverScorecard(
    val driverId: String = "",
    val driverName: String = "",
    val onTimePercentage: Int = 0,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val averageDelayMinutes: Double = 0.0,
    val gpsReliabilityPercentage: Int = 98,
    val rating: Double = 4.8
)

data class DelayHotspot(
    val stopId: String = "",
    val stopName: String = "",
    val routeName: String = "",
    val delayIncidentCount: Int = 0,
    val averageDelayMinutes: Double = 0.0
)
