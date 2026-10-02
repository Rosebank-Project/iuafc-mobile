package com.app.iuafc.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ValidatorsTest {

    @Test
    fun required_blankValue_returnsError() {
        assertEquals("Name is required", Validators.required("   ", "Name"))
        assertNull(Validators.required("Grace", "Name"))
    }

    @Test
    fun email_validatesFormat() {
        assertNull(Validators.email("member@iuafc.org"))
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("member@"))
        assertNotNull(Validators.email("member.iuafc.org"))
    }

    @Test
    fun password_requiresLengthLettersAndNumbers() {
        assertNull(Validators.password("church2026"))
        assertNotNull(Validators.password(""))
        assertNotNull(Validators.password("abc12"))
        assertNotNull(Validators.password("onlyletters"))
        assertNotNull(Validators.password("12345678"))
    }

    @Test
    fun confirmPassword_mustMatch() {
        assertNull(Validators.confirmPassword("church2026", "church2026"))
        assertEquals("Passwords do not match", Validators.confirmPassword("church2026", "church2025"))
        assertNotNull(Validators.confirmPassword("church2026", ""))
    }

    @Test
    fun phone_acceptsSouthAfricanNumbers() {
        assertNull(Validators.phone("0821234567"))
        assertNull(Validators.phone("+27821234567"))
        assertNull(Validators.phone("082 123 4567"))
        assertNotNull(Validators.phone("12345"))
        assertNotNull(Validators.phone(""))
    }

    @Test
    fun amount_mustBePositiveNumber() {
        assertNull(Validators.amount("150.50"))
        assertNull(Validators.amount("150,50"))
        assertNotNull(Validators.amount("0"))
        assertNotNull(Validators.amount("-10"))
        assertNotNull(Validators.amount("abc"))
        assertNotNull(Validators.amount(""))
    }

    @Test
    fun date_requiresIsoFormat() {
        assertNull(Validators.date("2026-10-02"))
        assertNotNull(Validators.date("02/10/2026"))
        assertNotNull(Validators.date("2026-13-40"))
        assertNotNull(Validators.date(""))
    }

    @Test
    fun pastDate_rejectsFutureDates() {
        val today = LocalDate.of(2026, 10, 2)
        assertNull(Validators.pastDate("1990-05-14", "Date of birth", today))
        assertEquals(
            "Date of birth cannot be in the future",
            Validators.pastDate("2027-01-01", "Date of birth", today),
        )
    }
}
