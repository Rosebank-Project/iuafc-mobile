package com.app.iuafc.ui.screens.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.AttendanceStatus
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Gender
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.screens.reports.ReportsViewModel
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Attendance, giving and membership summaries for church leadership. */
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = viewModel(factory = ReportsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.load() }

    ScreenScaffold(title = "Reports", modifier = modifier, onBack = onBack) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null -> ErrorState(
                state.errorMessage!!,
                Modifier.padding(padding),
                onRetry = viewModel::load,
            )
            else -> {
                val totalMembers = state.members.size
                val attendanceByEvent = state.events.mapNotNull { event ->
                    val records = state.attendance.filter { it.eventId == event.id }
                    if (records.isEmpty()) null else AttendanceSummary(
                        label = event.name,
                        count = records.count { it.status == AttendanceStatus.Present },
                        total = records.size,
                    )
                }
                val attendanceByMonth = state.attendance.groupBy { it.attendanceDate.take(7) }
                    .toSortedMap(compareByDescending { it })
                    .map { (month, records) ->
                        AttendanceSummary(
                            label = monthLabel(month),
                            count = records.count { it.status == AttendanceStatus.Present },
                            total = records.size,
                        )
                    }
                val donationByType = DonationType.entries.map { type ->
                    type.name to state.donations.filter { it.donationType == type }.sumOf { it.amount }
                }
                val donationByMonth = state.donations.groupBy { it.donationDate.take(7) }
                    .toSortedMap(compareByDescending { it })
                    .map { (month, donations) -> monthLabel(month) to donations.sumOf { it.amount } }
                val ministryCounts = state.ministries.map { ministry ->
                    ministry.name to state.members.count { it.ministryId == ministry.id }
                }

                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        SectionCard(title = "Membership") {
                            Text("$totalMembers total members", style = MaterialTheme.typography.titleMedium)
                            Text("Men: ${state.members.count { it.gender == Gender.Male }}")
                            Text("Women: ${state.members.count { it.gender == Gender.Female }}")
                            Text("Not assigned to a ministry: ${state.members.count { member -> state.ministries.none { it.id == member.ministryId } }}")
                        }
                    }
                    item {
                        SectionCard(title = "Members by ministry") {
                            ministryCounts.forEach { (name, count) ->
                                SummaryBar(
                                    label = name,
                                    value = count.toFloat(),
                                    total = totalMembers.toFloat(),
                                    trailing = "$count",
                                )
                            }
                        }
                    }
                    item {
                        SectionCard(title = "Giving by type") {
                            donationByType.forEach { (type, amount) ->
                                SummaryBar(
                                    label = type,
                                    value = amount.toFloat(),
                                    total = state.donations.sumOf { it.amount }.toFloat(),
                                    trailing = formatRand(amount),
                                )
                            }
                            Text(
                                "Total: ${formatRand(state.donations.sumOf { it.amount })}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    item {
                        SectionCard(title = "Giving by month") {
                            if (donationByMonth.isEmpty()) Text("No donation records yet.")
                            donationByMonth.forEach { (month, amount) ->
                                SummaryBar(
                                    label = month,
                                    value = amount.toFloat(),
                                    total = donationByMonth.maxOfOrNull { it.second }?.toFloat() ?: 0f,
                                    trailing = formatRand(amount),
                                )
                            }
                        }
                    }
                    item {
                        SectionCard(title = "Attendance by event") {
                            if (attendanceByEvent.isEmpty()) Text("No attendance records yet.")
                            attendanceByEvent.forEach { summary ->
                                SummaryBar(
                                    label = summary.label,
                                    value = summary.count.toFloat(),
                                    total = summary.total.toFloat(),
                                    trailing = "${summary.count}/${summary.total} present",
                                )
                            }
                        }
                    }
                    item {
                        SectionCard(title = "Attendance by month") {
                            if (attendanceByMonth.isEmpty()) Text("No attendance records yet.")
                            attendanceByMonth.forEach { summary ->
                                SummaryBar(
                                    label = summary.label,
                                    value = summary.count.toFloat(),
                                    total = summary.total.toFloat(),
                                    trailing = "${summary.count}/${summary.total} present",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AttendanceSummary(val label: String, val count: Int, val total: Int)

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SummaryBar(label: String, value: Float, total: Float, trailing: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(trailing, style = MaterialTheme.typography.labelMedium)
        }
        LinearProgressIndicator(
            progress = { if (total > 0f) (value / total).coerceIn(0f, 1f) else 0f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatRand(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA")).format(amount)

private fun monthLabel(value: String): String = runCatching {
    YearMonth.parse(value).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH))
}.getOrDefault(value)
