/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.other.Breaker
import net.ccbluex.liquidbounce.features.module.modules.player.ChestAura
import net.ccbluex.liquidbounce.features.module.modules.movement.Scaffold
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Rotations", category = ModuleCategory.CLIENT)
object Rotations : Module() {
    val headValue = BoolValue("Head", false)
    val bodyValue = BoolValue("Body", false)
    val fixedValue = ListValue("SensitivityFixed", arrayOf("None", "Old", "New"), "New")
    val rotationMode = ListValue("Mode", arrayOf("Normal", "Silent"), "Normal")
    val rotatingCheckValue = BoolValue("RotatingCheck", false)
    val nanValue = BoolValue("NaNCheck", true)
    val fakeValue = BoolValue("Ghost", true)
    var R = FloatValue("R", 255f, 0f, 255f)
    var G = FloatValue("G", 255f, 0f, 255f)
    var B = FloatValue("B", 255f, 0f, 255f)
    var Alpha = FloatValue("Alpha", 100f, 0f, 255f)

    var playerYaw: Float? = null

    @JvmStatic
    var prevHeadPitch = 0f

    @JvmStatic
    var headPitch = 0f

    @JvmStatic
    fun lerp(tickDelta: Float, old: Float, new: Float): Float {
        return old + (new - old) * tickDelta
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (RotationUtils.serverRotation != null && headValue.get())
            mc.thePlayer.rotationYawHead = RotationUtils.serverRotation.yaw
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (!bodyValue.get() || !shouldRotate() || mc.thePlayer == null)
            return

        val packet = event.packet
        if (packet is C03PacketPlayer.C06PacketPlayerPosLook || packet is C03PacketPlayer.C05PacketPlayerLook) {
            playerYaw = (packet as C03PacketPlayer).yaw
            mc.thePlayer.renderYawOffset = packet.getYaw()
            mc.thePlayer.rotationYawHead = packet.getYaw()
        } else {
            if (playerYaw != null)
                mc.thePlayer.renderYawOffset = this.playerYaw!!
            mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        prevHeadPitch = headPitch
        headPitch = RotationUtils.serverRotation.pitch
        val thePlayer = mc.thePlayer

        if (thePlayer == null) {
            playerYaw = null
            return
        }

        playerYaw = RotationUtils.serverRotation.yaw

        if (headValue.get())
            thePlayer.rotationYawHead = RotationUtils.serverRotation.yaw
    }

    private fun getState(module: Class<out Module>) = FDPClient.moduleManager[module]!!.state

    @JvmStatic
    fun shouldRotate(): Boolean {
        val killAura = FDPClient.moduleManager.getModule(KillAura::class.java) as KillAura
        val disabler = FDPClient.moduleManager.getModule(Disabler::class.java) as Disabler
        return getState(Scaffold::class.java) ||
                (killAura.state && killAura.currentTarget != null) ||
                (disabler.state && disabler.state) || getState(Breaker::class.java) ||
                getState(ChestAura::class.java) || getState(Flight::class.java)
    }

}
