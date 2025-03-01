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
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
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

    private val currentTickTasks = arrayListOf<BooleanSupplier>()
    private val nextTickTasks = arrayListOf<BooleanSupplier>()

    init {
        handler<GameTickEvent>(priority = Byte.MAX_VALUE) {
            currentTickTasks.removeIf { it.asBoolean }
            currentTickTasks += nextTickTasks
            nextTickTasks.clear()
        }
    }

    /**
     * Add a task for scheduling.
     *
     * @param breakLoop Stop tick the body when it returns `true`
     */
    fun schedule(breakLoop: BooleanSupplier) {
        // Prevent modification in removeIf (Continuation.resume)
        mc.addScheduledTask { nextTickTasks += breakLoop }
    }
}