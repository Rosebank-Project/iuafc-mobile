package com.app.iuafc.data.remote

import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import com.app.iuafc.data.repository.SaveResult
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Rows of the Supabase tables (supabase/migrations). Columns that can be empty in the
 * database are nullable here and turned into the app's models below, so a row with an
 * empty "created by" (e.g. its account was deleted) never crashes the app.
 */

@Serializable
internal data class MemberRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val gender: Gender,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("contact_number") val contactNumber: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("ministry_id") val ministryId: String? = null,
    @SerialName("join_date") val joinDate: String,
) {
    fun toMember() = Member(
        id = id,
        fullName = fullName,
        gender = gender,
        dateOfBirth = dateOfBirth.orEmpty(),
        contactNumber = contactNumber.orEmpty(),
        email = email.orEmpty(),
        address = address.orEmpty(),
        ministryId = ministryId,
        joinDate = joinDate,
    )
}

@Serializable
internal data class MinistryRow(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("leader_id") val leaderId: String? = null,
) {
    fun toMinistry() = Ministry(id = id, name = name, description = description, leaderId = leaderId)
}

@Serializable
internal data class EventRow(
    val id: String,
    val name: String,
    val description: String = "",
    @SerialName("event_date") val date: String,
    @SerialName("event_time") val time: String,
    val venue: String? = null,
    @SerialName("created_by") val createdBy: String? = null,
) {
    fun toEvent() = Event(
        id = id,
        name = name,
        description = description,
        date = date,
        time = time.take(5), // "09:00:00" -> "09:00"
        venue = venue.orEmpty(),
        createdBy = createdBy.orEmpty(),
    )
}

@Serializable
internal data class RegistrationRow(
    @SerialName("event_id") val eventId: String,
    @SerialName("member_id") val memberId: String,
)

@Serializable
internal data class AttendanceRow(
    val id: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("attendance_date") val attendanceDate: String,
    val status: AttendanceStatus,
) {
    fun toAttendance() = Attendance(id, memberId, eventId, attendanceDate, status)
}

@Serializable
internal data class DonationRow(
    val id: String,
    @SerialName("member_id") val memberId: String? = null,
    val amount: Double,
    @SerialName("donation_type") val donationType: DonationType,
    @SerialName("payment_method") val paymentMethod: PaymentMethod,
    @SerialName("donation_date") val donationDate: String,
    @SerialName("recorded_by") val recordedBy: String? = null,
    @SerialName("receipt_number") val receiptNumber: String,
) {
    fun toDonation() = Donation(
        id = id,
        memberId = memberId.orEmpty(),
        amount = amount,
        donationType = donationType,
        paymentMethod = paymentMethod,
        donationDate = donationDate,
        recordedBy = recordedBy.orEmpty(),
        receiptNumber = receiptNumber,
    )
}

@Serializable
internal data class AnnouncementRow(
    val id: String,
    val title: String,
    val message: String,
    @SerialName("publish_date") val publishDate: String,
    @SerialName("created_by") val createdBy: String? = null,
) {
    fun toAnnouncement() = Announcement(id, title, message, publishDate, createdBy.orEmpty())
}

@Serializable
internal data class ActivityRow(
    val id: String,
    @SerialName("ministry_id") val ministryId: String,
    val title: String,
    val description: String = "",
    @SerialName("proposed_date") val proposedDate: String,
    val venue: String = "",
    @SerialName("submitted_by") val submittedBy: String? = null,
    @SerialName("submitted_at") val submittedAt: String,
    val status: ActivityStatus = ActivityStatus.Pending,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("review_note") val reviewNote: String? = null,
) {
    fun toActivity() = MinistryActivity(
        id = id,
        ministryId = ministryId,
        title = title,
        description = description,
        proposedDate = proposedDate,
        venue = venue,
        submittedBy = submittedBy.orEmpty(),
        submittedAt = submittedAt,
        status = status,
        reviewedBy = reviewedBy,
        reviewedAt = reviewedAt,
        reviewNote = reviewNote,
    )
}

/** A row of `profile_directory()`: every account's name and role, without the email. */
@Serializable
internal data class DirectoryRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val role: Role,
) {
    fun toUser() = UserProfile(id = id, fullName = fullName, email = "", role = role)
}

/** Empty strings become `null`, so optional columns are cleared instead of saved as "". */
internal fun String?.orNullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/** Turns Supabase errors into messages the user understands. */
internal fun friendlyDataMessage(e: Exception): String = when (e) {
    is PostgrestRestException -> when (e.code) {
        "42501" -> "You don't have permission to do this."
        "23505" -> "This record already exists."
        "23503" -> "This record is linked to something that no longer exists. Refresh and try again."
        "23514" -> "Some of the values are not allowed. Check the form and try again."
        else -> e.error
    }
    is HttpRequestException -> "No internet connection. Check your connection and try again."
    is NoPermissionException -> e.message.orEmpty()
    else -> "Something went wrong. Please try again."
}

/** Runs a change and turns any failure into a [SaveResult.Error] with a friendly message. */
internal suspend fun saveCatching(block: suspend () -> Unit): SaveResult = try {
    block()
    SaveResult.Success
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    SaveResult.Error(friendlyDataMessage(e))
}

/** Thrown when Row Level Security silently hides the row a change was meant for. */
internal class NoPermissionException : Exception("You don't have permission to change this record, or it no longer exists.")
