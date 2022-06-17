/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.CFontRenderer
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.FontRenderer
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", blur = true)
class Notifications(
    x: Double = 0.0,
    y: Double = 0.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {

    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 235, 0, 255)

    private val TitleShadow = BoolValue("Title Shadow", false)
    private val MotionBlur = BoolValue("Motion blur", false)
    private val ContentShadow = BoolValue("Content Shadow", true)
 

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        // bypass java.util.ConcurrentModificationException
        LiquidBounce.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(index, FontLoaders.C16, backGroundAlphaValue.get(), blurValue.get(), this.renderX.toFloat(), this.renderY.toFloat(), scale,ContentShadow.get(),TitleShadow.get(),MotionBlur.get())) {
                LiquidBounce.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification)) {
                LiquidBounce.hud.addNotification(exampleNotification)
            }

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
        }

        return null
    }

    override fun drawBoarderBlur(blurRadius: Float) {}
}

class Notification(
    val title: String,
    val content: String,
    val type: NotifyType,
    val time: Int = 1500,
    val animeTime: Int = 500
) {
    var width = 100
    val height = 27

    var fadeState = FadeState.IN
    var nowY = -height
    var displayTime = System.currentTimeMillis()
    var animeXTime = System.currentTimeMillis()
    var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(index: Int, font: CFontRenderer, alpha: Int, blurRadius: Float, x: Float, y: Float, scale: Float,ContentShadow: Boolean,TitleShadow: Boolean,MotionBlur: Boolean): Boolean {
        this.width = 100.coerceAtLeast(font.getStringWidth(content)
            .coerceAtLeast(font.getStringWidth(title)) + 15)
        val realY = -(index+1) * height
        val nowTime = System.currentTimeMillis()
        var transY = nowY.toDouble()

        // Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct> 1) {
                nowY = realY
                pct = 1.0
            } else {
                pct = EaseUtils.easeOutExpo(pct)
            }
            transY += (realY - nowY) * pct
        } else {
            animeYTime = nowTime
        }

        // X-Axis Animation
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        when (fadeState) {
            FadeState.IN -> {
                if (pct> 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = EaseUtils.easeOutExpo(pct)
            }

            FadeState.STAY -> {
                pct = 1.0
                if ((nowTime - animeXTime)> time) {
                    fadeState = FadeState.OUT
                    animeXTime = nowTime
                }
            }

            FadeState.OUT -> {
                if (pct> 1) {
                    fadeState = FadeState.END
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = 1 - EaseUtils.easeInExpo(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        val transX = width - (width * pct) - width
        GL11.glTranslated(transX, transY, 0.0)
        if (blurRadius != 0f) {
            BlurUtils.draw(4 + (x + transX).toFloat() * scale, (y + transY).toFloat() * scale, (width * scale) , (height.toFloat()-5f) * scale, blurRadius)
        }

        // draw notify
        var colors=Color(type.renderColor.red,type.renderColor.green,type.renderColor.blue,alpha/3);
        if(MotionBlur) {
            when (fadeState) {
                FadeState.IN -> {
                    RenderUtils.drawRoundedCornerRect(
                        3f,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                    RenderUtils.drawRoundedCornerRect(
                        3F,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                }

                FadeState.STAY -> {
                    RenderUtils.drawRoundedCornerRect(
                        3f,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                    RenderUtils.drawRoundedCornerRect(
                        3F,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                }

                FadeState.OUT -> {
                    RenderUtils.drawRoundedCornerRect(
                        4F,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                    RenderUtils.drawRoundedCornerRect(
                        5F,
                        0F,
                        width.toFloat() + 5f,
                        height.toFloat() - 5f,
                        2f,
                        colors.rgb
                    )
                }
            }
        }else{
            RenderUtils.drawRoundedCornerRect(0F+3f, 0F, width.toFloat()+5f, height.toFloat()-5f,2f ,colors.rgb)
            RenderUtils.drawRoundedCornerRect(0F+3f, 0F, width.toFloat()+5f, height.toFloat()-5f,2f ,colors.rgb)
        }
        RenderUtils.drawRoundedCornerRect(0F+3f, 0F, width.toFloat()+5f, height.toFloat()-5f,2f ,colors.rgb)
        RenderUtils.drawRoundedCornerRect(0F+3f, 0F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time))+5f, 0F), height.toFloat()-5f,2f ,Color(0,0,0,26).rgb)
        FontLoaders.C12.DisplayFont2(FontLoaders.C12,title, 4F, 3F, Color(31,41,55).rgb,TitleShadow)
        font.DisplayFont2(font,content, 4F, 10F, Color(31,41,55).rgb,ContentShadow)
        return false
    }
}

//NotifyType Color
enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x36D399)),
    ERROR(Color(0xF87272)),
    WARNING(Color(0xFBBD23)),
    INFO(Color(0xF2F2F2));
}

enum class FadeState { IN, STAY, OUT, END }
