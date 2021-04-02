/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.icon.IconManager
import net.ccbluex.liquidbounce.utils.render.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FontValue
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
        for(notify in LiquidBounce.hud.notifications){
            notify.drawNotification(LiquidBounce.hud.notifications.indexOf(notify))
        }

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
    private var stayTimer = MSTimer()
    private var firstY:Float=1919F
    private var animeTime=System.currentTimeMillis()

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
    fun drawNotification(index: Int) {
        // Animation
        val delta = RenderUtils.deltaTime
        val width = textLength + 8F

        var y=index*30F
        if(firstY==1919F){
            firstY=y
        }
        if(firstY>y){
            firstY-=(firstY-y)*((System.currentTimeMillis()-animeTime)/500F)
            y=firstY
        }else{
            firstY=y
            animeTime=System.currentTimeMillis()
        }

        // Draw notification
        RenderUtils.drawRect(-x + 10 + textLength, 0F-y, -x - 10, -20F-y, Color.BLACK.rgb)
        RenderUtils.drawFilledCircle((-x-10).toInt(), (-10-y).toInt(), 10F,Color.BLACK)
        RenderUtils.drawImage(IconManager.getIcon(type.icon),(-x-17).toInt(), (-17-y).toInt(),14,14)
        Fonts.font35.drawString(message, -x + 5, -13F-y, Int.MAX_VALUE)
        GlStateManager.resetColor()

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

            FadeState.STAY -> if (stay > 0) {
                stay = 0F
                stayTimer.reset()
            }else if(stayTimer.hasTimePassed(1500)){
                fadeState = FadeState.OUT
            }

            FadeState.OUT -> if (x > 0) {
                x = AnimationUtils.easeOut(fadeStep, width) * width
                fadeStep -= delta / 4F
            } else
                fadeState = FadeState.END

            FadeState.END -> LiquidBounce.hud.removeNotification(this)
        }
    }
}

enum class NotifyType(var renderColor: Color, var icon: String) {
    OKAY(Color(0, 255, 127), "check-circle"),
    WARN(Color(255, 75, 0), "close-circle"),
    INFO(Color(0, 160, 255),"information");
}

