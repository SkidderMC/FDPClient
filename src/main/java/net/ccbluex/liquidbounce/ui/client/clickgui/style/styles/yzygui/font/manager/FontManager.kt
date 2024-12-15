/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.manager

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.minecraft.util.ResourceLocation

/**
 * @author opZywl - FontManager
 */
class FontManager {
    private val registry: MutableMap<String, FontRenderer> = HashMap()

    fun getFontResource(name: String): ResourceLocation {
        return ResourceLocation("${CLIENT_NAME.lowercase()}/clickgui/zywl/fonts/$name.ttf")
    }

    private fun register(name: String, location: ResourceLocation, size: Int) {
        val fontRenderer = FontRenderer(location, size.toFloat())

        registry[name] = fontRenderer
    }

    fun register() {
        this.register(
            "lato-bold-15",
            this.getFontResource("lato-bold"),
            15
        )
        this.register(
            "lato-bold-17",
            this.getFontResource("lato-bold"),
            17
        )
        this.register(
            "lato-bold-13",
            this.getFontResource("lato-bold"),
            13
        )

        this.register(
            "lato-bold-13",
            this.getFontResource("lato-bold"),
            13
        )
        this.register(
            "lato-bold-15",
            this.getFontResource("lato-bold"),
            15
        )
        this.register(
            "lato-bold-17",
            this.getFontResource("lato-bold"),
            17
        )
        this.register(
            "lato-bold-30",
            this.getFontResource("lato-bold"),
            30
        )
        this.register(
            "lato-bold-64",
            this.getFontResource("lato-bold"),
            64
        )
    }

    operator fun get(name: String): FontRenderer? {
        return registry[name]
    }
}