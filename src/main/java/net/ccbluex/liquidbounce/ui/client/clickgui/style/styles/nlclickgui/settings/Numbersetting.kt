/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.settings

import net.ccbluex.liquidbounce.config.BlockValue
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.Companion.getInstance
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Numbersetting(s: Value<*>, moduleRender: NlModule) : Downward<Value<*>>(s, moduleRender) {
    var percent: Float = 0f

    private var iloveyou = false
    private var isset = false

    private var finalvalue: String? = null

    var HoveringAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)

    private val decimalFormat = DecimalFormat("#.##").also {
        it.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = getInstance().x
        val mainy = getInstance().y

        val numbery = (y + getScrollY()).toInt()

        HoveringAnimation.direction = if (iloveyou || RenderUtil.isHovering(
                (getInstance().x + 150 + x),
                (getInstance().y + (y + getScrollY()).toInt() + 58).toFloat(),
                60f,
                2f,
                mouseX,
                mouseY
            )
        ) Direction.FORWARDS else Direction.BACKWARDS

        val clamp = MathHelper.clamp_double(Minecraft.getDebugFPS() / 30.0, 1.0, 9999.0)

        var minimum = 0.0
        var maximum = 1.0

        if (setting is IntValue) {
            minimum = (setting as IntValue).minimum.toDouble()
            maximum = (setting as IntValue).maximum.toDouble()
        } else if (setting is FloatValue) {
            minimum = (setting as FloatValue).minimum.toDouble()
            maximum = (setting as FloatValue).maximum.toDouble()
        } else if (setting is BlockValue) {
            minimum = (setting as BlockValue).minimum.toDouble()
            maximum = (setting as BlockValue).maximum.toDouble()
        }

        val current = (setting.get() as Number).toDouble()
        val percentBar = (current - minimum) / (maximum - minimum)

        percent = max(0f, min(1f, (percent + (max(0.0, min(percentBar, 1.0)) - percent) * (0.2 / clamp)).toFloat()))

        val (label, labelTruncated) = abbreviate(setting.name)
        val labelX = (mainx + 100 + x).toFloat()
        val labelY = (mainy + numbery + 57).toFloat()
        val sliderX = (mainx + 150 + x).toFloat()
        val valueBoxX = (mainx + 215 + x).toFloat()

        Fonts.Nl.Nl_16.Nl_16.drawString(
            label,
            labelX,
            labelY,
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        if (labelTruncated && RenderUtil.isHovering(labelX, labelY - 3f, Fonts.Nl.Nl_16.Nl_16.stringWidth(label).toFloat(), 12f, mouseX, mouseY)) {
            drawTooltip(setting.name, mouseX, mouseY)
        }

        RoundedUtil.drawRound(
            sliderX,
            (mainy + numbery + 58).toFloat(),
            60f,
            2f,
            2f,
            if (getInstance().light) Color(230, 230, 230) else Color(5, 22, 41)
        )

        RoundedUtil.drawRound(sliderX, (mainy + numbery + 58).toFloat(), 60 * percent, 2f, 2f, Color(12, 100, 138))

        RoundedUtil.drawCircle(
            mainx + 147 + x + (60 * percent),
            (mainy + numbery + 56).toFloat(),
            (5.5f + (0.5f * HoveringAnimation.getOutput())).toFloat(),
            NeverloseGui.neverlosecolor
        )

        if (iloveyou) {
            val percentt = min(1f, max(0f, ((mouseX.toFloat() - sliderX) / 99.0f) * 1.55f))
            val newValue = ((percentt * (maximum - minimum)) + minimum)

            if (setting is IntValue) {
                (setting as IntValue).set(newValue.roundToInt(), true)
            } else if (setting is FloatValue) {
                (setting as FloatValue).set(newValue.toFloat(), true)
            } else if (setting is BlockValue) {
                (setting as BlockValue).set(newValue.roundToInt(), true)
            }
        }

        if (isset) {
            GL11.glTranslatef(0.0f, 0.0f, 2.0f)
        }

        val displayString = if (isset) "${finalvalue ?: ""}_" else formatNumber(current)

        val stringWidth = Fonts.Nl_15.stringWidth(displayString) + 4

        RenderUtil.drawRoundedRect(
            valueBoxX,
            (mainy + numbery + 55).toFloat(),
            stringWidth.toFloat(),
            9f,
            1f,
            if (getInstance().light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Fonts.Nl_15.drawString(
            displayString,
            valueBoxX + 2f,
            (mainy + numbery + 58).toFloat(),
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        if (isset) {
            GL11.glTranslatef(0.0f, 0.0f, -2.0f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val current = (setting.get() as Number).toDouble()
        val sliderX = (getInstance().x + 150 + x).toFloat()
        val valueBoxX = (getInstance().x + 215 + x).toFloat()

        if (RenderUtil.isHovering(
                sliderX,
                (getInstance().y + (y + getScrollY()).toInt() + 58).toFloat(),
                60f,
                2f,
                mouseX,
                mouseY
            ) && !isset
        ) {
            if (mouseButton == 0) {
                iloveyou = true
            }
        }

        val displayString = if (isset) "${finalvalue ?: ""}_" else formatNumber(current)
        val stringWidth = Fonts.Nl_15.stringWidth(displayString) + 4

        if (RenderUtil.isHovering(
                valueBoxX,
                (getInstance().y + (y + getScrollY()) + 55),
                stringWidth.toFloat(),
                9f,
                mouseX,
                mouseY
            )
        ) {
            if (mouseButton == 0) {
                finalvalue = formatNumber(current)
                isset = true
            }
        } else {
            if (mouseButton == 0) {
                isset = false
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (state == 0) iloveyou = false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (isset) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                isset = false
            } else if (keynumbers(keyCode)) {
                if (!(keyCode == Keyboard.KEY_PERIOD && (finalvalue ?: "").contains("."))) {
                    finalvalue = "${finalvalue ?: ""}$typedChar"
                }
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_BACK) && !finalvalue.isNullOrEmpty()) {
                finalvalue = finalvalue!!.substring(0, finalvalue!!.length - 1)
            }

            if (keyCode == Keyboard.KEY_RETURN) {
                try {
                    val safeValue = finalvalue ?: "0"
                    if (setting is FloatValue) {
                        val floatSetting = setting as FloatValue
                        val `val` = safeValue.toFloat()
                        val max = floatSetting.maximum
                        val min = floatSetting.minimum
                        floatSetting.set(min(max(`val`, min), max), true)
                    } else if (setting is IntValue) {
                        val intSetting = setting as IntValue
                        val `val` = safeValue.toInt()
                        val max = intSetting.maximum
                        val min = intSetting.minimum
                        intSetting.set(min(max(`val`, min), max), true)
                    } else if (setting is BlockValue) {
                        val blockSetting = setting as BlockValue
                        val `val` = safeValue.toInt()
                        val max = blockSetting.maximum
                        val min = blockSetting.minimum
                        blockSetting.set(min(max(`val`, min), max), true)
                    }
                } catch (e: NumberFormatException) {
                }

                isset = false
            }
        }

        super.keyTyped(typedChar, keyCode)
    }

    fun keynumbers(keyCode: Int): Boolean {
        return (keyCode == Keyboard.KEY_0 || keyCode == Keyboard.KEY_1 || keyCode == Keyboard.KEY_2 || keyCode == Keyboard.KEY_3 || keyCode == Keyboard.KEY_4 || keyCode == Keyboard.KEY_6 || keyCode == Keyboard.KEY_5 || keyCode == Keyboard.KEY_7 || keyCode == Keyboard.KEY_8 || keyCode == Keyboard.KEY_9 || keyCode == Keyboard.KEY_PERIOD || keyCode == Keyboard.KEY_MINUS)
    }


    private fun formatNumber(value: Double): String {
        return if (setting is IntValue || setting is BlockValue) {
            value.toInt().toString()
        } else {
            decimalFormat.format(value)
        }
    }

    private fun abbreviate(value: String): Pair<String, Boolean> {
        return if (value.length > 10) {
            value.substring(0, 10) + "..." to true
        } else {
            value to false
        }
    }

    private fun drawTooltip(text: String, mouseX: Int, mouseY: Int) {
        val width = Fonts.Nl_15.stringWidth(text) + 6
        val height = Fonts.Nl_15.height + 4
        val renderX = (mouseX + 6).toFloat()
        val renderY = (mouseY - height - 2).toFloat()

        RenderUtil.drawRoundedRect(renderX, renderY, width.toFloat(), height.toFloat(), 2f, Color(0, 5, 19).rgb, 1f, Color(13, 24, 35).rgb)
        Fonts.Nl_15.drawString(text, renderX + 3f, renderY + 2f, Color.WHITE.rgb)
    }
}