package com.app.iuafc.data.mock

import com.app.iuafc.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Keeps the sample data consistent, like the database constraints will (issue #1). */
class MockDataTest {

    @Test
    fun everyTable_hasAtLeastTenRecords() {
        mapOf(
            "ministries" to MockData.ministries.size,
            "members" to MockData.members.size,
            "users" to MockData.users.size,
            "events" to MockData.events.size,
            "event_registrations" to MockData.eventRegistrations.size,
            "attendance" to MockData.attendance.size,
            "donations" to MockData.donations.size,
            "announcements" to MockData.announcements.size,
            "ministry_activities" to MockData.activities.size,
        ).forEach { (table, count) -> assertTrue("$table has only $count records", count >= 10) }
    }

    @Test
    fun ids_areUnique() {
        listOf(
            MockData.ministries.map { it.id },
            MockData.members.map { it.id },
            MockData.users.map { it.id },
            MockData.events.map { it.id },
            MockData.attendance.map { it.id },
            MockData.donations.map { it.id },
            MockData.announcements.map { it.id },
            MockData.activities.map { it.id },
            MockData.donations.map { it.receiptNumber },
            MockData.eventRegistrations.map { it.eventId to it.memberId },
        ).forEach { ids -> assertEquals(ids.size, ids.toSet().size) }
    }

    @Test
    fun foreignKeys_pointToExistingRecords() {
        val ministryIds = MockData.ministries.map { it.id }.toSet()
        val memberIds = MockData.members.map { it.id }.toSet()
        val userIds = MockData.users.map { it.id }.toSet()
        val eventIds = MockData.events.map { it.id }.toSet()

        MockData.members.forEach { assertTrue(it.ministryId == null || it.ministryId in ministryIds) }
        MockData.ministries.forEach { assertTrue(it.leaderId == null || it.leaderId in userIds) }
        MockData.users.forEach {
            assertTrue(it.memberId == null || it.memberId in memberIds)
            assertTrue(it.ministryId == null || it.ministryId in ministryIds)
        }
        MockData.events.forEach { assertTrue(it.createdBy in userIds) }
        MockData.eventRegistrations.forEach { assertTrue(it.eventId in eventIds && it.memberId in memberIds) }
        MockData.attendance.forEach { assertTrue(it.eventId in eventIds && it.memberId in memberIds) }
        MockData.donations.forEach { assertTrue(it.memberId in memberIds && it.recordedBy in userIds) }
        MockData.announcements.forEach { assertTrue(it.createdBy in userIds) }
        MockData.activities.forEach {
            assertTrue(it.ministryId in ministryIds && it.submittedBy in userIds)
            assertTrue(it.reviewedBy == null || it.reviewedBy in userIds)
        }
    }

    @Test
    fun members_passTheAppValidation() {
        MockData.members.forEach { member ->
            assertNull(member.fullName, Validators.email(member.email))
            assertNull(member.fullName, Validators.phone(member.contactNumber))
            assertNull(member.fullName, Validators.pastDate(member.dateOfBirth))
            assertNull(member.fullName, Validators.date(member.joinDate))
        }
    }

    @Test
    fun donations_havePositiveAmounts() {
        MockData.donations.forEach { assertTrue(it.amount > 0) }
    }
}
