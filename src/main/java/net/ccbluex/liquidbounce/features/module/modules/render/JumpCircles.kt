/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MathUtils.toRadians
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.GLUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "JumpCircles", category = ModuleCategory.RENDER)
object JumpCircles : Module() {

    private val disappearTime = IntegerValue("Time", 1000, 1000,3000)
    private val radius = FloatValue("Radius", 2f, 1f, 5f)

    private val start = FloatValue("Start", 0.5f, 0f, 1f)
    private val end = FloatValue("End", 0.3f, 0f,1f)

    private val circles = mutableListOf<Circle>()

    @EventTarget
    fun onJump(event: JumpEvent) {
        circles.add(Circle(System.currentTimeMillis(), mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {

        glPushMatrix()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDisable(GL_CULL_FACE)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glDisable(GL_ALPHA_TEST)
        glShadeModel(GL_SMOOTH)

        circles.forEach { it.draw() }

        glDisable(GL_BLEND)
        glEnable(GL_CULL_FACE)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glEnable(GL_ALPHA_TEST)
        glShadeModel(GL_FLAT)

        glPopMatrix()
    }

    class Circle(val time: Long, val x: Double, val y: Double, val z: Double) {
        fun draw() {
            val dif = (System.currentTimeMillis() - time)
            val c = 255 - ((dif / disappearTime) * 255)

            glPushMatrix()

            glTranslated(x - mc.renderManager.viewerPosX, y - mc.renderManager.viewerPosY, z - mc.renderManager.viewerPosZ)

            glBegin(GL_TRIANGLE_STRIP)
            for (i in 0 .. 360) {
                val color = ColorUtils.rainbow(i)
                val x = (dif * radius * 0.001 * sin(i.toDouble().toRadians()))
                val z = (dif * radius * 0.001 * cos(i.toDouble().toRadians()))

                GLUtils.glColor(color.red, color.green, color.blue, 0)
                glVertex3d(x / 2, 0.0, z / 2)

                GLUtils.glColor(color.red, color.green, color.blue)
                glVertex3d(x, 0.0, z)
            }
            glEnd()

            glPopMatrix()
        }

        private fun glVertex3d(x: Unit, d: Double, z: Unit) {

        }
    }
}

private operator fun Unit.div(i: Int) {
    TODO("Not yet implemented")
}

private operator fun Any.times(d: Double) {

}

private operator fun Long.times(radius: FloatValue) {

}

private operator fun Int.minus(unit: Unit) {

}

private operator fun Unit.times(i: Int): Unit = Unit

private operator fun Long.div(disappearTime: IntegerValue) {

}

