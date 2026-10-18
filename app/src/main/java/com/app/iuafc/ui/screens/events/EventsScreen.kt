package com.app.iuafc.ui.screens.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventsScreen(
    user: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel(factory = EventsViewModel.factory(user))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Events", modifier = modifier, onBack = onBack) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.events.isEmpty() ->
                ErrorState(state.errorMessage!!, Modifier.padding(padding), onRetry = viewModel::loadEvents)
            state.events.isEmpty() ->
                EmptyState(title = "No events", message = "There are no upcoming events at the moment.", modifier = Modifier.padding(padding))
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.events, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            isRegistered = state.registeredEventIds.contains(event.id),
                            canRegister = viewModel.canRegister,
                            canManage = viewModel.canManage,
                            onToggleRegistration = { viewModel.toggleRegistration(event.id) },
                            onDelete = { viewModel.deleteEvent(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    isRegistered: Boolean,
    canRegister: Boolean,
    canManage: Boolean,
    onToggleRegistration: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete Event", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, style = MaterialTheme.typography.bodyMedium)

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

            // Register button for Church Members
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

private fun formatDate(value: String): String {
    return try {
        LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
    } catch (_: Exception) {
        value
    }
}