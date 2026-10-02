package com.app.iuafc.ui.screens.members

import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MemberFormViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repository = MockMemberRepository(delayMillis = 0)

    private fun MemberFormViewModel.fillValid() {
        onFullNameChange("Ayanda Mthembu")
        onGenderChange(Gender.Female)
        onDateOfBirthChange("1994-03-18")
        onContactNumberChange("0827654321")
        onEmailChange("ayanda@iuafc.org")
        onAddressChange("4 Hope Street, Soweto")
    }

    @Test
    fun emptyForm_showsRequiredErrors() {
        val viewModel = MemberFormViewModel(null, repository)
        viewModel.save()

        val errors = viewModel.uiState.value.errors
        listOf(
            MemberField.FULL_NAME, MemberField.GENDER, MemberField.DATE_OF_BIRTH,
            MemberField.CONTACT_NUMBER, MemberField.EMAIL, MemberField.ADDRESS,
        ).forEach { assertNotNull("$it should have an error", errors[it]) }
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun futureDateOfBirth_isRejected() {
        val viewModel = MemberFormViewModel(null, repository)
        viewModel.fillValid()
        viewModel.onDateOfBirthChange("2999-01-01")
        viewModel.save()

        assertEquals("Date of birth cannot be in the future", viewModel.uiState.value.errors[MemberField.DATE_OF_BIRTH])
    }

    @Test
    fun invalidPhone_isRejected() {
        val viewModel = MemberFormViewModel(null, repository)
        viewModel.fillValid()
        viewModel.onContactNumberChange("12ab")
        viewModel.save()

        assertNotNull(viewModel.uiState.value.errors[MemberField.CONTACT_NUMBER])
    }

    @Test
    fun validForm_addsMember() = runTest {
        val before = repository.getMembers().size
        val viewModel = MemberFormViewModel(null, repository)
        viewModel.fillValid()
        viewModel.save()

        assertTrue(viewModel.uiState.value.saved)
        assertEquals(before + 1, repository.getMembers().size)
        assertTrue(repository.getMembers().any { it.email == "ayanda@iuafc.org" })
    }

    @Test
    fun duplicateEmail_showsError() {
        val viewModel = MemberFormViewModel(null, repository)
        viewModel.fillValid()
        viewModel.onEmailChange("grace@iuafc.org")
        viewModel.save()

        assertEquals("This email is already registered to another member.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun editMember_loadsAndSavesChanges() = runTest {
        val viewModel = MemberFormViewModel("mem-1", repository)
        assertEquals("Grace Adeyemi", viewModel.uiState.value.fullName)
        assertTrue(viewModel.uiState.value.isEditing)

        viewModel.onAddressChange("99 New Road, Sandton")
        viewModel.save()

        assertTrue(viewModel.uiState.value.saved)
        assertEquals("99 New Road, Sandton", repository.getMember("mem-1")?.address)
    }
}
