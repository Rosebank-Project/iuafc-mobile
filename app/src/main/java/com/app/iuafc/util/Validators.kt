package com.app.iuafc.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Input validation shared by every form. Each function returns an error message
 * to show under the field, or `null` when the value is valid.
 *
 * Keep these free of Android code so they can be unit tested (issue #5).
 */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // South African numbers: 0XXXXXXXXX or +27XXXXXXXXX
    private val PHONE_REGEX = Regex("^(\\+27|0)[0-9]{9}$")

    const val MIN_PASSWORD_LENGTH = 8

    fun required(value: String, fieldName: String): String? =
        if (value.isBlank()) "$fieldName is required" else null

    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(value.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Password is required"
        value.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        value.none { it.isLetter() } || value.none { it.isDigit() } ->
            "Password must contain letters and numbers"
        else -> null
    }

    fun confirmPassword(password: String, confirmation: String): String? = when {
        confirmation.isEmpty() -> "Please confirm your password"
        password != confirmation -> "Passwords do not match"
        else -> null
    }

    fun phone(value: String): String? {
        val digits = value.replace(" ", "")
        return when {
            digits.isBlank() -> "Contact number is required"
            !PHONE_REGEX.matches(digits) -> "Enter a valid phone number (e.g. 0821234567)"
            else -> null
        }
    }

    fun amount(value: String): String? {
        val number = value.replace(",", ".").toDoubleOrNull()
        return when {
            value.isBlank() -> "Amount is required"
            number == null -> "Enter a valid amount"
            number <= 0 -> "Amount must be greater than zero"
            else -> null
        }
    }

    /** Date in ISO format (yyyy-MM-dd). */
    fun date(value: String, fieldName: String = "Date"): String? {
        if (value.isBlank()) return "$fieldName is required"
        return try {
            LocalDate.parse(value)
            null
        } catch (_: DateTimeParseException) {
            "$fieldName must be in the format yyyy-MM-dd"
        }
    }

    fun pastDate(value: String, fieldName: String = "Date", today: LocalDate = LocalDate.now()): String? {
        date(value, fieldName)?.let { return it }
        return if (LocalDate.parse(value).isAfter(today)) "$fieldName cannot be in the future" else null
    }
}
