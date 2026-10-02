package com.app.iuafc

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.app.iuafc.data.remote.SupabaseProvider
import com.app.iuafc.data.repository.DemoSessionRepository
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SupabaseSessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

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
