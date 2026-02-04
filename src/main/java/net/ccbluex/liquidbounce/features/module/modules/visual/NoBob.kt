/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category

object NoBob : Module("NoBob", Category.VISUAL, Category.SubCategory.RENDER_SELF, gameDetecting = false) {

    val onMotion = handler<MotionEvent> {
        mc.thePlayer?.distanceWalkedModified = -1f
    }
}
