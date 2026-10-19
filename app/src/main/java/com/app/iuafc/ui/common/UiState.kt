package com.app.iuafc.ui.common

/**
 * State of a screen that loads data. Every ViewModel exposes one of these as a
 * `StateFlow<UiState<T>>`, and the screen shows [LoadingState], [ErrorState],
 * [EmptyState] or the content (see `ui/components/StateViews.kt`).
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
