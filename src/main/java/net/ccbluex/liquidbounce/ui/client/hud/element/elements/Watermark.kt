/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule
import net.ccbluex.liquidbounce.features.module.modules.visual.NameProtect
import net.ccbluex.liquidbounce.features.module.modules.other.AnticheatDetector
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import java.awt.Color

@ElementInfo(name = "Watermark")
class Watermark : Element("Watermark") {

    private val showPlayerName by boolean("Show Player Name", true)
    private val showFPS by boolean("Show FPS", false)
    private val showPosition by boolean("Show Position", true)
    private val showPing by boolean("Show Ping", true)
    private val showTPS by boolean("Show TPS", true)
    private val showAnticheat by boolean("Show Anticheat", true)
    private val showOnline by boolean("Show Online", true)

    private fun getTPS(): Float {
        return HUDModule.tps
    }

    override fun drawElement(): Border {
        val mc = Minecraft.getMinecraft()

        val posX = 4.0f
        val posY = 4.0f
        val iconSize = 5.0f
        val rectWidth = 10.0f
        val bgColorRGB = ClientThemesUtils.getBackgroundColor(0, 120).rgb
        val mainColor = ClientThemesUtils.getColor().rgb

        val title = "FDP"
        val titleWidth = Fonts.InterMedium_15.stringWidth(title)
        RenderUtils.drawCustomShapeWithRadius(
            posX,
            posY,
            rectWidth + iconSize * 2.5f + titleWidth,
            rectWidth + iconSize * 2.0f,
            4.0f,
            Color(bgColorRGB, true)
        )
        Fonts.Nursultan18.drawString(
            "S",
            posX + iconSize,
            posY + 2 + iconSize - 1.0f + 2f,
            mainColor
        )
        Fonts.InterMedium_15.drawString(
            title,
            posX + rectWidth + iconSize * 1.5f,
            posY + rectWidth / 2.0f + 1.5f + 2f,
            mainColor
        )

        val playerName = getProtectedName()
        val playerNameWidth = Fonts.InterMedium_15.stringWidth(playerName)
        val playerNameX = posX + rectWidth + iconSize * 2.5f + titleWidth + iconSize

        if (showPlayerName) {
            RenderUtils.drawCustomShapeWithRadius(
                playerNameX,
                posY,
                rectWidth + iconSize * 2.5f + playerNameWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.InterMedium_15.drawString(
                "W",
                playerNameX + iconSize,
                posY + 1 + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                playerName,
                playerNameX + iconSize * 1.5f + rectWidth,
                posY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val fps = Minecraft.getDebugFPS()
        val fpsText = "$fps FPS"
        val fpsTextWidth = Fonts.InterMedium_15.stringWidth(fpsText)
        val fpsX = playerNameX + rectWidth + iconSize * 2.5f + playerNameWidth + iconSize

        if (showFPS) {
            RenderUtils.drawCustomShapeWithRadius(
                fpsX,
                posY,
                rectWidth + iconSize * 2.5f + fpsTextWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                "X",
                fpsX + iconSize,
                posY + 1 + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                fpsText,
                fpsX + iconSize * 1.5f + rectWidth,
                posY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val playerPosition = "${mc.thePlayer.posX.toInt()} ${mc.thePlayer.posY.toInt()} ${mc.thePlayer.posZ.toInt()}"
        val positionTextWidth = Fonts.InterMedium_15.stringWidth(playerPosition)
        val positionY = posY + rectWidth + iconSize * 2.0f + iconSize

        if (showPosition) {
            RenderUtils.drawCustomShapeWithRadius(
                posX,
                positionY,
                rectWidth + iconSize * 2.5f + positionTextWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                "F",
                posX + iconSize,
                positionY + 1.5f + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                playerPosition,
                posX + iconSize * 1.5f + rectWidth,
                positionY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val ping = try {
            mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID)?.responseTime ?: 0
        } catch (e: Exception) {
            0
        }
        val pingText = "$ping Ping"
        val pingTextWidth = Fonts.InterMedium_15.stringWidth(pingText)
        val pingX = posX + rectWidth + iconSize * 2.5f + positionTextWidth + iconSize

        if (showPing) {
            RenderUtils.drawCustomShapeWithRadius(
                pingX,
                positionY,
                rectWidth + iconSize * 2.5f + pingTextWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                "Q",
                pingX + iconSize,
                positionY + 1 + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                pingText,
                pingX + iconSize * 1.5f + rectWidth,
                positionY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val onlineCount = try {
            val fromNetHandler = try { mc.netHandler.playerInfoMap.size } catch (t: Throwable) { null }
            fromNetHandler ?: mc.theWorld?.playerEntities?.size ?: 1
        } catch (_: Throwable) {
            1
        }
        val onlineText = "Online $onlineCount"
        val onlineTextWidth = Fonts.InterMedium_15.stringWidth(onlineText)
        val onlineX = pingX + rectWidth + iconSize * 2.5f + pingTextWidth + iconSize

        if (showOnline) {
            RenderUtils.drawCustomShapeWithRadius(
                onlineX,
                positionY,
                rectWidth + iconSize * 2.5f + onlineTextWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                "Y",
                onlineX + iconSize,
                positionY + 1 + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                onlineText,
                onlineX + iconSize * 1.5f + rectWidth,
                positionY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val tpsText = "TPS: %.2f".format(getTPS())
        val tpsIcon = "C"
        val tpsY = positionY + rectWidth + iconSize * 2.0f + 5f

        if (showTPS) {
            RenderUtils.drawCustomShapeWithRadius(
                posX,
                tpsY,
                rectWidth + iconSize * 2.5f + Fonts.InterMedium_15.stringWidth(tpsText),
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                tpsIcon,
                posX + iconSize,
                tpsY + 1.5f + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                tpsText,
                posX + iconSize * 1.5f + rectWidth,
                tpsY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        if (showAnticheat && AnticheatDetector.state) {
            val acName = AnticheatDetector.detectedACName.ifEmpty { "None" }
            val tpsBoxWidth = rectWidth + iconSize * 2.5f + Fonts.InterMedium_15.stringWidth(tpsText)
            val anticheatX = posX + tpsBoxWidth + iconSize

            val acTextWidth = Fonts.InterMedium_15.stringWidth(acName)
            RenderUtils.drawCustomShapeWithRadius(
                anticheatX,
                tpsY,
                rectWidth + iconSize * 2.5f + acTextWidth,
                rectWidth + iconSize * 2.0f,
                4.0f,
                Color(bgColorRGB, true)
            )
            Fonts.Nursultan18.drawString(
                "N",
                anticheatX + iconSize,
                tpsY + 1.5f + iconSize + 2f,
                mainColor
            )
            Fonts.InterMedium_15.drawString(
                acName,
                anticheatX + iconSize * 1.5f + rectWidth,
                tpsY + rectWidth / 2.0f + 1.5f + 2f,
                -1
            )
        }

        val overallWidth = maxOf(
            posX + rectWidth + iconSize * 2.5f + titleWidth,
            playerNameX + rectWidth + iconSize * 2.5f + playerNameWidth,
            fpsX + rectWidth + iconSize * 2.5f + fpsTextWidth,
            posX + rectWidth + iconSize * 2.5f + positionTextWidth,
            pingX + rectWidth + iconSize * 2.5f + pingTextWidth,
            onlineX + rectWidth + iconSize * 2.5f + onlineTextWidth,
            posX + rectWidth + iconSize * 2.5f + Fonts.InterMedium_15.stringWidth(tpsText)
        )
        val overallHeight = tpsY + rectWidth + iconSize * 2.0f

        return Border(0F, 0F, overallWidth, overallHeight)
    }

    private fun getProtectedName(): String {
        return if (NameProtect.state) {
            ColorUtils.stripColor(NameProtect.handleTextMessage(mc.thePlayer.name))
        } else {
            mc.thePlayer.name
        }
    }
}