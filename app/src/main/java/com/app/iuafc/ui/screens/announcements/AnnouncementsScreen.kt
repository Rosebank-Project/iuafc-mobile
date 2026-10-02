package com.app.iuafc.ui.screens.announcements

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Announcements screen – owner: Alexandra, issue #9. */
@Composable
fun AnnouncementsScreen(
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Announcements", modifier = modifier) { padding ->
        PlaceholderContent(feature = "Announcements", issue = "#9", modifier = Modifier.padding(padding))
    }
}
