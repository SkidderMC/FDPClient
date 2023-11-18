package net.ccbluex.liquidbounce.features.module.modules.movement.flys.default

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer

class VanillaFly : FlyMode("Vanilla") {
    private val smoothValue = BoolValue("${valuePrefix}Smooth", false)
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)
    private val vspeedValue = FloatValue("${valuePrefix}Vertical", 2f, 0f, 5f)
    private val kickBypassValue = BoolValue("${valuePrefix}KickBypass", false)
    private val kickBypassModeValue = ListValue("${valuePrefix}KickBypassMode", arrayOf("Motion", "Packet"), "Packet").displayable { kickBypassValue.get() }
    private val kickBypassMotionSpeedValue = FloatValue("${valuePrefix}KickBypass-MotionSpeed", 0.0626F, 0.05F, 0.1F).displayable { kickBypassModeValue.get() == "Motion" && kickBypassValue.get() }
    private val keepAliveValue = BoolValue("${valuePrefix}KeepAlive", false)
    private val noClipValue = BoolValue("${valuePrefix}NoClip", false)
    private val spoofValue = BoolValue("${valuePrefix}SpoofGround", false)

    private var packets = 0
    private var kickBypassMotion = 0f

    override fun onEnable() {
        packets = 0
        kickBypassMotion = 0f
    }

    override fun onUpdate(event: UpdateEvent) {
        if (keepAliveValue.get()) {
            mc.netHandler.addToSendQueue(C00PacketKeepAlive())
        }

        if (noClipValue.get()) {
            mc.thePlayer.noClip = true
        }

        if(kickBypassValue.get()) {
            if(kickBypassModeValue.get() === "Motion") {
                kickBypassMotion = kickBypassMotionSpeedValue.get()

                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    kickBypassMotion = -kickBypassMotion
                }

                if(!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    mc.thePlayer.motionY = kickBypassMotion.toDouble()
                }
            }
        }

        if(smoothValue.get()) {
            mc.thePlayer.capabilities.isFlying = true
            mc.thePlayer.capabilities.flySpeed = speedValue.get() * 0.05f
        } else {
            mc.thePlayer.capabilities.isFlying = false
            MovementUtils.resetMotion(true)
            if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY += vspeedValue.get()

            if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY -= vspeedValue.get()

            MovementUtils.strafe(speedValue.get())
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if(spoofValue.get()) packet.onGround = true
            if (packets++ >= 40 && kickBypassValue.get()) {
                packets = 0
                if(kickBypassModeValue.get() === "Packet") {
                    MovementUtils.handleVanillaKickBypass()
                }
            }
        }
    }
}
