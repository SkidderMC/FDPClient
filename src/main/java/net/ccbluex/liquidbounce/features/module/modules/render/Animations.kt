package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.FloatValue
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "Animations", description = "Render sword while blocking and inventory animation", category = ModuleCategory.RENDER)
class Animations : Module() {
    val presetValue = ListValue(
        "Preset", arrayOf(
            "Akrien", "Avatar", "ETB", "Exhibition", "Push", "Reverse",
            "Shield", "SigmaNew", "SigmaOld", "Slide", "SlideDown", "HSlide", "Swong", "VisionFX",
            "Swank", "Jello", "None","Rotate"
        ),
        "SlideDown"
    )

    val moveValue = ListValue("InvMode", arrayOf("None", "Slide", "Zoom"), "Slide")
    val timeValue = IntegerValue("InvTime",500,100,500)
    var translateX = FloatValue("TranslateX", 0.0f, 0.0f, 1.5f)
    var translateY = FloatValue("TranslateY", 0.0f, 0.0f, 0.5f)
    var translateZ = FloatValue("TranslateZ", 0.0f, 0.0f, -2.0f)
    val itemPosX = FloatValue("ItemPosX", 0.56F, -1.0F, 1.0F)
    val itemPosY = FloatValue("ItemPosY", -0.52F, -1.0F, 1.0F)
    val itemPosZ = FloatValue("ItemPosZ", -0.71999997F, -1.0F, 1.0F)
    var itemScale = FloatValue("ItemScale", 0.4f, 0.0f, 2.0f)
    var swingAnim = BoolValue("SwingAnim",false)

    override val tag: String
        get() = presetValue.get()
}
