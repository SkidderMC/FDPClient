/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.EnumTriggerType
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.value.BoolValue
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display

@ModuleInfo(name = "FreeLook",  description = "", category = ModuleCategory.VISUAL, keyBind = Keyboard.KEY_Z, triggerType = EnumTriggerType.PRESS)
class FreeLook : Module() {
    private val thirdPerson = BoolValue("ThirdPerson", true)
    val reverse = BoolValue("Reverse", false)

    override fun onEnable() {
        isEnabled = true
        isReverse = reverse.get()
        perspectiveToggled = !perspectiveToggled
        setRotations()
        if (perspectiveToggled) {
            previousPerspective = mc.gameSettings.thirdPersonView
            if (thirdPerson.get())
                mc.gameSettings.thirdPersonView = 1
        } else {
            mc.gameSettings.thirdPersonView = previousPerspective
        }
    }

    override fun onDisable() {
        isEnabled = false
        resetPerspective()
    }

    fun setRotations() {
        cameraYaw = mc.thePlayer.rotationYaw
        cameraPitch = mc.thePlayer.rotationPitch
    }

    companion object {
        private val mc = MinecraftInstance.mc

        @JvmField
        var isReverse = false

        @JvmField
        var isEnabled = false

        @JvmField
        var perspectiveToggled = false

        @JvmField
        var cameraYaw = 0f

        @JvmField
        var cameraPitch = 0f
        private var previousPerspective = 0

        @JvmStatic
        fun overrideMouse(): Boolean {
            if (mc.inGameHasFocus && Display.isActive()) {
                if (!perspectiveToggled) {
                    return true
                }
                mc.mouseHelper.mouseXYChange()
                val f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f
                val f2 = f1 * f1 * f1 * 8.0f
                val f3 = mc.mouseHelper.deltaX.toFloat() * f2
                val f4 = mc.mouseHelper.deltaY.toFloat() * f2
                cameraYaw += f3 * 0.15f
                cameraPitch -= f4 * 0.15f
                if (cameraPitch > 90) cameraPitch = 90f
                if (cameraPitch < -90) cameraPitch = -90f
            }
            return false
        }

        fun resetPerspective() {
            perspectiveToggled = false
            mc.gameSettings.thirdPersonView = previousPerspective
        }
    }
}
