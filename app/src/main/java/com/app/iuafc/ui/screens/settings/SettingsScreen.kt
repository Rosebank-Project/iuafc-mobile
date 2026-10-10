package com.app.iuafc.ui.screens.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Settings screen – owner: Alexandra, issue #10. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenHelp: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "Settings",
        modifier = modifier,
        onBack = onBack,
        actions = { TextButton(onClick = onOpenHelp) { Text("Help") } },
    ) { padding ->
        PlaceholderContent(feature = "Settings", issue = "#10", modifier = Modifier.padding(padding))
    }
}
