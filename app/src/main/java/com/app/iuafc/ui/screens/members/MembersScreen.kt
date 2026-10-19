package com.app.iuafc.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Member
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.util.initialsOf
import com.app.iuafc.ui.components.ScreenScaffold

/** Members screen (Administrator and Pastor). Only Administrators can add or edit. */
@Composable
fun MembersScreen(
    onBack: () -> Unit,
    canManage: Boolean,
    onOpenMember: (String) -> Unit,
    onAddMember: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MembersViewModel = viewModel(factory = MembersViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    ScreenScaffold(
        title = "Members",
        modifier = modifier,
        onBack = onBack,
        actions = { SortMenu(state.sort, viewModel::onSortChange) },
        floatingActionButton = {
            if (canManage) {
                ExtendedFloatingActionButton(
                    onClick = onAddMember,
                    icon = { Icon(Icons.Outlined.PersonAdd, contentDescription = null) },
                    text = { Text("Add member") },
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage!!, onRetry = viewModel::load)
                else -> {
                    SearchField(state.query, viewModel::onQueryChange)
                    MinistryFilter(state, viewModel::onMinistryFilterChange)
                    val members = state.visibleMembers
                    if (members.isEmpty()) {
                        EmptyState(
                            title = if (state.members.isEmpty()) "No members yet" else "No members found",
                            message = if (state.members.isEmpty()) null else "Try a different name, email or ministry.",
                            icon = Icons.Outlined.SearchOff,
                        )
                    } else {
                        Text(
                            "${members.size} of ${state.members.size} members",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(members, key = { it.id }) { member ->
                                MemberCard(member, state.ministryName(member.ministryId)) { onOpenMember(member.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search by name, email or phone") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun MinistryFilter(state: MembersUiState, onSelect: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = state.ministryFilter == null, onClick = { onSelect(null) }, label = { Text("All") })
        state.ministries.forEach { ministry ->
            FilterChip(
                selected = state.ministryFilter == ministry.id,
                onClick = { onSelect(ministry.id) },
                label = { Text(ministry.name) },
            )
        }
    }
}

@Composable
private fun SortMenu(current: MemberSort, onSortChange: (MemberSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort members")
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            MemberSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "Sort by ${sort.label}",
                            fontWeight = if (sort == current) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSortChange(sort)
                        open = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MemberCard(member: Member, ministryName: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Initials(member.fullName)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(member.fullName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    ministryName ?: "No ministry",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    member.contactNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun Initials(name: String, size: Int = 44) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initialsOf(name),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}
