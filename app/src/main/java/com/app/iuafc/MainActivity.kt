package com.app.iuafc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.data.repository.SessionState
import com.app.iuafc.navigation.AuthNavHost
import com.app.iuafc.navigation.MainScaffold
import com.app.iuafc.ui.components.LoadingState
import com.app.iuafc.ui.screens.auth.DemoLoginScreen
import com.app.iuafc.ui.theme.IUAFCTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as IuafcApplication).container
        val session = container.sessionRepository
        val settings = container.settingsRepository

        setContent {
            val themePreference by
            settings.themePreference.collectAsStateWithLifecycle()

            IUAFCTheme(themePreference = themePreference) {
                IuafcApp(session)
            }
        }
    }
}

/** Shows the login screens when signed out, and the main app when signed in. */
@Composable
fun IuafcApp(session: SessionRepository) {
    val state by session.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    when (val current = state) {
        SessionState.Loading -> {
            Surface(color = MaterialTheme.colorScheme.background) {
                LoadingState()
            }
        }

        SessionState.SignedOut -> {
            if (session.isDemo) {
                DemoLoginScreen(onSignIn = session::signInAsDemo)
            } else {
                AuthNavHost()
            }
        }

        is SessionState.SignedIn -> {
            MainScaffold(
                user = current.user,
                onSignOut = {
                    scope.launch {
                        session.signOut()
                    }
                },
            )
        }
    }
}