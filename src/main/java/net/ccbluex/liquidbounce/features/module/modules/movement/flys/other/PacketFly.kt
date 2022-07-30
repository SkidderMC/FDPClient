package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.client.C03PacketPlayer

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PacketFly : FlyMode("PacketFly") {
    private val tpPlayer = BoolValue("${valuePrefix}ClipPlayerPos", false)
    private val fValue = FloatValue("${valuePrefix}ForwardClipAmount", 2f, -5f, 5f)
    private val spaceClipValue = FloatValue("${valuePrefix}UpClipAmountOnSpace", 2f, 0f, 5f)
    private val shiftClipValue = FloatValue("${valuePrefix}DownClipAmountOnShift", 2f, 0f, 5f)
    private val yClipValue = FloatValue("${valuePrefix}UpClipAmount", 1f, 0f, 5f)
    private val noMove = BoolValue("${valuePrefix}NoMove", true)
    private val noYMotion = BoolValue("${valuePrefix}NoYMotion",false)
    private val noYOnClip = BoolValue("${valuePrefix}NoYMotionOnClip", true)
    private val acceptTp = boolValue("${valuePrefix}TPLessFlag", true)

    private val delayValue = IntegerValue("${valuePrefix}Delay", 200, 0, 1000)
    private val flyFlagClip = IntegerValue("${valuePrefix}PacketFlagYClip", -11.3f, -30f, 30f)
    private val flyFlagSpoofGround = BoolValue("${valuePrefix}PacketFlagGroundSpoof", true)

    private val timer = MSTimer()


    override fun onEnable() {
        timer.reset()
        lastJump = false
    }

    override fun onUpdate() {

        mc.timer.timerSpeed = timerValue.get()
        
        if (noMove.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        
        if (noYMotion.get()) {
            mc.thePlayer.motionY = 0.0
        }
        
        if (timer.hasTimePassed(delayValue.get().toLong())) {
            if (noYOnClip.get()) {
                mc.thePlayer.motionY = 0.0
            }
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            val x = fValue.get() * -sin(yaw)
            val z = fValue.get() * cos(yaw)
            
            if (mc.gameSettings.keyBindSneak.isKeyDown) {
                val y = shiftClipValue.get()
            } else if (mc.gameSettings.keyBindJump.isKeyDown) {
                val y = spaceClipValue.get()
            } else {
                val y = yClipValue.get()
            }
            
            if (tpPlayer.get()) {
                mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z)
            }
            
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z, false)
            PacketUtils.sendPacketNoEvent(C03PacketPlayer.C04PacketPlayerPosition(mc.player.posX + x, mc.player.posY + y + flyFlagClip.get(), mc.player.posZ + z, flyFlagSpoofGround.get())
            timer.reset()
        }

    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
      
        if (packet is S08PacketPlayerPosLook && acceptTp.get()) {
            val x = packet.x - mc.thePlayer.posX
            val y = packet.y - mc.thePlayer.posY
            val z = packet.z - mc.thePlayer.posZ
            val diff = sqrt(x * x + y * y + z * z)
            if (diff <= 8) {
                event.cancelEvent()
                PacketUtils.sendPacketNoEvent(
                    C03PacketPlayer.C06PacketPlayerPosLook(
                        packet.x,
                        packet.y,
                        packet.z,
                        packet.getYaw(),
                        packet.getPitch(),
                        mc.thePlayer.onGround
                    )
                )
            }
        }
    }
}
