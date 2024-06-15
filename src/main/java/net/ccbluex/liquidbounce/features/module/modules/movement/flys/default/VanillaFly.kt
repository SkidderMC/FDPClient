/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.default

import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer

class VanillaFly : FlyMode("Vanilla") {
    private val smoothValue = BoolValue("${valuePrefix}-Smooth", false)
    private val speedValue = FloatValue("${valuePrefix}-Speed", 2f, 0f, 5f)
    private val vspeedValue = FloatValue("${valuePrefix}-Vertical", 2f, 0f, 5f)
    private val kickBypassValue = BoolValue("${valuePrefix}-KickBypass", false)
    private val kickBypassModeValue = ListValue("${valuePrefix}-KickBypassMode", arrayOf("Motion", "Packet"), "Packet").displayable { kickBypassValue.get() }
    private val kickBypassMotionSpeedValue = FloatValue("${valuePrefix}KickBypass-MotionSpeed", 0.0626F, 0.05F, 0.1F).displayable { kickBypassModeValue.get() == "Motion" && kickBypassValue.get() }
    private val keepAliveValue = BoolValue("${valuePrefix}-KeepAlive", false)
    private val noClipValue = BoolValue("${valuePrefix}-NoClip", false)
    private val spoofValue = BoolValue("${valuePrefix}-SpoofGround", false)

    private var packets = 0
    private var kickBypassMotion = 0f

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer

    override fun onEnable() {
        packets = 0
        kickBypassMotion = 0f
    }

    override fun onUpdate(event: UpdateEvent) {
        if (keepAliveValue.get()) {
            mc.netHandler.addToSendQueue(C00PacketKeepAlive())
        }

        if (noClipValue.get()) {
            player.noClip = true
        }

        if(kickBypassValue.get()) {
            if(kickBypassModeValue.get() === "Motion") {
                kickBypassMotion = kickBypassMotionSpeedValue.get()

                if (player.ticksExisted % 2 == 0) {
                    kickBypassMotion = -kickBypassMotion
                }

                if(!mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    player.motionY = kickBypassMotion.toDouble()
                }
            }
        }

        if(smoothValue.get()) {
            player.capabilities.isFlying = true
            player.capabilities.flySpeed = speedValue.get() * 0.05f
        } else {
            player.capabilities.isFlying = false
            MovementUtils.resetMotion(true)
            if (mc.gameSettings.keyBindJump.isKeyDown) player.motionY += vspeedValue.get()

            if (mc.gameSettings.keyBindSneak.isKeyDown) player.motionY -= vspeedValue.get()

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
