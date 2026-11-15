package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntityPlayerSP
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacketSilently
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.rotation.PostRotationExecutor
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.block.BlockBush
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import kotlin.math.ceil
import kotlin.math.floor

@Suppress("TooManyFunctions")
object CornerTrap : Module("CornerTrap", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val targetType by choices(
        "TargetType", arrayOf("Players", "PlayersAndMobs"), "Players"
    ).describe("Players only, or players and mobs too.")

    private val placeMode by choices(
        "PlaceMode", arrayOf("Legit", "Blatant"), "Legit"
    ).describe("Legit turns the camera before placing; Blatant sends placements directly.")

    private val blockPriority by choices(
        "BlockPriority", arrayOf("Hardest", "Most"), "Hardest"
    ).describe("Hardest picks the toughest block; Most picks the largest stack.")

    private val trapMode by choices(
        "TrapMode", arrayOf("Attack", "Escape"), "Attack"
    ).describe("Attack puts the head support on the far side; Escape blocks your side.")

    private var liquidStage = 0
    private var liquidFinishTicks = 0
    private var pendingRestoreSlot: Int? = null

    private val placeTimer = MSTimer()

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private fun isHumanoid(entity: EntityLivingBase): Boolean {
        return entity.width <= 0.8f && entity.height in 1.5f..2.1f
    }

    private fun matchesTargetType(entity: EntityLivingBase): Boolean {
        if (entity is EntityPlayer) return true
        if (targetType == "Players") return false
        return isHumanoid(entity)
    }

    private fun findPlacePositions(enemy: EntityLivingBase): List<BlockPos> {
        val world = mc.theWorld ?: return emptyList()
        val box = enemy.entityBoundingBox

        val minX = floor(box.minX).toInt()
        val maxX = ceil(box.maxX).toInt() - 1
        val minZ = floor(box.minZ).toInt()
        val maxZ = ceil(box.maxZ).toInt() - 1
        val minY = floor(box.minY).toInt()

        val occupied = mutableSetOf<BlockPos>()
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                occupied += BlockPos(x, minY, z)
            }
        }
        val result = mutableSetOf<BlockPos>()
        for (base in occupied) {
            for (side in EnumFacing.entries) {
                if (side.axis == EnumFacing.Axis.Y) continue

                val neighbor = base.offset(side)
                if (neighbor !in occupied && world.isAirBlock(neighbor)) {
                    result += neighbor
                }
            }
        }

        val head = occupied.first().up()
        val headSides = listOf(head.north(), head.south(), head.east(), head.west())
        val player = mc.thePlayer

        val headChoice = if (player == null) {
            null
        } else if (trapMode == "Escape") {
            headSides.filter { world.isAirBlock(it) }
                .minByOrNull { it.distanceSqToCenter(player.posX, player.posY, player.posZ) }
        } else {
            if (headSides.all { world.isAirBlock(it) }) {
                headSides.maxByOrNull { it.distanceSqToCenter(player.posX, player.posY, player.posZ) }
            } else null
        }
        headChoice?.let { result += it }

        return result.toList()
    }

    private fun isValidBlock(stack: ItemStack): Boolean {
        val item = stack.item
        if (item !is ItemBlock || stack.stackSize <= 0) return false

        val block = item.block
        return block !in InventoryUtils.BLOCK_BLACKLIST &&
                block !is BlockBush &&
                block.isFullCube
    }

    private fun findBlockSlot(): Int? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val candidates = (36..44).filter { slot ->
            val stack = player.inventorySlot(slot).stack ?: return@filter false
            isValidBlock(stack)
        }

        return candidates.maxByOrNull { slot ->
            val stack = player.inventorySlot(slot).stack
            val block = (stack.item as ItemBlock).block

            when (blockPriority) {
                "Most" -> stack.stackSize.toDouble()
                else -> block.getBlockHardness(world, BlockPos(0, 0, 0)).toDouble()
            }
        }?.minus(36)
    }

    private data class PlaceTarget(
        val clickPos: BlockPos,
        val side: EnumFacing,
        val hitVec: Vec3
    )

    private fun findPlaceInfo(pos: BlockPos): PlaceTarget? {
        val world = mc.theWorld ?: return null

        return EnumFacing.entries.firstNotNullOfOrNull { side ->
            val neighbor = pos.offset(side)
            val neighborBlock = world.getBlockState(neighbor).block

            if (world.isAirBlock(neighbor) || neighborBlock in InventoryUtils.BLOCK_BLACKLIST) {
                null
            } else {
                val hitVec = neighbor.center.add(
                    Vec3(
                        side.opposite.directionVec.x * 0.45,
                        side.opposite.directionVec.y * 0.45,
                        side.opposite.directionVec.z * 0.45
                    )
                )
                PlaceTarget(neighbor, side.opposite, hitVec)
            }
        }
    }

    private fun sendSilentLook(rotation: Rotation): Boolean {
        val player = mc.thePlayer ?: return false

        val serverRotation = RotationUtils.serverRotation
        val continuousYaw = serverRotation.yaw + RotationUtils.angleDifference(rotation.yaw, serverRotation.yaw)
        val wireRotation = Rotation(continuousYaw, rotation.pitch)

        sendPacketSilently(C05PacketPlayerLook(wireRotation.yaw, wireRotation.pitch, player.onGround))
        RotationUtils.serverRotation = wireRotation
        (player as? IMixinEntityPlayerSP)?.syncLastReportedRotation(wireRotation.yaw, wireRotation.pitch)

        return true
    }

    private fun placeBlockAt(pos: BlockPos): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false
        val slot = findBlockSlot() ?: return false
        val target = findPlaceInfo(pos) ?: return false

        val originalSlot = player.inventory.currentItem
        player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItem()
        sendPacketSilently(C09PacketHeldItemChange(slot))

        val stack = player.inventorySlot(slot + 36).stack
        if (stack == null || stack.item !is ItemBlock) {
            player.inventory.currentItem = originalSlot
            mc.playerController.syncCurrentPlayItem()
            sendPacketSilently(C09PacketHeldItemChange(originalSlot))
            return false
        }

        val success = mc.playerController.onPlayerRightClick(
            player, world, stack, target.clickPos, target.side, target.hitVec
        )

        nextTick {
            if (player.inventory.currentItem == slot) {
                player.inventory.currentItem = originalSlot
                mc.playerController.syncCurrentPlayItem()
                sendPacketSilently(C09PacketHeldItemChange(originalSlot))
            }
        }

        if (success) player.swingItem()

        return success
    }

    private fun findLiquidSlot(): Int? {
        val player = mc.thePlayer ?: return null

        return (36..44).firstOrNull {
            player.inventorySlot(it).stack?.item === Items.lava_bucket
        }?.minus(36)
    }

    private fun useItemOn(slot: Int, target: PlaceTarget): Boolean {
        val player = mc.thePlayer ?: return false
        if (mc.theWorld == null) return false
        if (player.inventorySlot(slot + 36).stack == null) return false

        val rotation = RotationUtils.toRotation(target.hitVec, false, player)

        if (options.rotationsActive) {
            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)

            if (RotationUtils.rotationDifference(rotation) > 1f) {
                return false
            }
        } else {
            sendSilentLook(rotation)
        }

        PostRotationExecutor.runPostMove { sendItemUse(slot, target) }
        return true
    }

    private fun sendItemUse(slot: Int, target: PlaceTarget): Boolean {
        val player = mc.thePlayer ?: return false
        if (mc.theWorld == null) return false
        if (player.inventorySlot(slot + 36).stack == null) return false

        val rotation = RotationUtils.toRotation(target.hitVec, false, player).fixedSensitivity()
        if (RotationUtils.rotationDifference(rotation) > RotationUtils.getFixedAngleDelta()) {
            sendSilentLook(rotation)
        }

        val originalSlot = player.inventory.currentItem
        player.inventory.currentItem = slot
        mc.playerController.syncCurrentPlayItem()
        sendPacketSilently(C09PacketHeldItemChange(slot))

        val stack = player.inventorySlot(slot + 36).stack
        if (stack == null) {
            player.inventory.currentItem = originalSlot
            mc.playerController.syncCurrentPlayItem()
            sendPacketSilently(C09PacketHeldItemChange(originalSlot))
            return false
        }

        sendPacketSilently(C08PacketPlayerBlockPlacement(stack))

        pendingRestoreSlot = originalSlot

        return true
    }

    private fun useLiquid(target: PlaceTarget): Boolean {
        val slot = findLiquidSlot() ?: return false
        return useItemOn(slot, target)
    }

    private fun findSupport(pos: BlockPos): PlaceTarget? {
        val world = mc.theWorld ?: return null
        val player = mc.thePlayer ?: return null
        val eyes = player.getPositionEyes(1f)

        val candidates = EnumFacing.entries
            .filter { it.axis != EnumFacing.Axis.Y }
            .mapNotNull { dir ->
                val neighbor = pos.offset(dir)
                if (world.isAirBlock(neighbor)) return@mapNotNull null

                val side = dir.opposite
                val hitVec = neighbor.center.add(
                    Vec3(
                        side.directionVec.x * 0.30,
                        side.directionVec.y * 0.30,
                        side.directionVec.z * 0.30
                    )
                )
                PlaceTarget(neighbor, side, hitVec)
            }

        return candidates.firstOrNull { target ->
            val hit = world.rayTraceBlocks(eyes, target.hitVec, false, false, false)
            hit != null && hit.blockPos == target.clickPos && hit.sideHit == target.side
        }
    }

    private fun handleLiquids(enemy: EntityLivingBase) {
        val feet = BlockPos(enemy.posX, enemy.posY, enemy.posZ)
        val head = feet.up()

        val headTarget = findSupport(head)

        if (liquidStage != 0) return

        if (headTarget == null) {
            liquidStage = 2
            return
        }

        if (useLiquid(headTarget)) {
            liquidStage = 2
        }
    }

    override fun onDisable() {
        liquidStage = 0
        liquidFinishTicks = 0
        pendingRestoreSlot = null
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        pendingRestoreSlot?.let { restore ->
            player.inventory.currentItem = restore
            mc.playerController.syncCurrentPlayItem()
            sendPacketSilently(C09PacketHeldItemChange(restore))
            pendingRestoreSlot = null
        }

        if (mc.playerController.isHittingBlock) return@handler

        val enemies = world.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .filter { matchesTargetType(it) }
            .filter { EntityUtils.isSelected(it, true) }
            .filter { player.getDistanceToEntityBox(it) <= 4.5f }

        val enemy = enemies.singleOrNull() ?: return@handler

        val positions = findPlacePositions(enemy)
        if (positions.isEmpty()) {
            if (trapMode == "Attack") {
                handleLiquids(enemy)
                if (liquidStage == 2) {
                    if (++liquidFinishTicks >= 3) state = false
                }
            } else {
                state = false
            }
            return@handler
        }

        val pos = positions.first()

        if (placeMode == "Legit" && options.rotationsActive) {
            val info = findPlaceInfo(pos) ?: return@handler
            val rotation = RotationUtils.toRotation(info.hitVec, false, player)

            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)

            if (RotationUtils.rotationDifference(rotation) > 5f) {
                return@handler
            }
        }

        if (!placeTimer.hasTimePassed(150)) return@handler

        if (placeBlockAt(pos) && !world.isAirBlock(pos)) {
            placeTimer.reset()
        }
    }
}
