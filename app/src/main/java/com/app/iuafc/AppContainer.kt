package com.app.iuafc

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.app.iuafc.data.remote.SupabaseProvider
import com.app.iuafc.data.repository.AnnouncementRepository
import com.app.iuafc.data.repository.AttendanceRepository
import com.app.iuafc.data.repository.ChurchFeaturesRepository
import com.app.iuafc.data.repository.DashboardRepository
import com.app.iuafc.data.repository.DemoSessionRepository
import com.app.iuafc.data.repository.EventRepository
import com.app.iuafc.data.repository.MemberRepository
import com.app.iuafc.data.repository.MockAnnouncementRepository
import com.app.iuafc.data.repository.MockAttendanceRepository
import com.app.iuafc.data.repository.MockChurchFeaturesRepository
import com.app.iuafc.data.repository.MockDashboardRepository
import com.app.iuafc.data.repository.MockEventRepository
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
    private val context: Context,
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

    // Ministries, activities and donations (Avuzwa's screens).
    val churchFeaturesRepository: ChurchFeaturesRepository =
        MockChurchFeaturesRepository()

    // Reads ministries from churchFeaturesRepository so new ministries show up in Members.
    val memberRepository: MemberRepository =
        MockMemberRepository(churchFeaturesRepository = churchFeaturesRepository)

    // Shared announcement repository for the entire app.
    val announcementRepository: AnnouncementRepository =
        MockAnnouncementRepository()

    // The dashboard uses the shared announcement repository.
    val dashboardRepository: DashboardRepository =
        MockDashboardRepository(
            announcementRepository = announcementRepository,
        )

    // Notifications use the same announcements as the dashboard.
    val notificationRepository: NotificationRepository =
        MockNotificationRepository(
            announcementRepository = announcementRepository,
        )

    // Events and registrations (mock until the Supabase repository is added)
    val eventRepository: EventRepository =
        MockEventRepository()

    // Attendance captured by leaders and shown in My Attendance
    val attendanceRepository: AttendanceRepository =
        MockAttendanceRepository()

    val settingsRepository: SettingsRepository =
        MockSettingsRepository(
            context,
            appScope,
        )

    val userRepository: UserRepository =
        if (SupabaseProvider.isConfigured) {
            SupabaseUserRepository(
                SupabaseProvider.client,
            )
        } else {
            MockUserRepository()
        }
}

/**
 * Application class that owns the shared AppContainer.
 */
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

/**
 * Retrieves the application's shared AppContainer from ViewModel factories.
 */
val CreationExtras.appContainer: AppContainer
    get() {
        val application =
            this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as IuafcApplication

        return application.container
    }