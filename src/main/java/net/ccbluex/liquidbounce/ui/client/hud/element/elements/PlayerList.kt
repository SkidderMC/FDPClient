
/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */ 
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * A player list
 */
@ElementInfo(name = "PlayerList")
class PlayerList : Element() {

    private val decimalFormat3 = DecimalFormat("0.#", DecimalFormatSymbols(Locale.ENGLISH))
    private val sortValue = ListValue("Sort", arrayOf("Alphabet", "Distance", "Health"), "Alphabet")
    private val fontOffsetValue = FloatValue("Font-Offset", 0F, 3F, -3F)
    private val reverseValue = BoolValue("Reverse", false)
    private val fontValue = FontValue("Font", Fonts.font35)
    private val shadowValue = BoolValue("Shadow", false)
    private val lineValue = BoolValue("Line", true)
    private val redValue = IntegerValue("Red", 255, 0, 255)
    private val greenValue = IntegerValue("Green", 255, 0, 255)
    private val blueValue = IntegerValue("Blue", 255, 0, 255)
    private val alphaValue = IntegerValue("Alpha", 255, 0, 255)
    private val bgredValue = IntegerValue("Background-Red", 0, 0, 255)
    private val bggreenValue = IntegerValue("Background-Green", 0, 0, 255)
    private val bgblueValue = IntegerValue("Background-Blue", 0, 0, 255)
    private val bgalphaValue = IntegerValue("Background-Alpha", 120, 0, 255)
    private val rainbowList = ListValue("Rainbow", arrayOf("Normal", "Custom", "Slowly", "Off"), "Off")
    private val saturationValue = FloatValue("Saturation", 0.9f, 0f, 1f)
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f)
    private val distanceValue = IntegerValue("Line-Distance", 0, 0, 400)
    private val gradientAmountValue = IntegerValue("Gradient-Amount", 25, 1, 50)

    override fun drawElement(partialTicks: Float): Border {
        val reverse = reverseValue.get()
        val font = fontValue.get()
        val fontOffset = fontOffsetValue.get()
        val rainbowType = rainbowList.get().toLowerCase()

        var nameLength: Float
        var hpLength = font.getStringWidth("Health").toFloat()
        var distLength = font.getStringWidth("Distance").toFloat()

        var height = 4F + font.FONT_HEIGHT.toFloat()

        val color = Color(redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()).rgb
        val bgColor = Color(bgredValue.get(), bggreenValue.get(), bgblueValue.get(), bgalphaValue.get())

        var playerList = mc.theWorld.playerEntities.filter { !AntiBot.isBot(it) && it != mc.thePlayer }.toMutableList()

        nameLength = font.getStringWidth("Name (${playerList.size})").toFloat()

        when (sortValue.get()) {
            "Alphabet" -> playerList.sortWith(compareBy { it.name.toLowerCase() })
            "Distance" -> playerList.sortWith(Comparator{ a, b -> mc.thePlayer.getDistanceToEntityBox(a).compareTo(mc.thePlayer.getDistanceToEntityBox(b)) })
            else -> playerList.sortWith(Comparator{ a, b -> a.health.compareTo(b.health) })
        }

        if (reverse) playerList = playerList.reversed().toMutableList()

        playerList.forEach {
            if (font.getStringWidth(it.name) > nameLength)
                nameLength = font.getStringWidth(it.name).toFloat()

            if (font.getStringWidth("${decimalFormat3.format(it.health)} HP") > hpLength)
                hpLength = font.getStringWidth("${decimalFormat3.format(it.health)} HP").toFloat()

            if (font.getStringWidth("${decimalFormat3.format(mc.thePlayer.getDistanceToEntityBox(it))}m") > distLength)
                distLength = font.getStringWidth("${decimalFormat3.format(mc.thePlayer.getDistanceToEntityBox(it))}m").toFloat()
        }

        if (lineValue.get()) {
            val barLength = (nameLength + hpLength + distLength + 50F).toDouble()

            loop@ for (i in 0 until gradientAmountValue.get()) {
                val barStart = i.toDouble() / gradientAmountValue.get().toDouble() * barLength
                val barEnd = (i + 1).toDouble() / gradientAmountValue.get().toDouble() * barLength
                RenderUtils.drawGradientSideways(barStart, -1.0, barEnd, 0.0, 
                when (rainbowType) {
                    "normal" -> ColorUtils.rainbow(0).rgb
                    "custom" -> color
                    "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), i * distanceValue.get(), saturationValue.get(), brightnessValue.get()).rgb
                    else -> continue@loop
                },
                when (rainbowType) {
                    "normal" -> ColorUtils.rainbow(distanceValue.get()).rgb
                    "custom" -> color
                    "slowly" -> ColorUtils.slowlyRainbow(System.nanoTime(), (i + 1) * distanceValue.get(), saturationValue.get(), brightnessValue.get()).rgb
                    else -> continue@loop
                })
            }
        }

        RenderUtils.drawRect(0F, 0F, nameLength + hpLength + distLength + 50F, 4F + font.FONT_HEIGHT.toFloat(), bgColor.rgb)
        
        font.drawString("Name (${playerList.size})", 5F, 3F, -1, shadowValue.get())
        font.drawString("Distance", 5F + nameLength + 10F, 3F, -1, shadowValue.get())
        font.drawString("Health", 5F + nameLength + distLength + 20F, 3F, -1, shadowValue.get())

        playerList.forEachIndexed { index, player ->
            RenderUtils.drawRect(0F, height, nameLength + hpLength + distLength + 50F, height + 2F + font.FONT_HEIGHT.toFloat(), bgColor.rgb)

            font.drawString(player.name, 5F, height + 1F + fontOffset, -1, shadowValue.get())
            font.drawString("${decimalFormat3.format(mc.thePlayer.getDistanceToEntityBox(player))}m", 5F + nameLength + 10F, height + 1F + fontOffset, -1, shadowValue.get())
            font.drawString("${decimalFormat3.format(player.health)} HP", 5F + nameLength + distLength + 20F, height + 1F + fontOffset, -1, shadowValue.get())

            height += 2F + font.FONT_HEIGHT.toFloat()
        }

        return Border(0F, 0F, nameLength + hpLength + distLength + 50F, 4F + height + font.FONT_HEIGHT.toFloat())
    }
}
