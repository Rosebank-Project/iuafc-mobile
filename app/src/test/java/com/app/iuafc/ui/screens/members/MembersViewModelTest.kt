package com.app.iuafc.ui.screens.members

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MembersViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val viewModel = MembersViewModel(MockMemberRepository(delayMillis = 0))

    // Runs after MainDispatcherRule has set the test dispatcher.
    @Before
    fun setUp() = viewModel.load()

    @Test
    fun load_showsAllMembersSortedByName() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(MockData.members.size, state.visibleMembers.size)
        assertEquals(state.visibleMembers.map { it.fullName }.sortedBy { it.lowercase() }, state.visibleMembers.map { it.fullName })
    }

    @Test
    fun search_matchesNameEmailOrPhone() {
        viewModel.onQueryChange("grace")
        assertEquals(listOf("Grace Adeyemi"), viewModel.uiState.value.visibleMembers.map { it.fullName })

        viewModel.onQueryChange("thabo@iuafc")
        assertEquals(listOf("Thabo Nkosi"), viewModel.uiState.value.visibleMembers.map { it.fullName })

        viewModel.onQueryChange("083 234")
        assertEquals(listOf("Daniel Okeke"), viewModel.uiState.value.visibleMembers.map { it.fullName })
    }

    @Test
    fun ministryFilter_showsOnlyThatMinistry() {
        viewModel.onMinistryFilterChange("min-2")
        val visible = viewModel.uiState.value.visibleMembers
        assertTrue(visible.isNotEmpty())
        assertTrue(visible.all { it.ministryId == "min-2" })
    }

    @Test
    fun sortNewest_putsLatestJoinDateFirst() {
        viewModel.onSortChange(MemberSort.NEWEST)
        val dates = viewModel.uiState.value.visibleMembers.map { it.joinDate }
        assertEquals(dates.sortedDescending(), dates)
    }

    @Test
    fun noMatches_returnsEmptyList() {
        viewModel.onQueryChange("nobody with this name")
        assertTrue(viewModel.uiState.value.visibleMembers.isEmpty())
    }
}
