/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import java.nio.Buffer
import java.nio.ByteBuffer

/**
 * Prevents crashes when flip() is called from higher Java versions.
 */
fun ByteBuffer.flipSafely() {
    try {
        flip()
    } catch (ex: Exception) {
        try {
            (this as Buffer).flip()
        } catch (any: Exception) {
            any.printStackTrace()
        }
    }
}