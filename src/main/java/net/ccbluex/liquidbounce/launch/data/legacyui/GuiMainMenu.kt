/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.btn.TestBtn
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.i18n.Language
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    /* For modification, please keep "Designed by XiGua" */
    override fun initGui() {
        val defaultHeight = (this.height / 3.5).toInt()
        this.buttonList.add(TestBtn(1, (this.width / 2) - (130 / 2), this.height / 2 - 20, 130, 23,  I18n.format("menu.singleplayer"), null, 2,
            Color(20, 20, 20, 130)))

        this.buttonList.add(TestBtn(2, (this.width / 2) - (130 / 2), this.height / 2 + 10, 130, 23,  I18n.format("menu.multiplayer"), null, 2,
            Color(20, 20, 20, 130)))

        this.buttonList.add(TestBtn(100, (this.width / 2) - (130 / 2), this.height / 2 + 40, 130, 23,  LanguageManager.get("ui.altmanager"), null, 2,
            Color(20, 20, 20, 130)))

        this.buttonList.add(TestBtn(103, (this.width / 2) - (130 / 2), this.height / 2 + 70, 130, 23,  LanguageManager.get("ui.mods"), null, 2,
            Color(20, 20, 20, 130)))

        this.buttonList.add(TestBtn(0, this.width - 35, 10, 25, 25, I18n.format("menu.options"), ResourceLocation("fdpclient/imgs/icon/setting.png"), 2,
            Color(20, 20, 20, 130)))

        this.buttonList.add(TestBtn(4, this.width - 65, 10, 25, 25, I18n.format("menu.quit"), ResourceLocation("fdpclient/imgs/icon/quit.png"), 2,
            Color(20, 20, 20, 130)))

        //this.buttonList.add(TestBtn(102, this.width - 95, 10, 25, 25, LanguageManager.get("ui.background"), ResourceLocation("fdpclient/imgs/icon/wallpaper.png"), 2,
        //    Color(20, 20, 20, 130)))


        /*
        this.buttonList.add(GuiButton(1, this.width / 2 - 50, defaultHeight, 100, 20, I18n.format("menu.singleplayer")))
        this.buttonList.add(GuiButton(2, this.width / 2 - 50, defaultHeight + 24, 100, 20, I18n.format("menu.multiplayer")))
        this.buttonList.add(GuiButton(100, this.width / 2 - 50, defaultHeight + 24 * 2, 100, 20, "%ui.altmanager%"))
        this.buttonList.add(GuiButton(103, this.width / 2 - 50, defaultHeight + 24 * 3, 100, 20, "%ui.mods%"))
        this.buttonList.add(GuiButton(102, this.width / 2 - 50, defaultHeight + 24 * 4, 100, 20, "%ui.background%"))
        this.buttonList.add(GuiButton(0, this.width / 2 - 50, defaultHeight + 24 * 5, 100, 20, I18n.format("menu.options")))
        this.buttonList.add(GuiButton(4, this.width / 2 - 50, defaultHeight + 24 * 6, 100, 20, I18n.format("menu.quit")))
        */
        super.initGui()
        }

override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    drawBackground(0)
    val defaultHeight = (this.height).toFloat()
    val defaultWidth = (this.width).toFloat()
    //RenderUtils.drawCircle(defaultWidth/2,defaultHeight/2 + 60F, 150F,Color(0,0,0,100).rgb);
    val i=0;
    val defaultHeight1 = (this.height).toDouble()
    val defaultWidth1 = (this.width).toDouble()
    FontLoaders.F40.drawCenteredString(LiquidBounce.CLIENT_NAME,this.width.toDouble()/2,this.height.toDouble()/2 - 60,Color(255,255,255,200).rgb)
    
    /* For modification, please keep "Designed by XiGua" */
    FontLoaders.F16.drawString("Made by UnlegitMC Team & Designed by XiGua",10f,this.height-15f,Color(255,255,255,170).rgb)
    var versionMsg="Version: "+LiquidBounce.CLIENT_VERSION+if (LiquidBounce.VERSIONTYPE.contains("Release")) " | Release" else " | "+LiquidBounce.VERSIONTYPE+" (May be isn't work)"
    FontLoaders.F16.drawString(versionMsg,this.width - FontLoaders.F16.getStringWidth(versionMsg) - 10F,this.height-15f,Color(255,255,255,170).rgb)
    /*val bHeight = (this.height / 3.5).toInt()

    Gui.drawRect(width / 2 - 60, bHeight - 30, width / 2 + 60, bHeight + 174, Integer.MIN_VALUE)

    mc.fontRendererObj.drawCenteredString(LiquidBounce.CLIENT_NAME, (width / 2).toFloat(), (bHeight - 20).toFloat(), Color.WHITE.rgb, false)
    mc.fontRendererObj.drawString(LiquidBounce.CLIENT_VERSION, 3F, (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
    "§cWebsite: §fhttps://${LiquidBounce.CLIENT_WEBSITE}/".also { str ->
        mc.fontRendererObj.drawString(str, (this.width - mc.fontRendererObj.getStringWidth(str) - 3).toFloat(), (height - mc.fontRendererObj.FONT_HEIGHT - 2).toFloat(), 0xffffff, false)
    }

    if(LiquidBounce.latest != LiquidBounce.CLIENT_VERSION && LiquidBounce.latest.isNotEmpty()) {
        val str = LanguageManager.getAndFormat("ui.update.released", LiquidBounce.latest)
        val start = width / 2f - (mc.fontRendererObj.getStringWidth(str) / 2f)
        RenderUtils.drawRect(start, 15f, start + mc.fontRendererObj.getStringWidth(str), 15f + mc.fontRendererObj.FONT_HEIGHT, Color.BLACK.rgb)
        mc.fontRendererObj.drawString(str, start, 15f, Color.WHITE.rgb, false)
    }*/

    super.drawScreen(mouseX, mouseY, partialTicks)
}

override fun actionPerformed(button: GuiButton) {
when (button.id) {
    0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
    1 -> mc.displayGuiScreen(GuiSelectWorld(this))
    2 -> mc.displayGuiScreen(GuiMultiplayer(this))
    4 -> mc.shutdown()
    100 -> mc.displayGuiScreen(GuiAltManager(this))
    102 -> mc.displayGuiScreen(GuiBackground(this))
    103 -> mc.displayGuiScreen(GuiModList(this))
}
}

override fun keyTyped(typedChar: Char, keyCode: Int) {}
}
