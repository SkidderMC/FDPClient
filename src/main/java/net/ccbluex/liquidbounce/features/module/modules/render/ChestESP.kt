/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.launch.data.legacyui.ClickGUIModule
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.EventTarget

import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest

import org.lwjgl.opengl.GL11
import java.awt.Color

@ModuleInfo(name = "ChestESP", category = ModuleCategory.RENDER)
class ChestESP : Module() {
    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
        GL11.glPushMatrix()
        var amount = 0
        for (tileEntity in mc.theWorld.loadedTileEntityList) {
            if (tileEntity is TileEntityChest || tileEntity is TileEntityEnderChest) {
                render(amount, tileEntity)
                amount++
            }
        }
        GL11.glPopMatrix()
        GL11.glPopAttrib()
    }

    private fun render(amount: Int, p: TileEntity) {
        GL11.glPushMatrix()
        val renderManager = mc.renderManager
        val x = p.pos.x + 0.5 - renderManager.renderPosX
        val y = p.pos.y - renderManager.renderPosY
        val z = p.pos.z + 0.5 - renderManager.renderPosZ
        GL11.glTranslated(x, y, z)
        GL11.glRotated(-renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        GL11.glRotated(
            renderManager.playerViewX.toDouble(),
            if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0,
            0.0,
            0.0
        )
        val scale = 1 / 100f
        GL11.glScalef(-scale, -scale, scale)
        val offset = renderManager.playerViewX * 0.5f
        RenderUtils.lineNoGl(
            -50.0,
            offset.toDouble(),
            50.0,
            offset.toDouble(),
            Color(ClickGUIModule.colorRedValue.get(), ClickGUIModule.colorGreenValue.get(), ClickGUIModule.colorBlueValue.get())
        )
        RenderUtils.lineNoGl(
            -50.0,
            (-95 + offset).toDouble(),
            -50.0,
            offset.toDouble(),
            Color(ClickGUIModule.colorRedValue.get(), ClickGUIModule.colorGreenValue.get(), ClickGUIModule.colorBlueValue.get())
        )
        RenderUtils.lineNoGl(
            -50.0,
            (-95 + offset).toDouble(),
            50.0,
            (-95 + offset).toDouble(),
            Color(ClickGUIModule.colorRedValue.get(), ClickGUIModule.colorGreenValue.get(), ClickGUIModule.colorBlueValue.get())
        )
        RenderUtils.lineNoGl(
            50.0,
            (-95 + offset).toDouble(),
            50.0,
            offset.toDouble(),
            Color(ClickGUIModule.colorRedValue.get(), ClickGUIModule.colorGreenValue.get(), ClickGUIModule.colorBlueValue.get())
        )
        GL11.glPopMatrix()
    }
}