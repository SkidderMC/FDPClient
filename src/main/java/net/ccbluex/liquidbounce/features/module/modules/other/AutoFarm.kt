/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.block.BlockUtils.getCenterDistance
import net.ccbluex.liquidbounce.utils.block.BlockUtils.searchBlocks
import net.ccbluex.liquidbounce.utils.block.center
import net.ccbluex.liquidbounce.utils.block.state
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.extensions.eyes
import net.ccbluex.liquidbounce.utils.extensions.onPlayerRightClick
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.hasSpaceInInventory
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.getEnchantmentLevel
import net.minecraft.enchantment.Enchantment
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBlockBox
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceBlock
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.minecraft.block.Block
import net.minecraft.block.BlockCarrot
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockNetherWart
import net.minecraft.block.BlockPotato
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemDye
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.roundToInt

object AutoFarm : Module(
    "AutoFarm", Category.OTHER, Category.SubCategory.MISCELLANEOUS,
    gameDetecting = false, spacedName = "Auto Farm"
) {

    /**
     * OPTIONS
     */

    private val radius by float("Radius", 4.5F, 1F..6F)
        .describe("Maximum distance to search for crops.")
    private val throughWalls by boolean("ThroughWalls", false)
        .describe("Allow farming crops hidden behind walls.")
    private val wallRange by float("WallRange", 6F, 0F..6F) { throughWalls }
        .describe("Reduced reach used for crops behind walls.")

    private val replant by boolean("Replant", true)
        .describe("Replant the matching seed after harvesting.")
    private val bonemeal by boolean("Bonemeal", false)
        .describe("Apply bone meal to immature crops to grow them.")

    private val delay by int("Delay", 200, 0..2000)
        .describe("Delay between farming actions in milliseconds.")
    private val swapBackDelay by int("SwapBackDelay", 1, 1..20, "ticks")
        .describe("Ticks before switching back to the held item.")
    private val useFortune by boolean("UseFortune", false)
        .describe("Swap to a Fortune-enchanted tool before harvesting for more drops.")

    private val disableOnFullInventory by boolean("DisableOnFullInventory", false)
        .describe("Turn off the module when the inventory is full.")

    private val rotationsValue = boolean("Rotations", true)
        .describe("Rotate toward crops before harvesting them.")
    private val rotations by rotationsValue

    private val rotationSettings = RotationSettings(this) { rotations }.apply {
        withRequestPriority(RotationPriority.LOW)
        immediate = true

        resetTicksValue.excludeWithState()
        withoutKeepRotation()
    }

    private val mark by boolean("Mark", false)
        .describe("Render a box around the targeted crop.")

    init {
        group("Target", "Radius", "ThroughWalls", "WallRange")
        group("Actions", "Replant", "Bonemeal", "Delay", "SwapBackDelay")
        group("Misc", "DisableOnFullInventory", "Mark")
        rotationSettings.nestInto(group("Rotation", "Rotations"))
    }

    /**
     * Set of bone meal dye metadata (white dye color index 15).
     */
    private const val BONE_MEAL_META = 15

    private var lastActionTime = 0L
    private var renderBlock: BlockPos? = null

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler
        val world = mc.theWorld ?: return@handler

        renderBlock = null

        // Auto-disable when the inventory has no free slot left
        if (disableOnFullInventory && !hasSpaceInInventory()) {
            state = false
            return@handler
        }

        if (System.currentTimeMillis() - lastActionTime < delay) {
            return@handler
        }

        val eyes = player.eyes
        val scanRadius = radius.roundToInt() + 1

        // Search every crop block within radius
        val crops = searchBlocks(scanRadius, null) { pos, block ->
            if (getCenterDistance(pos) > radius || !isCrop(block)) {
                return@searchBlocks false
            }

            val inLineOfSight = world.rayTraceBlocks(eyes, pos.center, false, true, false)?.blockPos == pos

            // Line-of-sight crops use the full radius; crops behind walls are only
            // allowed (when ThroughWalls is on) within the reduced wall range
            inLineOfSight || (throughWalls && getCenterDistance(pos) <= wallRange)
        }

        if (crops.isEmpty()) {
            return@handler
        }

        // Sort by closest to the player's eyes
        val sorted = crops.entries.sortedBy { (pos, _) -> getCenterDistance(pos) }

        // Try to harvest the closest mature crop first
        for ((pos, block) in sorted) {
            if (!isMatureCrop(block, pos)) {
                continue
            }

            renderBlock = pos

            // Rotate towards the crop (silent / visible per RotationSettings)
            if (rotations) {
                val rotation = faceBlock(pos, throughWalls) ?: continue

                setTargetRotation(rotation.rotation, options = rotationSettings)
            }

            harvest(pos)

            // Best-effort replant on the soil below the harvested crop
            if (replant) {
                tryUseSeed(block, pos)
            }

            lastActionTime = System.currentTimeMillis()
            return@handler
        }

        // No mature crop found: try bone-mealing an immature crop to speed up growth
        if (bonemeal) {
            for ((pos, block) in sorted) {
                if (isMatureCrop(block, pos)) {
                    continue
                }

                renderBlock = pos

                if (rotations) {
                    val rotation = faceBlock(pos, throughWalls) ?: continue

                    setTargetRotation(rotation.rotation, options = rotationSettings)
                }

                if (tryUseBonemeal(pos)) {
                    lastActionTime = System.currentTimeMillis()
                    return@handler
                }
            }
        }
    }

    val onRender3D = handler<Render3DEvent> {
        if (!mark) {
            return@handler
        }

        val pos = renderBlock ?: return@handler

        drawBlockBox(pos, Color(0, 200, 70), true)
    }

    /**
     * Instantly break the crop at [pos]. Crops have ~0 hardness, so a single
     * start/stop digging sequence is enough.
     */
    private fun harvest(pos: BlockPos) {
        val player = mc.thePlayer ?: return

        if (useFortune) {
            findHotbarSlot { it.getEnchantmentLevel(Enchantment.fortune) > 0 }?.let { slot ->
                SilentHotbar.selectSlotSilently(this, slot, ticksUntilReset = swapBackDelay, immediate = true, render = false)
            }
        }

        sendPacket(C07PacketPlayerDigging(START_DESTROY_BLOCK, pos, EnumFacing.DOWN))
        player.swingItem()
        sendPacket(C07PacketPlayerDigging(STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN))
        mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN)
    }

    /**
     * Check if [block] is one of the supported crop block types.
     *
     * Note: in 1.8.9 BlockCarrot and BlockPotato extend BlockCrops, so the
     * BlockCrops branch already covers them, but they are listed explicitly
     * for clarity and to keep the seed mapping unambiguous.
     */
    private fun isCrop(block: Block): Boolean =
        block is BlockCrops || block is BlockCarrot || block is BlockPotato || block is BlockNetherWart

    /**
     * Check if [block] at [pos] is fully grown.
     */
    private fun isMatureCrop(block: Block, pos: BlockPos): Boolean {
        val state = pos.state ?: return false
        val meta = block.getMetaFromState(state)

        return when (block) {
            // Wheat, carrots and potatoes all share the 0..7 age range
            is BlockCarrot, is BlockPotato, is BlockCrops -> meta >= 7
            // Nether wart grows on a 0..3 age range
            is BlockNetherWart -> meta >= 3
            else -> false
        }
    }

    /**
     * Try to replant the matching seed on the soil below the harvested [pos]
     * by silently switching to it and right-clicking the soil.
     */
    private fun tryUseSeed(block: Block, pos: BlockPos): Boolean {
        val seed = seedFor(block) ?: return false
        val slot = findHotbarSlot { it.item == seed } ?: return false

        val soilPos = pos.down()

        return placeSilently(slot, soilPos, EnumFacing.UP, soilPos.center)
    }

    /**
     * Try to apply bone meal to the crop at [pos] by silently switching to a
     * bone meal stack and right-clicking the crop.
     */
    private fun tryUseBonemeal(pos: BlockPos): Boolean {
        val slot = findHotbarSlot {
            it.item is ItemDye && it.itemDamage == BONE_MEAL_META
        } ?: return false

        return placeSilently(slot, pos, EnumFacing.UP, pos.center)
    }

    /**
     * Switch to [slot] silently, right-click [clickPos] on [side], then restore.
     */
    private fun placeSilently(slot: Int, clickPos: BlockPos, side: EnumFacing, hitVec: Vec3): Boolean {
        val player = mc.thePlayer ?: return false

        SilentHotbar.selectSlotSilently(this, slot, ticksUntilReset = swapBackDelay, immediate = true, render = false)

        val stack = player.inventory.mainInventory[slot] ?: return false
        val success = player.onPlayerRightClick(clickPos, side, hitVec, stack)

        if (success) {
            player.swingItem()
        }

        SilentHotbar.resetSlot(this, immediate = true)

        return success
    }

    /**
     * Map a crop [block] to the item used to replant it.
     */
    private fun seedFor(block: Block): Item? = when (block) {
        is BlockCarrot -> Items.carrot
        is BlockPotato -> Items.potato
        is BlockNetherWart -> Items.nether_wart
        is BlockCrops -> Items.wheat_seeds
        else -> null
    }

    /**
     * Find a hotbar slot (0..8) whose stack matches [predicate].
     */
    private inline fun findHotbarSlot(predicate: (ItemStack) -> Boolean): Int? {
        val player = mc.thePlayer ?: return null

        for (i in 0..8) {
            val stack = player.inventory.mainInventory[i] ?: continue
            if (predicate(stack)) {
                return i
            }
        }

        return null
    }

    override fun onDisable() {
        renderBlock = null
        SilentHotbar.resetSlot(this)
    }
}
