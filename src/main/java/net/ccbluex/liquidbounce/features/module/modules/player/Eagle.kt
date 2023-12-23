/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.utils.MouseUtils
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import org.lwjgl.input.Mouse
import net.ccbluex.liquidbounce.utils.timer.MSTimer

@ModuleInfo(name = "Eagle", category = ModuleCategory.PLAYER)
object Eagle : Module() {
    
    private val motionPredictValue = FloatValue("MotionPredictAmount", 0.2f, 0.0f, 2.0f)
    private val limitTimeValue = BoolValue("SneakTimeLimit", false)
    private val holdTime = IntegerValue("MaxSneakTime", 120, 0, 900).displayable{ limitTimeValue.get() }
    private val onlyGround = BoolValue("OnlyGround", true)
    private val onlyLookingDown = BoolValue("OnlyLookingDown", true)
    private val onlyMovingBack = BoolValue("OnlyMovingBack", true)
    private val autoPlace = BoolValue("AutoPlace", true)
    private val md = BoolValue("PlaceOnMouseDown",  true). displayable { autoPlace.get() }
    
    private val holdTimer = MSTimer()
    
    private var sneakValue = false
    
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!limitTimeValue.get()) {
            sneakValue = false
        }
        if ( ( !onlyGround.get() || mc.thePlayer.onGround ) && ( !onlyLookingDown.get() || mc.thePlayer.rotationPitch.toDouble() > 65.0 ) && ( !onlyMovingBack.get() || mc.gameSettings.keyBindBack.pressed ) &&
                mc.theWorld.getBlockState(BlockPos(mc.thePlayer.posX + mc.thePlayer.motionX.toDouble() * motionPredictValue.get().toDouble(), mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + mc.thePlayer.motionZ.toDouble() * motionPredictValue.get().toDouble())).block == Blocks.air) {
            sneakValue = true
            holdTimer.reset()
        } else if (holdTimer.hasTimePassed(holdTime.get().toLong()) && limitTimeValue.get()) {
            sneakValue = false
        }
        mc.gameSettings.keyBindSneak.pressed = (GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || sneakValue)
    }
    
    override fun onEnable() {
        sneakValue = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
        holdTimer.reset()
    }

    override fun onDisable() {
        mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }

    private var l = 0L
    private var f = 0
    private var lm: MovingObjectPosition? = null
    private var lp: BlockPos? = null

    @EventTarget
    fun onRender(event: Render3DEvent) {
        if (autoPlace.get() && sneakValue) {
            if (mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
                val i = mc.thePlayer.heldItem
                if (i != null && i.item is ItemBlock) {
                    val m = mc.objectMouseOver
                    if (m != null && m.typeOfHit == MovingObjectType.BLOCK && (m.sideHit != EnumFacing.UP && m.sideHit != EnumFacing.DOWN) || (m.sideHit == EnumFacing.NORTH || m.sideHit == EnumFacing.EAST || m.sideHit == EnumFacing.SOUTH || m.sideHit == EnumFacing.WEST)) {
                        if (this.lm != null && this.f.toDouble() < 1f) {
                            ++this.f
                        } else {
                            this.lm = m
                            val pos = m.blockPos
                            if (this.lp == null || pos.x != lp!!.x || pos.y != lp!!.y || pos.z != lp!!.z) {
                                val b = mc.theWorld.getBlockState(pos).block
                                if (b != null && b !== Blocks.air && b !is BlockLiquid) {
                                    if (!md.get() || Mouse.isButtonDown(1)) {
                                        val n = System.currentTimeMillis()
                                        if (n - this.l >= 70L) {
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
}
