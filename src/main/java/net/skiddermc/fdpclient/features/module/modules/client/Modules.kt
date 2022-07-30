package net.skiddermc.fdpclient.features.module.modules.client

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.value.BoolValue
import net.skiddermc.fdpclient.value.ListValue
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "Modules", category = ModuleCategory.CLIENT, canEnable = false)
object Modules : Module() {
    val toggleIgnoreScreenValue = BoolValue("ToggleIgnoreScreen", false)
    private val toggleSoundValue = ListValue("ToggleSound", arrayOf("None", "Click", "Custom"), "Click")

    fun playSound(enable: Boolean) {
        when (toggleSoundValue.get().lowercase()) {
            "click" -> {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
            }

            "custom" -> {
                if (enable) {
                    FDPClient.tipSoundManager.enableSound.asyncPlay()
                } else {
                    FDPClient.tipSoundManager.disableSound.asyncPlay()
                }
            }
        }
    }
}