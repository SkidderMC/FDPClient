/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventMethod
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.*
import net.ccbluex.liquidbounce.utils.setClientRotation
import net.ccbluex.liquidbounce.utils.setServerRotation
import net.ccbluex.liquidbounce.utils.toRotation
import net.ccbluex.liquidbounce.utils.timer.TheTimer
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.minecraft.block.Block
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

@ModuleInfo(name = "Miner", category = ModuleCategory.WORLD)
object Miner : Module() {

    private val blockValue = BlockValue("Block", 26)
    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
    private val switchValue = IntValue("SwitchDelay", 250, 0, 1000)
    private val actionValue = ListValue("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val rotationsValue = ListValue("Rotations", arrayOf("Silent", "Direct", "None"), "Silent")
    private val updateHandleValue = ListValue("UpdateHandle", arrayOf("NotTarget", "Breakable", "None"), "NotTarget")
    private val swingValue = BoolValue("Swing", true)
    private val throughWallsValue = BoolValue("ThroughWalls", false)

    private var pos: BlockPos? = null
    private var oldPos: BlockPos? = null
    private var currentDamage = 0F
    private val switchTimer = TheTimer()

    override fun onEnable() {
        pos = null
        oldPos = null
        currentDamage = 0F
        switchTimer.reset()
    }

    @EventMethod
    fun onUpdate(event: UpdateEvent) {
        if (pos == null || BlockUtils.getCenterDistance(pos!!) > rangeValue.get()
            || (updateHandleValue.get() == "NotTarget" && (pos!!.getBlock()?.let { Block.getIdFromBlock(it) } ?: -1) != blockValue.get())
            || (updateHandleValue.get() == "Breakable" && (pos!!.getBlock()?.getBlockHardness(mc.theWorld, pos!!) ?: -1f) < 0f)) {
            pos = find(blockValue.get())
        }

        if (pos == null) {
            currentDamage = 0F
            return
        }

        if (oldPos != null && oldPos != pos) {
            currentDamage = 0F
            switchTimer.reset()
        }
        oldPos = pos

        if (!switchTimer.hasTimePassed(switchValue.get())) {
            return
        }

        val rotation = toRotation(Vec3(pos!!.x.toDouble() + 0.5, pos!!.y.toDouble() + 0.5, pos!!.z.toDouble() + 0.5), true)
        when(rotationsValue.get()) {
            "Silent" -> setServerRotation(rotation.first, rotation.second)
            "Direct" -> setClientRotation(rotation.first, rotation.second)
        }

        when(actionValue.get()) {
            "Destroy" -> {
                val block = pos!!.getBlock() ?: return

                if (currentDamage == 0F) {
                    val event = ClickBlockEvent(pos, EnumFacing.DOWN)
                    LiquidBounce.eventManager.callEvent(event)
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.DOWN))

                    if (mc.thePlayer.capabilities.isCreativeMode ||
                        block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, pos!!) >= 1.0F) {
                        if (swingValue.get())
                            mc.thePlayer.swingItem()
                        mc.playerController.onPlayerDestroyBlock(pos!!, EnumFacing.DOWN)

                        currentDamage = 0F
                        pos = null
                        return
                    }
                }

                if (swingValue.get())
                    mc.thePlayer.swingItem()

                currentDamage += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, pos)
                mc.theWorld!!.sendBlockBreakProgress(mc.thePlayer.entityId, pos, (currentDamage * 10F).toInt() - 1)

                if (currentDamage >= 1F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        pos, EnumFacing.DOWN))
                    mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN)
                    currentDamage = 0F
                    pos = null
                }
            }
            "Use" -> {
                if (mc.playerController.onPlayerRightClick(
                        mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, pos, EnumFacing.DOWN,
                        Vec3(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble()))) {
                    if (swingValue.get())
                        mc.thePlayer.swingItem()
                    currentDamage = 0F
                    pos = null
                }
            }
        }
    }

    @EventMethod
    fun onRender3D(event: Render3DEvent) {
        pos ?: return

        val rx = mc.renderManager.renderPosX
        val ry = mc.renderManager.renderPosY
        val rz = mc.renderManager.renderPosZ

        val axisMining = AxisAlignedBB(
            pos!!.x + 0.5 - (currentDamage * 0.5) - rx,
            pos!!.y + 0.5 - (currentDamage * 0.5) - ry,
            pos!!.z + 0.5 - (currentDamage * 0.5) - rz,
            pos!!.x + 0.5 + (currentDamage * 0.5) - rx,
            pos!!.y + 0.5 + (currentDamage * 0.5) - ry,
            pos!!.z + 0.5 + (currentDamage * 0.5) - rz
        )
        val axisBlock = AxisAlignedBB(
            pos!!.x - rx,
            pos!!.y - ry,
            pos!!.z - rz,
            pos!!.x + 1.0 - rx,
            pos!!.y + 1.0 - ry,
            pos!!.z + 1.0 - rz
        )

    }

    private fun find(targetID: Int): BlockPos? {
        val thePlayer = mc.thePlayer ?: return null

        val radius = rangeValue.get().toInt() + 1

        var nearestBlockDistance = Double.MAX_VALUE
        var nearestBlock: BlockPos? = null

        for (x in radius downTo -radius + 1) {
            for (y in radius downTo -radius + 1) {
                for (z in radius downTo -radius + 1) {
                    val blockPos = BlockPos(thePlayer.posX.toInt() + x, thePlayer.posY.toInt() + y,
                        thePlayer.posZ.toInt() + z)
                    val block = blockPos.getBlock() ?: continue

                    if (Block.getIdFromBlock(block) != targetID) continue

                    val distance = BlockUtils.getCenterDistance(pos!!)
                    if (distance > rangeValue.get()) continue
                    if (nearestBlockDistance < distance) continue
                    if (!throughWallsValue.get() && !isHitable(blockPos)) continue

                    nearestBlockDistance = distance
                    nearestBlock = blockPos
                }
            }
        }

        return nearestBlock
    }

    /**
     * Check if block is hitable (or allowed to hit through walls)
     */
    private fun isHitable(blockPos: BlockPos): Boolean {
        val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY +
                mc.thePlayer.eyeHeight, mc.thePlayer.posZ)
        val movingObjectPosition = mc.theWorld!!.rayTraceBlocks(eyesPos,
            Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5), false, true, false)

        return movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
    }
}