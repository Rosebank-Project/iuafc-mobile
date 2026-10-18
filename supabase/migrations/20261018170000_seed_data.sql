-- =============================================================================
-- Seed data (issue #2): realistic sample records for every church table.
--
-- Same records as the app's sample data (data/mock/MockData.kt), which is based on
-- the website's lib/seed-data.ts, so the app shows the same church before and after
-- switching to Supabase:
--   10 ministries, 12 members, 10 events, 15 event registrations, 12 attendance records,
--   12 donations, 10 announcements, 10 ministry activities.
--
-- Accounts (profiles) are not seeded here: they come from Supabase Auth, and
-- passwords must never be in the repository. Create the test accounts in the
-- dashboard and run supabase/scripts/test-accounts.sql (see docs/database.md).
--
-- created_by / recorded_by / submitted_by point to the first Administrator (or the
-- Finance Officer / the ministry's leader when those accounts exist). They stay
-- empty if no account exists yet.
--
-- Fixed ids (10000000-... ministries, 20000000-... members, ...) and
-- "on conflict do nothing" make the script safe to run again.
-- =============================================================================

do $$
declare
    admin_id   uuid := (select id from public.profiles where role = 'Administrator' order by created_at limit 1);
    finance_id uuid := coalesce(
        (select id from public.profiles where role = 'Finance Officer' order by created_at limit 1),
        admin_id);
begin
    insert into public.ministries (id, name, description) values
        ('10000000-0000-4000-8000-000000000001', 'Choir Ministry', 'Leads worship through song'),
        ('10000000-0000-4000-8000-000000000002', 'Youth Ministry', 'Engages and disciples young people'),
        ('10000000-0000-4000-8000-000000000003', 'Women''s Ministry', 'Fellowship, prayer and support for the women of the church'),
        ('10000000-0000-4000-8000-000000000004', 'Men''s Ministry', 'Brotherhood, mentorship and service for the men of the church'),
        ('10000000-0000-4000-8000-000000000005', 'Sunday School', 'Bible teaching for children aged 4 to 12'),
        ('10000000-0000-4000-8000-000000000006', 'Ushering Ministry', 'Welcomes visitors and keeps order during services'),
        ('10000000-0000-4000-8000-000000000007', 'Prayer Ministry', 'Intercession for the church and the community'),
        ('10000000-0000-4000-8000-000000000008', 'Evangelism Ministry', 'Outreach and sharing the gospel in the community'),
        ('10000000-0000-4000-8000-000000000009', 'Media Ministry', 'Sound, projection and live streaming of services'),
        ('10000000-0000-4000-8000-000000000010', 'Welfare Ministry', 'Supports members and families in need')
    on conflict do nothing;

    insert into public.members (id, full_name, gender, date_of_birth, contact_number, email, address, ministry_id, join_date) values
        ('20000000-0000-4000-8000-000000000001', 'Grace Adeyemi', 'Female', '1992-04-12', '0821234567', 'grace@iuafc.org', '12 Faith Avenue, Soweto, Johannesburg', '10000000-0000-4000-8000-000000000001', '2020-03-15'),
        ('20000000-0000-4000-8000-000000000002', 'Daniel Okeke', 'Male', '1988-09-23', '0832345678', 'daniel@iuafc.org', '5 Mercy Street, Rosebank, Johannesburg', '10000000-0000-4000-8000-000000000001', '2019-11-02'),
        ('20000000-0000-4000-8000-000000000003', 'Esther Bello', 'Female', '2001-07-08', '0743456789', 'esther@iuafc.org', '21 Hope Crescent, Randburg', '10000000-0000-4000-8000-000000000002', '2021-06-20'),
        ('20000000-0000-4000-8000-000000000004', 'Samuel Johnson', 'Male', '1995-12-30', '0614567890', 'samuel@iuafc.org', '8 Praise Road, Midrand', '10000000-0000-4000-8000-000000000002', '2022-01-10'),
        ('20000000-0000-4000-8000-000000000005', 'Ruth Eze', 'Female', '1985-02-17', '0725678901', 'ruth@iuafc.org', '33 Glory Lane, Sandton', '10000000-0000-4000-8000-000000000001', '2018-08-05'),
        ('20000000-0000-4000-8000-000000000006', 'Thabo Nkosi', 'Male', '1979-05-04', '0836789012', 'thabo@iuafc.org', '14 Grace Street, Alexandra, Johannesburg', '10000000-0000-4000-8000-000000000004', '2017-02-19'),
        ('20000000-0000-4000-8000-000000000007', 'Lerato Mokoena', 'Female', '1990-10-21', '0797890123', 'lerato@iuafc.org', '7 Zion Close, Soweto, Johannesburg', '10000000-0000-4000-8000-000000000003', '2019-05-12'),
        ('20000000-0000-4000-8000-000000000008', 'Sipho Dlamini', 'Male', '2003-01-15', '0658901234', 'sipho@iuafc.org', '42 Jordan Road, Diepkloof, Johannesburg', '10000000-0000-4000-8000-000000000009', '2023-03-26'),
        ('20000000-0000-4000-8000-000000000009', 'Nomvula Khumalo', 'Female', '1968-08-30', '0829012345', 'nomvula@iuafc.org', '3 Bethel Avenue, Roodepoort', '10000000-0000-4000-8000-000000000007', '2015-09-06'),
        ('20000000-0000-4000-8000-000000000010', 'Kagiso Molefe', 'Male', '1998-03-09', '0710123456', 'kagiso@iuafc.org', '19 Canaan Street, Fourways', '10000000-0000-4000-8000-000000000008', '2022-08-14'),
        ('20000000-0000-4000-8000-000000000011', 'Zanele Mahlangu', 'Female', '1987-06-27', '0841234560', 'zanele@iuafc.org', '26 Shiloh Drive, Germiston', '10000000-0000-4000-8000-000000000005', '2020-11-29'),
        ('20000000-0000-4000-8000-000000000012', 'Bongani Zulu', 'Male', '1983-11-11', '0762345601', 'bongani@iuafc.org', '11 Eden Park, Kempton Park', '10000000-0000-4000-8000-000000000006', '2016-04-03')
    on conflict do nothing;

    insert into public.events (id, name, description, event_date, event_time, venue, created_by) values
        ('30000000-0000-4000-8000-000000000001', 'Sunday Service', 'Weekly Sunday worship and word', '2026-05-17', '09:00', 'Main Sanctuary', admin_id),
        ('30000000-0000-4000-8000-000000000002', 'Youth Conference', 'Annual gathering for youth empowerment', '2026-06-14', '16:00', 'Fellowship Hall', admin_id),
        ('30000000-0000-4000-8000-000000000003', 'Heritage Day Thanksgiving', 'Thanksgiving service celebrating our heritage', '2026-09-24', '10:00', 'Main Sanctuary', admin_id),
        ('30000000-0000-4000-8000-000000000004', 'Sunday Service', 'Weekly Sunday worship and word', '2026-10-04', '09:00', 'Main Sanctuary', admin_id),
        ('30000000-0000-4000-8000-000000000005', 'Midweek Bible Study', 'Study of the book of Acts', '2026-10-07', '18:30', 'Fellowship Hall', admin_id),
        ('30000000-0000-4000-8000-000000000006', 'Women''s Prayer Breakfast', 'Breakfast, worship and prayer for the women of the church', '2026-10-17', '08:00', 'Fellowship Hall', admin_id),
        ('30000000-0000-4000-8000-000000000007', 'Community Outreach', 'Food parcels and prayer in Alexandra', '2026-10-24', '09:00', 'Alexandra Community Hall', admin_id),
        ('30000000-0000-4000-8000-000000000008', 'Men''s Fellowship Braai', 'Fellowship and mentorship for the men of the church', '2026-11-07', '13:00', 'Church Grounds', admin_id),
        ('30000000-0000-4000-8000-000000000009', 'Choir Anniversary Concert', 'Celebrating 10 years of the choir ministry', '2026-11-21', '17:00', 'Main Sanctuary', admin_id),
        ('30000000-0000-4000-8000-000000000010', 'Christmas Carol Service', 'Carols, nativity play and the Christmas message', '2026-12-20', '18:00', 'Main Sanctuary', admin_id)
    on conflict do nothing;

    insert into public.event_registrations (event_id, member_id) values
        ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001'),
        ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000002'),
        ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000003'),
        ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000003'),
        ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000004'),
        ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000008'),
        ('30000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000001'),
        ('30000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000006'),
        ('30000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000009'),
        ('30000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000007'),
        ('30000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000011'),
        ('30000000-0000-4000-8000-000000000007', '20000000-0000-4000-8000-000000000010'),
        ('30000000-0000-4000-8000-000000000007', '20000000-0000-4000-8000-000000000012'),
        ('30000000-0000-4000-8000-000000000009', '20000000-0000-4000-8000-000000000001'),
        ('30000000-0000-4000-8000-000000000009', '20000000-0000-4000-8000-000000000005')
    on conflict do nothing;

    insert into public.attendance (id, member_id, event_id, attendance_date, status, recorded_by) values
        ('40000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', '2026-05-17', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001', '2026-05-17', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000005', '30000000-0000-4000-8000-000000000001', '2026-05-17', 'Absent', admin_id),
        ('40000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000003', '30000000-0000-4000-8000-000000000002', '2026-06-14', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000005', '20000000-0000-4000-8000-000000000004', '30000000-0000-4000-8000-000000000002', '2026-06-14', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000008', '30000000-0000-4000-8000-000000000002', '2026-06-14', 'Absent', admin_id),
        ('40000000-0000-4000-8000-000000000007', '20000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000008', '20000000-0000-4000-8000-000000000006', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000009', '20000000-0000-4000-8000-000000000007', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000010', '20000000-0000-4000-8000-000000000009', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Absent', admin_id),
        ('40000000-0000-4000-8000-000000000011', '20000000-0000-4000-8000-000000000011', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Present', admin_id),
        ('40000000-0000-4000-8000-000000000012', '20000000-0000-4000-8000-000000000012', '30000000-0000-4000-8000-000000000003', '2026-09-24', 'Present', admin_id)
    on conflict do nothing;

    insert into public.donations (id, member_id, amount, donation_type, payment_method, donation_date, recorded_by, receipt_number) values
        ('50000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001', 2500.00, 'Tithe', 'Transfer', '2026-05-03', finance_id, 'RCP-1001'),
        ('50000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000002', 500.00, 'Offering', 'Cash', '2026-05-03', finance_id, 'RCP-1002'),
        ('50000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000005', 5000.00, 'Donation', 'Card', '2026-04-26', finance_id, 'RCP-1003'),
        ('50000000-0000-4000-8000-000000000004', '20000000-0000-4000-8000-000000000006', 1800.00, 'Tithe', 'Transfer', '2026-06-07', finance_id, 'RCP-1004'),
        ('50000000-0000-4000-8000-000000000005', '20000000-0000-4000-8000-000000000007', 300.00, 'Offering', 'Cash', '2026-06-14', finance_id, 'RCP-1005'),
        ('50000000-0000-4000-8000-000000000006', '20000000-0000-4000-8000-000000000001', 2500.00, 'Tithe', 'Transfer', '2026-07-05', finance_id, 'RCP-1006'),
        ('50000000-0000-4000-8000-000000000007', '20000000-0000-4000-8000-000000000009', 1000.00, 'Donation', 'Card', '2026-07-19', finance_id, 'RCP-1007'),
        ('50000000-0000-4000-8000-000000000008', '20000000-0000-4000-8000-000000000012', 750.00, 'Tithe', 'Cash', '2026-08-02', finance_id, 'RCP-1008'),
        ('50000000-0000-4000-8000-000000000009', '20000000-0000-4000-8000-000000000010', 200.00, 'Offering', 'Cash', '2026-08-16', finance_id, 'RCP-1009'),
        ('50000000-0000-4000-8000-000000000010', '20000000-0000-4000-8000-000000000001', 2500.00, 'Tithe', 'Transfer', '2026-09-06', finance_id, 'RCP-1010'),
        ('50000000-0000-4000-8000-000000000011', '20000000-0000-4000-8000-000000000011', 1200.00, 'Tithe', 'Transfer', '2026-09-06', finance_id, 'RCP-1011'),
        ('50000000-0000-4000-8000-000000000012', '20000000-0000-4000-8000-000000000003', 150.00, 'Offering', 'Card', '2026-09-24', finance_id, 'RCP-1012')
    on conflict do nothing;

    insert into public.announcements (id, title, message, publish_date, created_by) values
        ('60000000-0000-4000-8000-000000000001', 'Welcome to the New Church Management System', 'We are excited to launch our new digital platform to serve you better. Please update your profile and explore the features.', '2026-05-08', admin_id),
        ('60000000-0000-4000-8000-000000000002', 'Choir Rehearsal This Saturday', 'All choir members are invited to rehearsal on Saturday at 4:00 PM in the main sanctuary.', '2026-05-09', admin_id),
        ('60000000-0000-4000-8000-000000000003', 'Youth Conference Registration Open', 'Registration for the annual Youth Conference is now open. Register in the Events section of the app.', '2026-05-20', admin_id),
        ('60000000-0000-4000-8000-000000000004', 'Mid-Year Prayer Week', 'Join us every evening at 18:30 from 15 to 19 June for a week of prayer.', '2026-06-08', admin_id),
        ('60000000-0000-4000-8000-000000000005', 'Building Fund Update', 'Thank you for your generous giving. We have reached 60% of the target for the new fellowship hall.', '2026-07-12', admin_id),
        ('60000000-0000-4000-8000-000000000006', 'Heritage Day Thanksgiving Service', 'Come dressed in your traditional attire for our Heritage Day thanksgiving service on 24 September.', '2026-09-14', admin_id),
        ('60000000-0000-4000-8000-000000000007', 'New Midweek Bible Study', 'Starting 7 October we study the book of Acts every Wednesday at 18:30 in the Fellowship Hall.', '2026-09-28', admin_id),
        ('60000000-0000-4000-8000-000000000008', 'Volunteers Needed for Community Outreach', 'We need volunteers to pack and deliver food parcels in Alexandra on 24 October. Speak to the Evangelism Ministry.', '2026-09-30', admin_id),
        ('60000000-0000-4000-8000-000000000009', 'Women''s Prayer Breakfast', 'All women are invited to the prayer breakfast on 17 October at 08:00. Please register in the app.', '2026-10-01', admin_id),
        ('60000000-0000-4000-8000-000000000010', 'Choir Anniversary Concert', 'Save the date: the choir celebrates 10 years with a concert on 21 November at 17:00.', '2026-10-02', admin_id)
    on conflict do nothing;

    insert into public.ministry_activities (id, ministry_id, title, description, proposed_date, venue, submitted_by, submitted_at, status, reviewed_by, reviewed_at, review_note) values
        ('70000000-0000-4000-8000-000000000001', '10000000-0000-4000-8000-000000000001', 'Choir Robe Renewal Drive', 'Request approval to fundraise for new choir robes ahead of the anniversary service.', '2026-06-01', 'Fellowship Hall', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000001' limit 1), admin_id), '2026-05-09T10:30:00Z', 'Pending', null, null, null),
        ('70000000-0000-4000-8000-000000000002', '10000000-0000-4000-8000-000000000002', 'Youth Outreach Weekend', 'Two-day evangelism outreach in nearby community with the youth ministry team.', '2026-06-20', 'Community Center', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000002' limit 1), admin_id), '2026-05-05T08:15:00Z', 'Approved', admin_id, '2026-05-06T09:00:00Z', 'Approved. Coordinate with the welfare team for logistics.'),
        ('70000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003', 'Women''s Prayer Breakfast', 'Prayer breakfast for the women of the church.', '2026-10-17', 'Fellowship Hall', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000003' limit 1), admin_id), '2026-09-01T09:00:00Z', 'Approved', admin_id, '2026-09-03T11:00:00Z', 'Approved. Please share the menu with the welfare team.'),
        ('70000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004', 'Men''s Fellowship Braai', 'Fellowship braai with a short talk on fatherhood.', '2026-11-07', 'Church Grounds', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000004' limit 1), admin_id), '2026-09-10T14:20:00Z', 'Approved', admin_id, '2026-09-12T08:45:00Z', 'Approved.'),
        ('70000000-0000-4000-8000-000000000005', '10000000-0000-4000-8000-000000000001', 'Choir Anniversary Concert', 'Concert celebrating 10 years of the choir ministry.', '2026-11-21', 'Main Sanctuary', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000001' limit 1), admin_id), '2026-09-15T16:00:00Z', 'Approved', admin_id, '2026-09-18T10:00:00Z', 'Approved. Book the sound team early.'),
        ('70000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000002', 'Youth Movie Night', 'Movie night with a discussion on faith and friendship.', '2026-10-30', 'Fellowship Hall', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000002' limit 1), admin_id), '2026-09-20T12:00:00Z', 'Rejected', admin_id, '2026-09-22T09:30:00Z', 'The hall is booked that evening. Please propose another date.'),
        ('70000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000002', 'Youth Movie Night (new date)', 'Movie night with a discussion on faith and friendship.', '2026-11-13', 'Fellowship Hall', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000002' limit 1), admin_id), '2026-09-23T08:00:00Z', 'Pending', null, null, null),
        ('70000000-0000-4000-8000-000000000008', '10000000-0000-4000-8000-000000000003', 'Mothers'' Day Tea', 'Tea and fellowship for the mothers of the church.', '2027-05-08', 'Fellowship Hall', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000003' limit 1), admin_id), '2026-09-25T10:15:00Z', 'Pending', null, null, null),
        ('70000000-0000-4000-8000-000000000009', '10000000-0000-4000-8000-000000000004', 'Men''s Hiking Retreat', 'Day hike and devotion at the Magaliesberg.', '2026-12-05', 'Magaliesberg', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000004' limit 1), admin_id), '2026-09-28T07:40:00Z', 'Pending', null, null, null),
        ('70000000-0000-4000-8000-000000000010', '10000000-0000-4000-8000-000000000001', 'Christmas Carol Rehearsals', 'Extra rehearsals for the Christmas Carol Service.', '2026-12-12', 'Main Sanctuary', coalesce((select id from public.profiles where role = 'Ministry Leader' and ministry_id = '10000000-0000-4000-8000-000000000001' limit 1), admin_id), '2026-10-01T18:00:00Z', 'Pending', null, null, null)
    on conflict do nothing;
end
$$;

-- New receipts continue after the seeded ones (RCP-1013, ...).
select setval(
    'public.donation_receipt_seq',
    greatest(
        (select coalesce(max(substring(receipt_number from 5)::int), 1000) from public.donations),
        (select last_value from public.donation_receipt_seq)
    )
);
