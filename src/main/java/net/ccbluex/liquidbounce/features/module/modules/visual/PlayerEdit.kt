/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue

/**
 * The type Player edit.
 */
@ModuleInfo(name = "PlayerEdit", description = "", category = ModuleCategory.VISUAL)
class PlayerEdit : Module() {

    companion object {
        @JvmField
        val editPlayerSizeValue = BoolValue("PlayerSize", false)

        /**
         * The constant playerSizeValue.
         */
        @JvmField
        val playerSizeValue = FloatValue("PlayerSize", 1.5f, 0.5f, 2.5f)

        /**
         * The constant rotatePlayer.
         */
        @JvmField
        val rotatePlayer = BoolValue("PlayerRotate", true)

        @JvmField
        val xRot = FloatValue("X-Rotation", 90.0f, -180.0f, 180.0f)

        @JvmField
        val yPos = FloatValue("Y-Position", 0.0f, -5.0f, 5.0f)
    }

    @EventTarget
    fun onMotion(event: MotionEvent?) {
        if (editPlayerSizeValue.get()) mc.thePlayer.eyeHeight =
            playerSizeValue.get() + 0.62f else mc.thePlayer.eyeHeight = mc.thePlayer.defaultEyeHeight
    }

    override fun onDisable() {
        mc.thePlayer.eyeHeight = mc.thePlayer.defaultEyeHeight
    }
}