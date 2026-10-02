package com.app.iuafc.data.mock

import com.app.iuafc.data.model.ActivityStatus
import com.app.iuafc.data.model.Announcement
import com.app.iuafc.data.model.Attendance
import com.app.iuafc.data.model.AttendanceStatus
import com.app.iuafc.data.model.Donation
import com.app.iuafc.data.model.DonationType
import com.app.iuafc.data.model.Event
import com.app.iuafc.data.model.EventRegistration
import com.app.iuafc.data.model.Gender
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import com.app.iuafc.data.model.MinistryActivity
import com.app.iuafc.data.model.PaymentMethod
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile

/**
 * Sample data used until the Supabase tables are ready (issues #1 and #2).
 *
 * Based on `lib/seed-data.ts` of the website (same IDs and names for the first records),
 * extended to at least 10 records per table as the brief requires, with South African
 * phone numbers and addresses. Rolph can reuse it for `database/seed.sql`.
 *
 * Use it in a mock repository, e.g. `class MockEventRepository : EventRepository`, and swap
 * it for the Supabase repository later without touching the screen.
 */
object MockData {

    val ministries = listOf(
        Ministry("min-1", "Choir Ministry", "Leads worship through song", leaderId = "user-4"),
        Ministry("min-2", "Youth Ministry", "Engages and disciples young people", leaderId = "user-5"),
        Ministry("min-3", "Women's Ministry", "Fellowship, prayer and support for the women of the church", leaderId = "user-7"),
        Ministry("min-4", "Men's Ministry", "Brotherhood, mentorship and service for the men of the church", leaderId = "user-8"),
        Ministry("min-5", "Sunday School", "Bible teaching for children aged 4 to 12"),
        Ministry("min-6", "Ushering Ministry", "Welcomes visitors and keeps order during services"),
        Ministry("min-7", "Prayer Ministry", "Intercession for the church and the community"),
        Ministry("min-8", "Evangelism Ministry", "Outreach and sharing the gospel in the community"),
        Ministry("min-9", "Media Ministry", "Sound, projection and live streaming of services"),
        Ministry("min-10", "Welfare Ministry", "Supports members and families in need"),
    )

    val members = listOf(
        Member("mem-1", "Grace Adeyemi", Gender.Female, "1992-04-12", "0821234567", "grace@iuafc.org", "12 Faith Avenue, Soweto, Johannesburg", "min-1", "2020-03-15"),
        Member("mem-2", "Daniel Okeke", Gender.Male, "1988-09-23", "0832345678", "daniel@iuafc.org", "5 Mercy Street, Rosebank, Johannesburg", "min-1", "2019-11-02"),
        Member("mem-3", "Esther Bello", Gender.Female, "2001-07-08", "0743456789", "esther@iuafc.org", "21 Hope Crescent, Randburg", "min-2", "2021-06-20"),
        Member("mem-4", "Samuel Johnson", Gender.Male, "1995-12-30", "0614567890", "samuel@iuafc.org", "8 Praise Road, Midrand", "min-2", "2022-01-10"),
        Member("mem-5", "Ruth Eze", Gender.Female, "1985-02-17", "0725678901", "ruth@iuafc.org", "33 Glory Lane, Sandton", "min-1", "2018-08-05"),
        Member("mem-6", "Thabo Nkosi", Gender.Male, "1979-05-04", "0836789012", "thabo@iuafc.org", "14 Grace Street, Alexandra, Johannesburg", "min-4", "2017-02-19"),
        Member("mem-7", "Lerato Mokoena", Gender.Female, "1990-10-21", "0797890123", "lerato@iuafc.org", "7 Zion Close, Soweto, Johannesburg", "min-3", "2019-05-12"),
        Member("mem-8", "Sipho Dlamini", Gender.Male, "2003-01-15", "0658901234", "sipho@iuafc.org", "42 Jordan Road, Diepkloof, Johannesburg", "min-9", "2023-03-26"),
        Member("mem-9", "Nomvula Khumalo", Gender.Female, "1968-08-30", "0829012345", "nomvula@iuafc.org", "3 Bethel Avenue, Roodepoort", "min-7", "2015-09-06"),
        Member("mem-10", "Kagiso Molefe", Gender.Male, "1998-03-09", "0710123456", "kagiso@iuafc.org", "19 Canaan Street, Fourways", "min-8", "2022-08-14"),
        Member("mem-11", "Zanele Mahlangu", Gender.Female, "1987-06-27", "0841234560", "zanele@iuafc.org", "26 Shiloh Drive, Germiston", "min-5", "2020-11-29"),
        Member("mem-12", "Bongani Zulu", Gender.Male, "1983-11-11", "0762345601", "bongani@iuafc.org", "11 Eden Park, Kempton Park", "min-6", "2016-04-03"),
    )

    /** App users. In Supabase these will come from Auth + the `profiles` table. */
    val users = listOf(
        UserProfile("user-1", "Pastor John Williams", "admin@iuafc.org", Role.ADMINISTRATOR),
        UserProfile("user-2", "Rev. Michael Adeyemi", "pastor@iuafc.org", Role.PASTOR),
        UserProfile("user-3", "Sister Mary Okafor", "finance@iuafc.org", Role.FINANCE_OFFICER),
        UserProfile("user-4", "Brother Paul Adewale", "choirleader@iuafc.org", Role.MINISTRY_LEADER, ministryId = "min-1"),
        UserProfile("user-5", "Sister Deborah Nwosu", "youthleader@iuafc.org", Role.MINISTRY_LEADER, ministryId = "min-2"),
        UserProfile("user-6", "Grace Adeyemi", "grace@iuafc.org", Role.CHURCH_MEMBER, memberId = "mem-1"),
        UserProfile("user-7", "Lerato Mokoena", "lerato@iuafc.org", Role.MINISTRY_LEADER, memberId = "mem-7", ministryId = "min-3"),
        UserProfile("user-8", "Thabo Nkosi", "thabo@iuafc.org", Role.MINISTRY_LEADER, memberId = "mem-6", ministryId = "min-4"),
        UserProfile("user-9", "Sipho Dlamini", "sipho@iuafc.org", Role.CHURCH_MEMBER, memberId = "mem-8"),
        UserProfile("user-10", "Nomvula Khumalo", "nomvula@iuafc.org", Role.CHURCH_MEMBER, memberId = "mem-9"),
    )

    val events = listOf(
        Event("evt-1", "Sunday Service", "Weekly Sunday worship and word", "2026-05-17", "09:00", "Main Sanctuary", "user-1"),
        Event("evt-2", "Youth Conference", "Annual gathering for youth empowerment", "2026-06-14", "16:00", "Fellowship Hall", "user-1"),
        Event("evt-3", "Heritage Day Thanksgiving", "Thanksgiving service celebrating our heritage", "2026-09-24", "10:00", "Main Sanctuary", "user-2"),
        Event("evt-4", "Sunday Service", "Weekly Sunday worship and word", "2026-10-04", "09:00", "Main Sanctuary", "user-1"),
        Event("evt-5", "Midweek Bible Study", "Study of the book of Acts", "2026-10-07", "18:30", "Fellowship Hall", "user-2"),
        Event("evt-6", "Women's Prayer Breakfast", "Breakfast, worship and prayer for the women of the church", "2026-10-17", "08:00", "Fellowship Hall", "user-1"),
        Event("evt-7", "Community Outreach", "Food parcels and prayer in Alexandra", "2026-10-24", "09:00", "Alexandra Community Hall", "user-1"),
        Event("evt-8", "Men's Fellowship Braai", "Fellowship and mentorship for the men of the church", "2026-11-07", "13:00", "Church Grounds", "user-2"),
        Event("evt-9", "Choir Anniversary Concert", "Celebrating 10 years of the choir ministry", "2026-11-21", "17:00", "Main Sanctuary", "user-1"),
        Event("evt-10", "Christmas Carol Service", "Carols, nativity play and the Christmas message", "2026-12-20", "18:00", "Main Sanctuary", "user-1"),
    )

    val eventRegistrations = listOf(
        EventRegistration("evt-1", "mem-1"), EventRegistration("evt-1", "mem-2"), EventRegistration("evt-1", "mem-3"),
        EventRegistration("evt-2", "mem-3"), EventRegistration("evt-2", "mem-4"), EventRegistration("evt-2", "mem-8"),
        EventRegistration("evt-4", "mem-1"), EventRegistration("evt-4", "mem-6"), EventRegistration("evt-4", "mem-9"),
        EventRegistration("evt-6", "mem-7"), EventRegistration("evt-6", "mem-11"),
        EventRegistration("evt-7", "mem-10"), EventRegistration("evt-7", "mem-12"),
        EventRegistration("evt-9", "mem-1"), EventRegistration("evt-9", "mem-5"),
    )

    val attendance = listOf(
        Attendance("att-1", "mem-1", "evt-1", "2026-05-17", AttendanceStatus.Present),
        Attendance("att-2", "mem-2", "evt-1", "2026-05-17", AttendanceStatus.Present),
        Attendance("att-3", "mem-5", "evt-1", "2026-05-17", AttendanceStatus.Absent),
        Attendance("att-4", "mem-3", "evt-2", "2026-06-14", AttendanceStatus.Present),
        Attendance("att-5", "mem-4", "evt-2", "2026-06-14", AttendanceStatus.Present),
        Attendance("att-6", "mem-8", "evt-2", "2026-06-14", AttendanceStatus.Absent),
        Attendance("att-7", "mem-1", "evt-3", "2026-09-24", AttendanceStatus.Present),
        Attendance("att-8", "mem-6", "evt-3", "2026-09-24", AttendanceStatus.Present),
        Attendance("att-9", "mem-7", "evt-3", "2026-09-24", AttendanceStatus.Present),
        Attendance("att-10", "mem-9", "evt-3", "2026-09-24", AttendanceStatus.Absent),
        Attendance("att-11", "mem-11", "evt-3", "2026-09-24", AttendanceStatus.Present),
        Attendance("att-12", "mem-12", "evt-3", "2026-09-24", AttendanceStatus.Present),
    )

    val donations = listOf(
        Donation("don-1", "mem-1", 2500.00, DonationType.Tithe, PaymentMethod.Transfer, "2026-05-03", "user-3", "RCP-1001"),
        Donation("don-2", "mem-2", 500.00, DonationType.Offering, PaymentMethod.Cash, "2026-05-03", "user-3", "RCP-1002"),
        Donation("don-3", "mem-5", 5000.00, DonationType.Donation, PaymentMethod.Card, "2026-04-26", "user-3", "RCP-1003"),
        Donation("don-4", "mem-6", 1800.00, DonationType.Tithe, PaymentMethod.Transfer, "2026-06-07", "user-3", "RCP-1004"),
        Donation("don-5", "mem-7", 300.00, DonationType.Offering, PaymentMethod.Cash, "2026-06-14", "user-3", "RCP-1005"),
        Donation("don-6", "mem-1", 2500.00, DonationType.Tithe, PaymentMethod.Transfer, "2026-07-05", "user-3", "RCP-1006"),
        Donation("don-7", "mem-9", 1000.00, DonationType.Donation, PaymentMethod.Card, "2026-07-19", "user-3", "RCP-1007"),
        Donation("don-8", "mem-12", 750.00, DonationType.Tithe, PaymentMethod.Cash, "2026-08-02", "user-3", "RCP-1008"),
        Donation("don-9", "mem-10", 200.00, DonationType.Offering, PaymentMethod.Cash, "2026-08-16", "user-3", "RCP-1009"),
        Donation("don-10", "mem-1", 2500.00, DonationType.Tithe, PaymentMethod.Transfer, "2026-09-06", "user-3", "RCP-1010"),
        Donation("don-11", "mem-11", 1200.00, DonationType.Tithe, PaymentMethod.Transfer, "2026-09-06", "user-3", "RCP-1011"),
        Donation("don-12", "mem-3", 150.00, DonationType.Offering, PaymentMethod.Card, "2026-09-24", "user-3", "RCP-1012"),
    )

    val announcements = listOf(
        Announcement("ann-1", "Welcome to the New Church Management System", "We are excited to launch our new digital platform to serve you better. Please update your profile and explore the features.", "2026-05-08", "user-1"),
        Announcement("ann-2", "Choir Rehearsal This Saturday", "All choir members are invited to rehearsal on Saturday at 4:00 PM in the main sanctuary.", "2026-05-09", "user-1"),
        Announcement("ann-3", "Youth Conference Registration Open", "Registration for the annual Youth Conference is now open. Register in the Events section of the app.", "2026-05-20", "user-2"),
        Announcement("ann-4", "Mid-Year Prayer Week", "Join us every evening at 18:30 from 15 to 19 June for a week of prayer.", "2026-06-08", "user-2"),
        Announcement("ann-5", "Building Fund Update", "Thank you for your generous giving. We have reached 60% of the target for the new fellowship hall.", "2026-07-12", "user-1"),
        Announcement("ann-6", "Heritage Day Thanksgiving Service", "Come dressed in your traditional attire for our Heritage Day thanksgiving service on 24 September.", "2026-09-14", "user-2"),
        Announcement("ann-7", "New Midweek Bible Study", "Starting 7 October we study the book of Acts every Wednesday at 18:30 in the Fellowship Hall.", "2026-09-28", "user-2"),
        Announcement("ann-8", "Volunteers Needed for Community Outreach", "We need volunteers to pack and deliver food parcels in Alexandra on 24 October. Speak to the Evangelism Ministry.", "2026-09-30", "user-1"),
        Announcement("ann-9", "Women's Prayer Breakfast", "All women are invited to the prayer breakfast on 17 October at 08:00. Please register in the app.", "2026-10-01", "user-1"),
        Announcement("ann-10", "Choir Anniversary Concert", "Save the date: the choir celebrates 10 years with a concert on 21 November at 17:00.", "2026-10-02", "user-1"),
    )

    val activities = listOf(
        MinistryActivity("act-1", "min-1", "Choir Robe Renewal Drive", "Request approval to fundraise for new choir robes ahead of the anniversary service.", "2026-06-01", "Fellowship Hall", "user-4", "2026-05-09T10:30:00Z"),
        MinistryActivity("act-2", "min-2", "Youth Outreach Weekend", "Two-day evangelism outreach in nearby community with the youth ministry team.", "2026-06-20", "Community Center", "user-5", "2026-05-05T08:15:00Z", ActivityStatus.Approved, "user-2", "2026-05-06T09:00:00Z", "Approved. Coordinate with the welfare team for logistics."),
        MinistryActivity("act-3", "min-3", "Women's Prayer Breakfast", "Prayer breakfast for the women of the church.", "2026-10-17", "Fellowship Hall", "user-7", "2026-09-01T09:00:00Z", ActivityStatus.Approved, "user-2", "2026-09-03T11:00:00Z", "Approved. Please share the menu with the welfare team."),
        MinistryActivity("act-4", "min-4", "Men's Fellowship Braai", "Fellowship braai with a short talk on fatherhood.", "2026-11-07", "Church Grounds", "user-8", "2026-09-10T14:20:00Z", ActivityStatus.Approved, "user-1", "2026-09-12T08:45:00Z", "Approved."),
        MinistryActivity("act-5", "min-1", "Choir Anniversary Concert", "Concert celebrating 10 years of the choir ministry.", "2026-11-21", "Main Sanctuary", "user-4", "2026-09-15T16:00:00Z", ActivityStatus.Approved, "user-2", "2026-09-18T10:00:00Z", "Approved. Book the sound team early."),
        MinistryActivity("act-6", "min-2", "Youth Movie Night", "Movie night with a discussion on faith and friendship.", "2026-10-30", "Fellowship Hall", "user-5", "2026-09-20T12:00:00Z", ActivityStatus.Rejected, "user-2", "2026-09-22T09:30:00Z", "The hall is booked that evening. Please propose another date."),
        MinistryActivity("act-7", "min-2", "Youth Movie Night (new date)", "Movie night with a discussion on faith and friendship.", "2026-11-13", "Fellowship Hall", "user-5", "2026-09-23T08:00:00Z"),
        MinistryActivity("act-8", "min-3", "Mothers' Day Tea", "Tea and fellowship for the mothers of the church.", "2027-05-08", "Fellowship Hall", "user-7", "2026-09-25T10:15:00Z"),
        MinistryActivity("act-9", "min-4", "Men's Hiking Retreat", "Day hike and devotion at the Magaliesberg.", "2026-12-05", "Magaliesberg", "user-8", "2026-09-28T07:40:00Z"),
        MinistryActivity("act-10", "min-1", "Christmas Carol Rehearsals", "Extra rehearsals for the Christmas Carol Service.", "2026-12-12", "Main Sanctuary", "user-4", "2026-10-01T18:00:00Z"),
    )

    fun ministryName(ministryId: String?): String? = ministries.firstOrNull { it.id == ministryId }?.name

    fun memberName(memberId: String): String? = members.firstOrNull { it.id == memberId }?.fullName
}
