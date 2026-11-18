/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.config.*
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.Backtrack.runWithSimulatedPosition
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.other.Fucker
import net.ccbluex.liquidbounce.features.module.modules.other.Nuker
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.*
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Text
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.*
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.attack.CooldownHelper.getAttackCooldownProgress
import net.ccbluex.liquidbounce.utils.attack.CooldownHelper.resetLastAttackedTicks
import net.ccbluex.liquidbounce.utils.attack.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.attack.TargetPriority
import net.ccbluex.liquidbounce.utils.attack.TargetTracker
import net.ccbluex.liquidbounce.utils.client.BlinkUtils
import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.entity.PositionExtrapolation
import net.ccbluex.liquidbounce.utils.inventory.InventoryUtils.serverOpenInventory
import net.ccbluex.liquidbounce.utils.inventory.ItemUtils.isConsumingItem
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.movement.MovementUtils
import net.ccbluex.liquidbounce.utils.render.ColorSettingsInteger
import net.ccbluex.liquidbounce.utils.render.Render3D
import net.ccbluex.liquidbounce.utils.rotation.RandomizationSettings
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.raycastEntity
import net.ccbluex.liquidbounce.utils.rotation.RaycastUtils.runWithModifiedRaycastResult
import net.ccbluex.liquidbounce.utils.rotation.Rotation
import net.ccbluex.liquidbounce.utils.rotation.RotationPriority
import net.ccbluex.liquidbounce.utils.rotation.RotationSettings
import net.ccbluex.liquidbounce.utils.rotation.PostRotationExecutor
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.currentRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.getVectorForRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isRotationFaced
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.isVisible
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.rotationDifference
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.searchCenter
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.serverRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.setTargetRotation
import net.ccbluex.liquidbounce.utils.rotation.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.rotation.point.PointTracker
import net.ccbluex.liquidbounce.utils.simulation.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import net.ccbluex.liquidbounce.utils.timing.ClickPattern
import net.ccbluex.liquidbounce.utils.timing.ClickPatterns
import net.ccbluex.liquidbounce.utils.timing.Clicker
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.*
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.*
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.potion.Potion
import net.minecraft.util.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

object KillAura : Module("KillAura", Category.COMBAT, Category.SubCategory.COMBAT_RAGE, Keyboard.KEY_G) {
    /**
     * OPTIONS
     */

    private val simulateCooldown by boolean("SimulateCooldown", false)
        .describe("Wait for the vanilla attack cooldown.")
    private val simulateDoubleClicking by boolean("SimulateDoubleClicking", false) { !simulateCooldown }
        .describe("Occasionally click twice in a tick like a human.")

    // CPS - Attack speed
    private val cps by intRange("CPS", 5..8, 1..50) { !simulateCooldown }.onChanged {
        attackDelay = clicker.nextDelay(clickPattern, it.first, it.last)
    }

    private val clickPatternValue = choices("ClickPattern", ClickPatterns.names, "Stabilized") { !simulateCooldown }
    private val clickPatternName: String by clickPatternValue
    private val clickPattern: ClickPattern get() = ClickPatterns.byName(clickPatternName)
    private val clicker = Clicker()

    private val hurtTime by int("HurtTime", 10, 0..10) { !simulateCooldown }
        .describe("Only attack when target hurt-time is at or below this.")

    private val activationSlot by boolean("ActivationSlot", false)
        .describe("Only run while a specific hotbar slot is held.")
    private val preferredSlot by int("PreferredSlot", 1, 1..9) { activationSlot }
        .describe("Hotbar slot that activates the aura.")

    private val clickOnly by boolean("ClickOnly", false)
        .describe("Only attack while the attack key is held.")

    // Range
    // TODO: Make block range independent from attack range
    private val range: Float by float("Range", 3.7f, 1f..8f).onChanged {
        blockRange = blockRange.coerceAtMost(it)
    }
    private val scanRange by float("ScanRange", 2f, 0f..10f)
        .describe("Extra range used to search for the aim spot.")
    private val throughWallsRange by float("ThroughWallsRange", 3f, 0f..8f)
        .describe("Max distance to attack targets through walls.")
    private val rangeSprintReduction by float("RangeSprintReduction", 0f, 0f..0.4f)
        .describe("Range reduction applied while sprinting.")
    private val rangeChance by int("RangeChance", 100, 1..100, "%")
        .describe("Chance to use the full range each roll.")
    private var rolledRange = -1f
    private var rangeRollCounter = 0

    // Modes
    private val priority by choices(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "Direction",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier",
            "OnLadder",
            "InLiquid",
            "InWeb"
        ), "Armor"
    )
    private val targetMode by choices("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
        .describe("How many targets to engage at once.")
    private val stickyTarget by boolean("StickyTarget", false) { targetMode == "Single" }
        .describe("Keep the current target while valid instead of re-picking each tick.")
    private val limitedMultiTargets by int("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }
        .describe("Max targets to hit in multi mode, 0 is no limit.")
    private val maxSwitchFOV by float("MaxSwitchFOV", 90f, 30f..180f) { targetMode == "Switch" }
        .describe("Max angle to switch to another target.")

    private val targetTracker = TargetTracker(
        range = { maxRange },
        fov = { fov },
        distance = { entity ->
            Backtrack.runWithNearestTrackedDistance(entity) {
                mc.thePlayer?.getDistanceToEntityBox(entity) ?: Double.MAX_VALUE
            }
        },
    )

    // Delay
    private val switchDelay by int("SwitchDelay", 15, 1..1000) { targetMode == "Switch" }
        .describe("Delay before switching targets.")

    // Bypass
    private val swing by boolean("Swing", true)
        .describe("Swing the arm when attacking.")
    private val keepSprintMode by choices("KeepSprint", arrayOf("Standard", "Watchdog", "Off"), "Standard")
        .describe(
            "Standard: keep sprinting after the hit. Watchdog: stop sprinting one tick before the hit, " +
                "hit at walking speed and resume one tick after. Off: vanilla cancels sprint."
        )

    // Settings
    private val autoF5 by boolean("AutoF5", false)
        .describe("Switch to third person while a target exists.")
    private val onSwording by boolean("OnSwording", true)
        .describe("Only attack while holding a sword.")
    private val onScaffold by boolean("OnScaffold", false)
        .describe("Allow attacking while scaffolding.")
    private val onDestroyBlock by boolean("OnDestroyBlock", false)
        .describe("Allow attacking while breaking blocks.")

    private val postRotationAttack by boolean("PostRotationAttack", false)
        .describe("Attack only after the rotation reaches the target.")
    private val noScaffold by boolean("NoScaffold", false)
        .describe("Disable the aura while Scaffold is on.")
    private val noFly by boolean("NoFly", false)
        .describe("Disable the aura while Flight is on.")
    private val noEat by boolean("NoEat", false)
        .describe("Disable the aura while eating or drinking.")
    private val noBlocking by boolean("NoBlocking", false)
        .describe("Disable the aura while using a block item.")
    private val blinkCheck by boolean("BlinkCheck", false)
        .describe("Disable the aura while Blink is on.")

    // AutoBlock
    val autoBlock by choices("AutoBlock", arrayOf("Off", "Packet", "Fake"), "Packet")
        .describe("How to auto block with a sword.")
    private val blockMaxRange by float("BlockMaxRange", 3f, 0f..8f) { autoBlock == "Packet" }
        .describe("Max distance at which auto block engages.")
    private val unblockMode by choices(
        "UnblockMode", arrayOf("Stop", "Switch", "Empty"), "Stop"
    ) { autoBlock == "Packet" }
    private val releaseAutoBlock by boolean("ReleaseAutoBlock", true) { autoBlock !in arrayOf("Off", "Fake") }
        .describe("Release the block before each attack.")
    val forceBlockRender by boolean("ForceBlockRender", true) {
        autoBlock !in arrayOf(
            "Off", "Fake"
        ) && releaseAutoBlock
    }
    private val ignoreTickRule by boolean("IgnoreTickRule", false) {
        autoBlock !in arrayOf(
            "Off", "Fake"
        ) && releaseAutoBlock
    }
    private val blockRate by int("BlockRate", 100, 1..100) { autoBlock !in arrayOf("Off", "Fake") && releaseAutoBlock }
        .describe("Chance to actually block on each attack.")

    private val uncpAutoBlock by boolean("UpdatedNCPAutoBlock", false) {
        autoBlock !in arrayOf(
            "Off", "Fake"
        ) && !releaseAutoBlock
    }

    private val switchStartBlock by boolean("SwitchStartBlock", false) { autoBlock !in arrayOf("Off", "Fake") }
        .describe("Switch slot when starting to block.")

    private val interactAutoBlock by boolean("InteractAutoBlock", true) { autoBlock !in arrayOf("Off", "Fake") }
        .describe("Send an interact packet when starting to block.")

    val blinkAutoBlock by boolean("BlinkAutoBlock", false) { autoBlock !in arrayOf("Off", "Fake") }
        .describe("Use blink while auto blocking.")

    private val blinkBlockTicks by int("BlinkBlockTicks", 3, 2..5) {
        autoBlock !in arrayOf(
            "Off", "Fake"
        ) && blinkAutoBlock
    }

    // AutoBlock conditions
    private val smartAutoBlock by boolean("SmartAutoBlock", false) { autoBlock == "Packet" }
        .describe("Only block when it is actually useful.")

    // Ignore all blocking conditions, except for block rate, when standing still
    private val forceBlock by boolean("ForceBlockWhenStill", true) { smartAutoBlock }
        .describe("Always block when standing still.")

    // Don't block if target isn't holding a sword or an axe
    private val checkWeapon by boolean("CheckEnemyWeapon", true) { smartAutoBlock }
        .describe("Only block when the target holds a sword or axe.")

    // TODO: Make block range independent from attack range
    private var blockRange: Float by float("BlockRange", range, 1f..8f) {
        smartAutoBlock
    }.onChange { _, new ->
        new.coerceAtMost(this@KillAura.range)
    }

    // Don't block when you can't get damaged
    private val maxOwnHurtTime by int("MaxOwnHurtTime", 3, 0..10) { smartAutoBlock }
        .describe("Skip blocking when your hurt-time is above this.")

    // Don't block if target isn't looking at you
    private val maxDirectionDiff by float("MaxOpponentDirectionDiff", 60f, 30f..180f) { smartAutoBlock }
        .describe("Skip blocking if the target is not facing you.")

    // Don't block if target is swinging an item and therefore cannot attack
    private val maxSwingProgress by int("MaxOpponentSwingProgress", 1, 0..5) { smartAutoBlock }
        .describe("Skip blocking while the target is mid-swing.")

    // Rotations
    private val options = RotationSettings(this).withoutKeepRotation().withRequestPriority(RotationPriority.HIGH)
    private val pointTracker = PointTracker().also { addValue(it) }

    // Raycast
    private val raycastValue = boolean("RayCast", true) { options.rotationsActive }
        .describe("Require a raycast hit to confirm the target.")
    private val raycast by raycastValue
    private val raycastIgnored by boolean(
        "RayCastIgnored", false
    ) { raycastValue.isActive() && options.rotationsActive }
    private val livingRaycast by boolean("LivingRayCast", true) { raycastValue.isActive() && options.rotationsActive }
        .describe("Only count living entities for the raycast.")
    private val raytraceMode by choices("Raytrace", arrayOf("Normal", "Strict"), "Normal") { raycastValue.isActive() && options.rotationsActive }
        .describe("How strictly the raycast must land on the target.")

    // Hit delay
    private val useHitDelay by boolean("UseHitDelay", false)
        .describe("Delay hits after the raycast target type changes.")
    private val hitDelayTicks by int("HitDelayTicks", 1, 1..5) { useHitDelay }
        .describe("Ticks to wait after a target type change.")

    private val generateClicksBasedOnDist by boolean("GenerateClicksBasedOnDistance", false)
        .describe("Scale clicks by distance to the target.")
    private val cpsMultiplier by intRange("CPS-Multiplier", 1..2, 1..10) { generateClicksBasedOnDist }
        .describe("Multiplier range for distance-based clicks.")
    private val distanceFactor by floatRange("DistanceFactor", 5F..10F, 1F..10F) { generateClicksBasedOnDist }
        .describe("Distance divisor used to scale clicks.")

    private val generateSpotBasedOnDistance by boolean("GenerateSpotBasedOnDistance", false) { options.rotationsActive }
    private val stickyAim by boolean("StickyAim", false) { options.rotationsActive }
    private val usePointTracker by boolean("UsePointTracker", false) { options.rotationsActive }
        .describe("Pick aim spot scaled by distance to target.")

    private val randomization = RandomizationSettings(this) { options.rotationsActive }
    private val outBorder by boolean("OutBorder", false) { options.rotationsActive }
        .describe("Aim at the edge of the hitbox at times.")

    private val highestBodyPointToTargetValue = choices(
        "HighestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Head"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
        coercedPoint.displayName
    }
    private val highestBodyPointToTarget: String by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue = choices(
        "LowestBodyPointToTarget", arrayOf("Head", "Body", "Feet"), "Feet"
    ) {
        options.rotationsActive
    }.onChange { _, new ->
        val newPoint = RotationUtils.BodyPoint.fromString(new)
        val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
        val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
        coercedPoint.displayName
    }

    private val lowestBodyPointToTarget: String by lowestBodyPointToTargetValue

    private val horizontalBodySearchRange by floatRange(
        "HorizontalBodySearchRange", 0f..1f, 0f..1f
    ) { options.rotationsActive }

    private val fov by float("FOV", 180f, 0f..180f)
        .describe("Field of view in which targets are valid.")

    // Prediction
    private val predictClientMovement by int("PredictClientMovement", 2, 0..5)
        .describe("Ticks of your own movement to predict ahead.")
    private val predictOnlyWhenOutOfRange by boolean(
        "PredictOnlyWhenOutOfRange", false
    ) { predictClientMovement != 0 }
    private val predictEnemyPosition by float("PredictEnemyPosition", 1.5f, -1f..2f)
        .describe("How far ahead to predict the enemy position.")
    private val predictExitingRange by boolean("PredictExitingRange", false) { !simulateCooldown }
        .describe("Fire an early last-hit click when you are about to move out of reach of the target.")
    private val exitPredictTicks by int("ExitPredictTicks", 2, 1..5) { predictExitingRange && !simulateCooldown }
        .describe("Ticks ahead to check for leaving the target's range.")

    private val forceFirstHit by boolean("ForceFirstHit", false) { !respectMissCooldown && !useHitDelay }
        .describe("Force a click on the first hittable tick.")

    // Extra swing
    private val failSwing by boolean("FailSwing", true) { swing && options.rotationsActive }
        .describe("Swing even when no target can be hit.")
    private val respectMissCooldown by boolean(
        "RespectMissCooldown", false
    ) { swing && failSwing && options.rotationsActive }
    private val swingOnlyInAir by boolean("SwingOnlyInAir", true) { swing && failSwing && options.rotationsActive }
        .describe("Only fail-swing when the raycast misses.")
    private val maxRotationDifferenceToSwing by float(
        "MaxRotationDifferenceToSwing", 180f, 0f..180f
    ) { swing && failSwing && options.rotationsActive }
    private val swingWhenTicksLate = boolean("SwingWhenTicksLate", false) {
        swing && failSwing && maxRotationDifferenceToSwing != 180f && options.rotationsActive
    }
    private val ticksLateToSwing by int(
        "TicksLateToSwing", 4, 0..20
    ) { swing && failSwing && swingWhenTicksLate.isActive() && options.rotationsActive }
    private val renderBoxOnSwingFail by boolean("RenderBoxOnSwingFail", false) { failSwing }
        .describe("Render a box where a fail swing happened.")
    private val renderBoxColor = ColorSettingsInteger(this, "RenderBoxColor") { renderBoxOnSwingFail }.with(Color.CYAN)
    private val renderBoxFadeSeconds by float("RenderBoxFadeSeconds", 1f, 0f..5f) { renderBoxOnSwingFail }
        .describe("How long the fail-swing box stays visible.")
    private val soundOnSwingFail by boolean("SoundOnSwingFail", false) { failSwing }
        .describe("Play a sound when a swing misses.")
    private val swingFailSoundVolume by float("SwingFailSoundVolume", 0.5f, 0f..1f) { soundOnSwingFail && failSwing }
        .describe("Volume of the miss sound.")
    private val swingFailSoundPitch by float("SwingFailSoundPitch", 1.2f, 0.5f..2f) { soundOnSwingFail && failSwing }
        .describe("Pitch of the miss sound.")

    // Inventory
    private val simulateClosingInventory by boolean("SimulateClosingInventory", false) { !noInventoryAttack }
        .describe("Briefly close the inventory to attack.")
    private val noInventoryAttack by boolean("NoInvAttack", false)
        .describe("Do not attack while an inventory is open.")
    private val noInventoryDelay by int("NoInvDelay", 200, 0..500) { noInventoryAttack }
        .describe("Delay after closing an inventory before attacking.")
    private val noConsumeAttack by choices(
        "NoConsumeAttack", arrayOf("Off", "NoHits", "NoRotation"), "Off"
    ).subjective()


    private val displayDebug by boolean("Debug", false)
        .describe("Show on-screen combat debug info.")

    // RenderAimPoint
    private val renderAimPointBox by boolean("RenderAimPointBox", false).subjective()
        .describe("Render a box at the current aim point.")
    private val aimPointBoxColor by color("AimPointBoxColor", Color.CYAN) { renderAimPointBox }.subjective()
        .describe("Color of the aim point box.")
    private val aimPointBoxSize by float("AimPointBoxSize", 0.1f, 0f..0.2F) { renderAimPointBox }.subjective()
        .describe("Size of the aim point box.")

    // RangeIndicator
    private val rangeIndicator by boolean("RangeIndicator", false).subjective()
        .describe("Draw a reach ring on the ground around you.")
    private val rangeIndicatorColor by color("RangeIndicatorColor", Color(80, 180, 255, 90)) { rangeIndicator }.subjective()
        .describe("Ring color when no target is in reach.")
    private val rangeIndicatorActiveColor by color("RangeIndicatorActiveColor", Color(90, 255, 120, 110)) { rangeIndicator }.subjective()
        .describe("Ring color when a target is in reach.")
    private val rangeIndicatorOutline by boolean("RangeIndicatorOutline", true) { rangeIndicator }.subjective()
        .describe("Draw an outline around the reach ring.")

    private val clickerGroup = Configurable("Clicker")
    private val rangeGroup = Configurable("Range")
    private val targetGroup = Configurable("Target")
    private val targetFiltersGroup = Configurable("Filters")
    private val rotationsGroup = Configurable("Rotations")
    private val aimPointGroup = Configurable("AimPoint")
    private val raycastGroup = Configurable("Raycast")
    private val autoBlockingGroup = Configurable("AutoBlocking")
    private val smartAutoBlockingGroup = Configurable("SmartAutoBlock")
    private val failSwingGroup = Configurable("FailSwing")
    private val targetRenderingGroup = Configurable("TargetRendering")
    private val rangeIndicatorGroup = Configurable("RangeIndicator")

    init {
        moveValues(clickerGroup,
            "SimulateCooldown", "SimulateDoubleClicking", "CPS", "ClickPattern", "HurtTime", "ClickOnly",
            "GenerateClicksBasedOnDistance", "CPS-Multiplier", "DistanceFactor", "UseHitDelay",
            "HitDelayTicks", "ForceFirstHit", "RespectMissCooldown", "PostRotationAttack")

        moveValues(rangeGroup,
            "Range", "ScanRange", "ThroughWallsRange", "RangeSprintReduction", "RangeChance", "FOV")

        moveValues(targetFiltersGroup,
            "OnSwording", "OnScaffold", "OnDestroyBlock", "NoScaffold", "NoFly", "NoEat",
            "NoBlocking", "BlinkCheck")
        targetGroup.addValue(targetFiltersGroup)
        moveValues(targetGroup,
            "TargetMode", "StickyTarget", "LimitedMultiTargets", "MaxSwitchFOV", "SwitchDelay", "Priority",
            "ActivationSlot", "PreferredSlot")

        options.nestInto(rotationsGroup)
        randomization.nestInto(rotationsGroup)
        rotationsGroup.addValue(pointTracker)
        moveValues(rotationsGroup,
            "GenerateSpotBasedOnDistance", "StickyAim", "OutBorder", "PredictClientMovement",
            "PredictOnlyWhenOutOfRange", "PredictEnemyPosition", "PredictExitingRange", "ExitPredictTicks",
            "UsePointTracker")

        moveValues(aimPointGroup,
            "HighestBodyPointToTarget", "LowestBodyPointToTarget", "HorizontalBodySearchRange")
        moveValues(raycastGroup, "RayCast", "RayCastIgnored", "LivingRayCast", "Raytrace")

        moveValues(smartAutoBlockingGroup,
            "SmartAutoBlock", "ForceBlockWhenStill", "CheckEnemyWeapon", "BlockRange",
            "MaxOwnHurtTime", "MaxOpponentDirectionDiff", "MaxOpponentSwingProgress")
        autoBlockingGroup.addValue(smartAutoBlockingGroup)
        moveValues(autoBlockingGroup,
            "AutoBlock", "BlockMaxRange", "UnblockMode", "ReleaseAutoBlock", "ForceBlockRender",
            "IgnoreTickRule", "BlockRate", "UpdatedNCPAutoBlock", "SwitchStartBlock",
            "InteractAutoBlock", "BlinkAutoBlock", "BlinkBlockTicks")

        moveValues(failSwingGroup,
            "Swing", "FailSwing", "SwingOnlyInAir", "MaxRotationDifferenceToSwing",
            "SwingWhenTicksLate", "TicksLateToSwing", "RenderBoxOnSwingFail", "RenderBoxColor",
            "RenderBoxFadeSeconds", "SoundOnSwingFail", "SwingFailSoundVolume", "SwingFailSoundPitch")
        moveValues(targetRenderingGroup, "RenderAimPointBox", "AimPointBoxColor", "AimPointBoxSize")
        moveValues(rangeIndicatorGroup,
            "RangeIndicator", "RangeIndicatorColor", "RangeIndicatorActiveColor", "RangeIndicatorOutline")

        addValues(listOf(
            clickerGroup, rangeGroup, targetGroup, rotationsGroup, aimPointGroup, raycastGroup,
            autoBlockingGroup, failSwingGroup, targetRenderingGroup, rangeIndicatorGroup,
        ))
    }
    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var hittable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0
    private var clicks = 0
    private var attackTickTimes = mutableListOf<Pair<MovingObjectPosition, Int>>()

    // KeepSprint "Watchdog" sprint-reset cycle: tick N-1 stop -> tick N hit -> tick N+1 resume
    private enum class SprintResetStage { IDLE, STOPPED, ATTACKED }

    // A window that never gets its hit must not keep the player at walking speed. The click that
    // opened the window is carried over, so under normal conditions the hit lands one tick later.
    private const val SPRINT_RESET_TIMEOUT_TICKS = 3

    private var sprintResetStage = SprintResetStage.IDLE
    private var sprintResetTick = 0
    private var sprintResetAttackTick = -1
    private var sprintResumeTick = -1

    // Sprint was actually on when the window opened - this is what has to come back afterwards.
    private var sprintResetWantSprint = false
    private var sprintResetCarryClick = false

    // Container Delay
    private var containerOpen = -1L

    // Block status
    var renderBlocking = false
    var blockStatus = false
    private var blockStopInDead = false

    // Switch Delay
    private val switchTimer = MSTimer()

    // Blink AutoBlock
    private var blinked = false

    // Swing fails
    private val swingFails = mutableListOf<SwingFailData>()

    // text
    private val textElement = Text()
    /**
     * Disable kill aura module
     */
    override fun onToggle(state: Boolean) {
        target = null
        hittable = false
        prevTargetEntities.clear()
        attackTickTimes.clear()
        attackTimer.reset()
        clicker.reset()
        clicks = 0

        // Never leave the Watchdog sprint-reset window open when the aura is turned off.
        if (!state) {
            val player = mc.thePlayer
            if (player != null) finishSprintReset(player) else clearSprintResetState()
        }

        if (blinkAutoBlock) {
            BlinkUtils.unblink()
            blinked = false
        }

        if (autoF5) mc.gameSettings.thirdPersonView = 0

        stopBlocking(true)

        synchronized(swingFails) {
            swingFails.clear()
        }
    }

    val onRotationUpdate = handler<RotationUpdateEvent> {
        update()
    }

    fun update() {
        if (cancelRun || (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay))) return

        // Update target
        updateTarget()

        if (autoF5) {
            if (mc.gameSettings.thirdPersonView != 1 && target != null) {
                mc.gameSettings.thirdPersonView = 1
            }
        }
    }

    val onWorld = handler<WorldEvent> {
        attackTickTimes.clear()

        // A new world resets the player, so drop any half-open sprint window.
        clearSprintResetState()

        if (blinkAutoBlock && BlinkUtils.isBlinking) BlinkUtils.unblink()

        synchronized(swingFails) {
            swingFails.clear()
        }
    }

    /**
     * Safety net for the sprint-reset window: [onToggle] already hands sprint back, so this only
     * runs when that hook failed half-way. Without it [MovementUtils.sprintResetActive] could stay
     * set and [Sprint] would force sprint off on every following tick.
     */
    override fun onDisable() {
        if (sprintResetStage == SprintResetStage.IDLE) return

        val player = mc.thePlayer
        if (player != null) finishSprintReset(player) else clearSprintResetState()
    }

    /**
     * Tick event
     */
    val onTick = handler<Gam