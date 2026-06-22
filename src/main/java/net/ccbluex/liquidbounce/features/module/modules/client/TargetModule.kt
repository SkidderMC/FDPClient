/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object TargetModule : Module("Target", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL, gameDetecting = false, canBeEnabled = false) {
    var playerValue by boolean("Player", true)
        .describe("Allow players to be targeted.")
    var animalValue by boolean("Animal", true)
        .describe("Allow animals to be targeted.")
    var mobValue by boolean("Mob", true)
        .describe("Allow hostile mobs to be targeted.")
    var invisibleValue by boolean("Invisible", false)
        .describe("Allow invisible entities to be targeted.")
    var deadValue by boolean("Dead", false)
        .describe("Allow dead entities to be targeted.")

    override fun handleEvents() = true
}
