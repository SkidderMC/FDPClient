/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils.contains
import net.ccbluex.liquidbounce.event.handler
import net.minecraft.entity.boss.IBossDisplayData
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion

object GameDetector : Module("GameDetector", Category.CLIENT, gameDetecting = false) {
    // Check if player's gamemode is Survival or Adventure
    private val gameMode by boolean("GameModeCheck", true)

    // Check if player doesn't have unnatural capabilities
    private val capabilities by boolean("CapabilitiesCheck", true)

    // Check if there are > 1 players in tablist
    private val tabList by boolean("TabListCheck", true)

    // Check if there are > 1 teams or if friendly fire is enabled
    private val teams by boolean("TeamsCheck", true)

    // Check if player doesn't have infinite invisibility effect
    private val invisibility by boolean("InvisibilityCheck", true)

    // Check if player has compass inside their inventory
    private val compass by boolean("CompassCheck", false)

    // Check for compass inside inventory. If false, then it should only check for selected slot
    private val checkAllSlots by boolean("CheckAllSlots", true) { compass }
    private val slot by int("Slot", 1, 1..9) { compass && !checkAllSlots }

    // Check for any hub-like BossBar or ArmorStand entities
    private val entity by boolean("EntityCheck", false)

    // Check for strings in scoreboard that could signify that the game is waiting for players or if you are in a lobby
    // Needed on Gamster
    private val scoreboard by boolean("ScoreboardCheck", false)

    private val WHITELISTED_SUBSTRINGS = arrayOf(":", "Vazio!", "§6§lRumble Box", "§5§lDivine Drop")

    private var isPlaying = false

    private val LOBBY_SUBSTRINGS = arrayOf("lobby", "hub", "waiting", "loading", "starting")

    fun isInGame() = !state || isPlaying

    val onUpdate = handler<UpdateEvent>(priority = 1) {
        isPlaying = false

        val thePlayer = mc.thePlayer ?: return@handler
        val theWorld = mc.theWorld ?: return@handler
        val netHandler = mc.netHandler ?: return@handler
        val capabilities = thePlayer.capabilities

        val slots = slot - 1
        val itemSlot = mc.thePlayer.inventory.getStackInSlot(slots)

        if (gameMode && !mc.playerController.gameIsSurvivalOrAdventure())
            return@handler

        if (this@GameDetector.capabilities &&
            (!capabilities.allowEdit || capabilities.allowFlying || capabilities.isFlying || capabilities.disableDamage)
        )
            return@handler

        if (tabList && netHandler.playerInfoMap.size <= 1)
            return@handler

        if (teams && thePlayer.team?.allowFriendlyFire == false && theWorld.scoreboard.teams.size == 1)
            return@handler

        if (invisibility && thePlayer.getActivePotionEffect(Potion.invisibility)?.isPotionDurationMax == true)
            return@handler

        if (compass) {
            if (checkAllSlots && mc.thePlayer.inventory.hasItemStack(ItemStack(Items.compass)))
                return@handler

            if (!checkAllSlots && itemSlot?.item == Items.compass)
                return@handler
        }

        if (scoreboard) {
            if (LOBBY_SUBSTRINGS in theWorld.scoreboard.getObjectiveInDisplaySlot(1)?.displayName)
                return@handler

            if (theWorld.scoreboard.objectiveNames.any { LOBBY_SUBSTRINGS in it })
                return@handler

            if (theWorld.scoreboard.teams.any { LOBBY_SUBSTRINGS in it.colorPrefix })
                return@handler
        }

        if (entity) {
            for (entity in theWorld.loadedEntityList) {
                if (entity !is IBossDisplayData && entity !is EntityArmorStand)
                    continue

                val name = entity.customNameTag ?: continue

                // If an unnatural entity has been found, break the loop if its name includes a whitelisted substring
                if (WHITELISTED_SUBSTRINGS in name) break
                else return@handler
            }
        }

        isPlaying = true
    }

    val onWorld = handler<WorldEvent> {
        isPlaying = false
    }
}