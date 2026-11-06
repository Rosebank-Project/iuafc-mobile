package com.app.iuafc.util

import android.util.Log

/**
 * One place for the app's log messages, so every line in Logcat starts with "IUAFC/".
 * Filter Logcat by `tag:IUAFC` to follow what the app is doing: sign-in, the data it loads
 * and saves in Supabase, and every error a screen shows.
 *
 * Levels follow the Android guidance (Android Developers, 2026):
 * - [d] debug: normal steps (loading data, saving a record). Removed from release builds by R8.
 * - [i] info: important events (signed in, signed out, demo mode).
 * - [w] warning: something failed but the app recovered (no internet, permission refused).
 * - [e] error: an operation failed and the user sees an error message.
 *
 * Never log passwords, tokens, emails or other personal data.
 *
 * Reference: Android Developers. 2026. Log. [Online]. Available at:
 * https://developer.android.com/reference/android/util/Log [Accessed 6 November 2026].
 */
object AppLog {
    private const val PREFIX = "IUAFC/"

    fun d(tag: String, message: String) {
        Log.d(PREFIX + tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(PREFIX + tag, message)
    }

    fun w(tag: String, message: String, error: Throwable? = null) {
        Log.w(PREFIX + tag, message, error)
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        Log.e(PREFIX + tag, message, error)
    }
}
