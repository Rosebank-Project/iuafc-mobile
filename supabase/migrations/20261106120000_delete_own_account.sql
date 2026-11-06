-- Account deletion (required by Google Play for apps where users can create an account).
--
-- A signed-in user can delete their own account from the app (Settings → Delete account) or the
-- website (/delete-account). Deleting the auth user removes their profile (on delete cascade);
-- records they created stay for the church's history and lose the link to them
-- (events, announcements, donations, attendance and activities use on delete set null).
-- Member records are church data, not the account, so they are kept.

create or replace function public.delete_my_account()
returns void
language plpgsql
security definer
set search_path = ''
as $$
declare
    caller uuid := (select auth.uid());
begin
    if caller is null then
        raise exception 'You must be signed in to delete your account.' using errcode = '42501';
    end if;

    -- Same rule as set_user_role: the church must keep at least one Administrator.
    if (select role from public.profiles where id = caller) = 'Administrator'
       and (select count(*) from public.profiles where role = 'Administrator') <= 1 then
        raise exception 'You are the only Administrator. Make someone else an Administrator before deleting your account.'
            using errcode = 'P0001';
    end if;

    delete from auth.users where id = caller;
end;
$$;

revoke execute on function public.delete_my_account() from public, anon;
grant execute on function public.delete_my_account() to authenticated;
