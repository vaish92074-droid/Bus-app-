package com.example.smartbus.presentation.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.smartbus.data.model.UserRole
import com.example.smartbus.data.repository.SmartBusRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndSettingsScreen(
    repository: SmartBusRepository,
    onLogout: () -> Unit,
    onRoleChanged: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val isDemoMode by repository.isDemoMode.collectAsState()
    val isSimulating by repository.isSimulating.collectAsState()
    val simSpeed by repository.simulationSpeedMultiplier.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentUser?.role) {
                                UserRole.DRIVER -> Icons.Default.DirectionsCar
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                else -> Icons.Default.Person
                            },
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.name ?: "CAS Chelakkara Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.email ?: "student@caschelakkara.edu.in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AssistChip(
                        onClick = {},
                        label = { Text("Role: ${currentUser?.role?.name ?: "PASSENGER"}") },
                        leadingIcon = { Icon(Icons.Default.VerifiedUser, null, Modifier.size(14.dp)) }
                    )
                }
            }

            // Quick Role Switcher for Presentation
            Text(
                text = "Presentation Switcher",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Switch Active Portal for Demonstration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                repository.login("student@caschelakkara.edu.in", UserRole.PASSENGER, "CAS Student")
                                onRoleChanged()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Passenger", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                repository.login("suresh.driver@caschelakkara.edu.in", UserRole.DRIVER, "Suresh Kumar")
                                onRoleChanged()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Driver", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                repository.login("admin@caschelakkara.edu.in", UserRole.ADMIN, "Admin Officer")
                                onRoleChanged()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Admin", fontSize = 11.sp)
                        }
                    }
                }
            }

            // System & Simulation Controls
            Text(
                text = "GPS & System Preferences",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Academic Demo Mode", fontWeight = FontWeight.SemiBold)
                            Text("Simulates GPS coordinates & vehicle trajectory", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isDemoMode,
                            onCheckedChange = { repository.toggleDemoMode(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Simulation Speed", fontWeight = FontWeight.SemiBold)
                            Text("Currently set to ${simSpeed}x normal time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilledTonalButton(
                            onClick = {
                                val next = if (simSpeed == 1) 2 else if (simSpeed == 2) 5 else 1
                                repository.setSimulationSpeed(next)
                            }
                        ) {
                            Text("${simSpeed}x")
                        }
                    }
                }
            }

            // Information & Actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("About SmartBus") },
                        supportingContent = { Text("CAS Chelakkara GPS Transit Management v1.0") },
                        leadingContent = { Icon(Icons.Default.Info, null) },
                        modifier = Modifier.clickable { showAboutDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    ListItem(
                        headlineContent = { Text("Sign Out") },
                        supportingContent = { Text("End current session") },
                        leadingContent = { Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About SmartBus") },
            text = {
                Text(
                    "SmartBus is an intelligent GPS-based bus timing, geofencing, and fleet tracking system engineered for the College of Applied Sciences (CAS), Chelakkara.\n\n" +
                    "Features:\n" +
                    "• Real-time bus tracking and bearing calculation\n" +
                    "• Dynamic stop arrival estimation (ETA)\n" +
                    "• Stop geofencing (~50m radius) with automatic arrival logs\n" +
                    "• Student, Driver, and Admin management portals\n" +
                    "• Full academic demonstration engine with multi-speed simulation."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out of SmartBus?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        repository.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
