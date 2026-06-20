/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.input

import org.lwjgl.input.Keyboard

/**
 * Bounds-safe replacement for [Keyboard.getKeyName].
 *
 * LWJGL's getKeyName indexes a fixed-size internal array and throws
 * ArrayIndexOutOfBoundsException for any out-of-range key code — notably
 * negative codes (mouse-button binds are stored as negatives) or codes
 * greater than or equal to [Keyboard.KEYBOARD_SIZE]. This wrapper returns
 * null for any invalid code so callers can fall back instead of crashing.
 */
fun safeKeyName(key: Int): String? =
    if (key in 0 until Keyboard.KEYBOARD_SIZE) Keyboard.getKeyName(key) else null
