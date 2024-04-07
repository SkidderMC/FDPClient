/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class NCPFlys : FlyMode("NCP") {

    private var flys = ListValue("NCP-Mode", arrayOf("Latest", "Packet"), "Latest")

    private val verusBypass = BoolValue("NCPLatest-VerusBypass", true).displayable { flys.equals("Latest") }

    // Packet
    private val timerValue = FloatValue("NCPPacket-Timer", 1.1f, 1.0f, 1.3f).displayable { flys.equals("Packet") }
    private val speedValue = FloatValue("NCPPacket-Speed", 0.28f, 0.27f, 0.29f).displayable { flys.equals("Packet") }


    // Variables
    private var started = false
    private var cancelTp = true
    private var wasOnGround = false

    override fun onEnable() {
        if (!flys.equals("Latest")) { sendLegacy() }

        when (flys.get()) {
            "Latest" -> {
                cancelTp = true
                if (verusBypass.get()) {
                    val pos = mc.thePlayer.position.add(0.0, -1.5, 0.0)
                    mc.netHandler.addToSendQueue(
                        C08PacketPlayerBlockPlacement(pos, 1,
                            ItemStack(Blocks.stone.getItem(mc.theWorld, pos)), 0.0F, 0.5F + Math.random().toFloat() * 0.44.toFloat(), 0.0F)
                    )
                }
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.motionY,
                        mc.thePlayer.motionZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.motionY - 0.1,
                        mc.thePlayer.motionZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.motionY,
                        mc.thePlayer.motionZ,
                        false
                    )
                )

                started = true
            }
        }
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "Latest" -> {
                if (!started) return
                mc.timer.timerSpeed = 0.4f
                MovementUtils.strafe()

                if (mc.thePlayer.onGround) {
                    wasOnGround = true
                    mc.thePlayer.motionY = 0.42
                    MovementUtils.strafe(10f)
                } else if (wasOnGround) {
                    MovementUtils.strafe(9.6f)
                    wasOnGround = false
                }
            }
            "Packet" -> {
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
                val x = -sin(yaw) * speedValue.get()
                val z = cos(yaw) * speedValue.get()
                MovementUtils.resetMotion(true)
                mc.timer.timerSpeed = timerValue.get()
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX + x,
                        mc.thePlayer.motionY,
                        mc.thePlayer.motionZ + z,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX + x,
                        mc.thePlayer.motionY - 490,
                        mc.thePlayer.motionZ + z,
                        true
                    )
                )
                mc.thePlayer.posX += x
                mc.thePlayer.posZ += z
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (flys.get()) {
            "Latest" -> {
                if(packet is S08PacketPlayerPosLook && cancelTp) {
                    cancelTp = false
                    event.cancelEvent()
                }
            }
        }
    }

}