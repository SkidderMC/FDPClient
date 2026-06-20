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
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory

/**
 * Automatically closes container screens (chests, shops, forced server menus) a short delay after
 * they open. Useful against anti-AFK menus and servers that pop GUIs in your face.
 */
object GUICloser : Module("GUICloser", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val delay by int("Delay", 200, 0..5000)
    private val includeInventory by boolean("IncludeInventory", false)
    private val printScreenTitle by boolean("PrintScreenTitle", false)

    private var openAt = -1L

    val onUpdate = handler<UpdateEvent> {
        mc.thePlayer ?: return@handler
        val screen = mc.currentScreen

        val target = screen is GuiContainer && (includeInventory || screen !is GuiInventory)

        if (!target) {
            openAt = -1L
            return@handler
        }

        if (openAt < 0L) {
            openAt = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - openAt >= delay) {
            if (printScreenTitle) {
                val title = if (screen is GuiChest) screen.lowerChestInventory?.name else screen.javaClass.simpleName
                chat("§3Closed screen: §7${title ?: screen.javaClass.simpleName}")
            }
            mc.displayGuiScreen(null)
            openAt = -1L
        }
    }

    override fun onEnable() {
        openAt = -1L
        super.onEnable()
    }
}
