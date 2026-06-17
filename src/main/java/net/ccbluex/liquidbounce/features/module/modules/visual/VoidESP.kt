/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.floor

object VoidESP : Module("VoidESP", Category.VISUAL, Category.SubCategory.RENDER_OVERLAY, gameDetecting = false, spacedName = "Void ESP") {

    private val players by boolean("Players", true)
    private val otherEntities by boolean("OtherEntities", false)
    private val selfWarning by boolean("SelfWarning", true)
    private val color by color("Color", Color(255, 60, 60))

    private var wasOverVoid = false

    private fun isOverVoid(x: Double, y: Double, z: Double): Boolean {
        val world = mc.theWorld ?: return false
        val bx = floor(x).toInt()
        val bz = floor(z).toInt()
        var checkY = y.toInt()
        if (checkY < 0) return true
        while (checkY >= 0) {
            if (!world.isAirBlock(BlockPos(bx, checkY, bz))) return false
            checkY--
        }
        return true
    }

    val onRender3D = handler<Render3DEvent> {
        val world = mc.theWorld ?: return@handler
        val self = mc.thePlayer ?: return@handler
        val boxColor = color

        for (entity in world.loadedEntityList.toList()) {
            if (entity === self) continue

            val isPlayer = entity is EntityPlayer
            if (isPlayer && !players) continue
            if (!isPlayer && !otherEntities) continue

            if (isOverVoid(entity.posX, entity.posY, entity.posZ))
                drawEntityBox(entity, boxColor, false)
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (!selfWarning) {
            wasOverVoid = false
            return@handler
        }

        val self = mc.thePlayer ?: return@handler
        val overVoid = isOverVoid(self.posX, self.posY, self.posZ)

        if (overVoid && !wasOverVoid)
            addNotification(Notification("VoidESP", "You are standing over the void!", Type.ERROR, 2000))

        wasOverVoid = overVoid
    }
}
