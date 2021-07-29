/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module

enum class ModuleCategory(val displayName: String, val configName: String) {
    COMBAT("%module.category.combat%", "Combat"),
    PLAYER("%module.category.player%", "Player"),
    MOVEMENT("%module.category.movement%", "Movement"),
    RENDER("%module.category.render%", "Render"),
    CLIENT("%module.category.client%", "Client"),
    WORLD("%module.category.world%", "World"),
    MISC("%module.category.misc%", "Misc"),
    EXPLOIT("%module.category.exploit%", "Exploit")
}