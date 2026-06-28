/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import org.lwjgl.input.Keyboard

enum class ModuleBindAction(val displayName: String) {
    TOGGLE("Toggle"),
    HOLD("Hold"),
    SMART("Smart");

    companion object {
        fun fromDisplayName(name: String?) =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: TOGGLE
    }
}

enum class ModuleBindModifier(val displayName: String, private vararg val keyCodes: Int) {
    SHIFT("Shift", Keyboard.KEY_LSHIFT, Keyboard.KEY_RSHIFT),
    CONTROL("Control", Keyboard.KEY_LCONTROL, Keyboard.KEY_RCONTROL),
    ALT("Alt", Keyboard.KEY_LMENU, Keyboard.KEY_RMENU),
    SUPER("Super", Keyboard.KEY_LMETA, Keyboard.KEY_RMETA);

    fun isPressed() = keyCodes.any(Keyboard::isKeyDown)

    fun matchesKey(key: Int) = keyCodes.any { it == key }

    companion object {
        fun fromDisplayName(name: String?) =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) }
    }
}
