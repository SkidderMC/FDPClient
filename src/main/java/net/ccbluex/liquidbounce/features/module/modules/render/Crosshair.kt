/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "Crosshair", category = ModuleCategory.RENDER)
object Crosshair : Module() {
    // Color
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Slowly", "Rainbow"), "Custom")
    private val colorRedValue = IntegerValue("Red", 255, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255).displayable { colorModeValue.equals("Custom") }
    private val colorAlphaValue = IntegerValue("Alpha", 255, 0, 255)

    // Rainbow thingy
    private val saturationValue = FloatValue("Saturation", 1f, 0f, 1f).displayable { colorModeValue.equals("Slowly") }
    private val brightnessValue = FloatValue("Brightness", 1f, 0f, 1f).displayable { colorModeValue.equals("Slowly") }

    // Size, width, hitmarker
    private val widthValue = FloatValue("Width", 0.5f, 0.25f, 10f)
    private val sizeValue = FloatValue("Length", 7f, 0.25f, 15f)
    private val gapValue = FloatValue("Gap", 5f, 0.25f, 15f)
    private val dynamicValue = BoolValue("Dynamic", true)
    private val hitMarkerValue = BoolValue("HitMarker", true)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val sr = event.scaledResolution
        val width = widthValue.get()
        val size = sizeValue.get()
        val gap = gapValue.get()
        val isMoving = dynamicValue.get() && MovementUtils.isMoving()
        GL11.glPushMatrix()
        RenderUtils.drawBorderedRect(sr.scaledWidth / 2f - width, sr.scaledHeight / 2f - gap - size - if (isMoving) 2 else 0, sr.scaledWidth / 2f + 1.0f + width, sr.scaledHeight / 2f - gap - if (isMoving) 2 else 0, 0.5f, Color(0, 0, 0).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(sr.scaledWidth / 2f - width, sr.scaledHeight / 2f + gap + 1 + (if (isMoving) 2 else 0) - 0.15f, sr.scaledWidth / 2f + 1.0f + width, sr.scaledHeight / 2f + 1 + gap + size + (if (isMoving) 2 else 0) - 0.15f, 0.5f, Color(0, 0, 0).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(sr.scaledWidth / 2f - gap - size - (if (isMoving) 2 else 0) + 0.15f, sr.scaledHeight / 2f - width, sr.scaledWidth / 2f - gap - (if (isMoving) 2 else 0) + 0.15f, sr.scaledHeight / 2 + 1.0f + width, 0.5f, Color(0, 0, 0).rgb, crosshairColor.rgb)
        RenderUtils.drawBorderedRect(sr.scaledWidth / 2f + 1 + gap + if (isMoving) 2 else 0, sr.scaledHeight / 2f - width, sr.scaledWidth / 2f + size + gap + 1.0f + if (isMoving) 2 else 0, sr.scaledHeight / 2 + 1.0f + width, 0.5f, Color(0, 0, 0).rgb, crosshairColor.rgb)
        GL11.glPopMatrix()
        GlStateManager.resetColor()
        val target = FDPClient.combatManager.target/* ?: RaycastUtils.raycastEntity(Reach.hitReach.toDouble()) {
            it is EntityLivingBase
        } as EntityLivingBase? */
        if (hitMarkerValue.get() && target != null && target.hurtTime > 0) {
            GL11.glPushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            GL11.glColor4f(1f, 1f, 1f, target.hurtTime.toFloat() / target.maxHurtTime.toFloat())
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glLineWidth(1f)
            GL11.glBegin(3)
            GL11.glVertex2f(sr.scaledWidth / 2f + gap, sr.scaledHeight / 2f + gap)
            GL11.glVertex2f(sr.scaledWidth / 2f + gap + size, sr.scaledHeight / 2f + gap + size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(sr.scaledWidth / 2f - gap, sr.scaledHeight / 2f - gap)
            GL11.glVertex2f(sr.scaledWidth / 2f - gap - size, sr.scaledHeight / 2f - gap - size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(sr.scaledWidth / 2f - gap, sr.scaledHeight / 2f + gap)
            GL11.glVertex2f(sr.scaledWidth / 2f - gap - size, sr.scaledHeight / 2f + gap + size)
            GL11.glEnd()
            GL11.glBegin(3)
            GL11.glVertex2f(sr.scaledWidth / 2f + gap, sr.scaledHeight / 2f - gap)
            GL11.glVertex2f(sr.scaledWidth / 2f + gap + size, sr.scaledHeight / 2f - gap - size)
            GL11.glEnd()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GL11.glPopMatrix()
        }
    }

    private val crosshairColor: Color
        get() =
            when (colorModeValue.get().lowercase()) {
                "custom" -> Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
                "slowly" -> ColorUtils.reAlpha(ColorUtils.slowlyRainbow(System.nanoTime(), 0, saturationValue.get(), brightnessValue.get()), colorAlphaValue.get())
                "rainbow" -> ColorUtils.rainbowWithAlpha(colorAlphaValue.get())
                else -> Color.WHITE
            }
}