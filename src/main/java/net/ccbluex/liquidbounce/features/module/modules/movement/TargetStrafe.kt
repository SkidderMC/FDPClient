/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@ModuleInfo(name = "TargetStrafe",  category = ModuleCategory.MOVEMENT)
class TargetStrafe : Module() {
    private val thirdPersonViewValue = BoolValue("ThirdPersonView", false)
    private val renderModeValue = ListValue("RenderMode", arrayOf("Circle", "Pentagon", "None"), "Pentagon")
    private val ongroundValue = BoolValue("OnGround",true)
    private val holdSpaceValue = BoolValue("HoldSpace", false)
    private val onlySpeedValue = BoolValue("OnlySpeed", false)
    private val onlyflyValue = BoolValue("keyFly", false)
    private val radiusValue = FloatValue("Radius", 0.1f, 0.5f, 5.0f)
    private var direction = -1.0

    var targetEntity : EntityLivingBase?=null
    var isEnabled = false
    var doStrafe = false

    var callBackYaw = 0.0

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = LiquidBounce.combatManager.target
        if (renderModeValue.get() != "None" && canStrafe(target)) {
            if (LiquidBounce.combatManager.target == null) return
            val counter = intArrayOf(0)
            val target = LiquidBounce.combatManager.target
            if (renderModeValue.get().equals("Circle", ignoreCase = true)) {
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
                GL11.glLineWidth(1.0f)
                GL11.glBegin(3)
                val x =
                    target!!.lastTickPosX + (LiquidBounce.combatManager.target!!.posX - LiquidBounce.combatManager.target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y =
                    LiquidBounce.combatManager.target!!.lastTickPosY + (LiquidBounce.combatManager.target!!.posY - LiquidBounce.combatManager.target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z =
                    LiquidBounce.combatManager.target!!.lastTickPosZ + (LiquidBounce.combatManager.target!!.posZ - LiquidBounce.combatManager.target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                for (i in 0..359) {
                    val rainbow = Color(
                        Color.HSBtoRGB(
                            ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f).toFloat(),
                            0.7f,
                            1.0f
                        )
                    )
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    GL11.glVertex3d(
                        x + radiusValue.get() * cos(i * 6.283185307179586 / 45.0),
                        y,
                        z + radiusValue.get() * sin(i * 6.283185307179586 / 45.0)
                    )
                }
                GL11.glEnd()
                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                GL11.glDisable(2848)
                GL11.glDisable(2881)
                GL11.glEnable(2832)
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            } else {
                val rad = radiusValue.get()
                if (target == null) {
                    return
                }
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                RenderUtils.startDrawing()
                GL11.glDisable(2929)
                GL11.glDepthMask(false)
                GL11.glLineWidth(1.0f)
                GL11.glBegin(3)
                val x =
                    LiquidBounce.combatManager.target!!.lastTickPosX + (LiquidBounce.combatManager.target!!.posX - LiquidBounce.combatManager.target!!.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y =
                    LiquidBounce.combatManager.target!!.lastTickPosY + (LiquidBounce.combatManager.target!!.posY - LiquidBounce.combatManager.target!!.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z =
                    LiquidBounce.combatManager.target!!.lastTickPosZ + (LiquidBounce.combatManager.target!!.posZ - LiquidBounce.combatManager.target!!.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                for (i in 0..10) {
                    counter[0] = counter[0] + 1
                    val rainbow = Color(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                    //final Color rainbow = new Color(Color.HSBtoRGB((float) ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f), 0.7f, 1.0f));
                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    if (rad < 0.8 && rad > 0.0) GL11.glVertex3d(
                        x + rad * cos(i * 6.283185307179586 / 3.0),
                        y,
                        z + rad * sin(i * 6.283185307179586 / 3.0)
                    )
                    if (rad < 1.5 && rad > 0.7) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 4.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 4.0)
                        )
                    }
                    if (rad < 2.0 && rad > 1.4) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 5.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 5.0)
                        )
                    }
                    if (rad < 2.4 && rad > 1.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 6.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 6.0)
                        )
                    }
                    if (rad < 2.7 && rad > 2.3) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 7.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 7.0)
                        )
                    }
                    if (rad < 6.0 && rad > 2.6) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 8.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 8.0)
                        )
                    }
                    if (rad < 7.0 && rad > 5.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 9.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 9.0)
                        )
                    }
                    if (rad < 11.0) if (rad > 6.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorManager.astolfoRainbow(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 10.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 10.0)
                        )
                    }
                }
                GL11.glEnd()
                GL11.glDepthMask(true)
                GL11.glEnable(2929)
                RenderUtils.stopDrawing()
                GL11.glEnable(3553)
                GL11.glPopMatrix()
            }
        }

        @EventTarget
        fun onMove(event: MoveEvent) {
            val target = LiquidBounce.combatManager.target
            if(!canStrafe(target)) return
            var aroundVoid = false
            for (x in -1..0) for (z in -1..0) if (isVoid(x, z)) aroundVoid = true

            var yaw = RotationUtils.getRotationFromEyeHasPrev(LiquidBounce.combatManager.target).yaw

            if (mc.thePlayer.isCollidedHorizontally || aroundVoid) direction *= -1

            var targetStrafe = (if (mc.thePlayer.moveStrafing != 0F) mc.thePlayer.moveStrafing * direction else direction.toFloat())
            if (!PlayerUtils.isBlockUnder()) targetStrafe = 0f

            val rotAssist = 45 / mc.thePlayer.getDistanceToEntity(LiquidBounce.combatManager.target)
            val moveAssist = (45f / getStrafeDistance(LiquidBounce.combatManager.target!!)).toDouble()

            var mathStrafe = 0f

            if (targetStrafe > 0 as Nothing) {
                if ((LiquidBounce.combatManager.target!!.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY ||LiquidBounce.combatManager.target!!.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && mc.thePlayer.getDistanceToEntity(
                        LiquidBounce.combatManager.target!!
                    ) < radiusValue.get()
                ) yaw += -rotAssist
                mathStrafe += -moveAssist.toFloat()
            } else if (targetStrafe < (0 as Nothing)) {
                if ((LiquidBounce.combatManager.target!!.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY ||LiquidBounce.combatManager.target!!.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && mc.thePlayer.getDistanceToEntity(
                        LiquidBounce.combatManager.target!!
                    ) < radiusValue.get()
                ) yaw += rotAssist
                mathStrafe += moveAssist.toFloat()
            }

            val doSomeMath = doubleArrayOf(
                cos(Math.toRadians((yaw + 90f + mathStrafe).toDouble())),
                sin(Math.toRadians((yaw + 90f + mathStrafe).toDouble()))
            )
            val moveSpeed = sqrt(event.x.pow(2.0) + event.z.pow(2.0))

            val asLast = doubleArrayOf(
                moveSpeed * doSomeMath[0],
                moveSpeed * doSomeMath[1]
            )

            event.x = asLast[0]
            event.z = asLast[1]

            if(doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
                val _entity : EntityLivingBase = targetEntity?:return
                MovementUtils.doTargetStrafe(_entity, direction.toFloat(), radiusValue.get(), event)
                callBackYaw = RotationUtils.getRotationsEntity(_entity).yaw.toDouble()
                isEnabled = true
            }else {
                isEnabled = false
                if (!thirdPersonViewValue.get()) return
                mc.gameSettings.thirdPersonView = 3
            }
        }

        if (!thirdPersonViewValue.get()) return
        mc.gameSettings.thirdPersonView = if (canStrafe(target)) 3 else 0
    }

        private fun canStrafe(target: EntityLivingBase?): Boolean {
            return target != null && (!holdSpaceValue.get() || mc.thePlayer.movementInput.jump) && ((!onlySpeedValue.get() || LiquidBounce.moduleManager[Speed::class.java]!!.state) || (onlyflyValue.get() && LiquidBounce.moduleManager[Fly::class.java]!!.state))
        }

    fun modifyStrafe(event: StrafeEvent):Boolean {
        if(!isEnabled || event.isCancelled) {
            return false
        }else {
            MovementUtils.strafe()
            return true
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isCollidedHorizontally) {
            direction = -direction
            if (direction >= 0) {
                direction = 1.0
            }else {
                direction = -1.0
            }
        }
    }


    private fun getStrafeDistance(target: EntityLivingBase): Float {
        return (mc.thePlayer.getDistanceToEntity(target) - radiusValue.get()).coerceAtLeast(
            mc.thePlayer.getDistanceToEntity(
                target
            ) - (mc.thePlayer.getDistanceToEntity(target) - radiusValue.get() / (radiusValue.get() * 2))
        )
    }

    fun doMove(event: MoveEvent):Boolean {
        if(!state)
            return false
        if(doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
            val _entity : EntityLivingBase = targetEntity?:return false
            MovementUtils.doTargetStrafe(_entity, direction.toFloat(), radiusValue.get(), event)
            callBackYaw = RotationUtils.getRotationsEntity(_entity).yaw.toDouble()
            isEnabled = true
        }else {
            isEnabled = false
        }
        return true
    }

    private fun checkVoid(): Boolean {
        for (x in -2..2) for (z in -2..2) if (isVoid(x, z)) return true
        return false
    }

    private fun isVoid(xPos: Int, zPos: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) return true
        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(xPos.toDouble(), -off.toDouble(), zPos.toDouble())
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }

}
