/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.sound.TipSoundManager
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation

import org.lwjgl.input.Keyboard

class GuiInfo(private val prevGui: GuiScreen) : GuiScreen() {

    private val BUTTON_VERSIONS = 2

    override fun initGui() {
        buttonList.add(GuiButton(1, width / 2 - 100, height / 4 + 20 + 4, "Tools"))
        buttonList.add(GuiButton(2, width / 2 - 100, height / 4 + 55 + 2, "Version"))
        buttonList.add(GuiButton(3, width / 2 - 100, height / 4 + 90 - 2, "Website"))
        buttonList.add(GuiButton(4, width / 2 - 100, height / 4 + 125 - 4, "Staff"))
        buttonList.add(GuiButton(5, width / 2 - 100, height / 4 + 160 - 6, "Reload"))
        buttonList.add(GuiButton(6, width / 2 - 100, height / 4 + 195 - 8, "Done"))
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        RenderUtils.drawImage(ResourceLocation("fdpclient/ui/mainmenu/portal.png"), 0, 0, width, height)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevGui)
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiBackground(this))
            2 -> BUTTON_VERSIONS
            3 -> MiscUtils.showURL(FDPClient.CLIENT_WEBSITE)
            5 -> {
                FDPClient.commandManager = CommandManager()
                FDPClient.commandManager.registerCommands()
                for (module in FDPClient.moduleManager.modules)
                    FDPClient.moduleManager.generateCommand(module)
                FDPClient.tipSoundManager = TipSoundManager()
                FDPClient.combatManager = CombatManager()
                FDPClient.fileManager.loadConfig(FDPClient.fileManager.hudConfig)
                FDPClient.fileManager.loadConfig(FDPClient.fileManager.accountsConfig)
                FDPClient.fileManager.loadConfig(FDPClient.fileManager.friendsConfig)
            }
            6 -> mc.displayGuiScreen(prevGui)
        //   BUTTON_VERSIONS -> mc.displayGuiScreen(GuiProtocolSelector(this))
        }
    }
}