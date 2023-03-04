/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.MathUtils.toRadians
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "JumpCircle", category = ModuleCategory.RENDER)
class JumpCircle : Module() {

    val disappearTime = IntegerValue("Time", 1000, 1000,3000)
    val radius = FloatValue("Radius", 2f, 1f,5f)

    val rainbow = BoolValue("Rainbow", false)

    val start = FloatValue("Start", 0.5f, 0f,1f)
    val end = FloatValue("End", 0.3f, 0f,1f)

    val circles = mutableListOf<Circle>()
    var lastOnGround = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround && lastOnGround) {
            circles.add(Circle(System.currentTimeMillis(), mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
            lastOnGround = false
        }
        if (mc.thePlayer.onGround) {
            lastOnGround = true
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        circles.removeIf { System.currentTimeMillis() > it.time + disappearTime.get() }

        GL11.glPushMatrix()

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        GL11.glShadeModel(GL11.GL_SMOOTH)

        circles.forEach { it.draw() }

        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_ALPHA_TEST)
        GL11.glShadeModel(GL11.GL_FLAT)

        GL11.glPopMatrix()
    }

    class Circle(val time: Long, val x: Double, val y: Double, val z: Double){
        val jumpModule = LiquidBounce.moduleManager.getModule(JumpCircle::class.java) as JumpCircle

        fun draw() {

            val dif = (System.currentTimeMillis() - time)
            val c = 255 - (dif / jumpModule.disappearTime.get().toFloat()) * 255

            GL11.glPushMatrix()

            GL11.glTranslated(
                x - mc.renderManager.viewerPosX,
                y - mc.renderManager.viewerPosY,
                z - mc.renderManager.viewerPosZ
            )

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
            for (i in 0..360) {
                val color = if (jumpModule.rainbow.get()) Color.getHSBColor(i / 360f, 1f, 1f)
                else ColorUtils.hsbTransition(jumpModule.start.get(), jumpModule.end.get(), i)

                val x = (dif * jumpModule.radius.get() * 0.001 * sin(i.toDouble().toRadians()))
                val z = (dif * jumpModule.radius.get() * 0.001 * cos(i.toDouble().toRadians()))

                RenderUtils.glColor(color.red, color.green, color.blue, 0)
                GL11.glVertex3d(x / 2, 0.0, z / 2)

                RenderUtils.glColor(color.red, color.green, color.blue, c.toInt())
                GL11.glVertex3d(x, 0.0, z)
            }
            GL11.glEnd()

            GL11.glPopMatrix()
        }
    }

}
