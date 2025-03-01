/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.Main
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.objects.Drag
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.render.Scroll
import net.minecraft.util.ResourceLocation

enum class Category(val displayName: String, val configName: String, val htmlIcon: String, initialPosX: Int, initialPosY: Int, val clicked: Boolean = false, val showMods: Boolean = true) {
    COMBAT("Combat", "Combat", "&#xe000;", 15, 15),
    PLAYER("Player", "Player", "&#xe7fd;", 15, 180),
    MOVEMENT("Movement", "Movement", "&#xe566;", 330, 15),
    VISUAL("Visual", "Visual", "&#xe417;", 225, 15),
    CLIENT("Client", "Client", "&#xe869;", 15, 330),
    OTHER("Other", "Other", "&#xe5d3;", 15, 330),
    EXPLOIT("Exploit", "Exploit", "&#xe868;", 120, 180);

    var posX: Int = 40 + (Main.categoryCount * 120)
    var posY: Int = initialPosY

    val scroll = Scroll()
    val drag = Drag(posX.toFloat(), posY.toFloat())

    init {
        Main.categoryCount++
    }

    val iconResourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/category/${name.lowercase()}.png")
}
