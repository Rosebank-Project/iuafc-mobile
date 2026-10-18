package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import kotlinx.coroutines.delay

interface AttendanceRepository {
    suspend fun getAttendanceForEvent(eventId: String): List<Attendance>
    suspend fun saveAttendance(records: List<Attendance>)
}

class MockAttendanceRepository : AttendanceRepository {
    // We use a mutable list so we can save new records while the app is running
    private val attendanceList = MockData.attendance.toMutableList()

    override suspend fun getAttendanceForEvent(eventId: String): List<Attendance> {
        delay(300)
        return attendanceList.filter { it.eventId == eventId }
    }

    override suspend fun saveAttendance(records: List<Attendance>) {
        delay(300)
        // Remove existing records for these members/event to prevent duplicates
        val eventId = records.firstOrNull()?.eventId
        if (eventId != null) {
            attendanceList.removeAll { it.eventId == eventId && records.any { r -> r.memberId == it.memberId } }
            attendanceList.addAll(records)
        }
    }
}