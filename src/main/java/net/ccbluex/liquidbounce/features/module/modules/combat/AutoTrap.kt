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
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.isReplaceable
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Blocks
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
 * Module AutoTrap
 *
 * Places a cobweb onto the combat target's position so they get stuck in place,
 * leaving them unable to retreat while you keep hitting them.
 */
object AutoTrap : Module("AutoTrap", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, gameDetecting = false) {

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to the web before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing webs.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Delay between web placements.")

    // The horizontal reach (in blocks) at which a target may be trapped.
    private val range by float("Range", 4.5f, 1f..6f)
        .describe("Max distance to trap a target.")

    // Also web the layer above the feet so a tall target can't simply step out of a single web.
    private val trapLegs by boolean("TrapLegs", true)
        .describe("Also web the block above the feet.")

    // Verify the candidate spot is reachable by raytrace before placing.
    private val raycast by boolean("Raycast", true) { options.rotationsActive }
        .describe("Verify the spot is reachable before placing.")

    private val mark by boolean("Mark", false)
        .describe("Render boxes on planned trap positions.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

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

        val webSlot = findWebInHotbar() ?: return@handler

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

        val info = findPlaceInfo(nearest) ?: return@handler

        if (placeBlock(webSlot, info.blockPos, info.enumFacing, info.vec3)) {
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
     * Collects the cells the cobweb should occupy: the cell the [target] stands in
     * (and optionally the cell above it) when they are still air and reachable.
     */
    private fun collectTrapPositions(target: EntityLivingBase) {
        val feet = target.position ?: return

        val candidates = mutableListOf(feet)

        if (trapLegs) {
            candidates.add(feet.up())
        }

        for (pos in candidates) {
            if (pos.block !== Blocks.web && pos.isReplaceable) {
                trapPositions.add(pos)
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

    private fun placeBlock(webSlot: Int, blockPos: BlockPos, side: EnumFacing, hitVec: Vec3): Boolean {
        val player = mc.thePlayer ?: return false

        if (autoBlock != "Off") {
            SilentHotbar.selectSlotSilently(
                this,
                webSlot,
                immediate = true,
                render = autoBlock == "Pick",
                resetManually = true
            )
        }

        val stack = player.hotBarSlot(SilentHotbar.currentSlot).stack ?: return false

        if (!isWebStack(stack)) {
            return false
        }

        val success = tryToPlaceBlock(stack, blockPos, side, hitVec)

        if (autoBlock == "Switch" || autoBlock == "Spoof") {
            SilentHotbar.resetSlot(this, true)
        }

        return success
    }

    private fun isWebStack(stack: ItemStack): Boolean {
        val item = stack.item
        return item is ItemBlock && item.block === Blocks.web && stack.stackSize > 0
    }

    private fun findWebInHotbar(): Int? {
        val player = mc.thePlayer ?: return null

        return (0..8).firstOrNull {
            val stack = player.hotBarSlot(it).stack ?: return@firstOrNull false
            isWebStack(stack)
        }
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
