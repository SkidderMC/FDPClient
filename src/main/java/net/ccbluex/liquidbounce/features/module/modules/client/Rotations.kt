/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Rotations", category = ModuleCategory.CLIENT, canEnable = false)
object Rotations : Module() {
    val headValue = BoolValue("Head", true)
    val bodyValue = BoolValue("Body", true)
    val fixedValue = ListValue("SensitivityFixed", arrayOf("None", "Old", "New"), "New")
//    val rotationHumanizeType=EaseUtils.getEnumEasingList("HumanizeType")
//    val rotationHumanizeOrder=EaseUtils.getEnumEasingOrderList("HumanizeOrder")
    val nanValue = BoolValue("NaNCheck", true)

//    fun apply(value: Double):Double{
//        return EaseUtils.apply(toEnumType(), toEnumOrder(),value)
//    }
//
//    fun toEnumType():EaseUtils.EnumEasingType{
//        return EaseUtils.EnumEasingType.valueOf(rotationHumanizeType.get().uppercase())
//    }
//
//    fun toEnumOrder():EaseUtils.EnumEasingOrder{
//        return EaseUtils.EnumEasingOrder.valueOf(rotationHumanizeOrder.get().uppercase())
//    }
}
