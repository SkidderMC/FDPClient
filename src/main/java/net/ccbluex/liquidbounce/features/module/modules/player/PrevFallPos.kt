package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.abs

// THANKS FUNC16 GIVE ME THIS IDEA!
@ModuleInfo(name = "PrevFallPos", category = ModuleCategory.PLAYER)
class PrevFallPos : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Box", "OtherBox", "Outline"), "Box")
    private val outlineWidth = FloatValue("Outline-Width", 3f, 0.5f, 5f).displayable { modeValue.equals("Outline") }
    private val fallDist = FloatValue("FallDist", 1.15F, 0F, 5F)
    private val colorRedValue = IntegerValue("R", 255, 0, 255)
    private val colorGreenValue = IntegerValue("G", 255, 0, 255)
    private val colorBlueValue = IntegerValue("B", 255, 0, 255)
    private val colorAlphaValue = IntegerValue("A", 130, 0, 255)
    private val colorRainbow = BoolValue("Rainbow", false)

    private var pos: BlockPos? = null

    override fun onEnable() {
        pos = null
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        pos = if (!mc.thePlayer.onGround) {
            val fallingPlayer = FallingPlayer(mc.thePlayer)
            val collLoc = fallingPlayer.findCollision(60) // null -> too far to calc or fall pos in void
            if (abs((collLoc?.y ?: 0) - mc.thePlayer.posY) > (fallDist.get() + 1)) {
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

        val color = if (colorRainbow.get()) ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
        when (modeValue.get().lowercase()) {
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
