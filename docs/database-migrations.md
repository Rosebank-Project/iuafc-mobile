# Database migrations

Every change to the Supabase database is a SQL file in `supabase/migrations/`.
GitHub Actions (`.github/workflows/database.yml`) applies them, so nobody runs SQL by hand.

| When | What happens |
|---|---|
| You open a pull request that changes `supabase/` | **Dry run**: the log lists the migrations that would be applied. Nothing changes in the database. |
| The pull request is merged into `main` | The new migrations are **applied** to Supabase. Migrations that already ran are skipped. |

Supabase keeps the list of applied migrations in `supabase_migrations.schema_migrations`.

## Adding a migration

1. Create a file named `<timestamp>_<description>.sql` in `supabase/migrations/`.
   The timestamp is `YYYYMMDDHHMMSS` and sets the order:

   ```
   supabase/migrations/20261002120000_profiles.sql
   supabase/migrations/20261003120000_core_tables.sql
   …
   supabase/migrations/20261018170000_seed_data.sql
   supabase/migrations/<today's date and time>_<description>.sql   ← your new file
   ```

   With the Supabase CLI you can create it with `npx supabase migration new schema`.

2. Write the SQL. Prefer statements that can run twice without errors
   (`create table if not exists`, `create or replace function`, `drop policy if exists` …).

3. Open a pull request and check the **Database migrations** check: its log shows which files
   will be applied.

4. After the merge, check the **Actions** tab: *Apply migrations* must be green.

## Rules

- **The timestamp must be later than the last migration on `main`.** Supabase refuses a file dated
  before a migration that already ran, so use today's date and time, not the date of the older files.
- **Never edit a migration that is already on `main`** – it will not run again. Create a new
  migration to change something (e.g. `ALTER TABLE ...`).
- Seed data is a normal migration too (`supabase/seed.sql` is only used for local databases).
- One-off scripts with personal data (like `supabase/scripts/first-admin.sql`) are **not**
  migrations – run them by hand in the Supabase SQL Editor.
- Enable **Row Level Security** on every new table and add policies per role.

## Required secrets (already set)

`SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD` and `SUPABASE_PROJECT_ID` in
GitHub → Settings → Secrets and variables → Actions.
