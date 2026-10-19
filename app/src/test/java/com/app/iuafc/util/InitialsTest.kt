package com.app.iuafc.util

import org.junit.Assert.assertEquals
import org.junit.Test

class InitialsTest {
    @Test fun usesFirstAndLastName() = assertEquals("PW", initialsOf("Pastor John Williams"))
    @Test fun twoNames() = assertEquals("GA", initialsOf("Grace Adeyemi"))
    @Test fun oneName() = assertEquals("GR", initialsOf("grace"))
    @Test fun extraSpaces() = assertEquals("TN", initialsOf("  Thabo   Nkosi "))
    @Test fun emptyName() = assertEquals("?", initialsOf(" "))
}
