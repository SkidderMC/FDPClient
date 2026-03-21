package net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.ncp

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.blockDropHorizontalSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight.blockDropVerticalSpeed
import net.ccbluex.liquidbounce.features.module.modules.movement.flymodes.FlyMode
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object BlockDrop : FlyMode("BlockDrop") {
    private var startX = 0.0
    private var startY = 0.0
    private var startZ = 0.0
    private var startYaw = 0f
    private var startPitch = 0f

    override fun onEnable() {
        val player = mc.thePlayer ?: return

        startX = player.posX
        startY = player.posY
        startZ = player.posZ
        startYaw = player.rotationYaw
        startPitch = player.rotationPitch
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        MovementUtils.resetMotion(true)

        if (mc.gameSettings.keyBindJump.isKeyDown) {
            player.motionY = blockDropVerticalSpeed.toDouble()
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            player.motionY -= blockDropVerticalSpeed.toDouble()
        }

        MovementUtils.strafe(blockDropHorizontalSpeed)

        repeat(2) {
            sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    startX,
                    startY,
                    startZ,
                    startYaw,
                    startPitch,
                    true
                ),
                false
            )
        }

        repeat(2) {
            sendPacket(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    player.posX,
                    player.posY,
                    player.posZ,
                    startYaw,
                    startPitch,
                    false
                ),
                false
            )
        }
    }

    override fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is C03PacketPlayer -> event.cancelEvent()

            is S08PacketPlayerPosLook -> {
                startX = packet.x
                startY = packet.y
                startZ = packet.z
                startYaw = packet.yaw
                startPitch = packet.pitch
                event.cancelEvent()
            }
        }
    }
}
