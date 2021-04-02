package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Animations", description = "Render sword while blocking", category = ModuleCategory.RENDER)
class Animations : Module() {
    val presetValue = ListValue(
        "Preset", arrayOf("Sigma"),
        "Sigma"
    )

    override val tag: String
        get() = presetValue.get()
}