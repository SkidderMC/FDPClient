package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Settings

import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntValue
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.Downward
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NeverloseGui.Companion.getInstance
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.NlModule
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.drawRoundedRect
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.RenderUtil.isHovering
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations.impl.DecelerateAnimation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawCircle
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.round.RoundedUtil.Companion.drawRound
import net.ccbluex.liquidbounce.ui.font.Fonts.Nl_15
import net.ccbluex.liquidbounce.ui.font.Fonts.Nl_16
import net.minecraft.client.Minecraft
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class Numbersetting(s: Value<*>, moduleRender: NlModule) : Downward<Value<*>>(s, moduleRender) {
    var percent: Float = 0f

    private var iloveyou = false
    private var isset = false

    private var finalvalue: String? = null


    var HoveringAnimation: Animation = DecelerateAnimation(225, 1.0, Direction.BACKWARDS)


    override fun draw(mouseX: Int, mouseY: Int) {
        val mainx = getInstance().x
        val mainy = getInstance().y


        val numbery = (y + getScrollY()).toInt()



        HoveringAnimation.direction = if (iloveyou || isHovering(
                getInstance().x + 170 + x,
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
        }

        val current = (setting.get() as Number).toDouble()
        val percentBar = (current - minimum) / (maximum - minimum)

        percent = max(0f, min(1f, (percent + (max(0.0, min(percentBar, 1.0)) - percent) * (0.2 / clamp)).toFloat()))

        Nl_16.drawString(
            setting.name,
            mainx + 100 + x,
            (mainy + numbery + 57).toFloat(),
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        drawRound(
            mainx + 170 + x,
            (mainy + numbery + 58).toFloat(),
            60f,
            2f,
            2f,
            if (getInstance().light) Color(230, 230, 230) else Color(5, 22, 41)
        )

        drawRound(mainx + 170 + x, (mainy + numbery + 58).toFloat(), 60 * percent, 2f, 2f, Color(12, 100, 138))

        drawCircle(
            mainx + 167 + x + (60 * percent),
            (mainy + numbery + 56).toFloat(),
            (5.5f + (0.5f * HoveringAnimation.getOutput())).toFloat(),
            NeverloseGui.neverlosecolor
        )

        if (iloveyou) {

            val percentt = min(1f, max(0f, ((mouseX.toFloat() - (mainx + 170 + x)) / 99.0f) * 1.55f))
            val newValue = ((percentt * (maximum - minimum)) + minimum)

            if (setting is IntValue) {
                (setting as IntValue).set(Math.round(newValue).toInt(), true)
            } else if (setting is FloatValue) {
                (setting as FloatValue).set(newValue.toFloat(), true)
            }
        }

        if (isset) {
            GL11.glTranslatef(0.0f, 0.0f, 2.0f)
        }


        val displayString = if (isset) "${finalvalue ?: ""}_" else "$current"
        val stringWidth = Nl_15.stringWidth(displayString) + 4

        drawRoundedRect(
            mainx + 235 + x,
            (mainy + numbery + 55).toFloat(),
            stringWidth.toFloat(),
            9f,
            1f,
            if (getInstance().light) Color(255, 255, 255).rgb else Color(0, 5, 19).rgb,
            1f,
            Color(13, 24, 35).rgb
        )

        Nl_15.drawString(
            displayString,
            mainx + 237 + x,
            (mainy + numbery + 58).toFloat(),
            if (getInstance().light) Color(95, 95, 95).rgb else -1
        )

        if (isset) {
            GL11.glTranslatef(0.0f, 0.0f, -2.0f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val current = (setting.get() as Number).toDouble()


        if (isHovering(
                getInstance().x + 170 + x,
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


        val displayString = if (isset) "${finalvalue ?: ""}_" else "$current"
        val stringWidth = Nl_15.stringWidth(displayString) + 4


        if (isHovering(
                getInstance().x + 235 + x,
                getInstance().y + (y + getScrollY()) + 55,
                stringWidth.toFloat(),
                9f,
                mouseX,
                mouseY
            )
        ) {
            if (mouseButton == 0) {
                finalvalue = current.toString()
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
}
