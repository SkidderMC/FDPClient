/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import me.zywl.fdpclient.event.BlockBBEvent
import me.zywl.fdpclient.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB

@ModuleInfo(name = "AvoidHazards", category = ModuleCategory.PLAYER)
object AvoidHazards : Module() {

    private val fire = BoolValue("Fire", true)
    private val cobweb = BoolValue("Cobweb", true)
    private val cactus = BoolValue("Cactus", true)
    private val lava = BoolValue("Lava", true)
    private val water = BoolValue("Water", true)
    private val plate = BoolValue("PressurePlate", true)
    private val snow = BoolValue("Snow", true)

    @EventTarget
    fun onBlockBB(e: BlockBBEvent) {
        val thePlayer = mc.thePlayer ?: return

        when (e.block) {
            Blocks.fire -> if (!fire.get()) return

            Blocks.web -> if (!cobweb.get()) return

            Blocks.snow -> if (!snow.get()) return

            Blocks.cactus -> if (!cactus.get()) return

            Blocks.water, Blocks.flowing_water ->
                // Don't prevent water from cancelling fall damage.
                if (!water.get() || thePlayer.fallDistance >= 3.34627 || thePlayer.isInWater) return

            Blocks.lava, Blocks.flowing_lava -> if (!lava.get()) return

            Blocks.wooden_pressure_plate, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.heavy_weighted_pressure_plate -> {
                if (plate.get())
                    e.boundingBox = AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 0.25, e.z + 1.0)
                return
            }

            else -> return
        }

        e.boundingBox = AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 1.0, e.z + 1.0)
    }
}