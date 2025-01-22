/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object TargetModule : Module("Target", Category.CLIENT, gameDetecting = false, canBeEnabled = false) {
    var playerValue by boolean("Player", true)
    var animalValue by boolean("Animal", true)
    var mobValue by boolean("Mob", true)
    var invisibleValue by boolean("Invisible", false)
    var deadValue by boolean("Dead", false)

    override fun handleEvents() = true
}