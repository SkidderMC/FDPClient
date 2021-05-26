package net.ccbluex.liquidbounce.ui.click

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.modules.client.NewClickGui
import net.ccbluex.liquidbounce.ui.click.rainbow.RainbowGui
import net.ccbluex.liquidbounce.utils.MinecraftInstance

class ClickGuiManager : MinecraftInstance() {
    val clickGuis = HashMap<String,ClickGui>()

    init {
        load()
    }

    fun load(){
        registerClickGuis(
            RainbowGui::class.java
        )
    }

    fun initAll(){
        clickGuis.forEach {
            it.value.load()
        }
    }

    fun getNameList():Array<String>{
        val nameArr=mutableListOf<String>()

        clickGuis.forEach {
            nameArr.add(it.key)
        }

        return nameArr.toTypedArray()
    }

    @SafeVarargs
    fun registerClickGuis(vararg clickGuis: Class<out ClickGui>) {
        clickGuis.forEach { registerClickGui(it.newInstance()) }
    }

    fun registerClickGui(clickGui: ClickGui){
        if(clickGui.name.isNotEmpty()){
            clickGuis[clickGui.name]=clickGui
        }
    }

    fun displayGui(){
        val clickGui=clickGuis[(LiquidBounce.moduleManager.getModule(NewClickGui::class.java) as NewClickGui).modeValue.get()] ?: return
        mc.displayGuiScreen(clickGui)
    }
}
