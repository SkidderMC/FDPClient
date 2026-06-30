/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.isReplaceable
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.minecraft.block.BlockBush
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.ForgeEventFactory
import java.awt.Color
import kotlin.math.floor

/**
 * Module BlockTrap
 *
 * Traps the combat target by placing blocks in the open positions around it,
 * so the enemy can't move out, mine down or tower away.
 */
object BlockTrap : Module("BlockTrap", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, gameDetecting = false) {

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to the block before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing blocks.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Delay between block placements.")

    // The horizontal reach (in blocks) at which a target may be trapped.
    private val range by float("Range", 4.5f, 1f..6f)
        .describe("Max distance to trap a target.")

    // How many blocks may be placed within a single tick.
    private val maxPlacements by int("MaxPlacements", 1, 1..4)
        .describe("Max blocks placed in a single tick.")

    // Also place the ring of blocks one layer above the feet (the legs) so they can't simply step out.
    private val trapLegs by boolean("TrapLegs", true)
        .describe("Also wall the block above the feet.")

    // Place two blocks above the target's head so they can't mine straight up and tower to escape.
    private val trapAbove by boolean("TrapAbove", false)
        .describe("Place blocks above the head to stop towering.")

    // Verify the candidate spot is reachable by raytrace before placing.
    private val raycast by boolean("Raycast", true) { options.rotationsActive }
        .describe("Verify the spot is reachable before placing.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count placements toward the CPS counter.")
    private val mark by boolean("Mark", false)
        .describe("Render boxes on planned trap positions.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    init {
        group("Placing", "AutoBlock", "Swing", "PlaceDelay", "MaxPlacements")
        group("Trap", "Range", "TrapLegs", "TrapAbove", "Raycast")
        options.nestInto(group("Rotations"))
        group("Display", "TrackCPS", "Mark")
    }

    private val timerCounter = MSTimer()

    private val trapPositions = mutableListOf<BlockPos>()

    override fun onDisable() {
        trapPositions.clear()
        SilentHotbar.resetSlot(this)
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        trapPositions.clear()

        val target = world.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .filter { isSelected(it, true) && player.getDistanceToEntityBox(it) <= range }
            .minByOrNull { player.getDistanceToEntityBox(it) } ?: return@handler

        collectTrapPositions(target)

        if (trapPositions.isEmpty()) {
            return@handler
        }

        val playerPos = player.position ?: return@handler
        val nearest = trapPositions.minByOrNull { it.distanceSq(playerPos) } ?: return@handler

        val placeInfo = findPlaceInfo(nearest) ?: return@handler

        val rotation = RotationUtils.toRotation(placeInfo.vec3, false, player)

        if (options.rotationsActive) {
            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
        }

        if (!timerCounter.hasTimePassed(placeDelay)) {
            return@handler
        }

        var placed = 0

        for (pos in trapPositions.sortedBy { it.distanceSq(playerPos) }) {
            if (placed >= maxPlacements) {
                break
            }

            val info = findPlaceInfo(pos) ?: continue

            if (placeBlock(info.blockPos, info.enumFacing, info.vec3)) {
                placed++
            }
        }

        if (placed > 0) {
            timerCounter.reset()
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!mark) {
            return@handler
        }

        for (pos in trapPositions) {
            RenderUtils.drawBlockBox(pos, Color(255, 68, 68, 100), false)
        }
    }

    /**
     * Collects the open positions forming the cage around the [target]: the horizontal ring
     * around the feet (and optionally the legs), plus optionally two blocks above the head.
     */
    private fun collectTrapPositions(target: EntityLivingBase) {
        val box = target.entityBoundingBox

        val feetY = floor(box.minY).toInt()
        val headY = floor(box.maxY).toInt()

        val minX = floor(box.minX).toInt()
        val maxX = floor(box.maxX).toInt()
        val minZ = floor(box.minZ).toInt()
        val maxZ = floor(box.maxZ).toInt()

        // The cells the target itself occupies horizontally (the inside of the cage).
        val insideX = minX..maxX
        val insideZ = minZ..maxZ

        // The layers that make up the wall of the cage.
        val ringYs = mutableListOf(feetY)

        if (trapLegs && headY > feetY) {
            ringYs.add(feetY + 1)
        }

        for (y in ringYs) {
            for (x in (minX - 1)..(maxX + 1)) {
                for (z in (minZ - 1)..(maxZ + 1)) {
                    // Only the outer ring (skip the cells the target stands on).
                    if (x in insideX && z in insideZ) {
                        continue
                    }

                    // Skip diagonal corners so we don't waste blocks where the target can't slip through.
                    if (x !in insideX && z !in insideZ) {
                        continue
                    }

                    val pos = BlockPos(x, y, z)

                    if (pos.isReplaceable) {
                        trapPositions.add(pos)
                    }
                }
            }
        }

        if (trapAbove) {
            for (offset in 1..2) {
                val pos = BlockPos(insideX.first, headY + offset, insideZ.first)

                if (pos.isReplaceable) {
                    trapPositions.add(pos)
                }
            }
        }
    }

    /**
     * Finds a solid side neighbor to click against in order to fill [pos].
     */
    private fun findPlaceInfo(pos: BlockPos): PlaceTarget? {
        for (side in EnumFacing.entries) {
            val neighbor = pos.offset(side)

            if (neighbor.isReplaceable) {
                continue
            }

            val hitVec = neighbor.center.add(
                Vec3(
                    side.opposite.directionVec.x * 0.5,
                    side.opposite.directionVec.y * 0.5,
                    side.opposite.directionVec.z * 0.5
                )
            )

            if (raycast && options.rotationsActive) {
                val rotation = RotationUtils.toRotation(hitVec, false)
                val raytrace = performBlockRaytrace(rotation, mc.playerController.blockReachDistance) ?: continue

                if (raytrace.blockPos != neighbor) {
                    continue
                }
            }

            return PlaceTarget(neighbor, side.opposite, hitVec)
        }

        return null
    }

    private fun placeBlock(blockPos: BlockPos, side: EnumFacing, hitVec: Vec3): Boolean {
        val player = mc.thePlayer ?: return false

        var stack = player.inventorySlot(SilentHotbar.currentSlot + 36).stack

        if (stack == null || !isValidStack(stack)) {
            val blockSlot = findBlockInHotbar() ?: return false

            if (autoBlock != "Off") {
                SilentHotbar.selectSlotSilently(
                    this,
                    blockSlot,
                    immediate = true,
                    render = autoBlock == "Pick",
                    resetManually = true
                )
            }

            stack = player.inventorySlot(blockSlot + 36).stack ?: return false
        }

        val success = tryToPlaceBlock(stack, blockPos, side, hitVec)

        if (autoBlock == "Switch") {
            SilentHotbar.resetSlot(this, true)
        }

        switchBlockNextTickIfPossible(stack)

        if (success && trackCPS) {
            CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
        }

        return success
    }

    private fun isValidStack(stack: ItemStack): Boolean {
        val item = stack.item

        if (item !is ItemBlock || stack.stackSize <= 0) {
            return false
        }

        val block = item.block

        return block !in InventoryUtils.BLOCK_BLACKLIST && block !is BlockBush && block.isFullCube
    }

    private fun findBlockInHotbar(): Int? {
        val player = mc.thePlayer ?: return null

        return (36..44).firstOrNull {
            val stack = player.inventorySlot(it).stack ?: return@firstOrNull false
            isValidStack(stack)
        }?.minus(36)
    }

    private fun tryToPlaceBlock(stack: ItemStack, clickPos: BlockPos, side: EnumFacing, hitVec: Vec3): Boolean {
        val player = mc.thePlayer ?: return false

        val prevSize = stack.stackSize

        val clickedSuccessfully = player.onPlayerRightClick(clickPos, side, hitVec, stack)

        if (clickedSuccessfully) {
            if (swing) player.swingItem() else sendPacket(C0APacketAnimation())

            if (stack.stackSize <= 0) {
                player.inventory.mainInventory[SilentHotbar.currentSlot] = null
                ForgeEventFactory.onPlayerDestroyItem(player, stack)
            } else if (stack.stackSize != prevSize || mc.playerController.isInCreativeMode) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress()
            }
        } else {
            if (player.sendUseItem(stack)) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
            }
        }

        return clickedSuccessfully
    }

    private fun switchBlockNextTickIfPossible(stack: ItemStack) {
        if (autoBlock in arrayOf("Off", "Switch") || stack.stackSize > 0) {
            return
        }

        val switchSlot = findBlockInHotbar() ?: return

        nextTick {
            if (autoBlock != "Off") {
                SilentHotbar.selectSlotSilently(
                    this,
                    switchSlot,
                    immediate = true,
                    render = autoBlock == "Pick",
                    resetManually = true
                )
            }
        }
    }

    private fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val rotationVec = getVectorForRotation(rotation)

        val reach = eyes + (rotationVec * maxReach.toDouble())

        return world.rayTraceBlocks(eyes, reach, false, true, false)
    }

    private data class PlaceTarget(val blockPos: BlockPos, val enumFacing: EnumFacing, val vec3: Vec3)
}
