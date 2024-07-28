/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.animalValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.deadValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.invisibleValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.mobValue
import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule.playerValue
import net.ccbluex.liquidbounce.features.module.modules.client.Teams
import net.ccbluex.liquidbounce.handler.combat.CombatManager.isFocusEntity
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.StringUtils.contains
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Vec3
import kotlin.math.cos
import kotlin.math.sin

object EntityUtils : MinecraftInstance() {

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

        return if (health > 0) health else 20f
    }

}