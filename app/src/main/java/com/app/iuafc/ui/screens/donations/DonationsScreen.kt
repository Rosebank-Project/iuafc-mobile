package com.app.iuafc.ui.screens.donations

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Donations screen – owner: Avuzwa, issue #12. */
@Composable
fun DonationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Donations", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Donations", issue = "#12", modifier = Modifier.padding(padding))
    }
}
