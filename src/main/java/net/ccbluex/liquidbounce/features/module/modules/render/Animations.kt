package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

@ModuleInfo(name = "Animations", category = ModuleCategory.RENDER)
class Animations : Module() {
    val presetValue = ListValue(
        "Preset", arrayOf(
            "Akrien", "Avatar", "ETB", "Exhibition", "Push", "Reverse",
            "Shield", "SigmaNew", "SigmaOld", "Slide", "SlideDown", "HSlide", "Swong", "VisionFX",
            "Swank", "Jello", "None", "Rotate"
        ),
        "SlideDown"
    )

    val modeValue = ListValue("InvMode", arrayOf("None", "Slide", "Zoom"), "Slide")
    val timeValue = IntegerValue("InvTime", 500, 100, 500).displayable { !modeValue.equals("None") }
    val translateX = FloatValue("TranslateX", 0.0f, 0.0f, 1.5f)
    val translateY = FloatValue("TranslateY", 0.0f, 0.0f, 0.5f)
    val translateZ = FloatValue("TranslateZ", 0.0f, 0.0f, -2.0f)
    val itemPosX = FloatValue("ItemPosX", 0.56F, -1.0F, 1.0F)
    val itemPosY = FloatValue("ItemPosY", -0.52F, -1.0F, 1.0F)
    val itemPosZ = FloatValue("ItemPosZ", -0.71999997F, -1.0F, 1.0F)
    val itemScale = FloatValue("ItemScale", 0.4f, 0.0f, 2.0f)
    val swingAnim = BoolValue("SwingAnim", false)
    val swingSpeed = FloatValue("SwingSpeed", 1f, 0.5f, 5.0f)
    val anythingBlock = BoolValue("AnythingBlock", false)

    override val tag: String
        get() = presetValue.get()
}
