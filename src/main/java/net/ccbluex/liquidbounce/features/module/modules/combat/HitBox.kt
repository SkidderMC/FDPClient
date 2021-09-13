package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "HitBox", category = ModuleCategory.COMBAT)
class HitBox : Module() {
    val sizeValue = FloatValue("Size", 0.4F, 0F, 1F)
}