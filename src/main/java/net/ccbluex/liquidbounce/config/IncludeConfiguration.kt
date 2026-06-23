/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.config

/** Selects local-only value categories when a configurable tree is serialized. */
data class IncludeConfiguration(
    val subjectiveValues: Boolean,
    val hiddenValues: Boolean,
    val keyBindings: Boolean,
    val filePaths: Boolean
) {
    companion object {
        /** Preserves the historical local-file behavior. */
        @JvmField
        val LOCAL = IncludeConfiguration(
            subjectiveValues = true,
            hiddenValues = true,
            keyBindings = true,
            filePaths = true
        )

        /** Safe default for user-shared configuration output. */
        @JvmField
        val SHARED = IncludeConfiguration(
            subjectiveValues = false,
            hiddenValues = false,
            keyBindings = false,
            filePaths = false
        )
    }
}
