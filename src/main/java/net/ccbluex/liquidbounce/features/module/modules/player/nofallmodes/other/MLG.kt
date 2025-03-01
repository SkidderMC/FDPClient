/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.autoMLG
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.currentMlgBlock
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.maxRetrievalWaitingTime
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.options
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.retrieveDelay
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.retrievingPos
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.swing
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.*
import net.minecraftforge.event.ForgeEventFactory
import kotlin.math.min

object MLG : NoFallMode("MLG") {

    private val mlgSlot
        get() = findMlgSlot()

    private val currRotation
        get() = RotationUtils.currentRotation ?: RotationUtils.serverRotation

    override fun onRotationUpdate() {
        val player = mc.thePlayer ?: return

        retrievingPos?.let {
            if (player.hotBarSlot(SilentHotbar.currentSlot).stack?.item != Items.bucket) {
                retrievingPos = null
                return@let
            }

            RotationUtils.setTargetRotation(
                toRotation(it),
                options,
                if (options.keepRotation) options.resetTicks else 1
            )
        }

        mlgSlot ?: return

        currentMlgBlock = null

        val reach = mc.playerController.blockReachDistance

        if (player.fallDistance >= NoFall.minFallDistance) {
            SimulatedPlayer.fromClientPlayer(RotationUtils.modifiedInput).let { sim ->
                sim.rotationYaw = currRotation.yaw

                var suitablePos: BlockPos? = null

                for (i in 1..40) {
                    sim.tick()

                    val pos = BlockPos(sim.pos).down()

                    if (sim.fallDistance == 0F) {
                        var bestOffset: Vec3i? = null
                        var minDistance = Double.MAX_VALUE

                        for (x in -1..1) {
                            for (z in -1..1) {
                                val offset = Vec3i(x, 0, z)
                                val neighbor = pos.add(offset)
                                val center = neighbor.center

                                val raytrace = Backtrack.runWithSimulatedPosition(player, sim.pos) {
                                    performBlockRaytrace(toRotation(center), reach)
                                }

                                if (raytrace?.let { it.blockPos == neighbor && it.sideHit == EnumFacing.UP } == true) {
                                    val distance = BlockPos(sim.pos).distanceSq(neighbor)

                                    if (distance <= minDistance) {
                                        minDistance = distance
                                        bestOffset = offset
                                    }
                                }
                            }
                        }

                        bestOffset?.let {
                            suitablePos = pos.add(it)

                            if (suitablePos?.state?.block == Blocks.web ||
                                suitablePos?.up()?.block == Blocks.water
                            ) {
                                return
                            }
                        }

                        if (suitablePos != null) {
                            break
                        }
                    }
                }

                suitablePos
            }?.also { currentMlgBlock = it }?.let { pos ->
                // The higher the fall distance, the greater the focus to the center of the block
                val inc = 0.2 * min(player.fallDistance / 30F, 1F)

                faceBlock(pos, targetUpperFace = true, hRange = 0.3 + inc..0.701 - inc)?.run {
                    RotationUtils.setTargetRotation(
                        rotation, options, if (options.keepRotation) options.resetTicks else 1
                    )
                }
            }

        }
    }

    override fun onTick() {
        val player = mc.thePlayer ?: return
        val target = currentMlgBlock ?: run {
            // If the slot was modified but rotations did not reach the target spot in time, reset the slot
            if (retrievingPos == null) {
                SilentHotbar.resetSlot(this)
            }

            return
        }

        val reach = mc.playerController.blockReachDistance

        val stack = mlgSlot?.let {
            if (retrievingPos != null) return@let null

            SilentHotbar.selectSlotSilently(this, it, render = autoMLG == "Pick", resetManually = true)

            player.hotBarSlot(it).stack
        } ?: return

        val item = stack.item ?: return

        val wasWaterBucket = item == Items.water_bucket

        if (wasWaterBucket || (item as? ItemBlock)?.block == Blocks.web) {
            performBlockRaytrace(currRotation, reach)?.let {
                if (it.blockPos != target || it.sideHit != EnumFacing.UP) {
                    return@let
                }

                placeBlock(it.blockPos, it.sideHit, it.hitVec, stack, !wasWaterBucket) {
                    if (!wasWaterBucket) {
                        currentMlgBlock = null
                        retrievingPos = null
                    }
                }

                if (wasWaterBucket) {
                    val placePos = target.center.withY(0.5, true)

                    retrievingPos = placePos

                    WaitTickUtils.conditionalSchedule(this, maxRetrievalWaitingTime) { elapsedTicks ->
                        val newStack =
                            player.hotBarSlot(SilentHotbar.currentSlot).stack ?: return@conditionalSchedule null

                        if (newStack.item == Items.bucket) {
                            findMlgSlot(true)?.let { slot ->
                                SilentHotbar.selectSlotSilently(
                                    this, slot, render = autoMLG == "Pick", resetManually = true
                                )
                            } ?: run {
                                reset()

                                return@conditionalSchedule null
                            }
                        }

                        val block = target.state?.block

                        // Are we too far away from the block?
                        if (block == null || player.getDistanceToBox(
                                block.getSelectedBoundingBox(mc.theWorld, target)
                            ) > reach
                        ) {
                            reset()

                            return@conditionalSchedule null
                        }

                        if (player.fallDistance == 0F) {
                            val raytrace = performBlockRaytrace(currRotation, reach)
                            // Did the user decide to look somewhere else?
                            if (raytrace == null || raytrace.blockPos != target || raytrace.sideHit != EnumFacing.UP) {
                                // Reset the rotation if it took more than the max retrieval waiting time to retrieve
                                reset(elapsedTicks >= maxRetrievalWaitingTime)
                                return@conditionalSchedule null
                            }

                            // We are looking at the target block, now make sure the time has passed to retrieve
                            if (elapsedTicks < retrieveDelay) return@conditionalSchedule false

                            // Time to retrieve
                            placeBlock(it.blockPos, it.sideHit, it.hitVec, newStack)

                            reset()

                            return@conditionalSchedule true
                        }

                        return@conditionalSchedule false
                    }
                }
            }
        }
    }

    private fun reset(complete: Boolean = true) {
        // Reset target information
        currentMlgBlock = null

        if (complete) {
            retrievingPos = null

            SilentHotbar.resetSlot(this)
        }
    }

    private inline fun placeBlock(
        blockPos: BlockPos,
        side: EnumFacing,
        hitVec: Vec3,
        stack: ItemStack,
        finalStage: Boolean = true,
        onSuccess: () -> Unit = { }
    ) {
        tryToPlaceBlock(stack, blockPos, side, hitVec, onSuccess)

        if (finalStage) {
            switchBlockNextTickIfPossible(stack)
        }
    }

    private inline fun tryToPlaceBlock(
        stack: ItemStack, clickPos: BlockPos, side: EnumFacing, hitVec: Vec3, onSuccess: () -> Unit
    ): Boolean {
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

            onSuccess()
        } else {
            if (player.sendUseItem(stack)) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()

                onSuccess()
            }
        }

        return clickedSuccessfully
    }

    private fun switchBlockNextTickIfPossible(stack: ItemStack) {
        if (autoMLG == "Off" || stack.stackSize > 0) return

        val switchSlot = findMlgSlot() ?: return

        SilentHotbar.selectSlotSilently(this, switchSlot, render = autoMLG == "Pick", resetManually = true)
    }

    private fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val rotationVec = getVectorForRotation(rotation)

        val reach = eyes + (rotationVec * maxReach.toDouble())

        return world.rayTraceBlocks(eyes, reach, false, true, false)
    }

    private fun findMlgSlot(onlyBucket: Boolean = false): Int? {
        val player = mc.thePlayer ?: return null

        val bucket = if (onlyBucket) Items.bucket else Items.water_bucket

        player.hotBarSlot(SilentHotbar.currentSlot).stack?.item.let {
            // Already have required item? Why change slot?
            if (it == bucket || (it as? ItemBlock)?.block in arrayOf(Blocks.web)) {
                return SilentHotbar.currentSlot
            }
        }

        for (i in 36..44) {
            val item = player.inventorySlot(i).stack?.item ?: continue

            if (item == bucket || !onlyBucket && (item as? ItemBlock)?.block in arrayOf(Blocks.web)) {
                return i - 36
            }
        }

        return null
    }
}