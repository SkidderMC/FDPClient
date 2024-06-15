/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import me.zywl.fdpclient.event.EventTarget
import me.zywl.fdpclient.event.Render3DEvent
import me.zywl.fdpclient.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import me.zywl.fdpclient.value.impl.BoolValue
import me.zywl.fdpclient.value.impl.FloatValue
import me.zywl.fdpclient.value.impl.IntegerValue
import me.zywl.fdpclient.value.impl.ListValue
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.abs

@ModuleInfo(name = "PrevFallPos", category = ModuleCategory.VISUAL)
object PrevFallPos : Module() {

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

        val color = if (colorRainbowValue.get()) ColorUtils.rainbowWithAlpha(colorAlphaValue.get()) else Color(
            colorRedValue.get(), colorGreenValue.get(), colorBlueValue.get(), colorAlphaValue.get())
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
