-- =============================================================================
-- Give the test accounts their roles (run by hand, in the Supabase SQL Editor)
-- Not a migration: accounts and passwords are created in the dashboard and must
-- never be stored in the repository.
--
-- 1. Supabase -> Authentication -> Users -> Add user -> Create new user, once for
--    each email below, with "Auto Confirm User" ticked. Share the passwords with
--    the group privately (WhatsApp), never in GitHub.
-- 2. Run this script. It sets each account's name and role, links it to its
--    member record / ministry from the seed data (20261018170000_seed_data.sql),
--    and makes the test accounts the authors of the seeded events, announcements,
--    donations and activities (the seed leaves them empty when no account exists yet).
-- Safe to run again; accounts that don't exist yet are skipped.
-- =============================================================================

with test_accounts (email, full_name, role, member_id, ministry_id) as (
    values
        ('admin@iuafc.org',       'Pastor John Williams',  'Administrator',   null,                                   null),
        ('pastor@iuafc.org',      'Rev. Michael Adeyemi',  'Pastor',          null,                                   null),
        ('finance@iuafc.org',     'Sister Mary Okafor',    'Finance Officer', null,                                   null),
        ('choirleader@iuafc.org', 'Brother Paul Adewale',  'Ministry Leader', null,                                   '10000000-0000-4000-8000-000000000001'),
        ('youthleader@iuafc.org', 'Sister Deborah Nwosu',  'Ministry Leader', null,                                   '10000000-0000-4000-8000-000000000002'),
        ('lerato@iuafc.org',      'Lerato Mokoena',        'Ministry Leader', '20000000-0000-4000-8000-000000000007', '10000000-0000-4000-8000-000000000003'),
        ('thabo@iuafc.org',       'Thabo Nkosi',           'Ministry Leader', '20000000-0000-4000-8000-000000000006', '10000000-0000-4000-8000-000000000004'),
        ('grace@iuafc.org',       'Grace Adeyemi',         'Church Member',   '20000000-0000-4000-8000-000000000001', null),
        ('sipho@iuafc.org',       'Sipho Dlamini',         'Church Member',   '20000000-0000-4000-8000-000000000008', null),
        ('nomvula@iuafc.org',     'Nomvula Khumalo',       'Church Member',   '20000000-0000-4000-8000-000000000009', null)
)
update public.profiles p
set full_name   = t.full_name,
    role        = t.role,
    member_id   = t.member_id::uuid,
    ministry_id = t.ministry_id::uuid,
    updated_at  = now()
from test_accounts t
where p.email = t.email;

-- Each Ministry Leader account leads its ministry.
update public.ministries m
set leader_id = p.id
from public.profiles p
where p.role = 'Ministry Leader' and p.ministry_id = m.id;

-- Authors of the seed records (same as the app's sample data). Only empty fields are filled.
do $$
declare
    admin_id   uuid := (select id from public.profiles where email = 'admin@iuafc.org');
    pastor_id  uuid := (select id from public.profiles where email = 'pastor@iuafc.org');
    finance_id uuid := (select id from public.profiles where email = 'finance@iuafc.org');
begin
    update public.events
    set created_by = case when id in ('30000000-0000-4000-8000-000000000003',
                                      '30000000-0000-4000-8000-000000000005',
                                      '30000000-0000-4000-8000-000000000008')
                          then pastor_id else admin_id end
    where created_by is null;

    update public.announcements
    set created_by = case when id in ('60000000-0000-4000-8000-000000000003',
                                      '60000000-0000-4000-8000-000000000004',
                                      '60000000-0000-4000-8000-000000000006',
                                      '60000000-0000-4000-8000-000000000007')
                          then pastor_id else admin_id end
    where created_by is null;

    update public.donations  set recorded_by = finance_id where recorded_by is null;
    update public.attendance set recorded_by = admin_id   where recorded_by is null;

    -- Activities are proposed by their ministry's leader and reviewed by the Pastor
    -- (one by the Administrator).
    update public.ministry_activities a
    set submitted_by = p.id
    from public.profiles p
    where a.submitted_by is null and p.role = 'Ministry Leader' and p.ministry_id = a.ministry_id;

    update public.ministry_activities
    set reviewed_by = case when id = '70000000-0000-4000-8000-000000000004' then admin_id else pastor_id end
    where reviewed_by is null and status <> 'Pending';
end
$$;

-- Check: the test accounts with their role, member record and ministry
select p.full_name, p.email, p.role, mem.full_name as member, min.name as ministry
from public.profiles p
left join public.members mem on mem.id = p.member_id
left join public.ministries min on min.id = p.ministry_id
order by p.role, p.full_name;
