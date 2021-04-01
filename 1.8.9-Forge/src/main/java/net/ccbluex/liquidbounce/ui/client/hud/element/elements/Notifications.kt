/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.icon.IconManager
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 30.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Example Notification",NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(): Border? {
        if (LiquidBounce.hud.notifications.size > 0)
            LiquidBounce.hud.notifications[0].drawNotification()

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = Notification.FadeState.STAY
            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-95F, -20F, 0F, 0F)
        }

        return null
    }

}

class Notification(private val message: String,private val type: NotifyType) {
    var x = 0F
    var textLength = 0

    private var stay = 0F
    private var fadeStep = 0F
    var fadeState = FadeState.IN

    /**
     * Fade state for animation
     */
    enum class FadeState { IN, STAY, OUT, END }

    init {
        textLength = Fonts.font35.getStringWidth(message)
    }

    /**
     * Draw notification
     */
    fun drawNotification() {
        // Draw notification
        RenderUtils.drawRect(-x + 10 + textLength, 0F, -x - 10, -20F, Color.BLACK.rgb)
        RenderUtils.drawFilledCircle((-x-10).toInt(), -10, 10F,Color.BLACK)
        RenderUtils.drawImage(IconManager.getIcon(type.icon),(-x-17).toInt(), -17,14,14)
        Fonts.font35.drawString(message, -x + 5, -13F, Int.MAX_VALUE)
        GlStateManager.resetColor()

        // Animation
        val delta = RenderUtils.deltaTime
        val width = textLength + 8F

        when (fadeState) {
            FadeState.IN -> {
                if (x < width) {
                    x = AnimationUtils.easeOut(fadeStep, width) * width
                    fadeStep += delta / 4F
                }
                if (x >= width) {
                    fadeState = FadeState.STAY
                    x = width
                    fadeStep = width
                }

                stay = 60F
            }

            FadeState.STAY -> if (stay > 0)
                stay = 0F
            else
                fadeState = FadeState.OUT

            FadeState.OUT -> if (x > 0) {
                x = AnimationUtils.easeOut(fadeStep, width) * width
                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> LiquidBounce.hud.removeNotification(this)
        }
    }

}

