package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper

class AACPortSpeeds : SpeedMode("AACPort") {

    private var speeds = ListValue("AACPort-Mode", arrayOf("AACPort", "AACYPort", "AACYPort2"), "AACPort")

    private val aacportlength = FloatValue("AACPort-Length", 1F, 1F, 20F).displayable { speeds.equals("AACPort") }


    override fun onUpdate() {
        when (speeds.get()) {
            "AACPort" -> {
                if (!MovementUtils.isMoving()) return

                val f = mc.thePlayer.rotationYaw * 0.017453292f
                var d = 0.2
                while (d <= aacportlength.get()) {
                    val x = mc.thePlayer.posX - MathHelper.sin(f) * d
                    val z = mc.thePlayer.posZ + MathHelper.cos(f) * d

                    if (mc.thePlayer.posY < mc.thePlayer.posY.toInt() + 0.5 && BlockUtils.getBlock(
                            BlockPos(
                                x,
                                mc.thePlayer.posY,
                                z
                            )
                        ) !is BlockAir
                    ) {
                        break
                    }

                    mc.thePlayer.sendQueue.addToSendQueue(
                        C03PacketPlayer.C04PacketPlayerPosition(
                            x,
                            mc.thePlayer.posY,
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
                if (MovementUtils.isMoving() && !mc.thePlayer.isSneaking) {
                    mc.thePlayer.cameraPitch = 0f
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionY = 0.3425
                        mc.thePlayer.motionX *= 1.5893
                        mc.thePlayer.motionZ *= 1.5893
                    } else mc.thePlayer.motionY = -0.19
                }
            }
            "AACYPort2" -> {
                if (MovementUtils.isMoving()) {
                    mc.thePlayer.cameraPitch = 0f
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        mc.thePlayer.motionY = 0.3851
                        mc.thePlayer.motionX *= 1.01
                        mc.thePlayer.motionZ *= 1.01
                    } else mc.thePlayer.motionY = -0.21
                }
            }
        }
    }

}