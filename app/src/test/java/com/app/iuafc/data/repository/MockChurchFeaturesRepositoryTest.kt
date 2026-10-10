package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.PaymentMethod
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockChurchFeaturesRepositoryTest {

    private val repository = MockChurchFeaturesRepository(delayMillis = 0)

    @Test
    fun saveMinistry_addsAndUpdatesMinistry_andRejectsDuplicateNames() = runBlocking {
        val newMinistry = Ministry("", "Care Ministry", "Supports church members")
        assertEquals(SaveResult.Success, repository.saveMinistry(newMinistry))

        val created = repository.getMinistries().first { it.name == "Care Ministry" }
        assertTrue(created.id.isNotBlank())
        assertEquals(
            SaveResult.Error("A ministry with this name already exists."),
            repository.saveMinistry(Ministry("", " care ministry ", "Duplicate")),
        )
        assertEquals(
            SaveResult.Success,
            repository.saveMinistry(created.copy(description = "Updated description")),
        )
        assertEquals("Updated description", repository.getMinistries().first { it.id == created.id }.description)
    }

    @Test
    fun saveActivity_assignsId_andReviewUpdatesStatusAndNote() = runBlocking {
        val proposal = MinistryActivity(
            id = "",
            ministryId = "min-1",
            title = "Community visit",
            description = "Visit members in the community.",
            proposedDate = "2026-11-15",
            venue = "Church hall",
            submittedBy = "user-4",
            submittedAt = "2026-10-10T10:00:00Z",
        )
        assertEquals(SaveResult.Success, repository.saveActivity(proposal))
        val created = repository.getActivities().first { it.title == proposal.title }
        assertTrue(created.id.isNotBlank())

        val reviewed = created.copy(
            status = ActivityStatus.Approved,
            reviewedBy = "user-2",
            reviewedAt = "2026-10-10T11:00:00Z",
            reviewNote = "Approved.",
        )
        assertEquals(SaveResult.Success, repository.reviewActivity(reviewed))
        val result = repository.getActivities().first { it.id == created.id }
        assertEquals(ActivityStatus.Approved, result.status)
        assertEquals("Approved.", result.reviewNote)
    }

    @Test
    fun saveDonation_assignsUniqueReceiptNumber() = runBlocking {
        val donation = Donation(
            id = "",
            memberId = MockData.members.first().id,
            amount = 25.0,
            donationType = DonationType.Offering,
            paymentMethod = PaymentMethod.Cash,
            donationDate = "2026-10-10",
            recordedBy = "user-3",
            receiptNumber = "",
        )
        assertEquals(SaveResult.Success, repository.saveDonation(donation))

        val saved = repository.getDonations().first { it.amount == 25.0 }
        assertTrue(saved.id.isNotBlank())
        assertEquals("RCP-1013", saved.receiptNumber)
    }
}
