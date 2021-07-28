/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Rotations", description = "Rotation settings.", category = ModuleCategory.CLIENT, canEnable = false)
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
//        return EaseUtils.EnumEasingType.valueOf(rotationHumanizeType.get().toUpperCase())
//    }
//
//    fun toEnumOrder():EaseUtils.EnumEasingOrder{
//        return EaseUtils.EnumEasingOrder.valueOf(rotationHumanizeOrder.get().toUpperCase())
//    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (RotationUtils.serverRotation != null) {
            if (bodyValue.get()) {
                mc.thePlayer.prevRenderYawOffset = RotationUtils.serverRotation.yaw
                mc.thePlayer.renderYawOffset = RotationUtils.serverRotation.yaw
            }
            if (headValue.get()) {
                mc.thePlayer.prevRotationYawHead = RotationUtils.serverRotation.yaw
                mc.thePlayer.rotationYawHead = RotationUtils.serverRotation.yaw
            }
        }
    }

    //always handle event
    override fun handleEvents() = true
}
