package com.app.iuafc.ui.screens.notifications

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Notifications screen – owner: Alexandra, issue #9. */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Notifications", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Notifications", issue = "#9", modifier = Modifier.padding(padding))
    }
}
