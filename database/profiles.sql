-- =============================================================================
-- profiles: role and details of every user account (issue #20)
--
-- Accounts are created by Supabase Auth (email/password or Google). A trigger
-- creates the matching profile with the "Church Member" role. Only
-- Administrators can change roles, through the set_user_role() function.
--
-- Run once in Supabase: SQL Editor -> New query -> paste -> Run.
-- The rest of the schema (members, ministries, events...) is in schema.sql (#1).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Table
-- -----------------------------------------------------------------------------
create table if not exists public.profiles (
    id          uuid primary key references auth.users (id) on delete cascade,
    full_name   text        not null,
    email       text        not null,
    role        text        not null default 'Church Member'
                check (role in ('Administrator', 'Pastor', 'Finance Officer', 'Ministry Leader', 'Church Member')),
    -- Links to members(id) / ministries(id). The foreign keys are added in
    -- schema.sql (#1) once those tables exist.
    member_id   uuid,
    ministry_id uuid,
    created_at  timestamptz not null default now(),
    updated_at  timestamptz not null default now()
);

comment on table public.profiles is 'Role and details of each user account (one row per auth.users row).';

create index if not exists profiles_role_idx on public.profiles (role);

-- -----------------------------------------------------------------------------
-- Create a profile automatically when a user registers (email or Google)
-- -----------------------------------------------------------------------------
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id, full_name, email)
    values (
        new.id,
        coalesce(
            nullif(new.raw_user_meta_data ->> 'full_name', ''),
            nullif(new.raw_user_meta_data ->> 'name', ''),
            split_part(new.email, '@', 1)
        ),
        new.email
    );
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- -----------------------------------------------------------------------------
-- Helpers used by the security policies
-- (security definer so they can read profiles without triggering RLS again)
-- -----------------------------------------------------------------------------
create or replace function public.current_user_role()
returns text
language sql
stable
security definer
set search_path = ''
as $$
    select role from public.profiles where id = auth.uid();
$$;

create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select coalesce(public.current_user_role() = 'Administrator', false);
$$;

-- -----------------------------------------------------------------------------
-- Row Level Security
-- -----------------------------------------------------------------------------
alter table public.profiles enable row level security;

drop policy if exists "Users can read their own profile" on public.profiles;
create policy "Users can read their own profile"
    on public.profiles for select
    to authenticated
    using (id = (select auth.uid()));

drop policy if exists "Administrators and Pastors can read all profiles" on public.profiles;
create policy "Administrators and Pastors can read all profiles"
    on public.profiles for select
    to authenticated
    using ((select public.current_user_role()) in ('Administrator', 'Pastor'));

drop policy if exists "Users can update their own name" on public.profiles;
create policy "Users can update their own name"
    on public.profiles for update
    to authenticated
    using (id = (select auth.uid()))
    with check (id = (select auth.uid()));

-- Users may only change their own full_name directly. Roles change through
-- set_user_role(), which checks that the caller is an Administrator.
revoke insert, update, delete on public.profiles from anon, authenticated;
grant select on public.profiles to authenticated;
grant update (full_name, updated_at) on public.profiles to authenticated;

-- -----------------------------------------------------------------------------
-- set_user_role(): the only way to change a role (called by the app and the
-- website as supabase.rpc('set_user_role', {...}))
-- -----------------------------------------------------------------------------
create or replace function public.set_user_role(
    target_user     uuid,
    new_role        text,
    new_member_id   uuid default null,
    new_ministry_id uuid default null
)
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    target_role text;
begin
    if not public.is_admin() then
        raise exception 'Only Administrators can change roles.' using errcode = '42501';
    end if;

    if new_role not in ('Administrator', 'Pastor', 'Finance Officer', 'Ministry Leader', 'Church Member') then
        raise exception 'Invalid role: %', new_role using errcode = '22023';
    end if;

    select role into target_role from public.profiles where id = target_user;
    if not found then
        raise exception 'User not found.' using errcode = 'P0002';
    end if;

    if target_role = 'Administrator' and new_role <> 'Administrator'
       and (select count(*) from public.profiles where role = 'Administrator') <= 1 then
        raise exception 'The church must have at least one Administrator.' using errcode = 'P0001';
    end if;

    update public.profiles
    set role        = new_role,
        member_id   = case when new_role = 'Church Member'   then new_member_id   end,
        ministry_id = case when new_role = 'Ministry Leader' then new_ministry_id end,
        updated_at  = now()
    where id = target_user;
end;
$$;

revoke execute on function public.set_user_role(uuid, text, uuid, uuid) from public, anon;
grant execute on function public.set_user_role(uuid, text, uuid, uuid) to authenticated;

-- -----------------------------------------------------------------------------
-- Profiles for accounts that already existed before this script
-- -----------------------------------------------------------------------------
insert into public.profiles (id, full_name, email)
select
    u.id,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'full_name', ''),
        nullif(u.raw_user_meta_data ->> 'name', ''),
        split_part(u.email, '@', 1)
    ),
    u.email
from auth.users u
on conflict (id) do nothing;
