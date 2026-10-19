package com.app.iuafc.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventsScreen(
    user: UserProfile,
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel(factory = EventsViewModel.factory(user))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showForm by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    var deletingEvent by remember { mutableStateOf<Event?>(null) }
    var viewingDetails by remember { mutableStateOf<Event?>(null) }

    ScreenScaffold(
        title = "Events",
        modifier = modifier,
        floatingActionButton = {
            if (viewModel.canManage) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingEvent = null
                        showForm = true
                    },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("New Event") }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.events.isEmpty() ->
                ErrorState(state.errorMessage!!, Modifier.padding(padding), onRetry = viewModel::loadEvents)
            state.events.isEmpty() ->
                EmptyState(title = "No events", message = "There are no events at the moment.", modifier = Modifier.padding(padding))
            else -> {
                // Upcoming events first (soonest on top), then past events (most recent on top),
                // like the website. Members can only register for upcoming events.
                val today = LocalDate.now().toString()
                val upcoming = state.events.filter { it.date >= today }.sortedWith(compareBy({ it.date }, { it.time }))
                val past = state.events.filter { it.date < today }.sortedWith(compareByDescending<Event> { it.date }.thenByDescending { it.time })

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "upcoming-header") { SectionHeader("Upcoming (${upcoming.size})") }
                    if (upcoming.isEmpty()) {
                        item(key = "upcoming-empty") {
                            Text(
                                "No upcoming events.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(upcoming, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            isRegistered = state.registeredEventIds.contains(event.id),
                            canRegister = viewModel.canRegister,
                            canManage = viewModel.canManage,
                            onOpenDetails = { viewingDetails = event },
                            onToggleRegistration = { viewModel.toggleRegistration(event.id) },
                            onEdit = {
                                editingEvent = event
                                showForm = true
                            },
                            onDelete = { deletingEvent = event }
                        )
                    }
                    if (past.isNotEmpty()) {
                        item(key = "past-header") { SectionHeader("Past (${past.size})") }
                        items(past, key = { it.id }) { event ->
                            EventCard(
                                event = event,
                                isRegistered = state.registeredEventIds.contains(event.id),
                                canRegister = false,
                                canManage = viewModel.canManage,
                                onOpenDetails = { viewingDetails = event },
                                onToggleRegistration = {},
                                onEdit = {
                                    editingEvent = event
                                    showForm = true
                                },
                                onDelete = { deletingEvent = event }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        EventFormDialog(
            event = editingEvent,
            createdBy = user.id,
            onDismiss = { showForm = false },
            onSave = { newEvent ->
                if (editingEvent == null) viewModel.createEvent(newEvent)
                else viewModel.updateEvent(newEvent)
                showForm = false
            }
        )
    }

    if (deletingEvent != null) {
        AlertDialog(
            onDismissRequest = { deletingEvent = null },
            title = { Text("Delete event?") },
            text = { Text("Are you sure you want to delete ${deletingEvent?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEvent(deletingEvent!!.id)
                    deletingEvent = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deletingEvent = null }) { Text("Cancel") } }
        )
    }

    if (viewingDetails != null) {
        AlertDialog(
            onDismissRequest = { viewingDetails = null },
            title = { Text(viewingDetails!!.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(viewingDetails!!.description)
                    Text("Date: ${formatDate(viewingDetails!!.date)}", fontWeight = FontWeight.Bold)
                    Text("Time: ${viewingDetails!!.time}")
                    Text("Venue: ${viewingDetails!!.venue}")
                }
            },
            confirmButton = { TextButton(onClick = { viewingDetails = null }) { Text("Close") } }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EventCard(
    event: Event,
    isRegistered: Boolean,
    canRegister: Boolean,
    canManage: Boolean,
    onOpenDetails: () -> Unit,
    onToggleRegistration: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenDetails() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (canManage) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Event", tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete Event", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)

            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Date", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = formatDate(event.date), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Outlined.AccessTime, contentDescription = "Time", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.time, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, contentDescription = "Venue", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.venue, style = MaterialTheme.typography.bodySmall)
            }

            if (canRegister) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onToggleRegistration,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegistered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isRegistered) "Unregister" else "Register for Event")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormDialog(
    event: Event?,
    createdBy: String,
    onDismiss: () -> Unit,
    onSave: (Event) -> Unit
) {
    var name by remember { mutableStateOf(event?.name ?: "") }
    var desc by remember { mutableStateOf(event?.description ?: "") }
    var date by remember { mutableStateOf(event?.date ?: "") }
    var time by remember { mutableStateOf(event?.time ?: "") }
    var venue by remember { mutableStateOf(event?.venue ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    // UI states for date/time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Date Picker Dialog
    if (showDatePicker) {
        // A new event can't be scheduled in the past (an existing past event can still be edited).
        val todayMillis = LocalDate.now().atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = event != null || utcTimeMillis >= todayMillis
                override fun isSelectableYear(year: Int) = event != null || year >= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        // Convert millis to YYYY-MM-DD
                        val selectedDate = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        date = selectedDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    // Format to HH:mm
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    time = "$hour:$minute"
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (event == null) "New Event" else "Edit Event") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Event Name *") }, singleLine = true)
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })

                // Date Field (Clickable to open calendar)
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Outlined.CalendarMonth, "Pick Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Time Field (Clickable to open clock)
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = { Text("Time *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Outlined.AccessTime, "Pick Time")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue *") }, singleLine = true)

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || date.isBlank() || time.isBlank() || venue.isBlank()) {
                    error = "Please fill in all required fields."
                } else if (event == null && date < LocalDate.now().toString()) {
                    error = "The date can't be in the past."
                } else {
                    onSave(
                        Event(
                            id = event?.id ?: "evt-${System.currentTimeMillis()}",
                            name = name.trim(),
                            description = desc.trim(),
                            date = date,
                            time = time,
                            venue = venue.trim(),
                            createdBy = event?.createdBy ?: createdBy
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatDate(value: String): String {
    return try {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
    } catch (_: Exception) {
        value
    }
}