package com.app.iuafc.data.repository

import com.app.iuafc.data.mock.MockData
import com.app.iuafc.data.model.Member
import com.app.iuafc.data.model.Ministry
import kotlinx.coroutines.delay

/** Church members (table `members`) and the ministries they belong to. */
interface MemberRepository {
    suspend fun getMembers(): List<Member>
    suspend fun getMember(id: String): Member?
    suspend fun getMinistries(): List<Ministry>

    /** Adds the member when [Member.id] is blank, otherwise updates it. */
    suspend fun saveMember(member: Member): SaveResult
    suspend fun deleteMember(id: String): SaveResult
}

/**
 * In-memory version using [MockData] until the `members` table exists (#1).
 * Changes last until the app is closed. [delayMillis] simulates the network.
 */
class MockMemberRepository(
    private val delayMillis: Long = 400,
    private val churchFeaturesRepository: ChurchFeaturesRepository? = null,
) : MemberRepository {

    private val members = MockData.members.toMutableList()
    private var nextId = members.size + 1

    override suspend fun getMembers(): List<Member> {
        delay(delayMillis)
        return members.toList()
    }

    override suspend fun getMember(id: String): Member? {
        delay(delayMillis)
        return members.firstOrNull { it.id == id }
    }

    override suspend fun getMinistries(): List<Ministry> =
        churchFeaturesRepository?.getMinistries() ?: MockData.ministries

    override suspend fun saveMember(member: Member): SaveResult {
        delay(delayMillis)
        val emailTaken = members.any { it.id != member.id && it.email.equals(member.email, ignoreCase = true) }
        if (emailTaken) return SaveResult.Error("This email is already registered to another member.")

        if (member.id.isBlank()) {
            members += member.copy(id = "mem-${nextId++}")
        } else {
            val index = members.indexOfFirst { it.id == member.id }
            if (index == -1) return SaveResult.Error("Member not found.")
            members[index] = member
        }
        return SaveResult.Success
    }

    override suspend fun deleteMember(id: String): SaveResult {
        delay(delayMillis)
        return if (members.removeAll { it.id == id }) SaveResult.Success else SaveResult.Error("Member not found.")
    }
}
