/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import me.zywl.fdpclient.FDPClient
import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.MotionEvent
import me.zywl.fdpclient.event.PacketEvent
import me.zywl.fdpclient.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.exploit.Disabler
import net.ccbluex.liquidbounce.features.module.modules.movement.Flight
import net.ccbluex.liquidbounce.features.module.modules.movement.Scaffold
import net.ccbluex.liquidbounce.features.module.modules.player.Breaker
import net.ccbluex.liquidbounce.features.module.modules.player.ChestAura
import net.ccbluex.liquidbounce.utils.RotationUtils
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "Rotations", category = ModuleCategory.CLIENT)
object Rotations : Module() {
    val headValue = BoolValue("Head", false)
    val bodyValue = BoolValue("Body", false)
    val rotationMode = ListValue("Mode", arrayOf("Normal", "Silent"), "Normal")
    val rotatingCheckValue = BoolValue("RotatingCheck", false)
    val nanValue = BoolValue("NaNCheck", true)
    var R = FloatValue("R", 255f, 0f, 255f)
    var G = FloatValue("G", 255f, 0f, 255f)
    var B = FloatValue("B", 255f, 0f, 255f)
    var Alpha = FloatValue("Alpha", 100f, 0f, 255f)

    private var playerYaw: Float? = null

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
            mc.thePlayer.rotationYawHead = RotationUtils.serverRotation!!.yaw
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
        headPitch = RotationUtils.serverRotation!!.pitch
        val thePlayer = mc.thePlayer

        if (thePlayer == null) {
            playerYaw = null
            return
        }

        playerYaw = RotationUtils.serverRotation!!.yaw

        if (headValue.get())
            thePlayer.rotationYawHead = RotationUtils.serverRotation!!.yaw
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
