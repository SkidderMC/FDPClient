package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "Alerts", single = true)
class Alerts : Element(0.0, 50.0, 1.0F, Side(Side.Horizontal.MIDDLE,Side.Vertical.UP)){
    val alphaValue = IntegerValue("Alpha", 255, 0, 255)

    private val exampleAlert = Alert("Alert", "This is a example alert", NotifyType.INFO, 1500)

    override fun drawElement(partialTicks: Float): Border? {
        if (LiquidBounce.hud.alerts.size > 0)
            LiquidBounce.hud.alerts[0].draw(this)

        if (mc.currentScreen is GuiHudDesigner) {
            if (!LiquidBounce.hud.alerts.contains(exampleAlert))
                LiquidBounce.hud.addAlert(exampleAlert)

            exampleAlert.fadeState=FadeState.STAY
            exampleAlert.yOffset=0F

            return Border(-60F,0F,60F,50F)
        }

        return null
    }
}

class Alert(val title: String, message: String, val type: NotifyType, val stayTime: Int) {
    var yOffset=-1F
    var smallestY=-1F
    var fadeState = FadeState.IN

    private var animeTime=-1L
    private val linedMessage = ArrayList<String>()

    init {
        var msg=""
        val font=Fonts.font35

        for(char in message.toCharArray()){
            msg += char
            if(font.getStringWidth(msg)>110){
                linedMessage.add(msg)
                msg=""
            }
        }
        if(msg.isNotEmpty()) {
            linedMessage.add(msg)
        }
    }

    fun draw(alerts: Alerts){
        if(yOffset==-1F){
            yOffset= (-alerts.y).toFloat() - 50F
            smallestY=-yOffset
        }
        if(animeTime==-1L){
            animeTime=System.currentTimeMillis()
        }

        val alpha=alerts.alphaValue.get()

        RenderUtils.drawRect(-60F,yOffset,60F,yOffset+5F, ColorUtils.reAlpha(type.renderColor,alpha).rgb)
        RenderUtils.drawRect(-60F,yOffset+5,60F,yOffset+50F, ColorUtils.reAlpha(Color.BLACK,alpha).rgb)

        Fonts.font40.drawString(title,-55, (yOffset+10).toInt(),Color.WHITE.rgb)

        var y=0
        for(str in linedMessage) {
            Fonts.font35.drawString(str, -55, (yOffset + 23 + y).toInt(), Color.GRAY.rgb)
            y+=Fonts.font35.height+2
        }

        val nowTime=System.currentTimeMillis()
        val spaceTime=nowTime-animeTime
        when(fadeState){
            FadeState.IN -> {
                yOffset+=smallestY*(spaceTime/500F)

                if(yOffset>0){
                    yOffset=0F
                    fadeState=FadeState.STAY
                }

                animeTime=nowTime
            }

            FadeState.STAY -> {
                if(spaceTime>stayTime){
                    animeTime=nowTime
                    fadeState=FadeState.OUT
                }
            }

            FadeState.OUT -> {
                yOffset-=smallestY*(spaceTime/500F)

                if(yOffset<(-smallestY)){
                    yOffset=-smallestY
                    fadeState=FadeState.END
                }

                animeTime=nowTime
            }

            FadeState.END -> {
                if(spaceTime>100) {
                    LiquidBounce.hud.removeAlert(this)
                }
            }
        }
    }
}