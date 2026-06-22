/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumParticleTypes
import kotlin.random.Random

object HitFX : Module("HitFX", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false) {

    private val effect by choices(
        "Effect",
        arrayOf("Fire", "Heart", "Water", "Smoke", "Magic", "Crits", "Burst"),
        "Fire"
    )

    private val minAmount by int("MinAmount", 4, 1..40)
        .describe("Minimum number of particles spawned on hit.")
    private val maxAmount by int("MaxAmount", 8, 1..40)
        .describe("Maximum number of particles spawned on hit.")
    private val spread by float("Spread", 0.6f, 0f..2f)
        .describe("How far the particles spread on hit.")

    private var lastSpawn = 0L

    private val type: EnumParticleTypes
        get() = when (effect) {
            "Heart" -> EnumParticleTypes.HEART
            "Water" -> EnumParticleTypes.WATER_DROP
            "Smoke" -> EnumParticleTypes.SMOKE_NORMAL
            "Magic" -> EnumParticleTypes.CRIT_MAGIC
            "Crits" -> EnumParticleTypes.CRIT
            "Burst" -> EnumParticleTypes.EXPLOSION_NORMAL
            else -> EnumParticleTypes.LAVA
        }

    val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity as? EntityLivingBase ?: return@handler
        if (!target.isEntityAlive) return@handler

        val world = mc.theWorld ?: return@handler

        val now = System.currentTimeMillis()
        if (now - lastSpawn < 120L) return@handler
        lastSpawn = now

        val box = target.entityBoundingBox
        val centerX = (box.minX + box.maxX) / 2.0
        val centerY = (box.minY + box.maxY) / 2.0
        val centerZ = (box.minZ + box.maxZ) / 2.0

        val lo = minOf(minAmount, maxAmount)
        val hi = maxOf(minAmount, maxAmount)
        val amount = if (lo == hi) lo else Random.nextInt(lo, hi + 1)
        val s = spread.toDouble()
        val particle = type

        repeat(amount) {
            world.spawnParticle(
                particle,
                centerX + (Random.nextDouble() - 0.5) * s,
                centerY + (Random.nextDouble() - 0.5) * s,
                centerZ + (Random.nextDouble() - 0.5) * s,
                (Random.nextDouble() - 0.5) * 0.2,
                Random.nextDouble() * 0.2,
                (Random.nextDouble() - 0.5) * 0.2
            )
        }
    }
}
