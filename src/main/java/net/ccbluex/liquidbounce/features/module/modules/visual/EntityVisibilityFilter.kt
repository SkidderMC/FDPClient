/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.modules.client.TargetFocus
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.entity.Entity

object EntityVisibilityFilter : MinecraftInstance {

    @JvmStatic
    fun shouldHideFromRender(entity: Entity?): Boolean {
        if (entity == null || entity == mc.thePlayer) {
            return false
        }

        if (TargetFocus.shouldForceRender(entity)) {
            return false
        }

        if (HideClans.shouldHideEntity(entity)) {
            return true
        }

        if (TargetFocus.isManagingVisibility()) {
            return TargetFocus.shouldHideEntity(entity)
        }

        return false
    }

    @JvmStatic
    fun shouldSkipMouseOver(entity: Entity?): Boolean {
        return shouldHideFromRender(entity)
    }

    @JvmStatic
    fun shouldForceRender(entity: Entity?): Boolean {
        return TargetFocus.shouldForceRender(entity)
    }
}
