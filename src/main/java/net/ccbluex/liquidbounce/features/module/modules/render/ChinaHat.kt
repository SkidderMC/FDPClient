package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU

@ModuleInfo(name = "ChinaHat", category = ModuleCategory.RENDER)
class ChinaHat : Module() {

    private val colorRedValue = IntegerValue("Red", 255, 0, 255)
    private val colorGreenValue = IntegerValue("Green", 255, 0, 255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("Alpha", 200, 0, 255)

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        GL11.glPushMatrix()
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glColor4f(colorRedValue.get()/255f, colorGreenValue.get()/255f, colorBlueValue.get()/255f, colorAlphaValue.get()/255f)
        GL11.glTranslatef(0f, mc.thePlayer.height+0.4f, 0f)
        GL11.glRotatef(90f, 1f, 0f, 0f)

        val shaft = Cylinder()
        shaft.drawStyle = GLU.GLU_FILL
        shaft.draw(0f, 0.7f, 0.3f, 30, 1)

        GlStateManager.resetColor()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }
}