/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.longjumpmodes

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.NamedMode
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance

open class LongJumpMode(override val modeName: String) : MinecraftInstance, NamedMode {
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onMotion(event: MotionEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onBlockBB(event: BlockBBEvent) {}
    open fun onJump(event: JumpEvent) {}
    open fun onStep(event: StepEvent) {}
    open fun onAttemptJump() {
        mc.thePlayer?.jump()
    }
    open fun onAttemptDisable() {
        net.ccbluex.liquidbounce.features.module.modules.movement.LongJump.state = false
    }

    open fun onEnable() {}
    open fun onDisable() {}
}
