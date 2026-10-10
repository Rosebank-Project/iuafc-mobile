package com.app.iuafc.ui.screens.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.iuafc.ui.components.EmptyState
import com.app.iuafc.ui.components.ScreenScaffold

private data class HelpArticle(val section: String, val question: String, val answer: String)

private val helpArticles = listOf(
    HelpArticle("Getting started", "How do I register?", "Choose Register on the sign-in screen and enter the requested account details. If registration is unavailable, ask a church Administrator to help set up your account."),
    HelpArticle("Getting started", "How do I log in?", "Enter the email address and password associated with your account on the sign-in screen. You can use Forgot password to request a reset."),
    HelpArticle("Getting started", "Why can’t I see a feature?", "Available screens depend on your church role. Ask an Administrator if you believe your account has the wrong role or ministry assignment."),
    HelpArticle("Dashboard", "What is on the dashboard?", "The dashboard is your starting point for church updates and shortcuts to the features available to your account."),
    HelpArticle("Events and announcements", "How do I find an event?", "Open Events to browse church events and their dates, times and venues. Open Announcements to read notices from church leadership."),
    HelpArticle("Attendance", "How do I view attendance?", "Church members can open My Attendance to see their own records. Authorized leaders can open Attendance to manage event attendance."),
    HelpArticle("Donations", "Where can I find my giving history?", "Open Donations to view the donation records linked to your member account. Contact the Finance Officer if a record needs correction."),
    HelpArticle("Donations", "How is a donation recorded?", "Authorized Finance Officers and Administrators can record a member, amount, donation type, payment method and date. A receipt number is assigned automatically."),
    HelpArticle("Ministries", "How do I find ministry details?", "Open Ministries to see ministry descriptions and their assigned leaders. Administrators can add or edit ministry details."),
    HelpArticle("Ministry activities", "How do I propose an activity?", "Ministry Leaders can open Ministry Activities, choose Propose activity and submit its title, description, date and venue for review."),
    HelpArticle("Ministry activities", "How are activities approved?", "Administrators and Pastors can review pending activity requests and approve or reject them with an optional note. The decision appears with the activity."),
    HelpArticle("Reports", "Who can see reports?", "Reports are available to Administrators and Pastors and summarize membership, attendance and donations."),
    HelpArticle("Settings", "How do I change my settings?", "Open Settings from the Menu. If you cannot find a setting or need help with your account, contact a church Administrator."),
)

/** Searchable, feature-grouped FAQ and support guidance. */
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expandedQuestion by rememberSaveable { mutableStateOf<String?>(null) }
    val q = query.trim().lowercase()
    val filtered = helpArticles.filter {
        q.isEmpty() || it.section.lowercase().contains(q) ||
            it.question.lowercase().contains(q) || it.answer.lowercase().contains(q)
    }

    ScreenScaffold(title = "Help & FAQs", modifier = modifier, onBack = onBack) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search help by feature") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            if (filtered.isEmpty()) {
                EmptyState(
                    title = "No help articles found",
                    message = "Try searching for another feature or question.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val sections = filtered.groupBy { it.section }
                    sections.forEach { (section, articles) ->
                        item(key = "section-$section") {
                            Text(
                                section,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                            )
                        }
                        items(articles, key = { it.question }) { article ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    expandedQuestion = if (expandedQuestion == article.question) null else article.question
                                },
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(article.question, fontWeight = FontWeight.SemiBold)
                                    if (expandedQuestion == article.question || q.isNotEmpty()) {
                                        Text(article.answer, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Need more help?", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "For account access, incorrect records or feature support, contact your church Administrator or Pastor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
