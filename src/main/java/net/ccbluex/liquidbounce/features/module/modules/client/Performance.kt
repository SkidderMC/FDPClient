package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "Performance", category = ModuleCategory.CLIENT)
object Performance : Module() {
    // render settings
    val staticParticleColorValue = BoolValue("StaticParticleColor", false)

    // lighting settings
//    val fastEntityLightningValue = BoolValue("FastEntityLightning", false)

    // TODO: more
}