package com.example.smartbus.data.sample

import com.example.smartbus.data.model.*

object SampleDataProvider {

    // 15+ Realistic Bus Stops
    val STOPS = listOf(
        RouteStop("stop_1", "Thiruvilwamala Bus Stand", 10.6974, 76.4385, 1, 60.0, 0),
        RouteStop("stop_2", "Ivor Madom Road", 10.6990, 76.4250, 2, 50.0, 4),
        RouteStop("stop_3", "Pampady Junction", 10.7010, 76.4100, 3, 50.0, 8),
        RouteStop("stop_4", "Kaniarkode Center", 10.7015, 76.3950, 4, 50.0, 13),
        RouteStop("stop_5", "Cherakkuzhi Center", 10.7020, 76.3850, 5, 55.0, 18),
        RouteStop("stop_6", "Cherakkuzhi HS Jn", 10.7018, 76.3720, 6, 50.0, 22),
        RouteStop("stop_7", "Pulakkode Bridge", 10.7014, 76.3580, 7, 50.0, 27),
        RouteStop("stop_8", "Chelakkara Bus Stand", 10.7012, 76.3450, 8, 70.0, 33),
        RouteStop("stop_9", "Panchayat Office Jn", 10.7030, 76.3380, 9, 50.0, 36),
        RouteStop("stop_10", "CAS Chelakkara Campus Gate", 10.7055, 76.3320, 10, 60.0, 40),

        // Stops for Route 3 and intermediate
        RouteStop("stop_11", "Pazhayannur Temple Jn", 10.6650, 76.4800, 1, 50.0, 0),
        RouteStop("stop_12", "Vadakkethara Jn", 10.6800, 76.4200, 2, 50.0, 12),
        RouteStop("stop_13", "Venganellur", 10.7080, 76.3250, 3, 50.0, 25),
        RouteStop("stop_14", "Attur Junction", 10.7150, 76.3050, 4, 50.0, 32),
        RouteStop("stop_15", "Wadakkanchery Station", 10.6630, 76.2480, 5, 60.0, 45)
    )

    // Route 1: Thiruvilwamala -> Cherakkuzhi -> Chelakkara (CAS Chelakkara)
    val ROUTE_1 = BusRoute(
        routeId = "route_1",
        routeName = "Thiruvilwamala – Cherakkuzhi – CAS Chelakkara",
        source = "Thiruvilwamala",
        destination = "CAS Chelakkara Campus",
        direction = "FORWARD",
        active = true,
        distanceKm = 14.8,
        estimatedDurationMinutes = 40,
        stops = STOPS.subList(0, 10)
    )

    // Route 2: Chelakkara -> Cherakkuzhi -> Thiruvilwamala (Return)
    val ROUTE_2 = BusRoute(
        routeId = "route_2",
        routeName = "CAS Chelakkara – Cherakkuzhi – Thiruvilwamala",
        source = "CAS Chelakkara Campus",
        destination = "Thiruvilwamala",
        direction = "RETURN",
        active = true,
        distanceKm = 14.8,
        estimatedDurationMinutes = 40,
        stops = STOPS.subList(0, 10).reversed().mapIndexed { index, stop ->
            stop.copy(
                stopId = "${stop.stopId}_rev",
                sequence = index + 1,
                scheduledOffsetMinutes = index * 4
            )
        }
    )

    // Route 3: Pazhayannur -> Chelakkara -> Wadakkanchery
    val ROUTE_3 = BusRoute(
        routeId = "route_3",
        routeName = "Pazhayannur – Chelakkara – Wadakkanchery",
        source = "Pazhayannur",
        destination = "Wadakkanchery Station",
        direction = "FORWARD",
        active = true,
        distanceKm = 24.2,
        estimatedDurationMinutes = 55,
        stops = listOf(
            STOPS[10], // Pazhayannur Temple Jn
            STOPS[11], // Vadakkethara Jn
            STOPS[7],  // Chelakkara Bus Stand
            STOPS[9],  // CAS Chelakkara Campus Gate
            STOPS[12], // Venganellur
            STOPS[13], // Attur Junction
            STOPS[14]  // Wadakkanchery Station
        ).mapIndexed { index, stop ->
            stop.copy(sequence = index + 1, scheduledOffsetMinutes = index * 8)
        }
    )

    val ROUTES = listOf(ROUTE_1, ROUTE_2, ROUTE_3)

    // Initial Drivers
    val DRIVERS = listOf(
        Driver("drv_01", "usr_drv_01", "Suresh Kumar", "+91 94471 23450", "suresh.smartbus@gmail.com", "DRV-101", "bus_01", "BUS-01 (CAS Express)", "route_1", "Thiruvilwamala – CAS Chelakkara", true, 96, 142),
        Driver("drv_02", "usr_drv_02", "Manoj Varghese", "+91 98462 87651", "manoj.smartbus@gmail.com", "DRV-102", "bus_02", "BUS-02 (Campus Line)", "route_2", "CAS Chelakkara – Thiruvilwamala", true, 92, 118),
        Driver("drv_03", "usr_drv_03", "Radhakrishnan P.", "+91 97453 54321", "radha.smartbus@gmail.com", "DRV-103", "bus_03", "BUS-03 (Pazhayannur Flyer)", "route_3", "Pazhayannur – Wadakkanchery", true, 95, 135),
        Driver("drv_04", "usr_drv_04", "Prasanth Nair", "+91 94460 99881", "prasanth.smartbus@gmail.com", "DRV-104", "bus_04", "BUS-04 (Chelakkara City)", "route_1", "Thiruvilwamala – CAS Chelakkara", true, 89, 98),
        Driver("drv_05", "usr_drv_05", "Anil Das", "+91 95678 11223", "anil.smartbus@gmail.com", "DRV-105", "bus_05", "BUS-05 (Reserve Special)", "", "Unassigned", true, 94, 75)
    )

    // Initial Buses
    val BUSES = listOf(
        Bus(
            busId = "bus_01",
            busNumber = "BUS-01 (CAS Express)",
            registrationNumber = "KL-48-B-1204",
            capacity = 48,
            driverId = "drv_01",
            driverName = "Suresh Kumar",
            routeId = "route_1",
            routeName = "Thiruvilwamala – Cherakkuzhi – CAS Chelakkara",
            status = BusStatus.IN_TRANSIT,
            active = true,
            currentSpeedKmh = 38.5,
            bearing = 265f,
            latitude = 10.7018,
            longitude = 10.7018,
            nextStopName = "Cherakkuzhi Center",
            nextStopEtaMinutes = 3,
            delayMinutes = 0,
            lastUpdated = System.currentTimeMillis() - 25000,
            currentTripId = "trip_demo_1"
        ),
        Bus(
            busId = "bus_02",
            busNumber = "BUS-02 (Campus Line)",
            registrationNumber = "KL-48-C-3419",
            capacity = 42,
            driverId = "drv_02",
            driverName = "Manoj Varghese",
            routeId = "route_2",
            routeName = "CAS Chelakkara – Cherakkuzhi – Thiruvilwamala",
            status = BusStatus.IN_TRANSIT,
            active = true,
            currentSpeedKmh = 32.0,
            bearing = 85f,
            latitude = 10.7014,
            longitude = 76.3580,
            nextStopName = "Cherakkuzhi HS Jn",
            nextStopEtaMinutes = 4,
            delayMinutes = 4, // 4 mins delay
            lastUpdated = System.currentTimeMillis() - 15000,
            currentTripId = "trip_demo_2"
        ),
        Bus(
            busId = "bus_03",
            busNumber = "BUS-03 (Pazhayannur Flyer)",
            registrationNumber = "KL-48-A-5690",
            capacity = 50,
            driverId = "drv_03",
            driverName = "Radhakrishnan P.",
            routeId = "route_3",
            routeName = "Pazhayannur – Chelakkara – Wadakkanchery",
            status = BusStatus.ACTIVE,
            active = true,
            currentSpeedKmh = 0.0,
            bearing = 0f,
            latitude = 10.6650,
            longitude = 76.4800,
            nextStopName = "Vadakkethara Jn",
            nextStopEtaMinutes = 12,
            delayMinutes = 0,
            lastUpdated = System.currentTimeMillis() - 60000,
            currentTripId = ""
        ),
        Bus(
            busId = "bus_04",
            busNumber = "BUS-04 (Chelakkara Shuttle)",
            registrationNumber = "KL-48-D-8821",
            capacity = 36,
            driverId = "drv_04",
            driverName = "Prasanth Nair",
            routeId = "route_1",
            routeName = "Thiruvilwamala – Cherakkuzhi – CAS Chelakkara",
            status = BusStatus.OFFLINE,
            active = true,
            currentSpeedKmh = 0.0,
            bearing = 0f,
            latitude = 10.6974,
            longitude = 76.4385,
            nextStopName = "Not Started",
            nextStopEtaMinutes = 0,
            delayMinutes = 0,
            lastUpdated = System.currentTimeMillis() - 3600000,
            currentTripId = ""
        ),
        Bus(
            busId = "bus_05",
            busNumber = "BUS-05 (Reserve Fleet)",
            registrationNumber = "KL-48-E-9012",
            capacity = 45,
            driverId = "",
            driverName = "Unassigned",
            routeId = "",
            routeName = "Depot Standby",
            status = BusStatus.OFFLINE,
            active = true,
            currentSpeedKmh = 0.0,
            bearing = 0f,
            latitude = 10.7012,
            longitude = 76.3450,
            nextStopName = "Depot",
            nextStopEtaMinutes = 0,
            delayMinutes = 0,
            lastUpdated = System.currentTimeMillis() - 7200000,
            currentTripId = ""
        )
    )

    // Predefined Schedules
    val SCHEDULES = listOf(
        Schedule(
            scheduleId = "sch_01",
            routeId = "route_1",
            routeName = "Thiruvilwamala – Cherakkuzhi – CAS Chelakkara",
            busId = "bus_01",
            busNumber = "BUS-01 (CAS Express)",
            driverId = "drv_01",
            driverName = "Suresh Kumar",
            operatingDays = "Mon - Fri",
            departureTime = "08:15 AM",
            active = true,
            stopTimes = listOf(
                StopScheduleTime("stop_1", "Thiruvilwamala Bus Stand", "08:15 AM", "08:15 AM", "08:16 AM", 1, 0),
                StopScheduleTime("stop_2", "Ivor Madom Road", "08:19 AM", "08:20 AM", "08:20 AM", 2, 1),
                StopScheduleTime("stop_3", "Pampady Junction", "08:23 AM", "08:24 AM", "08:25 AM", 3, 1),
                StopScheduleTime("stop_4", "Kaniarkode Center", "08:28 AM", null, null, 4, 0),
                StopScheduleTime("stop_5", "Cherakkuzhi Center", "08:33 AM", null, null, 5, 0),
                StopScheduleTime("stop_6", "Cherakkuzhi HS Jn", "08:37 AM", null, null, 6, 0),
                StopScheduleTime("stop_7", "Pulakkode Bridge", "08:42 AM", null, null, 7, 0),
                StopScheduleTime("stop_8", "Chelakkara Bus Stand", "08:48 AM", null, null, 8, 0),
                StopScheduleTime("stop_9", "Panchayat Office Jn", "08:51 AM", null, null, 9, 0),
                StopScheduleTime("stop_10", "CAS Chelakkara Campus Gate", "08:55 AM", null, null, 10, 0)
            )
        ),
        Schedule(
            scheduleId = "sch_02",
            routeId = "route_2",
            routeName = "CAS Chelakkara – Cherakkuzhi – Thiruvilwamala",
            busId = "bus_02",
            busNumber = "BUS-02 (Campus Line)",
            driverId = "drv_02",
            driverName = "Manoj Varghese",
            operatingDays = "Mon - Fri",
            departureTime = "04:10 PM",
            active = true,
            stopTimes = listOf(
                StopScheduleTime("stop_10", "CAS Chelakkara Campus Gate", "04:10 PM", null, null, 1, 0),
                StopScheduleTime("stop_9", "Panchayat Office Jn", "04:14 PM", null, null, 2, 0),
                StopScheduleTime("stop_8", "Chelakkara Bus Stand", "04:18 PM", null, null, 3, 0),
                StopScheduleTime("stop_7", "Pulakkode Bridge", "04:24 PM", null, null, 4, 0),
                StopScheduleTime("stop_6", "Cherakkuzhi HS Jn", "04:29 PM", null, null, 5, 0),
                StopScheduleTime("stop_5", "Cherakkuzhi Center", "04:33 PM", null, null, 6, 0),
                StopScheduleTime("stop_4", "Kaniarkode Center", "04:38 PM", null, null, 7, 0),
                StopScheduleTime("stop_3", "Pampady Junction", "04:43 PM", null, null, 8, 0),
                StopScheduleTime("stop_2", "Ivor Madom Road", "04:47 PM", null, null, 9, 0),
                StopScheduleTime("stop_1", "Thiruvilwamala Bus Stand", "04:50 PM", null, null, 10, 0)
            )
        ),
        Schedule(
            scheduleId = "sch_03",
            routeId = "route_3",
            routeName = "Pazhayannur – Chelakkara – Wadakkanchery",
            busId = "bus_03",
            busNumber = "BUS-03 (Pazhayannur Flyer)",
            driverId = "drv_03",
            driverName = "Radhakrishnan P.",
            operatingDays = "Mon - Sat",
            departureTime = "09:00 AM",
            active = true,
            stopTimes = listOf(
                StopScheduleTime("stop_11", "Pazhayannur Temple Jn", "09:00 AM", null, null, 1, 0),
                StopScheduleTime("stop_12", "Vadakkethara Jn", "09:12 AM", null, null, 2, 0),
                StopScheduleTime("stop_8", "Chelakkara Bus Stand", "09:25 AM", null, null, 3, 0),
                StopScheduleTime("stop_10", "CAS Chelakkara Campus Gate", "09:33 AM", null, null, 4, 0),
                StopScheduleTime("stop_13", "Venganellur", "09:41 AM", null, null, 5, 0),
                StopScheduleTime("stop_14", "Attur Junction", "09:48 AM", null, null, 6, 0),
                StopScheduleTime("stop_15", "Wadakkanchery Station", "09:55 AM", null, null, 7, 0)
            )
        )
    )

    val INITIAL_NOTIFICATIONS = listOf(
        NotificationItem(
            id = "notif_1",
            userId = "all",
            title = "BUS-01 Approaching Cherakkuzhi",
            message = "CAS Express is approx. 3 minutes away from Cherakkuzhi Center. On-time schedule.",
            type = NotificationType.BUS_APPROACHING,
            timestamp = System.currentTimeMillis() - 120000,
            read = false,
            busId = "bus_01",
            routeId = "route_1"
        ),
        NotificationItem(
            id = "notif_2",
            userId = "all",
            title = "Trip Started: BUS-02",
            message = "Manoj Varghese has started trip from Chelakkara to Thiruvilwamala.",
            type = NotificationType.TRIP_STARTED,
            timestamp = System.currentTimeMillis() - 900000,
            read = true,
            busId = "bus_02",
            routeId = "route_2"
        ),
        NotificationItem(
            id = "notif_3",
            userId = "all",
            title = "Exam Special Timetable Active",
            message = "Special university exam timings are in effect today until 5:00 PM for CAS Chelakkara students.",
            type = NotificationType.ADMIN_ANNOUNCEMENT,
            timestamp = System.currentTimeMillis() - 7200000,
            read = true
        )
    )

    val INITIAL_FEEDBACK = listOf(
        FeedbackItem(
            id = "fb_1",
            userId = "usr_demo",
            userName = "Ananya Nair",
            userEmail = "ananya.cas@gmail.com",
            category = "Schedule Issue",
            message = "Morning 8:15 AM bus from Thiruvilwamala was crowded near Pampady. Could we get an extra shuttle?",
            busId = "bus_01",
            routeId = "route_1",
            tripId = "trip_demo_1",
            status = "REVIEWED",
            adminReply = "Reviewed by Transport Committee. We are monitoring passenger counts.",
            createdAt = System.currentTimeMillis() - 86400000
        )
    )
}
