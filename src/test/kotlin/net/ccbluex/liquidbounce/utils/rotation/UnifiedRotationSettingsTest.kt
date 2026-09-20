package net.ccbluex.liquidbounce.utils.rotation

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.Rotations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class UnifiedRotationSettingsTest {

    @Test
    fun `rotation settings expose one nextgen engine without legacy selectors`() {
        val module = Module("UnifiedRotationSettingsTest", Category.CLIENT, gameDetecting = false)
        val settings = RotationSettings(module)
        val names = module.values.map { it.name }.toSet()

        assertFalse("Engine" in names)
        assertTrue(LEGACY_NAMES.none(names::contains))
        assertTrue(NEXTGEN_NAMES.all(names::contains))
        assertEquals(
            setOf("None", "Linear", "Sigmoid", "Interpolation", "Acceleration", "AI"),
            settings.modernAngleSmoothValue.values.toSet(),
        )
        assertEquals(null, Rotations.findDeep("Engine"))
    }

    @Test
    fun `old reset accessor is an alias to the single nextgen value`() {
        val module = Module("UnifiedRotationResetTest", Category.CLIENT, gameDetecting = false)
        val settings = RotationSettings(module)

        assertSame(settings.modernTicksUntilResetValue, settings.resetTicksValue)
        assertTrue(settings.modernTicksUntilResetValue.set(12, saveImmediately = false))
        assertEquals(12, settings.effectiveResetTicks)
        assertEquals(12, settings.resetTicks)
    }

    @Test
    fun `legacy config keys migrate to nextgen values without exposing legacy controls`() {
        val module = Module("UnifiedRotationMigrationTest", Category.CLIENT, gameDetecting = false)
        val settings = RotationSettings(module)

        assertSame(settings.modernTicksUntilResetValue, module.findDeep("ResetTicks"))
        assertSame(settings.modernResetThresholdValue, module.findDeep("AngleResetDifference"))
        assertSame(settings.modernHorizontalTurnSpeedValue, module.findDeep("HorizontalAngleChange"))
        assertSame(settings.modernVerticalTurnSpeedValue, module.findDeep("VerticalAngleChange"))
        assertSame(settings.modernShortStopValue, module.findDeep("SimulateShortStop"))
    }

    private companion object {
        val LEGACY_NAMES = setOf(
            "SimulateShortStop", "RotationDiffBuildUpToStop", "MaxThresholdAttemptsToStop",
            "Strafe", "Strict", "ResetTicks", "Legitimize", "HorizontalAngleChange",
            "VerticalAngleChange", "AngleResetDifference", "MinRotationDifference",
            "MinRotationDifferenceResetTiming",
        )
        val NEXTGEN_NAMES = setOf(
            "AngleSmooth", "MovementCorrection", "ResetThreshold", "TicksUntilReset",
            "IgnoreOpenInventory", "HorizontalTurnSpeed", "VerticalTurnSpeed", "ShortStop", "Fail",
        )
    }
}
