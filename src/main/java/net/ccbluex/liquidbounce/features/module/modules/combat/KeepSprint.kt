package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.value.BoolValue

class KeepSprint : Module(name = "KeepSprint", category = ModuleCategory.COMBAT) {
    val aws = BoolValue("AlwaysSprint", false)
}