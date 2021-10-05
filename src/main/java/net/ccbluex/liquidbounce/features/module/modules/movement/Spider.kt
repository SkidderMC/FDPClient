package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "Spider", category = ModuleCategory.MOVEMENT)
class Spider : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Collide", "Motion"), "Collide")
    private val heightValue = IntegerValue("Height", 2, 1, 10)
    private val motionValue = FloatValue("Motion", 0.42F, 0.1F, 1F).displayable { modeValue.equals("Motion") }

    private var startHeight=0.0

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!mc.thePlayer.isCollidedHorizontally || !mc.gameSettings.keyBindForward.pressed || mc.thePlayer.posY-heightValue.get()>startHeight) {
            if(mc.thePlayer.onGround){
                startHeight=mc.thePlayer.posY
            }
            return
        }

        when (modeValue.get().lowercase()){
            "collide" -> {
                if(mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                }
            }
            "motion" -> {
                mc.thePlayer.motionY=motionValue.get().toDouble()
            }
        }
    }


    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if(!mc.thePlayer.isCollidedHorizontally || !mc.gameSettings.keyBindForward.pressed || mc.thePlayer.posY-heightValue.get()>startHeight)
            return

        when (modeValue.get().lowercase()) {
            "collide" -> {
                val block=BlockUtils.getBlock(Vec3(mc.thePlayer.posX + (-sin(mc.thePlayer.rotationYaw) * 0.7), mc.thePlayer.posY, mc.thePlayer.posZ + (cos(mc.thePlayer.rotationYaw) * 0.7))) ?: return

                if(block.isFullBlock){
                    event.boundingBox=AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(),
                        event.x+1.0, BigDecimal.valueOf(mc.thePlayer.posY).setScale(0, RoundingMode.DOWN).toDouble(), event.z+1.0)
                }
            }
        }
    }
}