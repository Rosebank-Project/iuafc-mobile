package com.app.iuafc

import com.app.iuafc.data.repository.AnnouncementRepository
import com.app.iuafc.data.repository.MockAnnouncementRepository
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.app.iuafc.data.remote.SupabaseProvider
import com.app.iuafc.data.repository.DemoSessionRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.data.repository.MockUserRepository
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SupabaseSessionRepository
import com.app.iuafc.data.repository.SupabaseUserRepository
import com.app.iuafc.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import com.app.iuafc.data.repository.DashboardRepository
import com.app.iuafc.data.repository.MockDashboardRepository
import com.app.iuafc.data.repository.NotificationRepository
import com.app.iuafc.data.repository.MockNotificationRepository


/**
 * Creates the repositories once and shares them with the whole app.
 *
 * When you add a repository for your feature (e.g. `EventRepository`), create it
 * here and read it in your ViewModel factory with [appContainer].
 */
class AppContainer(appScope: CoroutineScope) {
    /** Real Supabase login when the keys are in local.properties, otherwise demo mode. */
    val sessionRepository: SessionRepository =
        if (SupabaseProvider.isConfigured) SupabaseSessionRepository(SupabaseProvider.client, appScope)
        else DemoSessionRepository()

    // Mock until the `members` table is ready (#1). Swap for the Supabase version then.
    val memberRepository: MemberRepository = MockMemberRepository()
    val dashboardRepository: DashboardRepository = MockDashboardRepository()
    val announcementRepository: AnnouncementRepository = MockAnnouncementRepository()
    val notificationRepository: NotificationRepository = MockNotificationRepository()
    val userRepository: UserRepository =
        if (SupabaseProvider.isConfigured) SupabaseUserRepository(SupabaseProvider.client)
        else MockUserRepository()
}

class IuafcApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(MainScope())
    }
}

/** Use inside a `viewModelFactory { initializer { ... } }` to get the repositories. */
val CreationExtras.appContainer: AppContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as IuafcApplication).container
