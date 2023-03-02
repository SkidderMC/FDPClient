package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.value.ListValue

//TODO: Add more modes to ClientCrasher

@ModuleInfo(name = "ClientCrasher", category = ModuleCategory.MISC, canEnable = false)
class ClientCrasher : Module() {
    private val modeValue = ListValue("Mode", arrayOf(
        "nullptr",
    ), "nullptr")

    override fun onEnable() {
        when (modeValue.get()) {
            "nullptr" -> {
                mc.thePlayer = null
            }
        }
    }
}
