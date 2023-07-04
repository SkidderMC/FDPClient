/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.*
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import kotlin.random.Random

@ModuleInfo(name = "Aimbot", category = ModuleCategory.COMBAT)
object Aimbot : Module() {

    private val rangeValue = FloatValue("Range", 4.4F, 1F, 8F)
    private val turnSpeedValue = FloatValue("TurnSpeed", 2F, 1F, 180F)
    private val randomTurnValue = FloatValue("TurnSpeedRandomRate", 1.0F, 0F, 15F)
    private val smoothValue = BoolValue("Smooth", false)
    private val smoothAngleValue = IntegerValue("SmoothMinAngle", 30, 1, 180).displayable { smoothValue.get() }
    private val fovValue = FloatValue("FOV", 180F, 1F, 180F)
    private val rotMode = ListValue("RotationMode", arrayOf("LiquidBounce", "Full", "HalfUp", "HalfDown", "CenterSimple", "CenterLine"), "HalfUp")
    private val lockValue = BoolValue("Lock", true)
    private val onClickValue = BoolValue("OnClick", false)
    private val onClickDurationValue = IntegerValue("OnClickDuration", 500, 100, 1000).displayable { onClickValue.get() }
    private val jitterValue = BoolValue("Jitter", false)
    private val randomJitterValue = FloatValue("JitterRandomRate", 1.0F, 0F, 5.0F).displayable { jitterValue.get() }

    private val clickTimer = MSTimer()

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mc.gameSettings.keyBindAttack.isKeyDown) {
            clickTimer.reset()
        }

        if (onClickValue.get() && clickTimer.hasTimePassed(onClickDurationValue.get().toLong())) {
            return
        }

        val range = rangeValue.get()
        val entity = mc.theWorld.loadedEntityList
            .filter {
                EntityUtils.isSelected(it, true) && mc.thePlayer.canEntityBeSeen(it) &&
                        mc.thePlayer.getDistanceToEntityBox(it) <= range && RotationUtils.getRotationDifference(it) <= fovValue.get()
            }
            .minByOrNull { RotationUtils.getRotationDifference(it) } ?: return

        if (!lockValue.get() && RotationUtils.isFaced(entity, range.toDouble())) {
            return
        }
        
        val calcBaseSpeed = turnSpeedValue.get() + Math.random() * randomTurnValue.get() - Math.random() * randomTurnValue.get()
        val angleDiff = RotationUtils.getRotationDifference(entity)
        val calcPrecent = if (angleDiff >= smoothAngleValue.get() || !smoothValue.get()) { 1.0 } else { angleDiff / smoothAngleValue.get() }
        
        val rotation = RotationUtils.limitAngleChange(
            Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch),
            (RotationUtils.calculateCenter(
                        rotMode.get(),
                        "Horizontal",
                        0.1,
                        entity.hitBox,
                        false,
                        true)
            ).rotation,
            (calcBaseSpeed * calcPrecent).toFloat()
        )

        rotation.toPlayer(mc.thePlayer)

        if (jitterValue.get()) {
            val yaw = Random.nextBoolean()
            val pitch = Random.nextBoolean()
            val yawNegative = Random.nextBoolean()
            val pitchNegative = Random.nextBoolean()

            if (yaw) {
                mc.thePlayer.rotationYaw += if (yawNegative) -RandomUtils.nextFloat(0F, randomJitterValue.get()) else RandomUtils.nextFloat(0F, randomJitterValue.get())
            }

            if (pitch) {
                mc.thePlayer.rotationPitch += if (pitchNegative) -RandomUtils.nextFloat(0F, randomJitterValue.get()) else RandomUtils.nextFloat(0F, randomJitterValue.get())
                if (mc.thePlayer.rotationPitch > 90) {
                    mc.thePlayer.rotationPitch = 90F
                } else if (mc.thePlayer.rotationPitch < -90) {
                    mc.thePlayer.rotationPitch = -90F
                }
            }
        }
    }
}
