package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import kotlinx.coroutines.delay

interface AttendanceRepository {
    suspend fun getAttendance(): List<Attendance>
    suspend fun getAttendanceForEvent(eventId: String): List<Attendance>
    suspend fun saveAttendance(records: List<Attendance>)
    suspend fun getAttendanceForMember(memberId: String): List<Attendance>
}

class MockAttendanceRepository(private val delayMillis: Long = 300) : AttendanceRepository {
    private val attendanceList = MockData.attendance.toMutableList()

    override suspend fun getAttendance(): List<Attendance> {
        delay(delayMillis)
        return attendanceList.toList()
    }

    override suspend fun getAttendanceForEvent(eventId: String): List<Attendance> {
        delay(delayMillis)
        return attendanceList.filter { it.eventId == eventId }
    }

    override suspend fun saveAttendance(records: List<Attendance>) {
        delay(delayMillis)
        val eventId = records.firstOrNull()?.eventId
        if (eventId != null) {
            attendanceList.removeAll { it.eventId == eventId && records.any { r -> r.memberId == it.memberId } }
            attendanceList.addAll(records)
        }
    }

    override suspend fun getAttendanceForMember(memberId: String): List<Attendance> {
        delay(delayMillis)
        return attendanceList.filter { it.memberId == memberId }
    }
}