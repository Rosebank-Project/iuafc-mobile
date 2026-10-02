package com.app.iuafc.ui.screens.events

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Events screen – owner: Rolph, issue #4. */
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Events", modifier = modifier) { padding ->
        PlaceholderContent(feature = "Events", issue = "#4", modifier = Modifier.padding(padding))
    }
}
