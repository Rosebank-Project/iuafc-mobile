package com.app.iuafc.ui.screens.myattendance

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.iuafc.ui.components.PlaceholderContent
import com.app.iuafc.ui.components.ScreenScaffold

/** My Attendance screen – owner: Rolph, issue #4. */
@Composable
fun MyAttendanceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "My Attendance", modifier = modifier, onBack = onBack) { padding ->
        PlaceholderContent(feature = "My Attendance", issue = "#4", modifier = Modifier.padding(padding))
    }
}
