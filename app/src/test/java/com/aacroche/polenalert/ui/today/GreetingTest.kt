package com.aacroche.polenalert.ui.today

import com.aacroche.polenalert.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun `greetingFor boundary values test`() {
        assertEquals(R.string.greeting_night, greetingFor(5))
        assertEquals(R.string.greeting_morning, greetingFor(6))
        assertEquals(R.string.greeting_morning, greetingFor(13))
        assertEquals(R.string.greeting_afternoon, greetingFor(14))
        assertEquals(R.string.greeting_afternoon, greetingFor(20))
        assertEquals(R.string.greeting_night, greetingFor(21))
    }
}