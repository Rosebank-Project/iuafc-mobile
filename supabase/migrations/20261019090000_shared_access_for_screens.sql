-- =============================================================================
-- Access the app's and the website's screens need, with the least data exposed.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Names of accounts for every signed-in user.
-- Screens show who wrote an announcement, who leads a ministry, who recorded a
-- donation or reviewed an activity. RLS only lets Administrators and Pastors read
-- every profile (with emails), so this function returns just id, name and role.
-- -----------------------------------------------------------------------------
create or replace function public.profile_directory()
returns table (id uuid, full_name text, role text)
language sql
stable
security definer
set search_path = ''
as $$
    select p.id, p.full_name, p.role
    from public.profiles p
    where (select auth.uid()) is not null
    order by p.full_name;
$$;

revoke execute on function public.profile_directory() from public, anon;
grant execute on function public.profile_directory() to authenticated;

-- -----------------------------------------------------------------------------
-- members: Finance Officers choose the member when recording a donation and
-- Ministry Leaders mark attendance, so both need to read the member list.
-- -----------------------------------------------------------------------------
drop policy if exists "Finance Officers and Ministry Leaders read members" on public.members;
create policy "Finance Officers and Ministry Leaders read members"
    on public.members for select
    to authenticated
    using ((select public.current_user_role()) in ('Finance Officer', 'Ministry Leader'));

-- -----------------------------------------------------------------------------
-- event_registrations: every signed-in user sees how many people registered for
-- an event. Rows only hold ids; member names stay protected by the members table.
-- -----------------------------------------------------------------------------
drop policy if exists "Signed-in users read registrations" on public.event_registrations;
create policy "Signed-in users read registrations"
    on public.event_registrations for select
    to authenticated
    using (true);

-- -----------------------------------------------------------------------------
-- ministry_activities: a Ministry Leader can withdraw their own proposal while it
-- is still Pending.
-- -----------------------------------------------------------------------------
drop policy if exists "Leaders withdraw their pending activities" on public.ministry_activities;
create policy "Leaders withdraw their pending activities"
    on public.ministry_activities for delete
    to authenticated
    using (
        (select public.current_user_role()) = 'Ministry Leader'
        and submitted_by = (select auth.uid())
        and status = 'Pending'
    );
