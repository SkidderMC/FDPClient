package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui

import com.mojang.realmsclient.gui.ChatFormatting
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.handler.api.ClientUpdate
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import java.awt.Color
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NlSetting {
    var x = 50
    var y = 100
    private var dragging = false
    private var x2 = 0
    private var y2 = 0
    var Light = false

    fun draw(mx: Int, my: Int) {
        if (dragging) {
            x = x2 + mx
            y = y2 + my
        }
        RoundedUtil.drawRound(x.toFloat(), y.toFloat(), 160f, 160f, 3f, if (Light) Color(238, 240, 235, 230) else Color(7, 13, 23, 230))
        Fonts.Nl_15.drawString("About ${FDPClient.CLIENT_NAME}", (x + 13).toFloat(), (y + 4).toFloat(), if (Light) Color(95, 95, 95).rgb else -1)
        Fonts.Nl_16_ICON.drawString("x", (x + 2).toFloat(), (y + 4).toFloat(), NeverloseGui.neverlosecolor.rgb)
        if (!Light) {
            NLOutline(FDPClient.CLIENT_NAME, Fonts.NLBold_35, x.toFloat(), (y + 30).toFloat(), -1, NeverloseGui.neverlosecolor.rgb, 160, 0.7f)
        } else {
            Fonts.NLBold_35.drawCenteredString(FDPClient.CLIENT_NAME, (x + 80).toFloat(), (y + 30).toFloat(), Color(51, 51, 51).rgb)
        }
        var version = FDPClient.clientVersionText
        if (version == "unknown") {
            version = FDPClient.CLIENT_VERSION
        }
        Fonts.Nl_18.drawString((if (!Light) ChatFormatting.WHITE else ChatFormatting.BLACK).toString() + "Version: " + ChatFormatting.RESET + version, (x + 10).toFloat(), (y + 65).toFloat(), NeverloseGui.neverlosecolor.rgb)
        val buildType = "Development"
        Fonts.Nl_18.drawString((if (!Light) ChatFormatting.WHITE else ChatFormatting.BLACK).toString() + "Build Type: " + ChatFormatting.RESET + buildType, (x + 10).toFloat(), (y + 65 + Fonts.Nl_18.height + 5).toFloat(), NeverloseGui.neverlosecolor.rgb)
        val gitInfo = ClientUpdate.gitInfo
        val rawBuildTime = gitInfo.getProperty("git.build.time", "Unknown")
        var formattedBuildTime = rawBuildTime
        try {
            formattedBuildTime = DateTimeFormatter.ofPattern("dd:MM HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(rawBuildTime))
        } catch (_: Exception) {
            try {
                formattedBuildTime = DateTimeFormatter.ofPattern("dd:MM HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(rawBuildTime.replace(" ", "T")))
            } catch (_: Exception) {
            }
        }
        Fonts.Nl_18.drawString((if (!Light) ChatFormatting.WHITE else ChatFormatting.BLACK).toString() + "Build Date: " + ChatFormatting.RESET + formattedBuildTime, (x + 10).toFloat(), (y + 65 + (Fonts.Nl_18.height + 5) * 2).toFloat(), NeverloseGui.neverlosecolor.rgb)
        Fonts.Nl_18.drawString((if (!Light) ChatFormatting.WHITE else ChatFormatting.BLACK).toString() + "Registered to: " + ChatFormatting.RESET + FDPClient.CLIENT_AUTHOR, (x + 10).toFloat(), (y + 65 + (Fonts.Nl_18.height + 5) * 3).toFloat(), NeverloseGui.neverlosecolor.rgb)
        Fonts.Nl_18.drawCenteredString("fdpclient @ 2019", x + 80f, (y + 65 + (Fonts.Nl_18.height + 5) * 4 + 7).toFloat(), if (Light) Color(95, 95, 95).rgb else -1)
        Fonts.Nl_18.drawString("Style", (x + 10).toFloat(), (y + 145).toFloat(), if (Light) Color(95, 95, 95).rgb else -1)
        if (Light) {
            RoundedUtil.drawRound((x + 39).toFloat(), (y + 143).toFloat(), 11.5f, 11.5f, 5.5f, NeverloseGui.neverlosecolor)
        }
        RoundedUtil.drawRound((x + 40).toFloat(), (y + 144).toFloat(), 9.5f, 9.5f, 4.5f, Color(210, 210, 210))
        if (!Light) {
            RoundedUtil.drawRound((x + 59).toFloat(), (y + 143).toFloat(), 11.5f, 11.5f, 5.5f, NeverloseGui.neverlosecolor)
        }
        RoundedUtil.drawRound((x + 60).toFloat(), (y + 144).toFloat(), 9.5f, 9.5f, 4.5f, Color(7, 13, 23, 230))
    }

    fun released(mx: Int, my: Int, mb: Int) {
        if (mb == 0) {
            dragging = false
        }
    }

    fun click(mx: Int, my: Int, mb: Int) {
        if (mb == 0) {
            if (RenderUtil.isHovering(x.toFloat(), y.toFloat(), 160f, 160f, mx, my)) {
                x2 = x - mx
                y2 = y - my
                dragging = true
            }
            if (RenderUtil.isHovering((x + 60).toFloat(), (y + 144).toFloat(), 9.5f, 9.5f, mx, my)) {
                Light = false
                dragging = false
            }
            if (RenderUtil.isHovering((x + 40).toFloat(), (y + 144).toFloat(), 9.5f, 9.5f, mx, my)) {
                Light = true
                dragging = false
            }
        }
    }

    companion object {
        @JvmStatic
        fun NLOutline(str: String, fontRenderer: FontRenderer, x: Float, y: Float, color: Int, color2: Int, w: Int, size: Float) {
            fontRenderer.drawCenteredString(str, x + w / 2f + size, y, color2, false)
            fontRenderer.drawCenteredString(str, x + w / 2f, y - size, color2, false)
            fontRenderer.drawCenteredString(str, x + w / 2f, y, color, false)
        }
    }
}
