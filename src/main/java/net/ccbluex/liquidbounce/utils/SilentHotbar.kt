/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
@file:Suppress("unused")

package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.minecraft.network.play.client.C09PacketHeldItemChange

object SilentHotbar : Listenable, MinecraftInstance() {

    var hotbarState: SilentHotbarState? = null

    private var ticksSinceLastUpdate: Int = 0
    private var originalSlot: Int? = null

    val currentSlot: Int
        get() = hotbarState?.enforcedSlot ?: mc.thePlayer?.inventory?.currentItem ?: 0

    var ignoreSlotChange = false

    /**
     * Silently switches the player's current slot to the given [slot]
     *
     * [requester] The class that called this function
     * [ticksUntilReset] The amount of ticks it will take to reset
     * [immediate] An option to update the slot server-side
     * [render] Whether the slot should be rendered in MixinGuiInGameMenu/MixinItemRenderer or not
     * [resetManually] When true, the [hotbarState] will reset only when the user switches to another slot themselves.
     */
    fun selectSlotSilently(
        requester: Any?, slot: Int, ticksUntilReset: Int? = null, immediate: Boolean = false,
        render: Boolean = true, resetManually: Boolean = false,
    ) {
        if (originalSlot == null) {
            originalSlot = mc.thePlayer?.inventory?.currentItem ?: 0
        }

        hotbarState = SilentHotbarState(slot, requester, ticksUntilReset, render, resetManually)
        ticksSinceLastUpdate = 0

        if (immediate) {
            mc.playerController?.syncCurrentPlayItem()
        }
    }

    fun resetSlot(requester: Any? = null, immediate: Boolean = false) {
        val state = hotbarState ?: return

        originalSlot = null

        if (requester == null || state.requester == requester) {
            hotbarState = null

            if (requester != null && immediate) {
                mc.playerController?.syncCurrentPlayItem()
            }
        }
    }

    /**
     * Is the slot modified by the given requester?
     */
    fun isSlotModified(requester: Any?) = hotbarState?.requester == requester

    fun updateSilentSlot() {
        val hotbarState = hotbarState ?: return

        hotbarState.resetTicks?.let { ticksUntilReset ->
            if (ticksSinceLastUpdate >= ticksUntilReset) {
                resetSlot(hotbarState.requester)
                return
            }
        }

        ticksSinceLastUpdate++
    }

    fun renderSlot(option: Boolean): Int {
        val player = mc.thePlayer ?: return 0

        val original = player.inventory.currentItem

        val state = hotbarState ?: return original

        return if (option || state.render) currentSlot else original
    }

    private fun shouldReset(slot: Int, other: Int? = originalSlot) = slot != other && hotbarState?.resetManually == true

    @EventTarget
    fun onSlotChange(event: ClientSlotChange) {
        /**
         * Is true only when the server sends S09PacketHeldItemChange to the client.
         */
        if (ignoreSlotChange) {
            event.modifiedSlot = event.supposedSlot

            originalSlot = null
            ignoreSlotChange = false
            return
        }

        /**
         * When the user performs a slot switch and [SilentHotbarState.resetManually] is active,
         * it lets the user's slot change override [currentSlot]
         */
        if (originalSlot != null && shouldReset(event.supposedSlot, originalSlot)) {
            resetSlot()

            event.modifiedSlot = event.supposedSlot
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet !is C09PacketHeldItemChange)
            return

        /**
         * When a class (module usually) directly sends this packet and [SilentHotbarState.resetManually]
         * is active, it lets the class' slot change request override [currentSlot]
         */
        if (shouldReset(packet.slotId, currentSlot))
            resetSlot()
    }

    override fun handleEvents() = mc.thePlayer != null && mc.theWorld != null

}

class SilentHotbarState(
    val enforcedSlot: Int, var requester: Any?, var resetTicks: Int?,
    val render: Boolean, val resetManually: Boolean,
)