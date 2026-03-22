/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player.nofallmodes

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.NamedMode
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

open class NoFallMode(override val modeName: String) : MinecraftInstance, NamedMode {
    open fun onMove(event: MoveEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onRender2D(event: Render2DEvent) {}
    open fun onRender3D(event: Render3DEvent) {}
    open fun onBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onUpdate() {}
    open fun onTick () {}
    open fun onRotationUpdate() {}
    open fun onEnable() {}
    open fun onDisable() {}
}
