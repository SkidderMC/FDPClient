/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.client.gui.colortheme.ClientTheme
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.CPSCounterUtils
import net.ccbluex.liquidbounce.utils.extensions.ping
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.ShadowUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.math.sqrt

/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "Text", blur = true)
class Text(
    x: Double = 10.0,
    y: Double = 10.0,
    scale: Float = 1F,
    side: Side = Side.default()
) : Element(x, y, scale, side) {

    companion object {
        val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        val HOUR_FORMAT = SimpleDateFormat("HH:mm")

        val DECIMAL_FORMAT = DecimalFormat("#.##")
        val NO_DECIMAL_FORMAT = DecimalFormat("#")
    }
    val displayString = TextValue("DisplayText", "")
    private val shadowValue = BoolValue("Shadow", false)
    private val shadowStrength = FloatValue("Shadow-Strength", 1F, 0.01F, 8F).displayable { shadowValue.get() }
    private val shadow = BoolValue("TextShadow", false)
    private val fontValue = FontValue("Font", Fonts.font40)
    val rectValue = ListValue("Rect", arrayOf("Normal", "RNormal", "OneTap", "Skeet", "Rounded", "None"), "None")
    private val rectExpandValue = FloatValue("RectExpand", 0.3F, 0F, 1F)
    private val rectRoundValue = FloatValue("RectRoundingMultiplier", 1.5F, 0.1F, 4F)

    private var editMode = false
    private var editTicks = 0
    private var prevClick = 0L

    private var suggestion = mutableListOf<String>()
    private var autoComplete = ""
    private var displayText = display
    private var pointer = 0

    private val display: String
        get() {
            val textContent = if (displayString.get().isEmpty() && !editMode) {
                "Click To Add Text"
            } else {
                displayString.get()
            }

            return multiReplace(textContent)
        }

    private fun getReplacement(str: String): String? {
        if (mc.thePlayer != null) {
            when (str) {
                "x" -> return DECIMAL_FORMAT.format(mc.thePlayer.posX)
                "y" -> return DECIMAL_FORMAT.format(mc.thePlayer.posY)
                "z" -> return DECIMAL_FORMAT.format(mc.thePlayer.posZ)
                "xpos" -> return NO_DECIMAL_FORMAT.format(mc.thePlayer.posX)
                "ypos" -> return NO_DECIMAL_FORMAT.format(mc.thePlayer.posY)
                "zpos" -> return NO_DECIMAL_FORMAT.format(mc.thePlayer.posZ)
                "xdp" -> return mc.thePlayer.posX.toString()
                "ydp" -> return mc.thePlayer.posY.toString()
                "zdp" -> return mc.thePlayer.posZ.toString()
                "velocity" -> return DECIMAL_FORMAT.format(sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ))
                "ping" -> return "${mc.thePlayer.ping}"
                "speed" -> return DECIMAL_FORMAT.format(MovementUtils.getSpeed())
                "bps" -> return DECIMAL_FORMAT.format(MovementUtils.bps)
                "counter" -> return DECIMAL_FORMAT.format("" + mc.thePlayer.currentEquippedItem.stackSize)
                "health" -> return DECIMAL_FORMAT.format(mc.thePlayer.health)
                "yaw" -> return DECIMAL_FORMAT.format(mc.thePlayer.rotationYaw)
                "pitch" -> return DECIMAL_FORMAT.format(mc.thePlayer.rotationPitch)
                "attackDist" -> return if (FDPClient.combatManager.target != null) mc.thePlayer.getDistanceToEntity(FDPClient.combatManager.target).toString() + " Blocks" else "Hasn't attacked"
            }
        }

        return when (str) {
            "username" -> mc.getSession().username
            "clientName" -> FDPClient.CLIENT_NAME
            "clientVersion" -> FDPClient.CLIENT_VERSION
            "clientCreator" -> FDPClient.CLIENT_DEV
            "fps" -> Minecraft.getDebugFPS().toString()
            "date" -> DATE_FORMAT.format(System.currentTimeMillis())
            "time" -> HOUR_FORMAT.format(System.currentTimeMillis())
            "serverIp" -> ServerUtils.getRemoteIp()
            "cps", "lcps" -> return CPSCounterUtils.getCPS(
                CPSCounterUtils.MouseButton.LEFT).toString()
            "mcps" -> return CPSCounterUtils.getCPS(CPSCounterUtils.MouseButton.MIDDLE).toString()
            "rcps" -> return CPSCounterUtils.getCPS(CPSCounterUtils.MouseButton.RIGHT).toString()
            "currentconfig" -> FDPClient.configManager.nowConfig
            else -> null // Null = don't replace
        }
    }

    private fun multiReplace(str: String): String {
        var lastPercent = -1
        val result = StringBuilder()
        for (i in str.indices) {
            if (str[i] == '%') {
                if (lastPercent != -1) {
                    if (lastPercent + 1 != i) {
                        val replacement = getReplacement(str.substring(lastPercent + 1, i))

                        if (replacement != null) {
                            result.append(replacement)
                            lastPercent = -1
                            continue
                        }
                    }
                    result.append(str, lastPercent, i)
                }
                lastPercent = i
            } else if (lastPercent == -1) {
                result.append(str[i])
            }
        }

        if (lastPercent != -1) {
            result.append(str, lastPercent, str.length)
        }

        return result.toString()
    }
    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border {


        val fontRenderer = fontValue.get()
        val expand = fontRenderer.FONT_HEIGHT * rectExpandValue.get()
        when (rectValue.get().lowercase()) {
            "normal" -> {
                RenderUtils.drawRect(-expand, -expand, fontRenderer.getStringWidth(displayText) + expand, fontRenderer.FONT_HEIGHT + expand, ClientTheme.getColor(1))
            }

            "rounded" -> {
                RenderUtils.drawRoundedCornerRect(-expand, -expand, fontRenderer.getStringWidth(displayText) + expand, fontRenderer.FONT_HEIGHT + expand, 2 + (expand / 4) * rectRoundValue.get(), ClientTheme.getColor(1).rgb)
            }

            "rnormal" -> {
                RenderUtils.drawRect(-expand, -expand - 1, fontRenderer.getStringWidth(displayText) + expand, -expand, ColorUtils.rainbow())
                RenderUtils.drawRect(-expand, -expand, fontRenderer.getStringWidth(displayText) + expand, fontRenderer.FONT_HEIGHT + expand, ClientTheme.getColor(1))
            }
            "onetap" -> {
                RenderUtils.drawRect(-4.0f, -8.0f, (fontRenderer.getStringWidth(displayText) + 3).toFloat(), fontRenderer.FONT_HEIGHT.toFloat(), Color(43, 43, 43).rgb)
                RenderUtils.drawGradientSidewaysH(-3.0, -7.0, fontRenderer.getStringWidth(displayText) + 2.0, -3.0, Color(ClientTheme.getColor(1).rgb).darker().rgb, ClientTheme.getColor(1).rgb)
            }
            "skeet" -> {
                RenderUtils.drawRect(-11.0, -11.0, (fontRenderer.getStringWidth(displayText) + 10).toDouble(), fontRenderer.FONT_HEIGHT.toDouble() + 8.0, Color(0, 0, 0).rgb)
                RenderUtils.drawOutLineRect(-10.0, -10.0, (fontRenderer.getStringWidth(displayText) + 9).toDouble(), fontRenderer.FONT_HEIGHT.toDouble() + 7.0, 8.0, Color(59, 59, 59).rgb, Color(59, 59, 59).rgb)
                RenderUtils.drawOutLineRect(-9.0, -9.0, (fontRenderer.getStringWidth(displayText) + 8).toDouble(), fontRenderer.FONT_HEIGHT.toDouble() + 6.0, 4.0, Color(59, 59, 59).rgb, Color(40, 40, 40).rgb)
                RenderUtils.drawOutLineRect(-4.0, -4.0, (fontRenderer.getStringWidth(displayText) + 3).toDouble(), fontRenderer.FONT_HEIGHT.toDouble() + 1.0, 1.0, Color(18, 18, 18).rgb, Color(0, 0, 0).rgb)
            }
        }

        if (shadowValue.get()) {
            GL11.glTranslated(-renderX, -renderY, 0.0)
            GL11.glPushMatrix()
            ShadowUtils.shadow(shadowStrength.get(), {
                GL11.glPushMatrix()
                GL11.glTranslated(renderX, renderY, 0.0)
                fontRenderer.drawString(displayText, 0F*scale, 0F*scale, if (ClientTheme.textValue.get()) Color.WHITE.rgb else getColor(1).rgb, false)
                GL11.glPopMatrix()
            }, {})
            GL11.glPopMatrix()
            GL11.glTranslated(renderX, renderY, 0.0)
        }

        fontRenderer.drawString(displayText, 0F, 0F, if (ClientTheme.textValue.get()) Color.WHITE.rgb else getColor(1).rgb, shadow.get())


        if (editMode && mc.currentScreen is GuiHudDesigner && editTicks <= 40) {
            fontRenderer.drawString("_", fontRenderer.getStringWidth(displayText) + 2F,
                0F, Color.WHITE.rgb, shadow.get())
        }

        if (editMode && mc.currentScreen !is GuiHudDesigner) {
            editMode = false
            updateElement()
        }

        return Border(
            -2F,
            -2F,
            fontRenderer.getStringWidth(displayText) + 2F,
            fontRenderer.FONT_HEIGHT.toFloat()
        )
    }

    override fun updateElement() {
        editTicks += 5
        if (editTicks > 80) editTicks = 0

        displayText = if (editMode) displayString.get() else display


        var suggestStr = ""
        var foundPlaceHolder = false
        for (i in displayText.length - 1 downTo 0 step 1) {
            if (displayText[i].toString() == "%") {
                var placeHolderCounter = 1
                var z = i

                for (j in z downTo 0 step 1) {
                    if (displayText[j].toString() == "%") placeHolderCounter++
                }

                if (placeHolderCounter % 2 == 0) {
                    try {
                        suggestStr = displayText.substring(i, displayText.length).replace("%", "")
                        foundPlaceHolder = true
                    } catch (e: Exception) {
                        e.printStackTrace() // and then ignore
                    }
                }

                break
            }
        }
        autoComplete = ""

        if (!foundPlaceHolder)
            suggestion.clear()
        else suggestion = listOf(
            "x",
            "y",
            "z",
            "xInt",
            "yInt",
            "zInt",
            "xdp",
            "ydp",
            "zdp",
            "velocity",
            "ping",
            "health",
            "maxHealth",
            "healthInt",
            "maxHealthInt",
            "yaw",
            "pitch",
            "yawInt",
            "pitchInt",
            "bps",
            "inBound",
            "outBound",
            "hurtTime",
            "onGround",
            "userName",
            "clientName",
            "clientVersion",
            "clientCreator",
            "fps",
            "date",
            "time",
            "serverIp",
            "cps", "lcps",
            "mcps",
            "rcps",
            "portalVersion",
            "watchdogLastMin",
            "staffLastMin",
            "wdStatus",
            "sessionTime",
            "worldTime"
        ).filter { it.startsWith(suggestStr, true) && it.length > suggestStr.length }.sortedBy { it.length }.reversed()
            .toMutableList()

        pointer = pointer.coerceIn(0, (suggestion.size - 1).coerceAtLeast(0))

        // may require sth
        if (suggestion.size > 0) {
            autoComplete = suggestion[pointer].substring(
                (suggestStr.length).coerceIn(0, suggestion[pointer].length),
                suggestion[pointer].length
            )
            suggestion.replaceAll { s ->
                "§7$suggestStr§r${
                    s.substring(
                        (suggestStr.length).coerceIn(0, s.length),
                        s.length
                    )
                }"
            }
        }
    }

    override fun handleMouseClick(x: Double, y: Double, mouseButton: Int) {
        if (isInBorder(x, y) && mouseButton == 0) {
            if (System.currentTimeMillis() - prevClick <= 250L) {
                editMode = true
            }

            prevClick = System.currentTimeMillis()
        } else {
            editMode = false
        }
    }
    override fun handleKey(c: Char, keyCode: Int) {
        if (editMode && mc.currentScreen is GuiHudDesigner) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (displayString.get().isNotEmpty()) {
                    displayString.set(displayString.get().substring(0, displayString.get().length - 1))
                }

                updateElement()
                return
            }

            if (ChatAllowedCharacters.isAllowedCharacter(c) || c == '§') {
                displayString.set(displayString.get() + c)
            }

            updateElement()
        }
    }
    fun getColor(index : Int) : Color {
        return  ClientTheme.getColor(index)
    }
}
