package com.app.iuafc.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Read-only field that opens a calendar. [value] and [onValueChange] use ISO dates
 * ("2026-10-02"), the same format as the database.
 *
 * ```
 * DateField(value = dob, onValueChange = { dob = it }, label = "Date of birth",
 *     error = dobError, allowFuture = false)
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    allowFuture: Boolean = true,
    allowPast: Boolean = true,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Open the calendar when the field is tapped.
    LaunchedEffect(interactionSource, enabled) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release && enabled) showPicker = true
        }
    }

    OutlinedTextField(
        value = value.toDisplayDate(),
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Outlined.CalendarToday, contentDescription = "Choose date") },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        interactionSource = interactionSource,
        singleLine = true,
        modifier = modifier,
    )

    if (showPicker) {
        val today = LocalDate.now()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = value.toLocalDateOrNull()?.toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    return (allowFuture || !date.isAfter(today)) && (allowPast || !date.isBefore(today))
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onValueChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                        }
                        showPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
}

private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** "2026-10-02" → "2 Oct 2026" */
fun String.toDisplayDate(): String = toLocalDateOrNull()?.format(DISPLAY_FORMAT) ?: this
