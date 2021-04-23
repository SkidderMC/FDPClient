package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "InventoryAnimation", description = "Display a animation when you open inventory", category = ModuleCategory.RENDER)
class InventoryAnimation : Module() {
    val moveValue=ListValue("Mode", arrayOf("Slide","Zoom"),"Slide")
    val timeValue=IntegerValue("Time",300,100,500)
}