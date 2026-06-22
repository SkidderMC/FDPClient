/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.config.BoolValue
import net.ccbluex.liquidbounce.config.ColorValue
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.config.CurveValue
import net.ccbluex.liquidbounce.config.FileValue
import net.ccbluex.liquidbounce.config.FloatRangeValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.FontValue
import net.ccbluex.liquidbounce.config.IntRangeValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.KeyBindValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.config.MultiSelectValue
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.config.Vec3Value

/**
 * Style-independent classification of every concrete value type, so a style can
 * pick a renderer without a chain of `is` checks of its own.
 */
enum class ValueKind {
    BOOL,
    INT,
    INT_RANGE,
    FLOAT,
    FLOAT_RANGE,
    TEXT,
    FILE,
    FONT,
    BLOCK,
    LIST,
    COLOR,
    MULTI_SELECT,
    KEY_BIND,
    VEC3,
    CURVE,
    OTHER
}

/**
 * Exhaustive mapping from a concrete value to its [ValueKind]. Configurable and
 * any unknown subclass fall through to [ValueKind.OTHER].
 */
fun Value<*>.kind(): ValueKind = when (this) {
    is BoolValue -> ValueKind.BOOL
    is IntValue -> ValueKind.INT
    is IntRangeValue -> ValueKind.INT_RANGE
    is FloatValue -> ValueKind.FLOAT
    is FloatRangeValue -> ValueKind.FLOAT_RANGE
    is TextValue -> ValueKind.TEXT
    is FileValue -> ValueKind.FILE
    is FontValue -> ValueKind.FONT
    is BlockValue -> ValueKind.BLOCK
    is ListValue -> ValueKind.LIST
    is ColorValue -> ValueKind.COLOR
    is MultiSelectValue -> ValueKind.MULTI_SELECT
    is KeyBindValue -> ValueKind.KEY_BIND
    is Vec3Value -> ValueKind.VEC3
    is CurveValue -> ValueKind.CURVE
    else -> ValueKind.OTHER
}

/**
 * Helpers for walking the values of a configurable container.
 */
object ValueDispatcher {

    /**
     * Values of [configurable] that should currently be drawn.
     */
    fun visible(configurable: Configurable): List<Value<*>> =
        configurable.values.filter { it.shouldRender() }
}
