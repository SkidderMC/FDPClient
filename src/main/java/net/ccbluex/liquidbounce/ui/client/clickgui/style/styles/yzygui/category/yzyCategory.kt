/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.category

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.features.module.Category
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*

/**
 * @author opZywl - Category
 */
enum class yzyCategory(val parent: Category, val displayName: String, val color: Color) {
    COMBAT(Category.COMBAT, "Combat", Color(-0x19b2c6)),
    PLAYER(Category.PLAYER, "Player", Color(-0x71ba52)),
    MOVEMENT(Category.MOVEMENT, "Movement", Color(-0xd13291)),
    VISUAL(Category.VISUAL, "Visual", Color(-0xc9fe32)),
    CLIENT(Category.CLIENT, "Client", Color(0xCBFF02)),
    OTHER(Category.OTHER, "Other", Color(0xFFC200)),
    EXPLOIT(Category.EXPLOIT, "Exploit", Color(-0xcc6727));

    fun getIcon(): ResourceLocation {
        return ResourceLocation("${CLIENT_NAME.lowercase()}/texture/clickgui/${displayName.lowercase(Locale.getDefault())}.png")
    }

    companion object {
        fun of(category: Category): yzyCategory? {
            return entries.find { it.parent == category }
        }
    }
}