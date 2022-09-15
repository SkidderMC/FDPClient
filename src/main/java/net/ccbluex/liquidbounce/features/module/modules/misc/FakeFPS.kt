/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.Minecraft

@ModuleInfo(name = "FakeFPS", category = ModuleCategory.MISC)
class FakeFPS : Module() {
    private val maxFps:IntegerValue = object : IntegerValue("MaxFPS",1000,30,3000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (minFps.get() > newValue) set(minFps.get())
        }
    }

    private val minFps:IntegerValue = object : IntegerValue("MinFPS",900,30,3000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            if (maxFps.get() < newValue) set(maxFps.get())
        }
    }

    private var minecraftFPS = 0
    private var fps = 0

    fun getFakeFPS(): Int {
        if (minecraftFPS != Minecraft.getDebugFPS()) {
            fps = RandomUtils.nextInt(minFps.get(),maxFps.get())
            minecraftFPS = Minecraft.getDebugFPS()
        }
        return fps
    }
}