package com.app.iuafc.ui.screens.activities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.DateField
import com.app.iuafc.ui.components.DropdownField
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.screens.features.ActivitiesViewModel

/** Ministry leaders submit activities; Administrators and Pastors review them. */
@Composable
fun ActivitiesScreen(
    onBack: () -> Unit,
    user: UserProfile,
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = viewModel(
        key = "activities-${user.id}",
        factory = viewModelFactory { initializer { ActivitiesViewModel(user, appContainer.churchFeaturesRepository) } },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var reviewing by remember { mutableStateOf<MinistryActivity?>(null) }
    var saveVersionOnOpen by remember { mutableStateOf(0) }
    var filter by remember { mutableStateOf<ActivityStatus?>(null) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.saveVersion) {
        if (state.saveVersion > saveVersionOnOpen) {
            showForm = false
            reviewing = null
        }
    }

    ScreenScaffold(
        title = "Ministry Activities",
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = {
            if (viewModel.canSubmit) {
                ExtendedFloatingActionButton(
                    onClick = {
                        saveVersionOnOpen = state.saveVersion
                        showForm = true
                    },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Propose activity") },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.activities.isEmpty() ->
                ErrorState(state.errorMessage!!, Modifier.padding(padding), onRetry = viewModel::load)
            else -> {
                val visible = viewModel.visibleActivities().filter { filter == null || it.status == filter }
                Column(Modifier.padding(padding).fillMaxSize()) {
                    state.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("All") })
                        ActivityStatus.entries.forEach { status ->
                            FilterChip(
                                selected = filter == status,
                                onClick = { filter = status },
                                label = { Text(status.name) },
                            )
                        }
                    }
                    if (visible.isEmpty()) {
                        EmptyState(
                            title = if (state.activities.isEmpty()) "No activities yet" else "No activities in this status",
                            message = if (viewModel.canSubmit) "Propose an activity for your ministry." else null,
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(visible, key = { it.id }) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    ministryName = state.ministries.firstOrNull { it.id == activity.ministryId }?.name
                                        ?: "Ministry",
                                    canReview = viewModel.canReview && activity.status == ActivityStatus.Pending,
                                    onReview = {
                                        reviewing = activity
                                        saveVersionOnOpen = state.saveVersion
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        ActivityForm(
            ministries = state.ministries,
            initialMinistryId = user.ministryId,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = { showForm = false },
            onSubmit = viewModel::submit,
        )
    }
    reviewing?.let { activity ->
        ReviewDialog(
            activity = activity,
            isSaving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = { reviewing = null },
            onReview = { status, note -> viewModel.review(activity, status, note) },
        )
    }
}

@Composable
private fun ActivityCard(
    activity: MinistryActivity,
    ministryName: String,
    canReview: Boolean,
    onReview: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    activity.status.name,
                    color = when (activity.status) {
                        ActivityStatus.Approved -> MaterialTheme.colorScheme.primary
                        ActivityStatus.Rejected -> MaterialTheme.colorScheme.error
                        ActivityStatus.Pending -> MaterialTheme.colorScheme.tertiary
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Text("$ministryName | ${activity.proposedDate} | ${activity.venue}", style = MaterialTheme.typography.bodySmall)
            Text(activity.description, style = MaterialTheme.typography.bodyMedium)
            activity.reviewNote?.let {
                Text("Review note: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (canReview) {
                TextButton(onClick = onReview) { Text("Review request") }
            }
        }
    }
}

@Composable
private fun ActivityForm(
    ministries: List<com.app.iuafc.data.model.Ministry>,
    initialMinistryId: String?,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit,
) {
    var ministryId by remember { mutableStateOf(initialMinistryId.orEmpty()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Propose an activity") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DropdownField(
                    label = "Ministry",
                    options = ministries,
                    selected = ministries.firstOrNull { it.id == ministryId },
                    onSelected = { ministryId = it.id },
                    optionLabel = { it.name },
                    placeholder = "Choose a ministry",
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    title, { title = it }, label = { Text("Title") }, singleLine = true,
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    description, { description = it }, label = { Text("Description") },
                    minLines = 2, enabled = !isSaving, modifier = Modifier.fillMaxWidth(),
                )
                DateField(date, { date = it }, "Proposed date", modifier = Modifier.fillMaxWidth(), enabled = !isSaving)
                OutlinedTextField(
                    venue, { venue = it }, label = { Text("Venue") }, singleLine = true,
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(ministryId, title, description, date, venue) },
                enabled = !isSaving && ministries.isNotEmpty(),
            ) { Text(if (isSaving) "Submitting..." else "Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } },
    )
}

@Composable
private fun ReviewDialog(
    activity: MinistryActivity,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onReview: (ActivityStatus, String) -> Unit,
) {
    var note by remember(activity.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review activity") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    note, { note = it }, label = { Text("Review note (optional)") },
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onReview(ActivityStatus.Approved, note) }, enabled = !isSaving) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Text("Approve")
                }
                TextButton(onClick = { onReview(ActivityStatus.Rejected, note) }, enabled = !isSaving) {
                    Icon(Icons.Outlined.Close, contentDescription = null)
                    Text("Reject")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } },
    )
}
