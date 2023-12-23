/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue

@ModuleInfo(name = "NoRotateSet", category = ModuleCategory.OTHER)
object NoRotateSet : Module() {

    val noLoadingValue = BoolValue("NoLoading", true)
    val overwriteTeleportValue = BoolValue("SilentConfirm", true)
    val rotateValue = BoolValue("SilentConfirmSetRotation", true).displayable { overwriteTeleportValue.get() }
}