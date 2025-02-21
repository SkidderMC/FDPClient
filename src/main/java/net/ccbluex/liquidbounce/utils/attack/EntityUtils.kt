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
import net.ccbluex.liquidbounce.utils.extensions.toRadiansD
import net.ccbluex.liquidbounce.utils.kotlin.StringUtils.contains
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

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

    fun isLookingOnEntities(entity: Any, maxAngleDifference: Double): Boolean {
        val player = mc.thePlayer ?: return false

        if (entity == player) return true

        val playerYaw = player.rotationYawHead
        val playerPitch = player.rotationPitch

        val maxAngleDifferenceRadians = Math.toRadians(maxAngleDifference)

        val lookVec = Vec3(
            -sin(playerYaw.toRadiansD()),
            -sin(playerPitch.toRadiansD()),
            cos(playerYaw.toRadiansD())
        ).normalize()

        val playerPos = player.positionVector.addVector(0.0, player.eyeHeight.toDouble(), 0.0)

        val entityPos = when (entity) {
            is Entity -> entity.positionVector.addVector(0.0, entity.eyeHeight.toDouble(), 0.0)
            is TileEntity -> Vec3(
                entity.pos.x.toDouble(),
                entity.pos.y.toDouble(),
                entity.pos.z.toDouble()
            )
            else -> return false
        }

        val directionToEntity = entityPos.subtract(playerPos).normalize()
        val dotProductThreshold = lookVec.dotProduct(directionToEntity)

        return dotProductThreshold > cos(maxAngleDifferenceRadians)
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
            if (index < 0 || index > 15) continue

            color = ColorUtils.hexColors[index]
            break
        }

        return Color(color)
    }

}