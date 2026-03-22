/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.ncp

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.SpeedMode
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.movement.MovementUtils.defaultSpeed
import kotlin.math.sqrt

object SNCPBHop : SpeedMode("SNCPBHop") {
    private var level = 1
    private var moveSpeed = 0.2873
    private var lastDist = 0.0
    private var timerDelay = 0
    override fun onEnable() {
        mc.timer.timerSpeed = 1f
        lastDist = 0.0
        moveSpeed = 0.0
        level = 4
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        moveSpeed = defaultSpeed()
        level = 0
    }

    override fun onMotion() {
        val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
        val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
        lastDist = sqrt(xDist * xDist + zDist * zDist)
    }


    // TODO: Recode this mess
    override fun onMove(event: MoveEvent) {
        val defaultSpeed = defaultSpeed()
        timerDelay = updateBHopTimer(timerDelay)
        if (mc.thePlayer.onGround && mc.thePlayer.isMoving) level = 2
        if (roundedBHopValue(mc.thePlayer.posY - mc.thePlayer.posY.toInt().toDouble()) == roundedBHopValue(0.138)) {
            mc.thePlayer.motionY -= 0.08
            event.y -= 0.09316090325960147
            mc.thePlayer.posY -= 0.09316090325960147
        }
        if (level == 1 && mc.thePlayer.isMoving) {
            level = 2
            moveSpeed = 1.35 * defaultSpeed - 0.01
        } else if (level == 2) {
            level = 3
            mc.thePlayer.motionY = 0.399399995803833
            event.y = 0.399399995803833
            moveSpeed *= 2.149
        } else if (level == 3) {
            level = 4
            val difference = 0.66 * (lastDist - defaultSpeed)
            moveSpeed = lastDist - difference
        } else if (level == 88) {
            moveSpeed = defaultSpeed
            lastDist = 0.0
            level = 89
        } else if (level == 89) {
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)).isNotEmpty() || mc.thePlayer.isCollidedVertically) level = 1
            lastDist = 0.0
            moveSpeed = defaultSpeed
            return
        } else {
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)).isNotEmpty() || mc.thePlayer.isCollidedVertically) {
                moveSpeed = defaultSpeed
                lastDist = 0.0
                level = 88
                return
            }
            moveSpeed = lastDist - lastDist / 159.0
        }
        moveSpeed = moveSpeed.coerceAtLeast(defaultSpeed)

        val movementInput = mc.thePlayer.movementInput
        applyBHopDirection(event, moveSpeed, movementInput.moveForward, movementInput.moveStrafe, mc.thePlayer.rotationYaw)
        mc.thePlayer.stepHeight = 0.6f

        if (!mc.thePlayer.isMoving) event.zeroXZ()
    }
}
