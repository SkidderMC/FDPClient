/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "SpeedMine", category = ModuleCategory.WORLD)
class SpeedMine : Module() {
    private val speedValue = FloatValue("Speed", 1.5f, 1f, 3f)
    private var facing: EnumFacing? = null
    private var pos: BlockPos? = null
    private var boost = false
    private var damage = 0f
    @EventTarget
    fun onMotion(e: MotionEvent) {
        if (e.isPre()) {
            mc.playerController.blockHitDelay = 0
            if (pos != null && boost) {
                val blockState = mc.theWorld.getBlockState(pos) ?: return
                damage += try {
                    blockState.block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * speedValue.get()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    return
                }
                if (damage >= 1) {
                    try {
                        mc.theWorld.setBlockState(pos, Blocks.air.defaultState, 11)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return
                    }
                    sendPacketNoEvent(
                        C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            pos,
                            facing
                        )
                    )
                    damage = 0f
                    boost = false
                }
            }
        }
    }

    @EventTarget
    fun onPacket(e: PacketEvent) {
        if (e.packet is C07PacketPlayerDigging) {
            val packet = e.packet
            if (packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                boost = true
                pos = packet.position
                facing = packet.facing
                damage = 0f
            } else if ((packet.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) or (packet.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                boost = false
                pos = null
                facing = null
            }
        }
    }
}