package com.app.iuafc.ui.screens.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.ui.components.DateField
import com.app.iuafc.ui.components.DropdownField
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ErrorState
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.components.toDisplayDate
import com.app.iuafc.ui.screens.donations.DonationsViewModel
import java.text.NumberFormat
import java.util.Locale

/** Members see only their own history; Finance Officers and Administrators can record gifts. */
@Composable
fun DonationsScreen(
    onBack: () -> Unit,
    user: UserProfile,
    modifier: Modifier = Modifier,
    viewModel: DonationsViewModel = viewModel(
        key = "donations-${user.id}",
        factory = viewModelFactory {
            initializer {
                DonationsViewModel(user, appContainer.churchFeaturesRepository, appContainer.memberRepository)
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var saveVersionOnOpen by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.saveVersion) {
        if (showForm && state.saveVersion > saveVersionOnOpen) showForm = false
    }

    ScreenScaffold(
        title = "Donations",
        modifier = modifier,
        onBack = onBack,
        floatingActionButton = {
            if (viewModel.canRecord) {
                ExtendedFloatingActionButton(
                    onClick = {
                        viewModel.clearError()
                        saveVersionOnOpen = state.saveVersion
                        showForm = true
                    },
                    icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                    text = { Text("Record donation") },
                )
            }
        },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.errorMessage != null && state.donations.isEmpty() ->
                ErrorState(state.errorMessage!!, Modifier.padding(padding), onRetry = viewModel::load)
            state.donations.isEmpty() -> EmptyState(
                title = "No donations yet",
                message = if (viewModel.canRecord) "Record a donation to get started." else "Your donation history will appear here.",
                modifier = Modifier.padding(padding),
            )
            else -> {
                val donations = state.donations.sortedByDescending { it.donationDate }
                Column(Modifier.padding(padding).fillMaxSize()) {
                    if (!showForm) {
                        state.errorMessage?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                        }
                    }
                    Text(
                        "${donations.size} donations | ${formatRand(donations.sumOf { it.amount })}",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(donations, key = { it.id }) { donation ->
                            DonationCard(
                                donation = donation,
                                memberName = state.members.firstOrNull { it.id == donation.memberId }?.fullName,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        DonationForm(
            members = state.members,
            saving = state.isSaving,
            errorMessage = state.errorMessage,
            onDismiss = {
                showForm = false
                viewModel.clearError()
            },
            onRecord = viewModel::record,
        )
    }
}

@Composable
private fun DonationCard(donation: Donation, memberName: String?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatRand(donation.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${donation.donationType.name} · ${donation.donationDate.toDisplayDate()} · ${donation.paymentMethod.name}")
            if (memberName != null) {
                Text("Member: $memberName", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Receipt ${donation.receiptNumber}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DonationForm(
    members: List<com.app.iuafc.data.model.Member>,
    saving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onRecord: (String, String, DonationType, PaymentMethod, String) -> Unit,
) {
    var memberId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DonationType.Tithe) }
    var method by remember { mutableStateOf(PaymentMethod.Cash) }
    var date by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record donation") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DropdownField(
                    label = "Member",
                    options = members,
                    selected = members.firstOrNull { it.id == memberId },
                    onSelected = { memberId = it.id },
                    optionLabel = { it.fullName },
                    placeholder = "Choose a member",
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (ZAR)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "Type",
                    options = DonationType.entries,
                    selected = type,
                    onSelected = { type = it },
                    optionLabel = { it.name },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownField(
                    label = "Payment method",
                    options = PaymentMethod.entries,
                    selected = method,
                    onSelected = { method = it },
                    optionLabel = { it.name },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(date, { date = it }, "Donation date", allowFuture = false, modifier = Modifier.fillMaxWidth(), enabled = !saving)
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onRecord(memberId, amount, type, method, date) },
                enabled = !saving && members.isNotEmpty(),
            ) { Text(if (saving) "Saving..." else "Record") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}

private fun formatRand(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA")).format(amount)
