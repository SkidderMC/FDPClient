/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.hud.element.elements

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.ui.font.cf.CFontRenderer
import net.ccbluex.liquidbounce.ui.font.cf.FontLoaders
import net.ccbluex.liquidbounce.ui.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.hud.element.*
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.*
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max


/**
 * CustomHUD Notification element
 */
@ElementInfo(name = "Notifications", blur = true)
class Notifications(x: Double = 0.0, y: Double = 0.0, scale: Float = 1F,side: Side = Side(Side.Horizontal.RIGHT, Side.Vertical.DOWN)) : Element(x, y, scale, side) {


    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val titleShadow = BoolValue("TitleShadow", false)
    private val motionBlur = BoolValue("Motionblur", false)
    private val contentShadow = BoolValue("ContentShadow", true)
    private val whiteText = BoolValue("WhiteTextColor", true)
    private val modeColored = BoolValue("CustomModeColored", true)
    companion object {
        val styleValue = ListValue("Mode", arrayOf("Classic", "FDP", "Modern", "LiquidBounce"), "FDP")
    }

    /**
     * Example notification for CustomHUD designer
     */
    private val exampleNotification = Notification("Notification", "This is an example notification.", NotifyType.INFO)

    /**
     * Draw element
     */
    override fun drawElement(partialTicks: Float): Border? {
        FDPClient.hud.notifications.map { it }.forEachIndexed { index, notify ->
            GL11.glPushMatrix()

            if (notify.drawNotification(index, FontLoaders.C16, backGroundAlphaValue.get(), blurValue.get(), this.renderX.toFloat(), this.renderY.toFloat(), scale,contentShadow.get(),titleShadow.get(),motionBlur.get(),whiteText.get(),modeColored.get(), Companion)) {
                FDPClient.hud.notifications.remove(notify)
            }

            GL11.glPopMatrix()
        }

        if (mc.currentScreen is GuiHudDesigner) {
            if (!FDPClient.hud.notifications.contains(exampleNotification)) {
                FDPClient.hud.addNotification(exampleNotification)
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
    private val animeTime: Int = 500
) {
    var width = 100
    val height = 30
    
    private val classicHeight = 30
    var x = 0F
    var textLengthtitle = 0
    var textLengthcontent = 0
    var textLength = 0f
    init {
        textLengthtitle = Fonts.font35.getStringWidth(title)
        textLengthcontent = Fonts.font35.getStringWidth(content)
        textLength = textLengthcontent.toFloat() + textLengthtitle.toFloat()
    }

    var fadeState = FadeState.IN
    private var nowY = -height
    var displayTime = System.currentTimeMillis()
    private var animeXTime = System.currentTimeMillis()
    private var animeYTime = System.currentTimeMillis()

    /**
     * Draw notification
     */
    fun drawNotification(
        index: Int, font: CFontRenderer, alpha: Int, blurRadius: Float, x: Float, y: Float, scale: Float,
        contentShadow: Boolean,
        titleShadow: Boolean,
        motionBlur: Boolean,
        whiteText: Boolean,
        modeColored: Boolean,
        parent: Notifications.Companion
        
    ): Boolean {
        this.width = 100.coerceAtLeast(
            (font.getStringWidth(content)
                .coerceAtLeast(font.getStringWidth(title)) + 15).toInt()
        )
        val realY = -(index + 1) * height
        val nowTime = System.currentTimeMillis()
        var transY = nowY.toDouble()
        var lbtl = font.getStringWidth("$title: $content")
        var x = 0f
        
        var textColor = Color(255, 255, 255).rgb
        
        if (whiteText) {
            textColor = Color(255, 255, 255).rgb
        } else {
            textColor = Color(10, 10, 10).rgb
        }

        // Y-Axis Animation
        if (nowY != realY) {
            var pct = (nowTime - animeYTime) / animeTime.toDouble()
            if (pct > 1) {
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
                if (pct > 1) {
                    fadeState = FadeState.STAY
                    animeXTime = nowTime
                    pct = 1.0
                }
                pct = EaseUtils.easeOutExpo(pct)
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
                pct = 1 - EaseUtils.easeInExpo(pct)
            }

            FadeState.END -> {
                return true
            }
        }
        val transX = width - (width * pct) - width
        GL11.glTranslated(transX, transY, 0.0)
        // draw notify
        val style = parent.styleValue.get()
        val nTypeWarning = type.renderColor == Color(0xF5FD00)
        val nTypeInfo = type.renderColor == Color(0x6490A7)
        val nTypeSuccess = type.renderColor == Color(0x60E092)
        val nTypeError = type.renderColor == Color(0xFF2F2F)


        if (style.equals("Modern")) {

            if (blurRadius != 0f) {
                BlurUtils.draw(4 + (x + transX).toFloat() * scale, (y + transY).toFloat() * scale, (width * scale), (27f - 5f) * scale, blurRadius)
            }
            var colorRed = type.renderColor.red
            var colorGreen = type.renderColor.green
            var colorBlue = type.renderColor.blue
            
            if (modeColored) {
                //success
                if (colorRed    == 60)   colorRed    = 36
                if (colorGreen  == 224)  colorGreen  = 211
                if (colorBlue   == 92)   colorBlue   = 99 

                //error
                if (colorRed    == 255) colorRed    = 248
                if (colorGreen  == 47)  colorGreen  = 72
                if (colorBlue   == 47)  colorBlue   = 72 

                //warning
                if (colorRed    == 245) colorRed    = 251
                if (colorGreen  == 253)  colorGreen  = 189
                if (colorBlue   == 0)  colorBlue   = 23

                //info
                if (colorRed    == 64) colorRed    = 242
                if (colorGreen  == 90)  colorGreen  = 242
                if (colorBlue   == 167)  colorBlue   = 242
            }
   
            
            val colors = Color(colorRed, colorGreen, colorBlue, alpha / 3)
            
            if (motionBlur) {
                when (fadeState) {
                    FadeState.IN -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.STAY -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.OUT -> {
                        RenderUtils.drawRoundedCornerRect(4F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(5F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.END -> TODO()
                }
            } else {
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            }
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            shadowRenderUtils.drawShadowWithCustomAlpha(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 240f)
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 5f, 0F), 27f - 5f, 2f, Color(0, 0, 0, 26).rgb)
            FontLoaders.C12.DisplayFont2(FontLoaders.C12, title, 4F, 3F, textColor, titleShadow)
            font.DisplayFont2(font, content, 4F, 10F, textColor, contentShadow)
            return false
        }
        
        if (style == "FDP") {

            if (blurRadius != 0f) {
                BlurUtils.draw(4 + (x + transX).toFloat() * scale, (y + transY).toFloat() * scale, (width * scale), (27f - 5f) * scale, blurRadius)
            }
            
            val colors = Color(0, 0, 0, alpha / 4)

            if (motionBlur) {
                when (fadeState) {
                    FadeState.IN -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.STAY -> {
                        RenderUtils.drawRoundedCornerRect(3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(3F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.OUT -> {
                        RenderUtils.drawRoundedCornerRect(4F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                        RenderUtils.drawRoundedCornerRect(5F, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                    }

                    FadeState.END -> TODO()
                }
            } else {
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
                RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            }
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 2f, colors.rgb)
            shadowRenderUtils.drawShadowWithCustomAlpha(0F + 3f, 0F, width.toFloat() + 5f, 27f - 5f, 240f)
            RenderUtils.drawRoundedCornerRect(0F + 3f, 0F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)) + 5f, 0F), 27f - 5f, 2f, Color(0, 0, 0, 40).rgb)
            FontLoaders.C12.DisplayFont2(FontLoaders.C12, title, 4F, 3F, textColor, titleShadow)
            font.DisplayFont2(font, content, 4F, 10F, textColor, contentShadow)
            return false
        }

//        // lbtl means liquidbounce text length
//        if(style.equals("LiquidBounce")) {
//            RenderUtils.drawRect(-1F, 0F, lbtl + 9F, -20F, Color(0, 0, 0, alpha))
//            FontLoaders.C12.DisplayFont2(FontLoaders.C12, title + ": " + content, -4F, 3F, textColor, titleShadow)
//            RenderUtils.drawRect(-1F + max(lbtl + 5F - (lbtl+ 5F) * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), 0F, 4F + max(lbtl + 5F - (lbtl+ 5F) * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), -20F, Color(0, 0, 0, alpha))
//            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
//        }


        /*  
        if(style.equals("Simple")) {
            RenderUtils.customRoundedinf(-x + 8F + lbtl, -y, -x - 2F, -18F - y, 0F, 3F, 3F, 0F, Color(0,0,0, alpha).rgb)
            RenderUtils.customRoundedinf(-x - 2F, -y, -x - 5F, -18F - y, 3F, 0F, 0F, 3F, type.renderColor)
            Fonts.font40.drawString("$title: $content", -x + 3, -13F - y, -1)
            }  */

        if(style.equals("Classic")) {
            if (blurRadius != 0f)
                BlurUtils.draw((x + transX).toFloat() * scale, (y + transY).toFloat() * scale, width * scale, classicHeight * scale, blurRadius) 
                
            RenderUtils.drawRect(0F, 0F, width.toFloat(), classicHeight.toFloat(), Color(0, 0, 0, alpha))
            shadowRenderUtils.drawShadowWithCustomAlpha(0F, 0F, width.toFloat(), classicHeight.toFloat(), 240f)
            RenderUtils.drawRect(0F, classicHeight - 2F, max(width - width * ((nowTime - displayTime) / (animeTime * 2F + time)), 0F), classicHeight.toFloat(), type.renderColor)
            font.drawString(title, 4F, 4F, textColor, false)
            font.drawString(content, 4F, 17F, textColor, false)
            return false
       }

    return false
    }
      
}

//NotifyType Color
enum class NotifyType(var renderColor: Color) {
    SUCCESS(Color(0x60E092)),
    ERROR(Color(0xFF2F2F)),
    WARNING(Color(0xF5FD00)),
    INFO(Color(0x6490A7));
}
    //classic
   // SUCCESS(Color((0x60E092)),
   // ERROR(Color(0xFF2F2F)),
   // WARNING(Color(0xF5FD00)),
   // INFO(Color( 0x6490A7)));
   //modern (shitty)
   //    SUCCESS(Color(0x36D399)),
   // ERROR(Color(0xF87272)),
   // WARNING(Color(0xFBBD23)),
   // INFO(Color(0xF2F2F2));


enum class FadeState { IN, STAY, OUT, END }


