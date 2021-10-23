package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "Target", category = ModuleCategory.CLIENT, canEnable = false)
object Target : Module() {
    val player = BoolValue("Player", true)
    val animal = BoolValue("Animal", false)
    val mob = BoolValue("Mob", true)
    val invisible = BoolValue("Invisible", false)
    val dead = BoolValue("Dead", false)

    // always handle event
    override fun handleEvents() = true
}