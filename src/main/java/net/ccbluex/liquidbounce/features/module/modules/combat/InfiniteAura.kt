/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.event.async.loopSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.pathfinding.PathUtils
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.*
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomClickDelay
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.PlayerCapabilities
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C13PacketPlayerAbilities
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt
import kotlin.math.sqrt

object InfiniteAura : Module(name = "InfiniteAura", category = Category.COMBAT, subCategory = Category.SubCategory.COMBAT_RAGE, spacedName = "Infinite Aura") {

    private val packetValue by choices("PacketMode", arrayOf("PacketPosition", "PacketPosLook"), "PacketPosition")
    private val packetBack by boolean("DoTeleportBackPacket", false)

    private val modeValue by choices("Mode", arrayOf("Aura", "Click"), "Aura")

    private val priority by choices(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier",
            "OnLadder",
            "InLiquid",
            "InWeb"
        ), "Distance"
    )

    private val targetMode by choices("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val limitedMultiTargets by int("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }
    private val maxSwitchFOV by float("MaxSwitchFOV", 90f, 30f..180f) { targetMode == "Switch" }
    private val switchDelay by int("SwitchDelay", 15, 1..1000) { targetMode == "Switch" }

    private val targetsValue by int("Targets", 3, 1..10) { modeValue == "Aura" }

    private val cps by intRange("CPS", 5..8, 1..50).onChanged {
        attackDelay = randomClickDelay(it.first, it.last)
    }

    private val hurtTime by int("HurtTime", 10, 0..10)

    private val distValue by int("Distance", 30, 20..100)
    private val moveDistanceValue by float("MoveDistance", 5f, 2f..15f)

    private val fov by float("FOV", 180f, 0f..180f)

    private val swing by boolean("Swing", true)
    private val swingValue by boolean("SwingAura", true) { modeValue == "Aura" }

    private val failSwing by boolean("FailSwing", true) { swing }
    private val maxRotationDifferenceToSwing by float("MaxRotationDifferenceToSwing", 180f, 0f..180f) { failSwing }
    private val renderBoxOnSwingFail by boolean("RenderBoxOnSwingFail", false) { failSwing }
    private val renderBoxColor = ColorSettingsInteger(this, "RenderBoxColor") { renderBoxOnSwingFail }.with(Color.CYAN)
    private val renderBoxFadeSeconds by float("RenderBoxFadeSeconds", 1f, 0f..5f) { renderBoxOnSwingFail }

    private val noRegenValue by boolean("NoRegen", true)
    private val noLagBackValue by boolean("NoLagback", true)

    private val pathRenderValue by boolean("PathRender", true)
    private val renderPathColor by color("PathColor", Color.GREEN) { pathRenderValue }.subjective()

    private val options = RotationSettings(this).withoutKeepRotation()
    private val randomization = RandomizationSettings(this) { options.rotationsActive }

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) { options.rotationsActive }
    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) { options.rotationsActive }
    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange("HorizontalBodySearchRange", 0f..1f, 0f..1f) { options.rotationsActive }

    private val predictClientMovement by int("PredictClientMovement", 2, 0..5)
    private val predictOnlyWhenOutOfRange by boolean("PredictOnlyWhenOutOfRange", false) { predictClientMovement != 0 }
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, -1f..2f)

    private val renderAimPointBox by boolean("RenderAimPointBox", false).subjective()
    private val aimPointBoxColor by color("AimPointBoxColor", Color.CYAN) { renderAimPointBox }.subjective()
    private val aimPointBoxSize by float("AimPointBoxSize", 0.1f, 0f..0.2F) { renderAimPointBox }.subjective()

    private val displayDebug by boolean("Debug", false)

    var target: EntityLivingBase? = null
    private var hittable = false
    private val prevTargetEntities = mutableListOf<Int>()

    private val points = mutableListOf<Vec3>()
    private val coroutineContext = Dispatchers.Default + CoroutineName("InfiniteAura-PathFinder")

    private val attackTimer = MSTimer()
    private var attackDelay = 0
    private var clicks = 0

    private val switchTimer = MSTimer()

    private val swingFails = mutableListOf<IASwingFailData>()

    private var aimPoint: Vec3? = null

    private val delayMillis: Long get() = 1000L / cps.random()

    override fun onDisable() {
        auraJob?.cancel()
        points.clear()
        target = null
        hittable = false
        prevTargetEntities.clear()
        clicks = 0

        synchronized(swingFails) {
            swingFails.clear()
        }
    }

    private val onWorld = handler<WorldEvent> {
        auraJob?.cancel()
        points.clear()
        target = null
        hittable = false
        prevTargetEntities.clear()

        synchronized(swingFails) {
            swingFails.clear()
        }
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        updateTarget()
    }

    private val auraJob by loopSequence(Dispatchers.Main) {
        when (modeValue) {
            "Aura" -> doTpAura()
            "Click" -> if (mc.gameSettings.keyBindAttack.isKeyDown) {
                val entity = findClickTarget() ?: return@loopSequence

                if (mc.thePlayer.getDistanceToEntity(entity) < 3) {
                    return@loopSequence
                }

                hit(entity, true)
            }
        }

        delay(delayMillis)
    }

    private fun findClickTarget(): EntityLivingBase? {
        val targets = mc.theWorld.loadedEntityList.filterTo(mutableListOf()) {
            it is EntityLivingBase && isSelected(it, true) &&
                mc.thePlayer.getDistanceSqToEntity(it) < distValue * distValue
        }

        if (targets.isEmpty()) return null

        return targets.minByOrNull {
            prioritizeTarget(it as EntityLivingBase)
        } as? EntityLivingBase
    }

    private suspend fun doTpAura() {
        val targets = mc.theWorld.loadedEntityList.filterTo(mutableListOf()) {
            it is EntityLivingBase &&
                isSelected(it, true) &&
                mc.thePlayer.getDistanceSqToEntity(it) < distValue * distValue
        }

        if (targets.isEmpty()) return

        val sortedTargets = targets.sortedBy { prioritizeTarget(it as EntityLivingBase) }

        var count = 0
        for (entity in sortedTargets) {
            if (hit(entity as EntityLivingBase)) {
                count++
            }
            if (count >= targetsValue) break
        }
    }

    private fun prioritizeTarget(entity: EntityLivingBase): Double {
        val player = mc.thePlayer ?: return Double.MAX_VALUE

        val distance = player.getDistanceToEntityBox(entity)
        val entityFov = rotationDifference(entity)

        return when (priority.lowercase()) {
            "distance" -> distance
            "direction" -> entityFov.toDouble()
            "health" -> entity.health.toDouble()
            "livingtime" -> -entity.ticksExisted.toDouble()
            "armor" -> entity.totalArmorValue.toDouble()
            "hurtresistance" -> entity.hurtResistantTime.toDouble()
            "hurttime" -> entity.hurtTime.toDouble()
            "healthabsorption" -> (entity.health + entity.absorptionAmount).toDouble()
            "regenamplifier" -> if (entity.isPotionActive(Potion.regeneration)) {
                entity.getActivePotionEffect(Potion.regeneration).amplifier.toDouble()
            } else -1.0
            "inweb" -> if (entity.isInWeb) -1.0 else Double.MAX_VALUE
            "onladder" -> if (entity.isOnLadder) -1.0 else Double.MAX_VALUE
            "inliquid" -> if (entity.isInWater || entity.isInLava) -1.0 else Double.MAX_VALUE
            else -> distance
        }
    }

    private suspend fun hit(entity: EntityLivingBase, force: Boolean = false): Boolean {
        val player = mc.thePlayer ?: return false

        updateRotations(entity)

        val path = withContext(coroutineContext) {
            PathUtils.findBlinkPath(entity.posX, entity.posY, entity.posZ, moveDistanceValue.toDouble())
        }

        if (path.isEmpty()) return false

        val lastDistance = path.last().let { entity.getDistance(it.xCoord, it.yCoord, it.zCoord) }
        if (!force && lastDistance > 10) return false

        if (!hittable && swing && failSwing) {
            val rotation = currentRotation ?: player.rotation

            if (rotationDifference(rotation) > maxRotationDifferenceToSwing) {
                return false
            }

            if (renderBoxOnSwingFail) {
                synchronized(swingFails) {
                    val centerDistance = (entity.hitBox.center - player.eyes).lengthVector()
                    val spot = player.eyes + getVectorForRotation(rotation) * centerDistance

                    swingFails += IASwingFailData(spot, System.currentTimeMillis())
                }
            }
        }

        path.forEach {
            if (packetValue == "PacketPosition") {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(it.xCoord, it.yCoord, it.zCoord, true))
            } else {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        it.xCoord,
                        it.yCoord,
                        it.zCoord,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        true
                    )
                )
            }
        }

        points.clear()
        points.addAll(path)

        if (lastDistance > 3 && packetBack) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(entity.posX, entity.posY, entity.posZ, true))
        }

        if (swing && swingValue) {
            mc.thePlayer.swingItem()
        } else {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        }

        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

        for (i in path.size - 1 downTo 0) {
            val vec = path[i]
            if (packetValue == "PacketPosition") {
                mc.netHandler.addToSendQueue(C04PacketPlayerPosition(vec.xCoord, vec.yCoord, vec.zCoord, true))
            } else {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        vec.xCoord,
                        vec.yCoord,
                        vec.zCoord,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        true
                    )
                )
            }
        }

        if (packetBack) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true))
        }

        return true
    }

    private fun updateTarget() {
        target = null

        val switchMode = targetMode == "Switch"

        val theWorld = mc.theWorld ?: return
        val thePlayer = mc.thePlayer ?: return

        var bestTarget: EntityLivingBase? = null
        var bestValue: Double? = null

        for (entity in theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isSelected(entity, true) ||
                (switchMode && entity.entityId in prevTargetEntities)) continue

            val distance = thePlayer.getDistanceToEntityBox(entity)

            if (distance > distValue) continue

            val entityFov = rotationDifference(entity)

            if (fov != 180F && entityFov > fov) continue

            if (switchMode && !thePlayer.isLookingOnEntity(entity, maxSwitchFOV.toDouble())) continue

            val currentValue = prioritizeTarget(entity)

            if (bestValue == null || currentValue < bestValue) {
                bestValue = currentValue
                bestTarget = entity
            }
        }

        if (bestTarget != null) {
            if (updateRotations(bestTarget)) {
                target = bestTarget
                updateHittable()

                val switchMode2 = targetMode == "Switch"

                if (!switchMode2 || switchTimer.hasTimePassed(switchDelay)) {
                    prevTargetEntities += bestTarget.entityId

                    if (switchMode2) {
                        switchTimer.reset()
                    }
                }

                return
            }
        }

        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    private fun updateRotations(entity: Entity): Boolean {
        val player = mc.thePlayer ?: return false

        if (!options.rotationsActive) {
            return player.getDistanceToEntityBox(entity) <= distValue
        }

        val prediction = entity.currPos.subtract(entity.prevPos).times(2 + predictEnemyPosition.toDouble())
        val boundingBox = entity.hitBox.offset(prediction)
        val (currPos, oldPos) = player.currPos to player.prevPos

        val simPlayer = SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput)
        simPlayer.rotationYaw = (currentRotation ?: player.rotation).yaw

        var pos = currPos

        repeat(predictClientMovement) {
            simPlayer.tick()

            if (predictOnlyWhenOutOfRange) {
                player.setPosAndPrevPos(simPlayer.pos)

                val currentDistance = player.getDistanceToEntityBox(entity)

                if (currentDistance <= distValue) {
                    return@repeat
                }
            }

            pos = simPlayer.pos
        }

        player.setPosAndPrevPos(currPos, oldPos)

        val eyes = pos.withY(pos.yCoord + player.eyeHeight)

        val rotation = RotationUtils.searchCenter(
            boundingBox,
            false,
            false,
            randomization,
            false,
            distValue.toFloat(),
            distValue.toFloat(),
            0f,
            listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
            horizontalBodySearchRange
        ) ?: return false

        aimPoint = boundingBox.center

        setTargetRotation(rotation, options = options)

        return true
    }

    private fun updateHittable() {
        val currentTarget = target ?: return
        val player = mc.thePlayer ?: return

        hittable = when {
            !options.rotationsActive -> true
            currentRotation == null -> false
            else -> {
                val distance = player.getDistanceToEntityBox(currentTarget)
                distance <= distValue && currentTarget.hurtTime <= hurtTime
            }
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        if (event.packet is S08PacketPlayerPosLook) {
            auraJob?.cancel()
        }

        val isMovePacket = (event.packet is C04PacketPlayerPosition || event.packet is C03PacketPlayer.C06PacketPlayerPosLook)

        if (noRegenValue && event.packet is C03PacketPlayer && !isMovePacket) {
            event.cancelEvent()
        }

        if (noLagBackValue && event.packet is S08PacketPlayerPosLook) {
            val capabilities = PlayerCapabilities()
            capabilities.allowFlying = true
            mc.netHandler.addToSendQueue(C13PacketPlayerAbilities(capabilities))

            val x = event.packet.x - mc.thePlayer.posX
            val y = event.packet.y - mc.thePlayer.posY
            val z = event.packet.z - mc.thePlayer.posZ
            val diff = sqrt(x * x + y * y + z * z)

            event.cancelEvent()
            sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    event.packet.x,
                    event.packet.y,
                    event.packet.z,
                    event.packet.yaw,
                    event.packet.pitch,
                    true
                )
            )
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        handleFailedSwings()
        drawAimPointBox()

        if (points.isEmpty() || !pathRenderValue) return@handler

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

        GL11.glColor4f(
            renderPathColor.red / 255f,
            renderPathColor.green / 255f,
            renderPathColor.blue / 255f,
            0.8f
        )

        for (vec in points) {
            val x = vec.xCoord - renderPosX
            val y = vec.yCoord - renderPosY
            val z = vec.zCoord - renderPosZ
            val width = 0.3
            val height = mc.thePlayer.eyeHeight.toDouble()

            mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
            GL11.glLineWidth(2f)
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
            GL11.glEnd()
        }

        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
        GL11.glColor4f(1f, 1f, 1f, 1f)
    }

    val onRender2D = handler<Render2DEvent> {
        if (!displayDebug) return@handler

        val sr = ScaledResolution(mc)
        val reach = if (target != null) {
            mc.thePlayer.getDistanceToEntityBox(target!!)
        } else {
            0.0
        }

        val formattedReach = String.format("%.2f", reach)
        val status = "InfiniteAura | Target: ${target?.name ?: "None"} | Reach: $formattedReach | Priority: $priority"

        mc.fontRendererObj.drawStringWithShadow(
            status,
            sr.scaledWidth / 2f - mc.fontRendererObj.getStringWidth(status) / 2f,
            sr.scaledHeight / 2f - 60f,
            Color.ORANGE.rgb
        )
    }

    private fun handleFailedSwings() {
        if (!renderBoxOnSwingFail) return

        val box = net.minecraft.util.AxisAlignedBB(0.0, 0.0, 0.0, 0.05, 0.05, 0.05)
        val renderManager = mc.renderManager

        synchronized(swingFails) {
            val fadeSeconds = renderBoxFadeSeconds * 1000L

            swingFails.removeAll {
                val timestamp = System.currentTimeMillis() - it.timestamp
                val transparency = (0f..255f).lerpWith(1 - (timestamp / fadeSeconds).coerceAtMost(1.0F))

                val offsetBox = box.offset(it.position - renderManager.renderPos)

                RenderUtils.drawAxisAlignedBB(offsetBox, renderBoxColor.color(a = transparency.roundToInt()))

                timestamp > fadeSeconds
            }
        }
    }

    private fun drawAimPointBox() {
        if (!renderAimPointBox) return

        val point = aimPoint ?: return
        val player = mc.thePlayer ?: return

        val size = aimPointBoxSize.toDouble()
        val box = net.minecraft.util.AxisAlignedBB(
            point.xCoord - size,
            point.yCoord - size,
            point.zCoord - size,
            point.xCoord + size,
            point.yCoord + size,
            point.zCoord + size
        )

        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ

        RenderUtils.drawAxisAlignedBB(
            box.offset(-renderPosX, -renderPosY, -renderPosZ),
            aimPointBoxColor
        )
    }

    override val tag: String
        get() = when {
            target != null -> target!!.name
            else -> "None"
        }
}

private data class IASwingFailData(val position: Vec3, val timestamp: Long)
