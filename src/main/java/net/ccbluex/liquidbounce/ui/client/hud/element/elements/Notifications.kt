/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max

/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", single = true)
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,
                    side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        val notifications = mutableListOf<Notification>()
        //FUCK YOU java.util.ConcurrentModificationException
        for((index, notify) in LiquidBounce.hud.notifications.withIndex()){
            GL11.glPushMatrix()

            if(notify.drawNotification(index)){
                notifications.add(notify)
            }

            GL11.glPopMatrix()
        }
        for(notify in notifications){
            LiquidBounce.hud.notifications.remove(notify)
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.notifications.contains(exampleNotification))
                LiquidBounce.hud.addNotification(exampleNotification)

            exampleNotification.fadeState = FadeState.STAY
            exampleNotification.displayTime = System.currentTimeMillis()
//            exampleNotification.x = exampleNotification.textLength + 8F

            return Border(-exampleNotification.width.toFloat(), -exampleNotification.height.toFloat(),0F,0F)
        }

        return null
    }

}

class Notification(val title: String, val content: String, val type: NotifyType, val time: Int=1500, val animeTime: Int=500) {
    val width=100.coerceAtLeast(Fonts.font35.getStringWidth(this.title)
                    .coerceAtLeast(Fonts.font35.getStringWidth(this.content)) + 10)
    val height=30

    var fadeState = FadeState.IN
    var nowY=-height
    var displayTime=System.currentTimeMillis()
    var animeXTime=System.currentTimeMillis()
    var animeYTime=System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(index: Int):Boolean {
        val realY=-(index+1)*height
        val nowTime=System.currentTimeMillis()

        //Y-Axis Animation
        if(nowY!=realY){
            var pct=(nowTime-animeYTime)/animeTime.toDouble()
            if(pct>1){
                nowY=realY
                pct=1.0
            }else{
                pct=EaseUtils.easeOutExpo(pct)
            }
            GL11.glTranslated(0.0,(realY-nowY)*pct,0.0)
        }else{
            animeYTime=nowTime
        }
        GL11.glTranslated(0.0,nowY.toDouble(),0.0)

        //X-Axis Animation
        var pct=(nowTime-animeXTime)/animeTime.toDouble()
        when(fadeState){
            FadeState.IN -> {
                if(pct>1){
                    fadeState=FadeState.STAY
                    animeXTime=nowTime
                    pct=1.0
                }
                pct=EaseUtils.easeOutExpo(pct)
            }

            FadeState.STAY -> {
                pct=1.0
                if((nowTime-animeXTime)>time){
                    fadeState=FadeState.OUT
                    animeXTime=nowTime
                }
            }

            FadeState.OUT -> {
                if(pct>1){
                    fadeState=FadeState.END
                    animeXTime=nowTime
                    pct=1.0
                }
                pct=1-EaseUtils.easeInExpo(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        GL11.glTranslated(width-(width*pct),0.0,0.0)
        GL11.glTranslatef(-width.toFloat(),0F,0F)

        //draw notify
//        GL11.glPushMatrix()
//        GL11.glEnable(GL11.GL_SCISSOR_TEST)
//        GL11.glScissor(width-(width*pct).toFloat(),0F, width.toFloat(),height.toFloat())
        RenderUtils.drawRect(0F,0F,width.toFloat(),height.toFloat(),Color(0,0,0,144))
        RenderUtils.drawRect(0F,height-2F,
            max(width-width*((nowTime-displayTime)/(animeTime*2F+time)),0F),height.toFloat(),type.renderColor)
        Fonts.font35.drawString(title,4F,4F,Color.WHITE.rgb)
        Fonts.font35.drawString(content,4F,17F,Color.WHITE.rgb)

        return false
    }
}

enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E092)),
    ERROR(Color(0xFF2F2F)),
    WARNING(Color(0xF5FD00)),
    INFO(Color(0x6490A7));
}


enum class FadeState { IN, STAY, OUT, END }

