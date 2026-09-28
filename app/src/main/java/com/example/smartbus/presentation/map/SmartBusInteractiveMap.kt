package com.example.smartbus.presentation.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartbus.data.model.Bus
import com.example.smartbus.data.model.BusRoute
import com.example.smartbus.data.model.BusStatus
import com.example.smartbus.data.model.RouteStop
import com.example.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun SmartBusInteractiveMap(
    buses: List<Bus>,
    routes: List<BusRoute>,
    selectedBus: Bus?,
    selectedRoute: BusRoute?,
    onBusSelected: (Bus) -> Unit,
    onStopSelected: (RouteStop) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = Pair(10.7020, 76.3850), // CAS Chelakkara area
    isDemoMode: Boolean = true
) {
    // Map bounds around Chelakkara / Thiruvilwamala / Pazhayannur
    // Center approx: 10.695, 76.380
    var zoomLevel by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val isDark = MaterialTheme.colorScheme.background == SmartBusBackgroundDark
    val textMeasurer = rememberTextMeasurer()

    // Base coordinate reference for projection
    val baseLat = 10.6950
    val baseLon = 76.3800
    val baseScale = 45000f // Scaling factor for Kerala corridor coordinates

    fun geoToScreen(lat: Double, lon: Double, center: Offset): Offset {
        val dx = ((lon - baseLon) * baseScale * zoomLevel).toFloat()
        val dy = (-(lat - baseLat) * baseScale * zoomLevel).toFloat()
        return Offset(center.x + dx + panOffset.x, center.y + dy + panOffset.y)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (MaterialTheme.colorScheme.background == SmartBusBackgroundDark) Color(0xFF101726) else Color(0xFFE9F0F8))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    panOffset += pan
                    zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 4.0f)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(buses, routes, zoomLevel, panOffset) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        // Check if tap hit a bus marker
                        var tappedBus: Bus? = null
                        for (bus in buses) {
                            if (bus.latitude != 0.0 && bus.longitude != 0.0) {
                                val busPos = geoToScreen(bus.latitude, bus.longitude, center)
                                val distance = (busPos - tapOffset).getDistance()
                                if (distance <= 45f) {
                                    tappedBus = bus
                                    break
                                }
                            }
                        }
                        if (tappedBus != null) {
                            onBusSelected(tappedBus)
                            return@detectTapGestures
                        }

                        // Check if tap hit a stop
                        val activeStops = selectedRoute?.stops ?: routes.flatMap { it.stops }
                        for (stop in activeStops) {
                            val stopPos = geoToScreen(stop.latitude, stop.longitude, center)
                            val distance = (stopPos - tapOffset).getDistance()
                            if (distance <= 35f) {
                                onStopSelected(stop)
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // 1. Draw subtle transit grid / geography background
            drawTransitGrid(size)

            // 2. Draw Route Polylines
            val routesToDraw = if (selectedRoute != null) listOf(selectedRoute) else routes
            for (route in routesToDraw) {
                val isFocused = selectedRoute == null || selectedRoute.routeId == route.routeId
                val pathColor = if (isFocused) SmartBusPrimary.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.4f)
                val strokeWidth = if (isFocused) 6.dp.toPx() else 3.dp.toPx()

                if (route.stops.size >= 2) {
                    val path = Path()
                    val firstPoint = geoToScreen(route.stops[0].latitude, route.stops[0].longitude, center)
                    path.moveTo(firstPoint.x, firstPoint.y)

                    for (i in 1 until route.stops.size) {
                        val pt = geoToScreen(route.stops[i].latitude, route.stops[i].longitude, center)
                        path.lineTo(pt.x, pt.y)
                    }

                    // Glow effect
                    drawPath(
                        path = path,
                        color = pathColor.copy(alpha = 0.25f),
                        style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    // Core polyline
                    drawPath(
                        path = path,
                        color = pathColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            // 3. Draw Bus Stops
            val displayedStops = selectedRoute?.stops ?: routes.flatMap { it.stops }.distinctBy { it.stopId }
            for (stop in displayedStops) {
                val pos = geoToScreen(stop.latitude, stop.longitude, center)

                // Geofence radius circle (scaled in pixels)
                val geofencePixels = (stop.geofenceRadiusMeters / 1000.0 * baseScale * zoomLevel * 0.008f).toFloat().coerceIn(12f, 40f)
                drawCircle(
                    color = SmartBusSecondary.copy(alpha = 0.18f),
                    radius = geofencePixels,
                    center = pos
                )
                drawCircle(
                    color = SmartBusSecondary.copy(alpha = 0.45f),
                    radius = geofencePixels,
                    center = pos,
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )

                // Stop node marker
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pos)
                drawCircle(color = SmartBusSecondary, radius = 4.5.dp.toPx(), center = pos)

                // Stop Label (show when zoomed in enough or selected)
                if (zoomLevel >= 0.8f) {
                    val stopText = textMeasurer.measure(
                        text = stop.stopName,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    )
                    drawText(
                        textLayoutResult = stopText,
                        topLeft = Offset(pos.x + 8.dp.toPx(), pos.y - 12.dp.toPx())
                    )
                }
            }

            // 4. Draw User Location Dot
            userLocation?.let { (uLat, uLon) ->
                val uPos = geoToScreen(uLat, uLon, center)
                drawCircle(
                    color = SmartBusPrimary.copy(alpha = 0.22f),
                    radius = 18.dp.toPx(),
                    center = uPos
                )
                drawCircle(color = Color.White, radius = 7.dp.toPx(), center = uPos)
                drawCircle(color = SmartBusPrimary, radius = 5.dp.toPx(), center = uPos)
            }

            // 5. Draw Buses
            for (bus in buses) {
                if (bus.latitude == 0.0 && bus.longitude == 0.0) continue
                val busPos = geoToScreen(bus.latitude, bus.longitude, center)
                val isSelected = selectedBus?.busId == bus.busId

                val statusColor = when (bus.status) {
                    BusStatus.ACTIVE, BusStatus.IN_TRANSIT -> StatusOnTime
                    BusStatus.DELAYED -> StatusDelayed
                    BusStatus.OFFLINE, BusStatus.MAINTENANCE -> StatusOffline
                }

                // Pulse aura
                drawCircle(
                    color = statusColor.copy(alpha = if (isSelected) 0.35f else 0.2f),
                    radius = if (isSelected) 24.dp.toPx() else 18.dp.toPx(),
                    center = busPos
                )

                // Outer border
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 14.dp.toPx() else 11.dp.toPx(),
                    center = busPos
                )

                // Bus core badge
                drawCircle(
                    color = statusColor,
                    radius = if (isSelected) 11.dp.toPx() else 9.dp.toPx(),
                    center = busPos
                )

                // Bearing direction triangle pointer
                if (bus.bearing != 0f) {
                    val angleRad = Math.toRadians((bus.bearing - 90).toDouble())
                    val pointerDist = if (isSelected) 18.dp.toPx() else 14.dp.toPx()
                    val pX = (busPos.x + pointerDist * cos(angleRad)).toFloat()
                    val pY = (busPos.y + pointerDist * sin(angleRad)).toFloat()
                    drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = Offset(pX, pY))
                    drawCircle(color = statusColor, radius = 2.5.dp.toPx(), center = Offset(pX, pY))
                }

                // Bus Name Badge floating above marker
                val labelText = "${bus.busNumber.take(6)} • ${bus.currentSpeedKmh.toInt()} km/h"
                val measuredLabel = textMeasurer.measure(
                    text = labelText,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                val labelW = measuredLabel.size.width + 16f
                val labelH = measuredLabel.size.height + 8f
                val labelTopLeft = Offset(busPos.x - labelW / 2f, busPos.y - 28.dp.toPx())

                drawRoundRect(
                    color = Color(0xCC0B1E36),
                    topLeft = labelTopLeft,
                    size = androidx.compose.ui.geometry.Size(labelW, labelH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
                drawText(
                    textLayoutResult = measuredLabel,
                    topLeft = Offset(labelTopLeft.x + 8f, labelTopLeft.y + 4f)
                )
            }
        }

        // Live Demo Watermark / GPS Mode Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDemoMode) Color(0xDD0B2240) else Color(0xDD1B5E20))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDemoMode) Icons.Default.Sensors else Icons.Default.GpsFixed,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isDemoMode) "DEMO GPS MODE (CAS CHELAKKARA)" else "LIVE SATELLITE GPS ACTIVE",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Map Control Floating Buttons (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(4.0f) },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }

            FloatingActionButton(
                onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.5f) },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }

            FloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomLevel = 1.0f
                },
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map")
            }
        }
    }
}

private fun DrawScope.drawTransitGrid(size: androidx.compose.ui.geometry.Size) {
    val step = 80f
    val lineColor = Color.Gray.copy(alpha = 0.08f)
    var x = 0f
    while (x < size.width) {
        drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += step
    }
    var y = 0f
    while (y < size.height) {
        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += step
    }
}

