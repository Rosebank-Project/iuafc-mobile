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

## 3. GitHub Actions

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
