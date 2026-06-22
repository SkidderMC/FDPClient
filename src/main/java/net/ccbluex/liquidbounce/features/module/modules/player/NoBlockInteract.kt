/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.block
import net.minecraft.block.Block
import net.minecraft.block.BlockAnvil
import net.minecraft.block.BlockBeacon
import net.minecraft.block.BlockBed
import net.minecraft.block.BlockBrewingStand
import net.minecraft.block.BlockButton
import net.minecraft.block.BlockCake
import net.minecraft.block.BlockCauldron
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockCommandBlock
import net.minecraft.block.BlockDaylightDetector
import net.minecraft.block.BlockDispenser
import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockEnderChest
import net.minecraft.block.BlockFenceGate
import net.minecraft.block.BlockFlowerPot
import net.minecraft.block.BlockFurnace
import net.minecraft.block.BlockHopper
import net.minecraft.block.BlockJukebox
import net.minecraft.block.BlockLever
import net.minecraft.block.BlockNote
import net.minecraft.block.BlockRedstoneDiode
import net.minecraft.block.BlockSign
import net.minecraft.block.BlockTrapDoor
import net.minecraft.block.BlockWorkbench
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

/**
 * Module NoBlockInteract
 *
 * Prevents right-clicking from opening or activating interactable blocks (chests, doors,
 * buttons, levers, etc.) by cancelling the block-placement/interaction packet for the
 * selected block categories. Useful to avoid accidentally opening containers or toggling
 * blocks while fighting or moving.
 */
object NoBlockInteract : Module("NoBlockInteract", Category.PLAYER, Category.SubCategory.PLAYER_ASSIST, gameDetecting = false) {

    private val containers by boolean("Containers", true)
        .describe("Block interaction with chests and containers.")
    private val doors by boolean("Doors", true)
        .describe("Block interaction with doors and gates.")
    private val buttonsAndLevers by boolean("ButtonsAndLevers", true)
        .describe("Block interaction with buttons and levers.")
    private val redstone by boolean("Redstone", true)
        .describe("Block interaction with redstone components.")
    private val miscellaneous by boolean("Miscellaneous", true)
        .describe("Block interaction with other misc blocks.")
    private val onlyWhenSneaking by boolean("OnlyWhenSneaking", false)
        .describe("Only block interactions while sneaking.")

    private val handlePacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler
        val packet = event.packet

        // Only react to actual block interactions. A direction of 0..5 means a real block
        // face was clicked; 255 means the client is using an item against air.
        if (packet !is C08PacketPlayerBlockPlacement || packet.placedBlockDirection !in 0..5) {
            return@handler
        }

        if (onlyWhenSneaking && !player.isSneaking) {
            return@handler
        }

        val block = packet.position?.block ?: return@handler

        if (shouldCancel(block)) {
            event.cancelEvent()
        }
    }

    private fun shouldCancel(block: Block): Boolean = when {
        containers && block.isContainer() -> true
        doors && block.isDoor() -> true
        buttonsAndLevers && (block is BlockButton || block is BlockLever) -> true
        redstone && block.isRedstone() -> true
        miscellaneous && block.isMisc() -> true
        else -> false
    }

    private fun Block.isContainer(): Boolean =
        this is BlockChest || this is BlockEnderChest || this is BlockFurnace ||
            this is BlockDispenser || this is BlockHopper || this is BlockBrewingStand ||
            this is BlockBeacon || this is BlockWorkbench || this is BlockAnvil

    private fun Block.isDoor(): Boolean =
        this is BlockDoor || this is BlockTrapDoor || this is BlockFenceGate

    private fun Block.isRedstone(): Boolean =
        this is BlockRedstoneDiode || this is BlockNote || this is BlockDaylightDetector ||
            this is BlockCommandBlock

    private fun Block.isMisc(): Boolean =
        this is BlockBed || this is BlockCake || this is BlockCauldron ||
            this is BlockFlowerPot || this is BlockSign || this is BlockJukebox
}
