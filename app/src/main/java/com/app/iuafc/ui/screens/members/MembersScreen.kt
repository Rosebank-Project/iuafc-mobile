package com.app.iuafc.ui.screens.members

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Members screen – owner: Miguel, issue #22. */
@Composable
fun MembersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Members", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Members", issue = "#22", modifier = Modifier.padding(padding))
    }
}
