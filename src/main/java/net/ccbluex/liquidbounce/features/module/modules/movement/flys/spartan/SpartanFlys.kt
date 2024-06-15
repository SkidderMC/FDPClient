/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.spartan

import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer

class SpartanFlys : FlyMode("Spartan") {

    private var flys = ListValue("Spartan-Mode", arrayOf("Normal", "Normal2", "Fast"), "Normal")

    private val speedValue = FloatValue("SpartanFast-Speed", 2f, 0f, 5f).displayable {flys.equals("Fast")}


    // Variables
    private val timer = TickTimer()

    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer


    override fun onEnable() {
        sendLegacy()

        if (flys.equals("Fast")) {
            repeat(65) {
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 0.049,
                        player.posZ,
                        false
                    )
                )
                mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        player.posX,
                        player.posY,
                        player.posZ,
                        false
                    )
                )
            }
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C04PacketPlayerPosition(
                    player.posX,
                    player.posY + 0.1,
                    player.posZ,
                    true
                )
            )

            player.motionX *= 0.1
            player.motionZ *= 0.1
            player.swingItem()
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        when (flys.get()) {
            "Normal" -> {
                fly.antiDesync = true
                player.motionY = 0.0
                timer.update()
                if (timer.hasTimePassed(12)) {
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            player.posX,
                            player.posY + 8,
                            player.posZ,
                            true
                        )
                    )
                    mc.netHandler.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            player.posX,
                            player.posY - 8,
                            player.posZ,
                            true
                        )
                    )
                    timer.reset()
                }
            }
            "Normal2" -> {
                fly.antiDesync = true
                MovementUtils.strafe(0.264f)

                if (player.ticksExisted % 8 == 0) {
                    player.sendQueue.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(player.posX, player.posY + 10, player.posZ, true))
                }
            }
            "Fast" -> {
                fly.antiDesync = true
                MovementUtils.resetMotion(true)
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    player.motionY += speedValue.get() * 0.5
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    player.motionY -= speedValue.get() * 0.5
                }

                MovementUtils.strafe(speedValue.get())
            }
        }

    }

}