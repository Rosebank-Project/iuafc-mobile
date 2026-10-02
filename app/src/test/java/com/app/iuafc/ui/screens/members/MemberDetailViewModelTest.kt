package com.app.iuafc.ui.screens.members

import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MemberDetailViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val repository = MockMemberRepository(delayMillis = 0)

    @Test
    fun load_showsMemberAndMinistry() {
        val viewModel = MemberDetailViewModel("mem-1", repository).apply { load() }

        assertEquals("Grace Adeyemi", viewModel.uiState.value.member?.fullName)
        assertEquals("Choir Ministry", viewModel.uiState.value.ministryName)
    }

    @Test
    fun unknownMember_showsError() {
        val viewModel = MemberDetailViewModel("mem-999", repository).apply { load() }

        assertNull(viewModel.uiState.value.member)
        assertEquals("This member no longer exists.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun delete_removesMember() = runTest {
        val viewModel = MemberDetailViewModel("mem-2", repository).apply { load() }
        viewModel.delete()

        assertTrue(viewModel.uiState.value.deleted)
        assertNull(repository.getMember("mem-2"))
    }
}
