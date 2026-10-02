# IUAFC Mobile App – Task 2 Work Split

**Group:** ALAVUMIRO TECH CONSULTANTS
**Module:** XISD6329 – Work Integrated Learning, Task 2 (Weeks 6–10)
**Goal:** Native Android app (Kotlin + Jetpack Compose) of the Church Management System, based on the web version (`Rosebank-Project/iuafc`) and the Task 1 sitemap/wireframes.

---

## 1. What Task 2 requires (from the module manual)

| Requirement | Rubric marks |
|---|---|
| App runs on a **real phone** (not only the emulator), no crashes | 10 |
| Register + sign in, **encrypted password**, **single sign-on (SSO)** | 10 |
| **Settings** menu that makes sense for the app | 10 |
| Creation/use of an **API** | 10 |
| **Integration** of the API in the app | 10 |
| **Database** (DBMS) – correct tables, PK/FK, data types, ≥ 10 records per table | 15 |
| User-friendly UI, handles invalid input without crashing | 10 |
| **User documentation** + in-app help system | 10 |
| **GitHub**: multiple commits, README, **automated tests run by GitHub Actions** | 10 |
| **Demo video** with voice-over, showing every feature + the data stored in auth service, API and database | 10 |

Also required (not on the rubric, but in the brief):
- Keep **Azure DevOps** (or similar) updated during the whole implementation.
- Regular team meetings (physical or virtual).
- Update the website to use the same backend.
- Word document with a cover page, an index and GitHub links. **No zip files.**

---

## 2. Technical decisions (confirmed: Supabase)

The web version stores everything in `localStorage` and only base64-encodes passwords, so it has **no real backend**. We need one shared by the app and the website:

- **Backend:** [Supabase](https://supabase.com) (free tier)
  - **PostgreSQL** → the DBMS (tables, PK/FK, seed data)
  - **Supabase Auth** → password hashing (bcrypt) + **Google SSO**
  - **REST API** (PostgREST, auto-generated from the tables) + Edge Functions where custom logic is needed
- **Android:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM (ViewModel + Repository), `supabase-kt` or Retrofit, DataStore for settings
- **CI:** GitHub Actions → build + unit tests on every push/PR

**Data model** (from `lib/types.ts`): `users`, `members`, `ministries`, `events`, `event_registrations`, `attendance`, `donations`, `announcements`, `ministry_activities`

**Roles:** Administrator, Pastor, Finance Officer, Ministry Leader, Church Member (same role-based menu as `components/app-sidebar.tsx`)

---

## 3. Tasks per member

Everyone codes. Each member works on a branch (`feature/<name>-<feature>`) and opens a Pull Request. The commit history is part of the mark and of the peer evaluation.

### Miguel – Technical Lead
- [ ] **M1. App foundation** *(Week 6 – blocks everyone)*: package structure, theme/colours to match the website, Navigation Compose, bottom bar/drawer filtered by role, data models, Supabase client, base Repository/ViewModel pattern
- [ ] **M2. Supabase project setup**: create the project, invite the group, share the keys safely (`local.properties`, never commit them)
- [ ] **M3. Authentication**: Register screen, Login screen, **Google SSO**, session persistence, logout, role-based navigation, input validation (empty fields, invalid email, weak password)
- [ ] **M4. GitHub Actions**: workflow that builds the APK and runs the unit tests on every push/PR
- [ ] **M5. Screens**: Members (list/search/add/edit – Admin, Pastor) and Users (Admin)
- [ ] **M6. Website update**: replace `localStorage` in the React app with the same Supabase backend
- [ ] Review and merge PRs

### Rolph – Business Analyst / Testing
- [ ] **R1. Database** *(Week 6 – blocks the screens)*: SQL script with all the tables, primary/foreign keys, correct data types, consistent `snake_case` naming, Row Level Security policies per role. Add it as a migration: `supabase/migrations/<timestamp>_schema.sql` (see `docs/database-migrations.md`)
- [ ] **R2. Seed data**: at least **10 records per table** (`supabase/migrations/<timestamp>_seed.sql`), based on `lib/seed-data.ts`
- [ ] **R3. ERD**: diagram of the database for the report
- [ ] **R4. Screens**: Events (list, details, register/unregister), Attendance (capture – Admin/Pastor/Ministry Leader), My Attendance (Church Member)
- [ ] **R5. Automated tests**: unit tests for the validation logic and ViewModels, plus at least 2 instrumented UI tests (login, one form). They must pass in GitHub Actions
- [ ] **R6. Test documentation**: test cases + results (screenshots) for the report

### Alexandra – Project Manager
- [ ] **A1. Azure DevOps** *(Week 6, then ongoing)*: create the project, add the group, turn this file into Epics/User Stories/Tasks, set up sprints, keep the board updated every week (take screenshots for the report)
- [ ] **A2. Meetings**: schedule the weekly meeting, keep minutes (date, attendees, decisions, actions)
- [ ] **A3. Screens**: Home/Dashboard (summary cards per role), Announcements (list + create for Admin/Pastor), Notifications
- [ ] **A4. Settings** *(10 marks)*: edit profile, change password, dark/light theme, notification on/off, language (optional), about, logout – saved with DataStore/Supabase
- [ ] **A5. Demo video**: script, record on a **real Android phone**, voice-over, show the data in Supabase Auth, the API and the tables. Each member presents their own features

### Avuzwa – Documentation Lead
- [ ] **V1. Screens**: Donations (record – Finance Officer; history – Member), Ministries, Ministry Activities (submit – Ministry Leader; approve/reject – Pastor/Admin)
- [ ] **V2. Screen**: Reports (attendance, donations and membership summaries)
- [ ] **V3. In-app Help**: Help/FAQ screen accessible from Settings, explaining each feature
- [ ] **V4. User documentation**: user manual with labelled screenshots of every screen (how to register, log in, use each feature)
- [ ] **V5. README.md**: description, features, screenshots, setup steps, how to run the tests, links (website, video, Azure DevOps)
- [ ] **V6. Task 2 report**: cover page, index, GitHub links, and evidence collected from everyone (DB, API, tests, DevOps, meeting minutes)

---

## 4. Timeline (Weeks 6–10)

| Week | Focus |
|---|---|
| 6 | Kick-off meeting, confirm decisions · **M1, M2, R1, R2, A1** (foundation + DB first) |
| 7 | **M3** auth · start the screens (A3, R4, V1, M5) |
| 8 | Finish the screens · **A4** settings · **M4** CI · **R5** tests |
| 9 | Help + docs (V3, V4, V5) · M6 website · bug fixing on a real phone |
| 10 | Video (A5) · report (V6) · final testing · submit the prototype for feedback |

## 5. Team rules
- Never commit straight to `main`; open a PR and ask for one review.
- Small commits with clear messages (`feat: add events screen`, `fix: login crash on empty email`).
- Never commit API keys, `local.properties` or `google-services.json`.
- Everyone installs **Android SDK Platform 37.1** (Android Studio → Settings → Android SDK).
- Update your Azure DevOps tasks when you start/finish them.
