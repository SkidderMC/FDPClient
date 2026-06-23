/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.block.FaceTargetMode
import net.ccbluex.liquidbounce.utils.block.FaceTargetPositionFactory
import net.ccbluex.liquidbounce.utils.block.SupportEnvironment
import net.ccbluex.liquidbounce.utils.block.SupportFeature
import net.ccbluex.liquidbounce.utils.math.geometry.approximatelyEquals
import net.ccbluex.liquidbounce.utils.pathfinding.GraphSearch
import net.ccbluex.liquidbounce.utils.pathfinding.SearchLimits
import net.ccbluex.liquidbounce.utils.pathfinding.SearchTermination
import net.ccbluex.liquidbounce.utils.pathfinding.WeightedEdge
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

object WorldFoundationVerification {
    @JvmStatic
    fun main(args: Array<String>) {
        verifyGraphSearch()
        verifyPlacementFoundations()
        println("World foundation verification passed")
    }

    private fun verifyGraphSearch() {
        val blocked = setOf(GridPoint(1, 0), GridPoint(1, 1))
        fun edges(point: GridPoint): List<WeightedEdge<GridPoint>> = listOf(
            GridPoint(point.x + 1, point.y), GridPoint(point.x - 1, point.y),
            GridPoint(point.x, point.y + 1), GridPoint(point.x, point.y - 1)
        ).filter { it.x in 0..3 && it.y in 0..3 && it !in blocked }
            .map { WeightedEdge(it, 1.0) }

        val goal = GridPoint(3, 0)
        val path = GraphSearch.aStar(
            start = GridPoint(0, 0),
            isGoal = { it == goal },
            edges = ::edges,
            heuristic = { kotlin.math.abs(goal.x - it.x) + kotlin.math.abs(goal.y - it.y).toDouble() }
        )
        check(path.reachedGoal)
        check(path.nodes.first() == GridPoint(0, 0) && path.nodes.last() == goal)
        check(path.totalCost == 7.0)

        val limited = GraphSearch.dijkstra(
            start = GridPoint(0, 0),
            isGoal = { it == goal },
            edges = ::edges,
            limits = SearchLimits(maximumCost = 2.0, maximumExpandedNodes = 100)
        )
        check(!limited.reachedGoal)
        check(limited.termination == SearchTermination.COST_LIMIT)
        check(limited.nodes.isNotEmpty() && limited.totalCost <= 2.0)
    }

    private fun verifyPlacementFoundations() {
        val center = FaceTargetPositionFactory(FaceTargetMode.CENTER).create(
            BlockPos(1, 2, 3), EnumFacing.NORTH, Vec3(0.0, 0.0, 0.0)
        )
        check(center.approximatelyEquals(Vec3(1.5, 2.5, 3.0)))

        val anchor = BlockPos(0, -1, 0)
        val target = BlockPos(0, 2, 0)
        val support = SupportFeature(object : SupportEnvironment {
            override fun isReplaceable(position: BlockPos): Boolean = position != anchor
            override fun canAnchor(position: BlockPos): Boolean = position == anchor
        }, maximumDepth = 4, maximumNodes = 128).plan(target)

        check(support != null)
        check(support.placements.last() == target)
        check(support.placements.first().y == 0)
        check(support.placements.size == 3)
    }

    private data class GridPoint(val x: Int, val y: Int)
}
