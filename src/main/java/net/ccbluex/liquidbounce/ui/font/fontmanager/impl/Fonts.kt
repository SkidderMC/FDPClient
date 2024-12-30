/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager.impl

import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontManager
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontRenderer
import net.ccbluex.liquidbounce.ui.font.fontmanager.api.FontType

object Fonts {

    private val fontManager: FontManager = SimpleFontManager.create()

    val FONT_MANAGER: FontManager
        get() = fontManager

    object ICONFONT {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.ICONFONT)
        val ICONFONT_20: FontRenderer = fontFamily.ofSize(20)
    }

    object CheckFont {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.Check)
        val CheckFont_20: FontRenderer = fontFamily.ofSize(20)
    }

    object SF {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.SF)
        val SF_14: FontRenderer = fontFamily.ofSize(14)
        val SF_16: FontRenderer = fontFamily.ofSize(16)
        val SF_18: FontRenderer = fontFamily.ofSize(18)
        val SF_20: FontRenderer = fontFamily.ofSize(20)
    }

    object SFBOLD {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.SFBOLD)
        val SFBOLD_26: FontRenderer = fontFamily.ofSize(26)
        val SFBOLD_18: FontRenderer = fontFamily.ofSize(18)
    }

    object NursultanMedium {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.NursultanMedium)
        val NursultanMedium15: FontRenderer = fontFamily.ofSize(15)
        val NursultanMedium16: FontRenderer = fontFamily.ofSize(16)
        val NursultanMedium18: FontRenderer = fontFamily.ofSize(18)
        val NursultanMedium20: FontRenderer = fontFamily.ofSize(20)
    }

    object InterMedium {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.InterMedium)
        val InterMedium15: FontRenderer = fontFamily.ofSize(15)
    }

    object InterBold {
        private val fontFamily = FONT_MANAGER.fontFamily(FontType.InterBold)
        val InterBold15: FontRenderer = fontFamily.ofSize(15)
        val InterBold20: FontRenderer = fontFamily.ofSize(20)
        val InterBold26: FontRenderer = fontFamily.ofSize(26)
        val InterBold30: FontRenderer = fontFamily.ofSize(30)
    }
}
