/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isAttackingEntity
import net.ccbluex.liquidbounce.utils.extensions.isInLiquid
import net.ccbluex.liquidbounce.utils.extensions.isMiss
import net.ccbluex.liquidbounce.utils.extensions.isEntity
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S19PacketEntityStatus
import net.minecraft.potion.Potion
import net.minecraft.util.MovingObjectPosition

/**
 * HitSelect module - Filters unnecessary clicks to reduce CPS while maintaining effectiveness
 * and reducing knockback taken by timing attacks better.
 *
 * Works with AutoClicker, legitimate clicking, or external clicker programs.
 *
 * @author itsakc-me
 */
object HitSelect : Module("HitSelect", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    // Main mode
    private val mode by choices("Mode", arrayOf("Burst", "Critical"), "Burst")

    // General settings
    private val pauseDuration by int("PauseDuration", 500, 50..1000) { mode == "Burst" }
    private val targetHurtTime by int("TargetHurtTime", 0, 0..10) { mode == "Critical" }
    private val waitForFirstHit by boolean("WaitForFirstHit", true)
    private val useServerAttackTime by boolean("UseServerAttackTime", false)
    private val fakeSwing by choices("FakeSwing", arrayOf("Off", "Client", "Server"), "Off")

    // Cancel rates
    private val cancelRateInCombat by int("CancelRate-InCombat", 100, 0..100)
    private val cancelAirSwing by boolean("CancelAirSwing", true)
    private val cancelRateMissedSwings by int("CancelRate-MissedSwings", 50, 0..100) { cancelAirSwing }
    private val fakeSwingAir by choices("FakeSwing-Air", arrayOf("Off", "Client", "Server"), "Off") { cancelAirSwing }

    // Critical mode settings
    private val disableDuringKnockback by boolean("DisableDuringKnockback", false) { mode == "Critical" }

    // Burst mode settings
    private val hitLaterInTrades by int("HitLaterInTrades", 0, 0..500) { mode == "Burst" }

    // State tracking
    private val pauseTimer = MSTimer()
    private val tradeTimer = MSTimer()
    private var wasHitFirst = false
    private var serverConfirmedDamage = true
    private var lastAttackedEntityId = -1
    private var lastPlayerHurtTime = 0

    override fun onEnable() {
        resetState()
    }

    override fun onDisable() {
        resetState()
    }

    private fun resetState() {
        pauseTimer.reset()
        tradeTimer.reset()
        wasHitFirst = false
        serverConfirmedDamage = true
        lastAttackedEntityId = -1
        lastPlayerHurtTime = 0
    }

    /**
     * Track player damage for "wait for first hit" or "only while damaged" features
     */
    val onUpdate = handler<UpdateEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        val theTarget = mc.objectMouseOver?.entityHit as? EntityLivingBase ?: return@handler

        // Detect when player gets hit (hurtTime goes from 0 to max)
        // Or what when target is afk or something?
        if ((thePlayer.hurtTime > lastPlayerHurtTime && lastPlayerHurtTime == 0)
            || !theTarget.isAttackingEntity(thePlayer, 30.0, 3.5)) {
            wasHitFirst = true
            tradeTimer.reset()
        }

        lastPlayerHurtTime = thePlayer.hurtTime
    }

    /**
     * Listen for server damage confirmation (S19PacketEntityStatus with opCode 2)
     */
    val onPacket = handler<PacketEvent> { event ->
        if (!useServerAttackTime) return@handler
        if (event.eventType == EventState.RECEIVE) {
            val packet = event.packet
            if (packet is S19PacketEntityStatus && packet.opCode == 2.toByte()) {
                // Damage animation packet - check if it's our target
                if (packet.entityId == lastAttackedEntityId) {
                    serverConfirmedDamage = true
                }
            }
        }
    }

    /**
     * Called from MixinMinecraft.clickMouse() to determine if the click should be canceled.
     * This is the main entry point - intercepts BEFORE both swing and attack happen.
     * 
     * @return true if the click should be canceled
     */
    @JvmStatic
    fun shouldCancelClick(objectMouseOver: MovingObjectPosition?, thePlayer: EntityPlayerSP?): Boolean {
        if (!handleEvents()) return false
        if (thePlayer == null || objectMouseOver == null) return false
        
        // Check what we're clicking at
        return when {
            // Clicking at entity - check combat filtering
            objectMouseOver.typeOfHit.isEntity -> {
                val target = objectMouseOver.entityHit as? EntityLivingBase ?: return false
                shouldCancelEntityClick(target, thePlayer)
            }
            // Clicking at air - check air swing filtering
            objectMouseOver.typeOfHit.isMiss -> {
                wasHitFirst = false // Reset first hit tracking when swinging at air
                shouldCancelAirClick()
            }
            // Clicking at block - don't interfere
            else -> false
        }
    }

    /**
     * Check if an entity click should be canceled based on combat conditions
     */
    private fun shouldCancelEntityClick(target: EntityLivingBase, thePlayer: EntityPlayerSP): Boolean {
        // Check if attack should be canceled OR if cancel rate applies
        if (!shouldCancelAttack(target, thePlayer) || nextInt(0, 100) >= cancelRateInCombat) {
            // Attack going through - track for server confirmation
            if (useServerAttackTime) {
                serverConfirmedDamage = false
                lastAttackedEntityId = target.entityId
            }

            if (mode == "Burst") pauseTimer.reset()
            return false
        }

        // Click is canceled - perform fake swing if configured
        performFakeSwing(fakeSwing)
        return true
    }

    /**
     * Check if an air click should be canceled
     */
    private fun shouldCancelAirClick(): Boolean {
        if (!cancelAirSwing) return false

        // Apply cancel rate
        if (nextInt(0, 100) >= cancelRateMissedSwings) {
            return false
        }

        // Click is canceled - perform fake swing if configured
        performFakeSwing(fakeSwingAir)
        return true
    }

    /**
     * Determine if an attack should be canceled based on current mode and conditions
     */
    private fun shouldCancelAttack(target: EntityLivingBase, thePlayer: EntityPlayerSP): Boolean {
        // Server attack time mode - wait for server to confirm previous damage
        if (useServerAttackTime && !serverConfirmedDamage) {
            return true
        }

        return when (mode) {
            "Burst" -> {
                // Pause duration timeout - don't block attacks forever
                if (waitForFirstHit && !wasHitFirst) {
                    // Wait for first hit feature
                    true
                } else if (hitLaterInTrades > 0 && wasHitFirst && !tradeTimer.hasTimePassed(hitLaterInTrades)) {
                    // Hit later in trades - delay attacks when in a trade
                    true
                } else {
                    !pauseTimer.hasTimePassed(pauseDuration)
                }
            }

            "Critical" -> {
                if (isInCriticalPosition(thePlayer)) {
                    // Critical is the foremost condition
                    false
                } else if (waitForFirstHit && !wasHitFirst) {
                    true // Cancel, wait for getting hit first
                } else if (disableDuringKnockback && thePlayer.hurtTime > 0) {
                    // Disable during knockback - don't wait for crits while being knocked back
                    false // Don't cancel, let attack through
                } else {
                    // Can target take damage?
                    !canTargetTakeDamage(target)
                }
            }

            else -> false // Unknown mode, don't cancel
        }
    }

    /**
     * Check if target can receive damage (not in invulnerability frames)
     */
    private fun canTargetTakeDamage(target: EntityLivingBase): Boolean {
        // hurtTime is the visual hurt animation (0-10 ticks, decrements each tick)
        // hurtResistantTime is the actual invulnerability (0-20 ticks, decrements each tick)
        // Entity can take damage when hurtResistantTime <= 10 (half of max 20)
        return target.hurtTime <= targetHurtTime || target.hurtResistantTime <= 10
    }

    /**
     * Check if player is in position for a critical hit (falling, not on ground)
     */
    private fun isInCriticalPosition(thePlayer: EntityPlayerSP): Boolean {
        return thePlayer.motionY < 0 &&
                !thePlayer.onGround &&
                thePlayer.fallDistance > 0 &&
                !thePlayer.isOnLadder &&
                !thePlayer.isInLiquid &&
                !thePlayer.isRiding
    }

    /**
     * Perform a fake swing based on the specified mode.
     *
     * @param swingMode "Off", "Client", or "Server"
     */
    private fun performFakeSwing(swingMode: String) {
        val thePlayer = mc.thePlayer ?: return

        when (swingMode) {
            "Client" -> {
                // Client-side only animation - cast to EntityLivingBase to use custom swing method
                // which only sets visual fields without sending packet
                (thePlayer as EntityLivingBase).animateSwingItem()
            }
            "Server" -> {
                // Server-side only - send packet without visual
                sendPacket(C0APacketAnimation())
            }
            // "Off" - do nothing
        }
    }

    /**
     * This is a custom swing animation method that only updates the visual swing fields without sending
     * any packets. This allows us to show the swing animation on the client while still canceling the actual attack.
     * The swing duration is adjusted based on potion effects to match the normal swing timing.
     *
     * Note: This method is called on the client thread and should not be used to trigger actual attacks, only visual swings.
     *
     * The logic is based on EntityLivingBase.swingItem() but without sending the S0BPacketAnimation packet.
     */
    private fun EntityLivingBase.animateSwingItem() {
        val armSwingAnimationEnd = if (isPotionActive(Potion.digSpeed)) {
            6 - (1 + getActivePotionEffect(Potion.digSpeed).amplifier) * 1
        } else {
            if (isPotionActive(Potion.digSlowdown)) {
                6 + (1 + getActivePotionEffect(Potion.digSlowdown).amplifier) * 2
            } else 6
        }

        if (heldItem == null || heldItem.item == null || !heldItem.item.onEntitySwing(this, heldItem)) {
            if (!isSwingInProgress || swingProgressInt >= armSwingAnimationEnd / 2 || swingProgressInt < 0) {
                swingProgressInt = -1;
                isSwingInProgress = true;
            }
        }
    }
}
