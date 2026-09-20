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
        arrayOf(
            "Vanilla", "Ping", "NameLength", "DisplayNameLength",
            "Alphabetical", "ReverseAlphabetical", "None"
        ),
        "Vanilla"
    ).describe("Reorder the player list by the chosen criterion.")
    val tabMaxPlayers by int("Max Players", 80, 1..1000)
        .describe("Maximum number of players the tab list can show.")
    val tabColumnHeight by int("Column Height", 20, 1..100)
        .describe("Maximum player rows per column before another column is created.")
    val tabShowGameMode by boolean("Show Game Mode", true)
        .describe("Append each player's current game mode to their tab name.")
    val tabHidePlayers by boolean("Hide Players", false)
        .describe("Hide tab entries whose selected name matches the configured regular expression.")
    val tabHideFilterBy by choices(
        "Hide Filter By", arrayOf("PlayerName", "DisplayName"), "PlayerName"
    ) { tabHidePlayers }.describe("Choose which tab name representation is tested by the hide filter.")

    private var hidePattern = Regex("(?!)")
    val tabHideNameRegex by text("Hide Name Regex", "") { tabHidePlayers }.onChange { old, new ->
        runCatching { Regex(new, RegexOption.IGNORE_CASE) }
            .onSuccess { hidePattern = it }
            .fold(onSuccess = { new }, onFailure = { old })
    }.describe("Regular expression used by Hide Players. Invalid expressions are rejected.")

    var flagRenderTabOverlay = false
        get() = field && tabShowPlayerSkin

    init {
        group(
            "Players",
            "Show Player Heads", "Move Self To Top", "Show Friends", "Show Enemies", "Show Health"
        )
        group("Ping", "Show Ping Numbers", "Show Ping MS Tag", "Ping Text Shadow")
        group(
            "Layout",
            "Show Header", "Show Footer", "Scale", "Sorting", "Max Players", "Column Height", "Show Game Mode"
        )
        group("Player Hider", "Hide Players", "Hide Filter By", "Hide Name Regex")
    }

    fun shouldHidePlayer(playerName: String, displayName: String): Boolean {
        if (!tabHidePlayers || tabHideNameRegex.isBlank()) return false
        val candidate = if (tabHideFilterBy == "DisplayName") displayName else playerName
        return hidePattern.containsMatchIn(candidate)
    }

    override fun onDisable() {
        flagRenderTabOverlay = false
    }
}
