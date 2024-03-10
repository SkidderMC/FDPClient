/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.BowModule
import net.ccbluex.liquidbounce.utils.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.*
import org.lwjgl.opengl.Display
import java.util.*
import kotlin.math.*

class RotationUtils : MinecraftInstance(), Listenable {
    /**
     * On tick.
     *
     * @param event the event
     */
    @EventTarget
    fun onTick(event: TickEvent?) {
        if (targetRotation != null) {
            keepLength--
            if (keepLength <= 0) reset()
        }
        if (random.nextGaussian() > 0.8) x = Math.random()
        if (random.nextGaussian() > 0.8) y = Math.random()
        if (random.nextGaussian() > 0.8) z = Math.random()
    }

    /**
     * On packet.
     *
     * @param event the event
     */
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            if (targetRotation != null && !keepCurrentRotation && (targetRotation?.yaw !== serverRotation?.yaw || targetRotation?.pitch !== serverRotation?.pitch)) {
                packet.yaw = targetRotation?.yaw!!
                packet.pitch = targetRotation?.pitch!!
                packet.rotating = true
            }

            if (packet.rotating) serverRotation = Rotation(packet.yaw, packet.pitch)
        }
    }

   /*
    fun onJump(event: JumpEvent) {
        if (FDPClient.moduleManager.getModule(BetterView::class.java)?.rotating!! && FDPClient.moduleManager.getModule(
                BetterView::class.java
            )?.state!! && FDPClient.moduleManager.getModule(BetterView::class.java)?.customStrafe?.get()!!
        )
            event.yaw = cameraYaw
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (FDPClient.moduleManager.getModule(BetterView::class.java)?.rotating!! && FDPClient.moduleManager.getModule(
                BetterView::class.java
            )?.state!! && FDPClient.moduleManager.getModule(BetterView::class.java)?.customStrafe?.get()!!
        )
            event.yaw = cameraYaw
    }

    */

    override fun handleEvents(): Boolean {
        return true
    }

    companion object {
        private val random = Random()

        /**
         * The constant targetRotation.
         */
        @JvmField
        var targetRotation: Rotation? = null

        /**
         * The constant serverRotation.
         */
        @JvmField
        var serverRotation: Rotation? = Rotation(30f, 30f)

        /**
         * The constant keepCurrentRotation.
         */
        var keepCurrentRotation = false
        private var keepLength = 0
        private var x = random.nextDouble()
        private var y = random.nextDouble()
        private var z = random.nextDouble()
        private var revTick = 0


        @JvmField
        var perspectiveToggled = false

        @JvmField
        var cameraYaw = 0f

        @JvmField
        var cameraPitch = 0f

        @JvmStatic
        fun overrideMouse(): Boolean {
            if (mc.inGameHasFocus && Display.isActive()) {
                if (!perspectiveToggled) {
                    return true
                }
                mc.mouseHelper.mouseXYChange()
                val f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f
                val f2 = f1 * f1 * f1 * 8.0f
                val f3 = mc.mouseHelper.deltaX * f2
                val f4 = mc.mouseHelper.deltaY * f2
                cameraYaw += f3 * 0.15f
                cameraPitch -= f4 * 0.15f
                if (cameraPitch > 90) cameraPitch = 90f
                if (cameraPitch < -90) cameraPitch = -90f
            }
            return false
        }

        fun enableLook() {
            if (!FDPClient.isStarting) {
                perspectiveToggled = true
                if (mc.thePlayer != null) {
                    cameraYaw = mc.thePlayer.rotationYaw
                    cameraPitch = mc.thePlayer.rotationPitch
                }
            }
        }

        fun disableLook() {
            if (!FDPClient.isStarting) {
                perspectiveToggled = false
                if (mc.thePlayer != null) {
                    mc.thePlayer.rotationYaw = cameraYaw
                    mc.thePlayer.rotationPitch = cameraPitch
                }
            }
        }


        fun calculateYawFromSrcToDst(yaw: Float, srcX: Double, srcZ: Double, dstX: Double, dstZ: Double): Float {
            val xDist = dstX - srcX
            val zDist = dstZ - srcZ
            val var1 = (StrictMath.atan2(zDist, xDist) * 180.0 / Math.PI).toFloat() - 90.0f
            return yaw + MathHelper.wrapAngleTo180_float(var1 - yaw)
        }
        /**
         * Other rotation rotation.
         *
         * @param bb           the bb
         * @param vec          the vec
         * @param predict      the predict
         * @param throughWalls the through walls
         * @param distance     the distance
         * @return the rotation
         */
        fun OtherRotation(
            bb: AxisAlignedBB,
            vec: Vec3,
            predict: Boolean,
            throughWalls: Boolean,
            distance: Float
        ): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            val eyes = mc.thePlayer.getPositionEyes(1f)
            var vecRotation: VecRotation? = null
            var xSearch = 0.15
            while (xSearch < 0.85) {
                var ySearch = 0.15
                while (ySearch < 1.0) {
                    var zSearch = 0.15
                    while (zSearch < 0.85) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch, bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)
                        val vecDist = eyes.distanceTo(vec3)
                        if (vecDist > distance) {
                            zSearch += 0.1
                            continue
                        }
                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)
                            if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                )
                            )
                                vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
            if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ),
                MathHelper.wrapAngleTo180_float(
                    (-Math.toDegrees(
                        atan2(
                            diffY,
                            sqrt(diffX * diffX + diffZ * diffZ)
                        )
                    )).toFloat()
                )
            )
        }

        /**
         * Face block vec rotation.
         *
         * @param blockPos the block pos
         * @return the vec rotation
         */
        fun faceBlock(blockPos: BlockPos?): VecRotation? {
            if (blockPos == null) return null
            var vecRotation: VecRotation? = null
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val eyesPos = Vec3(
                            mc.thePlayer.posX,
                            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                            mc.thePlayer.posZ
                        )
                        val posVec = Vec3(blockPos).addVector(xSearch, ySearch, zSearch)
                        val dist = eyesPos.distanceTo(posVec)
                        val diffX = posVec.xCoord - eyesPos.xCoord
                        val diffY = posVec.yCoord - eyesPos.yCoord
                        val diffZ = posVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float(-Math.toDegrees(atan2(diffY, diffXZ)).toFloat())
                        )
                        val rotationVector = getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                            rotationVector.zCoord * dist
                        )
                        val obj = mc.theWorld.rayTraceBlocks(
                            eyesPos, vector, false,
                            false, true
                        )
                        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            val currentVec = VecRotation(posVec, rotation)
                            if (vecRotation == null || getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                )
                            ) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
            return vecRotation
        }

        /**
         * @param target      the target
         * @param silent      the silent
         * @param predict     the predict
         * @param predictSize the predict size
         */
        fun faceLook(target: Entity, minTurnSpeed: Float, maxTurnSpeed: Float) {
            val player = mc.thePlayer
            val posX: Double =
                target.posX + 0.toDouble() - player.posX
            val posY: Double =
                target.entityBoundingBox.minY + target.eyeHeight - 0.15 - player.entityBoundingBox.minY - player.getEyeHeight()
            val posZ: Double =
                target.posZ + 0.toDouble() - player.posZ
            val posSqrt = sqrt(posX * posX + posZ * posZ)
            var velocity = 1f
            velocity = (velocity * velocity + velocity * 2) / 3
            if (velocity > 1) velocity = 1f
            val rotation = Rotation(
                (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90,
                -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt)))
                    .toFloat()
            )
            setTargetRotation(
                limitAngleChange(
                    serverRotation!!,
                    rotation,
                    RandomUtils.nextFloat(minTurnSpeed, maxTurnSpeed)
                )
            )
        }

        /**
         *
         * @param entity
         * @return
         */
        fun getRotationsNonLivingEntity(entity: Entity): Rotation {
            return getRotations(
                entity.posX,
                entity.posY + (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 0.5,
                entity.posZ
            )
        }

        /**
         * Face bow.
         *
         * @param target      the target
         * @param silent      the silent
         * @param predict     the predict
         * @param predictSize the predict size
         */
        fun faceBow(target: Entity, silent: Boolean, predict: Boolean, predictSize: Float) {
            val player = mc.thePlayer
            val posX: Double =
                target.posX + (if (predict) (target.posX - target.prevPosX) * predictSize else 0.toDouble()) - (player.posX + if (predict) player.posX - player.prevPosX else 0.toDouble())
            val posY: Double =
                target.entityBoundingBox.minY + (if (predict) (target.entityBoundingBox.minY - target.prevPosY) * predictSize else 0.toDouble()) + target.eyeHeight - 0.15 - (player.entityBoundingBox.minY + if (predict) player.posY - player.prevPosY else 0.toDouble()) - player.getEyeHeight()
            val posZ: Double =
                target.posZ + (if (predict) (target.posZ - target.prevPosZ) * predictSize else 0.toDouble()) - (player.posZ + if (predict) player.posZ - player.prevPosZ else 0.toDouble())
            val posSqrt = sqrt(posX * posX + posZ * posZ)
            var velocity =
                if (FDPClient.moduleManager.getModule(BowModule::class.java)!!.state) 1f else player.itemInUseDuration / 20f
            velocity = (velocity * velocity + velocity * 2) / 3
            if (velocity > 1) velocity = 1f
            val rotation = Rotation(
                (atan2(posZ, posX) * 180 / Math.PI).toFloat() - 90,
                -Math.toDegrees(atan((velocity * velocity - sqrt(velocity * velocity * velocity * velocity - 0.006f * (0.006f * (posSqrt * posSqrt) + 2 * posY * (velocity * velocity)))) / (0.006f * posSqrt)))
                    .toFloat()
            )
            if (silent) setTargetRotation(rotation) else limitAngleChange(
                Rotation(player.rotationYaw, player.rotationPitch), rotation, (10 +
                        Random().nextInt(6)).toFloat()
            ).toPlayer(mc.thePlayer)
        }

        /**
         * To rotation rotation.
         *
         * @param vec     the vec
         * @param predict the predict
         * @return the rotation
         */
        fun toRotation(vec: Vec3, predict: Boolean): Rotation {
            val eyesPos = Vec3(
                mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                        mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ
            )
            if (predict) eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ)
            val diffX = vec.xCoord - eyesPos.xCoord
            val diffY = vec.yCoord - eyesPos.yCoord
            val diffZ = vec.zCoord - eyesPos.zCoord
            return Rotation(
                MathHelper.wrapAngleTo180_float(
                    Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
                ),
                MathHelper.wrapAngleTo180_float(
                    (-Math.toDegrees(
                        atan2(
                            diffY,
                            sqrt(diffX * diffX + diffZ * diffZ)
                        )
                    )).toFloat()
                )
            )
        }

        fun calculateCenter(
            calMode: String?,
            randMode: String,
            randomRange: Double,
            bb: AxisAlignedBB,
            predict: Boolean,
            throughWalls: Boolean
        ): VecRotation? {
            //final Rotation randomRotation = toRotation(randomVec, predict);

            var vecRotation: VecRotation? = null

            var xMin: Double
            var yMin: Double
            var zMin: Double
            var xMax: Double
            var yMax: Double
            var zMax: Double
            var xDist: Double
            var yDist: Double
            var zDist: Double

            xMin = 0.15
            xMax = 0.85
            xDist = 0.1
            yMin = 0.15
            yMax = 1.00
            yDist = 0.1
            zMin = 0.15
            zMax = 0.85
            zDist = 0.1

            var curVec3: Vec3? = null

            when (calMode) {
                "LiquidBounce" -> {}
                "Full" -> {
                    xMin = 0.00
                    xMax = 1.00
                    yMin = 0.00
                    zMin = 0.00
                    zMax = 1.00
                }

                "HalfUp" -> {
                    xMin = 0.10
                    xMax = 0.90
                    yMin = 0.50
                    yMax = 0.90
                    zMin = 0.10
                    zMax = 0.90
                }

                "HalfDown" -> {
                    xMin = 0.10
                    xMax = 0.90
                    yMin = 0.10
                    yMax = 0.50
                    zMin = 0.10
                    zMax = 0.90
                }

                "CenterSimple" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.65
                    yMax = 0.75
                    yDist = 0.0125
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }

                "CenterLine" -> {
                    xMin = 0.45
                    xMax = 0.55
                    xDist = 0.0125
                    yMin = 0.10
                    yMax = 0.90
                    zMin = 0.45
                    zMax = 0.55
                    zDist = 0.0125
                }
                "CenterDot" -> {
                    xMin = 0.48
                    xMax = 0.52
                    xDist = 0.005
                    yMin = 0.58
                    yMax = 0.62
                    yDist = 0.005
                    zMin = 0.48
                    zMax = 0.52
                    zDist = 0.005
                }
                "MidRange" -> {
                    xMin = 0.20
                    xMax = 0.80
                    xDist = 0.1
                    yMin = 0.40
                    yMax = 0.60
                    yDist = 0.0125
                    zMin = 0.20
                    zMax = 0.80
                    zDist = 0.1
                }
                "HeadRange" -> {
                    xMin = 0.20
                    xMax = 0.80
                    xDist = 0.1
                    yMin = 0.55
                    yMax = 0.80
                    yDist = 0.0125
                    zMin = 0.20
                    zMax = 0.80
                    zDist = 0.1
                }
                "Optimal" -> {

                }

            }
            var xSearch = xMin
            while (xSearch < xMax) {
                var ySearch = yMin
                while (ySearch < yMax) {
                    var zSearch = zMin
                    while (zSearch < zMax) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch,
                            bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (getRotationDifference(currentVec.rotation) < getRotationDifference(
                                    vecRotation.rotation
                                ))
                            ) {
                                vecRotation = currentVec
                                curVec3 = vec3
                            }
                        }
                        zSearch += zDist
                    }
                    ySearch += yDist
                }
                xSearch += xDist
            }

            if (calMode === "Optimal") {
                val vec3 = Vec3(
                    max(bb.minX, min(mc.thePlayer.posX, bb.maxX)), max(bb.minY, min(mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), bb.maxY)), max(
                        bb.minZ, min(
                            mc.thePlayer.posZ, bb.maxZ
                        )
                    )
                )
                val rotation = toRotation(vec3, predict)
                vecRotation = VecRotation(vec3, rotation)
            }

            if (vecRotation == null || randMode == "Off") return vecRotation

            var rand1 = random.nextDouble()
            var rand2 = random.nextDouble()
            var rand3 = random.nextDouble()

            val xRange = bb.maxX - bb.minX
            val yRange = bb.maxY - bb.minY
            val zRange = bb.maxZ - bb.minZ
            var minRange = 999999.0

            if (xRange <= minRange) minRange = xRange
            if (yRange <= minRange) minRange = yRange
            if (zRange <= minRange) minRange = zRange

            rand1 = rand1 * minRange * randomRange
            rand2 = rand2 * minRange * randomRange
            rand3 = rand3 * minRange * randomRange

            val xPrecent = minRange * randomRange / xRange
            val yPrecent = minRange * randomRange / yRange
            val zPrecent = minRange * randomRange / zRange

            var randomVec3 = Vec3(
                curVec3!!.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
            )
            when (randMode) {
                "Horizonal" -> randomVec3 = Vec3(
                    curVec3.xCoord - xPrecent * (curVec3.xCoord - bb.minX) + rand1,
                    curVec3.yCoord,
                    curVec3.zCoord - zPrecent * (curVec3.zCoord - bb.minZ) + rand3
                )

                "Vertical" -> randomVec3 = Vec3(
                    curVec3.xCoord,
                    curVec3.yCoord - yPrecent * (curVec3.yCoord - bb.minY) + rand2,
                    curVec3.zCoord
                )
            }
            val randomRotation = toRotation(randomVec3, predict)

            vecRotation = VecRotation(randomVec3, randomRotation)

            return vecRotation
        }


        /**
         * Gets center.
         *
         * @param bb the bb
         * @return the center
         */
        fun getCenter(bb: AxisAlignedBB): Vec3 {
            return Vec3(
                bb.minX + (bb.maxX - bb.minX) * 0.5,
                bb.minY + (bb.maxY - bb.minY) * 0.5,
                bb.minZ + (bb.maxZ - bb.minZ) * 0.5
            )
        }

        /**
         * Search good center
         *
         * @param bb enemy box
         * @param outborder outborder option
         * @param random random option
         * @param predict predict option
         * @param throughWalls throughWalls option
         * @return center
         */
        //TODO : searchCenter Big Update lol(Better Center calculate method & Jitter Support(Better Random Center)) / Co丶Dynamic : Wait until Mid-Autumn Festival
        fun searchCenter(
            bb: AxisAlignedBB,
            outborder: Boolean,
            random: Boolean,
            predict: Boolean,
            throughWalls: Boolean
        ): VecRotation? {
            if (outborder) {
                val vec3 = Vec3(
                    bb.minX + (bb.maxX - bb.minX) * (x * 0.3 + 1.0),
                    bb.minY + (bb.maxY - bb.minY) * (y * 0.3 + 1.0),
                    bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.3 + 1.0)
                )
                return VecRotation(vec3, toRotation(vec3, predict))
            }

            val randomVec = Vec3(
                bb.minX + (bb.maxX - bb.minX) * (x * 0.8 + 0.2),
                bb.minY + (bb.maxY - bb.minY) * (y * 0.8 + 0.2),
                bb.minZ + (bb.maxZ - bb.minZ) * (z * 0.8 + 0.2)
            )
            val randomRotation = toRotation(randomVec, predict)

            var vecRotation: VecRotation? = null

            var xSearch = 0.15
            while (xSearch < 0.85) {
                var ySearch = 0.15
                while (ySearch < 1.0) {
                    var zSearch = 0.15
                    while (zSearch < 0.85) {
                        val vec3 = Vec3(
                            bb.minX + (bb.maxX - bb.minX) * xSearch,
                            bb.minY + (bb.maxY - bb.minY) * ySearch,
                            bb.minZ + (bb.maxZ - bb.minZ) * zSearch
                        )
                        val rotation = toRotation(vec3, predict)

                        if (throughWalls || isVisible(vec3)) {
                            val currentVec = VecRotation(vec3, rotation)

                            if (vecRotation == null || (if (random) getRotationDifference(
                                    currentVec.rotation,
                                    randomRotation
                                ) < getRotationDifference(
                                    vecRotation.rotation,
                                    randomRotation
                                ) else getRotationDifference(currentVec.rotation) < getRotationDifference(vecRotation.rotation))
                            ) vecRotation = currentVec
                        }
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }

            return vecRotation
        }


        /**
         * Round rotation float.
         *
         * @param yaw      the yaw
         * @param strength the strength
         * @return the float
         */
        fun roundRotation(yaw: Float, strength: Int): Float {
            return ((yaw / strength).roundToInt() * strength).toFloat()
        }

        /**
         * Gets rotation difference.
         *
         * @param entity the entity
         * @return the rotation difference
         */
        fun getRotationDifference(entity: Entity): Double {
            val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
            return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
        }

        /**
         * Gets rotation back difference.
         *
         * @param entity the entity
         * @return the rotation back difference
         */
        fun getRotationBackDifference(entity: Entity): Double {
            val rotation = toRotation(getCenter(entity.entityBoundingBox), true)
            return getRotationDifference(rotation, Rotation(mc.thePlayer.rotationYaw - 180, mc.thePlayer.rotationPitch))
        }

        /**
         * Gets rotation difference.
         *
         * @param a the a
         * @param b the b
         * @return the rotation difference
         */
        fun getRotationDifference(a: Rotation, b: Rotation?): Double {
            return hypot(getAngleDifference(a.yaw, b!!.yaw).toDouble(), (a.pitch - b.pitch).toDouble())
        }

        /**
         * Calculate difference between the server rotation and your rotation
         *
         * @param rotation your rotation
         * @return difference between rotation
         */
        fun getRotationDifference(rotation: Rotation?): Double {
            return if (serverRotation == null) 0.0 else getRotationDifference(rotation!!, serverRotation)
        }


        /**
         * Limit angle change rotation.
         *
         * @param currentRotation the current rotation
         * @param targetRotation  the target rotation
         * @param turnSpeed       the turn speed
         * @return the rotation
         */
        fun limitAngleChange(currentRotation: Rotation, targetRotation: Rotation, turnSpeed: Float): Rotation {
            val yawDifference = getAngleDifference(targetRotation.yaw, currentRotation.yaw)
            val pitchDifference = getAngleDifference(targetRotation.pitch, currentRotation.pitch)
            return Rotation(
                currentRotation.yaw + if (yawDifference > turnSpeed) turnSpeed else yawDifference.coerceAtLeast(-turnSpeed),
                currentRotation.pitch + if (pitchDifference > turnSpeed) turnSpeed else pitchDifference.coerceAtLeast(-turnSpeed)
            )
        }

        @JvmStatic
        fun getAngleDifference(a: Float, b: Float): Float {
            return ((a - b) % 360f + 540f) % 360f - 180f
        }

        /**
         * Gets vector for rotation.
         *
         * @param rotation the rotation
         * @return the vector for rotation
         */
        fun getVectorForRotation(rotation: Rotation): Vec3 {
            val yawCos = MathHelper.cos(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
            val yawSin = MathHelper.sin(-rotation.yaw * 0.017453292f - Math.PI.toFloat())
            val pitchCos = -MathHelper.cos(-rotation.pitch * 0.017453292f)
            val pitchSin = MathHelper.sin(-rotation.pitch * 0.017453292f)
            return Vec3((yawSin * pitchCos).toDouble(), pitchSin.toDouble(), (yawCos * pitchCos).toDouble())
        }

        /**
         * Allows you to check if your crosshair is over your target entity
         *
         * @param targetEntity your target entity
         * @param blockReachDistance your reach
         * @return if crosshair is over target
         */
        fun isFaced(targetEntity: Entity, blockReachDistance: Double): Boolean {
            return raycastEntity(blockReachDistance) { entity: Entity -> entity === targetEntity } != null
        }

        /**
         * Is visible boolean.
         *
         * @param vec3 the vec 3
         * @return the boolean
         */
        private fun isVisible(vec3: Vec3?): Boolean {
            val eyesPos = Vec3(
                mc.thePlayer.posX,
                mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ
            )
            return mc.theWorld.rayTraceBlocks(eyesPos, vec3) == null
        }

        /**
         * Sets target rotation.
         *
         * @param rotation the rotation
         */
        fun setTargetRotation(rotation: Rotation) {
            setTargetRotation(rotation, 0)
        }

        /**
         * Sets target rotation.
         *
         * @param rotation   the rotation
         * @param keepLength the keep length
         */
        fun setTargetRotation(rotation: Rotation, keepLength: Int) {
            try {
                if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return
            } catch (ignored: Exception) {
                return
            }
            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            Companion.keepLength = keepLength
        }


        fun setTargetRotationReverse(rotation: Rotation, kl: Int, rt: Int) {
            if (java.lang.Double.isNaN(rotation.yaw.toDouble()) || java.lang.Double.isNaN(rotation.pitch.toDouble()) || rotation.pitch > 90 || rotation.pitch < -90) return

            rotation.fixedSensitivity(mc.gameSettings.mouseSensitivity)
            targetRotation = rotation
            keepLength = kl
            revTick = rt + 1
        }

        /**
         * Reset.
         */
        fun reset() {
            keepLength = 0
            targetRotation = null
        }

        /**
         * Gets rotations entity.
         *
         * @param entity the entity
         * @return the rotations entity
         */
        fun getRotationsEntity(entity: EntityLivingBase): Rotation {
            return getRotations(entity.posX, entity.posY + entity.eyeHeight - 0.4, entity.posZ)
        }

        /**
         * Gets rotations.
         *
         * @param posX the pos x
         * @param posY the pos y
         * @param posZ the pos z
         * @return the rotations
         */
        fun getRotations(posX: Double, posY: Double, posZ: Double): Rotation {
            val player = mc.thePlayer
            val x = posX - player.posX
            val y = posY - (player.posY + player.getEyeHeight().toDouble())
            val z = posZ - player.posZ
            val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
            val yaw = (atan2(z, x) * 180.0 / 3.141592653589793).toFloat() - 90.0f
            val pitch = (-(atan2(y, dist) * 180.0 / 3.141592653589793)).toFloat()
            return Rotation(yaw, pitch)
        }

        private fun calculate(from: Vec3?, to: Vec3): Rotation {
            val diff = to.subtract(from)
            val distance = hypot(diff.xCoord, diff.zCoord)
            val yaw = (MathHelper.atan2(diff.zCoord, diff.xCoord) * (180f / Math.PI)).toFloat() - 90.0f
            val pitch = (-(MathHelper.atan2(diff.yCoord, distance) * (180f / Math.PI))).toFloat()
            return Rotation(yaw, pitch)
        }

        fun calculate(to: Vec3): Rotation {
            return calculate(
                mc.thePlayer.positionVector.add(Vec3(0.0, mc.thePlayer.getEyeHeight().toDouble(), 0.0)),
                Vec3(to.xCoord, to.yCoord, to.zCoord)
            )
        }

        /**
         * Gets rotations.
         *
         * @param ent the ent
         * @return the rotations
         */
        fun getRotations(ent: Entity): Rotation {
            val x = ent.posX
            val z = ent.posZ
            val y = ent.posY + (ent.eyeHeight / 2.0f).toDouble()
            return getRotationFromPosition(x, z, y)
        }

        /**
         * Get rotations 1 float [ ].
         *
         * @param posX the pos x
         * @param posY the pos y
         * @param posZ the pos z
         * @return the float [ ]
         */
        fun getRotations1(posX: Double, posY: Double, posZ: Double): FloatArray {
            val player = mc.thePlayer
            val x = posX - player.posX
            val y = posY - (player.posY + player.getEyeHeight().toDouble())
            val z = posZ - player.posZ
            val dist = MathHelper.sqrt_double(x * x + z * z).toDouble()
            val yaw = (atan2(z, x) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = -(atan2(y, dist) * 180.0 / Math.PI).toFloat()
            return floatArrayOf(yaw, pitch)
        }

        /**
         * Gets rotation from position.
         *
         * @param x the x
         * @param z the z
         * @param y the y
         * @return the rotation from position
         */
        fun getRotationFromPosition(x: Double, z: Double, y: Double): Rotation {
            val xDiff = x - mc.thePlayer.posX
            val zDiff = z - mc.thePlayer.posZ
            val yDiff = y - mc.thePlayer.posY - 1.2
            val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
            val yaw = (atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90.0f
            val pitch = (-atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
            return Rotation(yaw, pitch)
        }
    }
}