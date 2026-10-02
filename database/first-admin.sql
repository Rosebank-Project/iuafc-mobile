-- =============================================================================
-- Make the first Administrator (run once, after profiles.sql)
--
-- Every new account starts as "Church Member", and only an Administrator can
-- change roles, so the first Administrator has to be set here.
-- Replace the email with the account you use to sign in, then Run.
-- =============================================================================
update public.profiles
set role = 'Administrator', updated_at = now()
where email = 'YOUR_EMAIL@gmail.com';

-- Check: should show your account with the Administrator role
select full_name, email, role from public.profiles order by created_at;
