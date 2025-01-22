/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal

import net.ccbluex.liquidbounce.FDPClient.moduleManager
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object Main {
    var categoryCount: Int = 0

    var reloadModules: Boolean = false

    @JvmField
    var allowedClickGuiHeight: Float = 300f

    fun getModulesInCategory(category: Category): List<Module> {
        return moduleManager.filter { module -> module.category == category }
    }

}