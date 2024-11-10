/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.value.boolean
import net.ccbluex.liquidbounce.value.float

object CameraView : Module("CameraView", Category.VISUAL, hideModule = false) {

    val clipValue by boolean("CameraClip", true)
    private val customY by float("CustomY", 0f, -10f..10f)
    private val onScaffold by boolean("OnScaffold", false)

  //  val fovValue by float("FOV", 1f, 0f.. 30f)

    private var launchY: Double ?= null
    override fun onEnable() {
        mc.thePlayer?.run {
            launchY = posY
        }
    }
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.POST) return
        mc.thePlayer?.run {
            if (onGround || ticksExisted == 1) {
                launchY = posY
            }
        }
    }
    @EventTarget
    fun onCameraUpdate(event: CameraPositionEvent) {
        mc.thePlayer?.run {
            val currentLaunchY = launchY ?: return
            if (onScaffold && !Scaffold.handleEvents()) return
            event.withY(currentLaunchY + customY)
        }
    }
}