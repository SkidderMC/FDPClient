/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

/**
 * AutoF5 forces a chosen camera perspective while enabled and can snap back to
 * first person whenever the player swings or attacks, restoring the original
 * perspective once the module is turned off.
 *
 * @author opZywl
 */
object AutoF5 : Module("AutoF5", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    private val perspective by choices("Perspective", arrayOf("ThirdBack", "ThirdFront", "First"), "ThirdBack")
    private val autoReturnOnAttack by boolean("AutoReturnOnAttack", true)

    private var previousView = 0

    private fun viewFor(mode: String) = when (mode.lowercase()) {
        "thirdback" -> 1
        "thirdfront" -> 2
        else -> 0
    }

    override fun onEnable() {
        previousView = mc.gameSettings.thirdPersonView
        mc.gameSettings.thirdPersonView = viewFor(perspective)
    }

    override fun onDisable() {
        mc.gameSettings.thirdPersonView = previousView
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (autoReturnOnAttack && (player.isSwingInProgress || mc.gameSettings.keyBindAttack.pressTime != 0)) {
            if (mc.gameSettings.thirdPersonView != 0) mc.gameSettings.thirdPersonView = 0
            return@handler
        }

        val target = viewFor(perspective)
        if (mc.gameSettings.thirdPersonView != target) mc.gameSettings.thirdPersonView = target
    }
}
