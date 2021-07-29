/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawTriAngle
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.MathHelper
import java.awt.Color
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "PointerESP", category = ModuleCategory.RENDER)
class PointerESP : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Solid","Line"),"Solid")
    private val redValue = IntegerValue("Red",140,0,255)
    private val greenValue = IntegerValue("Green",140,0,255)
    private val blueValue = IntegerValue("Blue",255,0,255)
    private val alphaValue = IntegerValue("Alpha",255,0,255)

    @EventTarget
    fun onRender2D(event : Render2DEvent) {
        val sr = ScaledResolution(mc)
        val color=Color(redValue.get(),greenValue.get(),blueValue.get(),alphaValue.get())

        GlStateManager.pushMatrix()
        val size = 50
        val xOffset = sr.scaledWidth / 2 - 24.5
        val yOffset = sr.scaledHeight / 2 - 25.2
        val playerOffsetX = mc.thePlayer.posX
        val playerOffSetZ = mc.thePlayer.posZ

        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity,true)) {
                val pos1 = (((entity.posX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks) - playerOffsetX) * 0.2)
                val pos2 = (((entity.posZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks) - playerOffSetZ) * 0.2)
                val cos = cos(mc.thePlayer.rotationYaw * (Math.PI * 2 / 360))
                val sin = sin(mc.thePlayer.rotationYaw * (Math.PI * 2 / 360))
                val rotY = -(pos2 * cos - pos1 * sin)
                val rotX = -(pos1 * cos + pos2 * sin)
                val var7 = 0 - rotX
                val var9 = 0 - rotY
                if (MathHelper.sqrt_double(var7 * var7 + var9 * var9) < size / 2 - 4) {
                    val angle = (atan2(rotY - 0, rotX - 0) * 180 / Math.PI).toFloat()
                    val x = ((size / 2) * cos(Math.toRadians(angle.toDouble()))) + xOffset + size / 2;
                    val y = ((size / 2) * sin(Math.toRadians(angle.toDouble()))) + yOffset + size / 2;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x, y, 0.0)
                    GlStateManager.rotate(angle, 0F, 0F, 1F)
                    GlStateManager.scale(1.5, 1.0, 1.0)
                    when(modeValue.get().toLowerCase()){
                        "solid" -> {
                            drawTriAngle(0F, 0F, 2.2F, 3F, color)
                            drawTriAngle(0F, 0F, 1.5F, 3F, color)
                            drawTriAngle(0F, 0F, 1.0F, 3F, color)
                            drawTriAngle(0F, 0F, 0.5F, 3F, color)
                        }
                        "line" -> {
                            drawTriAngle(0F, 0F, 2.2F, 3F, color)
                        }
                    }
                    GlStateManager.popMatrix()
                }
            }
        }

        GlStateManager.popMatrix()
    }
}
