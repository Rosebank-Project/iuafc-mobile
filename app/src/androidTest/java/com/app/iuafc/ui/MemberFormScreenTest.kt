package com.app.iuafc.ui

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.ui.screens.members.MemberFormScreen
import com.app.iuafc.ui.screens.members.MemberFormViewModel
import com.app.iuafc.ui.theme.IUAFCTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** UI tests for the member form: required fields, invalid phone numbers and saving. */
@RunWith(AndroidJUnit4::class)
class MemberFormScreenTest {

    @get:Rule val compose = createComposeRule()

    private val repository = MockMemberRepository(delayMillis = 0)
    private var closed = false

    private fun showForm(memberId: String?) {
        compose.setContent {
            IUAFCTheme {
                MemberFormScreen(
                    onBack = { closed = true },
                    viewModel = MemberFormViewModel(memberId, repository),
                )
            }
        }
    }

    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label))
    private fun button(text: String) = compose.onNode(hasText(text) and hasClickAction())

    // Error messages are part of their text field, so search the unmerged tree too.
    private fun waitForText(text: String) = compose.waitUntil(5_000) {
        compose.onAllNodes(hasText(text, substring = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun newMember_emptyForm_showsRequiredErrors() {
        showForm(memberId = null)

        button("Add member").performScrollTo().performClick()

        waitForText("Full name is required")
        compose.onNodeWithText("Contact number is required").performScrollTo().assertExists()
        compose.onNodeWithText("Email is required").performScrollTo().assertExists()
        compose.onNodeWithText("Address is required").performScrollTo().assertExists()
        assertTrue(!closed)
    }

    @Test
    fun invalidPhone_showsError_andIsNotSaved() {
        showForm(memberId = "mem-1")
        waitForText("Grace Adeyemi")

        field("Contact number").performScrollTo().performTextReplacement("12345")
        button("Save changes").performScrollTo().performClick()

        waitForText("Enter a valid phone number (e.g. 0821234567)")
        assertTrue(!closed)
        assertEquals("0821234567", runBlocking { repository.getMember("mem-1") }?.contactNumber)
    }

    @Test
    fun validChanges_areSaved_andTheFormCloses() {
        showForm(memberId = "mem-1")
        waitForText("Grace Adeyemi")

        field("Contact number").performScrollTo().performTextReplacement("0829998877")
        button("Save changes").performScrollTo().performClick()

        compose.waitUntil(5_000) { closed }
        assertEquals("0829998877", runBlocking { repository.getMember("mem-1") }?.contactNumber)
    }
}
