/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.target
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object TargetStrafe : Module("TargetStrafe", Category.MOVEMENT, gameDetecting = false) {

    private val thirdPersonViewValue by boolean("ThirdPersonView", false)

    private val radiusValue by float("Radius", 0.5f, 0.1f..5.0f)
    private val radiusModeValue by choices("RadiusMode", arrayOf("Normal", "Strict"), "Normal")

    private val ongroundValue by boolean("OnlyOnGround", false)
    private val holdSpaceValue by boolean("HoldSpace", false)
    private val onlySpeedValue by boolean("OnlySpeed", true)

    private val speedValue by float("Speed", 0.30f, 0.05f..3.0f)

    private val pointsProperty by int("Points", 12, 1..18)
    private val lineWidthValue by float("LineWidth", 1f, 1f..10f) { renderModeValue != "None" }

    private val renderModeValue by choices("RenderMode", arrayOf("Circle", "Polygon", "Zavz", "None"), "Zavz")
    private val zavzRender by choices("Mark", arrayOf("Circle", "Points"), "Points") { renderModeValue == "Zavz" }

    private val colorMode by choices(
        "Color-Mode",
        arrayOf("Custom", "Fade", "Theme"),
        "Custom"
    ) { renderModeValue != "None" }

    private val customColor1 by color("Custom-Color-1", Color(0xFF0054).rgb) {
        renderModeValue != "None" && colorMode == "Custom"
    }
    private val customColor2 by color("Custom-Color-2", Color(0x001300).rgb) {
        renderModeValue != "None" && colorMode == "Custom"
    }

    private val fadeColor1 by color("Fade-Color-1", Color(0xFF0054).rgb) {
        renderModeValue != "None" && colorMode == "Fade"
    }
    private val fadeColor2 by color("Fade-Color-2", Color(0x001300).rgb) {
        renderModeValue != "None" && colorMode == "Fade"
    }
    private val fadeDistance by int("Fade-Distance", 50, 0..100) {
        renderModeValue != "None" && colorMode == "Fade"
    }

    private var direction = -1.0
    private var directionA = 1

    private val currentPoints: ArrayList<Point> = ArrayList()
    private var currentPoint: Point? = null

    private var isEnabled = false
    var doStrafe = false

    private var callBackYaw = 0.0

    private fun getThemeColor(index: Int): Color {
        return ColorUtils.fade(Color(customColor1.rgb), index * 10, 100)
    }

    private fun getBaseColors(segmentIndex: Int): Pair<Color, Color> {
        return when (colorMode) {
            "Custom" -> Color(customColor1.rgb) to Color(customColor2.rgb)

            "Fade" -> {
                val c1 = ColorUtils.fade(Color(fadeColor1.rgb), segmentIndex * fadeDistance, 100)
                val c2 = ColorUtils.fade(Color(fadeColor2.rgb), segmentIndex * fadeDistance, 100)
                c1 to c2
            }

            "Theme" -> {
                val theme = getThemeColor(segmentIndex)
                theme to theme
            }

            else -> Color(customColor1.rgb) to Color(customColor2.rgb)
        }
    }

    private fun getSegmentColor(segmentIndex: Int): Int {
        val (c1, c2) = getBaseColors(segmentIndex)
        val t = abs(
            System.currentTimeMillis() / 360.0 +
                    (segmentIndex * 34 / 360.0) * 56 / 100.0
        ) / 10.0
        return RenderUtils.getGradientOffset(c1, c2, t).rgb
    }

    @Suppress("unused")
    val onRender3D = handler<Render3DEvent> { event ->
        val auraTarget = target as? EntityLivingBase ?: return@handler

        if (renderModeValue == "None" || !canStrafe())
            return@handler

        val partialTicks = event.partialTicks

        val x = auraTarget.lastTickPosX + (auraTarget.posX - auraTarget.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y = auraTarget.lastTickPosY + (auraTarget.posY - auraTarget.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z = auraTarget.lastTickPosZ + (auraTarget.posZ - auraTarget.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ

        val radius = radiusValue.toDouble()
        val twoPi = Math.PI * 2.0
        val circleStep = twoPi / 45.0

        if (renderModeValue.equals("Circle", ignoreCase = true)) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
            GL11.glLineWidth(lineWidthValue)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            for (i in 0..359) {
                val rgb = getSegmentColor(i)
                val c = Color(rgb, true)
                GL11.glColor4f(
                    c.red / 255.0f,
                    c.green / 255.0f,
                    c.blue / 255.0f,
                    c.alpha / 255.0f
                )
                val angle = i * circleStep
                GL11.glVertex3d(
                    x + radius * cos(angle),
                    y,
                    z + radius * sin(angle)
                )
            }

            GL11.glEnd()
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()

        } else if (renderModeValue.equals("Polygon", true)) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            RenderUtils.startDrawing()
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
            GL11.glLineWidth(lineWidthValue)
            GL11.glBegin(GL11.GL_LINE_STRIP)

            val rad = radius
            for (i in 0..10) {
                RenderUtils.glColor(getSegmentColor(i))

                val angle3 = i * twoPi / 3.0
                val angle4 = i * twoPi / 4.0
                val angle5 = i * twoPi / 5.0
                val angle6 = i * twoPi / 6.0
                val angle7 = i * twoPi / 7.0
                val angle8 = i * twoPi / 8.0
                val angle9 = i * twoPi / 9.0
                val angle10 = i * twoPi / 10.0

                if (rad < 0.8 && rad > 0.0) {
                    GL11.glVertex3d(x + rad * cos(angle3), y, z + rad * sin(angle3))
                }
                if (rad < 1.5 && rad > 0.7) {
                    GL11.glVertex3d(x + rad * cos(angle4), y, z + rad * sin(angle4))
                }
                if (rad < 2.0 && rad > 1.4) {
                    GL11.glVertex3d(x + rad * cos(angle5), y, z + rad * sin(angle5))
                }
                if (rad < 2.4 && rad > 1.9) {
                    GL11.glVertex3d(x + rad * cos(angle6), y, z + rad * sin(angle6))
                }
                if (rad < 2.7 && rad > 2.3) {
                    GL11.glVertex3d(x + rad * cos(angle7), y, z + rad * sin(angle7))
                }
                if (rad < 6.0 && rad > 2.6) {
                    GL11.glVertex3d(x + rad * cos(angle8), y, z + rad * sin(angle8))
                }
                if (rad < 7.0 && rad > 5.9) {
                    GL11.glVertex3d(x + rad * cos(angle9), y, z + rad * sin(angle9))
                }
                if (rad < 11.0 && rad > 6.9) {
                    GL11.glVertex3d(x + rad * cos(angle10), y, z + rad * sin(angle10))
                }
            }

            GL11.glEnd()
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            RenderUtils.stopDrawing()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()

        } else if (renderModeValue.equals("Zavz", true)) {
            GL11.glPushMatrix()
            mc.entityRenderer.disableLightmap()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDepthMask(false)

            GL11.glPushMatrix()
            GL11.glLineWidth(2.0f)

            if (zavzRender.equals("Points", true)) {
                GL11.glEnable(GL11.GL_POINT_SMOOTH)
                GL11.glPointSize(7.0f)
                GL11.glBegin(GL11.GL_POINTS)
                for (i in 0..90) {
                    RenderUtils.color(getSegmentColor(i))
                    val angle = i * circleStep
                    GL11.glVertex3d(
                        x + radius * cos(angle),
                        y,
                        z + radius * sin(angle)
                    )
                }
                GL11.glEnd()
            } else {
                GL11.glBegin(GL11.GL_LINE_STRIP)
                for (i in 0..90) {
                    RenderUtils.color(getSegmentColor(i))
                    val angle = i * circleStep
                    GL11.glVertex3d(
                        x + radius * cos(angle),
                        y,
                        z + radius * sin(angle)
                    )
                }
                GL11.glEnd()
            }

            GL11.glPopMatrix()

            GL11.glDepthMask(true)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            mc.entityRenderer.enableLightmap()
            GL11.glPopMatrix()
        }
    }

    @Suppress("unused")
    val onMove = handler<MoveEvent> { event ->
        val auraTarget = target ?: run {
            isEnabled = false
            if (thirdPersonViewValue) mc.gameSettings.thirdPersonView = 0
            return@handler
        }

        if (!doStrafe || (ongroundValue && !mc.thePlayer.onGround) || !canStrafe()) {
            isEnabled = false
            if (thirdPersonViewValue) mc.gameSettings.thirdPersonView = 0
            return@handler
        }

        var aroundVoid = false
        for (x in -1..0) {
            for (z in -1..0) {
                if (isVoid(x, z)) {
                    aroundVoid = true
                    break
                }
            }
            if (aroundVoid) break
        }

        if (aroundVoid) {
            direction = -direction
        }

        val numberStrafe = if (radiusModeValue.equals("Strict", ignoreCase = true)) 1 else 0

        MovementUtils.doTargetStrafe(auraTarget, direction.toFloat(), radiusValue, event, numberStrafe)
        callBackYaw = RotationUtils.getRotationsEntity(auraTarget).yaw.toDouble()
        isEnabled = true

        if (thirdPersonViewValue) {
            mc.gameSettings.thirdPersonView = 3
        }
    }

    @Suppress("unused")
    val modifyStrafe = handler<StrafeEvent> { event ->
        if (!isEnabled) return@handler

        event.cancelEvent()
        MovementUtils.strafe()
    }

    private fun isCloseToPoint(point: Point): Boolean {
        return MovementUtils.distance(
            mc.thePlayer.posX,
            mc.thePlayer.posZ,
            point.point.xCoord,
            point.point.zCoord
        ) < 0.2
    }

    private fun canStrafe(): Boolean {
        return (!holdSpaceValue || mc.thePlayer.movementInput.jump) &&
                (!onlySpeedValue || Speed.state)
    }

    @Suppress("unused")
    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer.isCollidedHorizontally) {
            direction = -direction
            direction = if (direction >= 0) 1.0 else -1.0
        }

        currentPoint = if (target != null) {
            val entity = target as EntityLivingBase
            collectPoints(
                (pointsProperty * radiusValue).roundToInt(),
                radiusValue.toDouble(),
                entity
            )
            findOptimalPoint(entity, currentPoints)
        } else {
            null
        }
    }

    @Suppress("unused")
    val doSetSpeed = handler<MoveEvent> { event ->
        val point = currentPoint ?: return@handler

        val speed = speedValue.toDouble()

        MovementUtils.setSpeed(
            event,
            speed,
            1f,
            0f,
            RotationUtils.calculateYawFromSrcToDst(
                mc.thePlayer.rotationYaw,
                mc.thePlayer.posX, mc.thePlayer.posZ,
                point.point.xCoord, point.point.zCoord
            )
        )
    }

    private fun findOptimalPoint(
        target: EntityLivingBase,
        points: List<Point>
    ): Point? {
        val closest = getClosestPoint(mc.thePlayer.posX, mc.thePlayer.posZ, points) ?: return null

        val pointsSize = points.size
        if (pointsSize == 1) return closest

        val closestIndex = points.indexOf(closest)
        var nextPoint: Point
        var passes = 0

        do {
            if (passes > pointsSize)
                return null

            var nextIndex = closestIndex + directionA
            if (nextIndex < 0) nextIndex = pointsSize - 1
            else if (nextIndex >= pointsSize) nextIndex = 0

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
        var closest = Double.MAX_VALUE
        var bestPoint: Point? = null

        for (point in points) {
            if (point.valid) {
                val dist = MovementUtils.distance(
                    srcX,
                    srcZ,
                    point.point.xCoord,
                    point.point.zCoord
                )
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
            val angle = i * pix2 / size
            val cos = radius * cos(angle)
            val sin = radius * sin(angle)
            val point = Point(
                entity,
                Vec3(cos, 0.0, sin),
                validatePoint(Vec3(x + cos, entity.posY, z + sin))
            )
            currentPoints.add(point)
        }
    }

    private fun validatePoint(point: Vec3): Boolean {
        val rayTraceResult = mc.theWorld.rayTraceBlocks(
            mc.thePlayer.positionVector,
            point,
            false,
            true,
            false
        )
        if (rayTraceResult != null &&
            rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
        ) return false

        val pointPos = BlockPos(point)
        val blockState = mc.theWorld.getBlockState(pointPos)
        if (blockState.block.canCollideCheck(blockState, false) &&
            !blockState.block.isPassable(mc.theWorld, pointPos)
        ) return false

        val blockStateAbove = mc.theWorld.getBlockState(pointPos.add(0, 1, 0))
        return !blockStateAbove.block.canCollideCheck(blockState, false) &&
                !isOverVoid(
                    point.xCoord,
                    point.yCoord.coerceAtMost(mc.thePlayer.posY),
                    point.zCoord
                )
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

    private fun isVoid(xPos: Int, zPos: Int): Boolean {
        if (mc.thePlayer.posY < 0.0) return true

        var off = 0
        while (off < mc.thePlayer.posY.toInt() + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(
                xPos.toDouble(),
                -off.toDouble(),
                zPos.toDouble()
            )
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }

    class Point(
        private val entity: EntityLivingBase,
        private val posOffset: Vec3,
        val valid: Boolean
    ) {
        val point: Vec3 = calculatePos()

        private fun calculatePos(): Vec3 {
            return entity.positionVector.add(posOffset)
        }
    }
}
