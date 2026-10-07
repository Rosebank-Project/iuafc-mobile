package com.app.iuafc.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.iuafc.appContainer
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.ThemePreference

@Composable
fun SettingsScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val container = this.appContainer

                SettingsViewModel(
                    settingsRepository =
                        container.settingsRepository,
                    sessionRepository =
                        container.sessionRepository,
                )
            }
        },
    )

    val profile by
    viewModel.profile.collectAsStateWithLifecycle()

    val notificationsEnabled by
    viewModel.notificationsEnabled
        .collectAsStateWithLifecycle()

    val isProfileLoading by
    viewModel.isProfileLoading
        .collectAsStateWithLifecycle()

    val profileMessage by
    viewModel.profileMessage
        .collectAsStateWithLifecycle()

    val passwordResetMessage by
    viewModel.passwordResetMessage
        .collectAsStateWithLifecycle()

    val themePreference by
    viewModel.themePreference
        .collectAsStateWithLifecycle()

    val isThemeLoading by
    viewModel.isThemeLoading
        .collectAsStateWithLifecycle()

    var fullName by remember(user.id) {
        mutableStateOf(user.fullName)
    }

    var phone by remember(user.id) {
        mutableStateOf("")
    }

    var address by remember(user.id) {
        mutableStateOf("")
    }

    var showEditProfile by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(user.id) {
        viewModel.loadProfile(user)
    }

    LaunchedEffect(Unit) {
        viewModel.loadThemePreference()
    }

    LaunchedEffect(profile) {
        profile?.let {
            fullName = it.fullName
            phone = it.phone
            address = it.address
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState(),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // ---------------------------------------------------------
        // Header
        // ---------------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onBack,
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                )

                Spacer(
                    modifier = Modifier.padding(4.dp),
                )

                Text("Back")
            }

            Spacer(
                modifier = Modifier.weight(1f),
            )

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        // ---------------------------------------------------------
        // Account
        // ---------------------------------------------------------

        Text(
            text = "Account",
            style = MaterialTheme.typography.titleLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Text(
                    text = "Name",
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = profile?.fullName
                        ?: user.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "Email",
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "Role",
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = user.role.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(
                    modifier = Modifier.height(8.dp),
                )

                OutlinedButton(
                    onClick = {
                        showEditProfile =
                            !showEditProfile

                        viewModel.clearProfileMessage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                    )

                    Spacer(
                        modifier = Modifier.padding(4.dp),
                    )

                    Text(
                        if (showEditProfile) {
                            "Cancel Editing"
                        } else {
                            "Edit Profile"
                        },
                    )
                }
            }
        }

        // ---------------------------------------------------------
        // Edit Profile
        // ---------------------------------------------------------

        if (showEditProfile) {

            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp),
                ) {

                    Text(
                        text = "Edit Profile",
                        style =
                            MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        text =
                            "Update your personal information.",
                        style =
                            MaterialTheme.typography.bodyMedium,
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            viewModel.clearProfileMessage()
                        },
                        label = {
                            Text("Full Name")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isProfileLoading,
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            viewModel.clearProfileMessage()
                        },
                        label = {
                            Text("Phone Number")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isProfileLoading,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Phone,
                            ),
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            viewModel.clearProfileMessage()
                        },
                        label = {
                            Text("Address")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        enabled = !isProfileLoading,
                    )

                    profileMessage?.let { message ->

                        Text(
                            text = message,
                            color =
                                if (
                                    message.contains(
                                        "successfully",
                                        ignoreCase = true,
                                    )
                                ) {
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                } else {
                                    MaterialTheme
                                        .colorScheme
                                        .error
                                },
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                user = user,
                                fullName = fullName,
                                phone = phone,
                                address = address,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProfileLoading,
                    ) {
                        Text(
                            if (isProfileLoading) {
                                "Saving..."
                            } else {
                                "Save Changes"
                            },
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // Notifications
        // ---------------------------------------------------------

        HorizontalDivider()

        Text(
            text = "Notifications",
            style = MaterialTheme.typography.titleLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Notifications,
                    contentDescription = null,
                )

                Spacer(
                    modifier = Modifier.padding(8.dp),
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = "Push Notifications",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )

                    Text(
                        text =
                            if (notificationsEnabled) {
                                "Notifications are enabled."
                            } else {
                                "Notifications are disabled."
                            },
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = {
                        viewModel
                            .setNotificationsEnabled(it)
                    },
                )
            }
        }

        // ---------------------------------------------------------
        // Appearance / Theme
        // ---------------------------------------------------------

        HorizontalDivider()

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp),
            ) {

                Text(
                    text = "Theme",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                )

                Text(
                    text =
                        "Choose how the app should appear.",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )

                ThemeOption(
                    title = "System default",
                    selected =
                        themePreference ==
                                ThemePreference.SYSTEM,
                    enabled = !isThemeLoading,
                    onClick = {
                        viewModel
                            .updateThemePreference(
                                ThemePreference.SYSTEM,
                            )
                    },
                )

                ThemeOption(
                    title = "Light",
                    selected =
                        themePreference ==
                                ThemePreference.LIGHT,
                    enabled = !isThemeLoading,
                    onClick = {
                        viewModel
                            .updateThemePreference(
                                ThemePreference.LIGHT,
                            )
                    },
                )

                ThemeOption(
                    title = "Dark",
                    selected =
                        themePreference ==
                                ThemePreference.DARK,
                    enabled = !isThemeLoading,
                    onClick = {
                        viewModel
                            .updateThemePreference(
                                ThemePreference.DARK,
                            )
                    },
                )
            }
        }

        // ---------------------------------------------------------
        // Security
        // ---------------------------------------------------------

        HorizontalDivider()

        Text(
            text = "Security",
            style = MaterialTheme.typography.titleLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Lock,
                        contentDescription = null,
                    )

                    Spacer(
                        modifier = Modifier.padding(6.dp),
                    )

                    Text(
                        text = "Password",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )
                }

                Text(
                    text =
                        "Send a password reset email to your account.",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )

                OutlinedButton(
                    onClick = {
                        viewModel.sendPasswordReset(
                            user.email,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Reset Password")
                }

                passwordResetMessage?.let { message ->

                    Text(
                        text = message,
                        color =
                            if (
                                message.contains(
                                    "sent",
                                    ignoreCase = true,
                                )
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            },
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                    )
                }
            }
        }

        // ---------------------------------------------------------
        // About
        // ---------------------------------------------------------

        HorizontalDivider()

        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Info,
                        contentDescription = null,
                    )

                    Spacer(
                        modifier = Modifier.padding(6.dp),
                    )

                    Text(
                        text = "IUAFC Mobile",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )
                }

                Text(
                    text =
                        "International United Apostolic Faith Church",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )

                Text(
                    text = "Version 1.0",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )
            }
        }

        // ---------------------------------------------------------
        // Sign Out
        // ---------------------------------------------------------

        Button(
            onClick = {
                viewModel.signOut()
                onSignOut()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector =
                    Icons.Default.Logout,
                contentDescription = null,
            )

            Spacer(
                modifier = Modifier.padding(4.dp),
            )

            Text("Sign Out")
        }

        Spacer(
            modifier = Modifier.height(20.dp),
        )
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
        )

        Text(
            text = title,
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            modifier =
                Modifier.padding(start = 8.dp),
        )
    }
}