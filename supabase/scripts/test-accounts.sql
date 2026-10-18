-- =============================================================================
-- Give the test accounts their roles (run by hand, in the Supabase SQL Editor)
-- Not a migration: accounts and passwords are created in the dashboard and must
-- never be stored in the repository.
--
-- 1. Supabase -> Authentication -> Users -> Add user -> Create new user, once for
--    each email below, with "Auto Confirm User" ticked. Share the passwords with
--    the group privately (WhatsApp), never in GitHub.
-- 2. Run this script. It sets each account's name and role and links it to its
--    member record / ministry from the seed data (20261018170000_seed_data.sql).
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

-- Check: the test accounts with their role, member record and ministry
select p.full_name, p.email, p.role, mem.full_name as member, min.name as ministry
from public.profiles p
left join public.members mem on mem.id = p.member_id
left join public.ministries min on min.id = p.ministry_id
order by p.role, p.full_name;
