/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.client

import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.ListValue
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
