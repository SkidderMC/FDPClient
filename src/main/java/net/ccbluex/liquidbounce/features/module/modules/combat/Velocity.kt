/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.world.Explosion

@ModuleInfo(name = "Velocity", description = "Allows you to modify the amount of knockback you take.", category = ModuleCategory.COMBAT)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F)
    private val verticalValue = FloatValue("Vertical", 0F, 0F, 1F)
    private val modeValue = ListValue("Mode", arrayOf("Simple", "AAC", "AACPush", "AACZero", "AACv4",
            "Reverse", "SmoothReverse", "Jump", "Phase", "PacketPhase"), "Simple")

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F)
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F)

    // AAC Push
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F)
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true)

    // phase
    private val phaseHeightValue = FloatValue("PhaseHeight",0.5F,0F,1F)
    private val phaseOnlyGround = BoolValue("PhaseOnlyGround",true)

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityInput = false

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    override val tag: String
        get() = modeValue.get()

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "jump" -> if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42
            }

            "reverse" -> {
                if (!velocityInput)
                    return

                if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L))
                    velocityInput = false
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer.hurtTime > 0)
                    reverseHurt = true

                if (!mc.thePlayer.onGround) {
                    if (reverseHurt)
                        mc.thePlayer.speedInAir = reverse2StrengthValue.get()
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac" -> if (velocityInput && velocityTimer.hasTimePassed(80L)) {
                mc.thePlayer.motionX *= horizontalValue.get()
                mc.thePlayer.motionZ *= horizontalValue.get()
                //mc.thePlayer.motionY *= verticalValue.get() ?
                velocityInput = false
            }

            "aacv4" -> {
                if (mc.thePlayer.hurtTime>0 && !mc.thePlayer.onGround){
                    mc.thePlayer.motionX *= 0.62
                    mc.thePlayer.motionZ *= 0.62
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround)
                        jump = false
                } else {
                    // Strafe
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0)
                        mc.thePlayer.onGround = true

                    // Reduce Y
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get()
                            && !LiquidBounce.moduleManager[Speed::class.java]!!.state)
                        mc.thePlayer.motionY -= 0.014999993
                }

                // Reduce XZ
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }

            "aaczero" -> if (mc.thePlayer.hurtTime > 0) {
                if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F)
                    return

                mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                mc.thePlayer.onGround = true
            } else
                velocityInput = false
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer)
                return

            velocityTimer.reset()

            when (modeValue.get().toLowerCase()) {
                "simple" -> {
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F)
                        event.cancelEvent()

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "aac", "reverse", "smoothreverse", "aaczero" -> velocityInput = true

                "phase" -> {
                    if (!mc.thePlayer.onGround&&phaseOnlyGround.get())
                        return

                    velocityInput = true
                    mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX,mc.thePlayer.posY-phaseHeightValue.get(),mc.thePlayer.posZ)
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }

                "packetphase" -> {
                    if (!mc.thePlayer.onGround&&phaseOnlyGround.get())
                        return

//                    chat("MOTX=${packet.motionX}, MOTZ=${packet.motionZ}")
                    if(packet.motionX<500&&packet.motionY<500)
                        return

                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX,mc.thePlayer.posY-phaseHeightValue.get(),mc.thePlayer.posZ,false))
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }
            }
        }

        if (packet is S27PacketExplosion) {
            event.cancelEvent()
            // particles
            val explosion = Explosion(mc.theWorld, null, packet.x, packet.y, packet.z, packet.strength, packet.affectedBlockPositions)
            explosion.doExplosionB(true)
            // convert it to velocity packet
            val velocityPacket=S12PacketEntityVelocity(mc.thePlayer.entityId,
                mc.thePlayer.motionX+packet.func_149149_c(),
                mc.thePlayer.motionY+packet.func_149144_d(),
                mc.thePlayer.motionZ+packet.func_149147_e())
            val packetEvent=PacketEvent(velocityPacket,PacketEvent.Type.RECEIVE)
            LiquidBounce.eventManager.callEvent(packetEvent)
            if(!packetEvent.isCancelled) {
                PacketUtils.handlePacket(velocityPacket)
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb)
            return

        when (modeValue.get().toLowerCase()) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer.isCollidedVertically)
                    event.cancelEvent()
            }
            "aaczero" -> if (mc.thePlayer.hurtTime > 0)
                event.cancelEvent()
        }
    }
}
