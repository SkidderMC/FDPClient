/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.utils.client.PacketUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.canClickInventory
import net.ccbluex.liquidbounce.utils.inventory.InventoryManager.hasScheduledInLastLoop
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenContainer
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.settings.GameSettings
import net.minecraft.client.settings.KeyBinding
import net.minecraft.init.Blocks
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.potion.Potion
import org.lwjgl.input.Mouse

// Buffer mode keeps its packet state machine next to the options it reads, like the other
// module-level bypasses in this file, so the object carries more helpers than the default limit.
@Suppress("TooManyFunctions")
object InvMove : Module("InvMove", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    /**
     * Classic: the original inventory-move behaviour (key simulation + optional bypasses).
     *
     * Buffer: client-side only packet buffering. While a container/inventory is open the selected
     * outgoing packets are held back, and every [blinkTicks] ticks the server-side window is
     * closed, the held packets are released, the window is reopened and buffering restarts.
     * This keeps the server from ever seeing movement while its own container is open.
     *
     * Tick: no packet tricks at all. While a screen is open the cycle is
     * `[tickInterval]` ticks of standing still followed by `[tickMoveTicks]` ticks of real
     * keyboard movement. Sprint is hard-disabled, the same way Buffer's `SprintMode=Off` does it.
     */
    private val mode by choices("Mode", arrayOf("Classic", "Buffer", "Tick"), "Classic").describe(
        "Classic keeps the normal behaviour. Buffer holds outgoing packets while a container is open. " +
            "Tick only lets the player move on every Nth tick."
    )

    private val isClassic get() = mode == "Classic"
    private val isBuffer get() = mode == "Buffer"
    private val isTick get() = mode == "Tick"

    private val notInChests by boolean("NotInChests", false) { isClassic }
        .describe("Do not allow movement while a chest is open.")
    val aacAdditionPro by boolean("AACAdditionPro", false) { isClassic }
        .describe("Use the AACAdditionPro bypass for moving in menus.")
    private val intave by boolean("Intave", false) { isClassic }
        .describe("Use the Intave bypass for moving in menus.")
    private val saveC0E by boolean("SaveC0E", false) { isClassic }
        .describe("Queue inventory click packets while moving.")
    private val noSprintWhenClosed by boolean("NoSprintWhenClosed", false) { isClassic && saveC0E }
        .describe("Stop sprinting while the inventory is closed.")

    private val isIntave get() = isClassic && (mc.currentScreen is GuiInventory || mc.currentScreen is GuiChest) && intave
    private val clickWindowList = ArrayDeque<C0EPacketClickWindow>()

    private val noMove by shared(InventoryManager.noMoveValue)
    private val noMoveAir by shared(InventoryManager.noMoveAirValue)
    private val noMoveGround by shared(InventoryManager.noMoveGroundValue)
    private val undetectable by shared(InventoryManager.undetectableValue)

    // If player violates nomove check and inventory is open, close inventory and reopen it when still
    private val silentlyCloseAndReopen by boolean(
        "SilentlyCloseAndReopen",
        false
    ) { isClassic && noMove && (noMoveAir || noMoveGround) }
        .describe("Close and later reopen the inventory after a no-move flag.")

    // Reopen closed inventory just before a click (could flag for clicking too fast after opening inventory)
    private val reopenOnClick by boolean(
        "ReopenOnClick",
        false
    ) { isClassic && silentlyCloseAndReopen && noMove && (noMoveAir || noMoveGround) }
        .describe("Reopen the closed inventory right before a click.")

    private val inventoryMotion by float("InventoryMotion", 1F, 0F..2F) { isClassic }
        .describe("Motion multiplier applied while in the inventory.")

    // ──────────────────────────────────────────────
    //  Buffer mode
    // ──────────────────────────────────────────────

    private val sprintMode by choices("SprintMode", arrayOf("Normal", "Off"), "Normal") { isBuffer }
        .describe("Normal keeps the usual sprint handling. Off forces sprinting off while buffering.")

    private val blinkPacket by choices("Blink", arrayOf("C03", "Click", "All"), "C03") { isBuffer }
        .describe("C03 holds player movement/look packets, Click holds window clicks, All holds everything.")

    private val blinkTicks by int("BlinkTicks", 20, 1..200) { isBuffer }
        .describe("Ticks to hold packets before the window is closed and the buffered packets are released.")

    private val sendCloseWindow by boolean("SendCloseWindow", true) { isBuffer }
        .describe("Close the window at the end of a cycle and reopen it on the next tick.")

    // ──────────────────────────────────────────────
    //  Tick mode
    // ──────────────────────────────────────────────

    private val tickInterval by int("TickInterval", 3, 1..40) { isTick }
        .describe("Ticks to wait before the player is allowed to move again.")

    private val tickMoveTicks by int("Ticks", 1, 1..40) { isTick }
        .describe("Ticks the player is allowed to move after TickInterval has elapsed.")

    private enum class BufferPhase {
        /** No container is buffered. */
        Idle,

        /** Outgoing packets selected by [blinkPacket] are being held. */
        Blinking,

        /** The window was closed; the reopen packet is sent on the next tick. */
        WaitingToReopen
    }

    /** Blocks whose right click is a safe "open container" interaction that can be repeated. */
    private val reopenableBlocks = setOf(
        Blocks.chest,
        Blocks.trapped_chest,
        Blocks.ender_chest,
        Blocks.crafting_table,
        Blocks.furnace,
        Blocks.lit_furnace,
        Blocks.dispenser,
        Blocks.dropper,
        Blocks.hopper,
        Blocks.anvil,
        Blocks.brewing_stand,
        Blocks.enchanting_table,
        Blocks.beacon
    )

    /** How long a container interaction may be reused to reopen the same block. */
    private const val REOPEN_TIMEOUT_MS = 2_000L

    private val heldPackets = ArrayList<Packet<*>>()

    @Volatile
    private var bufferPhase = BufferPhase.Idle

    @Volatile
    private var reopenPacket: Packet<*>? = null

    @Volatile
    private var lastContainerInteraction: C08PacketPlayerBlockPlacement? = null

    @Volatile
    private var lastContainerInteractionTime = 0L

    @Volatile
    private var elapsedTicks = 0

    @Volatile
    private var activeWindowId = 0

    @Volatile
    private var clicksHeldThisTick = false

    /**
     * True while the player keeps sprinting inside a container.
     *
     * Opening any GUI runs [net.minecraft.client.Minecraft.setIngameNotInFocus], which clears
     * every [KeyBinding.pressed] flag. A player who was sprinting therefore loses the sprint key
     * for good, even though they never released it.
     */
    private var sprintKept = false

    /**
     * Tick-mode phase counter. Reset while no screen is open, so opening a container always starts
     * at tick 0 — the player first waits [tickInterval] ticks, then moves for [tickMoveTicks].
     */
    private var tickCounter = 0

    /** Total length of one wait + move cycle. */
    private val tickCycleLength get() = tickInterval + tickMoveTicks

    /** True on the ticks where [isTick] lets the player move. */
    private val tickAllowsMovement get() = tickCounter >= tickInterval

    init {
        group("Bypasses", "AACAdditionPro", "Intave").setSupport { isClassic }
        group("PacketSaving", "SaveC0E", "NoSprintWhenClosed").setSupport { isClassic }
        group("NoMove", "SilentlyCloseAndReopen", "ReopenOnClick").setSupport { isClassic }
        group("Misc", "NotInChests", "InventoryMotion").setSupport { isClassic }
        group("Buffer", "SprintMode", "Blink", "BlinkTicks", "SendCloseWindow").setSupport { isBuffer }
        group("Tick", "TickInterval", "Ticks").setSupport { isTick }
    }

    private val affectedBindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    val onUpdate = handler<UpdateEvent>(priority = -1) {
        val player = mc.thePlayer ?: return@handler
        val screen = mc.currentScreen

        if (shouldFreezeInputs(screen)) {
            unPressKeys()
            return@handler
        }

        if (isClassic) {
            if (screen is GuiInventory || screen is GuiChest) {
                player.motionX *= inventoryMotion
                player.motionZ *= inventoryMotion
            }

            if (silentlyCloseAndReopen && screen is GuiInventory) {
                if (canClickInventory(closeWhenViolating = true) && !reopenOnClick) serverOpenInventory = true
            }
        }

        // Tick mode limits the key mirroring itself: only the movement ticks receive the real
        // keys, every other tick is forced to "no input" so the player stops between bursts.
        if (isTick && screen != null) {
            handleTickMode(player)
            return@handler
        }

        for (affectedBinding in affectedBindings)
            affectedBinding.pressed = isButtonPressed(affectedBinding) ||
                affectedBinding == mc.gameSettings.keyBindSprint && Sprint.handleEvents() && Sprint.mode == "Legit" &&
                (!Sprint.onlyOnSprintPress || player.isSprinting) ||
                affectedBinding == mc.gameSettings.keyBindForward && AutoWalk.handleEvents()

        // Buffer decides about sprint on its own; the generic key mirroring above cannot keep a
        // sprint alive because the GUI wipes every key press when it opens.
        if (isBuffer) handleSprint(player)
    }

    /**
     * Tick-mode key handling.
     *
     * There are no sub-options: the player may only move on ticks where `tick % [tickInterval] == 0`
     * and sprinting is always off, exactly like Buffer's `SprintMode=Off`.
     */
    private fun handleTickMode(player: EntityPlayerSP) {
        // Sprint has to go first: both the key and the state, otherwise the vanilla sprint check
        // inside onLivingUpdate starts a sprint on the very next movement tick.
        mc.gameSettings.keyBindSprint.pressed = false
        player.isSprinting = false

        if (!tickAllowsMovement) {
            unPressKeys()
            return
        }

        for (affectedBinding in affectedBindings) {
            if (affectedBinding == mc.gameSettings.keyBindSprint) continue

            affectedBinding.pressed = isButtonPressed(affectedBinding) ||
                affectedBinding == mc.gameSettings.keyBindForward && AutoWalk.handleEvents()
        }
    }

    /**
     * Extra guard for the frozen Tick ticks: modules such as Scaffold rewrite the movement input
     * after the key state was cleared, so the input itself is zeroed as well.
     */
    val onMovementInput = handler<MovementInputEvent> { event ->
        if (!isTick) return@handler
        val screen = mc.currentScreen ?: return@handler
        if (tickAllowsMovement && !shouldFreezeInputs(screen)) return@handler

        event.originalInput.moveForward = 0F
        event.originalInput.moveStrafe = 0F
        event.originalInput.jump = false
    }

    /**
     * Buffer-mode sprint control.
     *
     * `Minecraft.displayGuiScreen` calls `setIngameNotInFocus`, which runs
     * [KeyBinding.unPressAllKeys]. Every `pressed` flag is cleared, so both the sprint key and the
     * sprint state are lost the moment a container opens — moving still works because the key
     * mirroring above re-presses WASD from the real keyboard, but sprint silently degrades to
     * walking speed.
     *
     * `Normal` re-asserts the sprint key from the real keyboard and keeps the sprint state alive
     * while the player holds forward, exactly like vanilla does outside a GUI. `Off` hard-disables
     * sprint for the whole buffer cycle.
     */
    private fun handleSprint(player: EntityPlayerSP) {
        val sprintBinding = mc.gameSettings.keyBindSprint

        if (sprintMode == "Off") {
            sprintKept = false
            sprintBinding.pressed = false
            player.isSprinting = false
            return
        }

        // The latch only spans one container visit: leaving the GUI or letting go of forward
        // hands sprint back to vanilla, so Normal never turns into a permanent auto-sprint.
        val forwardHeld = isButtonPressed(mc.gameSettings.keyBindForward) || AutoWalk.handleEvents()
        val insideContainer = mc.currentScreen != null && forwardHeld

        if (insideContainer && player.isSprinting) sprintKept = true
        if (!insideContainer) sprintKept = false

        val keyDown = isButtonPressed(sprintBinding) || sprintModuleWantsSprint(player)

        sprintBinding.pressed = keyDown || sprintKept

        // Vanilla can only (re)start a sprint while the key is down. The key state is the only
        // thing the GUI destroyed, so restart it with the conditions vanilla itself uses.
        if (needsSprintRestart(player, keyDown || sprintKept)) player.isSprinting = true
    }

    /** Whether a sprint that vanilla would have started has to be re-applied by hand. */
    private fun needsSprintRestart(player: EntityPlayerSP, wanted: Boolean): Boolean =
        wanted && !player.isSprinting && canSprint(player)

    /** Whether the Sprint module forces the sprint key down on its own. */
    private fun sprintModuleWantsSprint(player: EntityPlayerSP): Boolean =
        Sprint.handleEvents() && Sprint.mode == "Legit" && (!Sprint.onlyOnSprintPress || player.isSprinting)

    /** The vanilla conditions for starting a sprint while the player already holds forward. */
    private fun canSprint(player: EntityPlayerSP): Boolean =
        player.movementInput.moveForward >= 0.8F && !player.isUsingItem && sprintAllowed(player)

    private fun sprintAllowed(player: EntityPlayerSP): Boolean =
        (player.foodStats.foodLevel > 6F || player.capabilities.allowFlying) &&
            !player.isCollidedHorizontally && !player.isCollidedHorizontally && !player.isPotionActive(Potion.blindness)

    private fun shouldFreezeInputs(screen: GuiScreen?): Boolean {
        // Don't make player move when chat or ESC menu are open
        if (screen is GuiChat || screen is GuiIngameMenu) return true

        // Undetectable/NotInChests are classic-only bypasses; Buffer and Tick decide on their own
        // when to move.
        if (!isClassic) return false

        if (undetectable && (screen != null && screen !is GuiHudDesigner && screen !is ClickGui)) return true

        if (notInChests && screen is GuiChest) return true

        return false
    }

    /** Whether other movement modules may override keys while this screen is open. */
    fun allowsMovementOverride(screen: GuiScreen?): Boolean =
        handleEvents() && screen != null && !shouldFreezeInputs(screen) &&
            (!isTick || tickAllowsMovement)

    /**
     * Whether InvMove owns sprint and forbids it while a screen is open.
     *
     * [Sprint.correctSprintState] runs after the key mirroring and would otherwise start a sprint
     * again as soon as the player moves inside the container, defeating `SprintMode=Off` and the
     * Tick mode. Sprint checks this before doing any of its own work.
     */
    fun blocksSprint(): Boolean =
        handleEvents() && mc.currentScreen != null && (isTick || isBuffer && sprintMode == "Off")

    val onStrafe = handler<StrafeEvent> {
        if (isIntave) {
            mc.gameSettings.keyBindSneak.pressed = true
        }
    }

    val onJump = handler<JumpEvent> { event ->
        if (isIntave) event.cancelEvent()
    }

    val onClick = handler<ClickWindowEvent> { event ->
        if (!isClassic) return@handler

        if (!canClickInventory()) event.cancelEvent()
        else if (reopenOnClick) {
            hasScheduledInLastLoop = false
            serverOpenInventory = true
        }
    }

    /** Starts a buffer cycle when the player opens their own inventory GUI. */
    val onScreen = handler<ScreenEvent> { event ->
        if (!isBuffer) return@handler

        // Closing a screen does not always send a close packet (a module can swap the screen
        // directly), so the buffer is released here as well to avoid holding packets forever.
        // WaitingToReopen is the exception: our own C0D makes vanilla close the GUI too, and the
        // reopen is already scheduled for the next tick.
        if (event.guiScreen == null) {
            if (bufferPhase == BufferPhase.Blinking) stopBuffer(flush = true)
            return@handler
        }

        if (event.guiScreen !is GuiInventory) return@handler
        if (bufferPhase != BufferPhase.Idle) return@handler

        startBuffer(C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT), windowId = 0)
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        val player = mc.thePlayer ?: return@handler

        if (isBuffer) {
            handleBufferPacket(event)
            return@handler
        }

        // SaveC0E is a Classic-only bypass; Tick must never inherit it after a mode switch.
        if (!isClassic || !saveC0E) return@handler

        if (noSprintWhenClosed) {
            if (clickWindowList.isNotEmpty() && !(serverOpenInventory || serverOpenContainer)) player.isSprinting =
                false

            if (packet is C0DPacketCloseWindow) {
                event.cancelEvent()
                player.isSprinting = false
                if (!player.serverSprintState) PacketUtils.sendPacket(C0DPacketCloseWindow(), false)
            }
        }

        if (serverOpenInventory || serverOpenContainer) {
            if (packet is C0EPacketClickWindow) {
                clickWindowList.add(packet)
                event.cancelEvent()
            }
        } else if (clickWindowList.isNotEmpty()) {
            clickWindowList.forEach {
                PacketUtils.sendPacket(it, false)
            }
            clickWindowList.clear()
        }
    }

    /**
     * Buffer-mode packet pipeline.
     *
     * Runs on the Netty thread for [EventState.SEND]/[EventState.RECEIVE]; shared state is either
     * volatile or guarded by [heldPackets] so the tick handler on the render thread stays coherent.
     */
    private fun handleBufferPacket(event: PacketEvent) {
        val packet = event.packet

        if (event.eventType == EventState.RECEIVE) {
            when (packet) {
                is S2DPacketOpenWindow -> onServerWindowOpened(packet)

                // The server answers our own C0D with S2E. While WaitingToReopen that answer must
                // be ignored, otherwise the pending reopen would be cancelled and the cycle dies.
                is S2EPacketCloseWindow -> if (bufferPhase == BufferPhase.Blinking) stopBuffer(flush = true)
            }
            return
        }

        if (event.isCancelled) return

        // Remember the interaction that opened a block-based container so it can be replayed later.
        if (packet is C08PacketPlayerBlockPlacement) rememberContainerInteraction(packet)

        // The player closed the container themselves: release everything we still hold. The close
        // itself is re-sent silently first, otherwise the buffered packets would reach the server
        // before the close packet that makes them legal.
        if (packet is C0DPacketCloseWindow) {
            if (bufferPhase == BufferPhase.Blinking) {
                event.cancelEvent()
                PacketUtils.sendPacket(packet, false)
                stopBuffer(flush = true)
            } else if (bufferPhase == BufferPhase.WaitingToReopen) {
                // A duplicate close while the reopen is pending would only desync the window id.
                event.cancelEvent()
            }
            return
        }

        // Packets sent while the reopen packet is pending must go out, otherwise the one tick the
        // server sees the window as closed would still be hidden and the cycle would never sync.
        if (bufferPhase != BufferPhase.Blinking) return
        if (!shouldHold(packet)) return

        event.cancelEvent()
        synchronized(heldPackets) { heldPackets += packet }

        if (packet is C0EPacketClickWindow) clicksHeldThisTick = true
    }

    private fun onServerWindowOpened(packet: S2DPacketOpenWindow) {
        val now = System.currentTimeMillis()
        val interaction = lastContainerInteraction
        val reusable = interaction != null && now - lastContainerInteractionTime <= REOPEN_TIMEOUT_MS

        // Plugin-driven GUIs (shops, menus) have no block to right click again. Such a window is
        // buffered but never closed, so the client and the server keep the same window.
        startBuffer(if (reusable) interaction else null, packet.windowId)
    }

    private fun startBuffer(reopen: Packet<*>?, windowId: Int) {
        if (!isBuffer) return

        if (bufferPhase == BufferPhase.Idle) {
            synchronized(heldPackets) { heldPackets.clear() }
            elapsedTicks = 0
            clicksHeldThisTick = false
        }

        reopenPacket = reopen
        activeWindowId = windowId
        bufferPhase = BufferPhase.Blinking
    }

    private fun rememberContainerInteraction(packet: C08PacketPlayerBlockPlacement) {
        // Directions outside 0..5 are the dummy "use item" packets some modules send, not a block click.
        if (packet.placedBlockDirection !in 0..5) return

        lastContainerInteraction = packet
        lastContainerInteractionTime = System.currentTimeMillis()
    }

    private fun shouldHold(packet: Packet<*>): Boolean = when (blinkPacket) {
        "C03" -> packet is C03PacketPlayer
        "Click" -> packet is C0EPacketClickWindow
        else -> when (packet) {
            // Never swallow the packets the buffer itself needs to stay in sync.
            is C0DPacketCloseWindow, is C16PacketClientStatus -> false
            else -> true
        }
    }

    val onGameTick = handler<GameTickEvent> {
        if (isTick) {
            // GameTick fires at the end of runTick, so the value the next UpdateEvent reads is
            // exactly "how many ticks were already spent on this screen".
            tickCounter = if (mc.currentScreen == null) 0 else (tickCounter + 1) % tickCycleLength
            return@handler
        }

        if (!isBuffer) return@handler

        when (bufferPhase) {
            BufferPhase.Blinking -> {
                elapsedTicks++

                // A tick without new clicks ends the click burst, so release it immediately.
                if (blinkPacket == "Click" && !clicksHeldThisTick) flushHeldClicks()
                clicksHeldThisTick = false

                if (elapsedTicks >= blinkTicks) endBufferCycle()
            }

            BufferPhase.WaitingToReopen -> {
                if (reopenWindow()) {
                    elapsedTicks = 0
                    clicksHeldThisTick = false
                    bufferPhase = BufferPhase.Blinking
                } else {
                    stopBuffer(flush = true)
                }
            }

            BufferPhase.Idle -> Unit
        }
    }

    /** Ends the current hold period and either closes the window or restarts buffering directly. */
    private fun endBufferCycle() {
        // Without a replayable reopen packet, closing would desync the client GUI from the server.
        val shouldClose = sendCloseWindow && reopenPacket != null

        if (!shouldClose) {
            flushHeld()
            elapsedTicks = 0
            return
        }

        // Clicks are only valid while the server still has the window open, so they go first.
        flushHeldClicks()

        // Switch the phase before sending: the server's S2E answer must never be mistaken for an
        // unexpected close while the remaining packets are still being released.
        bufferPhase = BufferPhase.WaitingToReopen
        elapsedTicks = 0

        PacketUtils.sendPacket(C0DPacketCloseWindow(activeWindowId), false)

        // Movement is released only after the close, so the server never sees it with an open window.
        flushHeld()
    }

    /** Sends the reopen packet. Returns false when it is no longer safe to replay. */
    private fun reopenWindow(): Boolean {
        // The packet stays armed: the same inventory/block interaction is replayed every cycle.
        val packet = reopenPacket ?: return false

        when (packet) {
            is C16PacketClientStatus -> Unit
            is C08PacketPlayerBlockPlacement -> {
                val world = mc.theWorld ?: return false
                if (world.getBlockState(packet.position).block !in reopenableBlocks) return false
            }
            else -> return false
        }

        PacketUtils.sendPacket(packet, false)

        // Silent sends skip the packet event, so refresh the replay source for the next cycle.
        if (packet is C08PacketPlayerBlockPlacement) {
            lastContainerInteraction = packet
            lastContainerInteractionTime = System.currentTimeMillis()
        }

        return true
    }

    private fun flushHeld() = sendSilently(
        synchronized(heldPackets) { heldPackets.toList().also { heldPackets.clear() } }
    )

    private fun takeHeldClicks(): List<Packet<*>> = synchronized(heldPackets) {
        val clicks = heldPackets.filterIsInstance<C0EPacketClickWindow>()
        if (clicks.isNotEmpty()) heldPackets.removeAll { it is C0EPacketClickWindow }
        clicks
    }

    private fun flushHeldClicks() = sendSilently(takeHeldClicks())

    private fun sendSilently(packets: List<Packet<*>>) {
        // Sends go out one by one in queue order; a vararg spread would copy the whole array first.
        for (packet in packets) PacketUtils.sendPacket(packet, false)
    }

    private fun stopBuffer(flush: Boolean) {
        bufferPhase = BufferPhase.Idle
        elapsedTicks = 0
        clicksHeldThisTick = false
        reopenPacket = null

        val pending = synchronized(heldPackets) {
            heldPackets.toList().also { heldPackets.clear() }
        }

        if (flush) sendSilently(pending)
    }

    val onWorld = handler<WorldEvent> { event ->
        if (event.worldClient == null) {
            // Never flush into a world we are leaving.
            stopBuffer(flush = false)
            lastContainerInteraction = null
            lastContainerInteractionTime = 0L
        }
    }

    override fun onDisable() {
        sprintKept = false
        tickCounter = 0

        for (affectedBinding in affectedBindings) affectedBinding.pressed = isButtonPressed(affectedBinding)

        if (isIntave && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) mc.gameSettings.keyBindSneak.pressed = false

        clickWindowList.forEach { PacketUtils.sendPacket(it, false) }
        clickWindowList.clear()

        stopBuffer(flush = true)
    }

    private fun isButtonPressed(keyBinding: KeyBinding): Boolean {
        return if (keyBinding.keyCode < 0) {
            Mouse.isButtonDown(keyBinding.keyCode + 100)
        } else {
            GameSettings.isKeyDown(keyBinding)
        }
    }

    private fun unPressKeys() {
        affectedBindings.forEach {
            it.pressed = false
        }
    }

    override val tag
        get() = when {
            isBuffer -> "Buffer"
            isTick -> "Tick"
            aacAdditionPro -> "AACAdditionPro"
            inventoryMotion != 1F -> inventoryMotion.toString()
            else -> null
        }
}
