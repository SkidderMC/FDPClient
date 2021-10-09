package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.AxisAlignedBB

@ModuleInfo(name = "Spider", category = ModuleCategory.MOVEMENT)
class Spider : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Collide", "Motion"/*,"AAC4"*/), "Collide")
    private val heightValue = IntegerValue("Height", 2, 1, 10)
    private val motionValue = FloatValue("Motion", 0.42F, 0.1F, 1F).displayable { modeValue.equals("Motion") }

    private var startHeight=0.0
    private var groundHeight = 0.0
    private var modifyBB = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(!mc.thePlayer.isCollidedHorizontally || !mc.gameSettings.keyBindForward.pressed || mc.thePlayer.posY-heightValue.get()>startHeight) {
            if(mc.thePlayer.onGround){
                startHeight=mc.thePlayer.posY
                groundHeight=mc.thePlayer.posY
            }
            modifyBB = false
            return
        }
        
        modifyBB = true
        
        when (modeValue.get().lowercase()){
            "collide" -> {
                if(mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    groundHeight=mc.thePlayer.posY
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
        if(!modifyBB || mc.thePlayer.motionY>0.0) return
        
        when (modeValue.get().lowercase()) {
            "collide" -> {
                if(event.y>groundHeight-0.0156249 && event.y<groundHeight+0.0156249)
                    event.boundingBox=AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(),
                        event.x+1.0, event.y+1.0, event.z+1.0)
            }
        }
    }
}
