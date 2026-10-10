package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import kotlinx.coroutines.delay
import java.util.Locale

/** Repository for church features whose Supabase tables have not been added yet. */
interface ChurchFeaturesRepository {
    suspend fun getMinistries(): List<Ministry>
    suspend fun saveMinistry(ministry: Ministry): SaveResult
    suspend fun getActivities(): List<MinistryActivity>
    suspend fun saveActivity(activity: MinistryActivity): SaveResult
    suspend fun reviewActivity(activity: MinistryActivity): SaveResult
    suspend fun getDonations(): List<Donation>
    suspend fun saveDonation(donation: Donation): SaveResult
}

/** In-memory repository. Updates are shared for the lifetime of the application process. */
class MockChurchFeaturesRepository(
    private val delayMillis: Long = 250,
) : ChurchFeaturesRepository {
    private val ministries = MockData.ministries.toMutableList()
    private val activities = MockData.activities.toMutableList()
    private val donations = MockData.donations.toMutableList()
    private var nextMinistryId = ministries.size + 1
    private var nextActivityId = activities.size + 1
    private var nextDonationId = donations.size + 1

    override suspend fun getMinistries(): List<Ministry> {
        delay(delayMillis)
        return synchronized(this) { ministries.toList() }
    }

    override suspend fun saveMinistry(ministry: Ministry): SaveResult {
        delay(delayMillis)
        synchronized(this) {
            if (ministries.any { it.id != ministry.id && it.name.equals(ministry.name.trim(), true) }) {
                return SaveResult.Error("A ministry with this name already exists.")
            }
            val index = ministries.indexOfFirst { it.id == ministry.id }
            if (index == -1) {
                ministries += ministry.copy(id = "min-${nextMinistryId++}", name = ministry.name.trim())
            } else {
                ministries[index] = ministry.copy(name = ministry.name.trim())
            }
        }
        return SaveResult.Success
    }

    override suspend fun getActivities(): List<MinistryActivity> {
        delay(delayMillis)
        return synchronized(this) { activities.toList() }
    }

    override suspend fun saveActivity(activity: MinistryActivity): SaveResult {
        delay(delayMillis)
        synchronized(this) {
            activities += activity.copy(id = "act-${nextActivityId++}")
        }
        return SaveResult.Success
    }

    override suspend fun reviewActivity(activity: MinistryActivity): SaveResult {
        delay(delayMillis)
        synchronized(this) {
            val index = activities.indexOfFirst { it.id == activity.id }
            if (index == -1) return SaveResult.Error("Activity not found.")
            activities[index] = activity
        }
        return SaveResult.Success
    }

    override suspend fun getDonations(): List<Donation> {
        delay(delayMillis)
        return synchronized(this) { donations.toList() }
    }

    override suspend fun saveDonation(donation: Donation): SaveResult {
        delay(delayMillis)
        synchronized(this) {
            val nextReceipt = (donations.mapNotNull { it.receiptNumber.removePrefix("RCP-").toIntOrNull() }
                .maxOrNull() ?: 1000) + 1
            donations += donation.copy(
                id = "don-${nextDonationId++}",
                receiptNumber = String.format(Locale.ROOT, "RCP-%04d", nextReceipt),
            )
        }
        return SaveResult.Success
    }
}
