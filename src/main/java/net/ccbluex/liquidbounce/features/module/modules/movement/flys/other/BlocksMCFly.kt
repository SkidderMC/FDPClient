package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.PlayerUtils
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithCustomRotation
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.block.BlockAir
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3

/**
 * by @DinoFengz xd | skid = timeout
 */
class BlocksMCFly : FlyMode("BlocksMC") {
    private val timerBoostValue = BoolValue("${valuePrefix}Timer", true)
    private var blocksBB = false
    private var ticks = 0
    override fun onEnable() {
        blocksBB = false
        ticks = 0
        if(mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.4
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onUpdate(event: UpdateEvent) {
        if(mc.thePlayer.posY >= fly.launchY + 0.8 && !blocksBB) {
            if(mc.thePlayer.onGround) {
                blocksBB = true
            } else {
                var slot = -1
                for (j in 0..8) {
                    if (mc.thePlayer.inventory.getStackInSlot(j) != null && mc.thePlayer.inventory
                            .getStackInSlot(j).item is ItemBlock
                    ) {
                        slot = PlayerUtils.findSlimeBlock()!!
                        break
                    }
                }

                if(slot == -1) {
                    fly.state = false
                    LiquidBounce.hud.addNotification(Notification("BlocksMCFly", "U need a slime blocks to use this fly", NotifyType.ERROR, 1000))
                    return
                }

                val oldSlot = mc.thePlayer.inventory.currentItem
                mc.thePlayer.inventory.currentItem = slot
                val movingObjectPosition: MovingObjectPosition = mc.thePlayer.rayTraceWithCustomRotation(4.5, mc.thePlayer.rotationYaw, 90.0f)
                if (movingObjectPosition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return
                val blockPos = movingObjectPosition.blockPos
                val enumFacing = movingObjectPosition.sideHit
                val hitVec: Vec3 = movingObjectPosition.hitVec
                if (mc.playerController.onPlayerRightClick(
                        mc.thePlayer,
                        mc.theWorld,
                        mc.thePlayer.heldItem,
                        blockPos,
                        enumFacing,
                        hitVec
                    )
                ) mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                mc.thePlayer.inventory.currentItem = oldSlot


            }
        }
        if(blocksBB) {
            if(timerBoostValue.get()) {
                ticks++
                when(ticks) {
                    in 1..10 -> mc.timer.timerSpeed = 2f

                    in 10..15 -> mc.timer.timerSpeed = 0.5f
                }
                if(ticks>=15) {
                    ticks = 0
                    mc.timer.timerSpeed = 0.6f
                }
            } else {
                mc.timer.timerSpeed = 1.0f
            }
        }
    }
    override fun onBlockBB(event: BlockBBEvent) {
        if(!blocksBB) return

        if (event.block is BlockAir && event.y <= fly.launchY + 1) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, fly.launchY, event.z + 1.0)
        }
    }
}