/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import lombok.Getter
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.clickgui.style.styles.fdpdropdown.utils.render.Scroll
enum class ModuleCategory(val displayName: String, val configName: String, val htmlIcon: String,  posX: Int, posY: Int, clicked: Boolean, showMods: Boolean) {
    COMBAT("%module.category.combat%", "Combat", "&#xe000;", 15, 15, false, true),
    PLAYER("%module.category.player%", "Player", "&#xe7fd;", 15, 180, false, true),
    MOVEMENT("%module.category.movement%", "Movement", "&#xe566;", 330, 15, false, true),
    VISUAL("%module.category.visual%", "Visual", "&#xe417;", 225, 15, false, true),
    CLIENT("%module.category.client%", "Client", "&#xe869;", 15, 330, false, true),
    OTHER("%module.category.other%", "Other", "&#xe5d3;", 15, 330, false, true),
    GHOST("%module.category.ghost%", "Ghost", "&#xe821;", 120, 180, false, true),
    EXPLOIT("%module.category.exploit%", "Exploit", "&#xe868;", 120, 180, false, true);

    private var expanded: Boolean
    private var posXs: Int
    private var posYs: Int
    private var clickeds: Boolean
    private var showModsV: Boolean

    @Getter
     val scroll = Scroll()

    @Getter
    val drag: Drag
    var posY: Int = 20

    init {
        var posX = posX
        posX = 40 + (Main.categoryCount * 120)
        drag = Drag(posX.toFloat(), posY.toFloat())
        expanded = true
        posXs = posX
        posYs = posY
        clickeds = clicked
        showModsV = showMods
        Main.categoryCount++
    }
}