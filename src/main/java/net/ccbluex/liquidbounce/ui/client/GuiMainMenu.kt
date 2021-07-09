/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import java.awt.Color
import java.io.UnsupportedEncodingException
import java.lang.StringBuilder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and
import kotlin.experimental.or

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    override fun initGui() {
        if(LiquidBounce.latestVersion.isNotEmpty()&&!LiquidBounce.displayedUpdateScreen){
            mc.displayGuiScreen(GuiUpdate())
            return
        }

        val defaultHeight = (this.height / 3.5).toInt()

        this.buttonList.add(GuiButton(1, this.width / 2 - 50, defaultHeight, 100, 20, I18n.format("menu.singleplayer")))
        this.buttonList.add(GuiButton(2, this.width / 2 - 50, defaultHeight + 24, 100, 20, I18n.format("menu.multiplayer")))
        this.buttonList.add(GuiButton(100, this.width / 2 - 50, defaultHeight + 24*2, 100, 20, "AltManager"))
        this.buttonList.add(GuiButton(103, this.width / 2 - 50, defaultHeight + 24*3, 100, 20, "Mods"))
        this.buttonList.add(GuiButton(102, this.width / 2 - 50, defaultHeight + 24*4, 100, 20, "Background"))
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, defaultHeight + 24*5, 100, 20, I18n.format("menu.options")))
        this.buttonList.add(GuiButton(4, this.width / 2 - 50, defaultHeight + 24*6, 100, 20, I18n.format("menu.quit")))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        val bHeight=(this.height / 3.5).toInt()

        Gui.drawRect(width / 2 - 60, bHeight - 30, width / 2 + 60, bHeight + 174, Integer.MIN_VALUE)

        Fonts.font40.drawCenteredString(LiquidBounce.CLIENT_NAME,(width / 2).toFloat(), (bHeight - 20).toFloat(),Color.WHITE.rgb,false)
        Fonts.font40.drawString(LiquidBounce.CLIENT_VERSION+if(LiquidBounce.latestVersion.isNotEmpty()){" §c-> §a"+LiquidBounce.latestVersion}else{""}
            , 3F, (height - Fonts.font35.FONT_HEIGHT).toFloat(), 0xffffff,  false)
        val str="§cWebsite: §fhttps://fdp.liulihaocai.pw/"
        Fonts.font40.drawString(str, (this.width - Fonts.font40.getStringWidth(str) - 3).toFloat(), (height - Fonts.font35.FONT_HEIGHT).toFloat(), 0xffffff, false)
        super.drawScreen(mouseX, mouseY, partialTicks)
        var ULY = 2f
        val UpdateLogs = arrayOf(
            "FDPClient Discord: https://discord.gg/dJtjF7swH9", "",
            "China QQ Group: 523201000", "",
            "Github: https://github.com/Project-EZ4H/FDPClient", "", "",
            "中国用户QQ群: 523201000", "",
            "本水影完全免费开源，如果付费获得则代表你被圈了", "", "",
            "Updated Logs:", "",
            "===========================================", "",
            "1.2.2:","",
            "[+] Added liquidbounce and flux target hud ","",
            "[+] Added new clickgui","",
            "[+] Improved velocity","",
            "[+] Added proxy manager","",
            "[+] Improved spammer","",
            "[-] Removed keep sword ","",
            "[+] Added old hypixel, MC986 and Verus disablers ","",
            "[+] Added enchant effect","",
            "[+] Added clip command","",
            "[+] Added chat animation","",
            "[+] Added head mode to gapple","",
            "[+] Added click mode to Infinite aura","",
            "[+] Added clickgui manager","",
            "[+] Added core.lib compatibility","",
            "[+] Added speed only option to target strafe","",
            "[+] Added fly flag, tp back, blink and ground spoof modes to antivoid","",
            "[+] Added blink auto scaffold mode to antivoid","",
            "[+] Bug fixes","",
            "[+] Changed deafult settings","",
            "[+] Other improvements","",
            "===========================================", ""

        )

        for (ChangeLog in UpdateLogs) {
            if (ChangeLog != null) {
                Fonts.font35.drawStringWithShadow(ChangeLog, 2f, ULY, Color(255, 255, 255, 160).rgb)
            }
            ULY += 5f
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            1 -> mc.displayGuiScreen(GuiSelectWorld(this))
            2 -> mc.displayGuiScreen(GuiMultiplayer(this))
            4 -> mc.shutdown()
            100 -> mc.displayGuiScreen(GuiAltManager(this))
            102 -> mc.displayGuiScreen(GuiBackground(this))
            103 -> mc.displayGuiScreen(GuiModsMenu(this))
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}
