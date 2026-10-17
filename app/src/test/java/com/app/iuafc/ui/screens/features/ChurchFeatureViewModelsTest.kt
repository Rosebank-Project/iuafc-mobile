package com.app.iuafc.ui.screens.features

import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.MockChurchFeaturesRepository
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.ui.screens.activities.ActivitiesViewModel
import com.app.iuafc.ui.screens.donations.DonationsViewModel
import com.app.iuafc.ui.screens.ministries.MinistriesViewModel
import com.app.iuafc.ui.screens.reports.ReportsViewModel
import com.app.iuafc.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChurchFeatureViewModelsTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val features = MockChurchFeaturesRepository(delayMillis = 0)
    private val members = MockMemberRepository(delayMillis = 0)

    @Test
    fun donationViewModel_rejectsZeroAmount_andRecordsValidDonationWithReceipt() = runTest {
        val viewModel = DonationsViewModel(
            UserProfile("user-3", "Finance Officer", "finance@iuafc.org", Role.FINANCE_OFFICER),
            features,
            members,
        )
        viewModel.load()
        val originalCount = features.getDonations().size

        viewModel.record("mem-1", "0", DonationType.Tithe, PaymentMethod.Cash, "2026-10-10")
        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(originalCount, features.getDonations().size)

        val futureDate = java.time.LocalDate.now().plusDays(5).toString()
        viewModel.record("mem-1", "100", DonationType.Tithe, PaymentMethod.Cash, futureDate)
        assertEquals("Donation date cannot be in the future", viewModel.uiState.value.errorMessage)
        assertEquals(originalCount, features.getDonations().size)

        viewModel.record("mem-1", "125.50", DonationType.Tithe, PaymentMethod.Transfer, "2026-10-10")
        assertEquals(originalCount + 1, features.getDonations().size)
        assertEquals("RCP-1013", viewModel.uiState.value.donations.last().receiptNumber)
        assertEquals(1, viewModel.uiState.value.saveVersion)
    }

    @Test
    fun churchMemberDonationHistory_isLimitedToLinkedMember() {
        val viewModel = DonationsViewModel(
            UserProfile("user-6", "Grace Adeyemi", "grace@iuafc.org", Role.CHURCH_MEMBER, memberId = "mem-1"),
            features,
            members,
        )
        viewModel.load()

        val visible = viewModel.uiState.value.donations
        assertFalse(viewModel.canRecord)
        assertTrue(visible.isNotEmpty())
        assertTrue(visible.all { it.memberId == "mem-1" })
    }

    @Test
    fun ministryLeaderCannotSubmitForAnotherMinistry_orForPastDate() {
        val viewModel = ActivitiesViewModel(
            UserProfile(
                "user-4",
                "Choir Leader",
                "choirleader@iuafc.org",
                Role.MINISTRY_LEADER,
                ministryId = "min-1",
            ),
            features,
        )
        viewModel.load()
        val initialCount = viewModel.uiState.value.activities.size

        viewModel.submit("min-2", "Youth night", "A youth gathering.", "2026-11-12", "Church hall")

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(initialCount, viewModel.uiState.value.activities.size)
        assertFalse(viewModel.canReview)

        val pastDate = java.time.LocalDate.now().minusDays(5).toString()
        viewModel.submit("min-1", "Past event", "A past gathering.", pastDate, "Church hall")
        assertEquals("Proposed date cannot be in the past.", viewModel.uiState.value.errorMessage)
        assertEquals(initialCount, viewModel.uiState.value.activities.size)
    }

    @Test
    fun pastorCanReviewPendingActivities() = runTest {
        val viewModel = ActivitiesViewModel(
            UserProfile("user-2", "Pastor", "pastor@iuafc.org", Role.PASTOR),
            features,
        )
        viewModel.load()
        val pending = viewModel.uiState.value.activities.first { it.status == ActivityStatus.Pending }

        viewModel.review(pending, ActivityStatus.Approved, "Approved.")

        assertTrue(viewModel.uiState.value.activities.first { it.id == pending.id }.status == ActivityStatus.Approved)
        assertEquals(1, viewModel.uiState.value.saveVersion)
    }

    @Test
    fun ministriesViewModel_savesNewMinistry_andHandlesDuplicateName() = runTest {
        val users = com.app.iuafc.data.repository.MockUserRepository(delayMillis = 0)
        val viewModel = MinistriesViewModel(features, users)
        viewModel.load()
        val initialCount = viewModel.uiState.value.ministries.size

        viewModel.save(com.app.iuafc.data.model.Ministry(id = "", name = "Choir Ministry", description = "Duplicate Choir"))
        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.save(com.app.iuafc.data.model.Ministry(id = "", name = "Media Outreach", description = "New Ministry"))
        assertEquals(initialCount + 1, viewModel.uiState.value.ministries.size)
    }

    @Test
    fun reportsViewModel_loadsSummaryDataSuccessfully() = runTest {
        val viewModel = ReportsViewModel(features, members)
        viewModel.load()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.members.isNotEmpty())
        assertTrue(state.ministries.isNotEmpty())
        assertTrue(state.donations.isNotEmpty())
    }
}
