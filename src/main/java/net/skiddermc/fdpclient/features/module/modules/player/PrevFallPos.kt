package net.skiddermc.fdpclient.features.module.modules.player

import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.Render3DEvent
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.utils.misc.FallingPlayer
import net.skiddermc.fdpclient.utils.render.ColorUtils
import net.skiddermc.fdpclient.utils.render.RenderUtils
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.FloatValue
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.abs

// THANKS FUNC16 GIVE ME THIS IDEA!
@ModuleInfo(name = "PrevFallPos", category = ModuleCategory.PLAYER)
class PrevFallPos : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline"), "Box")
    private val outlineWidthValue = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val fallDistValue = FloatValue("FallDist", 1.15F, 0F, 5F)
    private val colorRedValue = IntegerValue("R", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorGreenValue = IntegerValue("G", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorBlueValue = IntegerValue("B", 255, 0, 255).displayable { !colorRainbowValue.get() }
    private val colorAlphaValue = IntegerValue("A", 130, 0, 255)
    private val colorRainbowValue = BoolValue("Rainbow", false)

    private var pos: BlockPos? = null

    override fun onEnable() {
        pos = null
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        pos = if (!mc.thePlayer.onGround) {
            val fallingPlayer = FallingPlayer(mc.thePlayer)
            val collLoc = fallingPlayer.findCollision(60) // null -> too far to calc or fall pos in void
            if (abs((collLoc?.y ?: 0) - mc.thePlayer.posY) > (fallDistValue.get() + 1)) {
                collLoc
            } else {
                null
            }
        } else {
            null
        }
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        pos ?: return

        val color = if (colorRainbowValue.get()) ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        when (modeValue.get().lowercase()) {
            "box" -> {
                RenderUtils.drawBlockBox(pos, color, true, true, outlineWidthValue.get())
            }
            "otherbox" -> {
                RenderUtils.drawBlockBox(pos, color, false, true, outlineWidthValue.get())
            }
            "outline" -> {
                RenderUtils.drawBlockBox(pos, color, true, false, outlineWidthValue.get())
            }
        }
    }
}
