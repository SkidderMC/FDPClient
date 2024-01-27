/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeLook
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import kotlin.random.Random

@ModuleInfo(name = "SilentHitbox", category = ModuleCategory.COMBAT)
class SilentHitbox : Module() {

    private val rangeValue = FloatValue("Range", 4.4F, 1F, 8F)
    private val fovValue = FloatValue("Expand", 10F, 1F, 50F)
    private val onClickValue = BoolValue("OnClick", false)
    private val onClickDurationValue = IntegerValue("OnClickDuration", 500, 100, 1000).displayable { onClickValue.get() }

    private val clickTimer = MSTimer()
    
    private var playerRot = Rotation(0f, 0f)
    private var targetRot = Rotation(0f, 0f)

    val freeLook = FDPClient.moduleManager[FreeLook::class.java]!!
                                            

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.gameSettings.keyBindAttack.isKeyDown) {
            clickTimer.reset()
        }

        if (onClickValue.get() && clickTimer.hasTimePassed(onClickDurationValue.get().toLong())) {
            resetCamera()
        }

        val range = rangeValue.get()

        if (RotationUtils.isFaced(entity, range.toDouble())) resetCamera()

        val entity = mc.theWorld.loadedEntityList
            .filter {
                EntityUtils.isSelected(it, true) && mc.thePlayer.canEntityBeSeen(it) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= range && RotationUtils.getRotationDifference(it) <= fovValue.get()
            }
            .minByOrNull { RotationUtils.getRotationDifference(it) } ?: resetCamera()

        targetRot = (RotationUtils.calculateCenter(
            "LiquidBounce",
            "Horizontal",
            0.1,
            entity.hitBox,
            false,
            true)
        ).rotation

        playerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        val rotationCenter = RotationUtils.limitAngleChange(
            playerRot, targetRot,
            3f + (Math.random().toFloat() * 0.5f)
        )

        rotationCenter.toPlayer(mc.thePlayer)
    }

    private fun resetCamera() {
        freeLook.isEnabled = false
        mc.thePlayer.rotationYaw = freeLook.cameraYaw
        mc.thePlayer.rotationPitch = freeLook.cameraPitch
        freeLook.resetPerspective()
    }

    private fun startCamera () {
        freeLook.isEnabled = true
        freeLook.isReverse = false
        freeLook.setRotations()
    }
}
