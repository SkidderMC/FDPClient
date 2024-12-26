/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.extensions.rotation
import net.ccbluex.liquidbounce.utils.timing.WaitTickUtils
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.intRange
import net.minecraft.entity.player.EntityPlayer

object NoRotateSet : Module("NoRotateSet", Category.OTHER, gameDetecting = false, hideModule = false) {
    var savedRotation = Rotation.ZERO

    private val ignoreOnSpawn by boolean("IgnoreOnSpawn", false)
    val affectRotation by boolean("AffectRotation", true)

    private val ticksUntilStart = intRange("TicksUntilStart", 0..0, 0..20) { affectRotation }
    private val options = RotationSettings(this) { affectRotation }.apply {
        rotationsValue.excludeWithState(true)
        applyServerSideValue.excludeWithState(true)
        resetTicksValue.excludeWithState(1)

        withoutKeepRotation()
    }

    fun shouldModify(player: EntityPlayer) = handleEvents() && (!ignoreOnSpawn || player.ticksExisted != 0)

    fun rotateBackToPlayerRotation() {
        val player = mc.thePlayer ?: return

        currentRotation = player.rotation

        // This connects with the SimulateShortStop code, [performAngleChange] function.
        WaitTickUtils.schedule(ticksUntilStart.random, this)

        setTargetRotation(savedRotation, options = options)
    }
}