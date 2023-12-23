package net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.grim

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.combat.velocitys.VelocityMode
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

class GrimVelocity2 : VelocityMode("GrimC07") {
    private val alwaysValue = BoolValue("${valuePrefix}Always", true)

    private val onlyAirValue = BoolValue("${valuePrefix}OnlyBreakAir", true)

    private val worldValue = BoolValue("${valuePrefix}BreakOnWorld", false)

    private val sendC03Value = BoolValue("${valuePrefix}SendC03", false).displayable { false } // bypass latest but flag timer

    private val C06Value = BoolValue("${valuePrefix}Send1.17C06", false).displayable { sendC03Value.get() } // need via to 1.17+

    private val flagPauseValue = IntegerValue("FlagPause-Time", 50, 0, 5000)

    var gotVelo = false
    var flagTimer = MSTimer()

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

        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return
        if (gotVelo || alwaysValue.get()) { // packet processed event pls
            val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            if (checkBlock(pos) || checkBlock(pos.up())) {
                gotVelo = false
            }
        }
    }

    fun checkBlock(pos: BlockPos): Boolean {
        if (!onlyAirValue.get() || mc.theWorld.isAirBlock(pos)) {
            if (sendC03Value.get()) {
                if (C06Value.get())
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
