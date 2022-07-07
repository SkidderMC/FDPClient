/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import net.ccbluex.liquidbounce.file.FileConfig
import java.io.File

class FriendsConfig(file: File) : FileConfig(file) {

    val friends = mutableListOf<Friend>()

    override fun loadConfig(config: String) {
        clearFriends()
        config.split("\n").forEach { line ->
            if (line.contains(":")) {
                val data = line.split(":").toTypedArray()
                addFriend(data[0], data[1])
            } else {
                addFriend(line)
            }
        }
    }

    override fun saveConfig(): String {
        val builder = StringBuilder()

        for (friend in friends)
            builder.append(friend.playerName).append(":").append(friend.alias).append("\n")

        return builder.toString()
    }

    /**
     * Add friend to config
     *
     * @param playerName of friend
     * @param alias      of friend
     * @return of successfully added friend
     */
    @JvmOverloads
    fun addFriend(playerName: String, alias: String = playerName): Boolean {
        if (isFriend(playerName)) {
            return false
        }

        friends.add(Friend(playerName, alias))

        return true
    }

    /**
     * Remove friend from config
     *
     * @param playerName of friend
     */
    fun removeFriend(playerName: String): Boolean {
        if (!isFriend(playerName)) return false
        friends.removeIf { friend: Friend -> friend.playerName == playerName }
        return true
    }

    /**
     * Check is friend
     *
     * @param playerName of friend
     * @return is friend
     */
    fun isFriend(playerName: String): Boolean {
        for (friend in friends) if (friend.playerName == playerName) return true
        return false
    }

    /**
     * Clear all friends from config
     */
    fun clearFriends() {
        friends.clear()
    }

    /**
     * @param playerName of friend
     * @param alias      of friend
     */
    class Friend(val playerName: String, val alias: String)
}