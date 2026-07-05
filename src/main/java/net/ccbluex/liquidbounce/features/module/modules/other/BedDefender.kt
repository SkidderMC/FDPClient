/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isBlockBBValid
import net.ccbluex.liquidbounce.utils.block.center
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
import net.minecraft.block.Block
import net.minecraft.block.BlockBush
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockEnderChest
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.ForgeEventFactory
import java.awt.Color

object BedDefender : Module("BedDefender", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
        .describe("How to switch to a block before placing.")
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when placing a block.")
    private val placeDelay by int("PlaceDelay", 500, 0..1000)
        .describe("Minimum delay between placements in milliseconds.")
    private val maxLayers by int("MaxLayers", 1, 1..5)
        .describe("How many layers of blocks to wrap the bed in.")
    private val allowChests by boolean("AllowChests", false)
        .describe("Allow chests to be used as defence blocks.")
    private val raycastMode by choices(
        "Raycast",
        arrayOf("None", "Normal", "Around"),
        "Normal"
    ) { options.rotationsActive }
        .describe("How placements are raycast against the target.")
    private val scannerMode by choices("Scanner", arrayOf("Nearest", "Random"), "Nearest")
        .describe("Choose the nearest or a random spot to fill.")

    private val options = RotationSettings(this).withRequestPriority(RotationPriority.HIGH)

    private val onSneakOnly by boolean("OnSneakOnly", true)
        .describe("Only defend the bed while sneaking.")
    private val autoSneak by choices("AutoSneak", arrayOf("Off", "Normal", "Packet"), "Off") { !onSneakOnly }
        .describe("Automatically sneak while placing blocks.")
    private val trackCPS by boolean("TrackCPS", false)
        .describe("Count each placement toward the CPS counter.")
    private val mark by boolean("Mark", false)
        .describe("Render a box around the targeted block.")

    private val placingGroup = Configurable("Placing")
    private val rotationsGroup = Configurable("Rotations")
    private val sneakGroup = Configurable("Sneak")
    private val miscGroup = Configurable("Misc")

    init {
        moveValues(placingGroup,
            "AutoBlock", "Swing", "PlaceDelay", "MaxLayers", "AllowChests", "Raycast", "Scanner")

        options.nestInto(rotationsGroup)

        moveValues(sneakGroup, "OnSneakOnly", "AutoSneak")
        moveValues(miscGroup, "TrackCPS", "Mark")

        addValues(listOf(placingGroup, rotationsGroup, sneakGroup, miscGroup))
    }
    private val defenceBlocks = mutableListOf<BlockPos>()
    private val bedTopPositions = mutableListOf<BlockPos>()
    private val bedBottomPositions = mutableListOf<BlockPos>()

    private val timerCounter = MSTimer()
    private var blockPosition: BlockPos? = null

    override fun onDisable() {
        RotationUtils.cancelTargetRotation(this)

        blockPosition = null
        defenceBlocks.clear()
        bedTopPositions.clear()
        bedBottomPositions.clear()

        val player = mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (player.isSneaking) player.isSneaking = false
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        if (onSneakOnly && !mc.gameSettings.keyBindSneak.isKeyDown) {
            return@handler
        }

        val radius = 4 + (maxLayers - 1)
        val posX = player.posX.toInt()
        val posY = player.posY.toInt()
        val posZ = player.posZ.toInt()

        bedTopPositions.clear()
        bedBottomPositions.clear()
        defenceBlocks.clear()

        // Get placing positions
        for (x in posX - radius..posX + radius) {
            for (y in posY - radius..posY + radius) {
                for (z in posZ - radius..posZ + radius) {
                    val blockPos = BlockPos(x, y, z)
                    val block = world.getBlockState(blockPos).block
                    if (block == Blocks.bed) {
                        val metadata = block.getMetaFromState(world.getBlockState(blockPos))

                        if (metadata >= 8) {
                            bedTopPositions.add(blockPos)
                        } else {
                            bedBottomPositions.add(blockPos)
                        }
                    }
                }
            }
        }

        addDefenceBlocks(bedTopPositions)
        addDefenceBlocks(bedBottomPositions)

        if (defenceBlocks.isNotEmpty()) {
            val playerPos = player.position ?: return@handler
            val pos = if (scannerMode == "Nearest") defenceBlocks.minByOrNull { it.distanceSq(playerPos) }
                ?: return@handler else defenceBlocks.random()
            val blockPos = BlockPos(pos.x.toDouble(), pos.y - player.eyeHeight + 1.5, pos.z.toDouble())
            val rotation = RotationUtils.toRotation(blockPos.center, false, player)
            val raytrace = performBlockRaytrace(rotation, mc.playerController.blockReachDistance) ?: return@handler

            if (options.rotationsActive) {
                setTargetRotation(rotation, options, if (options.keepRotation) options.resetTicks else 1)
            }

            blockPosition = blockPos

            if (timerCounter.hasTimePassed(placeDelay)) {
                if (!isPlaceablePos(blockPos)) return@handler

                when (autoSneak.lowercase()) {
                    "normal" -> mc.gameSettings.keyBindSneak.pressed = false
                    "packet" -> sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SNEAKING))
                }

                if (!options.rotationsActive ||
                    RotationUtils.rotationDifference(rotation) <= RotationUtils.getFixedAngleDelta()
                ) {
                    placeBlock(blockPos, raytrace.sideHit, raytrace.hitVec)
                    timerCounter.reset()
                }
            } else {
                when (autoSneak.lowercase()) {
                    "normal" -> mc.gameSettings.keyBindSneak.pressed = true
                    "packet" -> sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING))
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (mark && blockPosition != null) {
            val blockPos = BlockPos(blockPosition!!.x, blockPosition!!.y + 1, blockPosition!!.z)
            RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
            return@handler
        }
    }

    private fun addDefenceBlocks(bedPositions: List<BlockPos>) {
        var frontier = bedPositions

        repeat(maxLayers) {
            val nextFrontier = mutableListOf<BlockPos>()

            for (basePos in frontier) {
                val surroundingPositions = listOf(
                    basePos.up(),
                    basePos.north(),
                    basePos.south(),
                    basePos.east(),
                    basePos.west()
                )

                for (pos in surroundingPositions) {
                    if (pos in bedTopPositions || pos in bedBottomPositions || pos in defenceBlocks) {
                        continue
                    }

                    if (mc.theWorld.isAirBlock(pos)) {
                        defenceBlocks.add(pos)
                        nextFrontier.add(pos)
                    }
                }
            }

            frontier = nextFrontier
        }
    }

    private fun placeBlock(blockPos: BlockPos, side: EnumFacing, hitVec: Vec3) {
        val player = mc.thePlayer ?: return

        var stack = player.inventorySlot(SilentHotbar.currentSlot + 36).stack ?: return

        if (!isValidDefenceStack(stack)) {
            val blockSlot = findDefenceBlockInHotbar() ?: return

            if (autoBlock != "Off") {
                SilentHotbar.selectSlotSilently(
                    this,
                    blockSlot,
                    immediate = true,
                    render = autoBlock == "Pick",
                    resetManually = true
                )
            }

            stack = player.inventorySlot(blockSlot).stack
        }

        tryToPlaceBlock(stack, blockPos, side, hitVec)

        // Since we violate vanilla slot switch logic if we send the packets now, we arrange them for the next tick
        if (autoBlock == "Switch")
            SilentHotbar.resetSlot(this, true)

        switchBlockNextTickIfPossible(stack)

        if (trackCPS) {
            CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
        }
    }

    private fun isChestBlock(block: Block) = block is BlockChest || block is BlockEnderChest

    private fun isValidDefenceStack(stack: ItemStack): Boolean {
        val item = stack.item
        if (item !is ItemBlock || stack.stackSize <= 0) return false

        val block = item.block
        if (block is BlockBush) return false

        if (block in InventoryUtils.BLOCK_BLACKLIST) {
            return allowChests && isChestBlock(block)
        }

        return true
    }

    private fun findDefenceBlockInHotbar(): Int? {
        val player = mc.thePlayer ?: return null
        val inventory = player.openContainer

        return (36..44).filter {
            val stack = inventory.getSlot(it).stack ?: return@filter false
            isValidDefenceStack(stack)
        }.minByOrNull {
            (inventory.getSlot(it).stack.item as ItemBlock).block.isFullCube
        }?.minus(36)
    }

    private fun tryToPlaceBlock(
        stack: ItemStack,
        clickPos: BlockPos,
        side: EnumFacing,
        hitVec: Vec3,
    ): Boolean {
        val player = mc.thePlayer ?: return false

        val prevSize = stack.stackSize

        val clickedSuccessfully = player.onPlayerRightClick(clickPos, side, hitVec, stack)

        if (clickedSuccessfully) {
            if (swing) player.swingItem() else sendPacket(C0APacketAnimation())

            if (stack.stackSize <= 0) {
                player.inventory.mainInventory[SilentHotbar.currentSlot] = null
                ForgeEventFactory.onPlayerDestroyItem(player, stack)
            } else if (stack.stackSize != prevSize || mc.playerController.isInCreativeMode)
                mc.entityRenderer.itemRenderer.resetEquippedProgress()

            blockPosition = null
        } else {
            if (player.sendUseItem(stack))
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
        }

        return clickedSuccessfully
    }

    private fun isPlaceablePos(pos: BlockPos): Boolean {
        val player = mc.thePlayer ?: return false
        val world = mc.theWorld ?: return false

        return when (raycastMode.lowercase()) {
            "normal" -> {
                val eyesPos = player.eyes
                val movingObjectPosition = world.rayTraceBlocks(eyesPos, pos.center, false, true, false)

                movingObjectPosition != null && movingObjectPosition.blockPos == pos
            }

            "around" -> EnumFacing.entries.any { !isBlockBBValid(pos.offset(it)) }

            else -> true
        }
    }

    private fun switchBlockNextTickIfPossible(stack: ItemStack) {
        if (autoBlock in arrayOf("Off", "Switch") || stack.stackSize > 0)
            return

        val switchSlot = findDefenceBlockInHotbar() ?: return

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
}
