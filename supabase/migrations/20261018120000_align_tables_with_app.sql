-- =============================================================================
-- Align the tables from 20261003130000 and 20261003140000 with the app.
--
-- The app's models (app/.../data/model/Models.kt) and the website's types
-- (lib/types.ts) read these exact column names and values. Without these
-- changes the app cannot load donations, attendance, announcements or ministry
-- activities from Supabase.
--
-- The tables were created empty, so renaming and adding columns is safe.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- donations: receipt number, and only the donation types the app knows
-- -----------------------------------------------------------------------------
create sequence if not exists public.donation_receipt_seq start 1001;
-- The app records donations as a signed-in user, so that role must be able to use it.
grant usage on sequence public.donation_receipt_seq to authenticated;

-- Each donation gets the next receipt number (RCP-1001, RCP-1002, ...).
alter table public.donations
    add column receipt_number text not null unique
        default ('RCP-' || nextval('public.donation_receipt_seq'));

update public.donations set donation_type = 'Donation' where donation_type = 'Building Fund';
alter table public.donations drop constraint if exists donations_donation_type_check;
alter table public.donations
    add constraint donations_donation_type_check
        check (donation_type in ('Tithe', 'Offering', 'Donation'));

-- Pastors see donations in the Donations and Reports screens (read only).
drop policy if exists "Pastors can read donations" on public.donations;
create policy "Pastors can read donations"
    on public.donations for select
    to authenticated
    using ((select public.current_user_role()) = 'Pastor');

-- -----------------------------------------------------------------------------
-- attendance: date of the attendance, and only Present / Absent
-- -----------------------------------------------------------------------------
alter table public.attendance
    add column attendance_date date not null default current_date;

update public.attendance set status = 'Present' where status = 'Late';
alter table public.attendance drop constraint if exists attendance_status_check;
alter table public.attendance
    add constraint attendance_status_check
        check (status in ('Present', 'Absent'));

alter table public.attendance
    alter column event_id set not null,
    alter column member_id set not null;

-- -----------------------------------------------------------------------------
-- announcements: same names as the app (message, created_by, publish_date)
-- -----------------------------------------------------------------------------
alter table public.announcements rename column content to message;
alter table public.announcements rename column author_id to created_by;
alter table public.announcements
    add column publish_date date not null default current_date;

-- -----------------------------------------------------------------------------
-- ministry_activities: venue and the approval workflow
--   A Ministry Leader proposes an activity for their ministry (status Pending);
--   an Administrator or Pastor approves or rejects it with an optional note.
-- -----------------------------------------------------------------------------
alter table public.ministry_activities rename column activity_date to proposed_date;
alter table public.ministry_activities rename column created_by to submitted_by;

update public.ministry_activities set description = '' where description is null;
alter table public.ministry_activities
    alter column description set default '',
    alter column description set not null,
    alter column ministry_id set not null;

alter table public.ministry_activities
    add column venue        text        not null default '',
    add column submitted_at timestamptz not null default now(),
    add column status       text        not null default 'Pending'
        check (status in ('Pending', 'Approved', 'Rejected')),
    add column reviewed_by  uuid references public.profiles(id) on delete set null,
    add column reviewed_at  timestamptz,
    add column review_note  text;

-- Replace "any leader can change any activity" with the app's rules.
drop policy if exists "Activities write for Leaders" on public.ministry_activities;

drop policy if exists "Leaders propose activities for their ministry" on public.ministry_activities;
create policy "Leaders propose activities for their ministry"
    on public.ministry_activities for insert
    to authenticated
    with check (
        (select public.current_user_role()) = 'Ministry Leader'
        and ministry_id = (select ministry_id from public.profiles where id = (select auth.uid()))
        and submitted_by = (select auth.uid())
        and status = 'Pending'
    );

drop policy if exists "Administrators and Pastors manage activities" on public.ministry_activities;
create policy "Administrators and Pastors manage activities"
    on public.ministry_activities for all
    to authenticated
    using ((select public.current_user_role()) in ('Administrator', 'Pastor'))
    with check ((select public.current_user_role()) in ('Administrator', 'Pastor'));
