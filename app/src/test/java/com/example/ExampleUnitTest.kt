package com.example

import com.example.smartbus.utils.GeoUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHaversineDistanceCalculation() {
        // Distance between Thiruvilwamala (10.6974, 76.4385) and Cherakkuzhi (10.7020, 76.3850)
        val distance = GeoUtils.calculateDistanceMeters(10.6974, 76.4385, 10.7020, 76.3850)
        assertTrue("Distance should be approximately 5.8 km", distance in 5000.0..6500.0)
    }

    @Test
    fun testGeofenceDetection() {
        val stopLat = 10.7055
        val stopLon = 76.3320
        val radius = 50.0 // 50m

        // Coordinates 20 meters away
        val insideLat = 10.7056
        val insideLon = 76.3320
        val isInside = GeoUtils.isInsideGeofence(insideLat, insideLon, stopLat, stopLon, radius)
        assertTrue("Bus should be detected inside geofence", isInside)

        // Coordinates 500 meters away
        val outsideLat = 10.7100
        val outsideLon = 76.3320
        val isOutside = GeoUtils.isInsideGeofence(outsideLat, outsideLon, stopLat, stopLon, radius)
        assertFalse("Bus should not be detected inside geofence", isOutside)
    }

    @Test
    fun testEtaCalculation() {
        // 3000 meters at 30 km/h = ~6 minutes
        val eta = GeoUtils.calculateEtaMinutes(distanceMeters = 3000.0, currentSpeedKmh = 30.0)
        assertEquals(6, eta)

        // Less than 60m = arriving (0 min)
        val arrivingEta = GeoUtils.calculateEtaMinutes(distanceMeters = 40.0, currentSpeedKmh = 30.0)
        assertEquals(0, arrivingEta)

        // Formatted strings
        assertEquals("Arriving now", GeoUtils.formatEta(0, 0))
        assertEquals("1 min away", GeoUtils.formatEta(1, 0))
        assertEquals("5 mins", GeoUtils.formatEta(5, 0))
        assertEquals("6 min (Delayed +4m)", GeoUtils.formatEta(6, 4))
    }
}
