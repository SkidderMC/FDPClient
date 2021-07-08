package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.entity.EntityValidator
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "TargetStrafe", description = "Strafe around your target.", category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val radius = FloatValue("Radius", 2.0f, 1.0f, 8.0f)
    private val render = BoolValue("Render", true)
    private val space = BoolValue("HoldSpace", false)
    private val safewalk = BoolValue("SafeWalk", true)
    private val onlySpeed = BoolValue("OnlySpeed", false)
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
        if (canStrafe(target))
            MovementUtils.strafe(MovementUtils.getSpeed())
        //    MovementUtils.setSpeed(event, MovementUtils.getSpeed().toDouble(), RotationUtils.getRotationsEntity(target).yaw, direction.toDouble(), if (mc.thePlayer.getDistanceToEntity(target) <= radius.get()) 0.0 else 1.0)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safewalk.get() && mc.thePlayer.onGround && canStrafe(LiquidBounce.combatManager.target))
            event.isSafeWalk = true
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = LiquidBounce.combatManager.target
        if (canStrafe(target) && render.get()) {
            target?:return
            GL11.glPushMatrix()
            GL11.glDisable(3553)
            GL11.glEnable(2848)
            GL11.glEnable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glHint(3153, 4354)
            GL11.glDisable(2929)
            GL11.glDepthMask(false)
            GL11.glLineWidth(3.0f)

            GL11.glBegin(3)
            val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
            val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
            val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
            for (i in 0..360) {
                val rainbow = Color(Color.HSBtoRGB((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)).toFloat() % 1.0f, 0.7f, 1.0f))
                GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                GL11.glVertex3d(x + radius.get() * cos(i * 6.283185307179586 / 45.0), y, z + radius.get() * sin(i * 6.283185307179586 / 45.0))
            }
            GL11.glEnd()

            GL11.glLineWidth(1.0f)
            GL11.glDepthMask(true)
            GL11.glEnable(2929)
            GL11.glDisable(2848)
            GL11.glDisable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3553)
            GL11.glPopMatrix()
        }
    }

    public fun canStrafe(target: EntityLivingBase?): Boolean {
        return state && target != null && targetValidator.validate(target) && (!space.get() || mc.thePlayer.movementInput.jump) && (!onlySpeed.get() || LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state)
    }
    //companion object{
        @JvmStatic
        public fun isCanStrafe(target: EntityLivingBase?): Boolean {
            return state && target != null && targetValidator.validate(target) && (!space.get() || mc.thePlayer.movementInput.jump) && (!onlySpeed.get() || LiquidBounce.moduleManager.getModule(Speed::class.java)!!.state)
        }
    @JvmStatic
    public fun calucateYaw(target: EntityLivingBase?): Long {
        var diffRange = radius.get() - mc.thePlayer.getDistanceToEntity(target)
        var targetYaw = RotationUtils.getRotationsEntity(target).yaw
        val moveSpeed = MovementUtils.getSpeed()
        
        if (diffRange>0)
            if (diffRange-moveSpeed<0)
                if (diffRange-0.47*moveSpeed<0)
                    return (targetYaw - 90 * direction + 180).toLong()
                else return (targetYaw - 75 * direction + 180).toLong()
            else return (targetYaw - 45 * direction + 180).toLong()
        else diffRange *= -1
        
        if (diffRange-moveSpeed<0)
            if (diffRange-0.85*moveSpeed>0)
                return (targetYaw + 45 * direction).toLong()
            else if (diffRange-0.6*moveSpeed>0)
                return (targetYaw + 60 * direction).toLong()
            else if (diffRange-0.323*moveSpeed>0)
                return (targetYaw + 75 * direction).toLong()
            else return (targetYaw + 90 * direction).toLong()
        else if (diffRange-2*moveSpeed>0)
                return (targetYaw).toLong()
            else if (diffRange-1.414*moveSpeed>0)
                return (targetYaw + 15 * direction).toLong()
            else if (diffRange-1.175*moveSpeed>0)
                return (targetYaw + 25 * direction).toLong()
            else if (diffRange-1.0323*moveSpeed>0)
                return (targetYaw + 35 * direction).toLong()
            else return (targetYaw + 45 * direction).toLong()
    }
   // }
    
}
