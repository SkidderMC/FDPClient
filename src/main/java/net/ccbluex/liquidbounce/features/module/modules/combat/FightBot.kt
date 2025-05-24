/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.ClientThemesUtils.getColor
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.pathfinding.PathUtils
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import kotlin.concurrent.thread
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object FightBot : Module("FightBot", Category.COMBAT) {

    private val pathRenderValue by boolean("PathRender", true)
    private val jumpResetValue by boolean("JumpReset", true)
    private val autoJumpValue by boolean("AutoJump", false)
    private val silentValue by boolean("Silent", false)
    private val blockMode by choices("blockMode", arrayOf("Skill", "Always", "Manual"), "Manual")
    private val findWay by choices("findWay", arrayOf("None", "Point", "Entity"), "Point")
    private val workReach by float("workReach", 10f, 1f..50f)

    private val mainPos: FloatArray = floatArrayOf(0f, 0f, 0f)
    private var entity: EntityLivingBase? = null
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val witherTargets = mutableListOf<EntityLivingBase>()
    private var path = mutableListOf<Vec3>()
    private var backPath = mutableListOf<Vec3>()

    private var thread: Thread? = null
    private var backThread: Thread? = null
    override fun onEnable() {
        if (!autoJumpValue) {
            Step.state = true
        }
        if (findWay.contains("Point")) {
            mainPos[0] = mc.thePlayer.posX.toFloat()
            mainPos[1] = mc.thePlayer.posY.toFloat()
            mainPos[2] = mc.thePlayer.posZ.toFloat()
        }
    }

    override fun onDisable() {
        if (!autoJumpValue) Step.state = false
        thread?.interrupt()
        backThread?.interrupt()
        mc.gameSettings.keyBindForward.pressed = false
    }

    val onAttack = handler<AttackEvent> {
        when (blockMode.lowercase()) {
            "skill" -> {
                if (mc.thePlayer.experienceLevel >= 100 && entity?.getDistanceToEntity(mc.thePlayer)!! < 3.5f)
                    mc.gameSettings.keyBindUseItem.pressed = true
            }

            "always" -> {
                mc.gameSettings.keyBindUseItem.pressed = entity?.getDistanceToEntity(mc.thePlayer)!! < 3.5f
            }
        }
    }

    private fun findWither(): EntityLivingBase? {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase) {
                if (entity != mc.thePlayer) {
                    if (entity is EntityWither) {
                        witherTargets.add(entity)
                    }
                }
            }
        }
        witherTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
        return witherTargets.firstOrNull()
    }

    val onUpdate = handler<UpdateEvent> {
        if (jumpResetValue) {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
        }
        try {
            discoveredTargets.clear()
            witherTargets.clear()
            if (findWay.lowercase().contains("entity") && findWither() == null) {
                this.state = false
                FDPClient.hud.addNotification(
                    Notification(
                        "FightBot",
                        "Cant find wither",
                        Type.WARNING,
                        4000,
                        500
                    )
                )
                return@handler
            }

            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase && entity != mc.thePlayer) {
                    if (entity is EntityWither) {
                        witherTargets.add(entity)
                    } else {
                        if (EntityUtils.isSelected(entity, true)) {
                            when (findWay.lowercase()) {
                                "point" -> {
                                    if (getDistanceToPos(
                                            mainPos[0],
                                            mainPos[1],
                                            mainPos[2],
                                            entity
                                        ) < workReach && !Teams.isInYourTeam(entity)) {
                                        discoveredTargets.add(entity)
                                    }
                                }

                                "none" -> {
                                    if (mc.thePlayer.getDistanceToEntity(entity) < workReach && !Teams.isInYourTeam(entity)) {
                                        discoveredTargets.add(entity)
                                    }
                                }

                                "entity" -> {
                                    if (entity.getDistanceToEntity(findWither()) < workReach && !Teams.isInYourTeam(entity)) {
                                        discoveredTargets.add(entity)
                                    }
                                }
                            }
                        }
                    }
                }
                if (discoveredTargets.isNotEmpty()) {
                    discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
                    witherTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) }
                    val entity = discoveredTargets[0]

                    if (thread?.isAlive != true) {
                        thread = thread(name = "FightBot-Find") {
                            path = PathUtils.findBlinkPath(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ,
                                entity.posX,
                                entity.posY,
                                entity.posZ,
                                2.5
                            ).toMutableList()
                            return@thread
                        }
                    }
                    if (path.size <= 1) {
                        strafeWithYaw(MovementUtils.defaultSpeed(), getRotation(entity))
                    } else {
                        strafeWithYaw(
                            MovementUtils.defaultSpeed(), getRotationFromPos(
                                (path[0].xCoord.toFloat() + path[1].xCoord.toFloat()) / 2.0f,
                                (path[0].yCoord.toFloat() + path[1].yCoord.toFloat()) / 2.0f,
                                (path[0].zCoord.toFloat() + path[1].zCoord.toFloat()) / 2.0f
                            )
                        )
                    }
                    if (MovementUtils.hasTheMotion()) {
                        if (mc.thePlayer.onGround && autoJumpValue) mc.thePlayer.jump()
                    }
                } else {
                    when (findWay.lowercase()) {
                        "point" -> {
                            if (getDistanceToPos(mainPos[0], mainPos[1], mainPos[2], mc.thePlayer) > 2) {
                                if (backThread?.isAlive != true) {
                                    backThread = thread(name = "FightBot-Back") {
                                        backPath = PathUtils.findBlinkPath(
                                            mc.thePlayer.posX,
                                            mc.thePlayer.posY,
                                            mc.thePlayer.posZ,
                                            mainPos[0].toDouble(), mainPos[1].toDouble(), mainPos[2].toDouble(),
                                            0.1
                                        ).toMutableList()
                                        return@thread
                                    }
                                }
                                if (backPath.isEmpty()) {
                                    strafeWithYaw(
                                        MovementUtils.defaultSpeed(),
                                        getRotationFromPos(mainPos[0], mainPos[1], mainPos[2])
                                    )
                                } else {
                                    strafeWithYaw(
                                        MovementUtils.defaultSpeed(), getRotationFromPos(
                                            backPath[0].xCoord.toFloat(),
                                            backPath[0].yCoord.toFloat(),
                                            backPath[0].zCoord.toFloat()
                                        )
                                    )
                                }
                                if (MovementUtils.hasTheMotion()) {
                                    if (mc.thePlayer.onGround && autoJumpValue) mc.thePlayer.jump()
                                }
                            } else if (mc.gameSettings.keyBindForward.isKeyDown) mc.gameSettings.keyBindForward.pressed =
                                false
                        }

                        "entity" -> {
                            val entity: EntityLivingBase = findWither()!!
                            if (mc.thePlayer.getDistanceToEntity(entity) > 2) {
                                if (backThread?.isAlive != true) {
                                    backThread = thread(name = "FightBot") {
                                        backPath = PathUtils.findBlinkPath(
                                            mc.thePlayer.posX,
                                            mc.thePlayer.posY,
                                            mc.thePlayer.posZ,
                                            entity.posX,
                                            entity.posY,
                                            entity.posZ,
                                            2.5
                                        ).toMutableList()
                                        return@thread
                                    }
                                }
                                if (backPath.isEmpty()) {
                                    strafeWithYaw(MovementUtils.defaultSpeed(), getRotation(entity))
                                } else {
                                    strafeWithYaw(
                                        MovementUtils.defaultSpeed(), getRotationFromPos(
                                            backPath[0].xCoord.toFloat(),
                                            backPath[0].yCoord.toFloat(),
                                            backPath[0].zCoord.toFloat()
                                        )
                                    )
                                }
                                if (MovementUtils.hasTheMotion()) {
                                    if (mc.thePlayer.onGround && autoJumpValue) mc.thePlayer.jump()
                                }
                            } else if (mc.gameSettings.keyBindForward.isKeyDown) mc.gameSettings.keyBindForward.pressed =
                                false
                        }
                    }
                    path = backPath
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDistanceToPos(posX: Float, posY: Float, posZ: Float, entity: EntityLivingBase): Float {
        val f: Float = (posX - entity.posX).toFloat()
        val f1: Float = (posY - entity.posY).toFloat()
        val f2: Float = (posZ - entity.posZ).toFloat()
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2)
    }

    private fun getRotation(entity: EntityLivingBase): FloatArray {
        val diffX = entity.posX - mc.thePlayer.posX
        val diffZ = entity.posZ - mc.thePlayer.posZ
        val diffY =
            entity.posY + entity.eyeHeight.toDouble() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight().toDouble())
        val dist = MathHelper.sqrt_double((diffX * diffX + diffZ * diffZ)).toDouble()
        val yaw = (atan2(diffZ, diffX) * 180 / 3.141592653589).toFloat() - 90.0f
        val pitch = (-(atan2(diffY, dist) * 180 / 3.141592653589)).toFloat()
        return floatArrayOf(yaw, pitch)
    }

    private fun getRotationFromPos(posX: Float, posY: Float, posZ: Float): FloatArray {
        val diffX = posX - mc.thePlayer.posX
        val diffZ = posZ - mc.thePlayer.posZ
        val diffY = posY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight().toDouble())
        val dist = MathHelper.sqrt_double((diffX * diffX + diffZ * diffZ)).toDouble()
        val yaw = (atan2(diffZ, diffX) * 180 / 3.141592653589).toFloat() - 90.0f
        val pitch = (-(atan2(diffY, dist) * 180 / 3.141592653589)).toFloat()
        return floatArrayOf(yaw, pitch)
    }

    private fun strafeWithYaw(speed: Double, yaw: FloatArray) {
        if(silentValue) {
            mc.thePlayer.isSprinting = true
            if (mc.thePlayer.onGround) {
                if (mc.gameSettings.keyBindForward.pressed) mc.gameSettings.keyBindForward.pressed = false
                RotationUtils.setTargetRotation(Rotation(yaw[0], yaw[1]), RotationSettings(this))
                val strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
                val cos = cos(Math.toRadians((yaw[0] + 90.0f).toDouble()))
                val sin = sin(Math.toRadians((yaw[0] + 90.0f).toDouble()))
                mc.thePlayer.motionX = (speed * cos + strafe * speed * sin)
                mc.thePlayer.motionZ = (speed * sin - strafe * speed * cos)
            } else {
                mc.thePlayer.rotationYaw = yaw[0]
                mc.gameSettings.keyBindForward.pressed = true
            }
        }else{
            mc.thePlayer.rotationYaw = yaw[0]
            mc.gameSettings.keyBindForward.pressed = true
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        synchronized(path) {
            if (path.isEmpty() || !pathRenderValue) return@handler
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            GL11.glPushMatrix()
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LIGHTING)
            GL11.glDepthMask(false)

            var i = 0
            GL11.glLineWidth(2F)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            var last: Vec3? =null
            var last2: Vec3? =null
            for (vec in path) {
                i += 100
                val themeColor = getColor(1).rgb
                RenderUtils.glColor(themeColor)
                val x = vec.xCoord - renderPosX
                val y = vec.yCoord - renderPosY
                val z = vec.zCoord - renderPosZ
                val width = 0.3
                val height = 0.3
                try {
                    if (last != null && last2 != null) {
                        var loops =
                            sqrt((vec.xCoord - last.xCoord) * (vec.xCoord - last.xCoord) + ((vec.zCoord - last.zCoord) * (vec.zCoord - last.zCoord)))
                        loops += sqrt((last2.xCoord - last.xCoord) * (last2.xCoord - last.xCoord) + ((last2.zCoord - last.zCoord) * (last2.zCoord - last.zCoord)))

                    }
                    if(last != null){
                        last2=last
                    }
                    last = vec
                }catch (e:Exception){
                    e.printStackTrace()
                }
                GL11.glVertex3d(x, y, z)
            }
            GL11.glEnd()
            GL11.glDepthMask(true)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glPopMatrix()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }
        if (!findWay.lowercase().contains("entity")) return@handler
        if (findWither() == null) return@handler
        val rad: Double = workReach.toDouble()
        val partialTicks: Float = event.partialTicks
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glDisable(3553)
        GL11.glDisable(2929)
        GL11.glDisable(2929)
        GL11.glDepthMask(false)
        GL11.glLineWidth(1.0f)
        GL11.glBegin(3)
        val x: Double = (findWither()!!.lastTickPosX
                + (findWither()!!.posX - findWither()!!.lastTickPosX) * partialTicks
                - mc.renderManager.viewerPosX)
        val y: Double = (findWither()!!.lastTickPosY
                + (findWither()!!.posY - findWither()!!.lastTickPosY) * partialTicks
                - mc.renderManager.viewerPosY)
        val z: Double = (findWither()!!.lastTickPosZ
                + (findWither()!!.posZ - findWither()!!.lastTickPosZ) * partialTicks
                - mc.renderManager.viewerPosZ)
        val pix2 = 3.1415926
        for (i in 0..20) {
            val themeColor = getColor(1).rgb
            RenderUtils.glColor(themeColor)
            GL11.glVertex3d(
                x + rad * cos(i * pix2 / 9.0), y,
                z + rad * sin(i * pix2 / 9.0)
            )
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        RenderUtils.stopDrawing()
        GL11.glEnable(3553)
        GL11.glPopMatrix()
    }
}