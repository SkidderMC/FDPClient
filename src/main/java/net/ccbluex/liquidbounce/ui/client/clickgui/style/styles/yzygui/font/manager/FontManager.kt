/*
 * FDPClient Hacked Client
 * A free open-source hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.manager

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.minecraft.util.ResourceLocation
import java.io.File

/**
 *  @author opZywl
 *
 */
class FontManager {

    companion object {
        private val warnedMissing = mutableSetOf<String>()
    }

    private val registry: MutableMap<String, FontRenderer> = HashMap()

    fun getFontResource(name: String): ResourceLocation {
        val localFile = File(FileManager.fontsDir, "$name.ttf")
        if (!localFile.exists() && warnedMissing.add(name)) {
            LOGGER.warn("TTF file not found: ${localFile.absolutePath}")
        }

        return ResourceLocation("fdpclient", "fonts/$name.ttf")
    }

    /**
     * Registers a font by constructing a [FontRenderer]
     * from the given [ResourceLocation] and [size].
     */
    private fun register(name: String, location: ResourceLocation, size: Int) {
        val fontRenderer = FontRenderer(location, size.toFloat())
        registry[name] = fontRenderer
    }

    /**
     * Example registrations: "lato-bold" at multiple sizes.
     */
    fun register() {
        register("lato-bold-13", getFontResource("lato-bold"), 13)
        register("lato-bold-15", getFontResource("lato-bold"), 15)
        register("lato-bold-17", getFontResource("lato-bold"), 17)
        register("lato-bold-30", getFontResource("lato-bold"), 30)
        register("lato-bold-64", getFontResource("lato-bold"), 64)
    }

    /**
     * Retrieves a previously registered [FontRenderer] by the given key,
     * e.g. "lato-bold-15".
     */
    operator fun get(name: String): FontRenderer? {
        return registry[name]
    }
}
