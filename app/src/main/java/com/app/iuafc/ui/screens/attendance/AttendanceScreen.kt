package com.app.iuafc.ui.screens.attendance

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** Attendance screen – owner: Rolph, issue #4. */
@Composable
fun AttendanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "Attendance", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "Attendance", issue = "#4", modifier = Modifier.padding(padding))
    }
}
