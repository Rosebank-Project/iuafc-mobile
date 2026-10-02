package com.app.iuafc.ui.screens.members

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.ui.components.DateField
import com.app.iuafc.ui.components.DropdownField
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.components.ValidatedTextField

/** Add or edit a member (Administrator only). */
@Composable
fun MemberFormScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemberFormViewModel = viewModel(factory = MemberFormViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.saved) { if (state.saved) onBack() }

    ScreenScaffold(
        title = if (state.isEditing) "Edit member" else "Add member",
        modifier = modifier,
        onBack = onBack,
    ) { padding ->
        if (state.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@ScreenScaffold
        }

        val enabled = !state.isSaving
        val errors = state.errors
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            ValidatedTextField(
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Full name",
                error = errors[MemberField.FULL_NAME],
                leadingIcon = Icons.Outlined.Person,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Gender", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Gender.entries.forEachIndexed { index, gender ->
                    SegmentedButton(
                        selected = state.gender == gender,
                        onClick = { viewModel.onGenderChange(gender) },
                        shape = SegmentedButtonDefaults.itemShape(index, Gender.entries.size),
                        enabled = enabled,
                    ) { Text(gender.name) }
                }
            }
            errors[MemberField.GENDER]?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }

            DateField(
                value = state.dateOfBirth,
                onValueChange = viewModel::onDateOfBirthChange,
                label = "Date of birth",
                error = errors[MemberField.DATE_OF_BIRTH],
                allowFuture = false,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.contactNumber,
                onValueChange = viewModel::onContactNumberChange,
                label = "Contact number",
                error = errors[MemberField.CONTACT_NUMBER],
                supportingText = "e.g. 0821234567",
                leadingIcon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                error = errors[MemberField.EMAIL],
                leadingIcon = Icons.Outlined.Email,
                keyboardType = KeyboardType.Email,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            ValidatedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = "Address",
                error = errors[MemberField.ADDRESS],
                leadingIcon = Icons.Outlined.Home,
                imeAction = ImeAction.Done,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )
            DropdownField(
                label = "Ministry (optional)",
                options = listOf<Ministry?>(null) + state.ministries,
                selected = state.ministries.firstOrNull { it.id == state.ministryId },
                onSelected = { viewModel.onMinistryChange(it?.id) },
                optionLabel = { it?.name ?: "No ministry" },
                placeholder = "No ministry",
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
            DateField(
                value = state.joinDate,
                onValueChange = viewModel::onJoinDateChange,
                label = "Join date",
                error = errors[MemberField.JOIN_DATE],
                allowFuture = false,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::save,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(if (state.isEditing) "Save changes" else "Add member")
                }
            }
        }
    }
}
