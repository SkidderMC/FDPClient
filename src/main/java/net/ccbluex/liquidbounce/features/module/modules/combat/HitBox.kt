/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

object HitBox : Module("HitBox", Category.COMBAT, Category.SubCategory.COMBAT_RAGE) {

    private val targetPlayers by boolean("TargetPlayers", true)
        .describe("Expand the hitbox of other players.")
    private val playerSize by float("PlayerSize", 0.4F, 0F..1F) { targetPlayers }
        .describe("Extra hitbox size for regular players.")
    private val friendSize by float("FriendSize", 0.4F, 0F..1F) { targetPlayers }
        .describe("Extra hitbox size for friends.")
    private val teamMateSize by float("TeamMateSize", 0.4F, 0F..1F) { targetPlayers }
        .describe("Extra hitbox size for teammates.")
    private val botSize by float("BotSize", 0.4F, 0F..1F) { targetPlayers }
        .describe("Extra hitbox size for bots.")

    private val targetMobs by boolean("TargetMobs", false)
        .describe("Expand the hitbox of hostile mobs.")
    private val mobSize by float("MobSize", 0.4F, 0F..1F) { targetMobs }
        .describe("Extra hitbox size for mobs.")

    private val targetAnimals by boolean("TargetAnimals", false)
        .describe("Expand the hitbox of animals.")
    private val animalSize by float("AnimalSize", 0.4F, 0F..1F) { targetAnimals }
        .describe("Extra hitbox size for animals.")

    init {
        group("Players", "TargetPlayers", "PlayerSize", "FriendSize", "TeamMateSize", "BotSize")
        group("Mobs", "TargetMobs", "MobSize")
        group("Animals", "TargetAnimals", "AnimalSize")
    }

    fun determineSize(entity: Entity): Float {
        return when (entity) {
            is EntityPlayer -> {
                if (entity.isSpectator || !targetPlayers) {
                    return 0F
                }

                if (isBot(entity)) {
                    return botSize
                } else if (entity.isClientFriend()) {
                    return friendSize
                } else if (Teams.handleEvents() && Teams.isInYourTeam(entity)) {
                    return teamMateSize
                }

                playerSize
            }

            else -> {
                if (entity.isMob() && targetMobs) {
                    return mobSize
                } else if (entity.isAnimal() && targetAnimals) {
                    return animalSize
                }

                0F
            }
        }
    }
}