/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.ghost
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
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.utils.extensions.hitBox

@ModuleInfo(name = "SilentHitbox", category = ModuleCategory.GHOST)
class SilentHitbox : Module() {

    private val rangeValue = FloatValue("Range", 4.4F, 1F, 8F)
    private val fovValue = FloatValue("Expand", 10F, 1F, 50F)
    private val onClickValue = BoolValue("OnClick", false)
    private val onClickDurationValue = IntegerValue("OnClickDuration", 500, 100, 1000).displayable { onClickValue.get() }

    private val clickTimer = MSTimer()

    private var playerRot = Rotation(0f, 0f)
    private var targetRot = Rotation(0f, 0f)

    private var dontReset = false
    private var enabled = false

    override fun onDisable() {
        enabled = false
        FDPClient.moduleManager[FreeLook::class.java]!!.disable()
    }

    override fun onEnable() {
        enabled = false
        FDPClient.moduleManager[FreeLook::class.java]!!.enable()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!enabled) {
            mc.thePlayer.rotationYaw = FreeLook.cameraYaw
            mc.thePlayer.rotationPitch = FreeLook.cameraPitch
        }

        if (!FreeLook.isEnabled) {
            FDPClient.moduleManager[FreeLook::class.java]!!.enable()
            // prevent noncompatability with legit scaffold or aura
        }
        if (mc.gameSettings.keyBindAttack.isKeyDown) {
            clickTimer.reset()
        }

        if (onClickValue.get() && clickTimer.hasTimePassed(onClickDurationValue.get().toLong())) {
            enabled = false
            return
        }

        if (dontReset) dontReset = false
        else if (enabled) {
            enabled = false
        }

        var oldYaw = mc.thePlayer.rotationYaw
        var oldPitch = mc.thePlayer.rotationPitch

        mc.thePlayer.rotationYaw = FreeLook.cameraYaw
        mc.thePlayer.rotationPitch = FreeLook.cameraPitch

        val range = rangeValue.get()
        val entity = mc.theWorld.loadedEntityList
            .filter {
                EntityUtils.isSelected(it, true) && mc.thePlayer.canEntityBeSeen(it) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= range && RotationUtils.getRotationDifference(it) <= fovValue.get()
            }
            .minByOrNull { RotationUtils.getRotationDifference(it) } ?: return

        val fovDist = RotationUtils.getRotationDifference(entity)

        mc.thePlayer.rotationYaw = oldYaw
        mc.thePlayer.rotationPitch = oldPitch

        dontReset = true

        if (!enabled) {
            enabled = true
        }

        if (fovDist > 2) {
          targetRot = (RotationUtils.calculateCenter(
              "LiquidBounce",
              "Horizontal",
              0.1,
              entity.hitBox,
              true,
              false)
                  )!!.rotation
        } else {
          targetRot = (RotationUtils.calculateCenter(
              "Optimal",
              "Cubic",
              0.1,
              entity.hitBox,
              true,
              false)
                  )!!.rotation
        }

        playerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

        val rotationCenter = RotationUtils.limitAngleChange(
            playerRot, targetRot,
            10f + (Math.random().toFloat() * 4f)
        )

        rotationCenter.toPlayer(mc.thePlayer)
    }

}
