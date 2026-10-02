package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import kotlinx.coroutines.delay

/**
 * App users and their roles (table `profiles`). Accounts themselves are created by
 * Supabase Auth (register / Google), so the app only manages roles.
 */
interface UserRepository {
    suspend fun getUsers(): List<UserProfile>

    /**
     * Changes the user's role. [memberId] links a Church Member to their member record and
     * [ministryId] links a Ministry Leader to their ministry; both are cleared for other roles.
     */
    suspend fun updateRole(userId: String, role: Role, memberId: String?, ministryId: String?): SaveResult
}

/** In-memory version using [MockData] until the `profiles` table exists (#1). */
class MockUserRepository(private val delayMillis: Long = 400) : UserRepository {

    private val users = MockData.users.toMutableList()

    override suspend fun getUsers(): List<UserProfile> {
        delay(delayMillis)
        return users.toList()
    }

    override suspend fun updateRole(userId: String, role: Role, memberId: String?, ministryId: String?): SaveResult {
        delay(delayMillis)
        val index = users.indexOfFirst { it.id == userId }
        if (index == -1) return SaveResult.Error("User not found.")

        val user = users[index]
        val isLastAdmin = user.role == Role.ADMINISTRATOR &&
            users.count { it.role == Role.ADMINISTRATOR } == 1
        if (isLastAdmin && role != Role.ADMINISTRATOR) {
            return SaveResult.Error("The church must have at least one Administrator.")
        }

        users[index] = user.copy(
            role = role,
            memberId = memberId.takeIf { role == Role.CHURCH_MEMBER },
            ministryId = ministryId.takeIf { role == Role.MINISTRY_LEADER },
        )
        return SaveResult.Success
    }
}
