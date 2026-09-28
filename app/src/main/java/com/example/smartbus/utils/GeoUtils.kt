package com.example.smartbus.utils

import kotlin.math.*

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance in meters between two coordinates using the Haversine formula.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates bearing in degrees (0 to 360) from point 1 to point 2.
     */
    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x)

        val bearingDegrees = (Math.toDegrees(theta) + 360) % 360
        return bearingDegrees.toFloat()
    }

    /**
     * Checks if coordinates are within a stop's geofence.
     */
    fun isInsideGeofence(
        busLat: Double, busLon: Double,
        stopLat: Double, stopLon: Double,
        radiusMeters: Double
    ): Boolean {
        if (busLat == 0.0 || busLon == 0.0 || stopLat == 0.0 || stopLon == 0.0) return false
        val distance = calculateDistanceMeters(busLat, busLon, stopLat, stopLon)
        return distance <= radiusMeters
    }

    /**
     * Computes ETA in minutes for a given distance and speed, with fallback logic.
     */
    fun calculateEtaMinutes(
        distanceMeters: Double,
        currentSpeedKmh: Double,
        defaultSpeedKmh: Double = 30.0
    ): Int {
        if (distanceMeters <= 60.0) return 0 // Arriving

        val effectiveSpeedKmh = when {
            currentSpeedKmh >= 8.0 -> currentSpeedKmh
            else -> defaultSpeedKmh
        }

        val speedMps = (effectiveSpeedKmh * 1000.0) / 3600.0
        val seconds = distanceMeters / speedMps
        val minutes = ceil(seconds / 60.0).toInt()
        return max(1, minutes)
    }

    /**
     * Human-friendly formatted ETA string.
     */
    fun formatEta(etaMinutes: Int, delayMinutes: Int = 0): String {
        return when {
            etaMinutes <= 0 -> "Arriving now"
            etaMinutes == 1 -> "1 min away"
            delayMinutes > 0 -> "$etaMinutes min (Delayed +${delayMinutes}m)"
            else -> "$etaMinutes mins"
        }
    }
}
