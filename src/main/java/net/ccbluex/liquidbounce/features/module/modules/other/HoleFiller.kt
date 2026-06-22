/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
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
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.ForgeEventFactory
import java.awt.Color

/**
 * Module HoleFiller
 *
 * Automatically fills the open sides of a hole the player is standing in (or near)
 * with blocks, so that you can't be knocked into it.
 */
object HoleFiller : Module("HoleFiller", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to a block before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing a block.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Minimum delay between placements in milliseconds.")

    // The horizontal radius (in blocks) around the player's feet that is scanned for the hole.
    private val area by int("Area", 1, 1..3)
        .describe("Horizontal radius scanned around the feet.")

    // How many blocks may be filled within a single tick.
    private val maxPlacements by int("MaxPlacements", 1, 1..4)
        .describe("Maximum blocks filled within a single tick.")

    // Only operate when you're actually standing inside a hole.
    private val onlyWhenInHole by boolean("OnlyWhenInHole", true)
        .describe("Only fill while standing inside a hole.")

    // Verify the candidate spot is reachable by raytrace before placing.
    private val raycast by boolean("Raycast", true) { options.rotationsActive }
        .describe("Verify the spot is reachable by raytrace before placing.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count each placement toward the CPS counter.")
    private val mark by boolean("Mark", false)
        .describe("Render a box around each fill position.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val timerCounter = MSTimer()

    private val fillPositions = mutableListOf<BlockPos>()
    private var targetPosition: BlockPos? = null

    override fun onDisable() {
        fillPositions.clear()
        targetPosition = null
        SilentHotbar.resetSlot(this)
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        fillPositions.clear()
        targetPosition = null

        val feet = BlockPos(player.posX, player.posY, player.posZ)

        if (onlyWhenInHole && !isHole(feet)) {
            return@handler
        }

        collectFillPositions(feet)

        if (fillPositions.isEmpty()) {
            return@handler
        }

        val playerPos = player.position ?: return@handler
        val nearest = fillPositions.minByOrNull { it.distanceSq(playerPos) } ?: return@handler

        targetPosition = nearest

        val placeInfo = findPlaceInfo(nearest) ?: return@handler

        val rotation = RotationUtils.toRotation(placeInfo.vec3, false, player)

        if (options.rotationsActive) {
            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
        }

        if (!timerCounter.hasTimePassed(placeDelay)) {
            return@handler
        }

        var placed = 0

        for (pos in fillPositions.sortedBy { it.distanceSq(playerPos) }) {
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

        for (pos in fillPositions) {
            RenderUtils.drawBlockBox(pos, Color(68, 117, 255, 100), false)
        }
    }

    /**
     * A position counts as a hole if its own block can be filled and every horizontal side
     * around it is solid, so the player's feet sit inside a one-block gap.
     */
    private fun isHole(feet: BlockPos): Boolean {
        if (!feet.isReplaceable) {
            return false
        }

        return horizontalFacings.all { !feet.offset(it).isReplaceable }
    }

    private fun collectFillPositions(feet: BlockPos) {
        for (x in -area..area) {
            for (z in -area..area) {
                val pos = feet.add(x, 0, z)

                if (!pos.isReplaceable) {
                    continue
                }

                // The block beneath must be open (a hole), and there must be a solid neighbor to place against.
                if (!pos.down().isReplaceable) {
                    continue
                }

                if (horizontalFacings.any { !pos.offset(it).isReplaceable } || !pos.down().down().isReplaceable) {
                    fillPositions.add(pos)
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
