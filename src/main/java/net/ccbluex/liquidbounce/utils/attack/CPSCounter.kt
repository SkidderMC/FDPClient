/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.attack

import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import org.lwjgl.input.Mouse
import java.nio.ByteBuffer

object CPSCounter {
    private const val MAX_CPS = 50
    private val TIMESTAMP_BUFFERS = Array(MouseButton.entries.size) { RollingArrayLongBuffer(MAX_CPS) }

    /**
     * Registers a mouse button click
     *
     * @param button The clicked button
     */
    fun registerClick(button: MouseButton) = TIMESTAMP_BUFFERS[button.ordinal].add(runTimeTicks.toLong())

    /**
     * Gets the count of clicks that have occurred in the last 1000ms
     *
     * @param button The mouse button
     * @return The CPS
     */
    fun getCPS(button: MouseButton, timeStampsSince: Int = runTimeTicks - 20) =
        TIMESTAMP_BUFFERS[button.ordinal].getTimestampsSince(timeStampsSince.toLong())

    enum class MouseButton { LEFT, MIDDLE, RIGHT }


    fun mouseWithinBounds(mouseX: Int, mouseY: Int, x: Float, y: Float, x2: Float, y2: Float) = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    fun isHovering(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int): Boolean {
        return mouseX in x..width && mouseY >= y && mouseY <= height
    }

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
