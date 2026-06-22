/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.attackDamage
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.potion.Potion
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumParticleTypes
import kotlin.math.max
import kotlin.math.sqrt

object FakePlayer : Module("FakePlayer", Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    private val healthValue by float("Health", 20F, 1F..40F, suffix = "HP")
        .describe("Starting health of the fake player.")
    private val weaponDamage by boolean("WeaponDamage", true)
        .describe("Use your held weapon damage instead of a base value.")
    private val baseDamage by float("BaseDamage", 1F, 0F..20F, suffix = "HP") { !weaponDamage }
        .describe("Fixed damage dealt per hit when weapon damage is off.")
    private val damageMultiplier by float("DamageMultiplier", 1F, 0.1F..5F)
        .describe("Multiplier applied to the dealt damage.")
    private val invulnerabilityTicks by int("InvulnerabilityTicks", 10, 0..20, suffix = "Ticks")
        .describe("Ticks the fake player is immune after a hit.")
    private val knockbackValue by float("Knockback", 0.4F, 0F..2F)
        .describe("Knockback strength applied on a hit.")
    private val sprintKnockbackValue by float("SprintKnockback", 0.4F, 0F..2F)
        .describe("Extra knockback applied while sprinting.")
    private val criticalParticles by boolean("CriticalParticles", true)
        .describe("Show critical-hit particles on the fake player.")
    private val removeOnDeath by boolean("RemoveOnDeath", true)
        .describe("Remove the fake player when it dies.")
    private val deathDelayTicks by int("DeathDelayTicks", 20, 0..40, suffix = "Ticks") { removeOnDeath }
        .describe("Ticks to wait before removing a dead fake player.")

    private var fakePlayer: EntityOtherPlayerMP? = null
    private var fakeHealth = 20F
    private var deathTicks = 0

    override val tag: String?
        get() = fakePlayer?.let { "%.1f HP".format(fakeHealth) }

    val onAttack = handler<AttackEvent>(priority = Byte.MAX_VALUE) { event ->
        val fake = fakePlayer ?: return@handler
        val target = event.targetEntity ?: return@handler

        if (target !== fake && target.entityId != fake.entityId) {
            return@handler
        }

        event.cancelEvent()

        if (fake.isDead || fakeHealth <= 0F || fake.hurtResistantTime > 0) {
            return@handler
        }

        applyFakeHit(fake)
    }

    val onUpdate = handler<UpdateEvent> {
        val fake = fakePlayer ?: return@handler

        if (fake.worldObj !== mc.theWorld) {
            fakePlayer = null
            fakeHealth = 0F
            deathTicks = 0
            return@handler
        }

        fakeHealth = fake.health.coerceAtLeast(0F)

        if (fakeHealth <= 0F) {
            deathTicks++
            fake.deathTime = deathTicks.coerceAtMost(20)
            if (removeOnDeath && deathTicks > deathDelayTicks) {
                removeFakePlayer()
            }
            return@handler
        }

        // EntityOtherPlayerMP normally only interpolates server positions and runs no physics of its
        // own, so its motion (e.g. knockback) is ignored. Drive movement + gravity + friction manually.
        fake.moveEntity(fake.motionX, fake.motionY, fake.motionZ)
        fake.motionY = (fake.motionY - 0.08) * 0.98
        val friction = if (fake.onGround) 0.6 * 0.91 else 0.91
        fake.motionX *= friction
        fake.motionZ *= friction
    }

    val onWorld = handler<WorldEvent>(always = true) {
        fakePlayer = null
        fakeHealth = 0F
        deathTicks = 0
    }

    override fun onEnable() {
        val world = mc.theWorld ?: return
        val player = mc.thePlayer ?: return

        removeFakePlayer()

        fakeHealth = healthValue
        deathTicks = 0
        fakePlayer = EntityOtherPlayerMP(world, player.gameProfile).apply {
            clonePlayer(player, true)
            rotationYawHead = player.rotationYawHead
            copyLocationAndAnglesFrom(player)
            capabilities.disableDamage = false
            getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue = healthValue.toDouble()
            health = healthValue
            hurtTime = 0
            maxHurtTime = 0
            hurtResistantTime = 0
            deathTime = 0
        }

        world.addEntityToWorld(nextFakePlayerId(), fakePlayer)
    }

    override fun onDisable() {
        removeFakePlayer()
    }

    private fun applyFakeHit(fake: EntityOtherPlayerMP) {
        val player = mc.thePlayer ?: return
        val damage = currentDamage()

        val damaged = fake.attackEntityFrom(DamageSource.causePlayerDamage(player), damage)
        if (!damaged && fake.health == fakeHealth) {
            return
        }

        fakeHealth = fake.health.coerceAtLeast(0F)
        fake.hurtTime = 10
        fake.maxHurtTime = 10
        fake.hurtResistantTime = invulnerabilityTicks
        fake.attackedAtYaw = player.rotationYaw - fake.rotationYaw

        applyKnockback(fake)
        spawnHitEffects(fake)

        if (criticalParticles && isCriticalHit(player)) {
            player.onCriticalHit(fake)
        }

        if (EnchantmentHelper.getModifierForCreature(player.heldItem, fake.creatureAttribute) > 0F) {
            player.onEnchantmentCritical(fake)
        }

        if (fakeHealth <= 0F || fake.isDead) {
            fakeHealth = 0F
            fake.health = 0F
            fake.isDead = false
            deathTicks = 0
            fake.deathTime = 1

            if (removeOnDeath && deathDelayTicks == 0) {
                removeFakePlayer()
            }
        }
    }

    private fun currentDamage(): Float {
        val player = mc.thePlayer ?: return baseDamage
        val damage = if (weaponDamage) {
            player.heldItem?.attackDamage?.toFloat() ?: 1F
        } else {
            baseDamage
        }

        return max(0F, damage * damageMultiplier)
    }

    private fun applyKnockback(fake: EntityOtherPlayerMP) {
        val player = mc.thePlayer ?: return
        val strength = knockbackValue + if (player.isSprinting) sprintKnockbackValue else 0F

        if (strength <= 0F) {
            return
        }

        var x = fake.posX - player.posX
        var z = fake.posZ - player.posZ
        var distance = sqrt(x * x + z * z)

        if (distance < 0.01) {
            x = -kotlin.math.sin(Math.toRadians(player.rotationYaw.toDouble()))
            z = kotlin.math.cos(Math.toRadians(player.rotationYaw.toDouble()))
            distance = sqrt(x * x + z * z)
        }

        fake.motionX += x / distance * strength
        fake.motionZ += z / distance * strength
        fake.motionY = max(fake.motionY, 0.35)
        fake.velocityChanged = true
    }

    private fun spawnHitEffects(fake: EntityLivingBase) {
        repeat(5) {
            mc.theWorld.spawnParticle(
                EnumParticleTypes.CRIT,
                fake.posX,
                fake.posY + fake.height * 0.5,
                fake.posZ,
                0.0,
                0.0,
                0.0
            )
        }
    }

    private fun isCriticalHit(player: EntityLivingBase): Boolean {
        return player.fallDistance > 0F &&
            !player.onGround &&
            !player.isOnLadder &&
            !player.isInWater &&
            !player.isPotionActive(Potion.blindness) &&
            player.ridingEntity == null
    }

    private fun removeFakePlayer() {
        val fake = fakePlayer ?: return
        val world = mc.theWorld

        world?.removeEntityFromWorld(fake.entityId)
        fake.setDead()

        fakePlayer = null
        fakeHealth = 0F
        deathTicks = 0
    }

    private fun nextFakePlayerId(): Int {
        val world = mc.theWorld ?: return DEFAULT_FAKE_PLAYER_ID
        var entityId = DEFAULT_FAKE_PLAYER_ID
        while (world.getEntityByID(entityId) != null) {
            entityId--
        }
        return entityId
    }

    private const val DEFAULT_FAKE_PLAYER_ID = -1000
}
