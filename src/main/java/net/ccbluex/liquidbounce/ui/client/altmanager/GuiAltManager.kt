/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager

import me.liuli.elixir.account.MinecraftAccount
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.SessionEvent
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiAdd
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiDirectLogin
import net.ccbluex.liquidbounce.ui.client.altmanager.sub.GuiMicrosoftLoginPending
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.login.LoginUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import net.minecraft.client.gui.GuiTextField
import net.minecraft.util.Session
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

class GuiAltManager(private val prevGui: GuiScreen) : GuiScreen() {
    var status = "§7${LanguageManager.getAndFormat("ui.alt.idle")}"
    private lateinit var altsList: GuiList

    override fun initGui() {
        altsList = GuiList(this)
        altsList.registerScrollButtons(7, 8)
        altsList.elementClicked(-1, false, 0, 0)
        altsList.scrollBy(-1 * altsList.slotHeight)
        val j = 22
        buttonList.add(GuiButton(1, width - 80, j + 24, 70, 20, "%ui.alt.add%"))
        buttonList.add(GuiButton(2, width - 80, j + 24 * 2, 70, 20, "%ui.alt.remove%"))
        buttonList.add(GuiButton(0, width - 80, height - 65, 70, 20, "%ui.back%"))
        buttonList.add(GuiButton(3, 5, j + 24, 90, 20, "%ui.alt.login%"))
        buttonList.add(GuiButton(6, 5, j + 24 * 2, 90, 20, "%ui.alt.directLogin%"))
        buttonList.add(GuiButton(11, 5, j + 24 * 3, 90, 20, "%ui.alt.msLogin%"))
        buttonList.add(GuiButton(4, 5, j + 24 * 4, 90, 20, "%ui.disconnect.randomAlt%"))
        buttonList.add(GuiButton(89, 5, j + 24 * 5, 90, 20, "%ui.disconnect.randomOffline%"))
        randomAltField.xPosition = 5
        randomAltField.yPosition = j + 24 * 6
        randomAltField.width = 90
        randomAltField.height = 20
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        altsList.drawScreen(mouseX, mouseY, partialTicks)
        mc.fontRendererObj.drawCenteredString(LanguageManager.getAndFormat("ui.altmanager"), (width / 2).toFloat(), 6f, 0xffffff)
        mc.fontRendererObj.drawCenteredString(LanguageManager.getAndFormat("ui.alt.alts", FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.size), (width / 2).toFloat(), 18f, 0xffffff)
        mc.fontRendererObj.drawCenteredString(status, (width / 2).toFloat(), 32f, 0xffffff)
        mc.fontRendererObj.drawStringWithShadow(LanguageManager.getAndFormat("ui.alt.username", mc.getSession().username), 6f, 6f, 0xffffff)
        mc.fontRendererObj.drawStringWithShadow(LanguageManager.getAndFormat("ui.alt.type", if (mc.getSession().token.length >= 32) "%ui.alt.type.premium%" else "%ui.alt.type.cracked%"), 6f, 15f, 0xffffff)
        randomAltField.drawTextBox()
        if (randomAltField.text.isEmpty() && !randomAltField.isFocused) {
            drawCenteredString(mc.fontRendererObj, "§7" + LanguageManager.getAndFormat("ui.alt.randomAltField"), width / 2 - 55, 66, 0xffffff)
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun actionPerformed(button: GuiButton) {
        if (!button.enabled) return
        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> mc.displayGuiScreen(GuiAdd(this))
            2 -> status = if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.size) {
                FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.removeAt(altsList.selectedSlot)
                FDPClient.fileManager.saveConfig(FDPClient.fileManager.accountsConfig)
                "§a${LanguageManager.getAndFormat("ui.alt.removed")}"
            } else {
                "§c${LanguageManager.getAndFormat("ui.alt.needSelect")}"
            }
            3 -> if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.size) {
                Thread {
                    val minecraftAccount = FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts[altsList.selectedSlot]
                    status = "§a${LanguageManager.getAndFormat("ui.alt.loggingIn")}"
                    status = login(minecraftAccount)
                }.start()
            } else {
                status = "§c${LanguageManager.getAndFormat("ui.alt.needSelect")}"
            }
            4 -> {
                if (FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.size <= 0) {
                    status = "§c${LanguageManager.getAndFormat("ui.alt.emptyList")}"
                    return
                }
                val randomInteger = Random().nextInt(FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.size)
                if (randomInteger < altsList.size) altsList.selectedSlot = randomInteger
                Thread {
                    val minecraftAccount =
                        FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts[randomInteger]
                    status = "§a${LanguageManager.getAndFormat("ui.alt.loggingIn")}"
                    status = login(minecraftAccount)
                }.start()
            }
            6 -> mc.displayGuiScreen(GuiDirectLogin(this))
            89 -> Thread { LoginUtils.randomCracked() }.start()
            11 -> mc.displayGuiScreen(GuiMicrosoftLoginPending(this))
        }
    }

    override fun updateScreen() {
        randomAltField.updateCursorCounter()
        super.updateScreen()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        randomAltField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_ESCAPE -> {
                FDPClient.fileManager.saveConfig(FDPClient.fileManager.specialConfig)
                mc.displayGuiScreen(prevGui)
                return
            }
            Keyboard.KEY_UP -> {
                var i = altsList.selectedSlot - 1
                if (i < 0) i = 0
                altsList.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_DOWN -> {
                var i = altsList.selectedSlot + 1
                if (i >= altsList.size) i = altsList.size - 1
                altsList.elementClicked(i, false, 0, 0)
            }
            Keyboard.KEY_RETURN -> {
                altsList.elementClicked(altsList.selectedSlot, true, 0, 0)
            }
            Keyboard.KEY_NEXT -> {
                altsList.scrollBy(height - 100)
            }
            Keyboard.KEY_PRIOR -> {
                altsList.scrollBy(-height + 100)
                return
            }
        }
        if (randomAltField.isFocused) randomAltField.textboxKeyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        altsList.handleMouseInput()
    }

    private inner class GuiList(prevGui: GuiScreen)
        : GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {

        var selectedSlot = 0
            get() {
                if (field > FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.size)
                    field = -1
                return field
            }

        override fun isSelected(id: Int): Boolean {
            return selectedSlot == id
        }

        public override fun getSize(): Int {
            return FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts.size
        }

        public override fun elementClicked(var1: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = var1
            if (doubleClick) {
                if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.size) {
                    Thread {
                        val minecraftAccount = FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts[altsList.selectedSlot]
                        status = "§a${LanguageManager.getAndFormat("ui.alt.loggingIn")}"
                        status = "§c" + login(minecraftAccount)
                    }.start()
                } else {
                    status = "§c${LanguageManager.getAndFormat("ui.alt.needSelect")}"
                }
            }
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val minecraftAccount = FDPClient.fileManager.accountsConfig.altManagerMinecraftAccounts[id]
            mc.fontRendererObj.drawCenteredString(minecraftAccount.name, width / 2f, y + 2f, Color.WHITE.rgb, true)
            mc.fontRendererObj.drawCenteredString(minecraftAccount.type, width / 2f, y + 15f, Color.LIGHT_GRAY.rgb, true)
        }

        override fun drawBackground() {}
    }

    companion object {
        var randomAltField = GuiTextField(2, Minecraft.getMinecraft().fontRendererObj, 0, 0, 0, 0)

        init {
            randomAltField.text = "FDP%n%n_%s%s%s%s"
            randomAltField.maxStringLength = Int.MAX_VALUE
        }

        fun login(account: MinecraftAccount): String {
            return try {
                val mc = Minecraft.getMinecraft()
                mc.session = account.session.let { Session(it.username, it.uuid, it.token, it.type) }
                FDPClient.eventManager.callEvent(SessionEvent())
                LanguageManager.getAndFormat("ui.alt.nameChanged", mc.session.username)
            } catch (e: Exception) {
                e.printStackTrace()
                LanguageManager.getAndFormat("ui.alt.error", e.message ?: "UNKNOWN")
            }
        }
    }
}