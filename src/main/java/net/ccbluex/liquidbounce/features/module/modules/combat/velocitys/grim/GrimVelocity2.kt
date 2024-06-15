/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.TickEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.IntegerValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class GrimVelocity2 : VelocityMode("GrimC07") {
    private val alwaysValue = BoolValue("Always", true)
    private val onlyAirValue = BoolValue("OnlyBreakAir", true)
    private val worldValue = BoolValue("BreakOnWorld", false)
    private val sendC03Value = BoolValue("SendC03", false)
    private val c06Value = BoolValue("Send1.17C06", false)
    private val flagPauseValue = IntegerValue("FlagPause-Time", 50, 0, 5000)

    private var gotVelo = false
    private var flagTimer = MSTimer()

    override fun onEnable() {
        gotVelo = false
        flagTimer.reset()
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S08PacketPlayerPosLook)
            flagTimer.reset()
        if (!flagTimer.hasTimePassed(flagPauseValue.get().toLong())) {
            gotVelo = false
            return
        }

        if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
            event.cancelEvent()
            gotVelo = true
        } else if (packet is S27PacketExplosion) {
            event.cancelEvent()
            gotVelo = true
        }
    }

    override fun onTick(event: TickEvent) {
        if (!flagTimer.hasTimePassed(flagPauseValue.get().toLong())) {
            gotVelo = false
            return
        }

        mc.thePlayer ?: return
        mc.theWorld ?: return

        if (gotVelo || alwaysValue.get()) { // packet processed event pls
            val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            if (checkBlock(pos) || checkBlock(pos.up()))
                gotVelo = false
        }
    }

    private fun checkBlock(pos: BlockPos): Boolean {
        if (!onlyAirValue.get() || mc.theWorld.isAirBlock(pos)) {
            if (sendC03Value.get()) {
                if (c06Value.get())
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
                else
                    mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
            }

            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
            if (worldValue.get())
                mc.theWorld.setBlockToAir(pos)
            return true
        }
        return false
    }
}
