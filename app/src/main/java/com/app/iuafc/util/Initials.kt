package com.app.iuafc.util

/**
 * Initials shown in the avatars: first letter of the first and last name
 * ("Pastor John Williams" -> "PW", "Grace" -> "GR"). Every avatar in the app uses this,
 * so the same person always has the same initials.
 */
fun initialsOf(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}".uppercase()
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "?"
    }
}
