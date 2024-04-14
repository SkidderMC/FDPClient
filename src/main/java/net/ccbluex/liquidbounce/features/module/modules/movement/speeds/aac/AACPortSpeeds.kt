package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockAir
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Timer

class AACPortSpeeds : SpeedMode("AACPort") {

    private var speeds = ListValue("AACPort-Mode", arrayOf("AACPort", "AACYPort", "AACYPort2"), "AACPort")

    private val aacportlength = FloatValue("AACPort-Length", 1F, 1F, 20F).displayable { speeds.equals("AACPort") }


    // Optimize code
    val player: EntityPlayerSP
        get() = mc.thePlayer
    val timer: Timer
        get() = mc.timer


    override fun onUpdate() {
        when (speeds.get()) {
            "AACPort" -> {
                if (!MovementUtils.isMoving()) return

                val f = player.rotationYaw * 0.017453292f
                var d = 0.2
                while (d <= aacportlength.get()) {
                    val x = player.posX - MathHelper.sin(f) * d
                    val z = player.posZ + MathHelper.cos(f) * d

                    if (player.posY < player.posY.toInt() + 0.5 && BlockUtils.getBlock(
                            BlockPos(
                                x,
                                player.posY,
                                z
                            )
                        ) !is BlockAir
                    ) {
                        break
                    }

                    player.sendQueue.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            x,
                            player.posY,
                            z,
                            true
                        )
                    )
                    d += 0.2
                }
            }
        }
    }

    override fun onPreMotion() {
        when (speeds.get()) {
            "AACYPort" -> {
                if (MovementUtils.isMoving() && !player.isSneaking) {
                    player.cameraPitch = 0f
                    if (player.onGround) {
                        player.motionY = 0.3425
                        player.motionX *= 1.5893
                        player.motionZ *= 1.5893
                    } else player.motionY = -0.19
                }
            }
            "AACYPort2" -> {
                if (MovementUtils.isMoving()) {
                    player.cameraPitch = 0f
                    if (player.onGround) {
                        player.jump()
                        player.motionY = 0.3851
                        player.motionX *= 1.01
                        player.motionZ *= 1.01
                    } else player.motionY = -0.21
                }
            }
        }
    }

}