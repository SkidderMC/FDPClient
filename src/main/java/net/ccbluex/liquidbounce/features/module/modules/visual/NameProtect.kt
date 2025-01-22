/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FDPClient.CLIENT_NAME
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.utils.render.ColorUtils.translateAlternateColorCodes
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S40PacketDisconnect
import java.util.*

object NameProtect : Module("NameProtect", Category.VISUAL, subjective = true, gameDetecting = false) {

    val allPlayers by boolean("AllPlayers", false)

    val skinProtect by boolean("SkinProtect", true)
    private val fakeName by text("FakeName", "&cMe")

    private val randomNames by boolean("RandomNames", false) { allPlayers }
    private val randomNameLength by boolean("RandomNameLength", false) { allPlayers && randomNames }

    private var nameLength by int("NameLength", 6, 6..16) {
        randomNames && allPlayers && !randomNameLength
    }

    private val nameLengthRange by intRange("NameLengthRange", 6..14, 6..16)
    { allPlayers && randomNames && randomNameLength }

    private val playerRandomNames = mutableMapOf<UUID, Pair<String, Int>>()
    private val characters = ('a'..'z') + ('0'..'9') + ('A'..'Z') + "_"

    private var savedName = -1
    private var savedLength: IntRange? = null

    override fun onEnable() {
        if (!allPlayers) {
            return
        }

        generateRandomNames()

        // Saving other player changed name length
        if (randomNames) {
            savedName = nameLength
        }

        // Saving other player random changed name length
        if (randomNameLength) {
            savedLength = nameLengthRange
        }
    }

    override fun onDisable() {
        playerRandomNames.clear()
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (mc.thePlayer == null || mc.theWorld == null) return@handler

        // Check for new players
        if (packet is S01PacketJoinGame) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                handleNewPlayer(playerInfo.gameProfile.id)
            }
        }

        // Check if player in game leave
        if (packet is S40PacketDisconnect) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                handlePlayerLeave(playerInfo.gameProfile.id)
            }
        }
    }

    /**
     * Generate random names for players
     */
    private fun generateRandomNames() {
        playerRandomNames.clear()

        if (randomNames) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                val playerUUID = playerInfo.gameProfile.id
                val randomizeName = (1..nameLength).joinToString("") { characters.random().toString() }
                playerRandomNames[playerUUID] = randomizeName to nameLength
            }
        }

        if (randomNameLength) {
            for (playerInfo in mc.netHandler.playerInfoMap) {
                val playerUUID = playerInfo.gameProfile.id

                val randomLength = nameLengthRange.random()
                val randomizeName = (1..randomLength).joinToString("") { characters.random().toString() }

                playerRandomNames[playerUUID] = randomizeName to randomLength
            }
        }
    }

    /**
     * Handle text messages from font renderer
     */
    fun handleTextMessage(text: String): String {
        val p = mc.thePlayer ?: return text

        // If the message includes the client name, don't change it
        if ("§8[§9§l$CLIENT_NAME§8] §3" in text) {
            return text
        }

        // Modify
        var newText = text

        for (friend in friendsConfig.friends) {
            newText = newText.replace(friend.playerName, translateAlternateColorCodes(friend.alias) + "§f")
        }

        // If the Name Protect module is disabled, return the text already without further processing
        if (!state) {
            return newText
        }

        // Replace original name with fake name
        newText = newText.replace(p.name, translateAlternateColorCodes(fakeName) + "§f")

        // Replace all other player names with "Protected User" or Random Characters
        for (playerInfo in mc.netHandler.playerInfoMap) {
            val playerUUID = playerInfo.gameProfile.id

            if (allPlayers) {
                if (randomNames) {
                    val (protectedUsername, _) = playerRandomNames.getOrPut(playerUUID) {
                        val randomizeName = (1..nameLength).joinToString("") { characters.random().toString() }
                        randomizeName to nameLength
                    }

                    val escapedName = Regex.escape(playerInfo.gameProfile.name)
                    newText = newText.replace(Regex(escapedName), protectedUsername)

                    // Update all other player names when nameLength & min/maxNameLength value are changed
                    if (savedName != nameLength || savedLength != nameLengthRange) {
                        generateRandomNames()
                        savedName = nameLength
                        savedLength = nameLengthRange
                    }

                } else {
                    // Default
                    newText = newText.replace(playerInfo.gameProfile.name, "Protected User")
                }
            }
        }

        return newText
    }

    /**
     * Handle new players name
     */
    private fun handleNewPlayer(playerUUID: UUID) {
        if (allPlayers && randomNames) {
            val length = if (randomNameLength) nameLengthRange.random() else nameLength
            val randomizeName = (1..length).joinToString("") { characters.random().toString() }
            playerRandomNames[playerUUID] = randomizeName to length
        }
    }

    /**
     * Remove players name from map when they leaved
     */
    private fun handlePlayerLeave(playerUUID: UUID) {
        playerRandomNames.remove(playerUUID)
    }

}