# App architecture – how to build your screen

The app uses **MVVM**: `Screen (Compose) → ViewModel → Repository → Supabase`.

```
app/src/main/java/com/app/iuafc/
├── AppContainer.kt          # creates the repositories once (simple dependency injection)
├── MainActivity.kt          # shows Login when signed out, MainScaffold when signed in
├── data/
│   ├── mock/MockData.kt     # sample data (≥10 records per table) until Supabase tables exist
│   ├── model/               # data classes matching the database tables
│   └── repository/          # talks to Supabase (one repository per feature)
├── navigation/
│   ├── Destinations.kt      # routes + which roles can open each screen
│   └── MainScaffold.kt      # bottom bar + NavHost (all the screens are registered here)
├── ui/
│   ├── common/UiState.kt    # Loading / Success / Error
│   ├── components/          # shared UI: ScreenScaffold, StateViews, ValidatedTextField
│   ├── screens/<feature>/   # YOUR SCREEN GOES HERE
│   └── theme/               # colours from the website (navy + gold), light/dark
└── util/Validators.kt       # input validation for every form
```

## Your screen already exists

Every feature has a placeholder screen in `ui/screens/<feature>/`, already connected to the
navigation and the role rules. Open your file, replace `PlaceholderContent(...)` with your UI,
and keep the function signature.

| Feature | File | Issue |
|---|---|---|
| Home | `ui/screens/dashboard/DashboardScreen.kt` | #9 |
| Events | `ui/screens/events/EventsScreen.kt` | #4 |
| Attendance / My Attendance | `ui/screens/attendance/`, `ui/screens/myattendance/` | #4 |
| Announcements / Notifications | `ui/screens/announcements/`, `ui/screens/notifications/` | #9 |
| Settings | `ui/screens/settings/SettingsScreen.kt` | #10 |
| Donations / Ministries / Activities | `ui/screens/donations/`, `ministries/`, `activities/` | #12 |
| Reports | `ui/screens/reports/ReportsScreen.kt` | #13 |
| Help | `ui/screens/help/HelpScreen.kt` | #14 |
| Members / Users | `ui/screens/members/`, `ui/screens/users/` | #22 |

## Pattern to follow

**1. Repository** (`data/repository/EventRepository.kt`) – an interface, with a mock version
that uses `MockData` until the Supabase tables are ready (#1, #2). When they are, add a
Supabase version and change one line in `AppContainer` – the screen stays the same.

```kotlin
interface EventRepository {
    suspend fun getEvents(): List<Event>
}

class MockEventRepository : EventRepository {
    private val events = MockData.events.toMutableList()
    override suspend fun getEvents(): List<Event> = events.toList()
}

// Later:
class SupabaseEventRepository(private val supabase: SupabaseClient) : EventRepository {
    override suspend fun getEvents(): List<Event> =
        supabase.from("events").select().decodeList<Event>()
}
```

Register it in `AppContainer`:

```kotlin
val eventRepository: EventRepository = MockEventRepository()
```

**2. ViewModel** (`ui/screens/events/EventsViewModel.kt`) – exposes a `UiState`.

```kotlin
class EventsViewModel(private val repository: EventRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Event>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Event>>> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = try {
                UiState.Success(repository.getEvents())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Could not load events")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { EventsViewModel(appContainer.eventRepository) }
        }
    }
}
```

**3. Screen** – uses `ScreenScaffold` and `UiStateContent`.

```kotlin
@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = viewModel(factory = EventsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenScaffold(title = "Events", modifier = modifier) { padding ->
        UiStateContent(state, Modifier.padding(padding), onRetry = viewModel::load) { events ->
            if (events.isEmpty()) EmptyState("No upcoming events")
            else LazyColumn { items(events) { EventCard(it) } }
        }
    }
}
```

## Rules

- **Never let invalid input crash the app.** Validate every field with `util/Validators.kt`
  and show the error with `ValidatedTextField`. Wrap Supabase calls in `try/catch`.
- **Show loading, empty and error states** on every screen that loads data.
- **Use theme colours** (`MaterialTheme.colorScheme.*`), never hard-coded colours, so dark mode works.
- **Check the role** with `AppDestination.X.isAllowedFor(user.role)` before showing admin-only buttons.
- Add unit tests for your ViewModel/validation in `app/src/test/` (#5).

## Testing without the real login

Until #20 is done, the app opens a **demo login**: choose any role to sign in and see the menu
for that role.
