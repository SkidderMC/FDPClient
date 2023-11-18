package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.utils.LocationCache
import kotlin.random.Random

@ModuleInfo(name = "AimAssist", category = ModuleCategory.COMBAT)
class AimAssist : Module() {

    private val rangeValue = FloatValue("Range", 4.4F, 1F, 8F)
    private val playerPredictValue = FloatValue("PlayerPredictAmount", 1.2f, -2f, 3f)
    private val opPredictValue = FloatValue("TargetPredictAmount", 1.5f, -2f, 3f)
    private val centerSpeed = FloatValue("CenterSpeed", 10F, 1F, 100F)
    private val centerRandom = FloatValue("CenterRandomRange", 1.0F, 0F, 15F)
    private val edgeSpeed = FloatValue("EdgeSpeed", 20F, 1F, 100F)
    private val edgeRandom = FloatValue("EdgeRandomRange", 1.0F, 0F, 15F)
    private val onTargetSlowdown = BoolValue("OnTargetSlowdown", true)
    private val slowdownAmount = FloatValue("SlowDownAmount", 0.5f, 0.1f, 0.7f).displayable { onTargetSlowdown.get() }
    private val fovValue = FloatValue("FOV", 180F, 1F, 180F)
    private val onClickValue = BoolValue("OnClick", false)
    private val onClickDurationValue = IntegerValue("OnClickDuration", 500, 100, 1000).displayable { onClickValue.get() }
    private val jitterValue = BoolValue("Jitter", false)
    private val randomJitterValue = FloatValue("JitterRandomRate", 1.0F, 0F, 5.0F).displayable { jitterValue.get() }

    private val clickTimer = MSTimer()

    private var oldMouse = Rotation(0f, 0f)
    private var newMouse = Rotation(0f, 0f)
    
    private var playerRot = Rotation(0f, 0f)
    private var targetRot = Rotation(0f, 0f)
    
    private var onTarget = false
    private var mouseSpeed = 0f
    private var rotDiff = 0f
                                            

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.gameSettings.keyBindAttack.isKeyDown) {
            clickTimer.reset()
        }

        oldMouse = newMouse
        newMouse = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

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

        entity.entityBoundingBox.offset((entity.posX - entity.lastTickPosX) * opPredictValue.get(),
                                        (entity.posY - entity.lastTickPosY) * opPredictValue.get(),
                                        (entity.posZ - entity.lastTickPosZ) * opPredictValue.get())
        entity.entityBoundingBox.offset(mc.thePlayer.motionX * -1f * playerPredictValue.get(),
                                        mc.thePlayer.motionY * -1f * playerPredictValue.get(),
                                        mc.thePlayer.motionX * -1f * playerPredictValue.get())

        
        mouseSpeed = RotationUtils.getRotationDifference(oldMouse,  newMouse).toFloat()
        onTarget = RotationUtils.isFaced(entity, range.toDouble())
        
        // on target slowdown
        if (onTarget) {
            val rotationSlowdown = RotationUtils.limitAngleChange(
                newMouse, oldMouse, mouseSpeed * slowdownAmount.get()
            )
            rotationSlowdown.toPlayer(mc.thePlayer)
        }
        
        targetRot = RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true)
        if (RotationUtils.getRotationDifference(oldMouse,  targetRot).toFloat()
            < RotationUtils.getRotationDifference(newMouse,  targetRot).toFloat() ) {
            return
        }
        
        
        // center rotation
        playerRot = Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        targetRot = RotationUtils.toRotation(RotationUtils.getCenter(entity.entityBoundingBox), true)
        rotDiff = RotationUtils.getRotationDifference(playerRot,  targetRot).toFloat()

        val rotationCenter = RotationUtils.limitAngleChange(
            playerRot, targetRot,
            (mouseSpeed / rotDiff) * mouseSpeed * (centerSpeed.get() + (centerRandom.get() * Math.random() * 0.5f)).toFloat() * 0.1f
        )

        rotationCenter.toPlayer(mc.thePlayer)

        //edge rotation
        if (!onTarget) {
            targetRot = RotationUtils.searchCenter(entity.entityBoundingBox, false, false, true, false).rotation
            rotDiff = RotationUtils.getRotationDifference(playerRot,  targetRot).toFloat()

            val rotationEdge = RotationUtils.limitAngleChange(
                playerRot, targetRot,
                (mouseSpeed / rotDiff) * mouseSpeed * (edgeSpeed.get() + (edgeRandom.get() * Math.random() * 0.5f)).toFloat() * 0.1f
            )

            rotationEdge.toPlayer(mc.thePlayer)
        }

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
