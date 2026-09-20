/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

import com.google.gson.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValueValidationTest {

    @Test
    fun `text and json inputs obey numeric bounds`() {
        val integer = IntValue("Delay", 5, 1..10)
        val decimal = FloatValue("Range", 3F, 1F..6F)

        integer.fromText("999")
        decimal.fromJson(JsonPrimitive(-20F))

        assertEquals(10, integer.get())
        assertEquals(1F, decimal.get())
    }

    @Test
    fun `invalid and non-finite serialized numbers preserve previous value`() {
        val decimal = FloatValue("Range", 3F, 1F..6F)

        assertFalse(decimal.fromText("NaN"))
        assertEquals(3F, decimal.get())

        assertFalse(decimal.fromText("not-a-number"))
        assertEquals(3F, decimal.get())
    }

    @Test
    fun `reversed ranges are rejected by every mutation path`() {
        val range = IntRangeValue("CPS", 8..12, 1..20)

        assertFalse(range.set(15..4, saveImmediately = false))
        assertFalse(range.fromText("18..2"))

        assertEquals(8..12, range.get())
    }

    @Test
    fun `vectors and curves reject non-finite components`() {
        val vector = Vec3Value("Position", doubleArrayOf(1.0, 2.0, 3.0))
        val curve = CurveValue("Smoothing")

        assertFalse(vector.fromText("1.0,NaN,3.0"))
        assertFalse(curve.fromText("0.0,Infinity,1.0"))

        assertEquals(listOf(1.0, 2.0, 3.0), vector.get().toList())
        assertEquals(listOf(0.0, 0.25, 0.5, 0.75, 1.0), curve.get().toList())
    }

    @Test
    fun `failed changed listener rolls scalar and observable state back`() {
        val value = IntValue("Transactional", 3, 0..10).onChanged {
            if (it == 7) error("listener rejected candidate")
        }

        assertFalse(value.set(7, saveImmediately = false))
        assertEquals(3, value.get())
        assertEquals(3, value.asStateFlow().value)

        assertTrue(value.set(6, saveImmediately = false))
        assertEquals(6, value.get())
        assertEquals(6, value.asStateFlow().value)
    }

    @Test
    fun `deserialization uses interceptors and is transactional`() {
        val value = IntValue("ConfigValue", 2, 0..20)
            .onChange { old, new -> if (new > 10) old else new }
            .onChanged { if (it == 9) error("invalid runtime combination") }

        assertFalse(value.fromText("15"))
        assertEquals(2, value.get())
        assertFalse(value.fromJson(JsonPrimitive(9)))
        assertEquals(2, value.get())
        assertTrue(value.fromJson(JsonPrimitive(8)))
        assertEquals(8, value.get())
    }

    @Test
    fun `immutable values reject runtime and serialized mutations`() {
        val value = TextValue("ReadOnly", "stable").immutable()

        assertFalse(value.set("runtime", saveImmediately = false))
        assertFalse(value.fromText("config"))
        assertEquals("stable", value.get())
        assertEquals("stable", value.asStateFlow().value)
    }
}
