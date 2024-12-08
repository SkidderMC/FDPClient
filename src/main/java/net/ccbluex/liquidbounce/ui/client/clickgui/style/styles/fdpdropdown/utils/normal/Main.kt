/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleManager

object Main {
    var categoryCount: Int = 0

    var reloadModules: Boolean = false

    @JvmField
    var allowedClickGuiHeight: Float = 300f

    fun getModulesInCategory(category: Category, moduleManager: ModuleManager): List<Module> {
        return moduleManager.modules
            .filter { module -> module.category == category }
    }
}