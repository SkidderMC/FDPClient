/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

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
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.math.sqrt

object NoSlow : Module("NoSlow", Category.MOVEMENT, Category.SubCategory.MOVEMENT_MAIN, gameDetecting = false) {

    private val swordMode by choices(
        "SwordMode",
        arrayOf(
            "None", "NCP", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08",
            "Blink", "Grim2371", "OldIntave", "Medusa", "HypixelNew", "SpamItemChange",
            "SpamPlace", "SpamEmptyPlace", "Matrix", "GrimAC"
        ),
        "None"
    )

    private val reblinkTicks by int("ReblinkTicks", 10, 1..20) { swordMode == "Blink" }
    private val antiSwitchItem by boolean("AntiSwitchItem", false)
    private val onlyGround by boolean("OnlyGround", false)
    private val onlyMove by boolean("OnlyMove", false)
    private val aac4C07 by boolean("AAC4-C07", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }
    private val aac4C08 by boolean("AAC4-C08", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }
    private val aac4OnGround by boolean("AAC4-OnGround", true) {
        swordMode == "AAC4" || consumeMode == "AAC4" || bowPacket == "AAC4"
    }

    private val blockForwardMultiplier by float("BlockForwardMultiplier", 1f, 0.2F..1f)
    private val blockStrafeMultiplier by float("BlockStrafeMultiplier", 1f, 0.2F..1f)

    private val consumeMode by choices(
        "ConsumeMode",
        arrayOf(
            "None", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08", "Intave",
            "Drop", "Grim2371", "HypixelNew", "SpamItemChange", "SpamPlace", "SpamEmptyPlace", "Medusa"
        ),
        "None"
    )

    private val consumeForwardMultiplier by float("ConsumeForwardMultiplier", 1f, 0.2F..1f)
    private val consumeStrafeMultiplier by float("ConsumeStrafeMultiplier", 1f, 0.2F..1f)
    private val consumeFoodOnly by boolean(
        "ConsumeFood",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }
    private val consumeDrinkOnly by boolean(
        "ConsumeDrink",
        true
    ) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }

    private val bowPacket by choices(
        "BowMode",
        arrayOf(
            "None", "UpdatedNCP", "AAC4", "AAC5", "SwitchItem", "InvalidC08",
            "Grim2371", "HypixelNew", "SpamItemChange", "SpamPlace", "SpamEmptyPlace", "Medusa"
        ),
        "None"
    )

    private val bowForwardMultiplier by float("BowForwardMultiplier", 1f, 0.2F..1f)
    private val bowStrafeMultiplier by float("BowStrafeMultiplier", 1f, 0.2F..1f)

    // Blocks
    val soulSand by boolean("SoulSand", true)
    val liquidPush by boolean("LiquidPush", true)
    private val teleportValue by boolean("Teleport", false)
    private val teleportMode by choices("TeleportMode", arrayOf("Vanilla", "VanillaNoSetback", "Custom", "Decrease"), "Vanilla") {
        teleportValue
    }
    private val teleportNoApplyValue by boolean("TeleportNoApply", false) { teleportValue }
    private val teleportCustomSpeedValue by float("Teleport-CustomSpeed", 0.13f, 0f..1f) {
        teleportValue && teleportMode == "Custom"
    }
    private val teleportCustomYValue by boolean("Teleport-CustomY", false) {
        teleportValue && teleportMode == "Custom"
    }
    private val teleportDecreasePercentValue by float("Teleport-DecreasePercent", 0.13f, 0f..1f) {
        teleportValue && teleportMode == "Decrease"
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

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> if (isForward) consumeForwardMultiplier else consumeStrafeMultiplier

        is ItemSword -> if (isForward) blockForwardMultiplier else blockStrafeMultiplier

        is ItemBow -> if (isForward) bowForwardMultiplier else bowStrafeMultiplier

        else -> 0.2F
    }

    fun isUNCPBlocking() =
        swordMode == "UpdatedNCP" && mc.gameSettings.keyBindUseItem.isKeyDown && (mc.thePlayer.heldItem?.item is ItemSword)

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
