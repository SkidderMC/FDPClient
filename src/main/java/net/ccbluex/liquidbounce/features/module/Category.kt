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

enum class Category(
    val displayName: String,
    val configName: String,
    val htmlIcon: String,
    initialPosX: Int,
    initialPosY: Int,
    val clicked: Boolean = false,
    val showMods: Boolean = true,
    val subCategories: Array<SubCategory>
) {
    COMBAT("Combat", "Combat", "&#xe000;", 15, 15, subCategories = arrayOf(SubCategory.COMBAT_RAGE, SubCategory.COMBAT_LEGIT)),
    PLAYER("Player", "Player", "&#xe7fd;", 15, 180, subCategories = arrayOf(SubCategory.PLAYER_COUNTER, SubCategory.PLAYER_ASSIST)),
    MOVEMENT("Movement", "Movement", "&#xe566;", 330, 15, subCategories = arrayOf(SubCategory.MOVEMENT_MAIN, SubCategory.MOVEMENT_EXTRAS)),
    VISUAL("Visual", "Visual", "&#xe417;", 225, 15, subCategories = arrayOf(SubCategory.RENDER_SELF, SubCategory.RENDER_OVERLAY)),
    CLIENT("Client", "Client", "&#xe869;", 15, 330, subCategories = arrayOf(SubCategory.CLIENT_GENERAL, SubCategory.CONFIGS)),
    OTHER("Other", "Other", "&#xe5d3;", 15, 330, subCategories = arrayOf(SubCategory.MISCELLANEOUS)),
    EXPLOIT("Exploit", "Exploit", "&#xe868;", 120, 180, subCategories = arrayOf(SubCategory.EXPLOIT_EXTRAS));

    var posX: Int = 40 + (Main.categoryCount * 120)
    var posY: Int = initialPosY

    val scroll = Scroll()
    val drag = Drag(posX.toFloat(), posY.toFloat())

    init {
        Main.categoryCount++
    }

    val iconResourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/texture/category/${name.lowercase()}.png")

    enum class SubCategory(val displayName: String, val icon: String) {
        // Combat
        COMBAT_RAGE("Rage", "a"),
        COMBAT_LEGIT("Legit", "e"),

        // Movement
        MOVEMENT_MAIN("Main", "g"),
        MOVEMENT_EXTRAS("Extras", "f"),

        // Visual
        RENDER_SELF("Self", "m"),
        RENDER_OVERLAY("Overlay", "h"),

        // Player
        PLAYER_COUNTER("Counterattack", "n"),
        PLAYER_ASSIST("Assist", "l"),

        // Client / Configs
        CLIENT_GENERAL("Client", "h"),
        CONFIGS("Configs", "x"),

        // Other
        MISCELLANEOUS("Miscellaneous", "\ue5d3"),

        // Exploit
        EXPLOIT_EXTRAS("Extras", "j"),

        // Fallback
        GENERAL("General", "h");

        override fun toString() = displayName
    }
}