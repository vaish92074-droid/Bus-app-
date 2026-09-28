package com.example.smartbus.presentation.passenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.smartbus.data.model.FeedbackItem
import com.example.smartbus.data.model.NotificationItem
import com.example.smartbus.data.model.NotificationType
import com.example.smartbus.data.repository.SmartBusRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    repository: SmartBusRepository,
    onNavigateBack: () -> Unit,
    onNavigateToBus: (String) -> Unit
) {
    val notifications by repository.notifications.collectAsState()
    var filterType by remember { mutableStateOf<NotificationType?>(null) }

    val displayed = if (filterType == null) notifications else notifications.filter { it.type == filterType }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & Alerts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { repository.markAllNotificationsAsRead() }) {
                        Text("Mark all read")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("All (${notifications.size})") }
                    )
                    FilterChip(
                        selected = filterType == NotificationType.BUS_APPROACHING,
                        onClick = { filterType = if (filterType == NotificationType.BUS_APPROACHING) null else NotificationType.BUS_APPROACHING },
                        label = { Text("Approaching") }
                    )
                    FilterChip(
                        selected = filterType == NotificationType.BUS_DELAYED,
                        onClick = { filterType = if (filterType == NotificationType.BUS_DELAYED) null else NotificationType.BUS_DELAYED },
                        label = { Text("Delays") }
                    )
                }
            }

            if (displayed.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                        Text("No notifications at this time", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(displayed) { item ->
                NotificationRow(
                    item = item,
                    onClick = {
                        repository.markNotificationAsRead(item.id)
                        if (item.busId != null) {
                            onNavigateToBus(item.busId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationRow(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val (icon, iconColor) = when (item.type) {
        NotificationType.BUS_APPROACHING -> Pair(Icons.Default.NearMe, StatusOnTime)
        NotificationType.BUS_DELAYED -> Pair(Icons.Default.Warning, StatusDelayed)
        NotificationType.BUS_ARRIVED -> Pair(Icons.Default.CheckCircle, StatusOnTime)
        NotificationType.TRIP_STARTED -> Pair(Icons.Default.DirectionsBus, MaterialTheme.colorScheme.primary)
        NotificationType.TRIP_CANCELLED -> Pair(Icons.Default.Cancel, StatusEmergency)
        NotificationType.EMERGENCY_ALERT -> Pair(Icons.Default.Emergency, StatusEmergency)
        else -> Pair(Icons.Default.Campaign, MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notif_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.read) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        fontWeight = if (!item.read) FontWeight.Bold else FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!item.read) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    repository: SmartBusRepository,
    initialBusId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val buses by repository.buses.collectAsState()
    val feedbackList by repository.feedbackList.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Submit, 1: Track Previous

    var category by remember { mutableStateOf("General") }
    var selectedBusId by remember { mutableStateOf(initialBusId ?: "") }
    var message by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }

    val categories = listOf("General", "Bus Issue", "Driver Issue", "Route Issue", "GPS Issue", "Schedule Issue")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback & Issue Reporting") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Report Issue") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History (${feedbackList.size})") }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Submit a Grievance or Suggestion",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transport Committee will review and respond to reports.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text("Category", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(3).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.drop(3).forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Associated Bus (Optional)", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            buses.take(4).forEach { bus ->
                                FilterChip(
                                    selected = selectedBusId == bus.busId,
                                    onClick = { selectedBusId = if (selectedBusId == bus.busId) "" else bus.busId },
                                    label = { Text(bus.busNumber.take(6)) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Describe the issue or suggestion...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("feedback_msg_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (message.isBlank()) {
                                    Toast.makeText(context, "Please describe your issue", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                repository.submitFeedback(
                                    category = category,
                                    message = message,
                                    busId = selectedBusId
                                )
                                message = ""
                                Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show()
                                selectedTab = 1
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_submit_feedback"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Submit to College Transport Office")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(feedbackList) { item ->
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
                                    Text(
                                        text = item.category,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (item.status) {
                                                    "RESOLVED" -> StatusOnTime.copy(alpha = 0.2f)
                                                    "REVIEWED" -> MaterialTheme.colorScheme.primaryContainer
                                                    else -> StatusDelayed.copy(alpha = 0.2f)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (item.status) {
                                                "RESOLVED" -> StatusOnTime
                                                "REVIEWED" -> MaterialTheme.colorScheme.primary
                                                else -> StatusDelayed
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(item.message, style = MaterialTheme.typography.bodyMedium)

                                if (item.adminReply.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Admin Response:",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(item.adminReply, fontSize = 12.sp)
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
}
