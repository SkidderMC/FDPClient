/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.FDPClient.hud
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.utils.client.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.client.AnticheatModeAdvisor
import net.ccbluex.liquidbounce.utils.client.ModeRisk
import net.ccbluex.liquidbounce.utils.client.ServerObserver
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

private val CRITICAL_MODES = arrayOf(
    "Packet", "NCPPacket", "BlocksMC", "BlocksMC2", "NoGround", "Hop", "TPHop",
    "Jump", "LowJump", "CustomMotion", "Visual"
)

object Criticals : Module("Criticals", Category.COMBAT, Category.SubCategory.COMBAT_LEGIT) {

    private val antiCheatValue = choices(
        "AntiCheat", arrayOf("Auto", "All", "NCP", "AAC", "Grim", "Vulcan"), "Auto"
    ).onChanged { refreshModeChoices(force = true) }
        .describe("Filter critical modes by a detected or explicitly selected anti-cheat family.")
    private val antiCheat by antiCheatValue

    private val modeValue = choices("Mode", CRITICAL_MODES.copyOf(), "Packet")
        .describe("Critical-hit mode with compatibility guidance for the selected anti-cheat.")
        .apply { onChanged { if (state) warnAboutMode(it) } }
    val mode by modeValue

    val delay by int("Delay", 0, 0..500)
        .describe("Minimum delay between critical hits.")
    private val hurtTime by int("HurtTime", 10, 0..10)
        .describe("Only crit when target hurt-time is at or below this.")
    private val customMotionY by float("Custom-Y", 0.2f, 0.01f..0.42f) { mode == "CustomMotion" }
        .describe("Upward motion used by the custom crit mode.")

    val msTimer = MSTimer()
    private var lastProfileFingerprint = ""
    private var lastModeWarning = ""

    override fun onEnable() {
        refreshModeChoices(force = true)
        warnAboutMode(mode)
        if (mode == "NoGround")
            mc.thePlayer.tryJump()
    }

    val onAttack = handler<AttackEvent> { event ->
        refreshModeChoices()
        if (event.targetEntity is EntityLivingBase) {
            val thePlayer = mc.thePlayer ?: return@handler
            val entity = event.targetEntity

            if (!thePlayer.onGround || thePlayer.isOnLadder || thePlayer.isInWeb || thePlayer.isInLiquid ||
                thePlayer.ridingEntity != null || entity.hurtTime > hurtTime ||
                Flight.handleEvents() || !msTimer.hasTimePassed(delay)
            )
                return@handler

            val (x, y, z) = thePlayer

            when (mode.lowercase()) {
                "packet" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.0625, z, true),
                        C04PacketPlayerPosition(x, y, z, false)
                    )
                    thePlayer.onCriticalHit(entity)
                }

                "ncppacket" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.11, z, false),
                        C04PacketPlayerPosition(x, y + 0.1100013579, z, false),
                        C04PacketPlayerPosition(x, y + 0.0000013579, z, false)
                    )
                    mc.thePlayer.onCriticalHit(entity)
                }

                "blocksmc" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.001091981, z, true),
                        C04PacketPlayerPosition(x, y, z, false)
                    )
                }

                "blocksmc2" -> {
                    if (thePlayer.ticksExisted % 4 == 0) {
                        sendPackets(
                            C04PacketPlayerPosition(x, y + 0.0011, z, true),
                            C04PacketPlayerPosition(x, y, z, false)
                        )
                    }
                }

                "hop" -> {
                    thePlayer.motionY = 0.1
                    thePlayer.fallDistance = 0.1f
                    thePlayer.onGround = false
                }

                "tphop" -> {
                    sendPackets(
                        C04PacketPlayerPosition(x, y + 0.02, z, false),
                        C04PacketPlayerPosition(x, y + 0.01, z, false)
                    )
                    thePlayer.setPosition(x, y + 0.01, z)
                }

                "jump" -> thePlayer.motionY = 0.42
                "lowjump" -> thePlayer.motionY = 0.3425
                "custommotion" -> thePlayer.motionY = customMotionY.toDouble()
                "visual" -> thePlayer.onCriticalHit(entity)
            }

            msTimer.reset()
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet is C03PacketPlayer && mode == "NoGround")
            packet.onGround = false
    }

    override val tag
        get() = mode

    private fun refreshModeChoices(force: Boolean = false) {
        val observed = ServerObserver.guessAnticheat()?.takeUnless { it.equals("Unknown", true) }
        val fingerprint = "$antiCheat|${observed.orEmpty()}"
        if (!force && fingerprint == lastProfileFingerprint) return
        lastProfileFingerprint = fingerprint

        val available = AnticheatModeAdvisor.filteredModes("Criticals", antiCheat, observed, CRITICAL_MODES)
        modeValue.updateValues(available)
        if (available.none { it.equals(mode, true) }) {
            val previous = mode
            modeValue.set(available.first())
            if (state && !previous.equals(mode, true)) {
                hud.addNotification(Notification("Criticals compatibility",
                    "$previous unavailable here; switched to $mode.", Type.WARNING, 4500))
            }
        } else if (state) {
            warnAboutMode(mode)
        }
    }

    private fun warnAboutMode(selectedMode: String) {
        val observed = ServerObserver.guessAnticheat()?.takeUnless { it.equals("Unknown", true) }
        if (antiCheat.equals("Auto", true) && observed == null) return
        val advice = AnticheatModeAdvisor.assess("Criticals", selectedMode, antiCheat, observed)
        if (advice.risk == ModeRisk.RECOMMENDED) return

        val warningKey = "${advice.profile}|$selectedMode|${advice.risk}"
        if (warningKey == lastModeWarning) return
        lastModeWarning = warningKey
        val message = when (advice.risk) {
            ModeRisk.EXPERIMENTAL -> "$selectedMode is experimental on ${advice.profile.displayName}; server correction is possible."
            ModeRisk.LIKELY_DETECTED -> "Probable detection: $selectedMode on ${advice.profile.displayName}. Prefer ${advice.recommendedMode}."
            ModeRisk.RECOMMENDED -> return
        }
        hud.addNotification(Notification("Criticals compatibility", message, Type.WARNING, 4500))
    }
}
