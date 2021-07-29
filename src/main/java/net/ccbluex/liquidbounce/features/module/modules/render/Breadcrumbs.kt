/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

@ModuleInfo(name = "Breadcrumbs", category = ModuleCategory.RENDER)
class Breadcrumbs : Module() {
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)
    private val fade = BoolValue("Fade",true)
    private val fadeTime = IntegerValue("FadeTime",5,1,20)

    private val positions = LinkedList<DoubleArray>()
    private val loadTime=System.currentTimeMillis()
    private var head=0

    fun getColor(): Color {
        return if (colorRainbow.get()) rainbow() else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get())
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color = getColor()
        val fTime=fadeTime.get()*1000
        val fadeSec=(System.currentTimeMillis()-fTime-loadTime).toInt()

        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(color)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (i in head until positions.size) {
                val pos=positions[i]
                if(fade.get()) {
                    val pct=((pos[3] - fadeSec) / fTime).toFloat()
                    if(pct<0||pct>1){
                        head=i
                        continue
                    }
                    RenderUtils.glColor(color, pct)
                }
                GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            }
            GL11.glColor4d(1.0,1.0,1.0,1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(positions) {
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ,(System.currentTimeMillis()-loadTime).toDouble()))
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent){
        synchronized(positions) {
            positions.clear()
            head=0
        }
    }

    override fun onEnable() {
        head=0
        if (mc.thePlayer == null) return
    }

    override fun onDisable() {
        synchronized(positions) {
            positions.clear()
            head=0
        }
    }
}