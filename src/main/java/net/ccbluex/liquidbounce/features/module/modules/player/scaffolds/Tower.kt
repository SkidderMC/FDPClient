/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.scaffolds

import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold.searchMode
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold.shouldGoDown
import net.ccbluex.liquidbounce.utils.block.block
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.isMoving
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.blocksAmount
import net.ccbluex.liquidbounce.utils.timing.TickTimer
import net.minecraft.init.Blocks.air
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos
import kotlin.math.truncate

object Tower : Configurable("Tower"), MinecraftInstance, Listenable {

    val towerModeValues = choices(
        "TowerMode",
        arrayOf(
            "None",
            "Jump",
            "MotionJump",
            "Motion",
            "ConstantMotion",
            "MotionTP",
            "Packet",
            "Teleport",
            "AAC3.3.9",
            "AAC3.6.4",
            "Vulcan2.9.0",
            "Pulldown"
        ),
        "None"
    )

    val stopWhenBlockAboveValues = boolean("StopWhenBlockAbove", false) { towerModeValues.get() != "None" }

    val onJumpValues = boolean("TowerOnJump", true) { towerModeValues.get() != "None" }
    val notOnMoveValues = boolean("TowerNotOnMove", false) { towerModeValues.get() != "None" }

    // Jump mode
    val jumpMotionValues = float("JumpMotion", 0.42f, 0.3681289f..0.79f) { towerModeValues.get() == "MotionJump" }
    val jumpDelayValues = int(
        "JumpDelay",
        0,
        0..20
    ) { towerModeValues.get() == "MotionJump" || towerModeValues.get() == "Jump" }

    // Constant Motion values
    val constantMotionValues = float(
        "ConstantMotion",
        0.42f,
        0.1f..1f
    ) { towerModeValues.get() == "ConstantMotion" }
    val constantMotionJumpGroundValues = float(
        "ConstantMotionJumpGround",
        0.79f,
        0.76f..1f
    ) { towerModeValues.get() == "ConstantMotion" }
    val constantMotionJumpPacketValues = boolean("JumpPacket", true) { towerModeValues.get() == "ConstantMotion" }

    // Pull-down
    val triggerMotionValues = float("TriggerMotion", 0.1f, 0.0f..0.2f) { towerModeValues.get() == "Pulldown" }
    val dragMotionValues = float("DragMotion", 1.0f, 0.1f..1.0f) { towerModeValues.get() == "Pulldown" }

    // Teleport
    val teleportHeightValues = float("TeleportHeight", 1.15f, 0.1f..5f) { towerModeValues.get() == "Teleport" }
    val teleportDelayValues = int("TeleportDelay", 0, 0..20) { towerModeValues.get() == "Teleport" }
    val teleportGroundValues = boolean("TeleportGround", true) { towerModeValues.get() == "Teleport" }
    val teleportNoMotionValues = boolean("TeleportNoMotion", false) { towerModeValues.get() == "Teleport" }

    var isTowering = false

    // Mode stuff
    private val tickTimer = TickTimer()
    private var jumpGround = 0.0

    // Handle motion events
    val onMotion = handler<MotionEvent> { event ->
        val eventState = event.eventState

        val player = mc.thePlayer ?: return@handler

        isTowering = false

        if (towerModeValues.get() == "None" || notOnMoveValues.get() && player.isMoving ||
            onJumpValues.get() && !mc.gameSettings.keyBindJump.isKeyDown
        ) {
            return@handler
        }

        isTowering = true

        if (eventState == EventState.POST) {
            tickTimer.update()

            if (!stopWhenBlockAboveValues.get() || BlockPos(player).up(2).block == air) {
                move()
            }

            val blockPos = BlockPos(player).down()

            if (blockPos.block == air) {
                Scaffold.search(blockPos, !shouldGoDown, searchMode == "Area")
            }
        }
    }

    // Handle jump events
    val onJump = handler<JumpEvent> { event ->
        if (onJumpValues.get()) {
            if (Scaffold.scaffoldMode == "GodBridge" && (Scaffold.jumpAutomatically) || !Scaffold.shouldJumpOnInput)
                return@handler
            if (towerModeValues.get() == "None" || towerModeValues.get() == "Jump")
                return@handler
            if (notOnMoveValues.get() && mc.thePlayer.isMoving)
                return@handler
            if (Speed.state || Flight.state)
                return@handler

            event.cancelEvent()
        }
    }

    // Send jump packets, bypasses Hypixel.
    private fun fakeJump() {
        mc.thePlayer?.isAirBorne = true
        mc.thePlayer?.triggerAchievement(StatList.jumpStat)
    }

    /**
     * Move player
     */
    private fun move() {
        val player = mc.thePlayer ?: return

        if (blocksAmount() <= 0)
            return

        when (towerModeValues.get().lowercase()) {
            "jump" -> if (player.onGround && tickTimer.hasTimePassed(jumpDelayValues.get())) {
                fakeJump()
                player.tryJump()
            } else if (!player.onGround) {
                player.isAirBorne = false
                tickTimer.reset()
            }

            "motion" -> if (player.onGround) {
                fakeJump()
                player.motionY = 0.42
            } else if (player.motionY < 0.1) {
                player.motionY = -0.3
            }

            // Old Name (Jump)
            "motionjump" -> if (player.onGround && tickTimer.hasTimePassed(jumpDelayValues.get())) {
                fakeJump()
                player.motionY = jumpMotionValues.get().toDouble()
                tickTimer.reset()
            }

            "motiontp" -> if (player.onGround) {
                fakeJump()
                player.motionY = 0.42
            } else if (player.motionY < 0.23) {
                player.setPosition(player.posX, truncate(player.posY), player.posZ)
            }

            "packet" -> if (player.onGround && tickTimer.hasTimePassed(2)) {
                fakeJump()
                sendPackets(
                    C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 0.42,
                        player.posZ,
                        false
                    ),
                    C04PacketPlayerPosition(
                        player.posX,
                        player.posY + 0.753,
                        player.posZ,
                        false
                    )
                )
                player.setPosition(player.posX, player.posY + 1.0, player.posZ)
                tickTimer.reset()
            }

            "teleport" -> {
                if (teleportNoMotionValues.get()) {
                    player.motionY = 0.0
                }
                if ((player.onGround || !teleportGroundValues.get()) && tickTimer.hasTimePassed(
                        teleportDelayValues.get()
                    )
                ) {
                    fakeJump()
                    player.setPositionAndUpdate(
                        player.posX, player.posY + teleportHeightValues.get(), player.posZ
                    )
                    tickTimer.reset()
                }
            }

            "constantmotion" -> {
                if (player.onGround) {
                    if (constantMotionJumpPacketValues.get()) {
                        fakeJump()
                    }
                    jumpGround = player.posY
                    player.motionY = constantMotionValues.get().toDouble()
                }
                if (player.posY > jumpGround + constantMotionJumpGroundValues.get()) {
                    if (constantMotionJumpPacketValues.get()) {
                        fakeJump()
                    }
                    player.setPosition(
                        player.posX, truncate(player.posY), player.posZ
                    ) // TODO: toInt() required?
                    player.motionY = constantMotionValues.get().toDouble()
                    jumpGround = player.posY
                }
            }

            "pulldown" -> {
                if (!player.onGround && player.motionY < triggerMotionValues.get()) {
                    player.motionY = -dragMotionValues.get().toDouble()
                } else {
                    fakeJump()
                }
            }

            // Credit: @localpthebest / Nextgen
            "vulcan2.9.0" -> {
                if (player.ticksExisted % 10 == 0) {
                    // Prevent Flight Flag
                    player.motionY = -0.1
                    return
                }

                fakeJump()

                if (player.ticksExisted % 2 == 0) {
                    player.motionY = 0.7
                } else {
                    player.motionY = if (player.isMoving) 0.42 else 0.6
                }
            }

            "aac3.3.9" -> {
                if (player.onGround) {
                    fakeJump()
                    player.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (player.motionY < 0) {
                    player.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
            }

            "aac3.6.4" -> if (player.ticksExisted % 4 == 1) {
                player.motionY = 0.4195464
                player.setPosition(player.posX - 0.035, player.posY, player.posZ)
            } else if (player.ticksExisted % 4 == 0) {
                player.motionY = -0.5
                player.setPosition(player.posX + 0.035, player.posY, player.posZ)
            }
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        val packet = event.packet

        if (towerModeValues.get() == "Vulcan2.9.0" && packet is C04PacketPlayerPosition &&
            !player.isMoving && player.ticksExisted % 2 == 0
        ) {
            packet.x += 0.1
            packet.z += 0.1
        }
    }

    override fun handleEvents() = Scaffold.handleEvents()
}