/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import org.lwjgl.input.Mouse


@ModuleInfo(name = "AutoPlace", category = ModuleCategory.PLAYER)
object AutoPlace : Module() {

    private val dl = FloatValue("Delay", 0F, 0F, 10F)
    private val md = BoolValue("MouseDown", false)
    private var l = 0L
    private var f = 0
    private var lm: MovingObjectPosition? = null
    private var lp: BlockPos? = null

    @EventTarget
    fun onRender(event: Render3DEvent) {
        if (mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
            val i = mc.thePlayer.heldItem
            if (i != null && i.item is ItemBlock) {
                val m = mc.objectMouseOver
                if (m != null && m.typeOfHit == MovingObjectType.BLOCK && (m.sideHit != EnumFacing.UP && m.sideHit != EnumFacing.DOWN) || (m.sideHit == EnumFacing.NORTH || m.sideHit == EnumFacing.EAST || m.sideHit == EnumFacing.SOUTH || m.sideHit == EnumFacing.WEST)) {
                    if (this.lm != null && this.f.toDouble() < dl.get()) {
                        ++this.f
                    } else {
                        this.lm = m
                        val pos = m.blockPos
                        if (this.lp == null || pos.x != lp!!.x || pos.y != lp!!.y || pos.z != lp!!.z) {
                            val b = mc.theWorld.getBlockState(pos).block
                            if (b != null && b !== Blocks.air && b !is BlockLiquid) {
                                if (!md.get() || Mouse.isButtonDown(1)) {
                                    val n = System.currentTimeMillis()
                                    if (n - this.l >= 25L) {
                                        this.l = n
                                        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, i, pos, m.sideHit, m.hitVec)) {
                                            MouseUtils.setMouseButtonState(1, true)
                                            mc.thePlayer.swingItem()
                                            mc.itemRenderer.resetEquippedProgress()
                                            MouseUtils.setMouseButtonState(1, false)
                                            this.lp = pos
                                            this.f = 0
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}