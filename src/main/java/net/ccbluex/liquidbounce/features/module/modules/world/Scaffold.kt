/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.injection.access.StaticStorage
import net.ccbluex.liquidbounce.ui.i18n.LanguageManager
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils.isReplaceable
import net.ccbluex.liquidbounce.utils.block.PlaceInfo
import net.ccbluex.liquidbounce.utils.block.PlaceInfo.Companion.get
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithServerSideRotation
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TickTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.minecraft.block.BlockAir
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.stats.StatList
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.*

@ModuleInfo(name = "Scaffold", category = ModuleCategory.WORLD, keyBind = Keyboard.KEY_V)
class Scaffold : Module() {
    
    // Block place
    private val placeOptions = BoolValue("Placement Options: ", true)
    
    private val placeableDelayValue = ListValue("PlaceableDelay", arrayOf("Normal", "Smart", "OFF"), "Normal").displayable { placeOptions.get() }
    private val placeDelayTower = BoolValue("PlaceableDelayWhenTowering", true).displayable { placeOptions.get() }
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minDelayValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { !placeableDelayValue.equals("OFF") && placeableDelayValue.displayable } as IntegerValue
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 0, 0, 1000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxDelayValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !placeableDelayValue.equals("OFF") && placeableDelayValue.displayable } as IntegerValue
    
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal").displayable { placeOptions.get() }
    private val searchValue = BoolValue("Search", true).displayable { placeOptions.get() }
    private val downValue = BoolValue("Down", false).displayable { placeOptions.get() }
    private val placeModeValue = ListValue("PlaceTiming", arrayOf("All", "Pre", "Post"), "All").displayable { placeOptions.get() }
    
    private val sameYValue = ListValue("SameY", arrayOf("Simple", "AutoJump", "WhenSpeed", "JumpUpY", "MotionY", "OFF"), "WhenSpeed").displayable { placeOptions.get() }
    private val hitableCheckValue = ListValue("HitableCheck", arrayOf("Simple", "Strict", "OFF"), "Simple").displayable { placeOptions.get() }
    private val expandLengthValue = IntegerValue("ExpandLength", 1, 1, 6).displayable { placeOptions.get() }
   
    
    // Movement
    private val moveOptions = BoolValue("Movement Options: ", true)
    
    private val sprintValue = ListValue("Sprint", arrayOf("Always", "Dynamic", "OnGround", "OffGround", "Alternating", "Hypixel", "OFF"), "Always").displayable { moveOptions.get() }
    
    private val safeWalkValue = ListValue("SafeWalk", arrayOf("Ground", "Air", "OFF"), "Ground").displayable { moveOptions.get() }
    private val eagleValue = ListValue("Eagle", arrayOf("Silent", "Normal", "Off"), "Off").displayable { moveOptions.get() }
    private val blocksToEagleValue = IntegerValue("BlocksToEagle", 0, 0, 10).displayable { !eagleValue.equals("Off") && eagleValue.displayable }
    private val edgeDistanceValue = FloatValue("EagleEdgeDistance", 0f, 0f, 0.5f).displayable { !eagleValue.equals("Off") && eagleValue.displayable }

    private val timerValue = FloatValue("Timer", 1f, 0.1f, 5f).displayable { moveOptions.get() }
    private val motionSpeedEnabledValue = BoolValue("MotionSpeedSet", false).displayable { moveOptions.get() }
    private val motionSpeedValue = FloatValue("MotionSpeed", 0.15f, 0.05f, 0.5f).displayable { motionSpeedEnabledValue.get() && motionSpeedEnabledValue.displayable }
    private val speedModifierValue = FloatValue("SpeedModifier", 1f, 0f, 2f).displayable { moveOptions.get() }
    
    
    // Tower
    private val towerModeValue = ListValue(
        "TowerMode", arrayOf(
            "Jump",
            "Motion", "Motion2", "Motion3",
            "ConstantMotion", "PlusMotion", "StableMotion",
            "MotionTP", "MotionTP2",
            "Packet", "Teleport",
            "AAC3.3.9", "AAC3.6.4", "AAC4.4Constant",  "AAC4Jump",
            "Universocraft",
            "Matrix6.9.2",
            "Verus",
            "NCP"
        ), "MotionTP2"
    ).displayable { moveOptions.get() }
    private val stopWhenBlockAboveValue = BoolValue("StopTowerWhenBlockAbove", true).displayable { moveOptions.get() }
    private val towerFakeJumpValue = BoolValue("TowerFakeJump", true).displayable { moveOptions.get() }
    private val towerActiveValue = ListValue("TowerActivation", arrayOf("Always", "PressSpace", "NoMove", "OFF"), "PressSpace").displayable { moveOptions.get() }
    private val towerTimerValue = FloatValue("TowerTimer", 1f, 0.1f, 5f).displayable { moveOptions.get() }
    

    // Jump mode
    private val jumpMotionValue = FloatValue("TowerJumpMotion", 0.42f, 0.3681289f, 0.79f).displayable { towerModeValue.equals("Jump") && towerModeValue.displayable }
    private val jumpDelayValue = IntegerValue("TowerJumpDelay", 0, 0, 20).displayable { towerModeValue.equals("Jump") && towerModeValue.displayable }

    // Stable/PlusMotion
    private val stableMotionValue = FloatValue("TowerStableMotion", 0.42f, 0.1f, 1f).displayable { towerModeValue.equals("StableMotion") && towerModeValue.displayable }
    private val plusMotionValue = FloatValue("TowerPlusMotion", 0.1f, 0.01f, 0.2f).displayable { towerModeValue.equals("PlusMotion") && towerModeValue.displayable }
    private val plusMaxMotionValue = FloatValue("TowerPlusMaxMotion", 0.8f, 0.1f, 2f).displayable { towerModeValue.equals("PlusMotion") && towerModeValue.displayable }

    // ConstantMotion
    private val constantMotionValue = FloatValue("TowerConstantMotion", 0.42f, 0.1f, 1f).displayable { towerModeValue.equals("ConstantMotion") && towerModeValue.displayable }
    private val constantMotionJumpGroundValue = FloatValue("TowerConstantMotionJumpGround", 0.79f, 0.76f, 1f).displayable { towerModeValue.equals("ConstantMotion") && towerModeValue.displayable }

    // Teleport
    private val teleportHeightValue = FloatValue("TowerTeleportHeight", 1.15f, 0.1f, 5f).displayable { towerModeValue.equals("Teleport") && towerModeValue.displayable }
    private val teleportDelayValue = IntegerValue("TowerTeleportDelay", 0, 0, 20).displayable { towerModeValue.equals("Teleport") && towerModeValue.displayable }
    private val teleportGroundValue = BoolValue("TowerTeleportGround", true).displayable { towerModeValue.equals("Teleport") && towerModeValue.displayable }
    private val teleportNoMotionValue = BoolValue("TowerTeleportNoMotion", false).displayable { towerModeValue.equals("Teleport") && towerModeValue.displayable }
    
   
    private val rotOptions = BoolValue("Rotation Options: ", true)

    // Rotations
    private val rotationsValue = ListValue("Rotations", arrayOf("None", "Better", "Vanilla", "AAC", "Static1", "Static2", "Custom", "Advanced", "Backwards", "Snap", "BackSnap"), "Backwards").displayable { rotOptions.get() }
    private val towerrotationsValue = ListValue("TowerRotations", arrayOf("None", "Better", "Vanilla", "AAC", "Static1", "Static2", "Custom"), "AAC").displayable { rotOptions.get() }
    private val advancedYawModeValue = ListValue("AdvancedYawRotations", arrayOf("Offset", "Static", "RoundStatic", "Vanilla", "Round", "MoveDirection", "OffsetMove"), "MoveDirection").displayable { rotationsValue.equals("Advanced") && rotationsValue.displayable}
    private val advancedPitchModeValue = ListValue("AdvancedPitchRotations", arrayOf("Offset", "Static", "Vanilla"), "Static").displayable { rotationsValue.equals("Advanced") && rotationsValue.displayable }
    private val advancedYawOffsetValue = IntegerValue("AdvancedOffsetYaw", -15, -180, 180).displayable { rotationsValue.equals("Advanced") && advancedYawModeValue.equals("Offset") && rotationsValue.displayable }
    private val advancedYawMoveOffsetValue = IntegerValue("AdvancedMoveOffsetYaw", -15, -180, 180).displayable { rotationsValue.equals("Advanced") && advancedYawModeValue.equals("Offset") && rotationsValue.displayable }
    private val advancedYawStaticValue = IntegerValue("AdvancedStaticYaw", 145, -180, 180).displayable { rotationsValue.equals("Advanced") && (advancedYawModeValue.equals("Static") || advancedYawModeValue.equals("RoundStatic")) && rotationsValue.displayable }
    private val advancedYawRoundValue = IntegerValue("AdvancedYawRoundValue", 45, 0, 180).displayable { rotationsValue.equals("Advanced") && (advancedYawModeValue.equals("Round") || advancedYawModeValue.equals("RoundStatic")) && rotationsValue.displayable }
    private val advancedPitchOffsetValue = FloatValue("AdvancedOffsetPitch", -0.4f, -90f, 90f).displayable { rotationsValue.equals("Advanced") && advancedPitchModeValue.equals("Offset") && rotationsValue.displayable }
    private val advancedPitchStaticValue = FloatValue("AdvancedStaticPitch", 82.4f, -90f, 90f).displayable { rotationsValue.equals("Advanced") && advancedPitchModeValue.equals("Static") && rotationsValue.displayable }
    private val aacYawValue = IntegerValue("AACYawOffset", 0, 0, 90).displayable { rotationsValue.equals("AAC") && rotationsValue.displayable }
    private val customYawValue = IntegerValue("CustomYaw", -145, -180, 180).displayable { rotationsValue.equals("Custom") || rotationsValue.equals("Better") && rotationsValue.displayable }
    private val customPitchValue = FloatValue("CustomPitch", 82.4f, -90f, 90f).displayable { rotationsValue.equals("Custom") && rotationsValue.displayable }
    private val customtowerYawValue = IntegerValue("CustomTowerYaw", -145, -180, 180).displayable { towerrotationsValue.equals("Custom") || towerrotationsValue.equals("Better") && towerrotationsValue.displayable }
    private val customtowerPitchValue = FloatValue("CustomTowerPitch", 79f, -90f, 90f).displayable { towerrotationsValue.equals("Custom") && towerrotationsValue.displayable }
    // private val tolleyBridgeValue = IntegerValue("TolleyBridgeTick", 0, 0, 10)
    // private val tolleyYawValue = IntegerValue("TolleyYaw", 0, 0, 90)
    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotationsValue.equals("None") && rotationsValue.displayable }
    private val minRotationSpeedValue: IntegerValue = object : IntegerValue("MinRotationSpeed", 80, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = maxRotationSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { !rotationsValue.equals("None") && rotationsValue.displayable } as IntegerValue
    private val maxRotationSpeedValue: IntegerValue = object : IntegerValue("MaxRotationSpeed", 100, 0, 180) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val v = minRotationSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { !rotationsValue.equals("None") && rotationsValue.displayable } as IntegerValue
    private val keepLengthValue = IntegerValue("KeepRotationTick", 1, 0, 20).displayable { !rotationsValue.equals("None") && rotationsValue.displayable }

    private val bypassOptions = BoolValue("Bypass Options: ", true)
    
    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Spoof", "LiteSpoof", "Switch", "OFF"), "Spoof").displayable { bypassOptions.get() }
    
    private val zitterModeValue = ListValue("ZitterMode", arrayOf("Teleport", "Smooth", "OFF"), "OFF").displayable { bypassOptions.get() }
    private val zitterSpeedValue = FloatValue("ZitterSpeed", 0.13f, 0.1f, 0.3f).displayable { !zitterModeValue.equals("OFF") && zitterModeValue.displayable }
    private val zitterStrengthValue = FloatValue("ZitterStrength", 0.072f, 0.05f, 0.2f).displayable { !zitterModeValue.equals("OFF") && zitterModeValue.displayable }
    
    private val extraClickValue = ListValue("ExtraClick", arrayOf("EmptyC08", "AfterPlace", "RayTrace", "OFF"), "OFF").displayable { bypassOptions.get() }
    private val extraClickMaxDelayValue: IntegerValue = object : IntegerValue("ExtraClickMaxDelay", 100, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMinDelayValue.get()
            if (i > newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") && extraClickValue.displayable } as IntegerValue
    private val extraClickMinDelayValue: IntegerValue = object : IntegerValue("ExtraClickMinDelay", 50, 20, 300) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = extraClickMaxDelayValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !extraClickValue.equals("OFF") && extraClickValue.displayable } as IntegerValue
    
    private val moveFixValue = BoolValue("StrafeFix", false).displayable { bypassOptions.get() }

    
    // Visuals
    private val renderOptions = BoolValue("Render Options:", true)
    
    private val counterDisplayValue = ListValue("Counter", arrayOf("FDP", "Rise", "Rise6", "Simple"), "FDP").displayable { renderOptions.get() }
    private val markValue = BoolValue("Mark", false).displayable { renderOptions.get() }
    private val markRedValue = IntegerValue("MarkColorRed", 68, 0, 255).displayable { markValue.get() && markValue.displayable }
    private val markGreenValue = IntegerValue("MarkColorGreen", 117, 0, 255).displayable { markValue.get() && markValue.displayable }
    private val markBlueValue = IntegerValue("MarkColorBlue", 255, 0, 255).displayable { markValue.get() && markValue.displayable }

    /**
     * MODULE
     */

    private var progress = 0f
    private var lastMS = 0L


    // Target block
    private var targetPlace: PlaceInfo? = null

    // Last OnGround position
    private var lastGroundY = 0

    // Rotation lock
    private var lockRotation: Rotation? = null

    // Auto block slot
    private var slot = 0

    // Zitter Smooth
    private var zitterDirection = false

    // Delay
    private val delayTimer = MSTimer()
    private val zitterTimer = MSTimer()
    private val clickTimer = MSTimer()
    private val towerTimer = TickTimer()
    private var delay: Long = 0
    private var clickDelay: Long = 0
    private var lastPlace = 0

    // Eagle
    private var placedBlocksWithoutEagle = 0
    private var eagleSneaking = false

    // Down
    private var shouldGoDown = false
    private var jumpGround = 0.0
    private var towerStatus = false
    private var canSameY = false
    private var lastPlaceBlock: BlockPos? = null
    private var afterPlaceC08: C08PacketPlayerBlockPlacement? = null

    //Other
    private var doSpoof = false
    

    //NCP
    private var offGroundTicks: Int = 0
    /**
     * Enable module
     */
    override fun onEnable() {
        slot = mc.thePlayer.inventory.currentItem
        doSpoof = false
        if (mc.thePlayer == null) return
        lastGroundY = mc.thePlayer.posY.toInt()
        lastPlace = 2
        clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
        delayTimer.reset()
        zitterTimer.reset()
        clickTimer.reset()
    }

    /**
     * Update event
     *
     * @param event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // if(!mc.thePlayer.onGround) tolleyStayTick=0
        //    else tolleyStayTick++
        // if(tolleyStayTick>100) tolleyStayTick=100
        if (towerStatus && towerModeValue.get().lowercase() != "aac3.3.9" && towerModeValue.get().lowercase() != "aac4.4constant" && towerModeValue.get().lowercase() != "aac4jump") mc.timer.timerSpeed = towerTimerValue.get()
        if (!towerStatus) mc.timer.timerSpeed = timerValue.get()
        if (towerStatus || mc.thePlayer.isCollidedHorizontally) {
            canSameY = false
            lastGroundY = mc.thePlayer.posY.toInt()
        } else {
            when (sameYValue.get().lowercase()) {
                "simple" -> {
                    canSameY = true
                }
                "autojump" -> {
                    canSameY = true
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                    }
                }
                "motiony" -> {
                    canSameY = true
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) mc.thePlayer.motionY = 0.42
                }
                "jumpupy" -> {
                    canSameY = false
                    if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                    }
                }
                "whenspeed" -> {
                    canSameY = FDPClient.moduleManager[Speed::class.java]!!.state
                }
                else -> {
                    canSameY = false
                }
            }
            if (mc.thePlayer.onGround) {
                lastGroundY = mc.thePlayer.posY.toInt()
            }
        }

        if (clickTimer.hasTimePassed(clickDelay)) {
            fun sendPacket(c08: C08PacketPlayerBlockPlacement) {
                if (clickDelay < 35) {
                    mc.netHandler.addToSendQueue(c08)
                }
                if (clickDelay < 50) {
                    mc.netHandler.addToSendQueue(c08)
                }
                mc.netHandler.addToSendQueue(c08)
            }
            when (extraClickValue.get().lowercase()) {
                "emptyc08" -> sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                "afterplace" -> {
                    if (afterPlaceC08 != null) {
                        if (mc.thePlayer.getDistanceSqToCenter(lastPlaceBlock) <10) {
                            sendPacket(afterPlaceC08!!)
                        } else {
                            afterPlaceC08 = null
                        }
                    }
                }
                "raytrace" -> {
                    val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
                    if (rayTraceInfo != null && BlockUtils.getBlock(rayTraceInfo.blockPos) != Blocks.air) {
                        val blockPos = rayTraceInfo.blockPos
                        val hitVec = rayTraceInfo.hitVec
                        val directionVec = rayTraceInfo.sideHit.directionVec
                        val targetPos = rayTraceInfo.blockPos.add(directionVec.x, directionVec.y, directionVec.z)
                        if (mc.thePlayer.entityBoundingBox.intersectsWith(Blocks.stone.getSelectedBoundingBox(mc.theWorld, targetPos))) {
                            sendPacket(C08PacketPlayerBlockPlacement(blockPos, rayTraceInfo.sideHit.index, mc.thePlayer.inventory.getStackInSlot(slot), (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat()))
                        } else {
                            sendPacket(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)))
                        }
                    }
                }
            }
            clickDelay = TimeUtils.randomDelay(extraClickMinDelayValue.get(), extraClickMaxDelayValue.get())
            clickTimer.reset()
        }

        mc.thePlayer.isSprinting = canSprint
        if (sprintValue.equals("Hypixel")) {
            mc.thePlayer.motionX *= 0.8
            mc.thePlayer.motionZ *= 0.8
        }

        if (mc.thePlayer.onGround) {
            offGroundTicks = 0
        } else offGroundTicks++

        shouldGoDown = downValue.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && blocksAmount > 1
        if (shouldGoDown) mc.gameSettings.keyBindSneak.pressed = false
        if (mc.thePlayer.onGround) {
            // Smooth Zitter
            if (zitterModeValue.equals("smooth")) {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
                if (zitterTimer.hasTimePassed(100)) {
                    zitterDirection = !zitterDirection
                    zitterTimer.reset()
                }
                if (zitterDirection) {
                    mc.gameSettings.keyBindRight.pressed = true
                    mc.gameSettings.keyBindLeft.pressed = false
                } else {
                    mc.gameSettings.keyBindRight.pressed = false
                    mc.gameSettings.keyBindLeft.pressed = true
                }
            }

            // Eagle
            if (!eagleValue.get().equals("Off", true) && !shouldGoDown) {
                var dif = 0.5
                val blockPos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)
                if (edgeDistanceValue.get() > 0) {
                    for (facingType in EnumFacing.values()) {
                        if (facingType == EnumFacing.UP || facingType == EnumFacing.DOWN) {
                            continue
                        }
                        val neighbor = blockPos.offset(facingType)
                        if (isReplaceable(neighbor)) {
                            val calcDif = (if (facingType == EnumFacing.NORTH || facingType == EnumFacing.SOUTH) {
                                abs((neighbor.z + 0.5) - mc.thePlayer.posZ)
                            } else {
                                abs((neighbor.x + 0.5) - mc.thePlayer.posX)
                            }) - 0.5

                            if (calcDif < dif) {
                                dif = calcDif
                            }
                        }
                    }
                }
                if (placedBlocksWithoutEagle >= blocksToEagleValue.get()) {
                    val shouldEagle =
                        isReplaceable(blockPos) || (edgeDistanceValue.get() > 0 && dif < edgeDistanceValue.get())
                    if (eagleValue.get().equals("Silent", true)) {
                        if (eagleSneaking != shouldEagle) {
                            mc.netHandler.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer, if (shouldEagle) {
                                        C0BPacketEntityAction.Action.START_SNEAKING
                                    } else {
                                        C0BPacketEntityAction.Action.STOP_SNEAKING
                                    }
                                )
                            )
                        }
                        eagleSneaking = shouldEagle
                    } else {
                        mc.gameSettings.keyBindSneak.pressed = shouldEagle
                    }
                    placedBlocksWithoutEagle = 0
                } else {
                    placedBlocksWithoutEagle++
                }
            }
            // Zitter
            if (zitterModeValue.equals("teleport")) {
                MovementUtils.strafe(zitterSpeedValue.get())
                val yaw = Math.toRadians(mc.thePlayer.rotationYaw + if (zitterDirection) 90.0 else -90.0)
                mc.thePlayer.motionX -= sin(yaw) * zitterStrengthValue.get()
                mc.thePlayer.motionZ += cos(yaw) * zitterStrengthValue.get()
                zitterDirection = !zitterDirection
            }
        }
        
        if (placeModeValue.equals("All")) place()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null) return
        val packet = event.packet

        //Verus
        if (packet is C03PacketPlayer) {
            if (doSpoof) {
                doSpoof = false
                packet.onGround = true
            }
        }

        // AutoBlock
        if (packet is C09PacketHeldItemChange) {
            if(packet.slotId == slot) {
                event.cancelEvent()
            } else {
                slot = packet.slotId
            }
        } else if (packet is C08PacketPlayerBlockPlacement) {
            // c08 item override to solve issues in scaffold and some other modules, maybe bypass some anticheat in future
            packet.stack = mc.thePlayer.inventory.mainInventory[slot]
            // illegal facing checks
            packet.facingX = packet.facingX.coerceIn(-1.0000F, 1.0000F)
            packet.facingY = packet.facingY.coerceIn(-1.0000F, 1.0000F)
            packet.facingZ = packet.facingZ.coerceIn(-1.0000F, 1.0000F)
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val eventState = event.eventState
        towerStatus = false
        // Tower
        if (motionSpeedEnabledValue.get()) MovementUtils.setMotion(motionSpeedValue.get().toDouble())
        towerStatus = (!stopWhenBlockAboveValue.get() || BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 2, mc.thePlayer.posZ)) is BlockAir)
        if (towerStatus) {
            // further checks
            when (towerActiveValue.get().lowercase()) {
                "off" -> towerStatus = false
                "always" -> {
                    towerStatus = (mc.gameSettings.keyBindLeft.isKeyDown ||
                            mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown ||
                            mc.gameSettings.keyBindBack.isKeyDown)
                }
                "pressspace" -> {
                    towerStatus = mc.gameSettings.keyBindJump.isKeyDown
                }
                "nomove" -> {
                    towerStatus = !(mc.gameSettings.keyBindLeft.isKeyDown ||
                            mc.gameSettings.keyBindRight.isKeyDown || mc.gameSettings.keyBindForward.isKeyDown ||
                            mc.gameSettings.keyBindBack.isKeyDown) && mc.gameSettings.keyBindJump.isKeyDown
                }
            }
        }
        if (towerStatus) move()

        // Lock Rotation
        if (rotationsValue.get() != "None" && keepLengthValue.get()> 0 && lockRotation != null && silentRotationValue.get()) {
            if (rotationsValue.equals("BackSnap")) {
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180), 80.0f), rotationSpeed)
                RotationUtils.setTargetRotation(limitedRotation, 1)
            } else {
                val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation, rotationSpeed)
                RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
            }
        }
        

        // Update and search for new block
        if (event.eventState == EventState.PRE) update()

        // Place block
        if (placeModeValue.equals(eventState.stateName)) place()

        // Reset placeable delay
        if (targetPlace == null && !placeableDelayValue.equals("OFF") && (!placeDelayTower.get() || !towerStatus)) {
            if (placeableDelayValue.equals("Smart")) {
                if (lastPlace == 0) {
                    delayTimer.reset()
                }
            } else {
                delayTimer.reset()
            }
        }
        
        FDPClient.moduleManager[StrafeFix::class.java]!!.applyForceStrafe(!rotationsValue.equals("None"), moveFixValue.get())
    }

    private fun fakeJump() {
        if (!towerFakeJumpValue.get()) {
            return
        }

        mc.thePlayer.isAirBorne = true
        mc.thePlayer.triggerAchievement(StatList.jumpStat)
    }

    private fun move() {
        towerTimer.update()
        when (towerModeValue.get().lowercase()) {
            "none" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.42
                }
            }
            "jump" -> {
                if (mc.thePlayer.onGround && towerTimer.hasTimePassed(jumpDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.motionY = jumpMotionValue.get().toDouble()
                    towerTimer.reset()
                }
            }
            "motion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.1) {
                    mc.thePlayer.motionY = -0.3
                }
            }
            "motion2" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.18) {
                    mc.thePlayer.motionY -= 0.02
                }
            }
            "motion3" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.1) {
                    mc.thePlayer.motionY = -0.08 * 0.98
                }
            }
            "ncp" -> {
                if (mc.thePlayer.posY % 1 <= 0.00153598) {
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX,
                        Math.floor(mc.thePlayer.posY),
                        mc.thePlayer.posZ
                    )
                    mc.thePlayer.motionY = 0.41998
                } else if (mc.thePlayer.posY % 1 < 0.1 && offGroundTicks != 0) {
                    mc.thePlayer.setPosition(
                        mc.thePlayer.posX,
                        Math.floor(mc.thePlayer.posY),
                        mc.thePlayer.posZ
                    )
                }
            }
            "motiontp" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.23) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                }
            }
            "motiontp2" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.23) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                    mc.thePlayer.onGround = true
                    mc.thePlayer.motionY = 0.41999998688698
                }
            }
            "packet" -> {
                if (mc.thePlayer.onGround && towerTimer.hasTimePassed(2)) {
                    fakeJump()
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42, mc.thePlayer.posZ, false))
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.753, mc.thePlayer.posZ, false))
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.0, mc.thePlayer.posZ)
                    towerTimer.reset()
                }
            }
            "teleport" -> {
                if (teleportNoMotionValue.get()) mc.thePlayer.motionY = 0.0
                if ((mc.thePlayer.onGround || !teleportGroundValue.get()) && towerTimer.hasTimePassed(teleportDelayValue.get())) {
                    fakeJump()
                    mc.thePlayer.setPositionAndUpdate(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + teleportHeightValue.get(),
                        mc.thePlayer.posZ
                    )
                    towerTimer.reset()
                }
            }
            "constantmotion" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                }
                if (mc.thePlayer.posY > jumpGround + constantMotionJumpGroundValue.get()) {
                    fakeJump()
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                    mc.thePlayer.motionY = constantMotionValue.get().toDouble()
                    jumpGround = mc.thePlayer.posY
                }
            }
            "plusmotion" -> {
                mc.thePlayer.motionY += plusMotionValue.get()
                if (mc.thePlayer.motionY >= plusMaxMotionValue.get()) {
                    mc.thePlayer.motionY = plusMaxMotionValue.get().toDouble()
                }
                fakeJump()
            }
            "stablemotion" -> {
                mc.thePlayer.motionY = stableMotionValue.get().toDouble()
                fakeJump()
            }
            "aac3.3.9" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.4001
                }
                mc.timer.timerSpeed = 1f
                if (mc.thePlayer.motionY < 0) {
                    mc.thePlayer.motionY -= 0.00000945
                    mc.timer.timerSpeed = 1.6f
                }
            }
            "aac3.6.4" -> {
                if (mc.thePlayer.ticksExisted % 4 == 1) {
                    mc.thePlayer.motionY = 0.4195464
                    mc.thePlayer.setPosition(mc.thePlayer.posX - 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                } else if (mc.thePlayer.ticksExisted % 4 == 0) {
                    mc.thePlayer.motionY = -0.5
                    mc.thePlayer.setPosition(mc.thePlayer.posX + 0.035, mc.thePlayer.posY, mc.thePlayer.posZ)
                }
            }
            "aac4.4constant" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    jumpGround = mc.thePlayer.posY
                    mc.thePlayer.motionY = 0.41999998688698
                }
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = -0.00000001
                mc.thePlayer.jumpMovementFactor = 0.000F
                mc.timer.timerSpeed = 0.60f
                if (mc.thePlayer.posY > jumpGround + 0.99) {
                    fakeJump()
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.001335979112146, mc.thePlayer.posZ)
                    mc.thePlayer.motionY = 0.42
                    jumpGround = mc.thePlayer.posY
                    mc.timer.timerSpeed = 0.75f
                }
            }
            "verus" -> {
                mc.thePlayer.setPosition(mc.thePlayer.posX, (mc.thePlayer.posY * 2).roundToInt().toDouble() / 2, mc.thePlayer.posZ)
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    mc.thePlayer.motionY = 0.5
                    mc.timer.timerSpeed = 0.8f
                    doSpoof = false
                }else{
                    mc.timer.timerSpeed = 1.33f
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.onGround = true
                    doSpoof = true
                }
            }
            "aac4jump" -> {
                mc.timer.timerSpeed = 0.97f
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.387565
                    mc.timer.timerSpeed = 1.05f
                }
            }
            "universocraft" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.19) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                    mc.thePlayer.onGround = true
                    mc.thePlayer.motionY = 0.41999998688698
                }
            }
            "matrix6.9.2" -> {
                if (mc.thePlayer.onGround) {
                    fakeJump()
                    mc.thePlayer.motionY = 0.41999998688698
                } else if (mc.thePlayer.motionY < 0.19145141919180) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, truncate(mc.thePlayer.posY), mc.thePlayer.posZ)
                    mc.thePlayer.onGround = true
                    mc.thePlayer.motionY = 0.481145141919180
                }
            }
        }
    }

    private fun update() {
        if (if (!autoBlockValue.equals("off")) InventoryUtils.findAutoBlockBlock() == -1 else mc.thePlayer.heldItem == null ||
                    !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))) {
            return
        }

        findBlock(expandLengthValue.get()> 1)
    }

    /**
     * Search for new target block
     */
    private fun findBlock(expand: Boolean) {
        val blockPosition = if (shouldGoDown) {
            if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) {
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ)
            } else {
                BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.6, mc.thePlayer.posZ).down()
            }
        } else if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5 && !canSameY) {
            BlockPos(mc.thePlayer)
        } else if (canSameY && lastGroundY <= mc.thePlayer.posY) {
            BlockPos(mc.thePlayer.posX, lastGroundY - 1.0, mc.thePlayer.posZ)
        } else {
            BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down()
        }
        if (!expand && (!BlockUtils.isReplaceable(blockPosition) || search(blockPosition, !shouldGoDown))) return
        if (expand) {
            for (i in 0 until expandLengthValue.get()) {
                if (search(blockPosition.add(if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                        0, if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0), false)) {
                    return
                }
            }
        } else if (searchValue.get()) {
            for (x in -1..1) {
                for (z in -1..1) {
                    if (search(blockPosition.add(x, 0, z), !shouldGoDown)) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Place target block
     */
    private fun place() {
        if (targetPlace == null) {
            if (!placeableDelayValue.equals("OFF")) {
                if (lastPlace == 0 && placeableDelayValue.equals("Smart")) delayTimer.reset()
                if (placeableDelayValue.equals("Normal")) delayTimer.reset()
                if (lastPlace> 0) lastPlace--
            }
            return
        }
        if (!delayTimer.hasTimePassed(delay) || !towerStatus && canSameY && lastGroundY - 1 != targetPlace!!.vec3.yCoord.toInt()) {
            return
        }

        if (!rotationsValue.equals("None")) {
            val rayTraceInfo = mc.thePlayer.rayTraceWithServerSideRotation(5.0)
            when (hitableCheckValue.get().lowercase()) {
                "simple" -> {
                    if (rayTraceInfo != null && !rayTraceInfo.blockPos.equals(targetPlace!!.blockPos)) {
                        return
                    }
                }
                "strict" -> {
                    if (rayTraceInfo != null && (!rayTraceInfo.blockPos.equals(targetPlace!!.blockPos) || rayTraceInfo.sideHit != targetPlace!!.enumFacing)) {
                        return
                    }
                }
            }
        }

        val isDynamicSprint = sprintValue.equals("dynamic")
        var blockSlot = -1
        var itemStack = mc.thePlayer.heldItem
        if (mc.thePlayer.heldItem == null || !(mc.thePlayer.heldItem.item is ItemBlock && !InventoryUtils.isBlockListBlock(mc.thePlayer.heldItem.item as ItemBlock))) {
            if (autoBlockValue.equals("off")) return
            blockSlot = InventoryUtils.findAutoBlockBlock()
            if (blockSlot == -1) return
            if (autoBlockValue.equals("LiteSpoof") || autoBlockValue.equals("Spoof")) {
                mc.netHandler.addToSendQueue(C09PacketHeldItemChange(blockSlot - 36))
            } else {
                mc.thePlayer.inventory.currentItem = blockSlot - 36
            }
            itemStack = mc.thePlayer.inventoryContainer.getSlot(blockSlot).stack
        }
        if (isDynamicSprint) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING))
        }
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, targetPlace!!.blockPos, targetPlace!!.enumFacing, targetPlace!!.vec3)) {
            // delayTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
            if (mc.thePlayer.onGround) {
                val modifier = speedModifierValue.get()
                mc.thePlayer.motionX *= modifier.toDouble()
                mc.thePlayer.motionZ *= modifier.toDouble()
            }

            if (swingValue.equals("packet")) {
                mc.netHandler.addToSendQueue(C0APacketAnimation())
            } else if (swingValue.equals("normal")) {
                mc.thePlayer.swingItem()
            }
            lastPlace = 2
            lastPlaceBlock = targetPlace!!.blockPos.add(targetPlace!!.enumFacing.directionVec)
            when (extraClickValue.get().lowercase()) {
                "afterplace" -> {
                    // fake click
                    val blockPos = targetPlace!!.blockPos
                    val hitVec = targetPlace!!.vec3
                    afterPlaceC08 = C08PacketPlayerBlockPlacement(targetPlace!!.blockPos, targetPlace!!.enumFacing.index, itemStack, (hitVec.xCoord - blockPos.x.toDouble()).toFloat(), (hitVec.yCoord - blockPos.y.toDouble()).toFloat(), (hitVec.zCoord - blockPos.z.toDouble()).toFloat())
                }
            }
        }
        if (isDynamicSprint) {
            mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING))
        }

        if (autoBlockValue.equals("LiteSpoof") && blockSlot >= 0) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
        }

        // Reset
        targetPlace = null
    }

    /**
     * Disable scaffold module
     */
    override fun onDisable() {
        // tolleyStayTick=999
        if (mc.thePlayer == null) return
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (eagleSneaking) mc.netHandler.addToSendQueue(C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING))
        }
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindRight)) mc.gameSettings.keyBindRight.pressed = false
        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindLeft)) mc.gameSettings.keyBindLeft.pressed = false
        lockRotation = null
        mc.timer.timerSpeed = 1f
        shouldGoDown = false
        val limitedRotation = RotationUtils.limitAngleChange(RotationUtils.serverRotation, Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)!!, 58f)
        RotationUtils.setTargetRotation(limitedRotation, 2)
        if (slot != mc.thePlayer.inventory.currentItem) mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
    }

    /**
     * Entity movement event
     *
     * @param event
     */
    @EventTarget
    fun onMove(event: MoveEvent) {
        if (safeWalkValue.equals("off") || shouldGoDown) return
        if (safeWalkValue.equals("air") || mc.thePlayer.onGround) event.isSafeWalk = true
    }

    private val barrier = ItemStack(Item.getItemById(166), 0, 0)

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        progress = (System.currentTimeMillis() - lastMS).toFloat() / 100f
        lastMS = System.currentTimeMillis()
        if (progress >= 1) progress = 1f
        
        
        val scaledResolution = ScaledResolution(mc)
        val info = blocksAmount.toString() + " Blocks"
        when (counterDisplayValue.get().lowercase()) {
            "fdp" -> {
                GlStateManager.pushMatrix()
                val info = LanguageManager.getAndFormat("ui.scaffold.blocks", blocksAmount)
                val slot = InventoryUtils.findAutoBlockBlock()
                val height = event.scaledResolution.scaledHeight
                val width = event.scaledResolution.scaledWidth
                val w2=(mc.fontRendererObj.getStringWidth(info))
                RenderUtils.drawRoundedCornerRect(
                    (width - w2 - 20) / 2f,
                    height * 0.8f - 24f,
                    (width + w2 + 18) / 2f,
                    height * 0.8f + 12f,
                    3f,
                    Color(43, 45, 48).rgb
                )
                mc.fontRendererObj.drawCenteredString("▼",width / 2.0f + 2f, height * 0.8f+8f,Color(43,45,48).rgb)
                var stack = barrier
                if (slot != -1) {
                    if (mc.thePlayer.inventory.getCurrentItem() != null) {
                        val handItem = mc.thePlayer.inventory.getCurrentItem().item
                        if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                            stack = mc.thePlayer.inventory.getCurrentItem()
                        }
                    }
                    if (stack == barrier) {
                        stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                        if (stack == null) {
                            stack = barrier
                        }
                    }
                }

                RenderHelper.enableGUIStandardItemLighting()
                mc.renderItem.renderItemIntoGUI(stack, width / 2 - 9, (height * 0.8 - 20).toInt())
                RenderHelper.disableStandardItemLighting()
                mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.8f, Color.WHITE.rgb, false)
                GlStateManager.popMatrix()
            }
            "rise" -> {
                GlStateManager.pushMatrix()
                val info = blocksAmount.toString()
                val slot = InventoryUtils.findAutoBlockBlock()
                val height = event.scaledResolution.scaledHeight
                val width = event.scaledResolution.scaledWidth
                val w2=(mc.fontRendererObj.getStringWidth(info))
                RenderUtils.drawRoundedCornerRect(
                    (width - w2 - 20) / 2f,
                    height * 0.8f - 24f,
                    (width + w2 + 18) / 2f,
                    height * 0.8f + 12f,
                    5f,
                    Color(20, 20, 20, 100).rgb
                )
                var stack = barrier
                if (slot != -1) {
                    if (mc.thePlayer.inventory.getCurrentItem() != null) {
                        val handItem = mc.thePlayer.inventory.getCurrentItem().item
                        if (handItem is ItemBlock && InventoryUtils.canPlaceBlock(handItem.block)) {
                            stack = mc.thePlayer.inventory.getCurrentItem()
                        }
                    }
                    if (stack == barrier) {
                        stack = mc.thePlayer.inventory.getStackInSlot(InventoryUtils.findAutoBlockBlock() - 36)
                        if (stack == null) {
                            stack = barrier
                        }
                    }
                }

                RenderHelper.enableGUIStandardItemLighting()
                mc.renderItem.renderItemIntoGUI(stack, width / 2 - 9, (height * 0.8 - 20).toInt())
                RenderHelper.disableStandardItemLighting()
                mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 0.8f, Color.WHITE.rgb, false)
                GlStateManager.popMatrix()
            }
            
            "rise6" -> {
                val eeasing = EaseUtils.apply(EaseUtils.EnumEasingType.valueOf("BACK"), EaseUtils.EnumEasingOrder.valueOf("In"), progress.toDouble()).toFloat()
                val info = "Amount " + blocksAmount.toString()
                val height = event.scaledResolution.scaledHeight
                val width = event.scaledResolution.scaledWidth
                val w2=(mc.fontRendererObj.getStringWidth(info))
                RenderUtils.drawRoundedCornerRect(
                    (width - w2 - 20) / 2f,
                    height * 0.8f - 8f,
                    (width + w2 + 18) / 2f,
                    height * 0.8f + 12f,
                    4f,
                    Color(30, 30, 30, 120).rgb
                )
                mc.fontRendererObj.drawCenteredString(info, width / 2f, height * 1f - eeasing * 0.2f, Color.WHITE.rgb, false)
            }
            
        
            "simple" -> {
                Fonts.minecraftFont.drawString(
                    blocksAmount.toString() + " Blocks",
                    scaledResolution.scaledWidth / 1.95f,
                    (scaledResolution.scaledHeight / 2 + 20).toFloat(),
                    -1,
                    true
                )
            }
        }
    }

    /**
     * Scaffold visuals
     *
     * @param event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!markValue.get()) return
        for (i in 0 until (expandLengthValue.get() + 1)) {
            val blockPos = BlockPos(
                mc.thePlayer.posX + if (mc.thePlayer.horizontalFacing == EnumFacing.WEST) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.EAST) i else 0,
                mc.thePlayer.posY - (if (mc.thePlayer.posY == mc.thePlayer.posY.toInt() + 0.5) { 0.0 } else { 1.0 }) - (if (shouldGoDown) { 1.0 } else { 0.0 }),
                mc.thePlayer.posZ + if (mc.thePlayer.horizontalFacing == EnumFacing.NORTH) -i else if (mc.thePlayer.horizontalFacing == EnumFacing.SOUTH) i else 0
            )
            val placeInfo = get(blockPos)
            if (BlockUtils.isReplaceable(blockPos) && placeInfo != null) {
                RenderUtils.drawBlockBox(blockPos, Color(markRedValue.get(), markGreenValue.get(), markBlueValue.get(), 100), false, true, 1f)
                break
            }
        }
    }

    /**
     * Search for placeable block
     *
     * @param blockPosition pos
     * @param checks        visible
     * @return
     */
    private fun search(blockPosition: BlockPos, checks: Boolean): Boolean {
        if (!BlockUtils.isReplaceable(blockPosition)) return false
        val eyesPos = Vec3(
            mc.thePlayer.posX,
            mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight(),
            mc.thePlayer.posZ
        )
        var placeRotation: PlaceRotation? = null
        for (side in StaticStorage.facings()) {
            val neighbor = blockPosition.offset(side)
            if (!BlockUtils.canBeClicked(neighbor)) continue
            val dirVec = Vec3(side.directionVec)
            var xSearch = 0.1
            while (xSearch < 0.9) {
                var ySearch = 0.1
                while (ySearch < 0.9) {
                    var zSearch = 0.1
                    while (zSearch < 0.9) {
                        val posVec = Vec3(blockPosition).addVector(xSearch, ySearch, zSearch)
                        val distanceSqPosVec = eyesPos.squareDistanceTo(posVec)
                        val hitVec = posVec.add(Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5))
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(
                                posVec.add(dirVec)
                            ) || mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
                        ) {
                            zSearch += 0.1
                            continue
                        }

                        // face block
                        val diffX = hitVec.xCoord - eyesPos.xCoord
                        val diffY = hitVec.yCoord - eyesPos.yCoord
                        val diffZ = hitVec.zCoord - eyesPos.zCoord
                        val diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ).toDouble()
                        val rotation = Rotation(
                            MathHelper.wrapAngleTo180_float(Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
                            MathHelper.wrapAngleTo180_float((-Math.toDegrees(atan2(diffY, diffXZ))).toFloat())
                        )
                        val rotationVector = RotationUtils.getVectorForRotation(rotation)
                        val vector = eyesPos.addVector(
                            rotationVector.xCoord * 4,
                            rotationVector.yCoord * 4,
                            rotationVector.zCoord * 4
                        )
                        val obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true)
                        if (!(obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.blockPos == neighbor)) {
                            zSearch += 0.1
                            continue
                        }
                        if (placeRotation == null || RotationUtils.getRotationDifference(rotation) < RotationUtils.getRotationDifference(
                                placeRotation.rotation
                            )
                        ) placeRotation = PlaceRotation(PlaceInfo(neighbor, side.opposite, hitVec), rotation)
                        zSearch += 0.1
                    }
                    ySearch += 0.1
                }
                xSearch += 0.1
            }
        }
        if (placeRotation == null) return false
        if (!towerrotationsValue.equals("None") && towerStatus) {
            lockRotation = when (towerrotationsValue.get().lowercase()) {
                "better" -> {
                    Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), placeRotation.rotation.pitch)
                }
                "aac" -> {
                    Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacYawValue.get(), placeRotation.rotation.pitch)
                }
                "vanilla" -> {
                    placeRotation.rotation
                }
                "static1" -> {
                    val caluyaw = ((placeRotation.rotation.yaw / 45).roundToInt() * 45).toFloat()
                    Rotation(caluyaw, placeRotation.rotation.pitch)
                }
                "static2" -> {
                    Rotation(((MovementUtils.direction * 180f / Math.PI).toFloat() + 135), placeRotation.rotation.pitch)
                }
                "custom" -> {
                    Rotation(mc.thePlayer.rotationYaw + customtowerYawValue.get(), customtowerPitchValue.get().toFloat())
                }
                "backwards" -> {
                    var calcyaw = ((MovementUtils.movingYaw - 180) / 45).roundToInt() * 45
                    var calcpitch = 0f
                    if (calcyaw % 90 == 0) {
                        calcpitch = 82f
                    } else {
                        calcpitch = 78f
                    }
                    Rotation(calcyaw.toFloat(), calcpitch)
                }
                "advanced" -> {
                    var advancedYaw = 0f
                    var advancedPitch = 0f
                    advancedYaw = when (advancedYawModeValue.get().lowercase()) {
                        "offset" -> placeRotation.rotation.yaw + advancedYawOffsetValue.get()
                        "static" -> mc.thePlayer.rotationYaw + advancedYawStaticValue.get()
                        "vanilla" -> placeRotation.rotation.yaw
                        "round" -> ((placeRotation.rotation.yaw / advancedYawRoundValue.get()).roundToInt() * advancedYawRoundValue.get()).toFloat()
                        "roundstatic" -> (((mc.thePlayer.rotationYaw + advancedYawStaticValue.get()) / advancedYawRoundValue.get()).roundToInt() * advancedYawRoundValue.get()).toFloat()
                        "movedirection" -> MovementUtils.movingYaw - 180
                        "offsetmove" -> MovementUtils.movingYaw - 180 + advancedYawMoveOffsetValue.get()
                        else -> placeRotation.rotation.yaw
                    }
                    advancedPitch = when (advancedPitchModeValue.get().lowercase()) {
                        "offset" -> placeRotation.rotation.pitch + advancedPitchOffsetValue.get().toFloat()
                        "static" -> advancedPitchStaticValue.get().toFloat()
                        "vanilla" -> placeRotation.rotation.pitch
                        else -> placeRotation.rotation.pitch
                    }
                    Rotation(advancedYaw, advancedPitch)
                }
                else -> return false // this should not happen
            }
            if (silentRotationValue.get()) {
                val limitedRotation =
                    RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation!!, rotationSpeed)
                RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
            } else {
                mc.thePlayer.rotationYaw = lockRotation!!.yaw
                mc.thePlayer.rotationPitch = lockRotation!!.pitch
            }
        }
        if (!rotationsValue.equals("None") && !towerStatus) {
            lockRotation = when (rotationsValue.get().lowercase()) {
                "aac" -> {
                    Rotation(mc.thePlayer.rotationYaw + (if (mc.thePlayer.movementInput.moveForward < 0) 0 else 180) + aacYawValue.get(), placeRotation.rotation.pitch)
                }
                "vanilla", "snap", "backsnap" -> {
                    placeRotation.rotation
                }
                "static1" -> {
                    val caluyaw = ((placeRotation.rotation.yaw / 45).roundToInt() * 45).toFloat()
                    Rotation(caluyaw, placeRotation.rotation.pitch)
                }
                "static2" -> {
                    Rotation(((MovementUtils.direction * 180f / Math.PI).toFloat() + 135), placeRotation.rotation.pitch)
                }
                "custom" -> {
                    Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), customPitchValue.get().toFloat())
                }
                "better" -> {
                    Rotation(mc.thePlayer.rotationYaw + customYawValue.get(), placeRotation.rotation.pitch)
                }
                "backwards" -> {
                    var calcyaw = ((MovementUtils.movingYaw - 180) / 45).roundToInt() * 45
                    var calcpitch = 0f
                    if (calcyaw % 90 == 0) {
                        calcpitch = 82f
                    } else {
                        calcpitch = 78f
                    }
                    Rotation(calcyaw.toFloat(), calcpitch)
                }
                "advanced" -> {
                    var advancedYaw = 0f
                    var advancedPitch = 0f
                    advancedYaw = when (advancedYawModeValue.get().lowercase()) {
                        "offset" -> placeRotation.rotation.yaw + advancedYawOffsetValue.get()
                        "static" -> mc.thePlayer.rotationYaw + advancedYawStaticValue.get()
                        "vanilla" -> placeRotation.rotation.yaw
                        "round" -> ((placeRotation.rotation.yaw / advancedYawRoundValue.get()).roundToInt() * advancedYawRoundValue.get()).toFloat()
                        "roundstatic" -> (((mc.thePlayer.rotationYaw + advancedYawStaticValue.get()) / advancedYawRoundValue.get()).roundToInt() * advancedYawRoundValue.get()).toFloat()
                        "movedirection" -> MovementUtils.movingYaw - 180
                        "offsetmove" -> MovementUtils.movingYaw - 180 + advancedYawMoveOffsetValue.get()
                        else -> placeRotation.rotation.yaw
                    }
                    advancedPitch = when (advancedPitchModeValue.get().lowercase()) {
                        "offset" -> placeRotation.rotation.pitch + advancedPitchOffsetValue.get().toFloat()
                        "static" -> advancedPitchStaticValue.get().toFloat()
                        "vanilla" -> placeRotation.rotation.pitch
                        else -> placeRotation.rotation.pitch
                    }
                    Rotation(advancedYaw, advancedPitch)
                }
                else -> return false // this should not happen
            }
            if (silentRotationValue.get()) {
                val limitedRotation =
                    RotationUtils.limitAngleChange(RotationUtils.serverRotation, lockRotation!!, rotationSpeed)
                if (rotationsValue.equals("Snap") || rotationsValue.equals("BackSnap")) {
                    RotationUtils.setTargetRotation(limitedRotation, 0)
                } else {
                    RotationUtils.setTargetRotation(limitedRotation, keepLengthValue.get())
                }
            } else {
                mc.thePlayer.rotationYaw = lockRotation!!.yaw
                mc.thePlayer.rotationPitch = lockRotation!!.pitch
            }
        }
        targetPlace = placeRotation.placeInfo
        return true
    }

    /**
     * @return hotbar blocks amount
     */
    private val blocksAmount: Int
        get() {
            var amount = 0
            for (i in 36..44) {
                val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                if (itemStack != null && itemStack.item is ItemBlock && InventoryUtils.canPlaceBlock((itemStack.item as ItemBlock).block)) {
                    amount += itemStack.stackSize
                }
            }
            return amount
        }

    private val rotationSpeed: Float
        get() = (Math.random() * (maxRotationSpeedValue.get() - minRotationSpeedValue.get()) + minRotationSpeedValue.get()).toFloat()

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (towerStatus) {
            event.cancelEvent()
        }
    }

    val canSprint: Boolean
        get() = MovementUtils.isMoving() && when (sprintValue.get().lowercase()) {
            "always", "dynamic" -> true
            "onground" -> mc.thePlayer.onGround
            "offground" -> !mc.thePlayer.onGround
            "hypixel" -> mc.thePlayer.onGround
            "alternating" -> mc.thePlayer.ticksExisted % 2 == 0
            else -> false
        }

    override val tag: String
        get() = if (towerStatus) { "Tower" } else { "Normal" }
}
