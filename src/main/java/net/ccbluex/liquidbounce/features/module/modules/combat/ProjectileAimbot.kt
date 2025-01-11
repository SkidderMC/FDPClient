/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.inventory.isEmpty
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.rotation.RandomizationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.faceTrajectory
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.*
import java.awt.Color

object ProjectileAimbot : Module("ProjectileAimbot", Category.COMBAT, hideModule = false) {

    private val bow by boolean("Bow", true, subjective = true)
    private val egg by boolean("Egg", true, subjective = true)
    private val snowball by boolean("Snowball", true, subjective = true)
    private val pearl by boolean("EnderPearl", false, subjective = true)
    private val otherItems by boolean("OtherItems", false, subjective = true)

    private val range by float("Range", 10f, 0f..30f)
    private val throughWalls by boolean("ThroughWalls", false, subjective = true)
    private val throughWallsRange by float("ThroughWallsRange", 10f, 0f..30f) { throughWalls }

    private val priority by choices(
        "Priority",
        arrayOf("Health", "Distance", "Direction"),
        "Direction",
        subjective = true
    )

    private val gravityType by choices("GravityType", arrayOf("None", "Projectile"), "Projectile")

    private val predict by boolean("Predict", true) { gravityType == "Projectile" }
    private val predictSize by float("PredictSize", 2F, 0.1F..5F)
    { predict && gravityType == "Projectile" }

    private val options = RotationSettings(this).withoutKeepRotation()

    private val randomization = RandomizationSettings(this) { options.rotationsActive }

    private val highestBodyPointToTargetValue: ListValue = object : ListValue(
        "HighestBodyPointToTarget",
        arrayOf("Head", "Body", "Feet"),
        "Head"
    ) {
        override fun isSupported() = options.rotationsActive

        override fun onChange(oldValue: String, newValue: String): String {
            val newPoint = RotationUtils.BodyPoint.fromString(newValue)
            val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
            val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
            return coercedPoint.name
        }
    }
    private val highestBodyPointToTarget by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue: ListValue = object : ListValue(
        "LowestBodyPointToTarget",
        arrayOf("Head", "Body", "Feet"),
        "Body"
    ) {
        override fun isSupported() = options.rotationsActive

        override fun onChange(oldValue: String, newValue: String): String {
            val newPoint = RotationUtils.BodyPoint.fromString(newValue)
            val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
            val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
            return coercedPoint.name
        }
    }

    private val lowestBodyPointToTarget by lowestBodyPointToTargetValue

    private val maxHorizontalBodySearch: FloatValue = object : FloatValue("MaxHorizontalBodySearch", 1f, 0f..1f) {
        override fun isSupported() = options.rotationsActive

        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalBodySearch.get())
    }

    private val minHorizontalBodySearch: FloatValue = object : FloatValue("MinHorizontalBodySearch", 0f, 0f..1f) {
        override fun isSupported() = options.rotationsActive

        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalBodySearch.get())
    }

    private val mark by boolean("Mark", true, subjective = true)

    private var target: Entity? = null

    override fun onDisable() {
        target = null
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        target = null

        val targetRotation = when (val item = player.heldItem?.item) {
            is ItemBow -> {
                if (!bow || !player.isUsingItem)
                    return@handler

                target = getTarget(throughWalls, priority)

                faceTrajectory(target ?: return@handler, predict, predictSize)
            }

            is Item -> {
                if (!otherItems && !player.heldItem.isEmpty() ||
                    (!egg && item is ItemEgg || !snowball && item is ItemSnowball || !pearl && item is ItemEnderPearl)
                )
                    return@handler

                target = getTarget(throughWalls, priority)

                faceTrajectory(target ?: return@handler, predict, predictSize, gravity = 0.03f, velocity = 0.5f)
            }

            else -> return@handler
        }

        val normalRotation = target?.entityBoundingBox?.let {
            searchCenter(
                it,
                outborder = false,
                randomization = this.randomization,
                predict = true,
                lookRange = range,
                attackRange = range,
                throughWallsRange = throughWallsRange,
                bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
                horizontalSearch = minHorizontalBodySearch.get()..maxHorizontalBodySearch.get()
            )
        } ?: return@handler

        setTargetRotation(if (gravityType == "Projectile") targetRotation else normalRotation, options = options)
    }

    val onRender3D = handler<Render3DEvent> {
        if (target != null && priority != "Multi" && mark) {
            drawPlatform(target!!, Color(37, 126, 255, 70))
        }
    }

    private fun getTarget(throughWalls: Boolean, priorityMode: String): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld.loadedEntityList
            .asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filter {
                val distance = player.getDistanceToEntityBox(it)

                isSelected(it, true) && distance <= range && (throughWalls ||
                        player.canEntityBeSeen(it) && distance <= throughWallsRange)
            }.minByOrNull { entity ->
                return@minByOrNull when (priorityMode.uppercase()) {
                    "DISTANCE" -> player.getDistanceToEntityBox(entity)
                    "DIRECTION" -> rotationDifference(entity).toDouble()
                    "HEALTH" -> entity.health.toDouble()
                    else -> 0.0 // Edge case
                }
            }
    }

    fun hasTarget() = target != null && mc.thePlayer.canEntityBeSeen(target)
}
