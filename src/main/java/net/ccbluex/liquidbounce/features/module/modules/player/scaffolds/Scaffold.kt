/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.scaffolds

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.canUpdateRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.blocksAmount
import net.ccbluex.liquidbounce.utils.inventory.hotBarSlot
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timing.DelayTimer
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickDelayTimer
import net.ccbluex.liquidbounce.utils.timing.TimeUtils
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.config.FloatValue
import net.ccbluex.liquidbounce.config.IntegerValue
import net.ccbluex.liquidbounce.config.ListValue
import net.ccbluex.liquidbounce.config.boolean
import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.config.float
import net.ccbluex.liquidbounce.config.int
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.block.*
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.rotation.PlaceRotation
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationSettingsWithRotationModes
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.minecraft.block.BlockBush
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks.air
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import net.minecraftforge.event.ForgeEventFactory
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*

object Scaffold : Module("Scaffold", Category.PLAYER, Keyboard.KEY_I, hideModule = false) {

    /**
     * TOWER MODES & SETTINGS
     */

    // -->

    private val towerMode by Tower.towerModeValues
    private val stopWhenBlockAbove by Tower.stopWhenBlockAboveValues
    private val onJump by Tower.onJumpValues
    private val notOnMove by Tower.notOnMoveValues
    private val jumpMotion by Tower.jumpMotionValues
    private val jumpDelay by Tower.jumpDelayValues
    private val constantMotion by Tower.constantMotionValues
    private val constantMotionJumpGround by Tower.constantMotionJumpGroundValues
    private val constantMotionJumpPacket by Tower.constantMotionJumpPacketValues
    private val triggerMotion by Tower.triggerMotionValues
    private val dragMotion by Tower.dragMotionValues
    private val teleportHeight by Tower.teleportHeightValues
    private val teleportDelay by Tower.teleportDelayValues
    private val teleportGround by Tower.teleportGroundValues
    private val teleportNoMotion by Tower.teleportNoMotionValues

    // <--

    /**
     * SCAFFOLD MODES & SETTINGS
     */

    // -->

    val scaffoldMode by choices(
        "ScaffoldMode", arrayOf("Normal", "Rewinside", "Expand", "Telly", "GodBridge"), "Normal"
    )

    // Expand
    private val omniDirectionalExpand by boolean("OmniDirectionalExpand", false) { scaffoldMode == "Expand" }
    private val expandLength by int("ExpandLength", 1, 1..6) { scaffoldMode == "Expand" }

    // Placeable delay
    private val placeDelayValue = boolean("PlaceDelay", true) { scaffoldMode != "GodBridge" }
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0..1000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)
        override fun isSupported() = placeDelayValue.isActive()
    }
    private val maxDelay by maxDelayValue

    private val minDelayValue = object : IntegerValue("MinDelay", 0, 0..1000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)
        override fun isSupported() = placeDelayValue.isActive() && !maxDelayValue.isMinimal()
    }
    private val minDelay by minDelayValue

    // Extra clicks
    private val extraClicks by boolean("DoExtraClicks", false)
    private val simulateDoubleClicking by boolean("SimulateDoubleClicking", false) { extraClicks }
    private val extraClickMaxCPSValue: IntegerValue = object : IntegerValue("ExtraClickMaxCPS", 7, 0..50) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(extraClickMinCPS)
        override fun isSupported() = extraClicks
    }
    private val extraClickMaxCPS by extraClickMaxCPSValue

    private val extraClickMinCPS by object : IntegerValue("ExtraClickMinCPS", 3, 0..50) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(extraClickMaxCPS)
        override fun isSupported() = extraClicks && !extraClickMaxCPSValue.isMinimal()
    }

    private val placementAttempt by choices(
        "PlacementAttempt", arrayOf("Fail", "Independent"), "Fail"
    ) { extraClicks }

    // Autoblock
    private val autoBlock by choices("AutoBlock", arrayOf("Off", "Pick", "Spoof", "Switch"), "Spoof")
    private val sortByHighestAmount by boolean("SortByHighestAmount", false) { autoBlock != "Off" }
    private val earlySwitch by boolean("EarlySwitch", false) { autoBlock != "Off" && !sortByHighestAmount }
    private val amountBeforeSwitch by int(
        "SlotAmountBeforeSwitch", 3, 1..10
    ) { earlySwitch && !sortByHighestAmount }

    // Settings
    private val autoF5 by boolean("AutoF5", false, subjective = true)

    // Basic stuff
    val sprint by boolean("Sprint", false)
    private val swing by boolean("Swing", true, subjective = true)
    private val down by boolean("Down", true) { !sameY && scaffoldMode !in arrayOf("GodBridge", "Telly") }

    private val ticksUntilRotation: IntegerValue = object : IntegerValue("TicksUntilRotation", 3, 1..5) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceIn(minimum, maximum)
    }

    // GodBridge mode sub-values
    private val waitForRots by boolean("WaitForRotations", false) { isGodBridgeEnabled }
    private val useOptimizedPitch by boolean("UseOptimizedPitch", false) { isGodBridgeEnabled }
    private val customGodPitch by float(
        "GodBridgePitch", 73.5f, 0f..90f
    ) { isGodBridgeEnabled && !useOptimizedPitch }

    val jumpAutomatically by boolean("JumpAutomatically", true) { scaffoldMode == "GodBridge" }
    private val maxBlocksToJump: IntegerValue = object : IntegerValue("MaxBlocksToJump", 4, 1..8) {
        override fun isSupported() = scaffoldMode == "GodBridge" && !jumpAutomatically
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minBlocksToJump.get())
    }

    private val minBlocksToJump: IntegerValue = object : IntegerValue("MinBlocksToJump", 4, 1..8) {
        override fun isSupported() = scaffoldMode == "GodBridge" && !jumpAutomatically && !maxBlocksToJump.isMinimal()

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxBlocksToJump.get())
    }

    // Telly mode subvalues
    private val startHorizontally by boolean("StartHorizontally", true) { scaffoldMode == "Telly" }
    private val maxHorizontalPlacements: IntegerValue = object : IntegerValue("MaxHorizontalPlacements", 1, 1..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minHorizontalPlacements.get())
    }
    private val minHorizontalPlacements: IntegerValue = object : IntegerValue("MinHorizontalPlacements", 1, 1..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxHorizontalPlacements.get())
    }
    private val maxVerticalPlacements: IntegerValue = object : IntegerValue("MaxVerticalPlacements", 1, 1..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minVerticalPlacements.get())
    }

    private val minVerticalPlacements: IntegerValue = object : IntegerValue("MinVerticalPlacements", 1, 1..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxVerticalPlacements.get())
    }

    private val maxJumpTicks: IntegerValue = object : IntegerValue("MaxJumpTicks", 0, 0..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minJumpTicks.get())
    }
    private val minJumpTicks: IntegerValue = object : IntegerValue("MinJumpTicks", 0, 0..10) {
        override fun isSupported() = scaffoldMode == "Telly"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxJumpTicks.get())
    }

    private val allowClutching by boolean("AllowClutching", true) { scaffoldMode !in arrayOf("Telly", "Expand") }
    private val horizontalClutchBlocks: IntegerValue = object : IntegerValue("HorizontalClutchBlocks", 3, 1..5) {
        override fun isSupported() = allowClutching && scaffoldMode !in arrayOf("Telly", "Expand")
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceIn(minimum, maximum)
    }
    private val verticalClutchBlocks: IntegerValue = object : IntegerValue("VerticalClutchBlocks", 2, 1..3) {
        override fun isSupported() = allowClutching && scaffoldMode !in arrayOf("Telly", "Expand")
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceIn(minimum, maximum)
    }
    private val blockSafe by boolean("BlockSafe", false) { !isGodBridgeEnabled }

    // Eagle
    private val eagleValue =
        ListValue("Eagle", arrayOf("Normal", "Silent", "Off"), "Normal") { scaffoldMode != "GodBridge" }
    val eagle by eagleValue
    private val adjustedSneakSpeed by boolean("AdjustedSneakSpeed", true) { eagle == "Silent" }
    private val eagleSpeed by float("EagleSpeed", 0.3f, 0.3f..1.0f) { eagleValue.isSupported() && eagle != "Off" }
    val eagleSprint by boolean("EagleSprint", false) { eagleValue.isSupported() && eagle == "Normal" }
    private val blocksToEagle by int("BlocksToEagle", 0, 0..10) { eagleValue.isSupported() && eagle != "Off" }
    private val edgeDistance by float(
        "EagleEdgeDistance", 0f, 0f..0.5f
    ) { eagleValue.isSupported() && eagle != "Off" }
    private val onlyOnGround by boolean("OnlyOnGround", false) { eagleValue.isSupported() && eagle != "Off" }

    // Rotation Options
    private val modeList =
        choices("Rotations", arrayOf("Off", "Normal", "Stabilized", "ReverseYaw", "GodBridge"), "Normal")

    private val options = RotationSettingsWithRotationModes(this, modeList).apply {
        strictValue.excludeWithState()
        resetTicksValue.setSupport { it && scaffoldMode != "Telly" }
    }

    // Search options
    val searchMode by choices("SearchMode", arrayOf("Area", "Center"), "Area") { scaffoldMode != "GodBridge" }
    private val minDist by float("MinDist", 0f, 0f..0.2f) { scaffoldMode !in arrayOf("GodBridge", "Telly") }

    // Zitter
    private val zitterMode by choices("Zitter", arrayOf("Off", "Teleport", "Smooth"), "Off")
    private val zitterSpeed by float("ZitterSpeed", 0.13f, 0.1f..0.3f) { zitterMode == "Teleport" }
    private val zitterStrength by float("ZitterStrength", 0.05f, 0f..0.2f) { zitterMode == "Teleport" }

    private val maxZitterTicksValue: IntegerValue = object : IntegerValue("MaxZitterTicks", 3, 0..6) {
        override fun isSupported() = zitterMode == "Smooth"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minZitterTicks)
    }
    private val maxZitterTicks by maxZitterTicksValue

    private val minZitterTicksValue: IntegerValue = object : IntegerValue("MinZitterTicks", 2, 0..6) {
        override fun isSupported() = zitterMode == "Smooth" && !maxZitterTicksValue.isMinimal()
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxZitterTicks)
    }
    private val minZitterTicks by minZitterTicksValue

    private val useSneakMidAir by boolean("UseSneakMidAir", false) { zitterMode == "Smooth" }

    // Game
    val timer by float("Timer", 1f, 0.1f..10f)
    private val speedModifier by float("SpeedModifier", 1f, 0f..2f)
    private val speedLimiter by boolean("SpeedLimiter", false) { !slow }
    private val speedLimit by float("SpeedLimit", 0.11f, 0.01f..0.12f) { !slow && speedLimiter }
    private val slow by boolean("Slow", false)
    private val slowGround by boolean("SlowOnlyGround", false) { slow }
    private val slowSpeed by float("SlowSpeed", 0.6f, 0.2f..0.8f) { slow }

    // Jump Strafe
    private val jumpStrafe by boolean("JumpStrafe", false)
    private val maxJumpStraightStrafe: FloatValue = object : FloatValue("MaxStraightStrafe", 0.45f, 0.1f..1f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minJumpStraightStrafe.get())
        override fun isSupported() = jumpStrafe
    }

    private val minJumpStraightStrafe: FloatValue = object : FloatValue("MinStraightStrafe", 0.4f, 0.1f..1f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxJumpStraightStrafe.get())
        override fun isSupported() = jumpStrafe
    }

    private val maxJumpDiagonalStrafe: FloatValue = object : FloatValue("MaxDiagonalStrafe", 0.45f, 0.1f..1f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minJumpDiagonalStrafe.get())
        override fun isSupported() = jumpStrafe
    }

    private val minJumpDiagonalStrafe: FloatValue = object : FloatValue("MinDiagonalStrafe", 0.4f, 0.1f..1f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxJumpDiagonalStrafe.get())
        override fun isSupported() = jumpStrafe
    }

    // Safety
    private val sameY by boolean("SameY", false) { scaffoldMode != "GodBridge" }
    private val jumpOnUserInput by boolean("JumpOnUserInput", true) { sameY && scaffoldMode != "GodBridge" }

    private val safeWalkValue = boolean("SafeWalk", true) { scaffoldMode != "GodBridge" }
    private val airSafe by boolean("AirSafe", false) { safeWalkValue.isActive() }

    // Visuals
    private val mark by boolean("Mark", false, subjective = true)
    private val trackCPS by boolean("TrackCPS", false, subjective = true)

    // Target placement
    var placeRotation: PlaceRotation? = null

    // Launch position
    private var launchY = -999

    val shouldJumpOnInput
        get() = !jumpOnUserInput || !mc.gameSettings.keyBindJump.isKeyDown && mc.thePlayer.posY >= launchY && !mc.thePlayer.onGround

    private val shouldKeepLaunchPosition
        get() = sameY && shouldJumpOnInput && scaffoldMode != "GodBridge"

    // Zitter
    private var zitterDirection = false

    // Delay
    private val delayTimer = object : DelayTimer(minDelayValue, maxDelayValue, MSTimer()) {
        override fun hasTimePassed() = !placeDelayValue.isActive() || super.hasTimePassed()
    }

    private val zitterTickTimer = TickDelayTimer(minZitterTicksValue, maxZitterTicksValue)

    // Eagle
    private var placedBlocksWithoutEagle = 0
    var eagleSneaking = false
    private val isEagleEnabled
        get() = eagle != "Off" && !shouldGoDown && scaffoldMode != "GodBridge" && (!onlyOnGround || mc.thePlayer?.onGround == true)

    // Downwards
    val shouldGoDown
        get() = down && !sameY && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && scaffoldMode !in arrayOf(
            "GodBridge", "Telly"
        ) && blocksAmount() > 1

    // Current rotation
    private val currRotation
        get() = RotationUtils.currentRotation ?: mc.thePlayer.rotation

    // Extra clicks
    private var extraClick = ExtraClickInfo(TimeUtils.randomClickDelay(extraClickMinCPS, extraClickMaxCPS), 0L, 0)

    // GodBridge
    private var blocksPlacedUntilJump = 0

    private val isManualJumpOptionActive
        get() = scaffoldMode == "GodBridge" && !jumpAutomatically

    private var blocksToJump = randomDelay(minBlocksToJump.get(), maxBlocksToJump.get())

    private val isGodBridgeEnabled
        get() = scaffoldMode == "GodBridge" || scaffoldMode == "Normal" && options.rotationMode == "GodBridge"

    private var godBridgeTargetRotation: Rotation? = null

    private val isLookingDiagonally: Boolean
        get() {
            val player = mc.thePlayer ?: return false

            val directionDegree = MovementUtils.direction.toDegreesF()

            // Round the direction rotation to the nearest multiple of 45 degrees so that way we check if the player faces diagonally
            val yaw = round(abs(MathHelper.wrapAngleTo180_float(directionDegree)) / 45f) * 45f

            val isYawDiagonal = yaw % 90 != 0f
            val isMovingDiagonal = player.movementInput.moveForward != 0f && player.movementInput.moveStrafe == 0f
            val isStrafing = mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindLeft.isKeyDown

            return isYawDiagonal && (isMovingDiagonal || isStrafing)
        }

    // Telly
    private var offGroundTicks = 0
    private var ticksUntilJump = 0
    private var blocksUntilAxisChange = 0
    private var jumpTicks = randomDelay(minJumpTicks.get(), maxJumpTicks.get())
    private var horizontalPlacements = randomDelay(minHorizontalPlacements.get(), maxHorizontalPlacements.get())
    private var verticalPlacements = randomDelay(minVerticalPlacements.get(), maxVerticalPlacements.get())
    private val shouldPlaceHorizontally
        get() = scaffoldMode == "Telly" && mc.thePlayer.isMoving && (startHorizontally && blocksUntilAxisChange <= horizontalPlacements || !startHorizontally && blocksUntilAxisChange > verticalPlacements)

    // <--

    // Enabling module
    override fun onEnable() {
        val player = mc.thePlayer ?: return

        launchY = player.posY.roundToInt()
        blocksUntilAxisChange = 0
    }

    // Events
    val onUpdate = loopHandler {
        val player = mc.thePlayer ?: return@loopHandler

        if (mc.playerController.currentGameType == WorldSettings.GameType.SPECTATOR) return@loopHandler

        mc.timer.timerSpeed = timer

        // Telly
        if (player.onGround) {
            offGroundTicks = 0
            ticksUntilJump++
        } else {
            offGroundTicks++
        }

        if (shouldGoDown) {
            mc.gameSettings.keyBindSneak.pressed = false
        }

        if (slow) {
            if (!slowGround || slowGround && player.onGround) {
                player.motionX *= slowSpeed
                player.motionZ *= slowSpeed
            }
        }

        // Eagle
        if (isEagleEnabled) {
            var dif = 0.5
            val blockPos = BlockPos(player).down()

            for (side in EnumFacing.values()) {
                if (side.axis == EnumFacing.Axis.Y) {
                    continue
                }

                val neighbor = blockPos.offset(side)

                if (neighbor.isReplaceable) {
                    val calcDif = (if (side.axis == EnumFacing.Axis.Z) {
                        abs(neighbor.z + 0.5 - player.posZ)
                    } else {
                        abs(neighbor.x + 0.5 - player.posX)
                    }) - 0.5

                    if (calcDif < dif) {
                        dif = calcDif
                    }
                }
            }

            if (placedBlocksWithoutEagle >= blocksToEagle) {
                val shouldEagle = blockPos.isReplaceable || dif < edgeDistance
                if (eagle == "Silent") {
                    if (eagleSneaking != shouldEagle) {
                        sendPacket(
                            C0BPacketEntityAction(
                                player,
                                if (shouldEagle) C0BPacketEntityAction.Action.START_SNEAKING else C0BPacketEntityAction.Action.STOP_SNEAKING
                            )
                        )

                        // Adjust speed when silent sneaking
                        if (adjustedSneakSpeed && shouldEagle) {
                            player.motionX *= eagleSpeed
                            player.motionZ *= eagleSpeed
                        }
                    }

                    eagleSneaking = shouldEagle
                } else {
                    mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    eagleSneaking = shouldEagle
                }
                placedBlocksWithoutEagle = 0
            } else {
                placedBlocksWithoutEagle++
            }
        }

        if (player.onGround) {
            // Still a thing?
            if (scaffoldMode == "Rewinside") {
                MovementUtils.strafe(0.2F)
                player.motionY = 0.0
            }
        }
    }

    val onStrafe = handler<StrafeEvent> {
        val player = mc.thePlayer ?: return@handler

        // Jumping needs to be done here, so it doesn't get detected by movement-sensitive anti-cheats.
        if (scaffoldMode == "Telly" && player.onGround && player.isMoving && currRotation == player.rotation && ticksUntilJump >= jumpTicks) {
            player.tryJump()

            ticksUntilJump = 0
            jumpTicks = randomDelay(minJumpTicks.get(), maxJumpTicks.get())
        }
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.ticksExisted == 1) {
            launchY = player.posY.roundToInt()
        }

        val rotation = RotationUtils.currentRotation

        update()

        val ticks = if (options.keepRotation) {
            if (scaffoldMode == "Telly") 1 else options.resetTicks
        } else {
            if (isGodBridgeEnabled) options.resetTicks else RotationUtils.resetTicks
        }

        if (!Tower.isTowering && isGodBridgeEnabled && options.rotationsActive) {
            generateGodBridgeRotations(ticks)

            return@handler
        }

        if (options.rotationsActive && rotation != null) {
            val placeRotation = this.placeRotation?.rotation ?: rotation

            if (RotationUtils.resetTicks != 0 || options.keepRotation) {
                setRotation(placeRotation, ticks)
            }
        }
    }

    val onTick = handler<GameTickEvent> {
        val target = placeRotation?.placeInfo

        if (extraClicks) {
            val doubleClick = if (simulateDoubleClicking) RandomUtils.nextInt(-1, 1) else 0

            repeat(extraClick.clicks + doubleClick) {
                extraClick.clicks--

                doPlaceAttempt()
            }
        }

        if (target == null) {
            if (placeDelayValue.isActive()) {
                delayTimer.reset()
            }
            return@handler
        }

        val raycastProperly = !(scaffoldMode == "Expand" && expandLength > 1 || shouldGoDown) && options.rotationsActive

        performBlockRaytrace(currRotation, mc.playerController.blockReachDistance).let {
            if (!options.rotationsActive || it != null && it.blockPos == target.blockPos && (!raycastProperly || it.sideHit == target.enumFacing)) {
                val result = if (raycastProperly && it != null) {
                    PlaceInfo(it.blockPos, it.sideHit, it.hitVec)
                } else {
                    target
                }

                place(result)
            }
        }
    }

    val onSneakSlowDown = handler<SneakSlowDownEvent> { event ->
        if (!isEagleEnabled || eagle != "Normal") {
            return@handler
        }

        event.forward *= eagleSpeed / 0.3f
        event.strafe *= eagleSpeed / 0.3f
    }

    val onMovementInput = handler<MovementInputEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (!isGodBridgeEnabled || !player.onGround) return@handler

        if (waitForRots) {
            godBridgeTargetRotation?.run {
                event.originalInput.sneak = event.originalInput.sneak || rotationDifference(this, currRotation) != 0f
            }
        }

        val simPlayer = SimulatedPlayer.fromClientPlayer(event.originalInput)

        simPlayer.tick()

        if (!simPlayer.onGround && !isManualJumpOptionActive || blocksPlacedUntilJump > blocksToJump) {
            event.originalInput.jump = true

            blocksPlacedUntilJump = 0

            blocksToJump = randomDelay(minBlocksToJump.get(), maxBlocksToJump.get())
        }
    }

    fun update() {
        val player = mc.thePlayer ?: return
        val holdingItem = player.heldItem?.item is ItemBlock

        if (!holdingItem && (autoBlock == "Off" || InventoryUtils.findBlockInHotbar() == null)) {
            return
        }

        findBlock(scaffoldMode == "Expand" && expandLength > 1, searchMode == "Area")
    }

    private fun setRotation(rotation: Rotation, ticks: Int) {
        if (scaffoldMode == "Telly" && mc.thePlayer.isMoving) {
            if (offGroundTicks < ticksUntilRotation.get() && ticksUntilJump >= jumpTicks) {
                return
            }
        }

        setTargetRotation(rotation, options, ticks)
    }

    // Search for new target block
    private fun findBlock(expand: Boolean, area: Boolean) {
        val player = mc.thePlayer ?: return

        if (!shouldKeepLaunchPosition) launchY = player.posY.roundToInt()

        val blockPosition = if (shouldGoDown) {
            if (player.posY == player.posY.roundToInt() + 0.5) {
                BlockPos(player.posX, player.posY - 0.6, player.posZ)
            } else {
                BlockPos(player.posX, player.posY - 0.6, player.posZ).down()
            }
        } else if (shouldKeepLaunchPosition && launchY <= player.posY) {
            BlockPos(player.posX, launchY - 1.0, player.posZ)
        } else if (player.posY == player.posY.roundToInt() + 0.5) {
            BlockPos(player)
        } else {
            BlockPos(player).down()
        }

        if (!expand && (!blockPosition.isReplaceable || search(
                blockPosition, !shouldGoDown, area, shouldPlaceHorizontally
            ))
        ) {
            return
        }

        if (expand) {
            val yaw = player.rotationYaw.toRadiansD()
            val x = if (omniDirectionalExpand) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z

            repeat(expandLength) {
                if (search(blockPosition.add(x * it, 0, z * it), false, area)) return
            }
            return
        }

        val (horizontal, vertical) = if (scaffoldMode == "Telly") {
            5 to 3
        } else if (allowClutching) {
            horizontalClutchBlocks.get() to verticalClutchBlocks.get()
        } else {
            1 to 1
        }

        (-horizontal..horizontal).flatMap { x ->
            (0 downTo -vertical).flatMap { y ->
                (-horizontal..horizontal).map { z ->
                    Vec3i(x, y, z)
                }
            }
        }.sortedBy {
            BlockUtils.getCenterDistance(blockPosition.add(it))
        }.forEach {
            if (blockPosition.add(it).canBeClicked() || search(
                    blockPosition.add(it), !shouldGoDown, area, shouldPlaceHorizontally
                )
            ) {
                return
            }
        }
    }

    private fun place(placeInfo: PlaceInfo) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (!delayTimer.hasTimePassed() || shouldKeepLaunchPosition && launchY - 1 != placeInfo.vec3.yCoord.toInt() && scaffoldMode != "Expand") return

        val currentSlot = SilentHotbar.currentSlot

        var stack = player.hotBarSlot(currentSlot).stack

        //TODO: blacklist more blocks than only bushes
        if (stack == null || stack.item !is ItemBlock || (stack.item as ItemBlock).block is BlockBush || stack.stackSize <= 0 || sortByHighestAmount || earlySwitch) {
            val blockSlot = if (sortByHighestAmount) {
                InventoryUtils.findLargestBlockStackInHotbar() ?: return
            } else if (earlySwitch) {
                InventoryUtils.findBlockStackInHotbarGreaterThan(amountBeforeSwitch)
                    ?: InventoryUtils.findBlockInHotbar() ?: return
            } else {
                InventoryUtils.findBlockInHotbar() ?: return
            }

            if (autoBlock != "Off") {
                SilentHotbar.selectSlotSilently(
                    this, blockSlot, immediate = true, render = autoBlock == "Pick", resetManually = true
                )
            }

            stack = player.hotBarSlot(blockSlot).stack
        }

        if ((stack.item as? ItemBlock)?.canPlaceBlockOnSide(
                world, placeInfo.blockPos, placeInfo.enumFacing, player, stack
            ) == false
        ) {
            return
        }

        tryToPlaceBlock(stack, placeInfo.blockPos, placeInfo.enumFacing, placeInfo.vec3)

        if (autoBlock == "Switch") SilentHotbar.resetSlot(this, true)

        // Since we violate vanilla slot switch logic if we send the packets now, we arrange them for the next tick
        switchBlockNextTickIfPossible(stack)

        if (trackCPS) {
            CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
        }
    }

    private fun doPlaceAttempt() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        val stack = player.hotBarSlot(SilentHotbar.currentSlot).stack ?: return

        if (stack.item !is ItemBlock || InventoryUtils.BLOCK_BLACKLIST.contains((stack.item as ItemBlock).block)) {
            return
        }

        val block = stack.item as ItemBlock

        val raytrace = performBlockRaytrace(currRotation, mc.playerController.blockReachDistance) ?: return

        val canPlaceOnUpperFace = block.canPlaceBlockOnSide(
            world, raytrace.blockPos, EnumFacing.UP, player, stack
        )

        val shouldPlace = if (placementAttempt == "Fail") {
            !block.canPlaceBlockOnSide(world, raytrace.blockPos, raytrace.sideHit, player, stack)
        } else {
            if (shouldKeepLaunchPosition) {
                raytrace.blockPos.y == launchY - 1 && !canPlaceOnUpperFace
            } else if (shouldPlaceHorizontally) {
                !canPlaceOnUpperFace
            } else {
                raytrace.blockPos.y <= player.posY.toInt() - 1 && !(raytrace.blockPos.y == player.posY.toInt() - 1 && canPlaceOnUpperFace && raytrace.sideHit == EnumFacing.UP)
            }
        }

        if (!raytrace.typeOfHit.isBlock || !shouldPlace) {
            return
        }

        tryToPlaceBlock(stack, raytrace.blockPos, raytrace.sideHit, raytrace.hitVec, attempt = true)

        // Since we violate vanilla slot switch logic if we send the packets now, we arrange them for the next tick
        switchBlockNextTickIfPossible(stack)

        if (trackCPS) {
            CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
        }
    }

    // Disabling module
    override fun onDisable() {
        val player = mc.thePlayer ?: return

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking && player.isSneaking) {
                //sendPacket(C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING))

                /**
                 * Should prevent false flag by some AntiCheat (Ex: Verus)
                 */
                player.isSneaking = false
            }
        }

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) {
            mc.gameSettings.keyBindRight.pressed = false
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) {
            mc.gameSettings.keyBindLeft.pressed = false
        }

        if (autoF5) {
            mc.gameSettings.thirdPersonView = 0
        }

        placeRotation = null
        mc.timer.timerSpeed = 1f

        SilentHotbar.resetSlot(this)

        options.instant = false
    }

    // Entity movement event
    val onMove = handler<MoveEvent> { event ->
        val player = mc.thePlayer ?: return@handler

        if (!safeWalkValue.isActive() || shouldGoDown) {
            return@handler
        }

        if (airSafe || player.onGround) {
            event.isSafeWalk = true
        }
    }

    val jumpHandler = handler<JumpEvent> { event ->
        if (!jumpStrafe) return@handler

        if (event.eventState == EventState.POST) {
            MovementUtils.strafe(
                if (!isLookingDiagonally) (minJumpStraightStrafe.get()..maxJumpStraightStrafe.get()).random() else (minJumpDiagonalStrafe.get()..maxJumpDiagonalStrafe.get()).random()
            )
        }
    }

    // Visuals
    val onRender3D = handler<Render3DEvent> {
        val player = mc.thePlayer ?: return@handler

        val shouldBother =
            !(shouldGoDown || scaffoldMode == "Expand" && expandLength > 1) && extraClicks && (player.isMoving || MovementUtils.speed > 0.03)

        if (shouldBother) {
            currRotation.let {
                performBlockRaytrace(it, mc.playerController.blockReachDistance)?.let { raytrace ->
                    val timePassed = System.currentTimeMillis() - extraClick.lastClick >= extraClick.delay

                    if (raytrace.typeOfHit.isBlock && timePassed) {
                        extraClick = ExtraClickInfo(
                            TimeUtils.randomClickDelay(extraClickMinCPS, extraClickMaxCPS), System.currentTimeMillis(),
                            extraClick.clicks + 1
                        )
                    }
                }
            }
        }

        if (!mark) {
            return@handler
        }

        repeat(if (scaffoldMode == "Expand") expandLength + 1 else 2) {
            val yaw = player.rotationYaw.toRadiansD()
            val x = if (omniDirectionalExpand) -sin(yaw).roundToInt() else player.horizontalFacing.directionVec.x
            val z = if (omniDirectionalExpand) cos(yaw).roundToInt() else player.horizontalFacing.directionVec.z
            val blockPos = BlockPos(
                player.posX + x * it,
                if (shouldKeepLaunchPosition && launchY <= player.posY) launchY - 1.0 else player.posY - (if (player.posY == player.posY + 0.5) 0.0 else 1.0) - if (shouldGoDown) 1.0 else 0.0,
                player.posZ + z * it
            )
            val placeInfo = PlaceInfo.get(blockPos)

            if (blockPos.isReplaceable && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(68, 117, 255, 100), false)
                return@handler
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param raycast visible
     * @param area spot
     * @return
     */

    fun search(
        blockPosition: BlockPos,
        raycast: Boolean,
        area: Boolean,
        horizontalOnly: Boolean = false,
    ): Boolean {
        val player = mc.thePlayer ?: return false

        options.instant = false

        if (!blockPosition.isReplaceable) {
            if (autoF5) mc.gameSettings.thirdPersonView = 0
            return false
        } else {
            if (autoF5 && mc.gameSettings.thirdPersonView != 1) mc.gameSettings.thirdPersonView = 1
        }

        val maxReach = mc.playerController.blockReachDistance

        val eyes = player.eyes
        var placeRotation: PlaceRotation? = null

        var currPlaceRotation: PlaceRotation?

        for (side in EnumFacing.values().filter { !horizontalOnly || it.axis != EnumFacing.Axis.Y }) {
            val neighbor = blockPosition.offset(side)

            if (!neighbor.canBeClicked()) {
                continue
            }

            if (!area || isGodBridgeEnabled) {
                currPlaceRotation =
                    findTargetPlace(blockPosition, neighbor, Vec3(0.5, 0.5, 0.5), side, eyes, maxReach, raycast)
                        ?: continue

                placeRotation = compareDifferences(currPlaceRotation, placeRotation)
            } else {
                for (x in 0.1..0.9) {
                    for (y in 0.1..0.9) {
                        for (z in 0.1..0.9) {
                            currPlaceRotation =
                                findTargetPlace(blockPosition, neighbor, Vec3(x, y, z), side, eyes, maxReach, raycast)
                                    ?: continue

                            placeRotation = compareDifferences(currPlaceRotation, placeRotation)
                        }
                    }
                }
            }
        }

        placeRotation ?: return false

        if (options.rotationsActive && !isGodBridgeEnabled) {
            val rotationDifference = rotationDifference(placeRotation.rotation, currRotation)
            val rotationDifference2 = rotationDifference(placeRotation.rotation / 90F, currRotation / 90F)

            val simPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput)
            simPlayer.tick()

            // We don't want to use block safe all the time, so we check if it's not needed.
            options.instant =
                blockSafe && simPlayer.fallDistance > player.fallDistance + 0.05 && rotationDifference > rotationDifference2 / 2

            setRotation(placeRotation.rotation, if (scaffoldMode == "Telly") 1 else options.resetTicks)
        }

        this.placeRotation = placeRotation
        return true
    }

    /**
     * For expand scaffold, fixes vector values that should match according to direction vector
     */
    private fun modifyVec(original: Vec3, direction: EnumFacing, pos: Vec3, shouldModify: Boolean): Vec3 {
        if (!shouldModify) {
            return original
        }

        val x = original.xCoord
        val y = original.yCoord
        val z = original.zCoord

        val side = direction.opposite

        return when (side.axis ?: return original) {
            EnumFacing.Axis.Y -> Vec3(x, pos.yCoord + side.directionVec.y.coerceAtLeast(0), z)
            EnumFacing.Axis.X -> Vec3(pos.xCoord + side.directionVec.x.coerceAtLeast(0), y, z)
            EnumFacing.Axis.Z -> Vec3(x, y, pos.zCoord + side.directionVec.z.coerceAtLeast(0))
        }

    }

    private fun findTargetPlace(
        pos: BlockPos, offsetPos: BlockPos, vec3: Vec3, side: EnumFacing, eyes: Vec3, maxReach: Float, raycast: Boolean,
    ): PlaceRotation? {
        val world = mc.theWorld ?: return null

        val vec = (Vec3(pos) + vec3).addVector(
            side.directionVec.x * vec3.xCoord, side.directionVec.y * vec3.yCoord, side.directionVec.z * vec3.zCoord
        )

        val distance = eyes.distanceTo(vec)

        if (raycast && (distance > maxReach || world.rayTraceBlocks(eyes, vec, false, true, false) != null)) {
            return null
        }

        val diff = vec - eyes

        if (side.axis != EnumFacing.Axis.Y) {
            val dist = abs(if (side.axis == EnumFacing.Axis.Z) diff.zCoord else diff.xCoord)

            if (dist < minDist && scaffoldMode != "Telly") {
                return null
            }
        }

        var rotation = toRotation(vec, false)

        val roundYaw90 = round(rotation.yaw / 90f) * 90f
        val roundYaw45 = round(rotation.yaw / 45f) * 45f

        rotation = when (options.rotationMode) {
            "Stabilized" -> Rotation(roundYaw45, rotation.pitch)
            "ReverseYaw" -> Rotation(if (!isLookingDiagonally) roundYaw90 else roundYaw45, rotation.pitch)
            else -> rotation
        }.fixedSensitivity()

        // If the current rotation already looks at the target block and side, then return right here
        performBlockRaytrace(currRotation, maxReach)?.let { raytrace ->
            if (raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite)) {
                return PlaceRotation(
                    PlaceInfo(
                        raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                    ), currRotation
                )
            }
        }

        val raytrace = performBlockRaytrace(rotation, maxReach) ?: return null

        val multiplier = if (options.legitimize) 3 else 1

        if (raytrace.blockPos == offsetPos && (!raycast || raytrace.sideHit == side.opposite) && canUpdateRotation(
                currRotation, rotation, multiplier
            )
        ) {
            return PlaceRotation(
                PlaceInfo(
                    raytrace.blockPos, side.opposite, modifyVec(raytrace.hitVec, side, Vec3(offsetPos), !raycast)
                ), rotation
            )
        }

        return null
    }

    private fun performBlockRaytrace(rotation: Rotation, maxReach: Float): MovingObjectPosition? {
        val player = mc.thePlayer ?: return null
        val world = mc.theWorld ?: return null

        val eyes = player.eyes
        val rotationVec = getVectorForRotation(rotation)

        val reach = eyes + (rotationVec * maxReach.toDouble())

        return world.rayTraceBlocks(eyes, reach, false, false, true)
    }

    private fun compareDifferences(
        new: PlaceRotation, old: PlaceRotation?, rotation: Rotation = currRotation,
    ): PlaceRotation {
        if (old == null || rotationDifference(new.rotation, rotation) < rotationDifference(old.rotation, rotation)) {
            return new
        }

        return old
    }

    private fun switchBlockNextTickIfPossible(stack: ItemStack) {
        if (autoBlock in arrayOf("Off", "Switch")) return

        val switchAmount = if (earlySwitch) amountBeforeSwitch else 0

        if (stack.stackSize > switchAmount) return

        val switchSlot = if (earlySwitch) {
            InventoryUtils.findBlockStackInHotbarGreaterThan(amountBeforeSwitch) ?: InventoryUtils.findBlockInHotbar()
            ?: return
        } else {
            InventoryUtils.findBlockInHotbar()
        } ?: return

        SilentHotbar.selectSlotSilently(this, switchSlot, render = autoBlock == "Pick", resetManually = true)
    }

    private fun updatePlacedBlocksForTelly() {
        if (blocksUntilAxisChange > horizontalPlacements + verticalPlacements) {
            blocksUntilAxisChange = 0

            horizontalPlacements = randomDelay(minHorizontalPlacements.get(), maxHorizontalPlacements.get())
            verticalPlacements = randomDelay(minVerticalPlacements.get(), maxVerticalPlacements.get())
            return
        }

        blocksUntilAxisChange++
    }

    private fun tryToPlaceBlock(
        stack: ItemStack,
        clickPos: BlockPos,
        side: EnumFacing,
        hitVec: Vec3,
        attempt: Boolean = false,
    ): Boolean {
        val thePlayer = mc.thePlayer ?: return false

        val prevSize = stack.stackSize

        val clickedSuccessfully = thePlayer.onPlayerRightClick(clickPos, side, hitVec, stack)

        if (clickedSuccessfully) {
            if (!attempt) {
                delayTimer.reset()

                if (thePlayer.onGround) {
                    thePlayer.motionX *= speedModifier
                    thePlayer.motionZ *= speedModifier
                }
            }

            if (swing) thePlayer.swingItem()
            else sendPacket(C0APacketAnimation())

            if (isManualJumpOptionActive) blocksPlacedUntilJump++

            updatePlacedBlocksForTelly()

            if (stack.stackSize <= 0) {
                thePlayer.inventory.mainInventory[SilentHotbar.currentSlot] = null
                ForgeEventFactory.onPlayerDestroyItem(thePlayer, stack)
            } else if (stack.stackSize != prevSize || mc.playerController.isInCreativeMode) mc.entityRenderer.itemRenderer.resetEquippedProgress()

            placeRotation = null
        } else {
            if (thePlayer.sendUseItem(stack)) mc.entityRenderer.itemRenderer.resetEquippedProgress2()
        }

        return clickedSuccessfully
    }

    fun handleMovementOptions(input: MovementInput) {
        val player = mc.thePlayer ?: return

        if (!state) {
            return
        }

        if (!slow && speedLimiter && MovementUtils.speed > speedLimit) {
            input.moveStrafe = 0f
            input.moveForward = 0f
            return
        }

        when (zitterMode.lowercase()) {
            "off" -> {
                return
            }

            "smooth" -> {
                val notOnGround = !player.onGround || !player.isCollidedVertically

                if (player.onGround) {
                    input.sneak = eagleSneaking || GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
                }

                if (input.jump || mc.gameSettings.keyBindJump.isKeyDown || notOnGround) {
                    zitterTickTimer.reset()

                    if (useSneakMidAir) {
                        input.sneak = true
                    }

                    if (!notOnGround && !input.jump) {
                        // Attempt to move against the direction
                        input.moveStrafe = if (zitterDirection) 1f else -1f
                    } else {
                        input.moveStrafe = 0f
                    }

                    zitterDirection = !zitterDirection

                    // Recreate input in case the user was indeed pressing inputs
                    if (mc.gameSettings.keyBindLeft.isKeyDown) {
                        input.moveStrafe++
                    }

                    if (mc.gameSettings.keyBindRight.isKeyDown) {
                        input.moveStrafe--
                    }
                    return
                }

                if (zitterTickTimer.hasTimePassed()) {
                    zitterDirection = !zitterDirection
                    zitterTickTimer.reset()
                } else {
                    zitterTickTimer.update()
                }

                if (zitterDirection) {
                    input.moveStrafe = -1f
                } else {
                    input.moveStrafe = 1f
                }
            }

            "teleport" -> {
                MovementUtils.strafe(zitterSpeed)
                val yaw = (player.rotationYaw + if (zitterDirection) 90.0 else -90.0).toRadians()
                player.motionX -= sin(yaw) * zitterStrength
                player.motionZ += cos(yaw) * zitterStrength
                zitterDirection = !zitterDirection
            }
        }
    }

    private var isOnRightSide = false

    /**
     * God-bridge rotation generation method from Nextgen
     *
     * Credits to @opZywk
     */
    private fun generateGodBridgeRotations(ticks: Int) {
        val player = mc.thePlayer ?: return

        val direction = if (options.applyServerSide) {
            MovementUtils.direction.toDegreesF() + 180f
        } else MathHelper.wrapAngleTo180_float(player.rotationYaw)

        val movingYaw = round(direction / 45) * 45

        val steps45 = arrayListOf(-135f, -45f, 45f, 135f)

        val isMovingStraight = if (options.applyServerSide) {
            movingYaw % 90 == 0f
        } else movingYaw in steps45 && player.movementInput.isSideways

        if (!player.isNearEdge(2.5f)) return

        if (!player.isMoving) {
            placeRotation?.run {
                val axisMovement = floor(this.rotation.yaw / 90) * 90

                val yaw = axisMovement + 45f
                val pitch = 75f

                setRotation(Rotation(yaw, pitch), ticks)
                return
            }

            if (!options.keepRotation) return
        }

        val rotation = if (isMovingStraight) {
            if (player.onGround) {
                isOnRightSide = floor(player.posX + cos(movingYaw.toRadians()) * 0.5) != floor(player.posX) || floor(
                    player.posZ + sin(movingYaw.toRadians()) * 0.5
                ) != floor(player.posZ)

                val posInDirection =
                    BlockPos(player.positionVector.offset(EnumFacing.fromAngle(movingYaw.toDouble()), 0.6))

                val isLeaningOffBlock = player.position.down().block == air
                val nextBlockIsAir = posInDirection.down().block == air

                if (isLeaningOffBlock && nextBlockIsAir) {
                    isOnRightSide = !isOnRightSide
                }
            }

            val side = if (options.applyServerSide) {
                if (isOnRightSide) 45f else -45f
            } else 0f

            Rotation(movingYaw + side, if (useOptimizedPitch) 73.5f else customGodPitch)
        } else {
            Rotation(movingYaw, 75.6f)
        }.fixedSensitivity()

        godBridgeTargetRotation = rotation

        setRotation(rotation, ticks)
    }

    override val tag
        get() = if (towerMode != "None") ("$scaffoldMode | $towerMode") else scaffoldMode

    data class ExtraClickInfo(val delay: Int, val lastClick: Long, var clicks: Int)
}
