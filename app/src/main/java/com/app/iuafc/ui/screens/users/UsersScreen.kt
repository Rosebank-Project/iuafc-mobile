package com.app.iuafc.ui.screens.users

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Users screen – owner: Miguel, issue #22. */
@Composable
fun UsersScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Users", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Users", issue = "#22", modifier = Modifier.padding(padding))
    }
}
