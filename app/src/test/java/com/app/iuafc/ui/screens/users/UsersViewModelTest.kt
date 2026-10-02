package com.app.iuafc.ui.screens.users

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.data.repository.MockUserRepository
import com.app.iuafc.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsersViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val viewModel = UsersViewModel(MockUserRepository(delayMillis = 0), MockMemberRepository(delayMillis = 0))

    @Before
    fun setUp() = viewModel.load()

    private fun user(id: String) = viewModel.uiState.value.users.first { it.id == id }

    @Test
    fun roleFilter_andCounts() {
        viewModel.onRoleFilterChange(Role.MINISTRY_LEADER)

        val state = viewModel.uiState.value
        assertTrue(state.visibleUsers.all { it.role == Role.MINISTRY_LEADER })
        assertEquals(state.visibleUsers.size, state.roleCounts[Role.MINISTRY_LEADER])
        assertEquals(1, state.roleCounts[Role.ADMINISTRATOR])
    }

    @Test
    fun search_matchesNameEmailOrRole() {
        viewModel.onQueryChange("finance")
        assertEquals(listOf("user-3"), viewModel.uiState.value.visibleUsers.map { it.id })
    }

    @Test
    fun changeRole_updatesUser() {
        viewModel.startEditing(user("user-9"))
        viewModel.onEditRoleChange(Role.FINANCE_OFFICER)
        viewModel.saveRole()

        assertEquals(Role.FINANCE_OFFICER, user("user-9").role)
        assertNull(viewModel.uiState.value.editing)
        assertEquals("Sipho Dlamini is now Finance Officer", viewModel.uiState.value.message)
    }

    @Test
    fun lastAdministrator_cannotBeDemoted() {
        viewModel.startEditing(user("user-1"))
        viewModel.onEditRoleChange(Role.PASTOR)
        viewModel.saveRole()

        assertEquals(Role.ADMINISTRATOR, user("user-1").role)
        assertEquals("The church must have at least one Administrator.", viewModel.uiState.value.editing?.error)
    }

    @Test
    fun ministryLeader_requiresMinistry() {
        viewModel.startEditing(user("user-10"))
        viewModel.onEditRoleChange(Role.MINISTRY_LEADER)
        viewModel.saveRole()

        assertNotNull(viewModel.uiState.value.editing?.error)
        assertEquals(Role.CHURCH_MEMBER, user("user-10").role)

        viewModel.onEditMinistryChange("min-7")
        viewModel.saveRole()

        assertEquals(Role.MINISTRY_LEADER, user("user-10").role)
        assertEquals("min-7", user("user-10").ministryId)
    }

    @Test
    fun cancelEditing_closesDialogWithoutChanges() {
        viewModel.startEditing(user("user-2"))
        viewModel.onEditRoleChange(Role.CHURCH_MEMBER)
        viewModel.cancelEditing()

        assertNull(viewModel.uiState.value.editing)
        assertEquals(Role.PASTOR, user("user-2").role)
    }
}
