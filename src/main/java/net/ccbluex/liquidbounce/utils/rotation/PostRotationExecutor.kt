/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.rotation

/**
 * Schedules actions (attacks, interactions, place-clicks) to fire right after our silent rotation has
 * already been written into the outgoing movement packet ("post-move"), or on the next rotation tick.
 *
 * This guarantees the server processes the rotation before the action, removing the classic
 * rotation/action desync. Modules opt in by handing a task to [runPostMove] / [runNextTick]; nothing
 * runs unless something was scheduled, so it is inert until used.
 *
 * Drained by [RotationUtils]: [onRotationPacketSent] fires only when a rotation actually left this
 * tick (desync guard), and [tick] fires the next-tick queue once per rotation tick.
 */
object PostRotationExecutor {

    /** Hard cap so a module that schedules without anything ever flushing cannot leak. */
    private const val MAX_QUEUED = 32

    private val postMoveTasks = ArrayDeque<Pair<Long, () -> Unit>>()
    private val nextTickTasks = ArrayDeque<() -> Unit>()

    /**
     * Monotonic tick generation, bumped once per [tick]. Lets [tick] tell apart a post-move task that
     * was just queued this tick (still waiting for its rotation packet) from one orphaned in a prior
     * tick because no rotation packet ever left to flush it (e.g. the player stood still, so the idle
     * movement packet is not "rotating"). The orphan is drained as a fallback so a queued action can
     * never get stuck — this is what kept attacks from firing while standing still.
     */
    private var generation = 0L

    /**
     * Run [task] immediately after the next outgoing rotation packet carried our rotation.
     */
    fun runPostMove(task: () -> Unit) {
        if (postMoveTasks.size >= MAX_QUEUED) postMoveTasks.removeFirst()
        postMoveTasks.addLast(generation to task)
    }

    /**
     * Run [task] on the next rotation tick.
     */
    fun runNextTick(task: () -> Unit) {
        if (nextTickTasks.size >= MAX_QUEUED) nextTickTasks.removeFirst()
        nextTickTasks.addLast(task)
    }

    val hasPostMoveTasks: Boolean
        get() = postMoveTasks.isNotEmpty()

    /**
     * Drains the post-move queue. Called right after the rotation packet was sent with our rotation,
     * so the queued actions land directly behind the movement packet in the send channel.
     */
    fun onRotationPacketSent() {
        while (postMoveTasks.isNotEmpty()) {
            val (_, task) = postMoveTasks.removeFirst()
            runCatching { task() }
        }
    }

    /**
     * Drains the next-tick queue. Called once per rotation tick. Also drains any post-move task that
     * was orphaned in an earlier generation (no rotation packet ever left to flush it, e.g. the player
     * stood still) so the action still fires instead of getting stuck — tasks queued this very tick are
     * left untouched here so [onRotationPacketSent] can still fire them directly behind the rotation.
     */
    fun tick() {
        while (nextTickTasks.isNotEmpty()) {
            val task = nextTickTasks.removeFirst()
            runCatching { task() }
        }

        while (postMoveTasks.isNotEmpty() && postMoveTasks.first().first < generation) {
            val (_, task) = postMoveTasks.removeFirst()
            runCatching { task() }
        }

        generation++
    }

    fun clear() {
        postMoveTasks.clear()
        nextTickTasks.clear()
    }
}
