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
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockBush
import net.minecraft.block.material.Material
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.event.ForgeEventFactory
import java.awt.Color

/**
 * Module AirPlace
 *
 * Lets you place blocks in mid-air against an empty (air) position the player is aiming at,
 * even when there is no supporting block to click against. The placement packet is sent against
 * the targeted air block, producing a "ghost" placement.
 */
object AirPlace : Module("AirPlace", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    // Only act while the use-item key is held (closest to vanilla feel). When off, places continuously.
    private val onlyOnUseKey by boolean("OnlyOnUseKey", true)
        .describe("Only place while the use-item key is held down.")

    // Allow placing into a position that is replaceable (tall grass, snow layer) as well as plain air.
    private val placeInReplaceable by boolean("PlaceInReplaceable", true)
        .describe("Allow placing into replaceable blocks like tall grass.")

    // How far in front of the eyes the ghost block is targeted (capped by the reach distance).
    private val distance by float("Distance", 3f, 1f..5f)
        .describe("How far in front of the eyes the ghost block is placed.")

    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing a block.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Minimum delay between placements in milliseconds.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count each placement toward the CPS counter.")
    private val mark by boolean("Mark", false)
        .describe("Render a box around the targeted air position.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val timerCounter = MSTimer()

    private var targetPosition: BlockPos? = null

    override fun onDisable() {
        RotationUtils.cancelTargetRotation(this)
                targetPosition = null
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        targetPosition = null

        if (onlyOnUseKey && !mc.gameSettings.keyBindUseItem.isKeyDown) {
            return@handler
        }

        if (!isHoldingBlock()) {
            return@handler
        }

        val target = findAirTarget() ?: return@handler

        targetPosition = target.blockPos

        if (options.rotationsActive) {
            val rotation = toRotation(target.vec3, false, player)
            setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
        }

        if (!timerCounter.hasTimePassed(placeDelay)) {
            return@handler
        }

        if (placeBlock(target.blockPos, target.enumFacing, target.vec3)) {
            timerCounter.reset()
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!mark) {
            return@handler
        }

        targetPosition?.let {
            RenderUtils.drawBlockBox(it, Color(68, 117, 255, 100), false)
        }
    }

    /**
     * Resolves the empty position the player is aiming at and the side/hit-vec used for the placement.
     *
     * Vanilla ray-tracing returns nothing when the look vector only passes through air, so the target
     * point is derived geometrically along the look direction. The packet then clicks that air position
     * with the facing pointing back toward the player's eyes, producing a ghost placement.
     */
    private fun findAirTarget(): PlaceTarget? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val maxReach = mc.playerController.blockReachDistance
        val rotation = RotationUtils.currentRotation ?: player.rotation

        val eyes = player.eyes
        val rotationVec = getVectorForRotation(rotation)

        val reach = distance.coerceAtMost(maxReach).toDouble()
        val hitVec = eyes + (rotationVec * reach)

        // Reject if a solid block blocks the path: that is normal placement, not a ghost placement.
        if (world.rayTraceBlocks(eyes, hitVec, false, true, false) != null) {
            return null
        }

        val blockPos = BlockPos(hitVec)

        if (!isEmptyTarget(blockPos)) {
            return null
        }

        // Face back toward the player so the click vec stays inside reach.
        val side = sideTowardPlayer(blockPos)

        return PlaceTarget(blockPos, side, hitVec)
    }

    private fun isEmptyTarget(pos: BlockPos): Boolean {
        val state = pos.state ?: return false
        val material = state.block.material

        return material.isReplaceable && (placeInReplaceable || material == Material.air)
    }

    private fun sideTowardPlayer(pos: BlockPos): EnumFacing {
        val player = mc.thePlayer ?: return EnumFacing.UP
        val diff = player.eyes.subtract(pos.center)

        val ax = Math.abs(diff.xCoord)
        val ay = Math.abs(diff.yCoord)
        val az = Math.abs(diff.zCoord)

        return when {
            ay >= ax && ay >= az -> if (diff.yCoord >= 0) EnumFacing.UP else EnumFacing.DOWN
            ax >= az -> if (diff.xCoord >= 0) EnumFacing.EAST else EnumFacing.WEST
            else -> if (diff.zCoord >= 0) EnumFacing.SOUTH else EnumFacing.NORTH
        }
    }

    private fun isHoldingBlock(): Boolean {
        val stack = mc.thePlayer?.heldItem ?: return false
        return isValidStack(stack)
    }

    private fun placeBlock(blockPos: BlockPos, side: EnumFacing, hitVec: Vec3): Boolean {
        val player = mc.thePlayer ?: return false

        val stack = player.inventorySlot(SilentHotbar.currentSlot + 36).stack ?: return false

        if (!isValidStack(stack)) {
            return false
        }

        val success = tryToPlaceBlock(stack, blockPos, side, hitVec)

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

        return block !in InventoryUtils.BLOCK_BLACKLIST && block !is BlockBush
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

    private data class PlaceTarget(val blockPos: BlockPos, val enumFacing: EnumFacing, val vec3: Vec3)
}
