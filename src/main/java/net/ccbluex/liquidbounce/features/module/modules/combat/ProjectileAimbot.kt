/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

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

object ProjectileAimbot : Module("ProjectileAimbot", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val bow by boolean("Bow", true).subjective()
    private val egg by boolean("Egg", true).subjective()
    private val snowball by boolean("Snowball", true).subjective()
    private val pearl by boolean("EnderPearl", false).subjective()
    private val otherItems by boolean("OtherItems", false).subjective()

    private val range by float("Range", 10f, 0f..30f)
    private val throughWalls by boolean("ThroughWalls", false)
    private val throughWallsRange by float("ThroughWallsRange", 10f, 0f..30f) { throughWalls }

    private val priority by choices(
        "Priority",
        arrayOf("Health", "Distance", "Direction"),
        "Direction"
    )

    private val gravityType by choices("GravityType", arrayOf("None", "Projectile"), "Projectile")

    private val predict by boolean("Predict", true) { gravityType == "Projectile" }
    private val predictSize by float("PredictSize", 2F, 0.1F..5F)
    { predict && gravityType == "Projectile" }

    private val options = RotationSettings(this).withoutKeepRotation()

    private val randomization = RandomizationSettings(this) { options.rotationsActive }

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
        coercedPoint.displayName
    }
    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
        coercedPoint.displayName
    }

    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange("HorizontalBodySearchRange", 0f..1f, 0f..1f)
    { options.rotationsActive }

    private val mark by boolean("Mark", true).subjective()

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
                horizontalSearch = horizontalBodySearchRange
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