/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.builder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ParameterBuilderValidatorTest {

    @Test
    fun `integer validator rejects overflow`() {
        assertTrue(ParameterBuilder.INTEGER_VALIDATOR("2147483648").isError)
    }

    @Test
    fun `floating validators reject non finite numbers`() {
        assertTrue(ParameterBuilder.FLOAT_VALIDATOR("NaN").isError)
        assertTrue(ParameterBuilder.FLOAT_VALIDATOR("Infinity").isError)
        assertTrue(ParameterBuilder.DOUBLE_VALIDATOR("-Infinity").isError)
    }

    @Test
    fun `floating validators preserve finite values`() {
        val result = ParameterBuilder.DOUBLE_VALIDATOR("1.25")
        assertFalse(result.isError)
        assertEquals(1.25, result.value!!, 0.0)
    }

    @Test
    fun `boolean validator accepts operational spellings`() {
        listOf("true", "on", "yes", "1", "enable", "enabled").forEach {
            assertEquals(true, ParameterBuilder.BOOLEAN_VALIDATOR(it).value)
        }
        listOf("false", "off", "no", "0", "disable", "disabled").forEach {
            assertEquals(false, ParameterBuilder.BOOLEAN_VALIDATOR(it).value)
        }
    }

    @Test
    fun `boolean validator rejects ambiguous input`() {
        assertTrue(ParameterBuilder.BOOLEAN_VALIDATOR("maybe").isError)
    }
}
