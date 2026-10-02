package com.app.iuafc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.iuafc.data.repository.SessionRepository
import com.app.iuafc.navigation.MainScaffold
import com.app.iuafc.ui.screens.auth.DemoLoginScreen
import com.app.iuafc.ui.theme.IUAFCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = (application as IuafcApplication).container.sessionRepository
        setContent {
            IUAFCTheme {
                IuafcApp(session)
            }
        }
    }
}

/** Shows the login screen when signed out, and the main app when signed in. */
@Composable
fun IuafcApp(session: SessionRepository) {
    val user by session.currentUser.collectAsStateWithLifecycle()
    val signedInUser = user

    if (signedInUser == null) {
        DemoLoginScreen(onSignIn = session::signInAsDemo)
    } else {
        MainScaffold(user = signedInUser, onSignOut = session::signOut)
    }
}
