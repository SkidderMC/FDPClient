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
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura.target
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.attack.EntityUtils
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
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Module BlockIn
 *
 * Encloses the combat target by placing blocks on the open sides directly around it,
 * trapping the entity inside a wall of blocks.
 */
object BlockIn : Module("BlockIn", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, gameDetecting = false) {

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to the block before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing blocks.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Delay between block placements.")

    // Maximum distance (in blocks) to the target before BlockIn engages.
    private val maxDistance by float("MaxDistance", 4.5f, 1f..6f)
        .describe("Max distance to the target before it engages.")

    // Whether the upper ring (target's head level) is enclosed too.
    private val coverHead by boolean("CoverHead", true)
        .describe("Also enclose the target head level.")

    // How many blocks may be placed within a single tick.
    private val maxPlacements by int("MaxPlacements", 1, 1..4)
        .describe("Max blocks placed in a single tick.")

    // Verify the candidate spot is reachable by raytrace before placing.
    private val raycast by boolean("Raycast", true) { options.rotationsActive }
        .describe("Verify the spot is reachable before placing.")

    // Fall back to the nearest selectable enemy when KillAura has no current target.
    private val standaloneTarget by boolean("StandaloneTarget", true)
        .describe("Target nearest enemy when KillAura has none.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count placements toward the CPS counter.")
    private val mark by boolean("Mark", true)
        .describe("Render boxes on planned place positions.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val timerCounter = MSTimer()

    private val placePositions = mutableListOf<BlockPos>()

    override fun onDisable() {
        placePositions.clear()
        SilentHotbar.resetSlot(this)
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        placePositions.clear()

        val enemy = resolveTarget() ?: return@handler

        if (player.getDistanceToEntity(enemy) > maxDistance) {
            return@handler
        }

        collectPlacePositions(enemy)

        if (placePositions.isEmpty()) {
            return@handler
        }

        val playerPos = player.position ?: return@handler
        val ordered = placePositions.sortedBy { it.distanceSq(playerPos) }

        val first = ordered.first()
        findPlaceInfo(first)?.let { info ->
            val rotation = RotationUtils.toRotation(info.vec3, false, player)

            if (options.rotationsActive) {
                setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
            }
        }

        if (!timerCounter.hasTimePassed(placeDelay)) {
            return@handler
        }

        var placed = 0

        for (pos in ordered) {
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

        for (pos in placePositions) {
            RenderUtils.drawBlockBox(pos, Color(255, 71, 71, 100), false)
        }
    }

    /**
     * Returns the current combat target, falling back to the nearest selectable enemy
     * when [standaloneTarget] is enabled and KillAura is not currently locked on.
     */
    private fun resolveTarget(): EntityLivingBase? {
        target?.takeIf { it.isEntityAlive }?.let { return it }

        if (!standaloneTarget) {
            return null
        }

        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        return world.loadedEntityList
            .filterIsInstance<EntityLivingBase>()
            .filter { EntityUtils.isSelected(it, true) }
            .minByOrNull { player.getDistanceToEntity(it) }
    }

    /**
     * Collects every open block position that makes up the ring around the target at each
     * occupied level, so the entity ends up walled in.
     */
    private fun collectPlacePositions(enemy: EntityLivingBase) {
        val box = enemy.entityBoundingBox

        val minX = floor(box.minX).toInt()
        val maxX = ceil(box.maxX).toInt() - 1
        val minZ = floor(box.minZ).toInt()
        val maxZ = ceil(box.maxZ).toInt() - 1
        val minY = floor(box.minY).toInt()
        val maxY = if (coverHead) ceil(box.maxY).toInt() - 1 else minY

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                for (z in minZ..maxZ) {
                    val column = BlockPos(x, y, z)

                    for (side in horizontalFacings) {
                        val ringPos = column.offset(side)

                        // Skip spots inside the target's own footprint.
                        if (ringPos.x in minX..maxX && ringPos.z in minZ..maxZ) {
                            continue
                        }

                        if (!ringPos.isReplaceable) {
                            continue
                        }

                        if (ringPos !in placePositions) {
                            placePositions.add(ringPos)
                        }
                    }
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

    private val horizontalFacings = EnumFacing.entries.filter { it.axis != EnumFacing.Axis.Y }

    private data class PlaceTarget(val blockPos: BlockPos, val enumFacing: EnumFacing, val vec3: Vec3)
}
