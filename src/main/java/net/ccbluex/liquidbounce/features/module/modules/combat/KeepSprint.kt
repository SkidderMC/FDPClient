/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.kotlin.random
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import kotlin.random.Random

object KeepSprint : Module("KeepSprint", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    val motionAfterAttackOnGround by float("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
    val motionAfterAttackInAir by float("MotionAfterAttackInAir", 0.6f, 0.0f..1f)

    val motionAfterAttack: Float
        get() = getMotion().toFloat()

   
    private val chance by float("Chance", 100f, 0f..100f, "%")
    private val motionWhenHurtOnGround by float("MotionWhenHurtOnGround", 1f, 0.0f..1f)
    private val motionWhenHurtInAir by float("MotionWhenHurtInAir", 1f, 0.0f..1f)
    private val hurtTime by intRange("HurtTime", 1..10, 1..10)
    private val ticks by int("Ticks", 0, 0..20) 

    private val modifyPackets by boolean("ModifyPackets", false) 
    private val blink by boolean("Blink", false) 

    private var remainingTicks = 0
    var sprinting = false
        private set

    private var blinkActive = false
    private var blinkTimer = 0L
    private val blinkDelay = 300L 

    @Suppress("unused")
    private val postTickHandler = handler<PlayerPostTickEvent> {
        sprinting = mc.thePlayer.isSprinting
        if (remainingTicks > 0) remainingTicks--

        if (blinkActive && System.currentTimeMillis() - blinkTimer > blinkDelay) {
            flushBlink()
        }
    }

    @Suppress("unused")
    private val attackHandler = handler<AttackEvent> { event ->
        if (event.target != null) {
            remainingTicks = ticks

            if (blink && !blinkActive) {
                blinkActive = true
                blinkTimer = System.currentTimeMillis()
            }
        }
    }

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent> { event ->
        if (event.eventType != EventState.SEND) return@handler

        val packet = event.packet

        if (modifyPackets && remainingTicks > 0) {
            when (packet) {
                is C03PacketPlayer -> {
                    if (sprinting) {
                        packet.isSprinting = true
                    }
                }
                is C0BPacketEntityAction -> {
                    if (packet.action == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                        event.cancelEvent()
                    }
                }
            }
        }

        if (blinkActive && packet is C03PacketPlayer) {
            event.cancelEvent()
            BlinkUtils.blink(packet, event, sent = true, receive = false)
        }
    }

    override fun onDisable() {
        remainingTicks = 0
        if (blinkActive) {
            flushBlink()
        }
        sprinting = false
    }

    fun getMotion(): Double {
        if (remainingTicks <= 0) return 0.6
        if (Random.nextFloat() * 100 > chance) return 0.6

        val useHurt = mc.thePlayer.hurtTime in hurtTime
        val onGround = mc.thePlayer.onGround

        return if (useHurt) {
            if (onGround) motionWhenHurtOnGround else motionWhenHurtInAir
        } else {
            if (onGround) motionAfterAttackOnGround else motionAfterAttackInAir
        }.toDouble()
    }

    private fun flushBlink() {
        if (blinkActive && BlinkUtils.isBlinking) {
            BlinkUtils.unblink()
        }
        blinkActive = false
    }
}
