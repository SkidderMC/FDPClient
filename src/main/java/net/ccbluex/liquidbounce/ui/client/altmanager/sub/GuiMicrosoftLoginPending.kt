package net.ccbluex.liquidbounce.ui.client.altmanager.sub

import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.compat.OAuthServer
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.intellij.lang.annotations.Language

class GuiMicrosoftLoginPending(private val prevGui: GuiScreen) : GuiScreen() {
    private var stage = "Initializing..."
    private lateinit var server: OAuthServer

    override fun initGui() {
        server = MicrosoftAccount.Companion.buildFromOpenBrowser(object : MicrosoftAccount.OAuthHandler {
            override fun openUrl(url: String) {
                stage = "Check your browser for continue..."
                ClientUtils.logInfo("Opening URL: $url")
                MiscUtils.showURL(url)
            }

            override fun authError(error: String) {
                stage = "Error: $error"
            }

            override fun authResult(account: MicrosoftAccount) {
                if (LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.any { it.name == account.name }) {
                    stage = "§c${LanguageManager.getAndFormat("ui.alt.alreadyAdded")}"
                    return
                }
                LiquidBounce.fileManager.accountsConfig.altManagerMinecraftAccounts.add(account)
                LiquidBounce.fileManager.saveConfig(LiquidBounce.fileManager.accountsConfig)
                mc.displayGuiScreen(prevGui)
            }
        })

        buttonList.add(GuiButton(0, width / 2 - 100, height / 4 + 120 + 12, "Cancel"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            server.stop(true)
            mc.displayGuiScreen(prevGui)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        this.drawDefaultBackground()

        fontRendererObj.drawCenteredString(stage, width / 2f, height / 2f - 50, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}