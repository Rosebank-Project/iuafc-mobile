package com.app.iuafc.ui.screens

import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.ui.screens.events.EventsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // Fake Repository to control what the test sees
    private class FakeEventRepository : EventRepository {
        val eventsList = mutableListOf<Event>()
        val registeredIds = mutableListOf<String>()

        override suspend fun getEvents(): List<Event> = eventsList
        override suspend fun createEvent(event: Event) { eventsList.add(event) }
        override suspend fun updateEvent(event: Event) {}
        override suspend fun deleteEvent(eventId: String) {
            eventsList.removeAll { it.id == eventId }
        }
        override suspend fun registerForEvent(eventId: String, memberId: String) {
            registeredIds.add(eventId)
        }
        override suspend fun unregisterFromEvent(eventId: String, memberId: String) {
            registeredIds.remove(eventId)
        }
        override suspend fun getRegisteredEventIds(memberId: String): List<String> = registeredIds
    }

    @Before
    fun setUp() {
        // Tell coroutines to run instantly for tests
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadEvents successfully loads events into state`() = runTest {
        // 1. Setup
        val fakeRepo = FakeEventRepository()
        fakeRepo.eventsList.add(Event("1", "Test Event", "Desc", "2026-10-10", "10:00", "Hall", "user-1"))

        val adminUser = UserProfile("user-1", "Admin", "admin@test.com", Role.ADMINISTRATOR)
        val viewModel = EventsViewModel(fakeRepo, adminUser)

        // 2. Execute
        testDispatcher.scheduler.advanceUntilIdle() // Wait for coroutines to finish

        // 3. Assert
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(1, state.events.size)
        assertEquals("Test Event", state.events[0].name)
    }

    @Test
    fun `toggleRegistration adds event to registered list for church member`() = runTest {
        // 1. Setup
        val fakeRepo = FakeEventRepository()
        fakeRepo.eventsList.add(Event("1", "Test Event", "Desc", "2026-10-10", "10:00", "Hall", "user-1"))

        val memberUser = UserProfile("user-2", "Member", "member@test.com", Role.CHURCH_MEMBER, memberId = "mem-1")
        val viewModel = EventsViewModel(fakeRepo, memberUser)

        // 2. Execute
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleRegistration("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Assert
        val state = viewModel.uiState.value
        assertTrue(state.registeredEventIds.contains("1"))
        assertTrue(fakeRepo.registeredIds.contains("1"))
    }
}