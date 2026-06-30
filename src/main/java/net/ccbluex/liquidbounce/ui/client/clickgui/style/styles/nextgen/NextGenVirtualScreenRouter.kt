/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import net.ccbluex.liquidbounce.features.module.modules.client.ClickGUIModule
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.gui.GuiMainMenu
import net.ccbluex.liquidbounce.ui.client.gui.multiplayer.GuiServerSelect
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSelectWorld
import net.minecraft.client.gui.inventory.GuiInventory

/** Maps supported native screens to web-theme routes while retaining an escape-safe native fallback. */
object NextGenVirtualScreenRouter : MinecraftInstance {

    @Volatile
    private var bypassScreen: GuiScreen? = null

    @JvmStatic
    fun wrap(screen: GuiScreen?): GuiScreen? {
        if (screen == null || screen is NextGenClickGuiScreen) return null
        if (screen === bypassScreen) {
            bypassScreen = null
            return null
        }

        val route = routeFor(screen) ?: return null
        if (!ClickGUIModule.shouldUseVirtualScreen(route)) return null

        val fallback = if (screen is net.minecraft.client.gui.GuiMainMenu) GuiMainMenu() else screen
        return NextGenClickGuiScreen(route, fallback)
    }

    fun openRoute(route: String) {
        val normalized = route.trim().lowercase()
        if (normalized !in ThemeManager.availableScreens || normalized == "none") return
        mc.addScheduledTask {
            val fallback = mc.currentScreen?.takeUnless { it is NextGenClickGuiScreen }
            mc.displayGuiScreen(NextGenClickGuiScreen(normalized, fallback))
        }
    }

    fun closeCurrent() {
        mc.addScheduledTask {
            (mc.currentScreen as? NextGenClickGuiScreen)?.closeScreen() ?: mc.displayGuiScreen(null)
        }
    }

    fun displayNative(screen: GuiScreen) {
        bypassScreen = screen
        mc.displayGuiScreen(screen)
    }

    private fun routeFor(screen: GuiScreen): String? = when (screen) {
        is net.minecraft.client.gui.GuiMainMenu, is GuiMainMenu -> "title"
        is GuiMultiplayer, is GuiServerSelect -> "multiplayer"
        is GuiSelectWorld -> "singleplayer"
        is GuiAltManager -> "altmanager"
        is GuiDisconnected -> "disconnected"
        is GuiInventory -> "inventory"
        else -> null
    }
}
