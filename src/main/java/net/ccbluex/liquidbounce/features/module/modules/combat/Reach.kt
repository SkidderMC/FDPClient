/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.FloatValue
import kotlin.math.max

@ModuleInfo(name = "Reach", category = ModuleCategory.COMBAT)
object Reach : Module() {

    val combatReachValue = FloatValue("CombatReach", 3.5f, 3f, 7f)
    val buildReachValue = FloatValue("BuildReach", 5f, 4.5f, 7f)

    val maxRange: Float

        get() = max(combatReachValue.get(), buildReachValue.get())

    val hitReach: Float
        get() = if (state) {
            combatReachValue.get()
        } else {
            3f
        }
}
