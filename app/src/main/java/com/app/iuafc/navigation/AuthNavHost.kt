package com.app.iuafc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.iuafc.ui.screens.auth.ForgotPasswordScreen
import com.app.iuafc.ui.screens.auth.LoginScreen
import com.app.iuafc.ui.screens.auth.RegisterScreen
import kotlinx.serialization.Serializable

@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object ForgotPasswordRoute

/** Screens shown while signed out. Signing in switches MainActivity to [MainScaffold]. */
@Composable
fun AuthNavHost() {
    val navController = rememberNavController()
    val backToLogin: () -> Unit = { navController.popBackStack(LoginRoute, inclusive = false) }

    NavHost(navController = navController, startDestination = LoginRoute) {
        composable<LoginRoute> {
            LoginScreen(
                onRegister = { navController.navigate(RegisterRoute) },
                onForgotPassword = { navController.navigate(ForgotPasswordRoute) },
            )
        }
        composable<RegisterRoute> { RegisterScreen(onBackToLogin = backToLogin) }
        composable<ForgotPasswordRoute> { ForgotPasswordScreen(onBackToLogin = backToLogin) }
    }
}
