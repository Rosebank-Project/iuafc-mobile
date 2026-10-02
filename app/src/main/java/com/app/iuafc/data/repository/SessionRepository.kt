package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the logged-in user.
 *
 * TEMPORARY: until Supabase Auth is added (issue #20) you can sign in as a demo
 * user of any role, so every screen can be built and tested with the right menu.
 */
class SessionRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun signInAsDemo(role: Role) {
        _currentUser.value = UserProfile(
            id = "demo-${role.name.lowercase()}",
            fullName = "Demo ${role.displayName}",
            email = "${role.name.lowercase()}@iuafc.demo",
            role = role,
        )
    }

    fun signOut() {
        _currentUser.value = null
    }
}
