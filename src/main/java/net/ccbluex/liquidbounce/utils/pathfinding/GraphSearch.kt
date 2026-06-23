/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.pathfinding

import java.util.PriorityQueue

data class WeightedEdge<N>(val destination: N, val cost: Double) {
    init {
        require(cost.isFinite() && cost >= 0.0) { "Graph edge cost must be finite and non-negative" }
    }
}

data class SearchLimits(
    val maximumCost: Double = Double.POSITIVE_INFINITY,
    val maximumExpandedNodes: Int = 10_000
) {
    init {
        require(!maximumCost.isNaN() && maximumCost >= 0.0) { "Maximum cost must be non-negative" }
        require(maximumExpandedNodes > 0) { "Maximum expanded nodes must be positive" }
    }
}

enum class SearchTermination {
    GOAL_REACHED,
    FRONTIER_EXHAUSTED,
    COST_LIMIT,
    NODE_LIMIT
}

/** The path is always non-empty and acts as a safe best-effort fallback when the goal is not reached. */
data class ShortestPath<N>(
    val nodes: List<N>,
    val totalCost: Double,
    val reachedGoal: Boolean,
    val expandedNodes: Int,
    val termination: SearchTermination
)

object GraphSearch {
    fun <N> dijkstra(
        start: N,
        isGoal: (N) -> Boolean,
        edges: (N) -> Iterable<WeightedEdge<N>>,
        limits: SearchLimits = SearchLimits()
    ): ShortestPath<N> = search(start, isGoal, edges, { 0.0 }, limits)

    fun <N> aStar(
        start: N,
        isGoal: (N) -> Boolean,
        edges: (N) -> Iterable<WeightedEdge<N>>,
        heuristic: (N) -> Double,
        limits: SearchLimits = SearchLimits()
    ): ShortestPath<N> = search(start, isGoal, edges, heuristic, limits)

    private fun <N> search(
        start: N,
        isGoal: (N) -> Boolean,
        edges: (N) -> Iterable<WeightedEdge<N>>,
        heuristic: (N) -> Double,
        limits: SearchLimits
    ): ShortestPath<N> {
        data class QueueEntry<N>(val node: N, val cost: Double, val estimatedTotal: Double, val order: Long)

        val queue = PriorityQueue<QueueEntry<N>>(compareBy<QueueEntry<N>> { it.estimatedTotal }
            .thenBy { it.cost }
            .thenBy { it.order })
        val costs = HashMap<N, Double>()
        val parents = HashMap<N, N>()

        var insertionOrder = 0L
        var expanded = 0
        var bestNode = start
        var bestHeuristic = checkedHeuristic(heuristic(start))
        var bestCost = 0.0
        var sawCostLimitedEdge = false

        costs[start] = 0.0
        queue.add(QueueEntry(start, 0.0, bestHeuristic, insertionOrder++))

        while (queue.isNotEmpty()) {
            if (expanded >= limits.maximumExpandedNodes) {
                return result(bestNode, costs, parents, false, expanded, SearchTermination.NODE_LIMIT)
            }

            val current = queue.remove()
            val knownCost = costs[current.node] ?: continue
            if (current.cost > knownCost) continue

            if (isGoal(current.node)) {
                return result(current.node, costs, parents, true, expanded, SearchTermination.GOAL_REACHED)
            }

            expanded++
            for (edge in edges(current.node)) {
                val candidateCost = knownCost + edge.cost
                if (!candidateCost.isFinite() || candidateCost > limits.maximumCost) {
                    sawCostLimitedEdge = true
                    continue
                }
                if (candidateCost >= (costs[edge.destination] ?: Double.POSITIVE_INFINITY)) continue

                val estimate = checkedHeuristic(heuristic(edge.destination))
                costs[edge.destination] = candidateCost
                parents[edge.destination] = current.node
                queue.add(QueueEntry(edge.destination, candidateCost, candidateCost + estimate, insertionOrder++))

                if (estimate < bestHeuristic || estimate == bestHeuristic && candidateCost < bestCost) {
                    bestNode = edge.destination
                    bestHeuristic = estimate
                    bestCost = candidateCost
                }
            }
        }

        val termination = if (sawCostLimitedEdge) SearchTermination.COST_LIMIT else SearchTermination.FRONTIER_EXHAUSTED
        return result(bestNode, costs, parents, false, expanded, termination)
    }

    private fun checkedHeuristic(value: Double): Double {
        require(value.isFinite() && value >= 0.0) { "Graph heuristic must be finite and non-negative" }
        return value
    }

    private fun <N> result(
        end: N,
        costs: Map<N, Double>,
        parents: Map<N, N>,
        reachedGoal: Boolean,
        expandedNodes: Int,
        termination: SearchTermination
    ): ShortestPath<N> {
        val reversed = ArrayList<N>()
        var cursor = end
        reversed.add(cursor)
        while (true) {
            cursor = parents[cursor] ?: break
            reversed.add(cursor)
        }
        reversed.reverse()
        return ShortestPath(reversed, costs[end] ?: 0.0, reachedGoal, expandedNodes, termination)
    }
}

fun <N> dijkstraShortestPath(
    start: N,
    isGoal: (N) -> Boolean,
    edges: (N) -> Iterable<WeightedEdge<N>>,
    limits: SearchLimits = SearchLimits()
): ShortestPath<N> = GraphSearch.dijkstra(start, isGoal, edges, limits)

fun <N> aStarShortestPath(
    start: N,
    isGoal: (N) -> Boolean,
    edges: (N) -> Iterable<WeightedEdge<N>>,
    heuristic: (N) -> Double,
    limits: SearchLimits = SearchLimits()
): ShortestPath<N> = GraphSearch.aStar(start, isGoal, edges, heuristic, limits)
