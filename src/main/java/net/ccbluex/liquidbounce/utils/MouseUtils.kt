/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import org.lwjgl.input.Mouse
import java.nio.ByteBuffer

object MouseUtils {
    @JvmStatic
    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    fun setMouseButtonState(mouseButton: Int, held: Boolean) {
        val m = MouseEvent()
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent::class.java, m, mouseButton, "button")
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent::class.java, m, held, "buttonstate")
        MinecraftForge.EVENT_BUS.post(m)
        val buttons = ObfuscationReflectionHelper.getPrivateValue<ByteBuffer, Mouse?>(
            Mouse::class.java, null, "buttons"
        )
        buttons.put(mouseButton, (if (held) 1 else 0).toByte())
        ObfuscationReflectionHelper.setPrivateValue<Mouse?, ByteBuffer>(Mouse::class.java, null, buttons, "buttons")
    }
}