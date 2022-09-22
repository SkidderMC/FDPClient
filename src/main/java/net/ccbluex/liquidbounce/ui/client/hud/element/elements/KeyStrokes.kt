/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.BlurUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shadowRenderUtils
import net.ccbluex.liquidbounce.utils.CPSCounter
import net.ccbluex.liquidbounce.value.*
import net.minecraft.util.ResourceLocation
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.settings.KeyBinding
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

@ElementInfo(name = "KeyStrokes", blur = true)
class KeyStrokes : Element(5.0, 25.0, 1.5F, Side.default()) {
    private val keys = ArrayList<KeyStroke>()

    private val backGroundRedValue = IntegerValue("BackGroundRed", 0, 0, 255)
    private val backGroundGreenValue = IntegerValue("BackGroundGreen", 0, 0, 255)
    private val backGroundBlueValue = IntegerValue("BackGroundBlue", 0, 0, 255)
    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val textRedValue = IntegerValue("TextRed", 255, 0, 255)
    private val textGreenValue = IntegerValue("TextGreen", 255, 0, 255)
    private val textBlueValue = IntegerValue("TextBlue", 255, 0, 255)
    private val textAlphaValue = IntegerValue("TextAlpha", 255, 0, 255)
    private val highLightPercent = FloatValue("HighLightPercent", 0.5F, 0F, 1F)
    private val animSpeedValue = IntegerValue("AnimationSpeed", 300, 0, 700)
    private val outline = BoolValue("Outline", false)
    private val outlineBoldValue = IntegerValue("OutlineBold", 1, 0, 5)
    private val outlineRainbow = BoolValue("OutLineRainbow", false)
    private val fontValue = FontValue("Font", Fonts.font35)
    companion object {
        val keyStyleValue = ListValue("Mode", arrayOf("Custom", "Jello", "Juul"), "Jello")
    }


    init {
        keys.add(KeyStroke(mc.gameSettings.keyBindForward, 16, 0, 15, 15).initKeyName())
        keys.add(KeyStroke(mc.gameSettings.keyBindLeft, 0, 16, 15, 15).initKeyName())
        keys.add(KeyStroke(mc.gameSettings.keyBindBack, 16, 16, 15, 15).initKeyName())
        keys.add(KeyStroke(mc.gameSettings.keyBindRight, 32, 16, 15, 15).initKeyName())
        if(keyStyleValue.get().equals("Custom")) {
            keys.add(KeyStroke(mc.gameSettings.keyBindAttack, 0, 32, 23, 15).initKeyName("L"))
            keys.add(KeyStroke(mc.gameSettings.keyBindUseItem, 24, 32, 23, 15).initKeyName("R"))
        }
        if(keyStyleValue.get().equals("Jello")) {
            keys.add(KeyStroke(mc.gameSettings.keyBindAttack, 0, 32, 23, 15).initKeyName("L"))
            keys.add(KeyStroke(mc.gameSettings.keyBindUseItem, 24, 32, 23, 15).initKeyName("R"))
        }
        if(keyStyleValue.get().equals("Juul")) {
            keys.add(KeyStroke(mc.gameSettings.keyBindAttack, 0, 0, 0, 0).initKeyName("L"))
            keys.add(KeyStroke(mc.gameSettings.keyBindUseItem, 0, 0, 0, 0).initKeyName("R"))
        }
    } 

    override fun drawElement(partialTicks: Float): Border {
        val backGroundColor = Color(backGroundRedValue.get(), backGroundGreenValue.get(), backGroundBlueValue.get(), backGroundAlphaValue.get())
        val textColor = if (outlineRainbow.get()) {
            ColorUtils.rainbowWithAlpha(textAlphaValue.get())
        } else {
            Color(textRedValue.get(), textGreenValue.get(), textBlueValue.get(), textAlphaValue.get())
        }

        for (keyStroke in keys) {
            keyStroke.render(animSpeedValue.get(), backGroundColor, textColor, highLightPercent.get(), outline.get(), outlineBoldValue.get(), fontValue.get(), blurValue.get(), this.renderX.toFloat(), this.renderY.toFloat(), scale, Companion)
        }
        if(keyStyleValue.get().equals("Jello")) {
            RenderUtils.drawImage2(ResourceLocation("fdpclient/misc/keystrokes.png"), -3.5f, -3.5f, 54, 54)
        }
        if(keyStyleValue.get().equals("Juul")) {
            val fontRenderer = fontValue.get()
            RenderUtils.drawRoundedCornerRect(0f, 32f, 23f, 47f, 4f, if (mc.gameSettings.keyBindAttack.isKeyDown) { Color(65, 65, 75, 255).rgb } else { Color(95, 95, 105, 255).rgb } )
            RenderUtils.drawRoundedCornerRect(24f, 32f, 47f, 47f, 4f, if (mc.gameSettings.keyBindUseItem.isKeyDown) { Color(65, 65, 75, 255).rgb } else { Color(95, 95, 105, 255).rgb } )
            val juulLeft = if (CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toFloat() != 0f) { CPSCounter.getCPS(CPSCounter.MouseButton.LEFT).toString() + " cps" } else { "Left" }
            val juulRight = if (CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toFloat() != 0f) { CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT).toString() + "CPS" } else { "Right" }
            Fonts.font28.drawString(juulLeft, 15.5f - (fontRenderer.getStringWidth(juulLeft) / 2f) + 1f, 39.5f - (fontRenderer.FONT_HEIGHT / 2f) + 2f, textColor.rgb)
            Fonts.font28.drawString(juulRight, 39.5f - (fontRenderer.getStringWidth(juulRight).toFloat() / 2f) + 1f, 39.5f - (fontRenderer.FONT_HEIGHT.toFloat() / 2f) + 2f, textColor.rgb)
        }

        return Border(0F, 0F, 47F, 47F)
    }

    override fun drawBoarderBlur(blurRadius: Float) {}
}

class KeyStroke(val key: KeyBinding, val posX: Int, val posY: Int, val width: Int, val height: Int) {
    var keyName = "KEY"

    private var lastClick = false
    private val animations = ArrayList<Long>()

    fun render(
        speed: Int,
        bgColor: Color,
        textColor: Color,
        highLightPct: Float,
        outline: Boolean,
        outlineBold: Int,
        font: FontRenderer,
        blurRadius: Float,
        renderX: Float,
        renderY: Float,
        scale: Float,
        parent: KeyStrokes.Companion
    ) {


    val style = parent.keyStyleValue.get()
    if(style.equals("Custom")) {
        GL11.glPushMatrix()
        GL11.glTranslatef(posX.toFloat(), posY.toFloat(), 0F)

        if (blurRadius != 0f) {
            BlurUtils.draw((renderX + posX) * scale, (renderY + posY) * scale, width * scale, height * scale, blurRadius)
        }

       shadowRenderUtils.drawShadowWithCustomAlpha(0F, 0F, width.toFloat(), height.toFloat(), 240f)

        val highLightColor = Color(255 - ((255 - bgColor.red) * highLightPct).toInt(), 255 - ((255 - bgColor.blue) * highLightPct).toInt(), 255 - ((255 - bgColor.green) * highLightPct).toInt())
        val clickAlpha = 255 - (255 - bgColor.alpha) * highLightPct
        val centerX = width / 2
        val centerY = height / 2
        val nowTime = System.currentTimeMillis()

        val rectColor = if (lastClick && animations.isEmpty()) { ColorUtils.reAlpha(highLightColor, clickAlpha.toInt()) } else { bgColor }
        RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), rectColor)

        val removeAble = ArrayList<Long>()
        for (time in animations) {
            val pct = (nowTime - time) / (speed.toFloat())
            if (pct> 1) {
                removeAble.add(time)
                continue
            }
            RenderUtils.drawLimitedCircle(0F, 0F, width.toFloat(), height.toFloat(), centerX, centerY, (width * 0.7F) * pct, Color(255 - ((255 - highLightColor.red) * pct).toInt(), 255 - ((255 - highLightColor.green) * pct).toInt(), 255 - ((255 - highLightColor.blue) * pct).toInt(), 255 - ((255 - clickAlpha) * pct).toInt()))
        }
        for (time in removeAble) {
            animations.remove(time)
            removeAble.remove(time)
        }
        if (!lastClick && key.isKeyDown) {
            animations.add(nowTime)
        }
        
        if (key.isKeyDown && animations.isEmpty())
            RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), ColorUtils.reAlpha(highLightColor, clickAlpha.toInt()))
        
        lastClick = key.isKeyDown
        

        font.drawString(keyName, centerX - (font.getStringWidth(keyName) / 2) + 1, centerY - (font.FONT_HEIGHT / 2) + 1, textColor.rgb)
        if (outline) {
            RenderUtils.drawRect(0F, 0F, outlineBold.toFloat(), height.toFloat(), textColor.rgb)
            RenderUtils.drawRect((width - outlineBold).toFloat(), 0F, width.toFloat(), height.toFloat(), textColor.rgb)
            RenderUtils.drawRect((outlineBold).toFloat(), 0F, (width - outlineBold).toFloat(), outlineBold.toFloat(), textColor.rgb)
            RenderUtils.drawRect((outlineBold).toFloat(), (height - outlineBold).toFloat(), (width - outlineBold).toFloat(), height.toFloat(), textColor.rgb)
        }

        GL11.glPopMatrix()
    }

    if(style.equals("Jello")) {
        GL11.glPushMatrix()
        GL11.glTranslatef(posX.toFloat(), posY.toFloat(), 0F)

        BlurUtils.draw((renderX + posX) * scale, (renderY + posY) * scale, width * scale, height * scale, 10f)
        
        val highLightColor = Color(255 - ((255 - bgColor.red) * highLightPct).toInt(), 255 - ((255 - bgColor.blue) * highLightPct).toInt(), 255 - ((255 - bgColor.green) * highLightPct).toInt())
        val clickAlpha = 255 - (255 - bgColor.alpha) * highLightPct
        val centerX = width / 2
        val centerY = height / 2
        val nowTime = System.currentTimeMillis()

        val rectColor = if (lastClick && animations.isEmpty()) { ColorUtils.reAlpha(highLightColor, clickAlpha.toInt()) } else { Color(0f,0f,0f,0f) }
        RenderUtils.drawRect(0F, 0F, width.toFloat(), height.toFloat(), rectColor)

        val removeAble = ArrayList<Long>()
        for (time in animations) {
            val pct = (nowTime - time) / (speed.toFloat())
            if (pct> 1) {
                removeAble.add(time)
                continue
            }
            RenderUtils.drawLimitedCircle(0F, 0F, width.toFloat(), height.toFloat(), centerX, centerY, (width * 0.7F) * pct, Color(255 - ((255 - highLightColor.red) * pct).toInt(), 255 - ((255 - highLightColor.green) * pct).toInt(), 255 - ((255 - highLightColor.blue) * pct).toInt(), 255 - ((255 - clickAlpha) * pct).toInt()))
        }
        for (time in removeAble) {
            animations.remove(time)
        }
        if (!lastClick && key.isKeyDown) {
            animations.add(nowTime)
        }
        lastClick = key.isKeyDown


        GL11.glPopMatrix()
    } 

    if(style.equals("Juul")) {
        GL11.glPushMatrix()
        GL11.glTranslatef(posX.toFloat(), posY.toFloat(), 0F)

        val nowTime = System.currentTimeMillis()

         val rectColor = if (lastClick) { Color(65, 65, 65, 255) } else { Color(95, 95, 95, 255) }
        RenderUtils.drawRoundedCornerRect(0F, 0F, width.toFloat(), height.toFloat(), 3f, rectColor.rgb)
        lastClick = key.isKeyDown

        font.drawString(keyName, width / 2 - (font.getStringWidth(keyName) / 2) + 1, height / 2 - (font.FONT_HEIGHT / 2) + 2, textColor.rgb)

        GL11.glPopMatrix()
    }


    }

    fun initKeyName(): KeyStroke {
        keyName = Keyboard.getKeyName(key.keyCode)
        return this
    }

    fun initKeyName(name: String): KeyStroke {
        keyName = name
        return this
    }
}
