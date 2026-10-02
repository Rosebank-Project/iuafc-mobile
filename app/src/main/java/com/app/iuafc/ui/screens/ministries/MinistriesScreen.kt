package com.app.iuafc.ui.screens.ministries

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Ministries screen – owner: Avuzwa, issue #12. */
@Composable
fun MinistriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Ministries", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Ministries", issue = "#12", modifier = Modifier.padding(padding))
    }
}
