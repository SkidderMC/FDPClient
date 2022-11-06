package net.ccbluex.liquidbounce.features.module.modules.player.nofalls.normal

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.modules.player.nofalls.NoFallMode
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.VecRotation
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemBucket
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import kotlin.math.ceil
import kotlin.math.sqrt

class MLGNofall : NoFallMode("MLG") {
    private val minFallDistanceValue = FloatValue("${valuePrefix}MinMLGHeight", 5f, 2f, 50f)
    private val mlgTimer = TickTimer()
    private var currentMlgRotation: VecRotation? = null
    private var currentMlgItemIndex = 0
    private var currentMlgBlock: BlockPos? = null

    override fun onEnable() {
        mlgTimer.reset()
        currentMlgRotation = null
        currentMlgItemIndex = 0
        currentMlgBlock = null
    }
    override fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            currentMlgRotation = null
            mlgTimer.update()

            if (!mlgTimer.hasTimePassed(10))
                return


            if (mc.thePlayer.fallDistance > minFallDistanceValue.get()) {
                val fallingPlayer = FallingPlayer(mc.thePlayer)
                val maxDist = mc.playerController.blockReachDistance + 1.5
                val collision = fallingPlayer.findCollision(ceil(1.0 / mc.thePlayer.motionY * -maxDist).toInt()) ?: return
                var ok = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ).distanceTo(
                    Vec3(collision).addVector(0.5, 0.5, 0.5)) < mc.playerController.blockReachDistance + sqrt(0.75)

                if (mc.thePlayer.motionY < collision.y + 1 - mc.thePlayer.posY)
                    ok = true

                if (!ok)
                    return

                var index = -1

                for (i in 36..44) {
                    val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack

                    if (itemStack != null && (itemStack.item == Items.water_bucket || itemStack.item is ItemBlock && (itemStack.item as ItemBlock).block == Blocks.web)) {
                        index = i - 36

                        if (mc.thePlayer.inventory.currentItem == index)
                            break
                    }
                }

                if (index == -1)
                    return

                currentMlgItemIndex = index
                currentMlgBlock = collision

                if (mc.thePlayer.inventory.currentItem != index) {
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(index))
                }

                currentMlgRotation = RotationUtils.faceBlock(collision)
                currentMlgRotation!!.rotation.toPlayer(mc.thePlayer)
            }
        } else if (currentMlgRotation != null) {
            val stack = mc.thePlayer.inventory.mainInventory[currentMlgItemIndex]

            if (stack.item is ItemBucket)
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack)
            else if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, stack, currentMlgBlock, EnumFacing.UP, Vec3(0.0,0.5,0.0).add(
                    Vec3(currentMlgBlock ?: return)
                )))
                mlgTimer.reset()

            if (mc.thePlayer.inventory.currentItem != currentMlgItemIndex)
                mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }
    }
}