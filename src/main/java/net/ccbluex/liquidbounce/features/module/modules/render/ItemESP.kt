/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import net.ccbluex.liquidbounce.utils.render.shader.shaders.OutlineShader
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.projectile.EntityArrow
import java.awt.Color

@ModuleInfo(name = "ItemESP", category = ModuleCategory.RENDER)
class ItemESP : Module() {
    private val modeValue =
        ListValue("Mode", arrayOf("Box", "OtherBox", "Outline", "ShaderOutline", "ShaderGlow"), "Box")
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val colorRedValue = IntegerValue("R", 0, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 0, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", true)

    private fun getColor(): Color {
        return if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val color = getColor()
        for (entity in mc.theWorld.loadedEntityList) {
            if (!(entity is EntityItem || entity is EntityArrow)) continue
            when (modeValue.get().lowercase()) {
                "box" -> RenderUtils.drawEntityBox(entity, color, true, true, outlineWidth.get())
                "otherbox" -> RenderUtils.drawEntityBox(entity, color, false, true, outlineWidth.get())
                "outline" -> RenderUtils.drawEntityBox(entity, color, true, false, outlineWidth.get())
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val shader = (if (modeValue.equals("shaderoutline")) OutlineShader.OUTLINE_SHADER else if (modeValue.equals("shaderglow")) GlowShader.GLOW_SHADER else null)
            ?: return
        val partialTicks = event.partialTicks

        shader.startDraw(partialTicks)

        for (entity in mc.theWorld.loadedEntityList) {
            if (!(entity is EntityItem || entity is EntityArrow)) continue
            mc.renderManager.renderEntityStatic(entity, event.partialTicks, true)
        }

        shader.stopDraw(getColor(), outlineWidth.get(), 1f)
    }
}