/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

enum class Category(val displayName: String, val configName: String, val htmlIcon: String,  posX: Int, posY: Int, clicked: Boolean, showMods: Boolean) {
    COMBAT("Combat", "Combat", "&#xe000;", 15, 15, false, true),
    PLAYER("Player", "Player", "&#xe7fd;", 15, 180, false, true),
    MOVEMENT("Movement", "Movement", "&#xe566;", 330, 15, false, true),
    VISUAL("Visual", "Visual", "&#xe417;", 225, 15, false, true),
    CLIENT("Client", "Client", "&#xe869;", 15, 330, false, true),
    OTHER("Other", "Other", "&#xe5d3;", 15, 330, false, true),
    EXPLOIT("Exploit", "Exploit", "&#xe868;", 120, 180, false, true);

    /*
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

     */
}