# SmartBus – Smart Bus Time Management System Using GPS
### Academic Transit & Fleet Adherence System for College of Applied Sciences (CAS), Chelakkara

SmartBus is a production-grade, native Android application engineered to solve bus schedule unpredictability, long student waiting times, lack of real-time transit visibility, and fleet monitoring challenges for the academic community of **College of Applied Sciences (CAS), Chelakkara**.

---

## 🚀 Key Features

### 🎓 1. Passenger & Student Portal
- **Nearest Campus Stop Highlight**: Displays the closest stop (e.g., *CAS Chelakkara Campus Gate*) and live countdown to the next approaching bus.
- **Interactive Live Map**: Real-time 2D Mercator geographic projection of bus locations, moving along actual route coordinates with bearing pointers, speed gauges, and stop geofences.
- **Dynamic ETA Engine**: Computes realistic arrival times for upcoming stops using live vehicle speed and distance with intelligent fallback logic (*"Arriving now"*, *"4 mins"*, *"Delayed by 5 min"*).
- **Route & Stop Progression**: Ordered timeline view of all stops along the corridor with passed, current, and upcoming indicators.
- **Global Search**: Search by bus number (*BUS-01*), route name, or key stops (*Cherakkuzhi*, *Thiruvilwamala*, *Pampady*, *Pazhayannur*).
- **Timetable Explorer**: 8:00 AM to 5:00 PM college transit schedule with departure times, stop arrivals, and live delay markers.
- **Grievance & Feedback System**: Submit feedback across 6 categories (*General, Bus, Driver, Route, GPS, Schedule*) and track administrative responses.
- **Custom Alert Subscriptions**: Set arrival alerts for favorite buses and receive immediate notifications.

### 🚍 2. Driver Operations Console
- **Duty Assignment View**: View assigned bus, route, and target schedule timings.
- **One-Tap Trip Controller**:
  - `START TRIP & SHARE GPS`: Requests runtime location permissions or initializes live tracking.
  - `END TRIP`: Records final arrival and saves the run to history.
  - `REPORT DELAY`: Manually or automatically broadcast traffic congestion delays to waiting students.
- **Live GPS Telemetry**: Real-time speedometer (km/h), bearing, and connection indicator.
- **Geofence Detection**: Automatically detects when the bus enters within 50 meters of a stop, triggers arrival notifications, and advances the route progression index.

### 🛡️ 3. Admin Command Center
- **Fleet Metrics Overview**: Total buses, active fleet, on-duty drivers, punctuality score (%), and average delay.
- **Live Fleet Map Monitor**: Bird's-eye view of all buses concurrently in transit.
- **Bus Fleet CRUD**: Add new buses, update capacity, reassign drivers/routes, or safely deactivate buses.
- **Driver Management**: Register drivers, assign employee IDs, and monitor punctuality scorecards.
- **Route & Geofence Editor**: Configure routes, ordered stop sequences, and adjust geofence radii (default: 50m).
- **Schedule Management**: Manage daily departures, cancel runs during emergencies, and broadcast cancellation alerts.
- **Punctuality & Bottleneck Reports**: Identify delay hotspots (*Cherakkuzhi Center, Pampady Junction*) and evaluate driver scorecards.
- **Student Grievance Moderation**: Review student issues, update status to `REVIEWED` or `RESOLVED`, and dispatch official replies.

---

## 🛠️ Architecture & Tech Stack

- **Platform**: Native Android (Kotlin)
- **UI Framework**: Jetpack Compose & Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) with Repository Pattern
- **Reactive State**: Kotlin Coroutines & `StateFlow`
- **Navigation**: Jetpack Navigation Compose with role-based routing
- **Location & Sensors**: Android Fused Location Provider (`play-services-location`)
- **Backend Ready**: Firebase Auth & Firebase Firestore client integration with safe local cache fallback
- **Interactive Mapping**: Custom hardware-accelerated Vector & Coordinate Canvas projection engine with pan, zoom, bearing rotation, and gesture hit testing

---

## 🧭 Initial Route Corridors

1. **Route 1**: Thiruvilwamala → Pampady → Cherakkuzhi → Chelakkara Bus Stand → CAS Chelakkara Campus Gate (14.8 km, 40 mins)
2. **Route 2**: CAS Chelakkara Campus Gate → Cherakkuzhi → Thiruvilwamala (Return Line, 14.8 km)
3. **Route 3**: Pazhayannur Temple Jn → Vadakkethara → Chelakkara → CAS Campus → Wadakkanchery Station (24.2 km, 55 mins)

---

## 🎮 Presentation Demo Mode

For academic evaluations, viva presentations, or testing without driving a real bus:
1. Tap the **Portal Switcher** in the Profile or Login screen to seamlessly jump between **Passenger**, **Driver**, and **Admin** portals.
2. In the **Live Map Screen**, open the bottom simulation panel:
   - Tap **Play / Pause** to start the bus moving smoothly along route GPS waypoints.
   - Adjust simulation speed: `1x`, `2x`, or `5x`.
   - Tap `+5m Delay` to inject simulated traffic delays and view the UI react dynamically.
   - Tap `Arrival Alert` to simulate arriving at the CAS Chelakkara campus gate and test notification broadcasts.

---

## 🔑 Quick Demo Login Credentials

- **Student / Passenger**:
  - Email: `student@caschelakkara.edu.in`
  - Password: `password123`
- **Bus Driver**:
  - Email: `suresh.driver@caschelakkara.edu.in`
  - Password: `password123`
- **Admin / Transport Officer**:
  - Email: `admin@caschelakkara.edu.in`
  - Password: `password123`

*(You can also register any new account or click the role chips on the login screen to autofill)*

---

## 📋 Build & Run Instructions

```bash
# Clean compilation
gradle assembleDebug

# Run Unit Tests
gradle :app:testDebugUnitTest
```
