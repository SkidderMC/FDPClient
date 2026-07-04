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
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockLiquid
import net.minecraft.block.material.Material
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
 * Module LiquidPlace
 *
 * Lets you place a held block directly onto/against a liquid surface (water or lava) by
 * clicking against the liquid block you are aiming at. The look ray is traced with the
 * stop-on-liquid flag so the targeted liquid block can be resolved and used as the click face.
 */
object LiquidPlace : Module("LiquidPlace", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    // Only act while the use-item key is held (closest to vanilla feel). When off, places continuously.
    private val onlyOnUseKey by boolean("OnlyOnUseKey", true)
        .describe("Only place while the use-item key is held down.")

    private val water by boolean("Water", true)
        .describe("Allow placing against water.")
    private val lava by boolean("Lava", false)
        .describe("Allow placing against lava.")

    // Only place against full (source) liquid blocks, avoiding partial flowing edges.
    private val onlySource by boolean("OnlySource", true)
        .describe("Only place against full source liquid blocks.")

    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing a block.")
    private val placeDelay by int("PlaceDelay", 250, 0..1000)
        .describe("Minimum delay between placements in milliseconds.")

    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count each placement toward the CPS counter.")
    private val mark by boolean("Mark", false)
        .describe("Render a box around the targeted liquid block.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val timerCounter = MSTimer()

    private var targetPosition: BlockPos? = null

    init {
        group("Liquids", "Water", "Lava", "OnlySource")
        group("Placement", "OnlyOnUseKey", "Swing", "PlaceDelay")
        group("Visuals", "TrackCPS", "Mark")
    }

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

        val target = findLiquidTarget() ?: return@handler

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
     * Traces the look ray with the stop-on-liquid flag so the liquid block being aimed at is
     * returned, then keeps it only if it is an allowed liquid type. The click face and hit-vec
     * come straight from the ray-trace result so the placement targets the liquid surface.
     */
    private fun findLiquidTarget(): PlaceTarget? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val maxReach = mc.playerController.blockReachDistance.toDouble()
        val rotation = RotationUtils.currentRotation ?: player.rotation

        val eyes = player.eyes
        val rotationVec = RotationUtils.getVectorForRotation(rotation)

        val reach = eyes + (rotationVec * maxReach)

        val raytrace: MovingObjectPosition = world.rayTraceBlocks(eyes, reach, true, false, false) ?: return null

        if (raytrace.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return null
        }

        val blockPos = raytrace.blockPos ?: return null

        if (!isLiquidTarget(blockPos)) {
            return null
        }

        return PlaceTarget(blockPos, raytrace.sideHit, raytrace.hitVec)
    }

    private fun isLiquidTarget(pos: BlockPos): Boolean {
        val state = pos.state ?: return false
        val block = state.block

        if (block !is BlockLiquid) {
            return false
        }

        val material = block.material

        val allowed = (water && material == Material.water) || (lava && material == Material.lava)

        if (!allowed) {
            return false
        }

        // Liquid level 0 means a full source block; flowing liquids carry a higher level.
        if (onlySource && state.getValue(BlockLiquid.LEVEL) != 0) {
            return false
        }

        return true
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
