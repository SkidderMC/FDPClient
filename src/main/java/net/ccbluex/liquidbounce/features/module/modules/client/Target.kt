package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue

@ModuleInfo(name = "Target", category = ModuleCategory.CLIENT, canEnable = false)
object Target : Module() {
    val playerValue = BoolValue("Player", true)
    val animalValue = BoolValue("Animal", false)
    val mobValue = BoolValue("Mob", true)
    val invisibleValue = BoolValue("Invisible", false)
    val deadValue = BoolValue("Dead", false)

    // always handle event
    override fun handleEvents() = true
}