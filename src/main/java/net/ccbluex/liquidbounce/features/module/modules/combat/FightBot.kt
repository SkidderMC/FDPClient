/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.misc.Teams
import net.ccbluex.liquidbounce.features.module.modules.movement.Step
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.render.ColorManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils
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

@ModuleInfo(name = "FightBot", category = ModuleCategory.COMBAT)
object FightBot : Module() {

    private val pathRenderValue = BoolValue("PathRender", true)
    private val jumpResetValue = BoolValue("JumpReset", true)
    private val autoJumpValue = BoolValue("AutoJump", false)
    private val silentValue = BoolValue("Silent", false)
    private val blockMode = ListValue("blockMode", arrayOf("Skill", "Always", "Manual"), "Manual")
    private val findWay = ListValue("findWay", arrayOf("None", "Point", "Entity"), "Point")
    private val workReach = FloatValue("workReach", 10f, 1f, 50f)

    private var mainPos: FloatArray = floatArrayOf(0f, 0f, 0f)
    private var entity: EntityLivingBase? = null
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val witherTargets = mutableListOf<EntityLivingBase>()
    private var path = mutableListOf<Vec3>()
    private var backPath = mutableListOf<Vec3>()

    private var thread: Thread? = null
    private var backThread: Thread? = null
    override fun onEnable() {
        if (!autoJumpValue.get()) FDPClient.moduleManager[Step::class.java]!!.state = true
        if (!autoJumpValue.get()) FDPClient.moduleManager[Step::class.java]!!.modeValue.set("Jump")
        if (findWay.get().contains("Point")) mainPos =
            floatArrayOf(mc.thePlayer.posX.toFloat(), mc.thePlayer.posY.toFloat(), mc.thePlayer.posZ.toFloat())
    }

    override fun onDisable() {
        if (!autoJumpValue.get()) FDPClient.moduleManager[Step::class.java]!!.state = false
        thread?.stop()
        backThread?.stop()
        mc.gameSettings.keyBindForward.pressed = false
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        when (blockMode.get().lowercase()) {
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
        return if (witherTargets.size >= 1)
            witherTargets[0]
        else
            null
    }

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        if (jumpResetValue.get()) {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }
        }
        try {
            discoveredTargets.clear()
            witherTargets.clear()
            if (findWay.get().lowercase().contains("entity") && findWither() == null) {
                this.state = false
                FDPClient.hud.addNotification(
                    Notification(
                        "FightBot",
                        "Cant find wither",
                        NotifyType.WARNING,
                        4000,
                        500
                    )
                )
                return
            }
            val teams = FDPClient.moduleManager[Teams::class.java]!!
            for (entity in mc.theWorld.loadedEntityList) {
                if (entity is EntityLivingBase) {
                    if (entity != mc.thePlayer) {
                        if (entity is EntityWither) {
                            witherTargets.add(entity)
                        } else {
                            if (EntityUtils.isSelected(entity,true)) {
                                when (findWay.get().lowercase()) {
                                    "point" -> {
                                        if (getDistanceToPos(
                                                mainPos[0],
                                                mainPos[1],
                                                mainPos[2],
                                                entity
                                            ) < workReach.get() && !teams.isInYourTeam(entity)
                                        ) {
                                            discoveredTargets.add(entity)
                                        }
                                    }

                                    "none" -> {
                                        if (mc.thePlayer.getDistanceToEntity(entity) < workReach.get() && !teams.isInYourTeam(
                                                entity
                                            )
                                        ) {
                                            discoveredTargets.add(entity)
                                        }
                                    }

                                    "entity" -> {
                                        if (entity.getDistanceToEntity(findWither()) < workReach.get() && !teams.isInYourTeam(
                                                entity
                                            )
                                        ) {
                                            discoveredTargets.add(entity)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (discoveredTargets.size >= 1) {
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
                        )
                        return@thread
                    }
                }
                if (path.size<=1) {
                    strafeWithYaw(MovementUtils.defaultSpeed(), getRotation(entity))
                } else {
                    strafeWithYaw(
                        MovementUtils.defaultSpeed(), getRotationFromPos(
                            (path[0].xCoord.toFloat()+path[1].xCoord.toFloat())/2.0f,
                            (path[0].yCoord.toFloat()+path[1].yCoord.toFloat())/2.0f,
                            (path[0].zCoord.toFloat()+path[1].zCoord.toFloat())/2.0f
                        )
                    )
                }
                if (MovementUtils.hasMotion()) {
                    if (mc.thePlayer.onGround && autoJumpValue.get()) mc.thePlayer.jump()
                }
            } else {
                when (findWay.get().lowercase()) {
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
                                    )
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
                            if (MovementUtils.hasMotion()) {
                                if (mc.thePlayer.onGround && autoJumpValue.get()) mc.thePlayer.jump()
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
                                    )
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
                            if (MovementUtils.hasMotion()) {
                                if (mc.thePlayer.onGround && autoJumpValue.get()) mc.thePlayer.jump()
                            }
                        } else if (mc.gameSettings.keyBindForward.isKeyDown) mc.gameSettings.keyBindForward.pressed =
                            false
                    }
                }
                path = backPath
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
        if(silentValue.get()) {
            mc.thePlayer.isSprinting = true
            if (mc.thePlayer.onGround) {
                if (mc.gameSettings.keyBindForward.pressed) mc.gameSettings.keyBindForward.pressed = false
                RotationUtils.setTargetRotation(Rotation(yaw[0], yaw[1]), 10)
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

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        /*GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        mc.entityRenderer.disableLightmap()
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        for (point in path) {
            var lastPosX = 114514.0
            var lastPosY = 114514.0
            var lastPosZ = 114514.0
            GL11.glLineWidth(1f)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(Color(255,255,255).rgb)
            GL11.glVertex3d(point.xCoord - renderPosX, point.yCoord - renderPosY, point.zCoord - renderPosZ)
            GL11.glEnd()
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
        }
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glPopMatrix()*/
        synchronized(path) {
            if (path.isEmpty() || !pathRenderValue.get()) return
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
            var last: Vec3? =null;
            var last2: Vec3? =null;
            for (vec in path) {
                i += 100
                RenderUtils.glColor(ColorManager.astolfoRainbow(50, 10-(i/2), i))
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
                        //FontLoaders.C18.drawString("绘制次数: "+loops,20,20,Color(255,255,255).rgb)
                        /*var temp1 = (last.zCoord - y) / (last.xCoord - x);
                        var temp2 = (last2.zCoord - y) / (last2.xCoord - x);
                        var a = (temp1 - temp2) / (last.xCoord - last2.xCoord);
                        var b = temp1 - a * (x + last.xCoord);
                        var c = y - b * x - a * x * x;*/
                        //for(t in 1..3){
                        //    GL11.glVertex3d(((1 - t)*(1 - t)) * vec.xCoord + 2 * t * (1 - t) * last.xCoord + t * t * last2.xCoord, y, ((1 - t)*(1 - t)) * vec.zCoord + 2 * t * (1 - t) * last.zCoord + t * t * last2.zCoord)
                        //}

                    }
                    if(last != null){
                        last2=last
                    }
                    last = vec
                }catch (e:Exception){
                    e.printStackTrace()
                }
                GL11.glVertex3d(x, y, z)
                //mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
                /*GL11.glLineWidth(2F)
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glVertex3d(x - width, y, z - width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y, z + width)
                GL11.glVertex3d(x + width, y, z + width)
                GL11.glVertex3d(x + width, y, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x + width, y + height, z + width)
                GL11.glVertex3d(x + width, y + height, z - width)
                GL11.glEnd()
                GL11.glBegin(GL11.GL_LINE_STRIP)
                GL11.glVertex3d(x - width, y + height, z + width)
                GL11.glVertex3d(x - width, y + height, z - width)
                GL11.glEnd()*/
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
        if (!findWay.get().lowercase().contains("entity")) return
        if (findWither() == null) return
        val rad: Double = workReach.get().toDouble()
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
            RenderUtils.glColor(ColorManager.astolfoRainbow(5, 10, i))
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