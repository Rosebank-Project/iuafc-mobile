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
   supabase/migrations/20261002120000_profiles.sql     ← already there
   supabase/migrations/20261005090000_schema.sql       ← tables (#1)
   supabase/migrations/20261005100000_seed.sql         ← at least 10 records per table (#2)
   ```

   With the Supabase CLI you can create it with `npx supabase migration new schema`.

2. Write the SQL. Prefer statements that can run twice without errors
   (`create table if not exists`, `create or replace function`, `drop policy if exists` …).

3. Open a pull request and check the **Database migrations** check: its log shows which files
   will be applied.

4. After the merge, check the **Actions** tab: *Apply migrations* must be green.

## Rules

- **Never edit a migration that is already on `main`** – it will not run again. Create a new
  migration to change something (e.g. `ALTER TABLE ...`).
- Seed data is a normal migration too (`supabase/seed.sql` is only used for local databases).
- One-off scripts with personal data (like `supabase/scripts/first-admin.sql`) are **not**
  migrations – run them by hand in the Supabase SQL Editor.
- Enable **Row Level Security** on every new table and add policies per role.

## Required secret

`SUPABASE_DB_URL` in GitHub → Settings → Secrets and variables → Actions: the **Session pooler**
connection string from Supabase → **Connect** (with the database password filled in). The
workflow connects straight to Postgres with it, so no Supabase access token is needed.
Never commit it – it contains the database password.
