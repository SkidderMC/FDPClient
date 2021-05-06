package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.entity.EntityValidator
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "TargetStrafe", description = "Strafe around your target.", category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val radius = FloatValue("Radius", 2.0f, 0.1f, 4.0f)
    private val render = BoolValue("Render", true)
    private val space = BoolValue("HoldSpace", false)
    private val targetValidator: EntityValidator = EntityValidator()
    private var direction = -1

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState === EventState.PRE) {
            if (mc.thePlayer.isCollidedHorizontally) {
                switchDirection()
            }
            if (mc.gameSettings.keyBindLeft.isKeyDown) {
                direction = 1
            }
            if (mc.gameSettings.keyBindRight.isKeyDown) {
                direction = -1
            }
        }
    }

    private fun switchDirection() {
        direction = if (direction == 1) -1 else 1
    }

    @EventTarget
    fun strafe(event: MoveEvent) {
        val target = LiquidBounce.combatManager.target
        if (target != null)
            MovementUtils.setSpeed(event, MovementUtils.getSpeed().toDouble(), RotationUtils.getRotationsEntity(target).yaw, direction.toDouble(), if (mc.thePlayer.getDistanceToEntity(target) <= radius.get()) 0.0 else 1.0)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = LiquidBounce.combatManager.target
        if (canStrafe(target) && render.get()) {
            drawCircle(target!!, event.partialTicks, radius.get().toDouble())
        }
    }

    private fun drawCircle(entity: EntityLivingBase, partialTicks: Float, rad: Double) {
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        RenderUtils.startSmooth()
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glLineWidth(1.0f)
        GL11.glBegin(3)
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
        for (i in 0..360) {
            val rainbow = Color(Color.HSBtoRGB((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
            GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
            GL11.glVertex3d(x + rad * cos(i * 6.283185307179586 / 45.0), y, z + rad * sin(i * 6.283185307179586 / 45.0))
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        RenderUtils.endSmooth()
        GL11.glEnable(3553)
        GL11.glPopMatrix()
    }

    private fun canStrafe(target: EntityLivingBase?): Boolean {
        return state && target != null && targetValidator.validate(target) && (!space.get() || mc.thePlayer.movementInput.jump)
    }
}