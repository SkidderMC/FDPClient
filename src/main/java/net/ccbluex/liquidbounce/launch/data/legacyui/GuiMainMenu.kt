/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.btn.TestBtn
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.*
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    var drawed = false
    var clicked = false
    var displayed = false
    fun drawBtns() {
        this.buttonList.add(
            TestBtn(
                100,
                (this.width / 2) - (130 / 2),
                this.height / 2 - 20,
                130,
                23,
                I18n.format("menu.singleplayer"),
                null,
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                101,
                (this.width / 2) - (130 / 2),
                this.height / 2 + 10,
                130,
                23,
                I18n.format("menu.multiplayer"),
                null,
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                200,
                (this.width / 2) - (130 / 2),
                this.height / 2 + 40,
                130,
                23,
                LanguageManager.get("ui.altmanager"),
                null,
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                102,
                (this.width / 2) - (130 / 2),
                this.height / 2 + 70,
                130,
                23,
                LanguageManager.get("ui.mods"),
                null,
                2,
                Color(20, 20, 20, 130)
            )
        )


        this.buttonList.add(
            TestBtn(
                104,
                this.width - 35,
                10,
                25,
                25,
                I18n.format("menu.quit"),
                ResourceLocation("fdpclient/imgs/icon/quit.png"),
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                103,
                this.width - 65,
                10,
                25,
                25,
                I18n.format("menu.options").replace(".", ""),
                ResourceLocation("fdpclient/imgs/icon/setting.png"),
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                201,
                this.width - 95,
                10,
                25,
                25,
                I18n.format("ui.background"),
                ResourceLocation("fdpclient/imgs/icon/wallpaper.png"),
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                202,
                this.width - 125,
                10,
                25,
                25,
                "Announcement",
                ResourceLocation("fdpclient/imgs/icon/announcement.png"),
                2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                203, this.width - 155, 10, 25, 25, "Discord", ResourceLocation("fdpclient/imgs/icon/discord.png"), 2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                204, this.width - 185, 10, 25, 25, "Website", ResourceLocation("fdpclient/imgs/icon/website.png"), 2,
                Color(20, 20, 20, 130)
            )
        )

        this.buttonList.add(
            TestBtn(
                205, 20, 10, 25, 25, "Toggle theme", ResourceLocation("fdpclient/imgs/icon/moon-night.png"), 2,
                Color(20, 20, 20, 130)
            )
        )

        drawed = true
    }

    /* For modification, please keep "Designed by SkidderMC" */
    override fun initGui() {
        val defaultHeight = (this.height / 3.5).toInt()
        Thread {
            if (FDPClient.CLIENTTEXT.contains("Waiting") || FDPClient.CLIENTTEXT.contains("Oops")) {
                try {
                    FDPClient.CLIENTTEXT = HttpUtils.get("http://fdpinfo.github.io/changelogs")
                } catch (e: Exception) {
                    try {
                        FDPClient.CLIENTTEXT = HttpUtils.get("http://fdpinfo.github.io/changelogs")
                    } catch (e: Exception) {
                        FDPClient.CLIENTTEXT = "Oops.. :(\$Can't get information!#Try reopen the main menu\$140\$80"
                    }
                }
            }
        }.start()

        drawBtns()
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        val defaultHeight = (this.height).toFloat()
        val defaultWidth = (this.width).toFloat()
        val i = 0
        val defaultHeight1 = (this.height).toDouble()
        val defaultWidth1 = (this.width).toDouble()
        FontLoaders.F40.drawCenteredString(
            FDPClient.CLIENT_NAME,
            this.width.toDouble() / 2,
            this.height.toDouble() / 2 - 60,
            if (FDPClient.Darkmode) {
                Color(255, 255, 255, 200).rgb
            } else {
                Color(1, 1, 1, 170).rgb
            }
        )


        FontLoaders.F16.drawString(
            "Made by SkidderMC",
            10f,
            this.height - 15f,
            Color(1, 1, 1, 170).rgb
        )
        FontLoaders.F16.drawString(
            FDPClient.CLIENT_NAME,
            10f,
            this.height - 25f,
            Color(1, 1, 1, 170).rgb
        )
        var versionMsg =
            "Version: " + FDPClient.CLIENT_VERSION + if (FDPClient.VERSIONTYPE.contains("Release")) " | Release" else " | " + FDPClient.VERSIONTYPE + " (Bleeding Edge)"
        FontLoaders.F16.drawString(
            versionMsg,
            this.width - FontLoaders.F16.getStringWidth(versionMsg) - 10F,
            this.height - 15f,
            Color(1, 1, 1, 170).rgb
        )
        try {
            if (!displayed) {
                var back = Layer.draw(
                    defaultWidth.toInt(),
                    defaultHeight1.toInt(),
                    FDPClient.CLIENTTEXT.split("$")[2].toFloat(),
                    FDPClient.CLIENTTEXT.split("$")[3].toFloat(),
                    FDPClient.CLIENTTEXT.split("$")[0],
                    FDPClient.CLIENTTEXT.split("$")[1].replace("%VERSION%", FDPClient.CLIENT_VERSION),
                    255,
                    mouseX,
                    mouseY,
                    clicked
                )
                if (back == 1) {
                    drawed = false
                    buttonList.removeAll(buttonList)
                } else if (back == 2) {
                    displayed = true
                    drawBtns()
                }
                if (drawed && back != 1) {
                    //drawBtns()
                }
                clicked = false
            } else {
                if (!drawed) {
                    drawBtns()
                }
            }
        } catch (e: Exception) {
            //My HardDisk Exploded :(
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(p_mouseClicked_1_: Int, i2: Int, i3: Int) {
        clicked = true
        super.mouseClicked(p_mouseClicked_1_, i2, i3)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            100 -> mc.displayGuiScreen(GuiSelectWorld(this))
            101 -> mc.displayGuiScreen(GuiMultiplayer(this))
            102 -> mc.displayGuiScreen(GuiModList(this))
            103 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
            104 -> mc.shutdown()
            200 -> mc.displayGuiScreen(GuiAltManager(this))
            201 -> mc.displayGuiScreen(GuiBackground(this))
            202 -> displayed = false
            203 -> MiscUtils.showURL("https://${FDPClient.CLIENT_WEBSITE}/discord.html")
            204 -> MiscUtils.showURL("https://${FDPClient.CLIENT_WEBSITE}")
            205 -> FDPClient.Darkmode = !FDPClient.Darkmode
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}
