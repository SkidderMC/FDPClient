/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.modules.misc.Insult
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager
import net.ccbluex.liquidbounce.ui.font.Fonts

class ReloadCommand : Command("reload", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        alert("Reloading...")
        alert("§c§lReloading commands...")
        FDPClient.commandManager = CommandManager()
        FDPClient.commandManager.registerCommands()
        FDPClient.isStarting = true
        FDPClient.isLoadingConfig = true
        FDPClient.scriptManager.disableScripts()
        FDPClient.scriptManager.unloadScripts()
        for (module in FDPClient.moduleManager.modules)
            FDPClient.moduleManager.generateCommand(module)
        alert("§c§lReloading scripts...")
        FDPClient.scriptManager.loadScripts()
        FDPClient.scriptManager.enableScripts()
        alert("§c§lReloading fonts...")
        Fonts.loadFonts()
        alert("§c§lReloading modules...")
        FDPClient.configManager.load(FDPClient.configManager.nowConfig, false)
        Insult.loadFile()
        GuiCapeManager.load()
        alert("§c§lReloading accounts...")
        FDPClient.fileManager.loadConfig(FDPClient.fileManager.accountsConfig)
        alert("§c§lReloading friends...")
        FDPClient.fileManager.loadConfig(FDPClient.fileManager.friendsConfig)
        alert("§c§lReloading xray...")
        FDPClient.fileManager.loadConfig(FDPClient.fileManager.xrayConfig)
        alert("§c§lReloading HUD...")
        FDPClient.fileManager.loadConfig(FDPClient.fileManager.hudConfig)
        alert("Reloaded.")
        FDPClient.isStarting = false
        FDPClient.isLoadingConfig = false
        System.gc()
    }
}
