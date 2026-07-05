/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.Dispatchers
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.ClientUtils
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BooleanSupplier
import kotlin.coroutines.RestrictsSuspension

/**
 * This manager is for suspend tick functions.
 *
 * **ANY** scopes without [RestrictsSuspension] annotation can use wait actions.
 *
 * Note: These functions will be called on [Dispatchers.Main] (the Render thread).
 *
 * Most of the game events are called from the Render thread, except of [PacketEvent], it's called from the Netty client thread.
 * You should carefully use this to prevent unexpected thread issue.
 *
 * @author MukjepScarlet
 */
object TickScheduler : Listenable, MinecraftInstance {

    private const val MAX_SCHEDULED_TASKS = 8192

    private val currentTickTasks = arrayListOf<ScheduledTask>()
    private val pendingTickTasks = ConcurrentLinkedQueue<ScheduledTask>()
    private val requesterTasks = ConcurrentHashMap<Any, MutableSet<ScheduledTask>>()
    private val taskCount = AtomicInteger()

    init {
        handler<GameTickEvent>(priority = Byte.MAX_VALUE) {
            drainPendingTasks()
            currentTickTasks.removeIf(::runTask)
        }
    }

    /**
     * Add a task for scheduling.
     *
     * @param breakLoop Stop tick the body when it returns `true`
     */
    fun schedule(breakLoop: BooleanSupplier): Boolean = enqueue(null, breakLoop)

    /** Runs [action] after exactly [ticks] client ticks. */
    fun scheduleAfter(ticks: Int, requester: Any? = null, action: () -> Unit = {}): Boolean {
        require(ticks >= 0) { "Negative tick delay: $ticks" }

        if (ticks == 0) {
            action()
            return true
        }

        var elapsed = 0
        return enqueue(requester, BooleanSupplier {
            elapsed++
            if (elapsed < ticks) {
                false
            } else {
                action()
                true
            }
        })
    }

    /**
     * Runs [action] once per tick until it returns `true`. A `null` result keeps the task alive
     * until [maxTicks], or removes it immediately when no maximum was supplied.
     */
    fun scheduleConditional(
        requester: Any? = null,
        maxTicks: Int? = null,
        action: (elapsedTicks: Int) -> Boolean?,
    ): Boolean {
        require(maxTicks == null || maxTicks >= 0) { "Negative maximum tick count: $maxTicks" }

        if (maxTicks == 0) {
            return true
        }

        var elapsed = 0
        return enqueue(requester, BooleanSupplier {
            elapsed++
            action(elapsed) ?: (maxTicks == null || elapsed >= maxTicks)
        })
    }

    fun hasScheduled(requester: Any): Boolean =
        requesterTasks[requester]?.any { !it.cancelled } == true

    fun cancel(requester: Any): Int {
        val tasks = requesterTasks.remove(requester) ?: return 0
        tasks.forEach { it.cancelled = true }
        return tasks.size
    }

    private fun enqueue(requester: Any?, action: BooleanSupplier): Boolean {
        if (taskCount.incrementAndGet() > MAX_SCHEDULED_TASKS) {
            taskCount.decrementAndGet()
            ClientUtils.LOGGER.warn("[TickScheduler] Task queue full, dropping task")
            return false
        }

        val task = ScheduledTask(requester, action)
        if (requester != null) {
            requesterTasks.computeIfAbsent(requester) { ConcurrentHashMap.newKeySet() }.add(task)
        }
        pendingTickTasks += task
        return true
    }

    private fun runTask(task: ScheduledTask): Boolean {
        val complete = task.cancelled || runCatching(task.action::getAsBoolean).getOrElse {
            ClientUtils.LOGGER.error("[TickScheduler] Scheduled task failed", it)
            true
        }

        if (complete) complete(task)
        return complete
    }

    private fun complete(task: ScheduledTask) {
        taskCount.decrementAndGet()
        task.requester?.let { requester ->
            requesterTasks.computeIfPresent(requester) { _, tasks ->
                tasks.remove(task)
                tasks.takeUnless(MutableSet<ScheduledTask>::isEmpty)
            }
        }
    }

    private fun drainPendingTasks() {
        while (true) {
            currentTickTasks += pendingTickTasks.poll() ?: break
        }
    }

    private class ScheduledTask(
        val requester: Any?,
        val action: BooleanSupplier,
    ) {
        @Volatile
        var cancelled = false
    }
}
