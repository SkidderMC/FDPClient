/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.script.api.global.Chat
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.event.handler

object AutoRole : Module("AutoAddStaff", Category.OTHER, gameDetecting = false) {
    private val formattingValue by boolean("Formatting", true)

        private val STAFF_PREFIXES = arrayOf(
            "[Moderador] ",
            "[MODERADOR] ",
            "[Mod] ",
            "[MOD] ",
            "[Administrador] ",
            "[ADMINISTRADOR] ",
            "[Admin] ",
            "[ADMIN] ",
            "[Coordenador] ",
            "[COORDENADOR] ",
            "[Coord] ",
            "[COORD] ",
            "[Gerente] ",
            "[GERENTE] ",
            "[CEO] ",
            "[Dono] ",
            "[DONO] ",
            "[DIRETOR] ",
            "[DEV] ",
            "[Dev] ",
            "Diretor ",
            "DIRETOR ",
            "Dev ",
            "DEV ",
            "Mod ",
            "MOD ",
            "[Master] ",
            "[MASTER] ",
            "[CEO] ",
            "[TRIAL] "
        )

    private fun isStaff(prefix: String): Boolean {
        return STAFF_PREFIXES.toList().contains(stripColor(prefix))
    }

    val handle = handler<UpdateEvent> { event ->
        val friendManager = friendsConfig

        val formatCodes = arrayOf("§k", "§l", "§m", "§n", "§o")
        var currentFormatIndex = 0

        for (team in mc.theWorld.scoreboard.teams) {
            if (this.isStaff(team.colorPrefix)) {
                for (member in team.membershipCollection) {
                    if (!friendManager.isFriend(member)) {
                        friendManager.addFriend(member)

                        var colorPrefix = team.colorPrefix
                        if (formattingValue && currentFormatIndex < formatCodes.size) {
                            colorPrefix = formatCodes[currentFormatIndex] + colorPrefix
                            currentFormatIndex++
                            if (currentFormatIndex >= formatCodes.size) {
                                currentFormatIndex = 0
                            }
                        }

                        Chat.print("§7[§d!§7]§7 ADDED: $colorPrefix$member")
                    }
                }
            }
        }
    }
}
