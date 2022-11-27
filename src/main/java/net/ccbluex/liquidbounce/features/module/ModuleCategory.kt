/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import lombok.Getter
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.gui.clickgui.utils.render.Scroll

enum class ModuleCategory(val displayName: String, val configName: String, val htmlIcon: String) {
    COMBAT("%module.category.combat%", "Combat", "&#xe000;"),
    PLAYER("%module.category.player%", "Player", "&#xe7fd;"),
    MOVEMENT("%module.category.movement%", "Movement", "&#xe566;"),
    RENDER("%module.category.render%", "Render", "&#xe417;"),
    CLIENT("%module.category.client%", "Client", "&#xe869;"),
    WORLD("%module.category.world%", "World", "&#xe55b;"),
    MISC("%module.category.misc%", "Misc", "&#xe5d3;"),
    EXPLOIT("%module.category.exploit%", "Exploit", "&#xe868;");

    var namee: String? = null
    var posX = 0
    var expanded = false

    @Getter
    val scroll: Scroll = Scroll()

    @Getter
    var drag: Drag? = null
    var posY = 20

    open fun ModuleCategory(name: String?) {
        namee = name
        posX = 40 + Main.categoryCount * 120
        drag = Drag(posX.toFloat(), posY.toFloat())
        expanded = true
        Main.categoryCount++
    }

}
