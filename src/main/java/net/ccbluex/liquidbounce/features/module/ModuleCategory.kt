/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

//import lombok getter fix!
import jdk.nashorn.internal.objects.annotations.Getter
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.normal.Main
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.objects.Drag
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.utils.render.Scroll

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


    //Getter lombok import
    @get:Getter //remove when add lombok
    val scroll = Scroll()

    //Getter lombok import
    @get:Getter //remove when add lombok
    var drag: Drag? = null
    var posY = 20

    fun ModuleCategory(name: String?) {
        namee = name
        posX = 40 + Main.categoryCount * 120
        drag = Drag(posX.toFloat(), posY.toFloat())
        expanded = true
        Main.categoryCount++
    }
}
