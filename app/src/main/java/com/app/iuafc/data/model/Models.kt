package com.app.iuafc.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data models matching the website (`lib/types.ts`).
// @SerialName uses the snake_case column names proposed for the database (issue #1).
// If a column name changes in the schema migration, update it here too.
// Dates are ISO strings ("2026-10-02"), times are "HH:mm".

/** A logged-in user. Linked to Supabase Auth; the password is never stored here. */
@Serializable
data class UserProfile(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val role: Role,
    @SerialName("member_id") val memberId: String? = null,
    @SerialName("ministry_id") val ministryId: String? = null,
)

@Serializable
enum class Gender { Male, Female }

@Serializable
data class Member(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val gender: Gender,
    @SerialName("date_of_birth") val dateOfBirth: String,
    @SerialName("contact_number") val contactNumber: String,
    val email: String,
    val address: String,
    @SerialName("ministry_id") val ministryId: String? = null,
    @SerialName("join_date") val joinDate: String,
)

@Serializable
data class Ministry(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("leader_id") val leaderId: String? = null,
)

@Serializable
data class Event(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("event_date") val date: String,
    @SerialName("event_time") val time: String,
    val venue: String,
    @SerialName("created_by") val createdBy: String,
)

/** A member registered for an event (the website keeps this as `registeredMemberIds`). */
@Serializable
data class EventRegistration(
    @SerialName("event_id") val eventId: String,
    @SerialName("member_id") val memberId: String,
)

@Serializable
enum class AttendanceStatus { Present, Absent }

@Serializable
data class Attendance(
    val id: String,
    @SerialName("member_id") val memberId: String,
    @SerialName("event_id") val eventId: String,
    @SerialName("attendance_date") val attendanceDate: String,
    val status: AttendanceStatus,
)

@Serializable
enum class DonationType { Tithe, Offering, Donation }

@Serializable
enum class PaymentMethod { Cash, Card, Transfer }

@Serializable
data class Donation(
    val id: String,
    @SerialName("member_id") val memberId: String,
    val amount: Double,
    @SerialName("donation_type") val donationType: DonationType,
    @SerialName("payment_method") val paymentMethod: PaymentMethod,
    @SerialName("donation_date") val donationDate: String,
    @SerialName("recorded_by") val recordedBy: String,
    @SerialName("receipt_number") val receiptNumber: String,
)

@Serializable
data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    @SerialName("publish_date") val publishDate: String,
    @SerialName("created_by") val createdBy: String,
)

@Serializable
enum class ActivityStatus { Pending, Approved, Rejected }

@Serializable
data class MinistryActivity(
    val id: String,
    @SerialName("ministry_id") val ministryId: String,
    val title: String,
    val description: String,
    @SerialName("proposed_date") val proposedDate: String,
    val venue: String,
    @SerialName("submitted_by") val submittedBy: String,
    @SerialName("submitted_at") val submittedAt: String,
    val status: ActivityStatus = ActivityStatus.Pending,
    @SerialName("reviewed_by") val reviewedBy: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("review_note") val reviewNote: String? = null,
)
