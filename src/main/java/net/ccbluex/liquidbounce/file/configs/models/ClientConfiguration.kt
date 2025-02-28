/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs.models

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion
import net.ccbluex.liquidbounce.utils.render.IconUtils
import org.lwjgl.opengl.Display

object ClientConfiguration : Configurable("ClientConfiguration"), MinecraftInstance {
    var clientTitle by boolean("ClientTitle", true)
    var customBackground by boolean("CustomBackground", true)
    var particles by boolean("Particles", true)
    var stylisedAlts by boolean("StylisedAlts", true)
    var unformattedAlts by boolean("CleanAlts", true)
    var altsLength by int("AltsLength", 16, 4..20)
    var altsPrefix by text("AltsPrefix", "")
    // The game language can be overridden by the user. empty=default
    var overrideLanguage by text("OverrideLanguage","")

    fun updateClientWindow() {
        if (clientTitle) {
            // Set FDP title
            Display.setTitle(FDPClient.clientTitle)
            // Update favicon
            IconUtils.getFavicon()?.let { icons ->
                Display.setIcon(icons)
            }
        } else {
            // Set original title
            Display.setTitle("Minecraft 1.8.9")
            // Update favicon
            mc.setWindowIcon()
        }
    }
}