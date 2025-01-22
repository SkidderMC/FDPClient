/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object AutoPlay : Module("AutoPlay", Category.PLAYER, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Paper", "Hypixel"), "Paper")

    // Hypixel Settings
    private val hypixelMode by choices("HypixelMode", arrayOf("Skywars", "Bedwars"), "Skywars") {
        mode == "Hypixel"
    }
    private val skywarsMode by choices("SkywarsMode", arrayOf("SoloNormal", "SoloInsane"), "SoloNormal") {
        hypixelMode == "Skywars"
    }
    private val bedwarsMode by choices("BedwarsMode", arrayOf("Solo", "Double", "Trio", "Quad"), "Solo") {
        hypixelMode == "Bedwars"
    }

    private val delay by int("Delay", 50, 0..200)

    private var delayTick = 0

    /**
     * Update Event
     */
    val onGameTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler

        if (!playerInGame() || !player.inventory.hasItemStack(ItemStack(Items.paper))) {
            if (delayTick > 0)
                delayTick = 0

            return@handler
        } else {
            delayTick++
        }

        when (mode) {
            "Paper" -> {
                val paper = InventoryUtils.findItem(36, 44, Items.paper) ?: return@handler

                SilentHotbar.selectSlotSilently(this, paper, immediate = true, resetManually = true)

                if (delayTick >= delay) {
                    mc.playerController.sendUseItem(player, mc.theWorld, player.hotBarSlot(paper).stack)
                    delayTick = 0
                }
            }

            "Hypixel" -> {
                if (delayTick >= delay) {
                    when (hypixelMode.lowercase()) {
                        "skywars" -> when (skywarsMode) {
                            "SoloNormal" -> player.sendChatMessage("/play solo_normal")
                            "SoloInsane" -> player.sendChatMessage("/play solo_insane")
                        }

                        "bedwars" -> when (bedwarsMode) {
                            "Solo" -> player.sendChatMessage("/play bedwars_eight_one")
                            "Double" -> player.sendChatMessage("/play bedwars_eight_two")
                            "Trio" -> player.sendChatMessage("/play bedwars_four_three")
                            "Quad" -> player.sendChatMessage("/play bedwars_four_four")
                        }
                    }
                    delayTick = 0
                }
            }
        }
    }

    /**
     * Check whether player is in game or not
     */
    private fun playerInGame(): Boolean {
        val player = mc.thePlayer ?: return false

        return player.ticksExisted >= 20
                && (player.capabilities.isFlying
                || player.capabilities.allowFlying
                || player.capabilities.disableDamage)
    }

    /**
     * HUD Tag
     */
    override val tag
        get() = mode
}