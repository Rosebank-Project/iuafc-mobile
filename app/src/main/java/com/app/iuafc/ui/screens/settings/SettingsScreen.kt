package com.app.iuafc.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                    settingsRepository = container.settingsRepository,
                    sessionRepository = container.sessionRepository,
                )
            }
        },
    )

    val profile by
    viewModel.profile.collectAsStateWithLifecycle()

    val notificationsEnabled by
    viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    val isProfileLoading by
    viewModel.isProfileLoading.collectAsStateWithLifecycle()

    val profileMessage by
    viewModel.profileMessage.collectAsStateWithLifecycle()

    val passwordResetMessage by
    viewModel.passwordResetMessage.collectAsStateWithLifecycle()

    val themePreference by
    viewModel.themePreference.collectAsStateWithLifecycle()

    val isThemeLoading by
    viewModel.isThemeLoading.collectAsStateWithLifecycle()

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

    var showThemeSelector by remember {
        mutableStateOf(false)
    }

    var showHelp by remember {
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

    // Close the Edit Profile bubble only after
    // the profile has actually been saved successfully.
    LaunchedEffect(profileMessage) {
        if (
            profileMessage?.contains(
                "successfully",
                ignoreCase = true,
            ) == true
        ) {
            showEditProfile = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {

        // =========================================================
        // MAIN SETTINGS CONTENT
        // =========================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // -----------------------------------------------------
            // Header
            // -----------------------------------------------------

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
                        modifier = Modifier.size(4.dp),
                    )

                    Text("Back")
                }

                Spacer(
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            // -----------------------------------------------------
            // Account
            // -----------------------------------------------------

            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = profile?.fullName ?: user.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp),
                    )

                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp),
                    )

                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
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
                            showEditProfile = true
                            viewModel.clearProfileMessage()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp),
                        )

                        Text("Edit Profile")
                    }
                }
            }

            // -----------------------------------------------------
            // Notifications
            // -----------------------------------------------------

            HorizontalDivider()

            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color =
                            MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Notifications,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.size(12.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Push Notifications",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text =
                                if (notificationsEnabled) {
                                    "Notifications are enabled."
                                } else {
                                    "Notifications are disabled."
                                },
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            viewModel.setNotificationsEnabled(it)
                        },
                    )
                }
            }

            // -----------------------------------------------------
            // Appearance
            // -----------------------------------------------------

            HorizontalDivider()

            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color =
                            MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme.primary,
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.size(12.dp),
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Theme",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = when (themePreference) {
                                ThemePreference.SYSTEM ->
                                    "System default"

                                ThemePreference.LIGHT ->
                                    "Light"

                                ThemePreference.DARK ->
                                    "Dark"
                            },
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showThemeSelector = true
                        },
                        enabled = !isThemeLoading,
                    ) {
                        Text("Change")
                    }
                }
            }

            // -----------------------------------------------------
            // Security
            // -----------------------------------------------------

            HorizontalDivider()

            Text(
                text = "Security",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color =
                                MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.size(12.dp),
                        )

                        Text(
                            text = "Password",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Text(
                        text =
                            "Send a password reset email to your account.",
                        style =
                            MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.sendPasswordReset(user.email)
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
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                },
                            style =
                                MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            // -----------------------------------------------------
            // Help & About
            // -----------------------------------------------------

            HorizontalDivider()

            Text(
                text = "Help & About",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color =
                                MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector =
                                        Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.size(12.dp),
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Help & Support",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Text(
                                text =
                                    "Get help using the IUAFC app.",
                                style =
                                    MaterialTheme.typography.bodyMedium,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                showHelp = true
                            },
                        ) {
                            Text("Help")
                        }
                    }

                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color =
                                MaterialTheme.colorScheme.primaryContainer,
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint =
                                        MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier.size(12.dp),
                        )

                        Column {
                            Text(
                                text = "IUAFC Mobile",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )

                            Text(
                                text =
                                    "International United Apostolic Faith Church",
                                style =
                                    MaterialTheme.typography.bodyMedium,
                            )

                            Text(
                                text = "Version 1.0",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------
            // Sign Out
            // -----------------------------------------------------

            Button(
                onClick = {
                    viewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                )

                Spacer(
                    modifier = Modifier.size(8.dp),
                )

                Text("Sign Out")
            }

            Spacer(
                modifier = Modifier.height(20.dp),
            )
        }

        // =========================================================
        // EDIT PROFILE BUBBLE
        // =========================================================

        if (showEditProfile) {

            SettingsOverlay(
                onDismiss = {
                    showEditProfile = false
                },
            )

            EditProfileBubble(
                fullName = fullName,
                phone = phone,
                address = address,
                profileMessage = profileMessage,
                isLoading = isProfileLoading,
                onFullNameChange = {
                    fullName = it
                    viewModel.clearProfileMessage()
                },
                onPhoneChange = {
                    phone = it
                    viewModel.clearProfileMessage()
                },
                onAddressChange = {
                    address = it
                    viewModel.clearProfileMessage()
                },
                onSave = {
                    viewModel.updateProfile(
                        user = user,
                        fullName = fullName,
                        phone = phone,
                        address = address,
                    )
                },
                onClose = {
                    showEditProfile = false
                },
            )
        }

        // =========================================================
        // THEME BUBBLE
        // =========================================================

        if (showThemeSelector) {

            SettingsOverlay(
                onDismiss = {
                    showThemeSelector = false
                },
            )

            ThemeBubble(
                selectedTheme = themePreference,
                enabled = !isThemeLoading,
                onThemeSelected = {
                    viewModel.updateThemePreference(it)
                    showThemeSelector = false
                },
                onClose = {
                    showThemeSelector = false
                },
            )
        }

        // =========================================================
        // HELP BUBBLE
        // =========================================================

        if (showHelp) {

            SettingsOverlay(
                onDismiss = {
                    showHelp = false
                },
            )

            HelpBubble(
                onClose = {
                    showHelp = false
                },
            )
        }
    }
}

@Composable
private fun SettingsOverlay(
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.35f),
            )
            .clickable(
                onClick = onDismiss,
            ),
    )
}

@Composable
private fun EditProfileBubble(
    fullName: String,
    phone: String,
    address: String,
    profileMessage: String?,
    isLoading: Boolean,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 40.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Edit Profile",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp),
                        )

                        Text(
                            text =
                                "Update your personal information.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(
                        onClick = onClose,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    label = {
                        Text("Full Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    label = {
                        Text("Phone Number")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                    ),
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = {
                        Text("Address")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isLoading,
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
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                        style =
                            MaterialTheme.typography.bodyMedium,
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                ) {
                    Text(
                        if (isLoading) {
                            "Saving..."
                        } else {
                            "Save Changes"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeBubble(
    selectedTheme: ThemePreference,
    enabled: Boolean,
    onThemeSelected: (ThemePreference) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Appearance",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp),
                        )

                        Text(
                            text =
                                "Choose how the app should appear.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(
                        onClick = onClose,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(8.dp),
                )

                ThemeOption(
                    title = "System default",
                    selected =
                        selectedTheme ==
                                ThemePreference.SYSTEM,
                    enabled = enabled,
                    onClick = {
                        onThemeSelected(
                            ThemePreference.SYSTEM,
                        )
                    },
                )

                ThemeOption(
                    title = "Light",
                    selected =
                        selectedTheme ==
                                ThemePreference.LIGHT,
                    enabled = enabled,
                    onClick = {
                        onThemeSelected(
                            ThemePreference.LIGHT,
                        )
                    },
                )

                ThemeOption(
                    title = "Dark",
                    selected =
                        selectedTheme ==
                                ThemePreference.DARK,
                    enabled = enabled,
                    onClick = {
                        onThemeSelected(
                            ThemePreference.DARK,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun HelpBubble(
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
            ),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Help & Support",
                            style =
                                MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp),
                        )

                        Text(
                            text =
                                "Need help using the IUAFC mobile application?",
                            style =
                                MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(
                        onClick = onClose,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color =
                        MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Getting started",
                            style =
                                MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text =
                                "Use the navigation menu to access church announcements, events, attendance, donations, ministries, and your account settings.",
                            style =
                                MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Text(
                    text =
                        "For additional assistance, please contact your church administrator.",
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color =
            if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}