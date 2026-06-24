/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.minecraft.entity.player.EntityPlayer

class BotPredicate(
    val name: String,
    private val enabled: () -> Boolean,
    private val test: (EntityPlayer) -> Boolean
) {
    fun matches(player: EntityPlayer): Boolean = enabled() && test(player)
}

data class BotPredicateVerdict(val matchedRules: List<String>) {
    val detected: Boolean get() = matchedRules.isNotEmpty()
}

fun Iterable<BotPredicate>.evaluate(player: EntityPlayer): BotPredicateVerdict =
    BotPredicateVerdict(mapNotNull { predicate -> predicate.name.takeIf { predicate.matches(player) } })
