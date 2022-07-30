/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.command.commands

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.features.command.Command
import net.skiddermc.fdpclient.features.command.CommandManager
import net.skiddermc.fdpclient.features.module.modules.misc.KillInsults
import net.skiddermc.fdpclient.ui.cape.GuiCapeManager
import net.skiddermc.fdpclient.ui.font.Fonts

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
        KillInsults.loadFile()
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
