/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.modules.client.TargetModule
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.entity.Entity

object EntityVisibilityFilter : MinecraftInstance {

    @JvmStatic
    fun shouldHideFromRender(entity: Entity?): Boolean {
        if (entity == null || entity == mc.thePlayer) {
            return false
        }

        if (TargetModule.isManagingVisibility()) {
            return TargetModule.shouldHideEntity(entity)
        }

        return HideClans.shouldHideEntity(entity)
    }

    @JvmStatic
    fun shouldSkipMouseOver(entity: Entity?): Boolean {
        return shouldHideFromRender(entity)
    }

    @JvmStatic
    fun shouldForceRender(entity: Entity?): Boolean {
        return TargetModule.shouldForceRender(entity)
    }
}
