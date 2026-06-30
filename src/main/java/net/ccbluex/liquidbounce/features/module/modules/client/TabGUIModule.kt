/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object TabGUIModule : Module("TabGUI", Category.CLIENT, Category.SubCategory.CLIENT_GENERAL) {
    val tabShowPlayerSkin by boolean("Show Player Heads", true)
        .describe("Show player head icons in the tab list.")
    val tabShowPlayerPing by boolean("Show Ping Numbers", true)
        .describe("Show numeric ping values in the tab list.")
    val hidePingTag by boolean("Show Ping MS Tag", false) { tabShowPlayerPing }
        .describe("Append an ms tag to the ping value.")
    val pingTextShadow by boolean("Ping Text Shadow", true) { tabShowPlayerPing }
        .describe("Render a shadow behind the ping text.")
    val tabMoveSelfToTop by boolean("Move Self To Top", true)
        .describe("Move your own name to the top of the tab list.")
    val tabShowFriends by boolean("Show Friends", true)
        .describe("Show friends in the tab list.")
    val tabShowEnemies by boolean("Show Enemies", true)
        .describe("Show enemies in the tab list.")
    val tabShowHealth by boolean("Show Health", true)
        .describe("Show player health in the tab list.")
    val tabDisableHeader by boolean("Show Header", true)
        .describe("Show the tab list header.")
    val tabDisableFooter by boolean("Show Footer", true)
        .describe("Show the tab list footer.")
    val tabScale by choices("Scale", arrayOf("Small", "Normal", "Large", "Extra Large", "Default"), "Default")
        .describe("Scale of the tab list overlay.")
    val tabSorting by choices(
        "Sorting",
        arrayOf("Vanilla", "Ping", "NameLength", "DisplayNameLength", "Alphabetical", "ReverseAlphabetical"),
        "Vanilla"
    ).describe("Reorder the player list by the chosen criterion.")
    val tabMaxPlayers by int("Max Players", 80, 1..1000)
        .describe("Raise the cap on how many players the tab list can show.")

    var flagRenderTabOverlay = false
        get() = field && tabShowPlayerSkin

    init {
        group(
            "Players",
            "Show Player Heads", "Move Self To Top", "Show Friends", "Show Enemies", "Show Health"
        )
        group("Ping", "Show Ping Numbers", "Show Ping MS Tag", "Ping Text Shadow")
        group("Layout", "Show Header", "Show Footer", "Scale", "Sorting", "Max Players")
    }
}
