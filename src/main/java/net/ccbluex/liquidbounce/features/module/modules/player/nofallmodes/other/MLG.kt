/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.autoMLG
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.bucketUsed
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.currentMlgBlock
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.mlgInProgress
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.mlgRotation
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.options
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.retrieveDelay
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.shouldUse
import net.ccbluex.liquidbounce.features.module.modules.player.NoFall.swing
import net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes.NoFallMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.ccbluex.liquidbounce.utils.movement.FallingPlayer
import net.ccbluex.liquidbounce.utils.timing.TickedActions
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.minecraft.block.BlockWeb
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucket
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.event.ForgeEventFactory
import kotlin.math.ceil

object MLG : NoFallMode("MLG") {

    override fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return
        val mlgSlot = findMlgSlot() ?: return

        if (event.eventState != EventState.POST) return

        val fallingPlayer = FallingPlayer(player)
        val maxDist = mc.playerController.blockReachDistance + 1.5
        val collision = fallingPlayer.findCollision(ceil(1.0 / player.motionY * -maxDist).toInt()) ?: return

        // There's gotta be a better way of doing this
        if (player.motionY < collision.pos.y + 1 - player.posY || player.eyes.distanceTo(Vec3(collision.pos).addVector(
                0.5,
                0.5,
                0.5
            )
            ) < mc.playerController.blockReachDistance + 0.866025) {
            if (player.fallDistance < NoFall.minFallDistance) return
            currentMlgBlock = collision.pos

            if (autoMLG != "Off") {
                SilentHotbar.selectSlotSilently(this, mlgSlot, immediate = true, render = autoMLG == "Pick", resetManually = true)
            }

            currentMlgBlock?.toVec()?.let { RotationUtils.toRotation(it, false, player) }?.run {
                if (options.rotationsActive) {
                    RotationUtils.setTargetRotation(this, options, if (options.keepRotation) options.resetTicks else 1)
                }

                mlgRotation = this
                shouldUse = true
            }
        }
    }

    override fun onTick() {
        val player = mc.thePlayer ?: return
        val mlgSlot = findMlgSlot()
        val stack = mlgSlot?.let { player.hotBarSlot(it).stack } ?: return

        if (shouldUse && !bucketUsed) {
            TickedActions.TickScheduler(NoFall) += {
                when (stack.item) {
                    Items.water_bucket -> {
                        player.sendUseItem(stack)
                    }

                    is ItemBlock -> {
                        val blocks = (stack.item as ItemBlock).block
                        if (blocks is BlockWeb) {
                            val raytrace = performBlockRaytrace(mlgRotation?.fixedSensitivity()!!,
                                mc.playerController.blockReachDistance
                            )

                            if (raytrace != null) {
                                currentMlgBlock?.let { placeBlock(it, raytrace.sideHit, raytrace.hitVec, stack) }
                            }
                        }
                    }
                }
            }

            mlgInProgress = true
            bucketUsed = true
        }

        if (shouldUse) {
            WaitTickUtils.schedule(retrieveDelay) {
                if (!shouldUse) return@schedule // Without this, it'll retrieve twice IDK.

                if (stack.item is ItemBucket) {
                    player.sendUseItem(stack)
                }

                shouldUse = false
            }
        }

        if (mlgInProgress && !shouldUse) {
            WaitTickUtils.schedule(retrieveDelay + 2) {
                SilentHotbar.resetSlot(this)

                mlgInProgress = false
                bucketUsed = false
            }
        }
    }

    private fun placeBlock(blockPos: BlockPos, side: EnumFacing, hitVec: Vec3, stack: ItemStack) {
        tryToPlaceBlock(stack, blockPos, side, hitVec)

        // Since we violate vanilla slot switch logic if we send the packets now, we arrange them for the next tick
        if (autoMLG == "Switch")
            SilentHotbar.resetSlot(this)

        switchBlockNextTickIfPossible(stack)
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

            currentMlgBlock = null
            mlgRotation = null
        } else {
            if (player.sendUseItem(stack))
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
        }

        return clickedSuccessfully
    }

    private fun switchBlockNextTickIfPossible(stack: ItemStack) {
        if (autoMLG in arrayOf("Off", "Switch") || stack.stackSize > 0)
            return

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

    private fun findMlgSlot(): Int? {
        val player = mc.thePlayer ?: return null

        for (i in 36..44) {
            val itemStack = player.inventorySlot(i).stack ?: continue

            if (itemStack.item == Items.water_bucket ||
                (itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.web)) {
                return i - 36
            }
        }

        return null
    }
}