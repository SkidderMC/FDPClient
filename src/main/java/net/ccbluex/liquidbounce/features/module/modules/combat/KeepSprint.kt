/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue

@ModuleInfo(name = "KeepSprint", category = ModuleCategory.COMBAT)
class KeepSprint : Module() {

    val s = FloatValue("Motion", 0.0F , 0.0F, 1.0F)
    val aws = BoolValue("AlwaysSprint", false)
}
