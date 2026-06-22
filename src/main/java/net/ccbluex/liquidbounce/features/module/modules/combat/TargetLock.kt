/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.extensions.withAlpha
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBoxESP
import net.minecraft.entity.EntityLivingBase
import java.awt.Color

/**
 * TargetLock
 *
 * Locks onto the entity you are currently fighting and keeps the combat target
 * focused on it, even if a closer or otherwise higher-priority entity appears.
 * Optionally draws a marker on the locked entity.
 */
object TargetLock : Module("TargetLock", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, gameDetecting = false) {

    private val maxRange by float("MaxRange", 20F, 8F..40F)
        .describe("Drop the lock if the target gets farther than this.")
    private val drawMarker by boolean("Marker", true)
        .describe("Draw a marker on the locked entity.")
    private val rainbowColor by boolean("Rainbow", false) { drawMarker }
        .describe("Use a rainbow color for the marker.")
    private val red by int("Red", 0, 0..255) { drawMarker && !rainbowColor }
        .describe("Red component of the marker color.")
    private val green by int("Green", 200, 0..255) { drawMarker && !rainbowColor }
        .describe("Green component of the marker color.")
    private val blue by int("Blue", 255, 0..255) { drawMarker && !rainbowColor }
        .describe("Blue component of the marker color.")

    private var lockedEntity: EntityLivingBase? = null

    override val tag
        get() = lockedEntity?.name

    override fun onDisable() {
        lockedEntity = null
    }

    private fun isLockValid(entity: EntityLivingBase?): Boolean {
        val thePlayer = mc.thePlayer ?: return false

        if (entity == null || entity.isDead || entity.health <= 0F) {
            return false
        }

        if (!isSelected(entity, true)) {
            return false
        }

        return thePlayer.getDistanceToEntity(entity) <= maxRange
    }

    @Suppress("unused")
    private val onAttack = handler<AttackEvent> { event ->
        val target = event.targetEntity

        if (target is EntityLivingBase && isSelected(target, true)) {
            lockedEntity = target
        }
    }

    @Suppress("unused")
    private val onUpdate = handler<UpdateEvent> {
        val locked = lockedEntity

        if (!isLockValid(locked)) {
            lockedEntity = null
            return@handler
        }

        // Keep the rage combat target focused on the locked entity
        if (KillAura.handleEvents()) {
            KillAura.target = locked
        }
    }

    @Suppress("unused")
    private val onWorld = handler<WorldEvent> {
        lockedEntity = null
    }

    @Suppress("unused")
    private val onRender3D = handler<Render3DEvent> {
        if (!drawMarker) {
            return@handler
        }

        val locked = lockedEntity ?: return@handler

        if (!isLockValid(locked)) {
            return@handler
        }

        val color = if (rainbowColor) rainbow() else Color(red, green, blue).withAlpha(75)

        drawEntityBoxESP(locked, color)
    }
}
