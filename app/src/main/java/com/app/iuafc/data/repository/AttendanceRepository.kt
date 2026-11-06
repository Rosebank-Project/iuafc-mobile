package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import kotlinx.coroutines.delay

/**
 * Attendance captured per event: one Present or Absent record per member and event.
 * The Supabase version is [SupabaseAttendanceRepository].
 */
interface AttendanceRepository {
    /** Every attendance record (used by the reports and the dashboard). */
    suspend fun getAttendance(): List<Attendance>

    /** The records already captured for one event, to pre-fill the attendance screen. */
    suspend fun getAttendanceForEvent(eventId: String): List<Attendance>

    /** Saves the records of one event. Saving a member again replaces their previous record. */
    suspend fun saveAttendance(records: List<Attendance>)

    /** One member's attendance history (My Attendance screen). */
    suspend fun getAttendanceForMember(memberId: String): List<Attendance>
}

/**
 * In-memory attendance for demo mode. [delayMillis] imitates a network call so the loading
 * state can be seen; the unit tests pass 0.
 */
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
            // Same rule as the database's unique (event_id, member_id): replace, don't duplicate
            attendanceList.removeAll { it.eventId == eventId && records.any { r -> r.memberId == it.memberId } }
            attendanceList.addAll(records)
        }
    }

    override suspend fun getAttendanceForMember(memberId: String): List<Attendance> {
        delay(delayMillis)
        return attendanceList.filter { it.memberId == memberId }
    }
}