/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.attack

import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.animalValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.deadValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.invisibleValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.mobValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.playerValue
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.handler.combat.CombatManager.isFocusEntity
import net.ccbluex.liquidbounce.ui.font.GameFontRenderer.Companion.getColorIndex
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.extensions.isAnimal
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.extensions.isMob
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object EntityUtils : MinecraftInstance {

    private val healthSubstrings = arrayOf("hp", "health", "❤", "lives")

    fun isSelected(entity: Entity?, canAttackCheck: Boolean): Boolean {
        if (entity is EntityLivingBase && (deadValue || entity.isEntityAlive) && entity != mc.thePlayer) {
            if (invisibleValue || !entity.isInvisible) {
                if (playerValue && entity is EntityPlayer) {
                    if (canAttackCheck) {
                        if (isBot(entity))
                            return false

                        if (entity.isClientFriend())
                            return false

                        if (entity.isSpectator) return false

                        if (!isFocusEntity(entity)) {
                            return false
                        }

                        return !Teams.handleEvents() || !Teams.isInYourTeam(entity)
                    }
                    return true
                }

                return mobValue && entity.isMob() || animalValue && entity.isAnimal()
            }
        }
        return false
    }

    fun getHealth(entity: EntityLivingBase, fromScoreboard: Boolean = false, absorption: Boolean = true): Float {
        if (fromScoreboard && entity is EntityPlayer) run {
            val scoreboard = entity.worldScoreboard
            val objective = scoreboard.getValueFromObjective(entity.name, scoreboard.getObjectiveInDisplaySlot(2))

            if (healthSubstrings !in objective.objective?.displayName)
                return@run

            val scoreboardHealth = objective.scorePoints

            if (scoreboardHealth > 0)
                return scoreboardHealth.toFloat()
        }

        var health = entity.health

        if (absorption)
            health += entity.absorptionAmount

        return if (health >= 0) health else 20f
    }

    fun Entity.colorFromDisplayName(): Color? {
        val chars = (this.displayName ?: return null).formattedText.toCharArray()
        var color = Int.MAX_VALUE

        for (i in 0 until chars.lastIndex) {
            if (chars[i] != '§') continue

            val index = getColorIndex(chars[i + 1])
            if (index !in 0..15) continue

            color = ColorUtils.hexColors[index]
            break
        }

        return Color(color)
    }

}
