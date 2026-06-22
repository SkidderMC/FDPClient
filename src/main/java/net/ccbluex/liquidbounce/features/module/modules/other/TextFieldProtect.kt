/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object TextFieldProtect : Module("TextFieldProtect", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val patterns by text("Patterns", "/register,/login,/email")
        .describe("Comma-separated command prefixes to mask.")
    private val ignoreCase by boolean("IgnoreCase", true)
        .describe("Match the patterns case-insensitively.")

    private const val MASK_CHAR = '*'

    private val patternList: List<String>
        get() = patterns.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun matches(input: String): Boolean = patternList.any { input.startsWith(it, ignoreCase) }

    fun protect(input: String, firstCharacterIndex: Int): String {
        return if (!handleEvents() || !matches(input)) {
            input
        } else {
            MASK_CHAR.toString().repeat(firstCharacterIndex.coerceAtLeast(0))
        }
    }
}
