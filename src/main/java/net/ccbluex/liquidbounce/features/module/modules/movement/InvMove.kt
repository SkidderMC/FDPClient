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

@Suppress("TooManyFunctions")
object InvMove : Module("InvMove", Category.MOVEMENT, Category.SubCategory.MOVEMENT_EXTRAS, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Classic", "Buffer"), "Classic").describe(
        "Classic keeps the normal behaviour. Buffer combines independent packet buffering (Blink) " +
            "and tick-limited movement (Tick)."
    )

    private val isClassic get() = mode == "Classic"
    private val isBuffer get() = mode == "Buffer"

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

    private val silentlyCloseAndReopen by boolean(
        "SilentlyCloseAndReopen",
        false
    ) { isClassic && noMove && (noMoveAir || noMoveGround) }
        .describe("Close and later reopen the inventory after a no-move flag.")

    private val reopenOnClick by boolean(
        "ReopenOnClick",
        false
    ) { isClassic && silentlyCloseAndReopen && noMove && (noMoveAir || noMoveGround) }
        .describe("Reopen the closed inventory right before a click.")

    private val inventoryMotion by float("InventoryMotion", 1F, 0F..2F) { isClassic }
        .describe("Motion multiplier applied while in the inventory.")

    private val blink by boolean("Blink", true) { isBuffer }
        .describe(
            "Hold outgoing packets while a container is open and release them every BlinkTicks ticks. " +
                "This is the original Buffer behaviour and can run together with Tick."
        )

    private val blinkPacket by choices("BlinkMode", arrayOf("C03", "Click", "All"), "C03") { isBuffer && blink }
        .describe("C03 holds player movement/look packets, Click holds window clicks, All holds everything.")

    private val blinkTicks by int("BlinkTicks", 20, 1..200) { isBuffer && blink }
        .describe("Ticks to hold packets before the window is closed and the buffered packets are released.")

    private val sendCloseWindow by boolean("SendCloseWindow", true) { isBuffer && blink }
        .describe("Close the window at the end of a cycle and reopen it on the next tick.")

    private val tickLimit by boolean("Tick", false) { isBuffer }
        .describe(
            "Only let the player move on every Nth tick. Independent of Blink, so both can be active " +
                "at the same time."
        )

    private val tickInterval by int("TickInterval", 3, 1..40) { isBuffer && tickLimit }
        .describe("Ticks to wait before the player is allowed to move again.")

    private val tickMoveTicks by int("Ticks", 1, 1..40) { isBuffer && tickLimit }
        .describe("Ticks the player is allowed to move after TickInterval has elapsed.")

    private val sprintMode by choices("SprintMode", arrayOf("Normal", "Off"), "Normal") {
        isBuffer && blink && !tickLimit
    }
        .describe("Normal keeps the usual sprint handling. Off forces sprinting off while buffering.")

    private val isBlinking get() = isBuffer && blink

    private val tickLimited get() = isBuffer && tickLimit

    private enum class BufferPhase {
        Idle,

        Blinking,

        WaitingToReopen
    }

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

    private var sprintKept = false

    private var tickCounter = 0

    private val tickCycleLength get() = tickInterval + tickMoveTicks

    private val tickAllowsMovement get() = tickCounter >= tickInterval

    init {
        group("Bypasses", "AACAdditionPro", "Intave").setSupport { isClassic }
        group("PacketSaving", "SaveC0E", "NoSprintWhenClosed").setSupport { isClassic }
        group("NoMove", "SilentlyCloseAndReopen", "ReopenOnClick").setSupport { isClassic }
        group("Misc", "NotInChests", "InventoryMotion").setSupport { isClassic }
        group(
            "Buffer",
            "Blink", "BlinkMode", "BlinkTicks", "SendCloseWindow",
            "Tick", "TickInterval", "Ticks",
            "SprintMode"
        ).setSupport { isBuffer }
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

        syncBufferLifetime()

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

        if (tickLimited && screen != null) {
            handleTickMode(player)
            return@handler
        }

        for (affectedBinding in affectedBindings)
            affectedBinding.pressed = isButtonPressed(affectedBinding) ||
                affectedBinding == mc.gameSettings.keyBindSprint && Sprint.handleEvents() && Sprint.mode == "Legit" &&
                (!Sprint.onlyOnSprintPress || player.isSprinting) ||
                affectedBinding == mc.gameSettings.keyBindForward && AutoWalk.handleEvents()

        if (isBlinking && !tickLimited) handleSprint(player)
    }

    private fun handleTickMode(player: EntityPlayerSP) {
        sprintKept = false
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

    val onMovementInput = handler<MovementInputEvent> { event ->
        if (!tickLimited) return@handler
        val screen = mc.currentScreen ?: return@handler
        if (tickAllowsMovement && !shouldFreezeInputs(screen)) return@handler

        event.originalInput.moveForward = 0F
        event.originalInput.moveStrafe = 0F
        event.originalInput.jump = false
    }

    private fun handleSprint(player: EntityPlayerSP) {
        val sprintBinding = mc.gameSettings.keyBindSprint

        if (mc.currentScreen == null) {
            sprintKept = false
            return
        }

        if (sprintMode == "Off") {
            sprintKept = false
            sprintBinding.pressed = false
            player.isSprinting = false
            return
        }

        val forwardHeld = isButtonPressed(mc.gameSettings.keyBindForward) || AutoWalk.handleEvents()

        if (forwardHeld && player.isSprinting) sprintKept = true
        if (!forwardHeld) sprintKept = false

        val keyDown = isButtonPressed(sprintBinding) || sprintModuleWantsSprint(player)

        sprintBinding.pressed = keyDown || sprintKept

        if (needsSprintRestart(player, keyDown || sprintKept)) player.isSprinting = true
    }

    private fun needsSprintRestart(player: EntityPlayerSP, wanted: Boolean): Boolean =
        wanted && !player.isSprinting && canSprint(player)

    private fun sprintModuleWantsSprint(player: EntityPlayerSP): Boolean =
        Sprint.handleEvents() && Sprint.mode == "Legit" && (!Sprint.onlyOnSprintPress || player.isSprinting)

    private fun canSprint(player: EntityPlayerSP): Boolean =
        player.movementInput.moveForward >= 0.8F && !player.isUsingItem && sprintAllowed(player)

    private fun sprintAllowed(player: EntityPlayerSP): Boolean =
        (player.foodStats.foodLevel > 6F || player.capabilities.allowFlying) &&
            !player.isCollidedHorizontally && !player.isCollidedHorizontally && !player.isPotionActive(Potion.blindness)

    private fun shouldFreezeInputs(screen: GuiScreen?): Boolean {
        if (screen is GuiChat || screen is GuiIngameMenu) return true

        if (!isClassic) return false

        if (undetectable && (screen != null && screen !is GuiHudDesigner && screen !is ClickGui)) return true

        if (notInChests && screen is GuiChest) return true

        return false
    }

    fun allowsMovementOverride(screen: GuiScreen?): Boolean =
        handleEvents() && screen != null && !shouldFreezeInputs(screen) &&
            (!tickLimited || tickAllowsMovement)

    fun blocksSprint(): Boolean =
        handleEvents() && mc.currentScreen != null && (tickLimited || isBlinking && sprintMode == "Off")

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

    val onScreen = handler<ScreenEvent> { event ->
        if (!isBlinking) return@handler

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
            if (isBlinking) handleBufferPacket(event)
            return@handler
        }

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

    private fun handleBufferPacket(event: PacketEvent) {
        val packet = event.packet

        if (event.eventType == EventState.RECEIVE) {
            when (packet) {
                is S2DPacketOpenWindow -> onServerWindowOpened(packet)

                is S2EPacketCloseWindow -> if (bufferPhase == BufferPhase.Blinking) stopBuffer(flush = true)
            }
            return
        }

        if (event.isCancelled) return

        if (packet is C08PacketPlayerBlockPlacement) rememberContainerInteraction(packet)

        if (packet is C0DPacketCloseWindow) {
            if (bufferPhase == BufferPhase.Blinking) {
                event.cancelEvent()
                PacketUtils.sendPacket(packet, false)
                stopBuffer(flush = true)
            } else if (bufferPhase == BufferPhase.WaitingToReopen) {
                event.cancelEvent()
            }
            return
        }

        if (bufferPhase != BufferPhase.Blinking) return
        if (!shouldHold(packet)) return

        event.cancelEvent()
        synchronized(heldPackets) { heldPackets += packet }

        if (packet is C0EPacketClickWindow) clicksHeldThisTick = true
    }

    private fun onServerWindowOpened(packet: S2DPacketOpenWindow) {
        if (packet.windowId == 0) {
            startBuffer(C16PacketClientStatus(OPEN_INVENTORY_ACHIEVEMENT), 0)
            return
        }

        val now = System.currentTimeMillis()
        val interaction = lastContainerInteraction
        val reusable = interaction != null && now - lastContainerInteractionTime <= REOPEN_TIMEOUT_MS

        startBuffer(if (reusable) interaction else null, packet.windowId)
    }

    private fun startBuffer(reopen: Packet<*>?, windowId: Int) {
        if (!isBlinking) return

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
        if (packet.placedBlockDirection !in 0..5) return

        lastContainerInteraction = packet
        lastContainerInteractionTime = System.currentTimeMillis()
    }

    private fun shouldHold(packet: Packet<*>): Boolean = when (blinkPacket) {
        "C03" -> packet is C03PacketPlayer
        "Click" -> packet is C0EPacketClickWindow
        else -> when (packet) {
            is C0DPacketCloseWindow, is C16PacketClientStatus -> false
            else -> true
        }
    }

    val onGameTick = handler<GameTickEvent> {
        val screen = mc.currentScreen

        tickCounter = if (tickLimited && screen != null) (tickCounter + 1) % tickCycleLength else 0

        if (!isBlinking) {
            syncBufferLifetime()
            return@handler
        }

        when (bufferPhase) {
            BufferPhase.Blinking -> {
                elapsedTicks++

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

    private fun endBufferCycle() {
        val shouldClose = sendCloseWindow && reopenPacket != null

        if (!shouldClose) {
            flushHeld()
            elapsedTicks = 0
            return
        }

        flushHeldClicks()

        bufferPhase = BufferPhase.WaitingToReopen
        elapsedTicks = 0

        PacketUtils.sendPacket(C0DPacketCloseWindow(activeWindowId), false)

        flushHeld()
    }

    private fun reopenWindow(): Boolean {
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

    private fun syncBufferLifetime() {
        if (isBlinking) return

        sprintKept = false

        if (bufferPhase != BufferPhase.Idle) stopBuffer(flush = true)
    }

    val onWorld = handler<WorldEvent> { event ->
        if (event.worldClient == null) {
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
            isBlinking && tickLimited -> "Blink+Tick"
            isBlinking -> "Buffer"
            tickLimited -> "Tick"
            isBuffer -> "Idle"
            aacAdditionPro -> "AACAdditionPro"
            inventoryMotion != 1F -> inventoryMotion.toString()
            else -> null
        }
}
