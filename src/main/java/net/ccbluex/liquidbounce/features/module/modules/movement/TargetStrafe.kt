/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@ModuleInfo(name = "TargetStrafe",  category = ModuleCategory.MOVEMENT)
object TargetStrafe : Module() {

    private val thirdPersonViewValue = BoolValue("ThirdPersonView", false)

    private val radiusValue = FloatValue("Radius", 0.5f, 0.1f, 5.0f)
    private val radiusModeValue = ListValue("RadiusMode", arrayOf("Normal", "Strict"/*, "Dynamic"*/), "Normal")

    private val ongroundValue = BoolValue("OnlyOnGround",false)
    private val holdSpaceValue = BoolValue("HoldSpace", false)
    private val onlySpeedValue = BoolValue("OnlySpeed", true)

    private val pointsProperty = IntegerValue("Points", 12, 1, 18)
    private val adaptiveSpeedProperty = BoolValue("Adapt Speed", true)
    private val lineWidthValue = FloatValue("LineWidth", 1f, 1f, 10f).displayable {!renderModeValue.equals("None")}
    private val trips = FloatValue("Trips", 2.0f, 0.1f, 4.0f)

    private val renderModeValue = ListValue("RenderMode", arrayOf("Circle", "Polygon", "Zavz", "None"), "Zavz")
    private val zavzRender = ListValue("Zavz-Render", arrayOf("Circle", "Points"), "Points").displayable { renderModeValue.equals("Zavz") }

    private var redValue = IntegerValue("Zavz-Red", 0, 0, 255).displayable { renderModeValue.get().equals("Zavz", true) }
    private var greenValue = IntegerValue("Zavz-Green", 0, 0, 255).displayable { renderModeValue.get().equals("Zavz", true) }
    private var blueValue = IntegerValue("Zavz-Blue", 0, 0, 255).displayable { renderModeValue.get().equals("Zavz", true) }
    private var alphaValue = IntegerValue("Zavz-Alpha", 255, 0, 255).displayable { renderModeValue.get().equals("Zavz", true) }
    private var rainbowValue = BoolValue("Zavz-RainBow", false).displayable { renderModeValue.get().equals("Zavz", true) }


    private var direction = -1.0
    private var directionA = 1

    private val currentPoints: ArrayList<Point> = ArrayList()
    private var currentPoint: Point? = null

    var targetEntity : EntityLivingBase?=null
    private var isEnabled = false
    var doStrafe = false

    private var callBackYaw = 0.0

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = targetEntity
        if (renderModeValue.get() != "None" && canStrafe(target)) {
            if (target == null || !doStrafe) return
            val counter = intArrayOf(0)
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
                GL11.glLineWidth(lineWidthValue.get())
                GL11.glBegin(3)
                val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
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
            } else if(renderModeValue.get().equals("Polygon", true)) {
                val rad = radiusValue.get()
                GL11.glPushMatrix()
                GL11.glDisable(3553)
                RenderUtils.startDrawing()
                GL11.glDisable(2929)
                GL11.glDepthMask(false)
                GL11.glLineWidth(lineWidthValue.get())
                GL11.glBegin(3)
                val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                for (i in 0..10) {
                    counter[0] = counter[0] + 1
                    val rainbow = Color(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))

                    GL11.glColor3f(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f)
                    if (rad < 0.8 && rad > 0.0) GL11.glVertex3d(
                        x + rad * cos(i * 6.283185307179586 / 3.0),
                        y,
                        z + rad * sin(i * 6.283185307179586 / 3.0)
                    )
                    if (rad < 1.5 && rad > 0.7) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 4.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 4.0)
                        )
                    }
                    if (rad < 2.0 && rad > 1.4) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 5.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 5.0)
                        )
                    }
                    if (rad < 2.4 && rad > 1.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 6.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 6.0)
                        )
                    }
                    if (rad < 2.7 && rad > 2.3) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 7.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 7.0)
                        )
                    }
                    if (rad < 6.0 && rad > 2.6) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 8.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 8.0)
                        )
                    }
                    if (rad < 7.0 && rad > 5.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
                        GL11.glVertex3d(
                            x + rad * cos(i * 6.283185307179586 / 9.0),
                            y,
                            z + rad * sin(i * 6.283185307179586 / 9.0)
                        )
                    }
                    if (rad < 11.0) if (rad > 6.9) {
                        counter[0] = counter[0] + 1
                        RenderUtils.glColor(ColorUtils.otherAstolfo(counter[0] * 100, 5, 107))
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
            } else if (renderModeValue.get().equals("Zavz", true)) {
                GL11.glPushMatrix()
                mc.entityRenderer.disableLightmap()
                GL11.glDisable(3553)
                GL11.glEnable(3042)
                GL11.glBlendFunc(770, 771)
                GL11.glDisable(2929)
                GL11.glEnable(2848)
                GL11.glDepthMask(false)
                val x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
                val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
                val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
                GL11.glPushMatrix()
                val tau = 6.283185307179586
                val fans = 45.0
                GL11.glLineWidth(2.0f)
                if (zavzRender.get().equals("Points", true)) {
                    GL11.glEnable(GL11.GL_POINT_SMOOTH)
                    GL11.glPointSize(7.0f)
                    GL11.glBegin(GL11.GL_POINTS)
                    for (i in 0..90) {
                        val rainbow = Color(
                            Color.HSBtoRGB(
                                ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f).toFloat(),
                                0.7f,
                                1.0f
                            )
                        )

                        RenderUtils.color(if (rainbowValue.get()) Color(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f).rgb else Color(
                            redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()).rgb)
                        GL11.glVertex3d(
                            x + radiusValue.get() * cos(i * Math.PI * 2 / 45.0),
                            y,
                            z + radiusValue.get() * sin(i * Math.PI * 2 / 45.0)
                        )
                    }
                    GL11.glEnd()
                } else {
                    GL11.glBegin(1)
                    for (i in 0..90) {
                        val rainbow = Color(
                            Color.HSBtoRGB(
                                ((mc.thePlayer.ticksExisted / 70.0 + sin(i / 50.0 * 1.75)) % 1.0f).toFloat(),
                                0.7f,
                                1.0f
                            )
                        )

                        RenderUtils.color(if (rainbowValue.get()) Color(rainbow.red / 255.0f, rainbow.green / 255.0f, rainbow.blue / 255.0f).rgb else Color(
                            redValue.get(), greenValue.get(), blueValue.get(), alphaValue.get()).rgb)
                        GL11.glVertex3d(
                            x + radiusValue.get() * cos(i * Math.PI * 2 / 45.0),
                            y,
                            z + radiusValue.get() * sin(i * Math.PI * 2 / 45.0)
                        )
                    }
                    GL11.glEnd()
                }
                GL11.glPopMatrix()

                GL11.glDepthMask(true)
                GL11.glDisable(2848)
                GL11.glEnable(2929)
                GL11.glDisable(3042)
                GL11.glEnable(3553)
                mc.entityRenderer.enableLightmap()
                GL11.glPopMatrix()
            }
        }

        @EventTarget
        fun onMove(event: MoveEvent) {
            if(doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
                val entityStrafe : EntityLivingBase = targetEntity ?:return
                if(!canStrafe(entityStrafe)) {
                    isEnabled = false
                    return
                }
                var aroundVoid = false
                for (x in -1..0) for (z in -1..0)
                    if (isVoid(x, z))
                        aroundVoid = true
                if (aroundVoid)
                    direction *= -1
                var numberStrafe = 0
                if (radiusModeValue.get().equals("Strict", ignoreCase = true)) {
                    numberStrafe = 1
                }
                MovementUtils.doTargetStrafe(entityStrafe, direction.toFloat(), radiusValue.get(), event, numberStrafe)
                callBackYaw = RotationUtils.getRotationsEntity(entityStrafe).yaw.toDouble()
                isEnabled = true
                if (!thirdPersonViewValue.get())
                    return
                mc.gameSettings.thirdPersonView = if (canStrafe(target)) 3 else 0
            }else {
                isEnabled = false
                if (!thirdPersonViewValue.get()) return
                mc.gameSettings.thirdPersonView = 3
            }
        }
    }

    private fun isCloseToPoint(point: Point): Boolean {
        return PlayerUtils.distance(mc.thePlayer.posX, mc.thePlayer.posZ, point.point.xCoord, point.point.zCoord) < 0.2
    }
    fun shouldAdaptSpeed(): Boolean {
        return if (!adaptiveSpeedProperty.get()) false else isCloseToPoint(currentPoint!!)
    }

    fun getAdaptedSpeed(): Double {
        val aura = FDPClient.moduleManager[KillAura::class.java] as KillAura

        val entity: EntityLivingBase = aura.currentTarget ?: return 0.0
        return PlayerUtils.distance(entity.prevPosX, entity.prevPosZ, entity.posX, entity.posZ)
    }

    private fun canStrafe(target: EntityLivingBase?): Boolean {
        return target != null && (!holdSpaceValue.get() || mc.thePlayer.movementInput.jump) && (!onlySpeedValue.get() || FDPClient.moduleManager[Speed::class.java]!!.state)
    }

    fun modifyStrafe(event: StrafeEvent):Boolean {
        return if(!isEnabled || event.isCancelled) {
            false
        }else {
            MovementUtils.strafe()
            true
        }
    }

    fun toggleStrafe(): Boolean {
        return targetEntity != null && (!holdSpaceValue.get() || mc.thePlayer.movementInput.jump) && (!onlySpeedValue.get() || FDPClient.moduleManager[Speed::class.java]!!.state)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isCollidedHorizontally) {
            direction = -direction
            direction = if (direction >= 0) {
                1.0
            }else {
                -1.0
            }
        }

        val aura = FDPClient.moduleManager[KillAura::class.java] as KillAura

        currentPoint = if (aura.currentTarget != null) {

            collectPoints(
                (pointsProperty.get() * radiusValue.get()).roundToInt(),
                radiusValue.get().toDouble(),
                aura.currentTarget!!
            )

            findOptimalPoint(aura.currentTarget!!, currentPoints)
        } else {
            null
        }

    }

    fun setSpeed(event: MoveEvent, speed: Double) {
        val point: Point? = currentPoint
        MovementUtils.setSpeed2(
            event, speed, 1F, 0F,
            RotationUtils.calculateYawFromSrcToDst(
                mc.thePlayer.rotationYaw,
                mc.thePlayer.posX, mc.thePlayer.posZ,
                point!!.point.xCoord, point.point.zCoord
            )
        )
    }

    private fun findOptimalPoint(
        target: EntityLivingBase,
        points: List<Point>
    ): Point? {

        val closest: Point =
            getClosestPoint(mc.thePlayer.posX, mc.thePlayer.posZ, points)
                ?: return null
        val pointsSize = points.size
        if (pointsSize == 1) return closest
        val closestIndex = points.indexOf(closest)
        var nextPoint: Point
        var passes = 0
        do {
            if (passes > pointsSize) // Note :: Shit fix
                return null
            var nextIndex: Int = closestIndex + directionA
            if (nextIndex < 0) nextIndex = pointsSize - 1 else if (nextIndex >= pointsSize) nextIndex = 0
            nextPoint = points[nextIndex]
            if (!nextPoint.valid) directionA = -directionA
            ++passes
        } while (!nextPoint.valid)
        return nextPoint
    }

    private fun getClosestPoint(
        srcX: Double,
        srcZ: Double,
        points: List<Point>
    ): Point? {
        var closest: Double = Double.MAX_VALUE
        var bestPoint: Point? = null
        for (point in points) {
            if (point.valid) {
                val dist: Double = PlayerUtils.distance(srcX, srcZ, point.point.xCoord, point.point.zCoord)
                if (dist < closest) {
                    closest = dist
                    bestPoint = point
                }
            }
        }
        return bestPoint
    }


    private fun collectPoints(
        size: Int,
        radius: Double,
        entity: EntityLivingBase
    ) {
        currentPoints.clear()
        val x = entity.posX
        val z = entity.posZ
        val pix2 = Math.PI * 2.0
        for (i in 0 until size) {
            val cos = radius * StrictMath.cos(i * pix2 / size)
            val sin = radius * StrictMath.sin(i * pix2 / size)
            val point: Point =
                Point(
                    entity,
                    Vec3(cos, 0.0, sin),
                    validatePoint(Vec3(x + cos, entity.posY, z + sin))
                )
            currentPoints.add(point)
        }
    }

    private fun validatePoint(point: Vec3): Boolean {
        val rayTraceResult = mc.theWorld.rayTraceBlocks(
            mc.thePlayer.positionVector, point,
            false, true, false
        )
        if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) return false

        // TODO :: Replace this with bb check
        val pointPos = BlockPos(point)
        val blockState = mc.theWorld.getBlockState(pointPos)
        if (blockState.block.canCollideCheck(blockState, false) && !blockState.block.isPassable(
                mc.theWorld,
                pointPos
            )
        ) return false
        val blockStateAbove = mc.theWorld.getBlockState(pointPos.add(0, 1, 0))
        return !blockStateAbove.block.canCollideCheck(blockState, false) &&
                !isOverVoid(point.xCoord, point.yCoord.coerceAtMost(mc.thePlayer.posY), point.zCoord)
    }

    private fun isOverVoid(
        x: Double,
        y: Double,
        z: Double
    ): Boolean {
        var posY = y
        while (posY > 0.0) {
            val state = mc.theWorld.getBlockState(BlockPos(x, posY, z))
            if (state.block.canCollideCheck(state, false)) {
                return y - posY > 2
            }
            posY--
        }
        return true
    }


    fun doMove(event: MoveEvent):Boolean {
        if(!state)
            return false
        if(doStrafe && (!ongroundValue.get() || mc.thePlayer.onGround)) {
            val entityStrafe : EntityLivingBase = targetEntity ?:return false
            MovementUtils.doTargetStrafe(entityStrafe, direction.toFloat(), radiusValue.get(), event)
            callBackYaw = RotationUtils.getRotationsEntity(entityStrafe).yaw.toDouble()
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

    private fun getData(): Array<Float> {
        if (KillAura.currentTarget == null) return arrayOf(0F, 0F, 0F)

        val target = KillAura.currentTarget!!
        val rotYaw = RotationUtils.getRotationsEntity(target).yaw

        val forward = if (mc.thePlayer.getDistanceToEntity(target) <= trips.get()) 0F else 1F
        val strafe = direction.toFloat()

        return arrayOf(rotYaw, strafe, forward)
    }

    fun getMovingYaw(): Float {
        val dt = getData()
        return MovementUtils.getRawDirectionRotation(dt[0], dt[1], dt[2])
    }

    class Point(
        private val entity: EntityLivingBase,
        private val posOffset: Vec3,
        val valid: Boolean
    ) {
        val point: Vec3

        init {
            point = calculatePos()
        }

        private fun calculatePos(): Vec3 {
            return entity.positionVector.add(posOffset)
        }

    }
}

