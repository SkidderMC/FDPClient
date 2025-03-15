/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Animation
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.Direction
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations.impl.SmoothStepAnimation
import org.lwjgl.input.Mouse

class Scroll {

    var maxScroll = Float.MAX_VALUE
    var minScroll = 0f
    var rawScroll = 0f

    private var scrollAnimation: Animation = SmoothStepAnimation(0, 0.0, Direction.BACKWARDS)

    /**
     * Updates the raw scroll value based on the mouse wheel movement
     * and starts a new scroll animation.
     *
     * @param duration Animation duration in milliseconds
     */
    fun onScroll(duration: Int) {
        // Save the previous scroll value (already compensated by the current animation)
        val oldScroll = scroll

        // Update the raw scroll value using the mouse wheel
        rawScroll += Mouse.getDWheel() / 4f

        // Clamp rawScroll within the range [-maxScroll, minScroll]
        rawScroll = rawScroll.coerceIn(-maxScroll, minScroll)

        // Create an animation covering the offset between the old scroll and the new one
        scrollAnimation = SmoothStepAnimation(
            duration,
            (rawScroll - oldScroll).toDouble(),
            Direction.BACKWARDS
        )
    }

    /**
     * Returns true if the scroll animation is completed.
     */
    val isScrollAnimationDone: Boolean
        get() = scrollAnimation.isDone

    /**
     * The current scroll value adjusted by the animation output.
     * Use this property to draw or retrieve the "real" scroll offset.
     */
    val scroll: Float
        get() = (rawScroll - scrollAnimation.output).toFloat()
}