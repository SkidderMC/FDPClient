package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.util.BlockPos
import java.awt.Color

// THANKS FUNC16 GIVE ME THIS IDEA!
@ModuleInfo(name = "PrevFallPos", description = "Preview FallDown Pos", category = ModuleCategory.PLAYER)
class PrevFallPos : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline"), "Box")
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f)
    private val fallDist = FloatValue("FallDist",1.15F,0F,5F)
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("A", 130, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)

    private var pos:BlockPos?=null

    override fun onEnable() {
        pos=null
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        pos = if(mc.thePlayer.fallDistance>fallDist.get()){
            val fallingPlayer=FallingPlayer(mc.thePlayer.posX,mc.thePlayer.posY,mc.thePlayer.posZ,mc.thePlayer.motionX,mc.thePlayer.motionY,mc.thePlayer.motionZ,mc.thePlayer.rotationYaw,mc.thePlayer.moveStrafing,mc.thePlayer.moveForward)
            val collLoc=fallingPlayer.findCollision(60) ?: return // null -> too far to calc or fall pos in void
            collLoc
        }else{
            null
        }
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent){
        pos?:return

        val color=if (colorRainbow.get()) ColorUtils.rainbow(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        when (modeValue.get().toLowerCase()) {
            "box" -> {
                RenderUtils.drawBlockBox(pos, color, true, true, outlineWidth.get())
            }
            "otherbox" -> {
                RenderUtils.drawBlockBox(pos, color, false, true, outlineWidth.get())
            }
            "outline" -> {
                RenderUtils.drawBlockBox(pos, color, true, false, outlineWidth.get())
            }
        }
    }
}