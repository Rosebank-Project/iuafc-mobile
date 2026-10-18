

-- ==========================================
-- PHASE 4: SEED DATA
-- ==========================================

-- 1. Insert Ministries
INSERT INTO public.ministries (id, name, description) VALUES
('11111111-1111-1111-1111-111111111111', 'Choir Ministry', 'Leads the church in worship through song.'),
('22222222-2222-2222-2222-222222222222', 'Youth Ministry', 'Engages and mentors the youth of the church.'),
('33333333-3333-3333-3333-333333333333', 'Ushering Ministry', 'Welcomes members and maintains order during services.');

-- 2. Insert Members
INSERT INTO public.members (id, full_name, email, contact_number, gender, ministry_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'John Williams', 'john@example.com', '0821112222', 'Male', '11111111-1111-1111-1111-111111111111'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Grace Adeyemi', 'grace@example.com', '0833334444', 'Female', '11111111-1111-1111-1111-111111111111'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Daniel Okele', 'daniel@example.com', '0845556666', 'Male', '22222222-2222-2222-2222-222222222222'),
('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Esther Bello', 'esther@example.com', '0857778888', 'Female', '22222222-2222-2222-2222-222222222222');

-- 3. Insert Events
INSERT INTO public.events (id, name, description, event_date, event_time, venue) VALUES
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Sunday Service', 'Weekly Sunday worship and word.', CURRENT_DATE + INTERVAL '2 days', '09:00:00', 'Main Sanctuary'),
('ffffffff-ffff-ffff-ffff-ffffffffffff', 'Youth Conference', 'Annual gathering for youth empowerment.', CURRENT_DATE + INTERVAL '10 days', '14:00:00', 'Fellowship Hall'),
('99999999-9999-9999-9999-999999999999', 'Choir Rehearsal', 'Preparation for Sunday service.', CURRENT_DATE + INTERVAL '1 day', '16:00:00', 'Main Sanctuary');

-- 4. Insert Announcements
INSERT INTO public.announcements (title, content) VALUES
('Welcome to the New Church Management System', 'We are excited to launch this new digital platform to serve you better. Please update your profile and explore the features.'),
('Choir Rehearsal This Saturday', 'All choir members are invited to rehearsal on Saturday at 4:00 PM in the main sanctuary.');

-- 5. Insert Donations
INSERT INTO public.donations (member_id, amount, donation_type, payment_method) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 25000.00, 'Tithe', 'Transfer'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 5000.00, 'Offering', 'Cash'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 50000.00, 'Donation', 'Card');