package com.app.iuafc.ui.screens.features

import com.app.iuafc.data.model.*
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.SaveResult
import com.app.iuafc.ui.screens.attendance.AttendanceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeEventRepo : EventRepository {
        val events = mutableListOf<Event>()
        override suspend fun getEvents(): List<Event> = events
        override suspend fun createEvent(event: Event) {}
        override suspend fun updateEvent(event: Event) {}
        override suspend fun deleteEvent(eventId: String) {}
        override suspend fun registerForEvent(eventId: String, memberId: String) {}
        override suspend fun unregisterFromEvent(eventId: String, memberId: String) {}
        override suspend fun getRegisteredEventIds(memberId: String): List<String> = emptyList()
    }

    private class FakeMemberRepo : MemberRepository {
        val membersList = mutableListOf<Member>()
        val ministriesList = mutableListOf<Ministry>()

        override suspend fun getMembers(): List<Member> = membersList
        override suspend fun getMember(id: String): Member? = membersList.find { it.id == id }
        override suspend fun saveMember(member: Member): SaveResult = SaveResult.Success
        override suspend fun deleteMember(id: String): SaveResult = SaveResult.Success
        override suspend fun getMinistries(): List<Ministry> = ministriesList
    }

    private class FakeAttendanceRepo : AttendanceRepository {
        val savedRecords = mutableListOf<Attendance>()
        override suspend fun getAttendanceForEvent(eventId: String): List<Attendance> = savedRecords.filter { it.eventId == eventId }
        override suspend fun saveAttendance(records: List<Attendance>) {
            savedRecords.addAll(records)
        }
        override suspend fun getAttendanceForMember(memberId: String): List<Attendance> = emptyList()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveAttendance stores Present and Absent statuses correctly`() = runTest {
        val fakeEventRepo = FakeEventRepo()
        val fakeMemberRepo = FakeMemberRepo()
        val fakeAttendanceRepo = FakeAttendanceRepo()

        // 1. Add the event and member to our fake data
        fakeEventRepo.events.add(Event("event-1", "Test Event", "Desc", "2026-10-10", "10:00", "Hall", "user-1"))
        fakeMemberRepo.membersList.add(Member("mem-1", "John Doe", Gender.Male, "1990-01-01", "123", "a@b.com", "Addr", null, "2020-01-01"))

        val adminUser = UserProfile("user-1", "Admin", "admin@test.com", Role.ADMINISTRATOR)
        val viewModel = AttendanceViewModel(fakeEventRepo, fakeMemberRepo, fakeAttendanceRepo, adminUser)

        // 2. Let the ViewModel finish its initial `init` load
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Select the event and WAIT for it to load the members into the map
        viewModel.selectEvent("event-1")
        testDispatcher.scheduler.advanceUntilIdle()

        // 4. Now toggle the status (the map is populated now)
        viewModel.toggleStatus("mem-1", AttendanceStatus.Present)
        viewModel.saveAttendance()
        testDispatcher.scheduler.advanceUntilIdle()

        // 5. Assert the save worked
        assertEquals(1, fakeAttendanceRepo.savedRecords.size)
        assertEquals("mem-1", fakeAttendanceRepo.savedRecords[0].memberId)
        assertEquals(AttendanceStatus.Present, fakeAttendanceRepo.savedRecords[0].status)
        assertEquals("2026-10-10", fakeAttendanceRepo.savedRecords[0].attendanceDate)
    }
}