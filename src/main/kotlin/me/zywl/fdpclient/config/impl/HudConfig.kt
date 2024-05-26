/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.config.impl

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.config.FileConfig
import net.ccbluex.liquidbounce.ui.hud.Config
import java.io.File

class HudConfig(file: File) : FileConfig(file) {
    override fun loadConfig(config: String) {
        FDPClient.hud.clearElements()
        FDPClient.hud = Config(config).toHUD()
    }

    override fun saveConfig(): String {
        return Config(FDPClient.hud).toJson()
    }
}