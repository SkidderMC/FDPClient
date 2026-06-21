/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import org.lwjgl.input.Keyboard

/**
 * Runs a chat message or command the moment you press a chosen key. Three independent
 * slots, each with its own LWJGL key code and text. Leave the text starting with "/" for
 * a command. Useful for one-tap queue joins, shouts, or quick toggles.
 */
object Macros : Module("Macros", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val key1 by int("Key1", 0, 0..255)
    private val text1 by text("Text1", "/hub")
    private val key2 by int("Key2", 0, 0..255)
    private val text2 by text("Text2", "gg")
    private val key3 by int("Key3", 0, 0..255)
    private val text3 by text("Text3", "")

    private val down = BooleanArray(3)

    val onUpdate = handler<UpdateEvent> {
        trigger(0, key1, text1)
        trigger(1, key2, text2)
        trigger(2, key3, text3)
    }

    private fun trigger(index: Int, key: Int, message: String) {
        if (key <= 0) {
            down[index] = false
            return
        }

        val pressed = Keyboard.isKeyDown(key)
        if (pressed && !down[index] && message.isNotBlank()) {
            mc.thePlayer?.sendChatMessage(message)
        }
        down[index] = pressed
    }
}
