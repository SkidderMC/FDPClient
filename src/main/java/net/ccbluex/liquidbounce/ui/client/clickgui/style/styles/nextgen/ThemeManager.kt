/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import com.google.gson.JsonObject

/** Owns the browser theme's active virtual route and publishes route lifecycle changes. */
object ThemeManager {

    val availableScreens = setOf(
        "clickgui", "hud", "inventory", "title", "multiplayer", "altmanager",
        "singleplayer", "proxymanager", "disconnected", "browser", "none",
    )

    @Volatile
    var activeScreen: String = "none"
        private set

    fun url(screen: String): String = NextGenClickGuiServer.screenUrl(normalize(screen))

    fun open(screen: String) {
        val normalized = normalize(screen)
        activeScreen = normalized
        UiEventSocket.publish("virtualScreen", JsonObject().apply {
            addProperty("action", "open")
            addProperty("screenName", normalized)
        })
    }

    fun close(screen: String? = null) {
        if (screen != null && activeScreen != normalize(screen)) return
        val previous = activeScreen
        activeScreen = "none"
        UiEventSocket.publish("virtualScreen", JsonObject().apply {
            addProperty("action", "close")
            addProperty("screenName", previous)
        })
    }

    private fun normalize(screen: String): String =
        screen.trim().lowercase().takeIf(availableScreens::contains) ?: "none"
}
