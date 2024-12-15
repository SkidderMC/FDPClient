/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.client.*
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBacktrackBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.server.*
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3
import net.minecraft.world.WorldSettings
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object Backtrack : Module("Backtrack", Category.COMBAT, hideModule = false) {

    private val nextBacktrackDelay by int("NextBacktrackDelay", 0, 0..2000) { mode == "Modern" }
    private val maxDelay: IntegerValue = object : IntegerValue("MaxDelay", 80, 0..700) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay.get())
    }
    private val minDelay: IntegerValue = object : IntegerValue("MinDelay", 80, 0..700) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay.get())
        override fun isSupported() = mode == "Modern"
    }

    val mode by object : ListValue("Mode", arrayOf("Legacy", "Modern"), "Modern") {
        override fun onChanged(oldValue: String, newValue: String) {
            clearPackets()
            backtrackedPlayer.clear()
        }
    }

    // Legacy
    private val legacyPos by choices(
        "Caching mode",
        arrayOf("ClientPos", "ServerPos"),
        "ClientPos"
    ) { mode == "Legacy" }

    // Modern
    private val style by choices("Style", arrayOf("Pulse", "Smooth"), "Smooth") { mode == "Modern" }

    private val maxDistanceValue: FloatValue = object : FloatValue("MaxDistance", 3.0f, 0.0f..3.5f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minDistance)
        override fun isSupported() = mode == "Modern"
    }
    private val maxDistance by maxDistanceValue
    private val minDistance by object : FloatValue("MinDistance", 2.0f, 0.0f..3.0f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceIn(minimum, maxDistance)
        override fun isSupported() = mode == "Modern"
    }
    private val smart by boolean("Smart", true) { mode == "Modern" }

    // ESP
    private val espMode by choices(
        "ESP-Mode",
        arrayOf("None", "Box", "Model", "Wireframe"),
        "Box",
        subjective = true
    ) { mode == "Modern" }
    private val wireframeWidth by float("WireFrame-Width", 1f, 0.5f..5f) { espMode == "WireFrame" }

    private val espColorMode by choices("ESP-Color", arrayOf("Custom", "Rainbow"), "Custom")
    { espMode != "Model" && mode == "Modern" }
    private val espColor = ColorSettingsInteger(this, "ESP", withAlpha = false)
    { espColorMode == "Custom" && espMode != "Model" && mode == "Modern" }.with(0, 255, 0)

    private val packetQueue = ConcurrentLinkedQueue<QueueData>()
    private val positions = mutableListOf<Pair<Vec3, Long>>()

    var target: EntityLivingBase? = null

    private var globalTimer = MSTimer()

    var shouldRender = true

    private var ignoreWholeTick = false

    private var delayForNextBacktrack = 0L

    private var modernDelay = randomDelay(minDelay.get(), maxDelay.get()) to false

    private val supposedDelay
        get() = if (mode == "Modern") modernDelay.first else maxDelay.get()

    // Legacy
    private val maximumCachedPositions by int("MaxCachedPositions", 10, 1..20) { mode == "Legacy" }

    private val backtrackedPlayer = ConcurrentHashMap<UUID, MutableList<BacktrackData>>()

    private val nonDelayedSoundSubstrings = arrayOf("game.player.hurt", "game.player.die")

    val isPacketQueueEmpty
        get() = synchronized(packetQueue) { packetQueue.isEmpty() }

    val areQueuedPacketsEmpty
        get() = PacketUtils.queuedPackets?.run { synchronized(this) { isEmpty() } } == true

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (Blink.blinkingReceive() || event.isCancelled)
            return@handler

        when (mode.lowercase()) {
            "legacy" -> {
                when (packet) {
                    // Check if packet is a spawn player packet
                    is S0CPacketSpawnPlayer -> {
                        // Insert first backtrack data
                        addBacktrackData(
                            packet.player,
                            packet.realX,
                            packet.realY,
                            packet.realZ,
                            System.currentTimeMillis()
                        )
                    }

                    is S14PacketEntity -> if (legacyPos == "ServerPos") {
                        val entity = mc.theWorld?.getEntityByID(packet.entityId)
                        val entityMixin = entity as? IMixinEntity
                        if (entityMixin != null) {
                            addBacktrackData(
                                entity.uniqueID,
                                entityMixin.trueX,
                                entityMixin.trueY,
                                entityMixin.trueZ,
                                System.currentTimeMillis()
                            )
                        }
                    }

                    is S18PacketEntityTeleport -> if (legacyPos == "ServerPos") {
                        val entity = mc.theWorld?.getEntityByID(packet.entityId)
                        val entityMixin = entity as? IMixinEntity
                        if (entityMixin != null) {
                            addBacktrackData(
                                entity.uniqueID,
                                entityMixin.trueX,
                                entityMixin.trueY,
                                entityMixin.trueZ,
                                System.currentTimeMillis()
                            )
                        }
                    }
                }
            }

            "modern" -> {
                if (mc.isSingleplayer || mc.currentServerData == null) {
                    clearPackets()
                    return@handler
                }

                // Prevent cancelling packets when not needed
                if (isPacketQueueEmpty && areQueuedPacketsEmpty && !shouldBacktrack())
                    return@handler

                when (packet) {
                    // Ignore server related packets
                    is C00Handshake, is C00PacketServerQuery, is S02PacketChat, is S01PacketPong -> return@handler

                    is S29PacketSoundEffect -> if (nonDelayedSoundSubstrings in packet.soundName) return@handler

                    // Flush on own death
                    is S06PacketUpdateHealth -> if (packet.health <= 0) {
                        clearPackets()
                        return@handler
                    }

                    is S13PacketDestroyEntities -> if (target != null && target!!.entityId in packet.entityIDs) {
                        clearPackets()
                        reset()
                        return@handler
                    }

                    is S1CPacketEntityMetadata -> if (target?.entityId == packet.entityId) {
                        val metadata = packet.func_149376_c() ?: return@handler

                        metadata.forEach {
                            if (it.dataValueId == 6) {
                                val objectValue = it.getObject().toString().toDoubleOrNull()
                                if (objectValue != null && !objectValue.isNaN() && objectValue <= 0.0) {
                                    clearPackets()
                                    reset()
                                    return@handler
                                }
                            }
                        }

                        return@handler
                    }

                    is S19PacketEntityStatus -> if (packet.entityId == target?.entityId) return@handler
                }

                // Cancel every received packet to avoid possible server synchronization issues from random causes.
                if (event.eventType == EventState.RECEIVE) {
                    when (packet) {
                        is S14PacketEntity -> if (packet.entityId == target?.entityId) {
                            (target as? IMixinEntity)?.run {
                                synchronized(positions) {
                                    positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                }
                            }
                        }

                        is S18PacketEntityTeleport -> if (packet.entityId == target?.entityId) {
                            (target as? IMixinEntity)?.run {
                                synchronized(positions) {
                                    positions += Pair(Vec3(trueX, trueY, trueZ), System.currentTimeMillis())
                                }
                            }
                        }
                    }

                    event.cancelEvent()
                    synchronized(packetQueue) {
                        packetQueue += QueueData(packet, System.currentTimeMillis())
                    }
                }
            }
        }
    }

    val onGameLoop = handler<GameLoopEvent> {
        if (mode == "Legacy") {
            backtrackedPlayer.forEach { (key, backtrackData) ->
                // Remove old data
                backtrackData.removeAll { it.time + supposedDelay < System.currentTimeMillis() }

                // Remove player if there is no data left. This prevents memory leaks.
                if (backtrackData.isEmpty())
                    removeBacktrackData(key)
            }
        }

        val target = target
        val targetMixin = target as? IMixinEntity

        if (mode == "Modern") {
            if (targetMixin != null) {
                if (!Blink.blinkingReceive() && shouldBacktrack() && targetMixin.truePos) {
                    val trueDist = mc.thePlayer.getDistance(targetMixin.trueX, targetMixin.trueY, targetMixin.trueZ)
                    val dist = mc.thePlayer.getDistance(target.posX, target.posY, target.posZ)

                    if (trueDist <= 6f && (!smart || trueDist >= dist) && (style == "Smooth" || !globalTimer.hasTimePassed(
                            supposedDelay
                        ))
                    ) {
                        shouldRender = true

                        if (mc.thePlayer.getDistanceToEntityBox(target) in minDistance..maxDistance) {
                            handlePackets()
                        } else {
                            handlePacketsRange()
                        }
                    } else {
                        clearPackets()
                        globalTimer.reset()
                    }
                }
            } else {
                clearPackets()
                globalTimer.reset()
            }
        }

        ignoreWholeTick = false
    }

    /**
     * Priority lower than [PacketUtils] GameLoopEvent function's priority.
     */
    val onQueuePacketClear = handler<GameLoopEvent>(priority = -6) {
        val shouldChangeDelay = isPacketQueueEmpty && areQueuedPacketsEmpty

        if (!shouldChangeDelay) {
            modernDelay = modernDelay.first to false
        }

        if (shouldChangeDelay && !modernDelay.second && !shouldBacktrack()) {
            delayForNextBacktrack = System.currentTimeMillis() + nextBacktrackDelay
            modernDelay = randomDelay(minDelay.get(), maxDelay.get()) to true
        }
    }

    val onAttack = handler<AttackEvent> { event ->
        if (!isSelected(event.targetEntity, true))
            return@handler

        // Clear all packets, start again on enemy change
        if (target != event.targetEntity) {
            clearPackets()
            reset()
        }

        if (event.targetEntity is EntityLivingBase) {
            target = event.targetEntity
        }
    }

    val onRender3D = handler<Render3DEvent> { event ->
        val manager = mc.renderManager ?: return@handler

        when (mode.lowercase()) {
            "legacy" -> {
                val color = Color.RED

                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity is EntityPlayer) {
                        glPushMatrix()
                        glDisable(GL_TEXTURE_2D)
                        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                        glEnable(GL_LINE_SMOOTH)
                        glEnable(GL_BLEND)
                        glDisable(GL_DEPTH_TEST)

                        mc.entityRenderer.disableLightmap()

                        glBegin(GL_LINE_STRIP)
                        glColor(color)

                        loopThroughBacktrackData(entity) {
                            (entity.currPos - manager.renderPos).let { glVertex3d(it.xCoord, it.yCoord, it.zCoord) }
                            false
                        }

                        glColor4d(1.0, 1.0, 1.0, 1.0)
                        glEnd()
                        glEnable(GL_DEPTH_TEST)
                        glDisable(GL_LINE_SMOOTH)
                        glDisable(GL_BLEND)
                        glEnable(GL_TEXTURE_2D)
                        glPopMatrix()
                    }
                }
            }

            "modern" -> {
                if (!shouldBacktrack() || !shouldRender)
                    return@handler

                target?.run {
                    val targetEntity = target as IMixinEntity

                    val (x, y, z) = targetEntity.interpolatedPosition - manager.renderPos

                    if (targetEntity.truePos) {
                        when (espMode.lowercase()) {
                            "box" -> {
                                val axisAlignedBB = entityBoundingBox.offset(-currPos + Vec3(x, y, z))

                                drawBacktrackBox(axisAlignedBB, color)
                            }

                            "model" -> {
                                glPushMatrix()
                                glPushAttrib(GL_ALL_ATTRIB_BITS)
                                color(0.6f, 0.6f, 0.6f, 1f)
                                manager.doRenderEntity(
                                    this,
                                    x, y, z,
                                    prevRotationYaw + (rotationYaw - prevRotationYaw) * event.partialTicks,
                                    event.partialTicks,
                                    true
                                )

                                glPopAttrib()
                                glPopMatrix()
                            }

                            "wireframe" -> {
                                val color = if (espColorMode == "Rainbow") rainbow() else Color(espColor.color().rgb)

                                glPushMatrix()
                                glPushAttrib(GL_ALL_ATTRIB_BITS)

                                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                                glDisable(GL_TEXTURE_2D)
                                glDisable(GL_LIGHTING)
                                glDisable(GL_DEPTH_TEST)
                                glEnable(GL_LINE_SMOOTH)

                                glEnable(GL_BLEND)
                                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

                                glLineWidth(wireframeWidth)

                                glColor(color)
                                manager.doRenderEntity(
                                    this,
                                    x, y, z,
                                    prevRotationYaw + (rotationYaw - prevRotationYaw) * event.partialTicks,
                                    event.partialTicks,
                                    true
                                )
                                glColor(color)
                                manager.doRenderEntity(
                                    this,
                                    x, y, z,
                                    prevRotationYaw + (rotationYaw - prevRotationYaw) * event.partialTicks,
                                    event.partialTicks,
                                    true
                                )

                                glPopAttrib()
                                glPopMatrix()
                            }
                        }
                    }
                }
            }
        }
    }

    val onEntityMove = handler<EntityMovementEvent> { event ->
        if (mode == "Legacy" && legacyPos == "ClientPos") {
            val entity = event.movedEntity

            // Check if entity is a player
            if (entity is EntityPlayer) {
                // Add new data
                addBacktrackData(entity.uniqueID, entity.posX, entity.posY, entity.posZ, System.currentTimeMillis())
            }
        }
    }

           val onWorld = handler<WorldEvent> { event ->
        // Clear packets on disconnect only
        // Set target to null on world change
        if (mode == "Modern") {
            if (event.worldClient == null)
                clearPackets(false)
            target = null
        }
    }

    override fun onEnable() = reset()

    override fun onDisable() {
        clearPackets()
        backtrackedPlayer.clear()
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - supposedDelay) {
                    schedulePacketProcess(packet)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < System.currentTimeMillis() - supposedDelay }
        }
    }

    private fun handlePacketsRange() {
        val time = getRangeTime()

        if (time == -1L) {
            clearPackets()
            return
        }

        synchronized(packetQueue) {
            packetQueue.removeAll { (packet, timestamp) ->
                if (timestamp <= time) {
                    schedulePacketProcess(packet)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.removeAll { (_, timestamp) -> timestamp < time }
        }
    }

    private fun getRangeTime(): Long {
        val target = this.target ?: return 0L

        var time = 0L
        var found = false

        synchronized(positions) {
            for (data in positions) {
                time = data.second

                val targetPos = target.currPos

                val targetBox = target.hitBox.offset(data.first - targetPos)

                if (mc.thePlayer.getDistanceToBox(targetBox) in minDistance..maxDistance) {
                    found = true
                    break
                }
            }
        }

        return if (found) time else -1L
    }

    private fun clearPackets(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            packetQueue.removeAll {
                if (handlePackets) {
                    schedulePacketProcess(it.packet)
                }

                true
            }
        }

        positions.clear()
        shouldRender = false
        ignoreWholeTick = true
    }

    private fun addBacktrackData(id: UUID, x: Double, y: Double, z: Double, time: Long) {
        // Get backtrack data of player
        val backtrackData = getBacktrackData(id)

        // Check if there is already data of the player
        if (backtrackData != null) {
            // Check if there is already enough data of the player
            if (backtrackData.size >= maximumCachedPositions) {
                // Remove first data
                backtrackData.removeFirst()
            }

            // Insert new data
            backtrackData += BacktrackData(x, y, z, time)
        } else {
            // Create new list
            backtrackedPlayer[id] = mutableListOf(BacktrackData(x, y, z, time))
        }
    }

    private fun getBacktrackData(id: UUID) = backtrackedPlayer[id]

    private fun removeBacktrackData(id: UUID) = backtrackedPlayer.remove(id)

    /**
     * This function will return the nearest tracked range of an entity.
     */
    fun getNearestTrackedDistance(entity: Entity): Double {
        var nearestRange = 0.0

        loopThroughBacktrackData(entity) {
            val range = entity.getDistanceToEntityBox(mc.thePlayer)

            if (range < nearestRange || nearestRange == 0.0) {
                nearestRange = range
            }

            false
        }

        return nearestRange
    }

    /**
     * This function will loop through the backtrack data of an entity.
     */
    fun loopThroughBacktrackData(entity: Entity, action: () -> Boolean) {
        if (!state || entity !is EntityPlayer || mode == "Modern")
            return

        val backtrackDataArray = getBacktrackData(entity.uniqueID) ?: return

        val currPos = entity.currPos
        val prevPos = entity.prevPos

        // This will loop through the backtrack data. We are using reversed() to loop through the data from the newest to the oldest.
        for ((x, y, z, _) in backtrackDataArray.reversed()) {
            entity.setPosAndPrevPos(Vec3(x, y, z))

            if (action())
                break
        }

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)
    }

    fun runWithNearestTrackedDistance(entity: Entity, f: () -> Unit) {
        if (entity !is EntityPlayer || !handleEvents() || mode == "Modern") {
            f()

            return
        }

        var backtrackDataArray = getBacktrackData(entity.uniqueID)?.toMutableList()

        if (backtrackDataArray == null) {
            f()

            return
        }

        backtrackDataArray = backtrackDataArray.sortedBy { (x, y, z, _) ->
            runWithSimulatedPosition(entity, Vec3(x, y, z)) {
                mc.thePlayer.getDistanceToBox(entity.hitBox)
            }
        }.toMutableList()

        val (x, y, z, _) = backtrackDataArray.first()

        runWithSimulatedPosition(entity, Vec3(x, y, z)) {
            f()

            null
        }
    }

    fun runWithSimulatedPosition(entity: Entity, vec3: Vec3, f: () -> Double?): Double? {
        val currPos = entity.currPos
        val prevPos = entity.prevPos

        entity.setPosAndPrevPos(vec3)

        val result = f()

        // Reset position
        entity.setPosAndPrevPos(currPos, prevPos)

        return result
    }

    val color
        get() = if (espColorMode == "Rainbow") rainbow() else Color(espColor.color().rgb)

    private fun shouldBacktrack() =
        mc.thePlayer != null && mc.theWorld != null && target != null && mc.thePlayer.health > 0 && (target!!.health > 0 || target!!.health.isNaN())
                && mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR && System.currentTimeMillis() >= delayForNextBacktrack && target?.let {
            isSelected(it, true) && (mc.thePlayer?.ticksExisted ?: 0) > 20 && !ignoreWholeTick
        } == true

    private fun reset() {
        target = null
        globalTimer.reset()
    }

    override val tag: String?
        get() = supposedDelay.toString()
}

data class QueueData(val packet: Packet<*>, val time: Long)
data class BacktrackData(val x: Double, val y: Double, val z: Double, val time: Long)
