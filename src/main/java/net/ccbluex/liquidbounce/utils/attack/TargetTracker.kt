/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.attack

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.sorting.ComparatorChain
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion

/**
 * Shared, deterministic target selector. Validation, ordering and selection are kept in one place
 * so combat modules do not subtly disagree about which entity is the best valid target.
 */
class TargetTracker(
    private val range: () -> Float = { Float.MAX_VALUE },
    private val fov: () -> Float = { 180F },
    private val distance: (EntityLivingBase) -> Double = {
        MinecraftInstance.mc.thePlayer?.getDistanceToEntity(it)?.toDouble() ?: Double.MAX_VALUE
    },
    private val filter: (EntityLivingBase) -> Boolean = { EntityUtils.isSelected(it, true) },
) : MinecraftInstance {

    var target: EntityLivingBase? = null
        private set

    var closestEnemyDistance: Double = Double.POSITIVE_INFINITY
        private set

    private var comparator: Comparator<EntityLivingBase> = TargetPriority.TYPE.comparator(distance)

    fun priorities(vararg priorities: TargetPriority) = apply {
        comparator = ComparatorChain(*priorities.map { it.comparator(distance) }.toTypedArray())
    }

    fun targets(predicate: (EntityLivingBase) -> Boolean = { true }): List<EntityLivingBase> {
        val world = mc.theWorld ?: return emptyList()
        val candidates = world.loadedEntityList.asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter(::validate)
            .filter(predicate)
            .sortedWith(comparator)
            .toList()

        closestEnemyDistance = candidates.minOfOrNull(distance) ?: Double.POSITIVE_INFINITY
        return candidates
    }

    fun selectFirst(predicate: (EntityLivingBase) -> Boolean = { true }): EntityLivingBase? =
        targets(predicate).firstOrNull().also { target = it }

    fun <R : Any> select(
        predicate: (EntityLivingBase) -> Boolean = { true },
        evaluator: (EntityLivingBase) -> R?,
    ): R? {
        for (candidate in targets(predicate)) {
            val result = evaluator(candidate) ?: continue
            target = candidate
            return result
        }
        reset()
        return null
    }

    fun validateTarget(predicate: (EntityLivingBase) -> Boolean = { true }): Boolean {
        val current = target ?: return false
        if (validate(current) && predicate(current)) return true
        reset()
        return false
    }

    fun validate(entity: EntityLivingBase): Boolean {
        if (!filter(entity)) return false
        if (distance(entity) > range()) return false
        val maximumFov = fov()
        return maximumFov >= 180F || rotationDifference(entity) <= maximumFov
    }

    fun reset() {
        target = null
        closestEnemyDistance = Double.POSITIVE_INFINITY
    }
}

enum class TargetPriority {
    TYPE,
    HEALTH,
    DISTANCE,
    DIRECTION,
    LIVING_TIME,
    ARMOR,
    HURT_RESISTANCE,
    HURT_TIME,
    HEALTH_ABSORPTION,
    REGEN_AMPLIFIER,
    ON_LADDER,
    IN_LIQUID,
    IN_WEB;

    fun comparator(distance: (EntityLivingBase) -> Double): Comparator<EntityLivingBase> = when (this) {
        TYPE -> compareBy { if (it is EntityPlayer) 0 else 1 }
        HEALTH -> compareBy { it.health }
        DISTANCE -> compareBy(distance)
        DIRECTION -> compareBy { rotationDifference(it) }
        LIVING_TIME -> compareByDescending { it.ticksExisted }
        ARMOR -> compareBy { it.totalArmorValue }
        HURT_RESISTANCE -> compareBy { it.hurtResistantTime }
        HURT_TIME -> compareBy { it.hurtTime }
        HEALTH_ABSORPTION -> compareBy { it.health + it.absorptionAmount }
        REGEN_AMPLIFIER -> compareBy {
            if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1
        }
        ON_LADDER -> compareBy { !it.isOnLadder }
        IN_LIQUID -> compareBy { !(it.isInWater || it.isInLava) }
        IN_WEB -> compareBy { !it.isInWeb }
    }

    companion object {
        fun fromName(name: String): TargetPriority? = values().firstOrNull {
            it.name.replace("_", "").equals(name.replace("_", ""), true)
        }
    }
}
