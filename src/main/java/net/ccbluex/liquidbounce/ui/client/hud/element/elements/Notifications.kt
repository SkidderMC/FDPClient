/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.Fonts.fontIconXD85
import net.ccbluex.liquidbounce.ui.font.Fonts.fontNovoAngularIcon85
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSFUI35
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSFUI40
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold35
import net.ccbluex.liquidbounce.utils.io.APIConnectorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.ccbluex.liquidbounce.utils.render.Stencil
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInBackNotify
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeOutBackNotify
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.render.shader.UIEffectRenderer.drawShadowWithCustomAlpha
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications")
class Notifications(
    x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)
) : Element("Notifications", x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", Type.INFO)

    private val styleValue by choices(
        "Mode", arrayOf("ZAVZ", "CLASSIC", "IDE"), "ZAVZ"
    )

    private val colorMode by choices(
        "Color-Mode", arrayOf("Custom", "Fade", "Theme"), "Custom"
    )

    private val customColor1 by color("Custom-Color-1", Color(0xFF0054).rgb) { styleValue == "ZAVZ" && colorMode == "Custom" }
    private val customColor2 by color("Custom-Color-2", Color(0x001300).rgb) { styleValue == "ZAVZ" && colorMode == "Custom" }

    private val fadeColor1 by color("Fade-Color-1", Color(0xFF0054).rgb) { styleValue == "ZAVZ" && colorMode == "Fade" }
    private val fadeColor2 by color("Fade-Color-2", Color(0x001300).rgb) { styleValue == "ZAVZ" && colorMode == "Fade" }
    private val fadeDistance by int("Fade-Distance", 50, 0..100) { styleValue == "ZAVZ" && colorMode == "Fade" }

    private val backgroundMode by boolean("Background-Color", true)
    private val backgroundColor by color("Background", Color(0, 0, 0, 120)) { backgroundMode }

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        val notificationsToRemove = mutableListOf<Notification>()
        for ((index, notification) in hud.notifications.withIndex()) {
            GL11.glPushMatrix()
            val color1: Int
            val color2: Int
            when (colorMode) {
                "Custom" -> {
                    color1 = customColor1.rgb
                    color2 = customColor2.rgb
                }
                "Fade" -> {
                    color1 = fade(fadeColor1, index * fadeDistance, 100).rgb
                    color2 = fade(fadeColor2, index * fadeDistance, 100).rgb
                }
                "Theme" -> {
                    val themeColor = getColor(index).rgb
                    color1 = themeColor
                    color2 = themeColor
                }
                else -> {
                    color1 = customColor1.rgb
                    color2 = customColor2.rgb
                }
            }
            if (notification.drawNotification(index, styleValue, color1, color2, backgroundMode, backgroundColor)) {
                notificationsToRemove.add(notification)
            }
            GL11.glPopMatrix()
        }
        notificationsToRemove.forEach { hud.notifications.remove(it) }
        if (mc.currentScreen is GuiHudDesigner) {
            if (!hud.notifications.contains(exampleNotification)) {
                hud.addNotification(exampleNotification)
            }
            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
            return when (styleValue) {
                "IDE" -> Border(-180F, -30F, 0F, 0F)
                "ZAVZ" -> Border(-185F, -30F, 0F, 0F)
                "CLASSIC" -> Border(0F, -30F, 135F, 0F)
                else -> Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(), 0F, 0F)
            }
        }
        return null
    }
}

class Notification(
    private val title: String,
    private val content: String,
    val type: Type,
    val time: Int = 1500,
    private val animeTime: Int = 500
) {
    val width = 100.coerceAtLeast(
        fontSemibold35.getStringWidth(this.title).coerceAtLeast(fontSemibold35.getStringWidth(this.content)) + 12
    )
    val height = 30
    private var firstYz = 0
    var x = 0f
    private var textLength = Fonts.minecraftFont.getStringWidth(content) + 10
    var fadeState = FadeState.IN
    private var nowY = -height
    var displayTime = System.currentTimeMillis()
    private var animeXTime = System.currentTimeMillis()
    private var animeYTime = System.currentTimeMillis()
    private var textColor = Color(255, 255, 255).rgb

    /**
     * Draw notification
     */
    fun drawNotification(index: Int, style: String, colorValue1: Int, colorValue2: Int, backgroundMode: Boolean, backgroundColor: Color): Boolean {
        resetColor()
        glColor4f(1f, 1f, 1f, 1f)
        val nowTime = System.currentTimeMillis()
        val realY = -(index + 1) * height
        var pct = (nowTime - animeXTime) / animeTime.toDouble()
        if (style == "CLASSIC") {
            if (nowY != realY) {
                pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)
            pct = (nowTime - animeXTime) / animeTime.toDouble()
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                }
                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }
                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                }
                FadeState.END -> {
                    return true
                }
            }
            drawRect(0F, 0F, width.toFloat(), height.toFloat(), if (backgroundMode) backgroundColor.rgb else Color(0, 0, 0, 150).rgb)
            drawShadowWithCustomAlpha(0F, 0F, width.toFloat(), height.toFloat(), 240f)
            drawRect(
                0F,
                height - 2F,
                max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F),
                height.toFloat(),
                type.renderColor
            )
            fontSemibold35.drawString(title, 4F, 4F, textColor, false)
            fontSemibold35.drawString(content, 4F, 17F, textColor, false)
        }
        if (style == "IDE") {
            if (nowY != realY) {
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                    }
                }
                FadeState.STAY -> {
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }
                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                    }
                }
                FadeState.END -> {
                    return true
                }
            }
            val y = firstYz
            val kek = -x - 1 - 20F
            resetColor()
            Stencil.write(true)
            when (type) {
                Type.ERROR -> {
                    drawRect(
                        -textLength - 23f + 5, -y.toFloat(), kek + 21f, height.toFloat(), Color(115, 69, 75).rgb
                    )
                    drawRect(
                        -textLength.toFloat() - 22f + 5,
                        -y.toFloat() + 1,
                        kek + 20,
                        height.toFloat() - 1,
                        Color(89, 61, 65).rgb
                    )
                    Fonts.minecraftFont.drawStringWithShadow(
                        "IDE Error:", -textLength.toFloat() - 1, -y.toFloat() + 2, Color(249, 130, 108).rgb
                    )
                }
                Type.INFO -> {
                    drawRect(
                        -textLength - 23f + 5,
                        -y.toFloat(),
                        textLength.toFloat() - 162,
                        height.toFloat(),
                        Color(70, 94, 115).rgb
                    )
                    drawRect(
                        -textLength.toFloat() - 22f + 5,
                        -y + 1f,
                        textLength.toFloat() - 163,
                        height.toFloat() - 1,
                        Color(61, 72, 87).rgb
                    )
                    Fonts.minecraftFont.drawStringWithShadow(
                        "IDE Information:", -textLength.toFloat() - 1, -y.toFloat() + 2, Color(119, 145, 147).rgb
                    )
                }
                Type.SUCCESS -> {
                    drawRect(
                        -textLength - 23f + 5, -y.toFloat(), kek + 21f, height.toFloat(), Color(67, 104, 67).rgb
                    )
                    drawRect(
                        -textLength.toFloat() - 22f + 5, -y + 1f, kek + 20, height.toFloat() - 1, Color(55, 78, 55).rgb
                    )
                    Fonts.minecraftFont.drawStringWithShadow(
                        "IDE Success:", -textLength.toFloat() - 1, -y.toFloat() + 2, Color(10, 142, 2).rgb
                    )
                }
                Type.WARNING -> {
                    drawRect(
                        -textLength - 23f + 5, -y.toFloat(), kek + 21f, height.toFloat(), Color(103, 103, 63).rgb
                    )
                    drawRect(
                        -textLength.toFloat() - 22f + 5, -y + 1f, kek + 20, height.toFloat() - 1, Color(80, 80, 57).rgb
                    )
                    Fonts.minecraftFont.drawStringWithShadow(
                        "IDE Warning:", -textLength.toFloat() - 1, -y.toFloat() + 2, Color(175, 163, 0).rgb
                    )
                }
            }
            Stencil.erase(true)
            resetColor()
            Stencil.dispose()
            GL11.glPushMatrix()
            GlStateManager.disableAlpha()
            resetColor()
            val pn = when (type.name) {
                "SUCCESS" -> APIConnectorUtils.callImage("checkmarkIDE", "notifications")
                "ERROR" -> APIConnectorUtils.callImage("errorIDE", "notifications")
                "WARNING" -> APIConnectorUtils.callImage("warningIDE", "notifications")
                "INFO" -> APIConnectorUtils.callImage("infoIDE", "notifications")
                else -> APIConnectorUtils.callImage("errorIDE", "notifications")
            }
            RenderUtils.drawImage(pn, -textLength - 11, -y + 2, 7, 7)
            GlStateManager.enableAlpha()
            GL11.glPopMatrix()
            Fonts.minecraftFont.drawStringWithShadow(content, -textLength.toFloat() - 1, -y.toFloat() + 15, -1)
        }
        if (style == "ZAVZ") {
            val width = 100.coerceAtLeast((fontSemibold35.getStringWidth(this.content)) + 70)
            if (nowY != realY) {
                var pct = (nowTime - animeYTime) / animeTime.toDouble()
                if (pct > 1) {
                    nowY = realY
                    pct = 1.0
                } else {
                    pct = easeOutBackNotify(pct)
                }
                GL11.glTranslated(0.0, (realY - nowY) * pct, 0.0)
            } else {
                animeYTime = nowTime
            }
            GL11.glTranslated(0.0, nowY.toDouble(), 0.0)
            var pct = (nowTime - animeXTime) / animeTime.toDouble()
            when (fadeState) {
                FadeState.IN -> {
                    if (pct > 1) {
                        fadeState = FadeState.STAY
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = easeOutBackNotify(pct)
                }
                FadeState.STAY -> {
                    pct = 1.0
                    if ((nowTime - animeXTime) > time) {
                        fadeState = FadeState.OUT
                        animeXTime = nowTime
                    }
                }
                FadeState.OUT -> {
                    if (pct > 1) {
                        fadeState = FadeState.END
                        animeXTime = nowTime
                        pct = 1.0
                    }
                    pct = 1 - easeInBackNotify(pct)
                }
                FadeState.END -> {
                    return true
                }
            }
            GL11.glScaled(pct, pct, pct)
            GL11.glTranslatef(-width.toFloat(), -height.toFloat() + 30, 0F)
            RenderUtils.drawShadow(1F, 0F, width.toFloat() - 1, height.toFloat())
            drawRect(1F, 0F, width.toFloat(), height.toFloat(), if (backgroundMode) backgroundColor.rgb else Color(0, 0, 0, 150).rgb)
            fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
                GlStateManager.enableBlend()
                GlStateManager.disableTexture2D()
                GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ZERO
                )
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glLineWidth(2f)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                var i = end.toFloat()
                while (i >= start) {
                    val c = RenderUtils.getGradientOffset(
                        Color(colorValue1),
                        Color(colorValue2, true),
                        (abs(System.currentTimeMillis() / 360.0 + (i * 34 / 360) * 56 / 100) / 10)
                    ).rgb
                    val f2 = (c shr 24 and 255).toFloat() / 255.0f
                    val f22 = (c shr 16 and 255).toFloat() / 255.0f
                    val f3 = (c shr 8 and 255).toFloat() / 255.0f
                    val f4 = (c and 255).toFloat() / 255.0f
                    GlStateManager.color(f22, f3, f4, f2)
                    GL11.glVertex2f(
                        (x + cos(i * Math.PI / 180) * (radius * 1.001f)).toFloat(),
                        (y + sin(i * Math.PI / 180) * (radius * 1.001f)).toFloat()
                    )
                    i -= 360f / 90.0f
                }
                GL11.glEnd()
                GL11.glDisable(GL11.GL_LINE_SMOOTH)
                GlStateManager.enableTexture2D()
                GlStateManager.disableBlend()
            }
            RenderUtils.drawFilledForCircle(16f, 15f, 12.85f, Color(255, 255, 255, 255))
            RenderUtils.drawGradientSideways(
                1.0,
                height.toFloat() + 0.0,
                width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 0.0,
                height.toFloat() + 2.0,
                colorValue1,
                colorValue2
            )
            drawCircle(16f, 15f, 13f, 0, 360)
            when (type) {
                Type.INFO -> {
                    fontIconXD85.drawString("B", 11F, 8F, 0)
                }
                Type.WARNING -> {
                    fontIconXD85.drawString("A", 14F, 8F, 0)
                }
                Type.ERROR -> {
                    fontNovoAngularIcon85.drawString("L", 9F, 10F, 0)
                }
                else -> {
                    fontNovoAngularIcon85.drawString("M", 8F, 10F, 0)
                }
            }
            fontSFUI40.drawString(title, 34F, 4F, -1)
            fontSFUI35.drawString(
                "$content  (" + BigDecimal(((time - time * ((nowTime - displayTime) / (animeTime * 2F + time))) / 1000).toDouble()).setScale(
                    1,
                    BigDecimal.ROUND_HALF_UP
                ).toString() + "s)", 34F, 17F, -1
            )
            resetColor()
            return false
        }
        return false
    }
}

enum class Type(var renderColor: Color) {
    SUCCESS(Color(0x60E066)), ERROR(Color(0xFF2F3A)), WARNING(Color(0xF5FD00)), INFO(Color(106, 106, 220));
}

enum class FadeState { IN, STAY, OUT, END }
