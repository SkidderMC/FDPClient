/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold

object CameraView : Module("CameraView", Category.VISUAL, Category.SubCategory.RENDER_SELF) {

    val clip by boolean("Clip", true)
        .describe("Let the camera clip through blocks.")

    private val view by boolean("View", true)
        .describe("Override the camera view position.")

    private val lowerThirdPersonFov by boolean("LowerThirdPersonFOV", false) { view }
        .describe("Lower the FOV while in third person.")
    private val thirdPersonFov by float("ThirdPersonFOV", 50f, 10f..110f) { view && lowerThirdPersonFov }
        .describe("FOV value used in third person.")

    //  val fovValue by float("FOV", 1f, 0f.. 30f)

    private val customY by float("CustomY", 0f, -10f..10f) { view }
        .describe("Vertical offset applied to the camera.")
    private val saveLastGroundY by boolean("SaveLastGroundY", true)  { view }
        .describe("Anchor the camera to the last ground height.")
    private val onScaffold by boolean("OnScaffold", true)  { view }
        .describe("Apply the camera view while scaffolding.")
    private val onF5 by boolean("OnF5", true)  { view }
        .describe("Apply the camera view in third person only.")

    private var launchY: Double? = null

    init {
        group("General", "Clip", "View")
        group("FOV", "LowerThirdPersonFOV", "ThirdPersonFOV")
        group("Anchor", "CustomY", "SaveLastGroundY", "OnScaffold", "OnF5")
    }

    override fun onEnable() {
        if (view) {
            mc.thePlayer?.run { launchY = posY }
        }
    }

    override fun onDisable() {
        launchY = null
    }

    fun shouldLowerThirdPersonFov(): Boolean = view && lowerThirdPersonFov
    fun thirdPersonFovValue(): Float = thirdPersonFov

    val onMotion = handler<MotionEvent> { event ->
        if (!view) return@handler
        if (event.eventState != EventState.POST) return@handler

        mc.thePlayer?.run {
            if (!saveLastGroundY || (onGround || ticksExisted == 1)) {
                launchY = posY
            }
        }
    }

    val onCameraUpdate = handler<CameraPositionEvent> { event ->
        if (!view) return@handler
        mc.thePlayer?.run {
            val currentLaunchY = launchY ?: return@handler
            if (onScaffold && !Scaffold.handleEvents()) return@handler
            if (onF5 && mc.gameSettings.thirdPersonView == 0) return@handler
            event.withY(currentLaunchY + customY)
        }
    }
}