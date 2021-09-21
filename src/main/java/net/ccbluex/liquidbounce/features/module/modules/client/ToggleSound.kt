package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "ToggleSound", category = ModuleCategory.CLIENT, canEnable = false)
object ToggleSound : Module() {
    private val moveValue = ListValue("Mode", arrayOf("None","Click","Custom"),"Click")

    fun playSound(enable: Boolean){
        when(moveValue.get().lowercase()){
            "click" -> {
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.click"), 1F))
            }

            "custom" -> {
                if(enable){
                    LiquidBounce.tipSoundManager.enableSound.asyncPlay()
                }else{
                    LiquidBounce.tipSoundManager.disableSound.asyncPlay()
                }
            }
        }
    }
}