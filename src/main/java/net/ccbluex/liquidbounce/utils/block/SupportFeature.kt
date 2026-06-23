/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.block

import net.ccbluex.liquidbounce.utils.pathfinding.GraphSearch
import net.ccbluex.liquidbounce.utils.pathfinding.SearchLimits
import net.ccbluex.liquidbounce.utils.pathfinding.WeightedEdge
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

interface SupportEnvironment {
    fun isReplaceable(position: BlockPos): Boolean
    fun canAnchor(position: BlockPos): Boolean
}

data class SupportPlan(
    /** Placement order, starting next to an existing anchor and ending at the requested target. */
    val placements: List<BlockPos>,
    val expandedNodes: Int
)

/** Finds a bounded chain of replaceable blocks connected to an existing clickable anchor. */
class SupportFeature(
    private val environment: SupportEnvironment,
    val maximumDepth: Int = 8,
    val maximumNodes: Int = 2_048
) {
    init {
        require(maximumDepth > 0) { "Support depth must be positive" }
        require(maximumNodes > 0) { "Support node limit must be positive" }
    }

    fun plan(target: BlockPos): SupportPlan? {
        if (!environment.isReplaceable(target)) return null

        val path = GraphSearch.dijkstra(
            start = target,
            isGoal = ::hasAnchor,
            edges = ::edges,
            limits = SearchLimits(maximumDepth.toDouble(), maximumNodes)
        )
        if (!path.reachedGoal) return null
        return SupportPlan(path.nodes.asReversed(), path.expandedNodes)
    }

    private fun hasAnchor(position: BlockPos): Boolean =
        EnumFacing.values().any { environment.canAnchor(position.offset(it)) }

    private fun edges(position: BlockPos): List<WeightedEdge<BlockPos>> =
        EnumFacing.values().map { position.offset(it) }
            .filter(environment::isReplaceable)
            .map { WeightedEdge(it, 1.0) }
}
