/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.movement.StrafeFix
import net.ccbluex.liquidbounce.features.module.modules.movement.TargetStrafe
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.visual.FreeCam
import net.ccbluex.liquidbounce.handler.protocol.ProtocolBase
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.extensions.hitBox
import net.ccbluex.liquidbounce.utils.extensions.rayTraceWithServerSideRotation
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.*
import net.minecraft.network.play.client.*
import net.minecraft.potion.Potion
import net.minecraft.util.*
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*
import kotlin.math.*

@ModuleInfo(name = "KillAura", category = ModuleCategory.COMBAT, keyBind = Keyboard.KEY_G)
object KillAura : Module() {
    /**
     * OPTIONS
     */

    // CPS

    private val clickDisplay = BoolValue("Click Options:", true)

    private val maxCpsValue: IntegerValue = object : IntegerValue("MaxCPS", 12, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = minCpsValue.get()
            if (i > newValue) set(i)

            attackDelay = getAttackDelay(minCpsValue.get(), this.get())
        }
    }.displayable {!simulateCooldown.get() && clickDisplay.get()} as IntegerValue

    private val minCpsValue: IntegerValue = object : IntegerValue("MinCPS", 8, 1, 20) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val i = maxCpsValue.get()
            if (i < newValue) set(i)

            attackDelay = getAttackDelay(this.get(), maxCpsValue.get())
        }
    }.displayable {!simulateCooldown.get() && clickDisplay.get()} as IntegerValue

    private val CpsReduceValue = BoolValue("CPSReduceVelocity", false).displayable { clickDisplay.get() }

    // Attack Setting

    private val attackDisplay = BoolValue("Attack Options:", true)

    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal").displayable { attackDisplay.get() }

    private val attackTimingValue = ListValue("AttackTiming", arrayOf("All", "Pre", "Post", "Both"), "All").displayable { attackDisplay.get() }
    private val keepSprintValue = BoolValue("KeepSprint", true).displayable { attackDisplay.get() }

    private val hitselectValue = BoolValue("hitSelect", false).displayable { attackDisplay.get() }
    private val hitselectRangeValue = FloatValue("hitSelectRange", 3.0f, 2f, 4f).displayable { hitselectValue.get() && hitselectValue.displayable }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10).displayable { attackDisplay.get() }
    private val clickOnly = BoolValue("ClickOnly", false).displayable { attackDisplay.get() }
    private val simulateCooldown = BoolValue("CoolDown", false).displayable { attackDisplay.get() }
    private val cooldownNoDupAtk = BoolValue("NoDuplicateAttack", false).displayable { simulateCooldown.get() && attackDisplay.get() }

    // Range
    private val rangeDisplay = BoolValue("Range Options:", true)

    val rangeValue: FloatValue = object : FloatValue("Target-Range", 3.0f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { rangeDisplay.get() } as FloatValue

    private val discoverRangeValue = FloatValue("Discover-Range", 6f, 0f, 8f).displayable { rangeDisplay.get() }

    private val rangeSprintReducementValue = FloatValue("RangeSprintReducement", 0f, 0f, 0.4f).displayable { rangeDisplay.get() }

    private val swingRangeValue = object : FloatValue("SwingRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
            if (maxRange > newValue) set(maxRange)
        }
    }.displayable { rangeDisplay.get() } as FloatValue

    // Modes
    private val modeDisplay = BoolValue("Mode Options:", true)

    private val priorityValue = ListValue(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "LivingTime",
            "Fov",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "RegenAmplifier"
        ), "Health"
    ).displayable { modeDisplay.get() }

    private val targetModeValue = ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch").displayable { modeDisplay.get() }
    private val switchDelayValue = IntegerValue("SwitchDelay", 15, 1, 2000).displayable { targetModeValue.equals("Switch") && modeDisplay.get() }
    private val limitedMultiTargetsValue = IntegerValue("LimitedMultiTargets", 0, 0, 50).displayable { targetModeValue.equals("Multi") && modeDisplay.get() }

    // AutoBlock
    private val autoblockDisplay = BoolValue("AutoBlock Settings:", true)

    private val autoBlockValue = ListValue("AutoBlock", arrayOf("Range", "Fake", "Off"), "Range").displayable { autoblockDisplay.get() }

    private val autoBlockRangeValue = object : FloatValue("AutoBlockRange", 5f, 0f, 8f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val i = discoverRangeValue.get()
            if (i < newValue) set(i)
        }
    }.displayable { !autoBlockValue.equals("Off") && autoBlockValue.displayable }
    private val autoBlockPacketValue = ListValue("AutoBlockPacket", arrayOf("AfterAttack", "Vanilla", "Delayed", "Legit", "Legit2", "OldIntave", "Test", "HoldKey", "KeyBlock", "Test2", "Blink"), "Vanilla").displayable { autoBlockValue.equals("Range") && autoBlockValue.displayable }
    private val interactAutoBlockValue = BoolValue("InteractAutoBlock", false).displayable { autoBlockPacketValue.displayable }
    private val smartAutoBlockValue = BoolValue("SmartAutoBlock", false).displayable { autoBlockPacketValue.displayable }
    private val blockRateValue = IntegerValue("BlockRate", 100, 1, 100).displayable { autoBlockPacketValue.displayable }
    private val legitBlockBlinkValue = BoolValue("Legit2Blink", true).displayable { autoBlockPacketValue.displayable && autoBlockPacketValue.equals("Legit2") }
    private val alwaysBlockDisplayValue = BoolValue("AlwaysRenderBlocking", true).displayable { autoBlockValue.displayable && autoBlockValue.equals("Range") }

    // Rotations
    private val rotationDisplay = BoolValue("Rotation Options:", true)

    private val rotationModeValue = ListValue(
        "RotationMode",
        arrayOf("None", "LiquidBounce", "ForceCenter", "SmoothCenter", "SmoothLiquid", "LockView", "OldMatrix", "Test", "SmoothCustom"),
        "LiquidBounce"
    ).displayable { rotationDisplay.get()}

    private val customRotationValue = ListValue(
        "CustomRotationMode",
        arrayOf ("LiquidBounce", "Full", "HalfUp", "HalfDown", "CenterSimple", "CenterLine"),
        "HalfUp") .displayable { rotationDisplay.get() && rotationModeValue.equals("SmoothCustom") }

    private val silentRotationValue = BoolValue("SilentRotation", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}

    private val maxTurnSpeedValue: FloatValue = object : FloatValue("MaxTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minTurnSpeedValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && !rotationModeValue.equals("LockView")} as FloatValue

    private val minTurnSpeedValue: FloatValue = object : FloatValue("MinTurnSpeed", 180f, 1f, 180f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxTurnSpeedValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { rotationDisplay.get() && !rotationModeValue.equals("LockView")} as FloatValue

    private val rotationSmoothModeValue = ListValue("SmoothMode", arrayOf("Custom", "Line", "Quad", "Sine", "QuadSine"), "Custom").displayable { rotationDisplay.get() && !rotationModeValue.equals("LiquidBounce") && !rotationModeValue.equals("ForceCenter") && !rotationModeValue.equals("LockView")}
    private val rotationSmoothValue = FloatValue("CustomSmooth", 2f, 1f, 10f).displayable { rotationSmoothModeValue.equals("Custom") && rotationSmoothModeValue.displayable }

    // Random Value
    private val randomCenterModeValue = ListValue("RandomCenter", arrayOf("Off", "Cubic", "Horizontal", "Vertical"), "Off").displayable { rotationDisplay.get() }
    private val randomCenRangeValue = FloatValue("RandomRange", 0.0f, 0.0f, 1.2f).displayable { !randomCenterModeValue.equals("Off") && rotationDisplay.get()}

    // Keep Rotate
    private val rotationRevValue = BoolValue("RotationReverse", false).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    private val rotationRevTickValue = IntegerValue("RotationReverseTick", 5, 1, 20).displayable {  rotationRevValue.get() && rotationRevValue.displayable }
    private val keepDirectionValue = BoolValue("KeepDirection", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}
    private val keepDirectionTickValue = IntegerValue("KeepDirectionTick", 15, 1, 20).displayable { keepDirectionValue.get() && keepDirectionValue.displayable }
    private val rotationDelayValue = BoolValue("RotationDelay", false).displayable { !rotationModeValue.equals("None") && rotationDisplay.get() }
    private val rotationDelayMSValue = IntegerValue("RotationDelayMS", 300, 0, 1000).displayable { rotationDelayValue.get() && rotationDelayValue.displayable }

    private val fovValue = FloatValue("FOV", 180f, 0f, 180f).displayable { rotationDisplay.get() }
    private val hitAbleValue = BoolValue("AlwaysHitAble", true).displayable { rotationDisplay.get() }

    // Predict
    private val predictValue = BoolValue("Predict", true).displayable { !rotationModeValue.equals("None") && rotationDisplay.get()}

    private val maxPredictSizeValue: FloatValue = object : FloatValue("MaxPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    private val minPredictSizeValue: FloatValue = object : FloatValue("MinPredictSize", 1f, -2f, 5f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictValue.displayable && predictValue.get() } as FloatValue

    private val predictPlayerValue = BoolValue("PredictPlayer", true).displayable { !rotationModeValue.equals("None") && predictValue.get()}

    private val maxPredictPlayerSizeValue: FloatValue = object : FloatValue("MaxPredictPlayerSize", 1f, -1f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = minPredictPlayerSizeValue.get()
            if (v > newValue) set(v)
        }
    }.displayable { predictPlayerValue.displayable && predictPlayerValue.get() } as FloatValue

    private val minPredictPlayerSizeValue: FloatValue = object : FloatValue("MinPredictPlayerSize", 1f, -1f, 4f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            val v = maxPredictPlayerSizeValue.get()
            if (v < newValue) set(v)
        }
    }.displayable { predictPlayerValue.displayable && predictPlayerValue.get() } as FloatValue



    // Bypass
    private val bypassDisplay = BoolValue("Bypass Options:", true)

    private val raycastValue = BoolValue("RayCast", true).displayable { bypassDisplay.get() }
    private val raycastTargetValue = BoolValue("RaycastOnlyTarget", false).displayable { raycastValue.get() && raycastValue.displayable }

    private val throughWallsValue = BoolValue("ThroughWalls", false)

    private val multiCombo = BoolValue("MultiCombo", false).displayable { bypassDisplay.get() }
    private val amountValue = IntegerValue("Multi-Packet", 5, 0, 20, "x") { multiCombo.get() && bypassDisplay.get()}

    private val failRateValue = FloatValue("FailRate", 0f, 0f, 100f).displayable { bypassDisplay.get() }
    private val fakeSwingValue = BoolValue("FakeSwing", true).displayable { failRateValue.get() != 0f && failRateValue.displayable }
    private val rotationStrafeValue = ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Silent").displayable { silentRotationValue.get() && !rotationModeValue.equals("None") && bypassDisplay.get() }

    // Tools
    private val toolsDisplay = BoolValue("Tools Options:", true)

    private val blinkCheck = BoolValue("BlinkCheck", false).displayable { toolsDisplay.get() }
    private val noScaffValue = BoolValue("NoScaffold", false).displayable { toolsDisplay.get() }
    private val noFlyValue = BoolValue("NoFly", false).displayable { toolsDisplay.get() }
    private val noEat = BoolValue("NoEat", false).displayable { toolsDisplay.get() }
    private val noBlocking = BoolValue("NoBlocking", false).displayable { toolsDisplay.get() }
    private val noBadPacketsValue = BoolValue("NoBadPackets", false).displayable { toolsDisplay.get() }
    private val noInventoryAttackValue = ListValue("NoInvAttack", arrayOf("Spoof", "CancelRun", "Off"), "Off").displayable { toolsDisplay.get() }
    private val noInventoryDelayValue = IntegerValue("NoInvDelay", 200, 0, 500).displayable { !noInventoryAttackValue.equals("Off") && noInventoryAttackValue.displayable }
    private val onSwording = BoolValue("OnSword", true)
    private val displayDebug = BoolValue("Debug", false)

    private val displayMode = ListValue("DisplayMode", arrayOf("Simple", "LessSimple", "Complicated"), "Simple")

    /**
     * MODULE
     */

    // Target
    var currentTarget: EntityLivingBase? = null
    private var hitable = false
    private var packetSent = false
    private val prevTargetEntities = mutableListOf<Int>()
    private val discoveredTargets = mutableListOf<EntityLivingBase>()
    private val inRangeDiscoveredTargets = mutableListOf<EntityLivingBase>()
    private val canFakeBlock: Boolean
        get() = inRangeDiscoveredTargets.isNotEmpty()

    // Attack delay
    private val attackTimer = MSTimer()
    private val switchTimer = MSTimer()
    private val rotationTimer = MSTimer()
    private var attackDelay = 0L
    private var clicks = 0

    // Container Delay
    private var containerOpen = -1L

    // Swing
    private var canSwing = false

    // Last Tick Can Be Seen
    private var lastCanBeSeen = false

    // Fake block status
    var blockingStatus = false

    val displayBlocking: Boolean
        get() = blockingStatus || (((autoBlockValue.equals("Fake") || (alwaysBlockDisplayValue.get() && autoBlockValue.equals("Range"))) && canFakeBlock))

    private var predictAmount = 1.0f
    private var predictPlayerAmount = 1.0f

    // hit select
    private var canHitselect = false
    private val hitselectTimer = MSTimer()

    private val delayBlockTimer = MSTimer()
    private var delayBlock = false
    private var legitBlocking = 0
    private var legitCancelAtk = false

    private var test2_block = false
    private var wasBlink = false

    private val getAABB: ((Entity) -> AxisAlignedBB) = {
        var aabb = it.hitBox
        aabb = if (predictValue.get()) aabb.offset(
            (it.posX - it.lastTickPosX) * predictAmount,
            (it.posY - it.lastTickPosY) * predictAmount,
            (it.posZ - it.lastTickPosZ) * predictAmount
        ) else aabb
        aabb = if (predictPlayerValue.get()) aabb.offset(
            mc.thePlayer.motionX * predictPlayerAmount * -1f,
            mc.thePlayer.motionY * predictPlayerAmount * -1f,
            mc.thePlayer.motionZ * predictPlayerAmount * -1f
        ) else aabb
        aabb.expand(
            it.collisionBorderSize.toDouble(),
            it.collisionBorderSize.toDouble(),
            it.collisionBorderSize.toDouble()
        )
        aabb
    }

    /**
     * Enable kill aura module
     */
    override fun onEnable() {
        mc.thePlayer ?: return
        mc.theWorld ?: return
        lastCanBeSeen = false
        delayBlock = false
        legitBlocking = 0

        updateTarget()
    }

    /**
     * Disable kill aura module
     */
    override fun onDisable() {
        FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
        currentTarget = null
        hitable = false
        packetSent = false
        prevTargetEntities.clear()
        discoveredTargets.clear()
        inRangeDiscoveredTargets.clear()
        attackTimer.reset()
        clicks = 0
        canSwing = false

        stopBlocking()
        if (autoBlockPacketValue.equals("HoldKey") || autoBlockPacketValue.equals("KeyBlock")) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }

        RotationUtils.setTargetRotationReverse(
            RotationUtils.serverRotation,
            if (keepDirectionValue.get()) { keepDirectionTickValue.get() + 1 } else { 1 },
            if (rotationRevValue.get()) { rotationRevTickValue.get() + 1 } else { 0 }
        )
        if (wasBlink) {
            BlinkUtils.setBlinkState(off = true, release = true)
            wasBlink = false
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender2D(
        event: Render2DEvent) {
        if (displayDebug.get()) {
            val sr = ScaledResolution(mc)
            val blockingStatus = mc.thePlayer.isBlocking
            val maxRange = this.maxRange


            val reach = if (currentTarget != null) {
                mc.thePlayer.getDistanceToEntityBox(currentTarget!!)
            } else {
                0.0
            }

            val formattedReach = String.format("%.2f", reach)

            val rangeString = "Range: $maxRange"
            val reachString = "Reach: $formattedReach"

            val status = "Blocking: ${if (blockingStatus) "Yes" else "No"}, $clicks, $reachString, $rangeString"
            Fonts.minecraftFont.drawStringWithShadow(
                status,
                sr.scaledWidth / 2f - Fonts.minecraftFont.getStringWidth(status) / 2f,
                sr.scaledHeight / 2f - 60f,
                Color.orange.rgb
            )
        }
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            packetSent = false
        }

        updateHitable()
        val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return

        if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("HoldKey") && canBlock) {
            if (inRangeDiscoveredTargets.isEmpty()) {
                mc.gameSettings.keyBindUseItem.pressed = false
            } else if (mc.thePlayer.getDistanceToEntityBox(target) < maxRange) {
                mc.gameSettings.keyBindUseItem.pressed = true
            }
        }


        if ((attackTimingValue.equals("Pre") && event.eventState != EventState.PRE) || (attackTimingValue.equals("Post") && event.eventState != EventState.POST) || attackTimingValue.equals("All") || attackTimingValue.equals("Both"))
            return

        runAttackLoop()

        if (packetSent && noBadPacketsValue.get()) {
            return
        }
        return
        // AutoBlock
        if (autoBlockValue.equals("Range") && discoveredTargets.isNotEmpty() && (!autoBlockPacketValue.equals("AfterAttack")
                    || discoveredTargets.any { mc.thePlayer.getDistanceToEntityBox(it) > maxRange }) && canBlock
        ) {
            if (mc.thePlayer.getDistanceToEntityBox(target) <= autoBlockRangeValue.get()) {
                startBlocking(
                    target,
                    interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange)
                )
            } else {
                if (!mc.thePlayer.isBlocking) {
                    stopBlocking()
                }
            }
        }
    }

    /**
     * Update event
     */
    @EventTarget
    fun onUpdate(ignoredEvent: UpdateEvent) {
        if (clickOnly.get() && !mc.gameSettings.keyBindAttack.isKeyDown) return

        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }

        if (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer ||
                    System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get())
        ) {
            currentTarget = null
            hitable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }

        updateTarget()

        if (discoveredTargets.isEmpty()) {
            stopBlocking()
            if (wasBlink) {
                BlinkUtils.setBlinkState(off = true, release = true)
                wasBlink = false
            }
            return
        }


        FDPClient.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget?:return

        FDPClient.moduleManager[StrafeFix::class.java]!!.applyForceStrafe(rotationStrafeValue.equals("Silent"), !rotationStrafeValue.equals("Off") && !rotationModeValue.equals("None"))

        val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return

        if (autoBlockValue.equals("Range")) {
            if (autoBlockPacketValue.equals("Test")) {
                if (mc.thePlayer.swingProgressInt == 1) {
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                }
            }

            if (autoBlockPacketValue.equals("Legit2")) {
                if (mc.thePlayer.ticksExisted % 4 == 1 && (!smartAutoBlockValue.get() || mc.thePlayer.hurtTime < 3)) {
                    if (legitBlockBlinkValue.get() || wasBlink) {
                        BlinkUtils.setBlinkState(off = true, release = true)
                        wasBlink = false
                    }
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                } else if (mc.thePlayer.ticksExisted % 4 == 3 || (smartAutoBlockValue.get() && mc.thePlayer.hurtTime > 3)) {
                    if (legitBlockBlinkValue.get()) {
                        BlinkUtils.setBlinkState(all = true)
                        wasBlink = true
                    }
                    stopBlocking()
                }
            }

            if (autoBlockPacketValue.equals("Blink")) {
                if (mc.thePlayer.ticksExisted % 2 == 1) {
                    if (blockingStatus) {
                        BlinkUtils.setBlinkState(all = true)
                        wasBlink = true
                        stopBlocking()
                    }
                }
            }


            legitCancelAtk = false
            if (autoBlockPacketValue.equals("Legit")) {
                if (mc.thePlayer.hurtTime > 8) {
                    legitBlocking = 0
                    if (blockingStatus) {
                        stopBlocking()
                        blockingStatus = false
                        legitCancelAtk = true
                    }
                } else {
                    if (mc.thePlayer.hurtTime == 1) {
                        legitBlocking = 3
                    } else if (legitBlocking > 0) {
                        legitBlocking--
                        // this code is correct u idiots
                        if (discoveredTargets.isNotEmpty() && !blockingStatus) {
                            val target = this.currentTarget ?: discoveredTargets.first()
                            startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                            blockingStatus = true
                        }
                        if (clicks > 2)
                            clicks = 2
                        legitCancelAtk = true
                    } else {
                        if (!canHitselect && hitselectValue.get()) {
                            legitBlocking = 3
                        } else {
                            if (blockingStatus) stopBlocking()
                            blockingStatus = false
                            legitCancelAtk = true
                            // prevent hypixel flag
                        }
                    }
                }
            }
        }


        if (attackTimingValue.equals("All")) {
            runAttackLoop()
        }

        if (legitBlocking < 1 && autoBlockPacketValue.equals("Legit")) {
            if (blockingStatus) stopBlocking()
            blockingStatus = false
        }
    }

    private fun runAttackLoop() {

        if (CpsReduceValue.get() && mc.thePlayer.hurtTime > 8){
            clicks += 4
        }

        // hit select (take damage to get yvelo to crit, for legit killaura)
        if (hitselectValue.get()) {
            if (canHitselect) {
                if (inRangeDiscoveredTargets.isEmpty() && hitselectTimer.hasTimePassed(900L)) canHitselect = false
            } else {
                if (mc.thePlayer.hurtTime > 7) {
                    canHitselect = true
                    hitselectTimer.reset()
                }
                inRangeDiscoveredTargets.forEachIndexed { index, entity -> if ( mc.thePlayer.getDistanceToEntityBox(entity) < hitselectRangeValue.get() ) canHitselect = true; hitselectTimer.reset() }
            }
            if (!canHitselect) {
                if (clicks > 0)
                    clicks = 1
                return
            }
        }

        if (autoBlockValue.equals("Range")) {
            when (autoBlockPacketValue.get().lowercase()) {
                "legit" -> if (legitCancelAtk) return
                "legit2" -> if (mc.thePlayer.ticksExisted % 4 > 0 && (!smartAutoBlockValue.get() || mc.thePlayer.hurtTime < 3)) return
                "test", "test2" -> {
                    if (blockingStatus) {
                        stopBlocking()
                        return
                    }
                }
                "blink" -> if (mc.thePlayer.ticksExisted % 2 == 1) return
                else -> null
            }

        }


        if (simulateCooldown.get() && CooldownHelper.getAttackCooldownProgress() < 1.0f) {
            return
        }

        if (simulateCooldown.get() && cooldownNoDupAtk.get() && clicks > 0) {
            clicks = 1
        }

        try {
            while (clicks > 0) {
                runAttack()
                clicks--
            }
        } catch (e: java.lang.IllegalStateException) {
            return
        }

        if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("Blink")) {
            BlinkUtils.setBlinkState(off = true, release = true)
            wasBlink = false
            val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return
            startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
        }

        test2_block = true
    }

    /**
     * Attack enemy
     */
    private fun runAttack() {
        currentTarget ?: return

        // Settings
        val failRate = failRateValue.get()
        val openInventory = noInventoryAttackValue.equals("Spoof") && mc.currentScreen is GuiInventory
        val failHit = failRate > 0 && Random().nextInt(100) <= failRate

        // Check is not hitable or check failrate
        if (hitable && !failHit) {
            // Close inventory when open
            if (openInventory) {
                mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
            }

            // Attack
            if (!targetModeValue.equals("Multi")) {
                attackEntity(if (raycastValue.get()) {
                    (RaycastUtils.raycastEntity(maxRange.toDouble()) {
                        it is EntityLivingBase && it !is EntityArmorStand && (!raycastTargetValue.get() || EntityUtils.canRayCast(
                            it
                        )) && !EntityUtils.isFriend(it)
                    } ?: currentTarget!!) as EntityLivingBase } else { currentTarget!! })
            } else {
                inRangeDiscoveredTargets.forEachIndexed { index, entity ->
                    if (limitedMultiTargetsValue.get() == 0 || index < limitedMultiTargetsValue.get()) {
                        attackEntity(entity)
                    }
                }
            }

            if (targetModeValue.equals("Switch")) {
                if (switchTimer.hasTimePassed(switchDelayValue.get().toLong())) {
                    prevTargetEntities.add(currentTarget!!.entityId)
                    switchTimer.reset()
                }
            } else {
                prevTargetEntities.add(currentTarget!!.entityId)
            }

            // Open inventory
            if (openInventory) {
                mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            }
        } else if (fakeSwingValue.get() || canSwing) {
            runSwing()
        }
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        // Settings
        val fov = fovValue.get()
        val switchMode = targetModeValue.equals("Switch")

        // Find possible targets
        discoveredTargets.clear()

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !EntityUtils.isSelected(
                    entity,
                    true
                ) || (switchMode && prevTargetEntities.contains(entity.entityId))
            ) {
                continue
            }

            var distance = mc.thePlayer.getDistanceToEntityBox(entity)
            if (Backtrack.state) {
                val trackedDistance = Backtrack.getNearestTrackedDistance(entity)

                if (distance > trackedDistance) {
                    distance = trackedDistance
                }
            }

            val entityFov = RotationUtils.getRotationDifference(entity)

            if (distance <= discoverRangeValue.get() && (fov == 180F || entityFov <= fov)) {
                discoveredTargets.add(entity)
            }
        }

        // Sort targets by priority
        when (priorityValue.get().lowercase()) {
            "distance" -> discoveredTargets.sortBy { mc.thePlayer.getDistanceToEntityBox(it) } // Sort by distance
            "health" -> discoveredTargets.sortBy { it.health + it.absorptionAmount } // Sort by health
            "fov" -> discoveredTargets.sortBy { RotationUtils.getRotationDifference(it) } // Sort by FOV
            "livingtime" -> discoveredTargets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> discoveredTargets.sortBy { it.totalArmorValue } // Sort by armor
            "hurttime" -> discoveredTargets.sortBy { it.hurtTime } // Sort by hurt time
            "hurtresistance" -> discoveredTargets.sortBy { it.hurtResistantTime } // hurt resistant time
            "regenamplifier" -> discoveredTargets.sortBy { if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1 }
        }

        inRangeDiscoveredTargets.clear()
        inRangeDiscoveredTargets.addAll(discoveredTargets.filter { mc.thePlayer.getDistanceToEntityBox(it) < (swingRangeValue.get() - if (mc.thePlayer.isSprinting) rangeSprintReducementValue.get() else 0F) })

        // Cleanup last targets when no targets found and try again
        if (inRangeDiscoveredTargets.isEmpty() && prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
            return
        }

        // Find best target
        for (entity in inRangeDiscoveredTargets) {
            // Update rotations to current target
            if (!updateRotations(entity)) {
                var success = false
                Backtrack.loopThroughBacktrackData(entity) {
                    if (updateRotations(entity)) {
                        success = true
                        return@loopThroughBacktrackData true
                    }

                    return@loopThroughBacktrackData false
                }

                if (!success) {
                    // when failed then try another target
                    continue
                }

            }

            // Set target to current entity
            if (mc.thePlayer.getDistanceToEntityBox(entity) < discoverRangeValue.get()) {
                currentTarget = entity
                FDPClient.moduleManager[TargetStrafe::class.java]!!.targetEntity = currentTarget?:return
                FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe = FDPClient.moduleManager[TargetStrafe::class.java]!!.toggleStrafe()
                return
            }
        }

        currentTarget = null
        FDPClient.moduleManager[TargetStrafe::class.java]!!.doStrafe = false
    }

    private fun runSwing() {
        val swing = swingValue.get()
        if (swing.equals("packet", true)) {
            mc.netHandler.addToSendQueue(C0APacketAnimation())
        } else if (swing.equals("normal", true)) {
            mc.thePlayer.swingItem()
        }
    }

    /**
     * Attack [entity]
     * @throws IllegalStateException when bad packets protection
     */
    private fun attackEntity(entity: EntityLivingBase) {
        if (packetSent && noBadPacketsValue.get()) return
        if (mc.thePlayer.getDistanceToEntityBox(entity) > rangeValue.get())
            return

        // Call attack event
        val event = AttackEvent(entity)
        FDPClient.eventManager.callEvent(event)
        if (event.isCancelled) return

        // Stop blocking
        preAttack()

        // Attack target
        runSwing()
        packetSent = true
        mc.netHandler.addToSendQueue(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))


        swingKeepSprint(entity)

        postAttack(entity)

        CooldownHelper.resetLastAttackedTicks()
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (multiCombo.get()) {
            event.targetEntity
            repeat(amountValue.get()) {
                if (ProtocolBase.getManager().targetVersion.newerThan(ProtocolVersion.v1_8))
                    mc.netHandler.addToSendQueue(
                        C02PacketUseEntity(
                            event.targetEntity,
                            C02PacketUseEntity.Action.ATTACK
                        )
                    )

                mc.netHandler.addToSendQueue(C0APacketAnimation())

                if (!ProtocolBase.getManager().targetVersion.newerThan(ProtocolVersion.v1_8))
                    mc.netHandler.addToSendQueue(
                        C02PacketUseEntity(
                            event.targetEntity,
                            C02PacketUseEntity.Action.ATTACK
                        )
                    )
            }
        }
    }

    private fun preAttack() {
        if (mc.thePlayer.isBlocking || blockingStatus) {
            when (autoBlockPacketValue.get().lowercase()) {
                "vanilla" -> null
                "afterattack", "delayed" -> stopBlocking()
                "oldintave" -> {
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                    blockingStatus = false
                }
                "keyblock" -> mc.gameSettings.keyBindUseItem.pressed = false
                "legit", "test", "holdkey", "Legit2" -> null
                else -> null
            }
        }
    }

    private fun postAttack(entity: EntityLivingBase) {
        if (mc.thePlayer.isBlocking || (autoBlockValue.equals("Range") && canBlock)) {
            if (blockRateValue.get() > 0 && Random().nextInt(100) <= blockRateValue.get()) {
                if (smartAutoBlockValue.get() && clicks != 1 && mc.thePlayer.hurtTime < 4 && mc.thePlayer.getDistanceToEntityBox(entity) < 4) {
                    return
                }
                when (autoBlockPacketValue.get().lowercase()) {
                    "vanilla", "afterattack", "oldintave" -> startBlocking(entity, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(entity) < maxRange))
                    "delayed", "keyblock" -> delayBlockTimer.reset()
                    "legit", "test", "holdkey", "Legit2" -> null
                    else -> null
                }
            }
        }
    }

    private fun swingKeepSprint(entity: EntityLivingBase) {
        if (keepSprintValue.get() && (!CpsReduceValue.get() || mc.thePlayer.hurtTime < 7)) {
            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(mc.thePlayer.heldItem, entity.creatureAttribute) > 0F) {
                mc.thePlayer.onEnchantmentCritical(entity)
            }
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                mc.thePlayer.attackTargetEntityWithCurrentItem(entity)
            }
        }
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        if (rotationModeValue.equals("None")) {
            return true
        }

        // 视角差异
        val entityFov = RotationUtils.getRotationDifference(RotationUtils.toRotation(RotationUtils.getCenter(entity.hitBox), true), RotationUtils.serverRotation)

        // 可以被看见
        if (entityFov <= mc.gameSettings.fovSetting) lastCanBeSeen = true
        else if (lastCanBeSeen) { // 不可以被看见但是上一次tick可以看见
            rotationTimer.reset() // 重置计时器
            lastCanBeSeen = false
        }

        if (predictValue.get()) {
            predictAmount = RandomUtils.nextFloat(maxPredictSizeValue.get(), minPredictSizeValue.get())
        }
        if (predictPlayerValue.get()) {
            predictPlayerAmount = RandomUtils.nextFloat(maxPredictPlayerSizeValue.get(), minPredictPlayerSizeValue.get())
        }

        val boundingBox = if (rotationModeValue.get() == "Test") entity.hitBox else getAABB(entity)

        val rModes = when (rotationModeValue.get()) {
            "LiquidBounce", "SmoothLiquid" -> "LiquidBounce"
            "ForceCenter", "SmoothCenter", "OldMatrix" -> "CenterLine"
            "LockView" -> "CenterSimple"
            "SmoothCustom" -> customRotationValue.get()
            else -> "LiquidBounce"
        }

        val (_, directRotation) =
            RotationUtils.calculateCenter(
                rModes,
                randomCenterModeValue.get(),
                (randomCenRangeValue.get()).toDouble(),
                boundingBox,
                predictValue.get(),
                throughWallsValue.get()
            ) ?: return false

        if (rotationModeValue.get() == "OldMatrix") directRotation.pitch = 89.9f

        var diffAngle = RotationUtils.getRotationDifference(RotationUtils.serverRotation, directRotation)
        if (diffAngle < 0) diffAngle = -diffAngle
        if (diffAngle > 180.0) diffAngle = 180.0

        val calculateSpeed = when (rotationSmoothModeValue.get()) {
            "Custom" -> diffAngle / rotationSmoothValue.get()
            "Line" -> (diffAngle / 360) * maxTurnSpeedValue.get() + (1 - diffAngle / 360) * minTurnSpeedValue.get()
            "Quad" -> (diffAngle / 360.0).pow(2.0) * maxTurnSpeedValue.get() + (1 - (diffAngle / 360.0).pow(2.0)) * minTurnSpeedValue.get()
            "Sine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5) * maxTurnSpeedValue.get() + (cos(diffAngle / 360 * Math.PI) * 0.5 + 0.5) * minTurnSpeedValue.get()
            "QuadSine" -> (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0) * maxTurnSpeedValue.get() + (1 - (-cos(diffAngle / 180 * Math.PI) * 0.5 + 0.5).pow(2.0)) * minTurnSpeedValue.get()
            else -> 360.0
        }

        if (!lastCanBeSeen && rotationDelayValue.get() && !rotationTimer.hasTimePassed(rotationDelayMSValue.get().toLong())) return true

        val rotation = when (rotationModeValue.get()) {
            "LiquidBounce", "ForceCenter" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation, directRotation,
                (Math.random() * (maxTurnSpeedValue.get() - minTurnSpeedValue.get()) + minTurnSpeedValue.get()).toFloat()
            )
            "LockView" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (180.0).toFloat()
            )
            "SmoothCenter", "SmoothLiquid", "SmoothCustom", "OldMatrix" -> RotationUtils.limitAngleChange(
                RotationUtils.serverRotation,
                directRotation,
                (calculateSpeed).toFloat()
            )
            else -> return true
        }

        if (silentRotationValue.get()) {
            RotationUtils.setTargetRotationReverse(
                rotation,
                if (keepDirectionValue.get()) {
                    keepDirectionTickValue.get()
                } else {
                    1
                },
                if (rotationRevValue.get()) {
                    rotationRevTickValue.get()
                } else {
                    0
                }
            )
        } else {
            rotation.toPlayer(mc.thePlayer)
        }
        return true
    }

    /**
     * Check if enemy is hitable with current rotations
     */
    private fun updateHitable() {
        if (currentTarget == null) {
            canSwing = false
            hitable = false
            return
        }
        val entityDist = mc.thePlayer.getDistanceToEntityBox(currentTarget as Entity)
        canSwing = entityDist < swingRangeValue.get() && (currentTarget as EntityLivingBase).hurtTime <= hurtTimeValue.get()
        if (hitAbleValue.get()) {
            hitable = entityDist <= maxRange.toDouble()
            return
        }
        // Disable hitable check if turn speed is zero
        if (maxTurnSpeedValue.get() <= 0F) {
            hitable = true
            return
        }
        val wallTrace = mc.thePlayer.rayTraceWithServerSideRotation(entityDist)
        hitable = RotationUtils.isFaced(
            currentTarget,
            maxRange.toDouble()
        ) && (entityDist < discoverRangeValue.get() || wallTrace?.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) && (currentTarget as EntityLivingBase).hurtTime <= hurtTimeValue.get()
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean) {
        if (autoBlockValue.equals("Range") && mc.thePlayer.getDistanceToEntityBox(interactEntity) > autoBlockRangeValue.get()) {
            return
        }

        if (blockingStatus) {
            return
        }

        if (packetSent && noBadPacketsValue.get()) {
            return
        }

        if (interact) {
            val positionEye = mc.renderViewEntity?.getPositionEyes(1F)

            interactEntity.collisionBorderSize.toDouble()
            val boundingBox = interactEntity.hitBox

            val (yaw, pitch) = RotationUtils.targetRotation ?: Rotation(mc.thePlayer!!.rotationYaw, mc.thePlayer!!.rotationPitch)
            val yawCos = cos(-yaw * 0.017453292F - Math.PI.toFloat())
            val yawSin = sin(-yaw * 0.017453292F - Math.PI.toFloat())
            val pitchCos = -cos(-pitch * 0.017453292F)
            val pitchSin = sin(-pitch * 0.017453292F)
            val range = min(maxRange.toDouble(), mc.thePlayer!!.getDistanceToEntityBox(interactEntity)) + 1
            val lookAt = positionEye!!.addVector(yawSin * pitchCos * range, pitchSin * range, yawCos * pitchCos * range)

            val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
            val hitVec = movingObject.hitVec

            mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, Vec3(
                hitVec.xCoord - interactEntity.posX,
                hitVec.yCoord - interactEntity.posY,
                hitVec.zCoord - interactEntity.posZ)
            ))
            //mc.netHandler.addToSendQueue(C02PacketUseEntity(interactEntity, C02PacketUseEntity.Action.INTERACT))
        }

        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blockingStatus = true
        packetSent = true
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking() {
        if (blockingStatus) {
            if (packetSent && noBadPacketsValue.get()) {
                return
            }
            mc.netHandler.addToSendQueue(
                C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN, //if (MovementUtils.isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN,
                    EnumFacing.DOWN
                )
            )
            blockingStatus = false
            packetSent = true
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            currentTarget = null
            hitable = false
            stopBlocking()
            discoveredTargets.clear()
            inRangeDiscoveredTargets.clear()
        }
        if (currentTarget != null && attackTimer.hasTimePassed(attackDelay) && currentTarget!!.hurtTime <= hurtTimeValue.get()) {
            clicks++
            attackTimer.reset()
            attackDelay = getAttackDelay(minCpsValue.get(), maxCpsValue.get())
        }

        if (currentTarget != null && attackTimer.hasTimePassed((attackDelay.toDouble() * 0.9).toLong()) && (autoBlockValue.equals("Range") && canBlock) && autoBlockPacketValue.equals("KeyBlock")) {
            mc.gameSettings.keyBindUseItem.pressed = false
        }

        if (currentTarget != null && delayBlockTimer.hasTimePassed(30) && (autoBlockValue.equals("Range") && canBlock)) {
            if (autoBlockPacketValue.equals("KeyBlock")) {
                mc.gameSettings.keyBindUseItem.pressed = true
            }
            if (autoBlockPacketValue.equals("Delayed")) {
                val target = this.currentTarget ?: discoveredTargets.getOrNull(0) ?: return
                startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
            }

            if (autoBlockValue.equals("Range") && autoBlockPacketValue.equals("Test2") && !blockingStatus && test2_block) {
                if (discoveredTargets.isNotEmpty()) {
                    val target = this.currentTarget ?: discoveredTargets.first()
                    startBlocking(target, interactAutoBlockValue.get() && (mc.thePlayer.getDistanceToEntityBox(target) < maxRange))
                    blockingStatus = true
                    test2_block = false
                }
            }
        }
    }

    /**
     * Attack Delay
     */
    private fun getAttackDelay(minCps: Int, maxCps: Int): Long {
        return TimeUtils.randomClickDelay(minCps.coerceAtMost(maxCps), minCps.coerceAtLeast(maxCps))
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun: Boolean
        get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer)
                || (blinkCheck.get() && FDPClient.moduleManager[Blink::class.java]!!.state)
                || FDPClient.moduleManager[FreeCam::class.java]!!.state
                || (noScaffValue.get() && FDPClient.moduleManager[Scaffold::class.java]!!.state)
                || (noFlyValue.get() && FDPClient.moduleManager[Flight::class.java]!!.state)
                || (noEat.get() && mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item is ItemFood || mc.thePlayer.heldItem?.item is ItemBucketMilk || mc.thePlayer.isUsingItem && (mc.thePlayer.heldItem?.item is ItemPotion)))
                || (noBlocking.get() && mc.thePlayer.isUsingItem && mc.thePlayer.heldItem?.item is ItemBlock)
                || (noInventoryAttackValue.equals("CancelRun") && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelayValue.get()))
                || (onSwording.get() && mc.thePlayer.heldItem?.item !is ItemSword)


    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() = mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword

    /**
     * Range
     */
    private val maxRange: Float
        get() = max(rangeValue.get(), if (!throughWallsValue.get()) rangeValue.get() else 0.0f)

    /**
     * HUD Tag
     */
    override val tag: String
        get() = when (displayMode.get().lowercase()) {
            "simple" -> targetModeValue.get() + ""
            "lesssimple" -> rangeValue.get().toString() + " " + targetModeValue.get() + " " + autoBlockValue.get()
            "complicated" -> "M:" + targetModeValue.get() + ", AB:" + autoBlockValue.get() + ", R:" + rangeValue.get() + ", CPS:" + minCpsValue.get() + " - " + maxCpsValue.get()else -> targetModeValue.get() + ""
        }
}