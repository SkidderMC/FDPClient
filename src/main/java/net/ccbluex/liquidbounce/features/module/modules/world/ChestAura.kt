/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.InventoryUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.extensions.getVec
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.block.BlockChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import kotlin.concurrent.schedule

@ModuleInfo(name = "ChestAura", category = ModuleCategory.WORLD)
object ChestAura : Module() {

    private val rangeValue = FloatValue("Range", 5F, 1F, 6F)
    private val delayValue = IntegerValue("Delay", 100, 50, 500)
    private val throughWallsValue = BoolValue("ThroughWalls", true)
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val rotationsValue = BoolValue("Rotations", true)
    private val discoverDelayEnabledValue = BoolValue("DiscoverDelay", false)
    private val discoverDelayValue = IntegerValue("DiscoverDelayValue", 200, 50, 300).displayable { discoverDelayEnabledValue.get() }
    private val onlyOnGroundValue = BoolValue("OnlyOnGround", true)
    private val notOpenedValue = BoolValue("NotOpened", false)
    private val noCombatingValue = BoolValue("NoCombating", true)

    private var currentBlock: BlockPos? = null
    private var underClick = false

    val clickedBlocks = mutableListOf<BlockPos>()

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if ((onlyOnGroundValue.get() && !mc.thePlayer.onGround) || (noCombatingValue.get() && LiquidBounce.combatManager.inCombat)) {
            return
        }

        if (event.eventState == EventState.PRE) {
            if (mc.currentScreen is GuiContainer) {
                return
            }

            val radius = rangeValue.get() + 1

            val eyesPos = Vec3(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ)

            currentBlock = BlockUtils.searchBlocks(radius.toInt())
                .filter {
                    it.value is BlockChest && !clickedBlocks.contains(it.key) &&
                            BlockUtils.getCenterDistance(it.key) < rangeValue.get()
                }
                .filter {
                    if (throughWallsValue.get()) {
                        return@filter true
                    }

                    val blockPos = it.key
                    val movingObjectPosition = mc.theWorld.rayTraceBlocks(eyesPos,
                        blockPos.getVec(), false, true, false)

                    movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
                }
                .minByOrNull { BlockUtils.getCenterDistance(it.key) }?.key

            if (rotationsValue.get()) {
                RotationUtils.setTargetRotation((RotationUtils.faceBlock(currentBlock ?: return)
                    ?: return).rotation)
            }
        } else if (currentBlock != null && InventoryUtils.INV_TIMER.hasTimePassed(delayValue.get().toLong()) && !underClick) {
            underClick = true
            if (discoverDelayEnabledValue.get()) {
                java.util.Timer().schedule(discoverDelayValue.get().toLong()) {
                    click()
                }
            } else {
                click()
            }
        }
    }

    private fun click() {
        try {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, currentBlock,
                    EnumFacing.DOWN, currentBlock!!.getVec())) {
                if (swingValue.equals("packet")) {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                } else if (swingValue.equals("normal")) {
                    mc.thePlayer.swingItem()
                }

                clickedBlocks.add(currentBlock!!)
                currentBlock = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        underClick = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (notOpenedValue.get() && event.packet is S24PacketBlockAction) {
            val packet = event.packet
            if (packet.blockType is BlockChest && packet.data2 == 1 && !clickedBlocks.contains(packet.blockPosition)) {
                    clickedBlocks.add(packet.blockPosition)
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // clear blocks record when change world
        clickedBlocks.clear()
    }

    override fun onDisable() {
        clickedBlocks.clear()
        underClick = false
    }
}
