package com.app.iuafc.ui.screens.activities

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Ministry Activities screen – owner: Avuzwa, issue #12. */
@Composable
fun ActivitiesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Ministry Activities", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Ministry Activities", issue = "#12", modifier = Modifier.padding(padding))
    }
}
