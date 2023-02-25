/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.minecraft.network.play.server.S02PacketChat
import java.util.regex.Pattern

@ModuleInfo(name = "HytAntiBot", description = "Only supports bedwars", category = ModuleCategory.MISC)
class HytAntiBot : Module() {

    private val mode = ListValue("Mode", arrayOf("4V4/1V1/32/64", "16V16"), "4V4/1V1/32/64")
    private val debug = BoolValue("Debug", true)

    override fun onDisable() {
        LiquidBounce.fileManager.friendsConfig.clearFriends()
        super.onDisable()
    }

    private fun displayDebug(isAdd : Boolean, name : String) {
        if (!debug.get()) return
        if (isAdd) ClientUtils.displayChatMessage("${LiquidBounce.COLORED_NAME}§c§d添加假人：$name")
        else ClientUtils.displayChatMessage("${LiquidBounce.COLORED_NAME}§c§d删除假人：$name")
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S02PacketChat) {
            when (mode.get().lowercase()) {
                "4V4/1V1/32/64" -> {
                    val matcher = Pattern.compile("杀死了 (.*?)\\(").matcher(packet.chatComponent.unformattedText)
                    val matcher2 = Pattern.compile("起床战争>> (.*?) (\\(((.*?)死了!))").matcher(packet.chatComponent.unformattedText)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        if (name != "") {
                            LiquidBounce.fileManager.friendsConfig.addFriend(name)
                            displayDebug(true, name)
                            Thread {
                                try {
                                    Thread.sleep(5000)
                                    LiquidBounce.fileManager.friendsConfig.removeFriend(name)
                                    displayDebug(false, name)
                                } catch (ex: InterruptedException) {
                                    ex.printStackTrace()
                                }
                            }.start()
                        }
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        if (name != "") {
                            LiquidBounce.fileManager.friendsConfig.addFriend(name)
                            displayDebug(true, name)
                            Thread {
                                try {
                                    Thread.sleep(5000)
                                    LiquidBounce.fileManager.friendsConfig.removeFriend(name)
                                    displayDebug(false, name)
                                } catch (ex: InterruptedException) {
                                    ex.printStackTrace()
                                }
                            }.start()
                        }
                    }
                }
                "16v16" ->{
                    val matcher = Pattern.compile("击败了 (.*?)!").matcher(packet.chatComponent.unformattedText)
                    val matcher2 = Pattern.compile("玩家 (.*?)死了！").matcher(packet.chatComponent.unformattedText)
                    if (matcher.find()) {
                        val name = matcher.group(1).trim()
                        if (name != "") {
                            LiquidBounce.fileManager.friendsConfig.addFriend(name)
                            displayDebug(true, name)
                            Thread {
                                try {
                                    Thread.sleep(10000)
                                    LiquidBounce.fileManager.friendsConfig.removeFriend(name)
                                    displayDebug(false, name)
                                } catch (ex: InterruptedException) {
                                    ex.printStackTrace()
                                }
                            }.start()
                        }
                    }
                    if (matcher2.find()) {
                        val name = matcher2.group(1).trim()
                        if (name != "") {
                            LiquidBounce.fileManager.friendsConfig.addFriend(name)
                            displayDebug(true, name)
                            Thread {
                                try {
                                    Thread.sleep(10000)
                                    LiquidBounce.fileManager.friendsConfig.removeFriend(name)
                                    displayDebug(false, name)
                                } catch (ex: InterruptedException) {
                                    ex.printStackTrace()
                                }
                            }.start()
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        LiquidBounce.fileManager.friendsConfig.clearFriends()
    }
}