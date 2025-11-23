/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.realX
import net.ccbluex.liquidbounce.utils.client.realY
import net.ccbluex.liquidbounce.utils.client.realZ
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenContainer
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils.contains
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.performRayTrace
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.performRaytrace
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockEnderChest
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

object ChestAura : Module("ChestAura", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val chest by boolean("Chest", true)
    private val enderChest by boolean("EnderChest", false)

    private val range by float("Range", 5F, 1F..5F).onChanged { value ->
        rangeSq = value.pow(2)
        searchRadiusSq = (value + 1).pow(2)
    }
    private val delay by int("Delay", 200, 50..500)

    private val throughWalls by boolean("ThroughWalls", true)
    private val wallsRange by float("ThroughWallsRange", 3F, 1F..5F) {
        throughWalls
    }.onChange { _, new ->
        new.coerceAtMost(this@ChestAura.range)
    }.onChanged { value ->
        wallsRangeSq = value.pow(2)
    }

    private val minDistanceFromOpponent by float("MinDistanceFromOpponent", 10F, 0F..30F).onChanged { value ->
        minDistanceFromOpponentSq = value.pow(2)
    }

    private val visualSwing by boolean("VisualSwing", true).subjective()

    private val ignoreLooted by boolean("IgnoreLootedChests", true)
    private val detectRefill by boolean("DetectChestRefill", true)

    private val options = RotationSettings(this).withoutKeepRotation()

    private val openInfo by choices("OpenInfo", arrayOf("Off", "Self", "Other", "Everyone"), "Off")

    var tileTarget: TileTarget? = null
    private val timer = MSTimer()

    // Squared distances, they get updated when values initiate or get changed
    private var searchRadiusSq = 0f // (range + 1) ^ 2
    private var rangeSq = 0f
    private var wallsRangeSq = 0f
    private var minDistanceFromOpponentSq = 0f

    val clickedTileEntities = mutableSetOf<TileEntity>()
    private val chestOpenMap = mutableMapOf<BlockPos, Pair<Int, Long>>()

    // Substrings that indicate that chests have been refilled, broadcasted via title packet
    private val refillSubstrings = arrayOf("refill", "reabastecidos")
    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))

    data class TileTarget(
        val clickPoint: Vec3,
        val entity: TileEntity,
        val distanceSq: Double
    )

    val onRotationUpdate = handler<RotationUpdateEvent> {
        if (Blink.handleEvents() || KillAura.isBlockingChestAura || !timer.hasTimePassed(delay))
            return@handler

        val thePlayer = mc.thePlayer ?: return@handler

        // Check if there is an opponent in range
        if (mc.theWorld.loadedEntityList.any {
                isSelected(it, true) && thePlayer.getDistanceSqToEntity(it) < minDistanceFromOpponentSq
            }) return@handler

        if (serverOpenContainer && tileTarget != null) {
            timer.reset()

            return@handler
        }

        val eyes = thePlayer.eyes
        val (eyeX, eyeY, eyeZ) = eyes

        mc.theWorld.tickableTileEntities
            .asSequence()
            // Check if tile entity is correct type, not already clicked, not blocked by a block and in range
            .filter {
                shouldClickTileEntity(it) && it.pos.distanceSqToCenter(eyeX, eyeY, eyeZ) <= searchRadiusSq
            }.flatMap { entity ->
                val box = entity.blockType.getSelectedBoundingBox(mc.theWorld, entity.pos)

                (sequenceOf(getNearestPointBB(eyes, box)) + box.getPointSequence(step = 0.1)).mapNotNull { point ->
                    val distanceSq = point.squareDistanceTo(eyes)

                    if (distanceSq <= rangeSq) TileTarget(point, entity, distanceSq) else null
                }
            }.sortedBy { it.distanceSq }
            // Vecs are already sorted by distance
            .firstOrNull { (vec, entity) ->
                // If through walls is enabled and its range is same as normal, just return the first one
                if (throughWalls && wallsRange >= range)
                    return@firstOrNull true

                val result = mc.theWorld.rayTraceBlocks(eyes, vec) ?: return@firstOrNull false
                val distanceSq = result.hitVec.squareDistanceTo(eyes)

                // If chest is behind a wall, check if through walls is enabled and its range
                if (result.blockPos != entity.pos) {
                    throughWalls && distanceSq <= wallsRangeSq
                } else distanceSq <= rangeSq
            }?.let {
                tileTarget = it

                if (options.rotationsActive) {
                    setTargetRotation(toRotation(it.clickPoint), options = options)
                }
            }
    }

    val onWorld = handler<WorldEvent> {
        onDisable()
    }

    override fun onDisable() {
        clickedTileEntities.clear()
        chestOpenMap.clear()
    }

    val onPacket = handler<PacketEvent> { event ->
        when (val packet = event.packet) {
            // Detect chest opening from sound effect
            is S29PacketSoundEffect -> {
                if (packet.soundName != "random.chestopen")
                    return@handler

                val entity = mc.theWorld.getTileEntity(BlockPos(packet.x, packet.y, packet.z)) ?: return@handler

                clickedTileEntities += entity
            }

            // Detect already looted chests by having their lid open or closed
            is S24PacketBlockAction -> {
                if (!ignoreLooted || (packet.blockType !is BlockChest && packet.blockType !is BlockEnderChest))
                    return@handler

                val packetBlockPos = packet.blockPosition

                clickedTileEntities += mc.theWorld.getTileEntity(packetBlockPos)

                if (openInfo != "Off") {
                    val (prevState, prevTime) = chestOpenMap[packetBlockPos] ?: (null to null)

                    // Prevent repetitive packet spamming
                    if (prevState == packet.data2)
                        return@handler

                    // If there is no info about the chest ever being opened, don't print anything
                    if (packet.data2 == 0 && prevState != 1)
                        return@handler

                    val player: EntityPlayer
                    val distance: String

                    // If chest is not last clicked chest, find a player that might have opened it
                    if (packetBlockPos != tileTarget?.entity?.pos) {
                        val nearPlayers = (mc.theWorld.playerEntities ?: return@handler)
                            .mapNotNull {
                                val distanceSq = it.getDistanceSqToCenter(packetBlockPos)

                                if (distanceSq <= 36) it to distanceSq
                                else null
                            }.sortedBy { it.second }

                        // Find the closest player that is looking at the chest or else just the closest
                        player = (nearPlayers.firstOrNull { (player) ->
                            player.rayTrace(5.0, 1f)?.blockPos == packetBlockPos
                        } ?: nearPlayers.first()).first

                        val entity = mc.theWorld.getTileEntity(packetBlockPos)
                        val box = entity.blockType.getSelectedBoundingBox(mc.theWorld, packetBlockPos)
                        distance = decimalFormat.format(player.getDistanceToBox(box))
                    } else {
                        player = mc.thePlayer
                        distance = decimalFormat.format(sqrt(tileTarget!!.distanceSq))
                    }

                    when (player) {
                        mc.thePlayer -> if (openInfo == "Other") return@handler
                        else -> if (openInfo == "Self") return@handler
                    }

                    val actionMsg = if (packet.data2 == 1) "§a§lOpened§3" else "§c§lClosed§3"
                    val timeTakenMsg = if (packet.data2 == 0 && prevTime != null)
                        ", took §b${decimalFormat.format((System.currentTimeMillis() - prevTime) / 1000.0)} s§3"
                    else ""
                    val playerMsg =
                        if (player == mc.thePlayer) actionMsg else "§b${player.name} §3${actionMsg.lowercase()}"

                    chat("§8[§9§lChestAura§8] $playerMsg chest from §b$distance m§3$timeTakenMsg.")

                    chestOpenMap[packetBlockPos] = packet.data2 to System.currentTimeMillis()
                }
            }

            // Detect chests getting refilled
            is S45PacketTitle -> {
                if (!detectRefill)
                    return@handler

                if (refillSubstrings in packet.message?.unformattedText)
                    clickedTileEntities.clear()
            }

            // Armor stands might be showing time until opened chests get refilled
            // Whenever an armor stand spawns, blacklist chest that it might be inside
            is S0EPacketSpawnObject -> {
                if (ignoreLooted && packet.type == 78) {
                    val entity = mc.theWorld.getTileEntity(
                        BlockPos(packet.realX, packet.realY + 2.0, packet.realZ)
                    )

                    if (entity !is TileEntityChest && entity !is TileEntityEnderChest)
                        return@handler

                    clickedTileEntities += entity
                }
            }
        }
    }

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler
        val target = tileTarget ?: return@handler

        val rotationToUse = if (options.rotationsActive) {
            currentRotation ?: return@handler
        } else toRotation(target.clickPoint)

        val distance = sqrt(target.distanceSq)

        if (distance <= range) {
            val pos = target.entity.pos

            val rotationVec = getVectorForRotation(rotationToUse) * mc.playerController.blockReachDistance.toDouble()

            val visibleResult = performRayTrace(pos, rotationVec)?.takeIf { it.blockPos == pos }
            val invisibleResult = performRaytrace(pos, rotationToUse)?.takeIf { it.blockPos == pos }

            (visibleResult ?: invisibleResult)?.run {
                if (player.onPlayerRightClick(blockPos, sideHit, hitVec)) {
                    if (visualSwing) player.swingItem()
                    else sendPacket(C0APacketAnimation())

                    timer.reset()
                }
            }
        }
    }

    private fun shouldClickTileEntity(entity: TileEntity): Boolean {
        // Check if entity hasn't been clicked already
        if (entity in clickedTileEntities) return false

        // Check if entity is of correct type
        return when (entity) {
            is TileEntityChest -> {
                if (!chest) return false

                val block = entity.pos.block

                if (block !is BlockChest) return false

                // Check if there isn't a block above the chest (works even for double chests)
                block.getLockableContainer(mc.theWorld, entity.pos) != null
            }

            is TileEntityEnderChest ->
                enderChest && entity.pos.up().block?.isNormalCube != true

            else -> return false
        }
    }
}