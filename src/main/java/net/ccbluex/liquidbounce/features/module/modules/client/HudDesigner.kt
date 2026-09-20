/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen.NextGenClickGuiScreen
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner

object HudDesigner : Module("HudDesigner", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, canBeEnabled = false) {
    override fun onEnable() {
        openSelected()
    }

    fun openSelected() {
        if (HUDModule.modernElements) openModern(selectMode = false) else openLegacy(selectMode = false)
    }

    fun openModern(selectMode: Boolean = true) {
        if (selectMode) selectMode("Modern")
        mc.displayGuiScreen(NextGenClickGuiScreen(openHudEditor = true))
    }

    fun openLegacy(selectMode: Boolean = true) {
        if (selectMode) selectMode("Legacy")
        mc.displayGuiScreen(GuiHudDesigner())
    }

    private fun selectMode(mode: String) {
        if (HUDModule.elements == mode) return
        HUDModule.elements = mode
        FileManager.saveConfig(FileManager.valuesConfig)
    }
}
