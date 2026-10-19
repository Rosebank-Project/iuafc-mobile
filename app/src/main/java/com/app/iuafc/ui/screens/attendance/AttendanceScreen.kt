package com.app.iuafc.ui.screens.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.AttendanceStatus
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.components.DropdownField

@Composable
fun AttendanceScreen(
    user: UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AttendanceViewModel = viewModel(factory = AttendanceViewModel.factory(user))
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(title = "Capture Attendance", modifier = modifier, onBack = onBack) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

                val selectedEvent = state.events.find { it.id == state.selectedEventId }
                DropdownField(
                    label = "Select Event",
                    options = state.events,
                    selected = selectedEvent,
                    onSelected = { viewModel.selectEvent(it.id) },
                    optionLabel = { it.name },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                if (state.errorMessage != null) {
                    Text(
                        state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (state.selectedEventId == null) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text("Pick an event to capture attendance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.members, key = { it.id }) { member ->
                            val status = state.attendanceMap[member.id]
                            AttendanceRow(
                                memberName = member.fullName,
                                status = status,
                                onStatusChange = { viewModel.toggleStatus(member.id, it) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Outlined.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (state.saveSuccess) "Saved!" else "Save Attendance")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(
    memberName: String,
    status: AttendanceStatus?,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(memberName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = status == AttendanceStatus.Present,
                    onClick = { onStatusChange(AttendanceStatus.Present) },
                    label = { Text("Present") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    )
                )
                FilterChip(
                    selected = status == AttendanceStatus.Absent,
                    onClick = { onStatusChange(AttendanceStatus.Absent) },
                    label = { Text("Absent") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.error,
                        selectedLabelColor = MaterialTheme.colorScheme.onError,
                    )
                )
            }
        }
    }
}