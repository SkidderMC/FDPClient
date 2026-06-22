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
 * Module AutoBuild
 *
 * Places blocks following a small predefined pattern around the player
 * (a box that walls you in, or a plus/nucleus on the ground) using the
 * same placement path as HoleFiller.
 */
object AutoBuild : Module("AutoBuild", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val pattern by choices("Pattern", arrayOf("Box", "Nucleus", "Floor"), "Box")
        .describe("Block shape to build around the player.")

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to a block before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing a block.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Minimum delay between placements in milliseconds.")

    // How many blocks may be placed within a single tick.
    private val maxPlacements by int("MaxPlacements", 1, 1..4)
        .describe("Maximum blocks placed within a single tick.")

    // Verify the candidate spot is reachable by raytrace before placing.
    private val raycast by boolean("Raycast", true) { options.rotationsActive }
        .describe("Verify the spot is reachable by raytrace before placing.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count each placement toward the CPS counter.")
    private val mark by boolean("Mark", true)
        .describe("Render a box around each build position.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val timerCounter = MSTimer()

    private val buildPositions = mutableListOf<BlockPos>()

    override fun onDisable() {
        buildPositions.clear()
        SilentHotbar.resetSlot(this)
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        buildPositions.clear()

        val feet = BlockPos(player.posX, player.posY, player.posZ)

        collectBuildPositions(feet)

        if (buildPositions.isEmpty()) {
            return@handler
        }

        val playerPos = player.position ?: return@handler
        val nearest = buildPositions.minByOrNull { it.distanceSq(playerPos) } ?: return@handler

        val placeInfo = findPlaceInfo(nearest) ?: return@handler

        val rotation = RotationUtils.toRotation(placeInfo.vec3, false, player)

        if (options.rotationsActive) {
            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
        }

        if (!timerCounter.hasTimePassed(placeDelay)) {
            return@handler
        }

        var placed = 0

        for (pos in buildPositions.sortedBy { it.distanceSq(playerPos) }) {
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

        for (pos in buildPositions) {
            RenderUtils.drawBlockBox(pos, Color(68, 117, 255, 100), false)
        }
    }

    /**
     * Builds the list of positions that make up the selected pattern around the player's feet.
     * Only replaceable spots that have at least one solid neighbor to click against are kept.
     */
    private fun collectBuildPositions(feet: BlockPos) {
        val offsets = when (pattern) {
            // Four walls at feet and head height, leaving the player boxed in.
            "Box" -> buildList {
                for (y in 0..1) {
                    for (side in horizontalFacings) {
                        add(feet.add(side.directionVec.x, y, side.directionVec.z))
                    }
                }
            }
            // Plus/nucleus shape on the ground around the player's feet.
            "Nucleus" -> horizontalFacings.map { feet.down().offset(it) }
            // A full 3x3 floor underneath the player.
            else -> buildList {
                for (x in -1..1) {
                    for (z in -1..1) {
                        add(feet.add(x, -1, z))
                    }
                }
            }
        }

        for (pos in offsets) {
            if (!pos.isReplaceable) {
                continue
            }

            // Needs at least one solid neighbor to place against.
            if (EnumFacing.entries.any { !pos.offset(it).isReplaceable }) {
                buildPositions.add(pos)
            }
        }
    }

    /**
     * Finds a solid side neighbor to click against in order to place at [pos].
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
