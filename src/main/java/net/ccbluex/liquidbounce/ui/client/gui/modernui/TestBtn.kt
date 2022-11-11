package net.ccbluex.liquidbounce.ui.client.gui.modernui

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.font.FontLoaders
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class TestBtn : GuiButton {
    private var r: Int
    private var g: Int
    private var b: Int
    private var alpha: Int
    private var image: ResourceLocation?
    private var r1: Int
    private var g1: Int
    private var b1: Int
    private var alpha1: Int
    var timer = MSTimer()
    var timer1 = MSTimer()
    private var reachTime: Int

    private var animation = 0.0
    private var lastUpdate = System.currentTimeMillis()
    constructor(
        buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String?,
        image: ResourceLocation?, reachTime: Int, color: Color
    ) : super(buttonId, x, y, 10, 12, buttonText) {
        val color2 = Color(120, 120, 200, 255)
        width = widthIn
        height = heightIn
        r = color.red
        g = color.green
        b = color.blue
        alpha = color.alpha
        this.image = image
        r1 = color2.red
        g1 = color2.green
        b1 = color2.blue
        alpha1 = color2.alpha
        this.reachTime = reachTime
    }

    constructor(
        buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String?,
        image: ResourceLocation?, reachTime: Int, color: Color, color2: Color
    ) : super(buttonId, x, y, 10, 12, buttonText) {
        width = widthIn
        height = heightIn
        r = color.red
        g = color.green
        b = color.blue
        alpha = color.alpha
        this.image = image
        this.reachTime = reachTime
        r1 = color2.red
        g1 = color2.green
        b1 = color2.blue
        alpha1 = color2.alpha
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        var wi = 0
        hovered =
            mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.blendFunc(770, 771)
        var yOffset=0;
        var offsetLength=2;

        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / 500.0
        val percent = EaseUtils.easeInOutQuad(animation)

        if (this.isMouseOver) {
            if (animation < 1) {
                animation += pct
            }
            if (animation > 1) {
                animation = 1.0
            }
            timer1.reset()
            for (i in 0..offsetLength) {
                if (timer.hasTimePassed((i*10 * reachTime).toLong())) {
                    wi++
                }
            }
        } else if (!this.isMouseOver) {
            if (animation > 0) {
                animation -= pct
            }
            if (animation < 0) {
                animation = 0.0
            }
            timer.reset()
            wi = offsetLength
            for (i in 0 until offsetLength) {
                if (timer1.hasTimePassed((i*10 * reachTime).toLong())) {
                    wi--
                }
            }
        }
        yOffset=-1*wi
        RenderUtils.drawRoundedCornerRect(
            xPosition.toFloat()-0.7f, yPosition.toFloat()-0.7f + yOffset, (xPosition + width).toFloat()+0.7f, (
                    yPosition + height).toFloat()+0.7f + yOffset,
            if (image != null) width.toFloat()/2 else 2f, if(LiquidBounce.Darkmode) Color(255,255,255, 30).rgb else Color(0,0,0, 70).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            xPosition.toFloat()-0.5f, yPosition.toFloat()-0.5f + yOffset, (xPosition + width).toFloat()+0.5f, (
                    yPosition + height).toFloat()+0.5f + yOffset, if (image != null) width.toFloat()/2 else 3f, if(LiquidBounce.Darkmode) Color(255,255,255, 110+(wi*10)).rgb else  Color(0,0,0, 120+(wi*10)).rgb
        )
        RenderUtils.drawRoundedCornerRect(
            xPosition.toFloat(), yPosition.toFloat() + yOffset, (xPosition + width).toFloat(), (
                    yPosition + height).toFloat() + yOffset, if (image != null) width.toFloat()/2 else 3f,if(LiquidBounce.Darkmode) Color(255,255,255, 80).rgb else Color(0,0,0, 50).rgb
        )
        GL11.glColor3f(2.55f, 2.55f, 2.55f)
        mouseDragged(mc, mouseX, mouseY)
        GL11.glPushMatrix()
        GL11.glPushAttrib(1048575)
        GL11.glScaled(1.0, 1.0, 1.0)

        lastUpdate = time
        if (image != null) RenderUtils.drawImage(
            image, xPosition - 1 + height / 3, (yPosition - 1 + height / 3) + yOffset,
            height / 2, height / 2
        )
        if (image != null) {
            if(this.isMouseOver) {
                var font = FontLoaders.C12
                RenderUtils.drawRoundedCornerRect(
                    xPosition + width / 2 - (FontLoaders.C16.DisplayFontWidths(displayString,FontLoaders.C16)  / 2).toFloat() - 3,
                    yPosition.toFloat() - 0.5f + yOffset + height + 5,
                    3 + xPosition + width / 2 + (FontLoaders.C16.DisplayFontWidths(displayString,FontLoaders.C16)  / 2).toFloat(),
                    (
                            yPosition + font.height).toFloat() + 0.5f  + height + 5 + 8,
                    3f,
                    if(LiquidBounce.Darkmode) Color(255, 255, 255, (150*percent).toInt()).rgb else Color(0, 0, 0, (150*percent).toInt()).rgb
                )
                FontLoaders.C16.DisplayFonts(
                    displayString,
                    xPosition + width / 2 - (FontLoaders.C16.DisplayFontWidths(displayString,FontLoaders.C16) / 2).toFloat(),
                    (
                            yPosition + font.height / 2 - 3).toFloat() + height + 5 + 3,
                    if(LiquidBounce.Darkmode) Color(50, 50, 50, (254*percent).toInt()).rgb else Color(180, 180, 180, (254*percent).toInt()).rgb,FontLoaders.C16
                )
            }
        }else{
            val font = FontLoaders.C16;
            val s = font.DisplayFonts(
                displayString,
                xPosition + width / 2 - (font.DisplayFontWidths(displayString,font) / 2).toFloat(), (
                        yPosition + height / 2 - 4).toFloat() + yOffset, if(LiquidBounce.Darkmode) Color(50, 50, 50, 255).rgb else  Color(180, 180, 180, 255).rgb,FontLoaders.C16
            )
        }
        GL11.glPopAttrib()
        GL11.glPopMatrix()
    }
}