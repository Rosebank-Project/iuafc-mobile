package com.app.iuafc.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.navigation.AppDestination
import com.app.iuafc.ui.components.ScreenScaffold
import com.app.iuafc.ui.theme.IUAFCTheme

/**
 * Lists every feature the user's role can open (same rules as the website
 * sidebar), plus Settings, Help and Sign out.
 */
@Composable
fun MenuScreen(
    user: UserProfile,
    onNavigate: (AppDestination) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val features = AppDestination.menuItemsFor(user.role)

    ScreenScaffold(title = "Menu", modifier = modifier) { padding ->
        LazyColumn(contentPadding = padding) {
            item { ProfileHeader(user) }

            items(features) { destination ->
                MenuRow(destination.label, destination.icon) { onNavigate(destination) }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { MenuRow(AppDestination.SETTINGS.label, AppDestination.SETTINGS.icon) { onNavigate(AppDestination.SETTINGS) } }
            item { MenuRow(AppDestination.HELP.label, AppDestination.HELP.icon) { onNavigate(AppDestination.HELP) } }
            item {
                ListItem(
                    headlineContent = { Text("Sign out", color = MaterialTheme.colorScheme.error) },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.clickable(onClick = onSignOut),
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: UserProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    initials(user.fullName),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    user.role.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MenuRow(label: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

@Preview(showBackground = true)
@Composable
private fun MenuScreenPreview() {
    IUAFCTheme {
        MenuScreen(
            user = UserProfile("1", "Grace Mokoena", "grace@iuafc.org", Role.PASTOR),
            onNavigate = {},
            onSignOut = {},
        )
    }
}
