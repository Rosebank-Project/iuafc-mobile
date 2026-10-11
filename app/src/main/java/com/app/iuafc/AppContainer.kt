
package com.app.iuafc

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.app.iuafc.data.remote.SupabaseProvider
import com.app.iuafc.data.repository.AnnouncementRepository
import com.app.iuafc.data.repository.DashboardRepository
import com.app.iuafc.data.repository.DemoSessionRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.MockAnnouncementRepository
import com.app.iuafc.data.repository.MockDashboardRepository
import com.app.iuafc.data.repository.MockMemberRepository
import com.app.iuafc.data.repository.MockNotificationRepository
import com.app.iuafc.data.repository.MockSettingsRepository
import com.app.iuafc.data.repository.MockUserRepository
import com.app.iuafc.data.repository.NotificationRepository
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SettingsRepository
import com.app.iuafc.data.repository.SupabaseSessionRepository
import com.app.iuafc.data.repository.SupabaseUserRepository
import com.app.iuafc.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class AppContainer(
    private val context: android.content.Context,
    appScope: CoroutineScope,
) {

    val sessionRepository: SessionRepository =
        if (SupabaseProvider.isConfigured) {
            SupabaseSessionRepository(
                SupabaseProvider.client,
                appScope,
            )
        } else {
            DemoSessionRepository()
        }

    // Mock until the members table is ready (#1).
    val memberRepository: MemberRepository =
        MockMemberRepository()

    // One shared repository for announcements throughout the app.
    val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository()

    // The dashboard must use the same announcement repository.
    val dashboardRepository: DashboardRepository =
        MockDashboardRepository(
            announcementRepository = announcementRepository,
        )

    val notificationRepository: NotificationRepository =
        MockNotificationRepository()

    val settingsRepository: SettingsRepository =
        MockSettingsRepository(context, appScope)

    val userRepository: UserRepository =
        if (SupabaseProvider.isConfigured) {
            SupabaseUserRepository(
                SupabaseProvider.client,
            )
        } else {
            MockUserRepository()
        }
}

class IuafcApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(
            context = this,
            appScope = MainScope(),
        )
    }
}

val CreationExtras.appContainer: AppContainer
    get() = (
            this[
                ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY
            ] as IuafcApplication
            ).container