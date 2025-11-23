/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.get
import net.ccbluex.liquidbounce.utils.kotlin.SharedScopes
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import net.minecraft.network.play.server.S38PacketPlayerListItem.Action.UPDATE_LATENCY
import java.util.concurrent.ConcurrentHashMap

object StaffDetector : Module("StaffDetector", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    // Name to IP
    private val serverIpMap = mapOf(
        "BlocksMC" to "blocksmc.com",
        "CubeCraft" to "cubecraft.net",
        "Gamster" to "gamster.org",
        "AgeraPvP" to "agerapvp.club",
        "HypeMC" to "hypemc.pro",
        "Hypixel" to "hypixel.net",
        "SuperCraft" to "supercraft.es",
        "PikaNetwork" to "pika-network.net",
        "GommeHD" to "gommehd.net",
        "CoralMC" to "coralmc.it",
        "LibreCraft" to "librecraft.com",
        "Originera" to "mc.orea.asia",
        "OC-TC" to "oc.tc",
        "AssPixel" to "asspixel.net"
    )

    private val staffMode by choices(
        "StaffMode", serverIpMap.keys.toTypedArray(), "BlocksMC"
    ).onChanged(::loadStaffData)

    private val tab by boolean("TAB", true)
    private val packet by boolean("Packet", true)
    private val velocity by boolean("Velocity", true)
    private val vanish by boolean("Vanish", true)

    private val autoLeave by choices("AutoLeave", arrayOf("Off", "Leave", "Lobby", "Quit"), "Off") { tab || packet }

    private val spectator by boolean("StaffSpectator", false) { tab || packet }
    private val otherSpectator by boolean("OtherSpectator", false) { tab || packet }

    private val inGame by boolean("InGame", true) { autoLeave != "Off" }
    private val warn by choices("Warn", arrayOf("Chat", "Notification"), "Chat")

    private val checkedStaff: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val checkedSpectator: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val playersInSpectatorMode: MutableSet<String> = ConcurrentHashMap.newKeySet()

    private var attemptLeave = false

    private var alertClearVanish = false

    private val staffList = ConcurrentHashMap<String, Set<String>>()

    private val moduleJobs = mutableListOf<Job>()

    override fun onDisable() {
        moduleJobs.forEach { it.cancel() }
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
        attemptLeave = false
        alertClearVanish = false
    }

    private fun isStaff(player: String): Boolean =
        staffList.values.any { staffNames -> staffNames.any { player.contains(it) } }

    /**
     * Reset on World Change
     */
    val onWorld = handler<WorldEvent> {
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
        alertClearVanish = false
    }

    private fun loadStaffData(serverName: String) {
        val ip = serverIpMap[serverName] ?: return

        moduleJobs += SharedScopes.IO.launch {
            loadStaffList(ip)
        }
    }

    private fun checkedStaffRemoved() {
        mc.netHandler?.playerInfoMap?.mapNotNullTo(hashSetOf()) { it?.gameProfile?.name }?.let(checkedStaff::retainAll)
    }

    val onPacket = handler<PacketEvent> { event ->
        if (mc.thePlayer == null || mc.theWorld == null) {
            return@handler
        }

        val packet = event.packet

        /**
         * OLD BlocksMC Staff Spectator Check
         * Credit: @HU & Modified by @EclipsesDev
         *
         * NOTE: Doesn't detect staff spectator all the time.
         */
        if (spectator) {
            if (packet is S3EPacketTeams) {
                val teamName = packet.name

                if (teamName.equals("Z_Spectator", true)) {
                    val players = packet.players ?: return@handler

                    val staffSpectateList = players.filter { it !in checkedSpectator && isStaff(it) }
                    val nonStaffSpectateList = players.filter { it !in checkedSpectator && !isStaff(it) }

                    // Check for players who are using spectator menu
                    val miscSpectatorList = playersInSpectatorMode - players.toSet()

                    staffSpectateList.forEach { player ->
                        notifySpectators(player)
                    }

                    nonStaffSpectateList.forEach { player ->
                        if (otherSpectator) {
                            notifySpectators(player)
                        }
                    }

                    miscSpectatorList.forEach { player ->
                        val isStaff = isStaff(player)

                        if (isStaff && spectator) {
                            chat("§c[STAFF] §d${player} §3is using the spectator menu §e(compass/left)")
                        }

                        if (!isStaff && otherSpectator) {
                            chat("§d${player} §3is using the spectator menu §e(compass/left)")
                        }
                        checkedSpectator.remove(player)
                    }

                    // Update the set of players in spectator mode
                    playersInSpectatorMode.clear()
                    playersInSpectatorMode.addAll(players)
                }
            }

            // Handle other packets
            handleOtherChecks(packet)
        }

        /**
         * Velocity Check
         * Credit: @azureskylines / Nextgen
         *
         * Check if this is a regular velocity update
         */
        if (velocity) {
            if (packet is S12PacketEntityVelocity && packet.entityID == mc.thePlayer?.entityId) {
                if (packet.motionX == 0 && packet.motionZ == 0 && packet.motionY / 8000.0 > 0.075) {
                    attemptLeave = false
                    autoLeave()

                    if (warn == "Chat") {
                        chat("§3Staff is Watching")
                    } else {
                        hud.addNotification(Notification("§3Staff is Watching", "!!!", Type.WARNING, 3000))
                    }
                }
            }
        }
    }

    private fun notifySpectators(player: String) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff = isStaff(player)

        if (isStaff && spectator) {
            if (warn == "Chat") {
                chat("§c[STAFF] §d${player} §3is a spectator")
            } else {
                hud.addNotification(Notification("§c[STAFF] §d${player} §3is a spectators", "!!!", Type.INFO, 1000))
            }
        }

        if (!isStaff && otherSpectator) {
            if (warn == "Chat") {
                chat("§d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("§d${player} §3is a spectators", "!!!", Type.INFO, 60))
            }
        }

        attemptLeave = false
        checkedSpectator.add(player)

        if (isStaff) {
            autoLeave()
        }
    }

    /**
     * Check staff using TAB
     */
    private fun notifyStaff() {
        if (!tab)
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val playerInfoMap = mc.netHandler?.playerInfoMap ?: return

        val playerInfos = synchronized(playerInfoMap) {
            playerInfoMap.mapNotNull { playerInfo ->
                playerInfo?.gameProfile?.name?.let { playerName ->
                    playerName to playerInfo.responseTime
                }
            }
        }

        playerInfos.forEach { (player, responseTime) ->
            val isStaff = isStaff(player)

            val condition = when {
                responseTime > 0 -> "§e(${responseTime}ms)"
                responseTime == 0 -> "§a(Joined)"
                else -> "§c(Ping error)"
            }

            val warnings = "§c[STAFF] §d${player} §3is a staff §b(TAB) $condition"

            if (isStaff && player !in checkedStaff) {
                if (warn == "Chat") {
                    chat(warnings)
                } else {
                    hud.addNotification(Notification(warnings, "!!!", Type.WARNING, 60))
                }

                attemptLeave = false
                checkedStaff.add(player)

                autoLeave()
            }
        }
    }

    /**
     * Check staff using Packet
     */
    private fun notifyStaffPacket(staff: Entity) {
        if (!packet)
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff = if (staff is EntityPlayer) {
            isStaff(staff.gameProfile.name)
        } else {
            false
        }

        val condition = when (staff) {
            is EntityPlayer -> {
                val responseTime = mc.netHandler?.getPlayerInfo(staff.uniqueID)?.responseTime ?: 0
                when {
                    responseTime > 0 -> "§e(${responseTime}ms)"
                    responseTime == 0 -> "§a(Joined)"
                    else -> "§c(Ping error)"
                }
            }

            else -> ""
        }

        val playerName = if (staff is EntityPlayer) staff.gameProfile.name else ""

        val warnings = "§c[STAFF] §d${playerName} §3is a staff §b(Packet) $condition"

        if (isStaff && playerName !in checkedStaff) {
            if (warn == "Chat") {
                chat(warnings)
            } else {
                hud.addNotification(Notification(warnings, "!!!", Type.WARNING, 60))
            }

            attemptLeave = false
            checkedStaff.add(playerName)

            autoLeave()
        }
    }

    private fun autoLeave() {
        val firstSlotItemStack = mc.thePlayer.inventory.mainInventory[0] ?: return

        if (inGame && (firstSlotItemStack.item == Items.compass || firstSlotItemStack.item == Items.bow)) {
            return
        }

        if (!attemptLeave && autoLeave != "Off") {
            when (autoLeave.lowercase()) {
                "leave" -> mc.thePlayer.sendChatMessage("/leave")
                "lobby" -> mc.thePlayer.sendChatMessage("/lobby")
                "quit" -> mc.theWorld.sendQuittingDisconnectingPacket()
            }
            attemptLeave = true
        }
    }

    private fun handlePlayerList(packet: S38PacketPlayerListItem) {
        val action = packet.action
        val entries = packet.entries

        if (!vanish) return

        if (action == UPDATE_LATENCY) {
            val playerListSize = mc.netHandler?.playerInfoMap?.size ?: 0

            if (entries.size != playerListSize) {
                if (warn == "Chat") {
                    chat("§aA player might be vanished.")
                } else {
                    hud.addNotification(Notification("§aA player might be vanished.", "§aA player might be vanished.", Type.WARNING, 3000))
                }

                alertClearVanish = false
            } else {
                if (alertClearVanish)
                    return

                if (warn == "Chat") {
                    chat("§cNo players are vanished")
                } else {
                    hud.addNotification(Notification("§cNo players are vanished", "§cNo players are vanished",Type.WARNING, 3000))
                }

                alertClearVanish = true
            }
        }
    }

    private fun handleOtherChecks(packet: Packet<*>?) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        when (packet) {
            is S01PacketJoinGame -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S0CPacketSpawnPlayer -> handleStaff(mc.theWorld.getEntityByID(packet.entityID) ?: null)
            is S18PacketEntityTeleport -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1CPacketEntityMetadata -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1DPacketEntityEffect -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S1EPacketRemoveEntityEffect -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S19PacketEntityStatus -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S19PacketEntityHeadLook -> handleStaff(packet.getEntity(mc.theWorld) ?: null)
            is S49PacketUpdateEntityNBT -> handleStaff(packet.getEntity(mc.theWorld) ?: null)
            is S1BPacketEntityAttach -> handleStaff(mc.theWorld.getEntityByID(packet.entityId) ?: null)
            is S04PacketEntityEquipment -> handleStaff(mc.theWorld.getEntityByID(packet.entityID) ?: null)
            is S38PacketPlayerListItem -> handlePlayerList(packet)
        }
    }

    private fun handleStaff(staff: Entity?) {
        if (mc.thePlayer == null || mc.theWorld == null || staff == null) {
            return
        }

        checkedStaffRemoved()

        notifyStaff()
        notifyStaffPacket(staff)
    }

    private fun loadStaffList(serverIp: String) {
        try {
            HttpClient.get("$CLIENT_CLOUD/staffs/$serverIp").use { response ->
                when (val code = response.code) {
                    200 -> {
                        val staffs = response.body.charStream().buffered().lineSequence()
                            .mapNotNullTo(hashSetOf()) { line ->
                                line.trim().takeIf { it.isNotBlank() }
                            }

                        chat("§aSuccessfully loaded §9${staffs.size} §astaff names.")
                        staffList[serverIp] = staffs
                    }

                    404 -> {
                        chat("§cFailed to load staff list. §9(§3Doesn't exist in LiquidCloud§9)")
                    }

                    else -> {
                        chat("§cFailed to load staff list. §9(§3ERROR CODE: $code§9)")
                    }
                }
            }
        } catch (e: Exception) {
            chat("§cFailed to load staff list. §9(${e.message})")
        }
    }

    /**
     * HUD TAG
     */
    override val tag
        get() = staffMode
}