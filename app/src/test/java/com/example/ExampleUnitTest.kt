package com.example

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testColorConversion() {
    val whiteHex = 0xFFFFFFFFL
    val color = Color(whiteHex.toInt())
    assertEquals(1.0f, color.alpha, 0.01f)
    assertEquals(1.0f, color.red, 0.01f)
    assertEquals(1.0f, color.green, 0.01f)
    assertEquals(1.0f, color.blue, 0.01f)

    val amberHex = 0xFFFCD34DL
    val amber = Color(amberHex.toInt())
    assertEquals(1.0f, amber.alpha, 0.01f)
    assertTrue(amber.red > 0.9f)
  }
}
