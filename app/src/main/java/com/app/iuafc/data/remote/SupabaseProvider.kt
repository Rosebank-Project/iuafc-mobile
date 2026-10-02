package com.app.iuafc.data.remote

import com.app.iuafc.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Single Supabase client for the whole app.
 *
 * The URL and anon key come from `local.properties`:
 * ```
 * SUPABASE_URL=https://xxxx.supabase.co
 * SUPABASE_ANON_KEY=eyJ...
 * ```
 * Ask Miguel for the values. Never commit them.
 */
object SupabaseProvider {

    /** False when the keys are missing from local.properties. */
    val isConfigured: Boolean =
        BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        check(isConfigured) {
            "Supabase is not configured. Add SUPABASE_URL and SUPABASE_ANON_KEY to local.properties."
        }
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
}
