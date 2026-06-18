/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.core

import org.lwjgl.input.Keyboard

/**
 * Pure keybind-capture resolution shared by every style. ESCAPE, SPACE and
 * DELETE clear the binding (map to [Keyboard.KEY_NONE]); any other key is kept
 * as-is.
 */
object KeyCapture {

    fun resolve(keyCode: Int): Int = when (keyCode) {
        Keyboard.KEY_ESCAPE,
        Keyboard.KEY_SPACE,
        Keyboard.KEY_DELETE -> Keyboard.KEY_NONE
        else -> keyCode
    }
}
