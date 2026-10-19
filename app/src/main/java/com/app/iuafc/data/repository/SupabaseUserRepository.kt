
package com.app.iuafc.data.repository

import com.app.iuafc.data.model.Role
import com.app.iuafc.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

/**
 * Users and roles from the `profiles` table
 * (the profiles migration in supabase/migrations).
 *
 * Row Level Security controls profile access, and roles can only be
 * changed through the `set_user_role` database function.
 */
class SupabaseUserRepository(
    private val supabase: SupabaseClient,
) : UserRepository {

    override suspend fun getUsers(): List<UserProfile> =
        supabase.from("profiles")
            .select(PROFILE_COLUMNS) {
                order("full_name", Order.ASCENDING)
            }
            .decodeList<UserProfile>()

    override suspend fun updateFullName(
        userId: String,
        fullName: String,
    ): SaveResult {
        val cleanedName = fullName.trim()

        if (cleanedName.isBlank()) {
            return SaveResult.Error("Full name cannot be empty.")
        }

        return try {
            supabase.from("profiles").update(
                {
                    set("full_name", cleanedName)
                }
            ) {
                filter {
                    eq("id", userId)
                }
            }

            SaveResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: PostgrestRestException) {
            SaveResult.Error(e.error)
        } catch (_: HttpRequestException) {
            SaveResult.Error(
                "No internet connection. Check your connection and try again."
            )
        } catch (_: Exception) {
            SaveResult.Error(
                "Could not update your name. Please try again."
            )
        }
    }

    override suspend fun updateRole(
        userId: String,
        role: Role,
        memberId: String?,
        ministryId: String?,
    ): SaveResult =
        try {
            supabase.postgrest.rpc(
                "set_user_role",
                buildJsonObject {
                    put("target_user", userId)
                    put("new_role", role.displayName)

                    // Members and ministries are still mock data (#1)
                    // with IDs like "min-1"; only real database IDs
                    // (UUIDs) are sent.
                    put(
                        "new_member_id",
                        memberId?.takeIf(::isUuid)
                    )
                    put(
                        "new_ministry_id",
                        ministryId?.takeIf(::isUuid)
                    )
                },
            )

            SaveResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: PostgrestRestException) {
            // The database function raises messages written for the user.
            SaveResult.Error(e.error)
        } catch (_: HttpRequestException) {
            SaveResult.Error(
                "No internet connection. Check your connection and try again."
            )
        } catch (_: Exception) {
            SaveResult.Error(
                "Could not change the role. Please try again."
            )
        }

    companion object {

        /** Columns that match [UserProfile]. */
        val PROFILE_COLUMNS = Columns.list(
            "id",
            "full_name",
            "email",
            "role",
            "member_id",
            "ministry_id",
        )

        private fun isUuid(value: String): Boolean =
            runCatching {
                UUID.fromString(value)
            }.isSuccess
    }
}
