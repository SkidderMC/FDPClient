/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.hasMotion
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.item.*
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.DROP_ITEM
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.math.sqrt

object NoSlow : Module("NoSlow", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    private val swordMode by choices(
        "SwordMode",
        arrayOf(
            "None", "NCP", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08",
            "Blink", "Grim2371", "OldIntave", "Medusa", "HypixelNew", "SpamItemChange",
            "SpamPlace", "SpamEmptyPlace", "Matrix", "GrimAC", "Prediction", "Prediction2"
        ),
        "None"
    )
        .describe("Anticheat bypass used while blocking with a sword.")

    private val reblinkTicks by int("ReblinkTicks", 10, 1..20) { swordMode == "Blink" }
        .describe("Ticks before re-applying the blink in blink mode.")
    private val antiSwitchItem by boolean("AntiSwitchItem", false)
        .describe("Cancel server-forced item switches while using an item.")
    private val onlyGround by boolean("OnlyGround", false)
        .describe("Only apply no-slow while on the ground.")
    private val onlyMove by boolean("OnlyMove", false)
        .describe("Only apply no-slow while moving.")
    private val aac4C07 by boolean("AAC4-C07", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }
        .describe("Send a C07 packet for the AAC4 bypass.")
    private val aac4C08 by boolean("AAC4-C08", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }
        .describe("Send a C08 packet for the AAC4 bypass.")
    private val aac4OnGround by boolean("AAC4-OnGround", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }
        .describe("Only run the AAC4 bypass while on the ground.")

    private val blockForwardMultiplier by float("BlockForwardMultiplier", 1f, 0.2F..1f)
        .describe("Forward speed multiplier while blocking with a sword.")
    private val blockStrafeMultiplier by float("BlockStrafeMultiplier", 1f, 0.2F..1f)
        .describe("Strafe speed multiplier while blocking with a sword.")

    private val consumeMode by choices(
        "ConsumeMode",
        arrayOf(
            "None", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08", "Intave",
            "Drop", "Grim2371", "HypixelNew", "SpamItemChange", "SpamPlace", "SpamEmptyPlace", "Medusa",
            "Prediction"
        ),
        "None"
    )
        .describe("Anticheat bypass used while eating or drinking.")

    private val consumeForwardMultiplier by float("ConsumeForwardMultiplier", 1f, 0.2F..1f)
        .describe("Forward speed multiplier while eating or drinking.")
    private val consumeStrafeMultiplier by float("ConsumeStrafeMultiplier", 1f, 0.2F..1f)
        .describe("Strafe speed multiplier while eating or drinking.")
    private val consumeFoodOnly by boolean(
        "ConsumeFood",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }
        .describe("Apply the consume no-slow when eating food.")
    private val consumeDrinkOnly by boolean(
        "ConsumeDrink",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }
        .describe("Apply the consume no-slow when drinking.")

    private val bowPacket by choices(
        "BowMode",
        arrayOf(
            "None", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08",
            "Grim2371", "HypixelNew", "SpamItemChange", "SpamPlace", "SpamEmptyPlace", "Medusa"
        ),
        "None"
    )
        .describe("Anticheat bypass used while drawing a bow.")

    private val bowForwardMultiplier by float("BowForwardMultiplier", 1f, 0.2F..1f)
        .describe("Forward speed multiplier while drawing a bow.")
    private val bowStrafeMultiplier by float("BowStrafeMultiplier", 1f, 0.2F..1f)
        .describe("Strafe speed multiplier while drawing a bow.")

    // ── Prediction ───────────────────────────────────────────────────────────
    // Sword blocking / eating share one fixed cadence, taken from SpiterSemi's GrimTick beat: the
    // counter walks up while the item is in use, and every Nth tick is the one that cancels the
    // vanilla 0.2x. The ticks in between keep the real slowdown — the fast tick is the rare one.
    private val predictionForceSprint by boolean("ForceSprint", false) {
        swordMode == "Prediction" || consumeMode == "Prediction"
    }
        .describe("Keep sprinting while using the item, instead of letting the 0.2x input drop it.")
    private val predictionSlowInterval by int("Slow Interval", 4, 2..8) {
        swordMode == "Prediction" || consumeMode == "Prediction"
    }
        .describe("How many ticks of real 0.2x slowdown sit between two full-speed ticks.")

    // ── Prediction2 ──────────────────────────────────────────────────────────
    // Grim2374 "Blink Block" cycle: every cycle re-blocks with a C08, holds all outgoing action
    // packets for `BlinkTicks` ticks (server keeps itemInUse frozen = real blocking, client runs
    // full speed), then seals the batch with a slot change so the backlog is predicted un-slowed.
    private val prediction2BlinkTicks by int("BlinkTicks", 3, 1..8) {
        swordMode == "Prediction2"
    }
        .describe("Ticks each Blink Block cycle holds packets for.")

    // Blocks
    val soulSand by boolean("SoulSand", true)
        .describe("Ignore the slowdown from soul sand.")
    val liquidPush by boolean("LiquidPush", true)
        .describe("Ignore the push from flowing liquids.")
    private val teleportValue by boolean("Teleport", false)
        .describe("Keep momentum through server teleports.")
    private val teleportMode by choices("TeleportMode", arrayOf("Vanilla", "VanillaNoSetback", "Custom", "Decrease"), "Vanilla") {
        teleportValue
    }
        .describe("How to keep momentum through a teleport.")
    private val teleportNoApplyValue by boolean("TeleportNoApply", false) { teleportValue }
        .describe("Do not send the teleport confirmation packet.")
    private val teleportCustomSpeedValue by float("Teleport-CustomSpeed", 0.13f, 0f..1f) {
        teleportValue && teleportMode == "Custom"
    }
        .describe("Custom speed kept through the teleport.")
    private val teleportCustomYValue by boolean("Teleport-CustomY", false) {
        teleportValue && teleportMode == "Custom"
    }
        .describe("Also keep vertical motion through the teleport.")
    private val teleportDecreasePercentValue by float("Teleport-DecreasePercent", 0.13f, 0f..1f) {
        teleportValue && teleportMode == "Decrease"
    }
        .describe("Fraction of speed to reduce on each teleport.")

    private val swordGroup = Configurable("Sword")
    private val consumeGroup = Configurable("Consume")
    private val bowGroup = Configurable("Bow")
    private val aac4Group = Configurable("AAC4")
    private val blocksGroup = Configurable("Blocks")
    private val teleportGroup = Configurable("Teleport")
    private val generalGroup = Configurable("General")
    private val predictionGroup = Configurable("Prediction")
    private val prediction2Group = Configurable("Prediction2")

    init {
        moveValues(swordGroup,
            "SwordMode", "ReblinkTicks", "BlockForwardMultiplier", "BlockStrafeMultiplier")

        moveValues(consumeGroup,
            "ConsumeMode", "ConsumeForwardMultiplier", "ConsumeStrafeMultiplier",
            "ConsumeFood", "ConsumeDrink")

        moveValues(bowGroup,
            "BowMode", "BowForwardMultiplier", "BowStrafeMultiplier")

        moveValues(aac4Group, "AAC4-C07", "AAC4-C08", "AAC4-OnGround")

        moveValues(blocksGroup, "SoulSand", "LiquidPush")

        moveValues(teleportGroup,
            "Teleport", "TeleportMode", "TeleportNoApply", "Teleport-CustomSpeed",
            "Teleport-CustomY", "Teleport-DecreasePercent")

        moveValues(generalGroup, "AntiSwitchItem", "OnlyGround", "OnlyMove")

        moveValues(predictionGroup, "ForceSprint", "Slow Interval")

        moveValues(prediction2Group, "BlinkTicks")

        addValues(listOf(
            swordGroup, consumeGroup, bowGroup, aac4Group, blocksGroup, teleportGroup, generalGroup,
            predictionGroup, prediction2Group
        ))
    }
    private var shouldSwap = false
    private var shouldBlink = true
    private var shouldNoSlow = false
    private var pendingFlagApplyPacket = false
    private var lastMotionX = 0.0
    private var lastMotionY = 0.0
    private var lastMotionZ = 0.0

    private var hasDropped = false

    private val BlinkTimer = TickTimer()
    private val aac4Timer = MSTimer()
    private val matrixBufferTimer = MSTimer()

    private var grim2371DoNotSlow = false
    private val grim2371Timer = TickTimer()
    private val matrixPacketBuffer = mutableListOf<Packet<*>>()
    private var matrixBuffering = false
    private var lastBlockingState = false
    private var medusaCanStopSprint = true

    // ───────────────────────────── Prediction ─────────────────────────────
    /** Cadence counter; only advances while the item Prediction owns is actually in use. */
    private var predictionTick = 0

    // ──────────────────────────── Prediction2 ─────────────────────────────
    private enum class BlinkPhase { NONE, AWAY, BACK }

    /** Movement budget the server grants per position packet, in milliseconds. */
    private const val TICK_MS = 50.0

    /**
     * Packets held by the current Blink Block window. Sends happen on the main thread but incoming
     * packets are handled on the netty thread, so the queue is guarded by a lock and the flags the
     * network thread raises are volatile.
     */
    private val blinkLock = Any()
    private val blinkQueue = mutableListOf<Packet<*>>()
    @Volatile private var blinkHolding = false
    private var blinkHeldFlying = 0
    private var blinkStartNano = 0L
    @Volatile private var blinkAbort = false
    @Volatile private var blinkKb = false
    @Volatile private var blinkReleased = false
    private var blinkDoneThisCycle = false

    /** Hotbar slot this mode last put on the wire, plus the shape the server was left in. */
    private var blinkPhase = BlinkPhase.NONE
    private var blinkRunning = false
    private var grimSlot = 0
    private var blinkCyclePos = 0
    /** A foreign attack or slot/block packet landed in the open window: freeze instead of guessing. */
    private var windowDirty = false

    /** Slowdown ruling for this tick, decided at the head of the update before input is scaled. */
    private var blinkFullTick = true
    private var blinkUseActive = false

    override fun onDisable() {
        shouldSwap = false
        shouldBlink = true
        BlinkTimer.reset()
        BlinkUtils.unblink()
        grim2371DoNotSlow = false
        grim2371Timer.reset()
        pendingFlagApplyPacket = false
        matrixPacketBuffer.clear()
        matrixBuffering = false
        lastBlockingState = false
        medusaCanStopSprint = true
        predictionTick = 0
        flushBlink()
        stopResync()
        blinkFullTick = true
        blinkUseActive = false
    }

    val onMotion = handler<MotionEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val heldItem = player.heldItem ?: return@handler
        val isUsingItem = usingItemFunc()

        if ((!hasMotion && !shouldSwap) || !shouldHandleNoSlow(player))
            return@handler

        if (event.eventState == EventState.PRE && isUsingItem) {
            grim2371Timer.update()

            val shouldSendPacket = when {
                consumeMode == "Grim2371" && (heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk) -> true
                swordMode == "Grim2371" && heldItem.item is ItemSword -> true
                bowPacket == "Grim2371" && heldItem.item is ItemBow -> true
                else -> false
            }

            if (shouldSendPacket && grim2371Timer.hasTimePassed(3)) {
                sendPacket(C08PacketPlayerBlockPlacement(
                    BlockPos(-1, -1, -1),
                    255,
                    heldItem,
                    0f,
                    0f,
                    0f
                ))
                grim2371DoNotSlow = true
                grim2371Timer.reset()
            }
        }

        if (!isUsingItem && grim2371DoNotSlow) {
            grim2371DoNotSlow = false
        }

        if (swordMode == "Grim2371" && heldItem.item is ItemSword && isUsingItem) {
            if (event.eventState == EventState.PRE) {
                grim2371Timer.update()
                if (grim2371Timer.hasTimePassed(1)) {
                    grim2371DoNotSlow = true
                    sendPacket(C08PacketPlayerBlockPlacement(
                        BlockPos(-1, -1, -1),
                        255,
                        heldItem,
                        0f,
                        0f,
                        0f
                    ))
                    grim2371Timer.reset()
                }
            }
        }

        if (bowPacket == "Grim2371" && heldItem.item is ItemBow && isUsingItem) {
            if (event.eventState == EventState.PRE) {
                grim2371Timer.update()
                if (grim2371Timer.hasTimePassed(1)) {
                    grim2371DoNotSlow = true
                    sendPacket(C08PacketPlayerBlockPlacement(
                        BlockPos(-1, -1, -1),
                        255,
                        heldItem,
                        0f,
                        0f,
                        0f
                    ))
                    grim2371Timer.reset()
                }
            }
        }

        if (consumeMode == "Grim2371" && isUsingItem) {
            if (event.eventState == EventState.PRE) {
                grim2371Timer.update()
                if (grim2371Timer.hasTimePassed(1)) {
                    grim2371DoNotSlow = true
                    sendPacket(C08PacketPlayerBlockPlacement(
                        BlockPos(-1, -1, -1),
                        255,
                        heldItem,
                        0f,
                        0f,
                        0f
                    ))
                    grim2371Timer.reset()
                }
            }
        }

        if (isUsingItem || shouldSwap) {
            if (heldItem.item !is ItemSword && heldItem.item !is ItemBow && (consumeFoodOnly && heldItem.item is ItemFood ||
                        consumeDrinkOnly && (heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk))
            ) {
                when (consumeMode.lowercase()) {
                    "aac4" ->
                        handleAAC4Packet(event, heldItem)

                    "aac5" ->
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                    "switchitem" ->
                        if (event.eventState == EventState.PRE) {
                            updateSlot()
                        }

                    "updatedncp" ->
                        if (event.eventState == EventState.PRE && shouldSwap) {
                            updateSlot()
                            sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                            shouldSwap = false
                        }

                    "invalidc08" -> {
                        if (event.eventState == EventState.PRE) {
                            if (InventoryUtils.hasSpaceInInventory()) {
                                if (player.ticksExisted % 3 == 0)
                                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                            }
                        }
                    }

                    "intave" -> {
                        if (event.eventState == EventState.PRE) {
                            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP))
                        }
                    }

                    "hypixelnew" -> {
                        if (InventoryUtils.hasSpaceInInventory() && event.eventState == EventState.PRE && player.ticksExisted % 3 != 0) {
                            sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                        }
                    }

                    "spamitemchange" ->
                        if (event.eventState == EventState.PRE) {
                            sendPacket(C09PacketHeldItemChange(player.inventory.currentItem))
                        }

                    "spamplace" ->
                        if (event.eventState == EventState.PRE) {
                            sendPacket(C08PacketPlayerBlockPlacement(heldItem))
                        }

                    "spamemptyplace" ->
                        if (event.eventState == EventState.PRE) {
                            sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                        }
                }
            }
        }

        if (heldItem.item is ItemBow && (isUsingItem || shouldSwap)) {
            when (bowPacket.lowercase()) {
                "aac4" ->
                    handleAAC4Packet(event, heldItem)

                "aac5" ->
                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        updateSlot()
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        updateSlot()
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                        shouldSwap = false
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }

                "hypixelnew" -> {
                    if (InventoryUtils.hasSpaceInInventory() && event.eventState == EventState.PRE && player.ticksExisted % 3 != 0) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                    }
                }

                "spamitemchange" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C09PacketHeldItemChange(player.inventory.currentItem))
                    }

                "spamplace" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C08PacketPlayerBlockPlacement(heldItem))
                    }

                "spamemptyplace" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                    }
            }
        }

        if (heldItem.item is ItemSword && isUsingItem) {
            when (swordMode.lowercase()) {
                "ncp" ->
                    when (event.eventState) {
                        EventState.PRE -> sendPacket(
                            C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
                        )

                        EventState.POST -> sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f
                            )
                        )

                        else -> return@handler
                    }

                "aac4" ->
                    handleAAC4Packet(event, heldItem)

                "updatedncp" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                    }

                "aac5" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(
                            C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f)
                        )
                    }

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        updateSlot()
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }

                "oldintave" -> {
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C09PacketHeldItemChange((player.inventory.currentItem + 1) % 9))
                        sendPacket(C09PacketHeldItemChange(player.inventory.currentItem))
                    }

                    if (event.eventState == EventState.POST) {
                        sendPacket(C08PacketPlayerBlockPlacement(player.inventoryContainer.getSlot(player.inventory.currentItem + 36).stack))
                    }
                }

                "hypixelnew" -> {
                    if (InventoryUtils.hasSpaceInInventory() && event.eventState == EventState.PRE && player.ticksExisted % 3 != 0) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                    }
                }

                "spamitemchange" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C09PacketHeldItemChange(player.inventory.currentItem))
                    }

                "spamplace" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C08PacketPlayerBlockPlacement(heldItem))
                    }

                "spamemptyplace" ->
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0f, 0f, 0f))
                    }
            }
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val blockingSword = isBlockingSword()

        if ((swordMode == "Matrix" || swordMode == "GrimAC") && (lastBlockingState || blockingSword)) {
            if (matrixBufferTimer.hasTimePassed(230L) && matrixBuffering) {
                matrixBuffering = false

                if (swordMode == "GrimAC") {
                    sendPacket(C09PacketHeldItemChange((player.inventory.currentItem + 1) % 9), false)
                    sendPacket(C09PacketHeldItemChange(player.inventory.currentItem), false)
                } else {
                    sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN), false)
                }

                if (matrixPacketBuffer.isNotEmpty()) {
                    var canAttack = false

                    for (queuedPacket in matrixPacketBuffer) {
                        if (queuedPacket is C03PacketPlayer) {
                            canAttack = true
                        }

                        if (!((queuedPacket is C02PacketUseEntity || queuedPacket is C0APacketAnimation) && !canAttack)) {
                            sendPacket(queuedPacket, false)
                        }
                    }

                    matrixPacketBuffer.clear()
                }
            }

            if (!matrixBuffering) {
                lastBlockingState = blockingSword
                if (!blockingSword) {
                    return@handler
                }

                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, player.inventory.getCurrentItem(), 0f, 0f, 0f), false)
                matrixBuffering = true
                matrixBufferTimer.reset()
            }
        } else {
            lastBlockingState = false
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        val player = mc.thePlayer ?: return@handler

        if (!event.isCancelled && teleportValue) {
            when {
                packet is S08PacketPlayerPosLook && event.eventType == EventState.RECEIVE -> {
                    pendingFlagApplyPacket = true
                    lastMotionX = player.motionX
                    lastMotionY = player.motionY
                    lastMotionZ = player.motionZ

                    if (teleportMode == "VanillaNoSetback") {
                        val x = packet.x - player.posX
                        val y = packet.y - player.posY
                        val z = packet.z - player.posZ
                        val diff = sqrt(x * x + y * y + z * z)

                        if (diff <= 8) {
                            event.cancelEvent()
                            pendingFlagApplyPacket = false
                            sendPacket(
                                C03PacketPlayer.C06PacketPlayerPosLook(
                                    packet.x,
                                    packet.y,
                                    packet.z,
                                    packet.yaw,
                                    packet.pitch,
                                    player.onGround
                                ),
                                false
                            )
                        }
                    }
                }

                pendingFlagApplyPacket && packet is C03PacketPlayer.C06PacketPlayerPosLook && event.eventType == EventState.SEND -> {
                    pendingFlagApplyPacket = false

                    if (teleportNoApplyValue) {
                        event.cancelEvent()
                    }

                    when (teleportMode) {
                        "Vanilla", "VanillaNoSetback" -> {
                            player.motionX = lastMotionX
                            player.motionY = lastMotionY
                            player.motionZ = lastMotionZ
                        }

                        "Custom" -> {
                            if (player.isMoving) {
                                MovementUtils.strafe(teleportCustomSpeedValue)
                            }

                            if (teleportCustomYValue) {
                                player.motionY = if (lastMotionY > 0.0) {
                                    teleportCustomSpeedValue.toDouble()
                                } else {
                                    -teleportCustomSpeedValue.toDouble()
                                }
                            }
                        }

                        "Decrease" -> {
                            player.motionX = lastMotionX * teleportDecreasePercentValue
                            player.motionY = lastMotionY * teleportDecreasePercentValue
                            player.motionZ = lastMotionZ * teleportDecreasePercentValue
                        }
                    }
                }
            }
        }

        if (antiSwitchItem && packet is S09PacketHeldItemChange && event.eventType == EventState.RECEIVE && usingItemFunc()) {
            event.cancelEvent()
            sendPacket(C09PacketHeldItemChange(packet.heldItemHotbarIndex), false)
            sendPacket(C09PacketHeldItemChange(player.inventory.currentItem), false)
            return@handler
        }

        if (!shouldHandleNoSlow(player)) {
            return@handler
        }

        if (isMedusaActive()) {
            if (medusaCanStopSprint) {
                sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING), false)
                medusaCanStopSprint = false
            }
        } else {
            medusaCanStopSprint = true
        }

        if ((swordMode == "Matrix" || swordMode == "GrimAC") && matrixBuffering && event.eventType == EventState.SEND) {
            if ((packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement) && isBlockingSword()) {
                event.cancelEvent()
                return@handler
            }

            if (packet is C03PacketPlayer || packet is C0APacketAnimation || packet is C0BPacketEntityAction ||
                packet is C02PacketUseEntity || packet is C07PacketPlayerDigging || packet is C08PacketPlayerBlockPlacement
            ) {
                matrixPacketBuffer += packet
                event.cancelEvent()
                return@handler
            }
        }

        if (event.isCancelled || shouldSwap)
            return@handler

        // Credit: @ManInMyVan
        // TODO: Not sure how to fix random grim simulation flag. (Seem to only happen in Loyisa).
        if (consumeMode == "Drop") {
            if (player.heldItem?.item !is ItemFood || !player.isMoving) {
                shouldNoSlow = false
                return@handler
            }

            val isUsingItem = packet is C08PacketPlayerBlockPlacement && packet.placedBlockDirection == 255

            if (!player.isUsingItem) {
                shouldNoSlow = false
                hasDropped = false
            }

            if (isUsingItem && !hasDropped) {
                sendPacket(C07PacketPlayerDigging(DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                shouldNoSlow = false
                hasDropped = true
            } else if (packet is S2FPacketSetSlot && player.isUsingItem) {
                if (packet.func_149175_c() != 0 || packet.func_149173_d() != SilentHotbar.currentSlot + 36) return@handler

                event.cancelEvent()
                shouldNoSlow = true

                player.itemInUse = packet.func_149174_e()
                if (!player.isUsingItem) player.itemInUseCount = 0
                player.inventory.mainInventory[SilentHotbar.currentSlot] = packet.func_149174_e()
            }
        }

        if (swordMode == "Blink") {
            when (packet) {
                is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return@handler

                is C07PacketPlayerDigging, is C02PacketUseEntity, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                    BlinkTimer.update()
                    if (shouldBlink && BlinkTimer.hasTimePassed(reblinkTicks) && (BlinkUtils.packetsReceived.isNotEmpty() || BlinkUtils.packets.isNotEmpty())) {
                        BlinkUtils.unblink()
                        BlinkTimer.reset()
                        shouldBlink = false
                    } else if (!BlinkTimer.hasTimePassed(reblinkTicks)) {
                        shouldBlink = true
                    }
                    return@handler
                }

                // Flush on kb
                is S12PacketEntityVelocity -> {
                    if (mc.thePlayer.entityId == packet.entityID) {
                        BlinkUtils.unblink()
                        return@handler
                    }
                }

                // Flush on explosion
                is S27PacketExplosion -> {
                    if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                        BlinkUtils.unblink()
                        return@handler
                    }
                }

                is C03PacketPlayer -> {
                    if (swordMode == "Blink") {
                        if (player.isMoving) {
                            if (player.heldItem?.item is ItemSword && usingItemFunc()) {
                                if (shouldBlink)
                                    BlinkUtils.blink(packet, event)
                            } else {
                                shouldBlink = true
                                BlinkUtils.unblink()
                            }
                        }
                    }
                }
            }
        }

        when (packet) {
            is C08PacketPlayerBlockPlacement -> {
                if (packet.stack?.item != null && player.heldItem?.item != null && packet.stack.item == mc.thePlayer.heldItem?.item) {
                    if ((consumeMode == "UpdatedNCP" && (
                                packet.stack.item is ItemFood ||
                                        packet.stack.item is ItemPotion ||
                                        packet.stack.item is ItemBucketMilk)) ||
                        (bowPacket == "UpdatedNCP" && packet.stack.item is ItemBow)
                    ) {
                        shouldSwap = true
                    }
                }
            }
        }
    }

    val onSlowDown = handler<SlowDownEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val heldItem = mc.thePlayer.heldItem?.item

        if (!shouldHandleNoSlow(player)) {
            return@handler
        }

        // Prediction: SpiterSemi's beat. The tick whose counter lands on the interval cancels the
        // slowdown outright and runs at full speed; every other tick keeps the real 0.2x. Returning
        // here also skips the multiplier defaults below, which would erase those slow ticks.
        if (predictionActive(heldItem)) {
            if (predictionTick % predictionSlowInterval == 0) {
                event.forward = 1.0f
                event.strafe = 1.0f
            }
            return@handler
        }

        // Prediction2: ruled per tick. The hold window, the slot-change tick and the re-block tick
        // all run un-slowed; the cold start and frozen ticks stay at the real 0.2x.
        if (swordMode == "Prediction2" && heldItem is ItemSword) {
            if (blinkFullTick && blinkUseActive) {
                event.forward = 1.0f
                event.strafe = 1.0f
            }
            return@handler
        }

        if ((swordMode == "Grim2371" && heldItem is ItemSword) ||
            (consumeMode == "Grim2371" && (heldItem is ItemFood || heldItem is ItemPotion || heldItem is ItemBucketMilk)) ||
            (bowPacket == "Grim2371" && heldItem is ItemBow)) {
            if (grim2371DoNotSlow) {
                event.forward = 1.0f
                event.strafe = 1.0f
                grim2371DoNotSlow = false
                return@handler
            }
        }

        if (heldItem !is ItemSword) {
            if (!consumeFoodOnly && heldItem is ItemFood ||
                !consumeDrinkOnly && (heldItem is ItemPotion || heldItem is ItemBucketMilk)
            ) {
                return@handler
            }

            if (consumeMode == "Drop" && !shouldNoSlow)
                return@handler
        }

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    // ═══════════════════════════════ Prediction ═══════════════════════════════

    /**
     * Advances the cadence at the head of the player update, which is before the vanilla input
     * scaling [onSlowDown] rewrites, so the ruling later in the same tick reads a current counter.
     *
     * The counter only runs while the item is really in use, and stops the moment the use ends. The
     * beat depends on that: the tick that pays out has to be reached from the start of the use, so
     * the first tick of every block stays a slow one — merely holding the sword must not walk the
     * counter into the fast tick of a later block.
     */
    val onPredictionUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if (!usingItemFunc() || !predictionActive(player.heldItem?.item)) {
            predictionTick = 0
            return@handler
        }
        predictionTick++
    }

    /**
     * Sprint is rebuilt after the three vanilla stop conditions have run. The 0.2x input the slow
     * tick leaves behind sits below the 0.8 forward threshold, so vanilla would always drop sprint
     * there; forcing it back keeps sprint on both kinds of tick.
     */
    val onPredictionSprint = handler<PostSprintUpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if (!predictionForceSprint || !predictionActive(player.heldItem?.item)) return@handler
        if (predictionSprintSafe(player)) player.isSprinting = true
    }

    /**
     * Only sprint where the server would accept it: hunger, blindness, sneaking and being blocked
     * or in a fluid each have their own check, and forcing sprint through them is what gets flagged
     * rather than the speed it buys.
     */
    private fun predictionSprintSafe(player: net.minecraft.client.entity.EntityPlayerSP): Boolean =
        player.movementInput.moveForward > 0f &&
            (player.foodStats.foodLevel > 6 || player.capabilities.allowFlying) &&
            !player.isSneaking && !mc.gameSettings.keyBindSneak.isKeyDown &&
            !player.isPotionActive(Potion.blindness) &&
            !player.isCollidedHorizontally &&
            !player.isInWater && !player.isInLava

    /** True while the held item is one the Prediction cadence owns. */
    private fun predictionActive(item: Item?): Boolean = when {
        item is ItemSword -> swordMode == "Prediction"

        item is ItemFood -> consumeMode == "Prediction" && consumeFoodOnly

        item is ItemPotion || item is ItemBucketMilk -> consumeMode == "Prediction" && consumeDrinkOnly

        else -> false
    }

    // ══════════════════════════════ Prediction2 ═══════════════════════════════

    /**
     * Head of the tick. Takes the use snapshot the slowdown ruling reads, then either advances the
     * block cycle or tears the hold window down.
     */
    val onPrediction2Update = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        if (swordMode != "Prediction2" && !blinkHolding && blinkPhase == BlinkPhase.NONE) return@handler

        blinkUseActive = prediction2Active(player)

        if (!blinkUseActive) {
            blinkFullTick = true
            // The first held tick has no position packet queued yet, so flushing here would replay
            // a batch with nothing to close its window. Hold one more tick and let it fill up.
            if (blinkHolding && blinkHeldFlying == 0) return@handler
            flushBlink()
            stopResync()
            return@handler
        }

        val sealed = if (blinkHolding) blinkTick() else false
        if (blinkHolding || sealed) {
            blinkFullTick = true
        } else {
            resyncTick()
        }
    }

    /**
     * Opens the hold window once this tick's own position packet is already on the wire, so the
     * packet that clears the server's blocking state is never itself held back.
     */
    val onPrediction2PostMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.POST || swordMode != "Prediction2") return@handler
        if (!blinkHolding) tryStartBlink()
    }

    val onPrediction2Packet = handler<PacketEvent> { event ->
        if (event.eventType != EventState.SEND || event.isCancelled) return@handler
        if (swordMode != "Prediction2" && !blinkHolding) return@handler

        val packet = event.packet

        if (blinkHolding) {
            when {
                isHeldPacket(packet) -> synchronized(blinkLock) {
                    // Re-checked under the lock: the main thread may have flushed while we waited,
                    // in which case this packet simply goes out normally.
                    if (blinkHolding) {
                        blinkQueue += packet
                        if (packet is C03PacketPlayer) blinkHeldFlying++
                        event.cancelEvent()
                        return@handler
                    }
                }

                // The player let go: swallow the release so the flush can synthesise an equivalent
                // one behind a position packet, instead of letting it land in the open window.
                packet is C07PacketPlayerDigging && packet.status == RELEASE_USE_ITEM -> {
                    event.cancelEvent()
                    blinkAbort = true
                    blinkReleased = true
                    return@handler
                }

                // Foreign blocking packet: the frozen window already keeps the item in use, so this
                // buys nothing and would re-arm the slow state for the packets held behind it.
                packet is C08PacketPlayerBlockPlacement -> {
                    event.cancelEvent()
                    return@handler
                }

                // Foreign slot switch breaks the slot the seal relies on.
                packet is C09PacketHeldItemChange -> blinkAbort = true
            }
        }

        // Every position packet opens a fresh window on the server and closes the previous one.
        if (packet is C03PacketPlayer) {
            windowDirty = false
            return@handler
        }

        if (!blinkRunning && blinkPhase == BlinkPhase.NONE) return@handler

        when {
            packet is C02PacketUseEntity && packet.action == C02PacketUseEntity.Action.ATTACK ->
                windowDirty = true

            packet is C09PacketHeldItemChange -> {
                windowDirty = true
                grimSlot = packet.slotId
            }

            packet is C08PacketPlayerBlockPlacement -> windowDirty = true

            packet is C07PacketPlayerDigging && packet.status == RELEASE_USE_ITEM -> {
                blinkAbort = true
                blinkReleased = true
                stopResync()
            }
        }
    }

    /**
     * Knockback is resolved against a transaction boundary, so a frozen window must not be gambled
     * on: ending it immediately is cheaper than a desynced velocity prediction.
     */
    val onPrediction2Receive = handler<PacketEvent> { event ->
        if (event.eventType != EventState.RECEIVE || event.isCancelled || !blinkHolding) return@handler

        when (val packet = event.packet) {
            is S12PacketEntityVelocity -> if (packet.entityID == mc.thePlayer?.entityId) blinkKb = true

            is S27PacketExplosion -> blinkKb = true
        }
    }

    /** The queued packets belong to the old connection, so they are dropped rather than replayed. */
    val onPrediction2WorldChange = handler<WorldChangeEvent> {
        discardBlink()
        resetResync()
    }

    /**
     * True while a sword block owned by Prediction2 is up. Uses the module-wide [usingItemFunc]
     * instead of the vanilla item-in-use flag so a block started by KillAura's auto block runs the
     * cycle as well — every other sword mode already keys off that helper.
     */
    private fun prediction2Active(player: net.minecraft.client.entity.EntityPlayerSP): Boolean =
        swordMode == "Prediction2" && usingItemFunc() && player.heldItem?.item is ItemSword

    /**
     * Advances the block cycle by one tick.
     *
     * Cold start (the tick the player right-clicks) deliberately sends nothing: the real blocking
     * packet is already in this window and a slot change next to it trips the server's packet-order
     * check. That tick runs at the true 0.2x to collect a reward, and from the next tick the cycle
     * alternates:
     *  - AWAY moves the slot off the sword, so the server predicts the tick un-slowed (full speed);
     *  - BACK moves it back and re-sends the block packet, so the server blocks again. It still runs
     *    full speed, because the slot change one tick earlier armed the exemption that clears the
     *    flag the full-speed trajectory would otherwise raise.
     */
    private fun resyncTick() {
        val player = mc.thePlayer ?: return
        val local = player.inventory.currentItem

        if (blinkPhase == BlinkPhase.NONE) {
            grimSlot = local
            blinkRunning = true
            blinkFullTick = false
            // Must leave NONE: the server is already blocking from the real packet, and staying
            // here would re-enter this branch every tick and make the cycle unreachable.
            blinkPhase = BlinkPhase.BACK
            return
        }

        if (windowDirty) {
            // A foreign action packet shares this window. Reuse the previous tick's shape rather
            // than guess where the server state landed; two un-slowed ticks in a row is exactly
            // what the anticheat flags, so a frozen BACK tick stays at the real 0.2x.
            blinkFullTick = blinkPhase == BlinkPhase.AWAY
            return
        }

        if (blinkCyclePos == 0) {
            // AWAY: the target slot must differ from the one the server currently has, otherwise
            // the packet is dropped as a duplicate.
            var target = (local + 1) % 9
            if (target == grimSlot) target = (local + 2) % 9
            sendSlot(target)
            blinkPhase = BlinkPhase.AWAY
            blinkFullTick = true
            blinkCyclePos = 1
            return
        }

        // BACK: re-block, then immediately open the next cycle's hold window after this tick.
        if (grimSlot != local) sendSlot(local)
        player.heldItem?.let { sendBlock(it) }
        blinkPhase = BlinkPhase.BACK
        blinkFullTick = true
        blinkDoneThisCycle = false
        blinkCyclePos = 0
    }

    /** Leaves the cycle cleanly, moving the server's slot back if the tick stopped on AWAY. */
    private fun stopResync() {
        val player = mc.thePlayer
        val needCutBack = player != null && blinkRunning && blinkPhase == BlinkPhase.AWAY &&
            grimSlot != player.inventory.currentItem && !windowDirty

        blinkPhase = BlinkPhase.NONE
        blinkRunning = false
        blinkCyclePos = 0

        if (needCutBack) sendSlot(player.inventory.currentItem)
    }

    private fun resetResync() {
        blinkPhase = BlinkPhase.NONE
        blinkRunning = false
        windowDirty = false
        blinkCyclePos = 0
        grimSlot = 0
        blinkDoneThisCycle = false
    }

    private fun tryStartBlink() {
        val player = mc.thePlayer ?: return
        if (blinkHolding || blinkDoneThisCycle || windowDirty) return
        if (!blinkRunning || blinkPhase != BlinkPhase.BACK) return
        if (!prediction2Active(player)) return

        synchronized(blinkLock) {
            blinkQueue.clear()
            blinkHeldFlying = 0
            blinkAbort = false
            blinkKb = false
            blinkReleased = false
            blinkStartNano = System.nanoTime()
            // Opened last, inside the lock: packets arriving from another thread must either queue
            // before this point (and be drained by the flush) or go straight out.
            blinkHolding = true
        }
        blinkDoneThisCycle = true
    }

    /**
     * Ruling of an open window. It closes once [prediction2BlinkTicks] position packets are queued
     * and enough real time has passed for the server's movement budget to absorb the whole batch at
     * once; an abort ends it immediately through the plain flush instead.
     *
     * @return true when the seal ran, meaning the caller must not advance the cycle again this tick
     */
    private fun blinkTick(): Boolean {
        if (!blinkHolding) return false

        val want = maxOf(1, prediction2BlinkTicks)
        val heldMs = (System.nanoTime() - blinkStartNano) / 1.0e6
        val timeBudgetOk = heldMs + pingMs() >= blinkHeldFlying * TICK_MS
        val abort = blinkAbort || blinkKb || blinkReleased

        if (!abort) {
            if (blinkHeldFlying >= want && timeBudgetOk) {
                flushBlinkSeal()
                return true
            }
            return false
        }

        // Aborted before this tick's position packet was queued: replaying now would leave the
        // synthesised release with nothing behind it. Hold one more tick, then flush normally.
        if (blinkHeldFlying == 0) return false

        flushBlink()
        return false
    }

    /**
     * Normal end of a window. Moves the slot off the sword and replays the backlog in its original
     * order: the first queued position packet arrives on a mismatched slot, which both clears the
     * server's blocking state and arms the exemption, and this tick then runs un-slowed as well.
     */
    private fun flushBlinkSeal() {
        if (!blinkHolding) return

        val out = drainBlink() ?: return
        // Drained above, so the window is closed either way; with no session left there is nobody
        // to replay the batch to.
        val player = mc.thePlayer ?: return

        val local = player.inventory.currentItem
        var target = (local + 1) % 9
        if (target == grimSlot) target = (local + 2) % 9
        sendSlot(target)

        for (packet in out) sendPacket(packet, false)

        blinkPhase = BlinkPhase.AWAY
        blinkCyclePos = 1
        blinkFullTick = true
    }

    /**
     * Abort flush. Unlike the seal it prepends a release packet, because the backlog may have been
     * queued while the server still had the item in use and has to be predicted un-slowed.
     */
    private fun flushBlink() {
        val out = drainBlink() ?: return

        // A head release is only worth sending when a position packet travels with it, because that
        // packet is what closes the release's window. The position packet does not have to be the
        // first entry of the batch — dispatchFlush hoists the tick's own action packets in front of
        // it — so ask whether the batch carries one at all. A batch without one holds nothing that
        // needs un-slowing, and releasing there would drop a block the player still has up.
        dispatchFlush(out, out.any { it is C03PacketPlayer })
    }

    /**
     * Replays a drained batch in its original order.
     *
     * @param headRelease whether a release packet should open the batch. It is only worth sending
     *                    in front of a position packet, since that is what closes its window.
     */
    private fun dispatchFlush(out: MutableList<Packet<*>>, headRelease: Boolean) {
        var firstFlying = -1

        if (headRelease) {
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN), false)
            firstFlying = out.indexOfFirst { it is C03PacketPlayer }
        }

        if (firstFlying < 0) {
            // Nothing to close the release window with, so the batch just goes out in order.
            for (packet in out) sendPacket(packet, false)
            return
        }

        // Sprint and sneak toggles belong to the tick they were queued in: send them ahead of the
        // position packet that closes the release window, otherwise that tick is predicted with the
        // previous sprint state.
        for (index in 0 until firstFlying) {
            if (out[index] is C0BPacketEntityAction) sendPacket(out[index], false)
        }

        sendPacket(out.removeAt(firstFlying), false)

        for ((index, packet) in out.withIndex()) {
            if (index < firstFlying && packet is C0BPacketEntityAction) continue
            sendPacket(packet, false)
        }
    }

    /** Closes the window and hands back everything it held; null when it was already closed. */
    private fun drainBlink(): MutableList<Packet<*>>? = synchronized(blinkLock) {
        if (!blinkHolding) return null

        blinkHolding = false
        val out = blinkQueue.toMutableList()
        blinkQueue.clear()
        blinkHeldFlying = 0
        blinkAbort = false
        blinkKb = false
        blinkReleased = false
        blinkStartNano = 0L
        out
    }

    private fun discardBlink() {
        synchronized(blinkLock) {
            blinkHolding = false
            blinkQueue.clear()
            blinkHeldFlying = 0
            blinkAbort = false
            blinkKb = false
            blinkReleased = false
            blinkStartNano = 0L
            blinkDoneThisCycle = false
        }
    }

    /**
     * The packets the window delays. Everything else goes out live — transaction confirmations in
     * particular, since they are what closes the window on the server and holding them would leave
     * it open for the whole cycle.
     */
    private fun isHeldPacket(packet: Packet<*>): Boolean =
        packet is C03PacketPlayer || packet is C0BPacketEntityAction ||
            packet is C02PacketUseEntity || packet is C0APacketAnimation

    /** Silent slot switch: the local hotbar slot is deliberately left where the player put it. */
    private fun sendSlot(slot: Int) {
        sendPacket(C09PacketHeldItemChange(slot), false)
        grimSlot = slot
    }

    private fun sendBlock(stack: ItemStack) = sendPacket(C08PacketPlayerBlockPlacement(stack), false)

    /** Tab-list ping, or 0 when unknown, which is the conservative end of the hold-time budget. */
    private fun pingMs(): Double {
        val player = mc.thePlayer ?: return 0.0
        val info = mc.netHandler?.getPlayerInfo(player.uniqueID) ?: return 0.0
        return info.responseTime.toDouble()
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> if (isForward) consumeForwardMultiplier else consumeStrafeMultiplier

        is ItemSword -> if (isForward) blockForwardMultiplier else blockStrafeMultiplier

        is ItemBow -> if (isForward) bowForwardMultiplier else bowStrafeMultiplier

        else -> 0.2F
    }

    fun isUNCPBlocking() =
        handleEvents() && swordMode == "UpdatedNCP" && mc.gameSettings.keyBindUseItem.isKeyDown &&
            (mc.thePlayer.heldItem?.item is ItemSword)

    fun usingItemFunc() =
        mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && KillAura.blockStatus) || isUNCPBlocking())

    private fun shouldHandleNoSlow(player: net.minecraft.client.entity.EntityPlayerSP): Boolean {
        if (onlyMove && !player.isMoving) {
            return false
        }

        if (onlyGround && !player.onGround) {
            return false
        }

        return true
    }

    private fun handleAAC4Packet(event: MotionEvent, heldItem: ItemStack) {
        val player = mc.thePlayer ?: return

        if (aac4OnGround && !player.onGround) {
            return
        }

        if (aac4C07 && event.eventState == EventState.PRE && aac4Timer.hasTimePassed(80L)) {
            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
        }

        if (aac4C08 && event.eventState == EventState.POST && aac4Timer.hasTimePassed(80L)) {
            sendPacket(C08PacketPlayerBlockPlacement(heldItem))
            aac4Timer.reset()
        }
    }

    private fun isBlockingSword(): Boolean {
        val player = mc.thePlayer ?: return false
        return (player.isUsingItem || KillAura.blockStatus) && player.heldItem?.item is ItemSword
    }

    private fun isMedusaActive(): Boolean {
        val player = mc.thePlayer ?: return false
        val item = player.heldItem?.item ?: return false

        return when (item) {
            is ItemSword -> swordMode == "Medusa" && usingItemFunc()
            is ItemBow -> bowPacket == "Medusa" && usingItemFunc()
            is ItemFood, is ItemPotion, is ItemBucketMilk -> consumeMode == "Medusa" && usingItemFunc()
            else -> false
        }
    }

    private fun updateSlot() {
        SilentHotbar.selectSlotSilently(this, (SilentHotbar.currentSlot + 1) % 9, immediate = true)
        SilentHotbar.resetSlot(this, true)
    }
}
