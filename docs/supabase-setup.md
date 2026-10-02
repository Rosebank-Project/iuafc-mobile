# Supabase setup

The app uses one shared Supabase project for the database, authentication and API.

## 1. Create the project (once – Miguel)

1. Sign in at <https://supabase.com/dashboard> and click **New project**.
2. Organization: create one for the group (e.g. `Rosebank-Project`).
3. Name: `iuafc`, region: **South Africa (Cape Town)** or the closest one, and save the database password in a safe place.
4. Invite the group: **Organization settings → Team → Invite** (Rolph, Alexandra, Avuzwa).

## 2. Connect the app (every member)

1. In the Supabase dashboard open **Project Settings → API**.
2. Copy the **Project URL** and the **anon public** key.
3. Add them to `local.properties` in the project root (this file is in `.gitignore`):

```properties
SUPABASE_URL=https://xxxxxxxx.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOi...
```

4. **File → Sync Project with Gradle Files** in Android Studio.

The keys reach the code as `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_ANON_KEY`, and
the client is in `data/remote/SupabaseProvider.kt`.

> The anon key is safe to use in the app because the database is protected by Row Level
> Security (issue #1). **Never** put the `service_role` key in the app or in the repository.

## 3. Create the profiles table (once – Miguel)

`profiles` stores the role of every account. A trigger creates a profile (role **Church Member**)
when someone registers or signs in with Google, and only Administrators can change roles.

1. Supabase → **SQL Editor** → **New query**, paste `database/profiles.sql` and click **Run**.
2. Open `database/first-admin.sql`, replace `YOUR_EMAIL@gmail.com` with the email you sign in
   with, paste it in a new query and **Run**. The result should show your account as Administrator.
3. Sign out and sign in again in the app – the menu now shows the Administrator screens.

After that, change other people's roles in the app: **Menu → Users**.

## 4. GitHub Actions

For the CI build, add the same two values as repository secrets:
**GitHub → Settings → Secrets and variables → Actions → New repository secret**
named `SUPABASE_URL` and `SUPABASE_ANON_KEY`.

## Using the client in a repository

```kotlin
class EventRepository(private val supabase: SupabaseClient = SupabaseProvider.client) {
    suspend fun getEvents(): List<Event> =
        supabase.from("events").select().decodeList<Event>()
}
```
