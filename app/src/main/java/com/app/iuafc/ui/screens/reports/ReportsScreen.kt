package com.app.iuafc.ui.screens.reports

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Reports screen – owner: Avuzwa, issue #13. */
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Reports", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Reports", issue = "#13", modifier = Modifier.padding(padding))
    }
}
