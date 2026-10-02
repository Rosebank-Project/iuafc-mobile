package com.app.iuafc.ui.screens.help

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Help screen – owner: Avuzwa, issue #14. */
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Help", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Help", issue = "#14", modifier = Modifier.padding(padding))
    }
}
