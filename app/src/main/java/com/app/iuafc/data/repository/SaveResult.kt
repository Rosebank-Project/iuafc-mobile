package com.app.iuafc.data.repository

/** Result of creating, updating or deleting a record. */
sealed interface SaveResult {
    data object Success : SaveResult
    data class Error(val message: String) : SaveResult
}
