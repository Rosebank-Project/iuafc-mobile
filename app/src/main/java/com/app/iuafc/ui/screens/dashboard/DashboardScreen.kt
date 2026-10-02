package com.app.iuafc.ui.screens.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Home screen – owner: Alexandra, issue #9. */
@Composable
fun DashboardScreen(
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "Home",
        modifier = modifier,
        actions = {
            IconButton(onClick = onOpenNotifications) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications")
            }
        },
    ) { padding ->
        PlaceholderContent(feature = "Home", issue = "#9", modifier = Modifier.padding(padding))
    }
}
