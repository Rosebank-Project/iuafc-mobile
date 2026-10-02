package com.app.iuafc

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.app.iuafc.data.repository.SessionRepository

/**
 * Creates the repositories once and shares them with the whole app.
 *
 * When you add a repository for your feature (e.g. `EventRepository`), create it
 * here and read it in your ViewModel factory with [appContainer].
 */
class AppContainer {
    val sessionRepository = SessionRepository()
}

class IuafcApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}

/** Use inside a `viewModelFactory { initializer { ... } }` to get the repositories. */
val CreationExtras.appContainer: AppContainer
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as IuafcApplication).container
