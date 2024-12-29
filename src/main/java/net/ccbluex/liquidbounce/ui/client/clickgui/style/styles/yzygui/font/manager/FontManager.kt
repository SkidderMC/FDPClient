/*
 * FDPClient Hacked Client
 * A free open-source hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.manager

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.yzygui.font.renderer.FontRenderer
import net.minecraft.util.ResourceLocation
import java.io.File

/**
 *  @author opZywl
 *
 */
class FontManager {

    private val registry: MutableMap<String, FontRenderer> = HashMap()

    fun getFontResource(name: String): ResourceLocation {
        // Ensure the TTF file physically exists in fontsDir/lato-bold.ttf, etc.
        // You might create or verify the file here if needed.
        val localFile = File(FileManager.fontsDir, "$name.ttf")
        if (!localFile.exists()) {
            // You could log a warning or handle the missing file as needed:
            println("Warning: TTF file not found: ${localFile.absolutePath}")
        }

        // Return a ResourceLocation with a chosen domain + path.
        // The domain can be anything not conflicting with existing MC domains.
        // The path is "fonts/<name>.ttf" to match what your FontRenderer might look for.
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
        register("lato-bold-15", getFontResource("lato-bold"), 15)
        register("lato-bold-17", getFontResource("lato-bold"), 17)
        register("lato-bold-13", getFontResource("lato-bold"), 13)

        // The duplicates from your snippet; you can remove if you prefer:
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
