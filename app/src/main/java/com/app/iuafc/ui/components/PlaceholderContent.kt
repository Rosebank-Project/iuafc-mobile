package com.app.iuafc.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Temporary content for a screen that has not been built yet.
 * Replace it with the real screen when you work on the issue.
 */
@Composable
fun PlaceholderContent(
    feature: String,
    issue: String,
    modifier: Modifier = Modifier,
) {
    MessageState(
        icon = Icons.Outlined.Construction,
        title = "$feature – coming soon",
        message = "This screen is being built in issue $issue.",
        modifier = modifier,
    )
}
