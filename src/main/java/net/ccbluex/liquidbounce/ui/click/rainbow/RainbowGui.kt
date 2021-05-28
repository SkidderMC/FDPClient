package net.ccbluex.liquidbounce.ui.click.rainbow

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.ui.click.ClickGui
import net.ccbluex.liquidbounce.ui.click.utils.ClickGuiUtils

// arrow 🢒
class RainbowGui : ClickGui() {
    override val name: String
        get() = "Rainbow"

    private val clickLists = ArrayList<ClickList>()

    override fun load() {
        var y=5
        ModuleCategory.values().forEach {
            val clickList=ClickList(it,5,y,LiquidBounce.moduleManager.modules.filter { module -> module.category==it })

            clickLists.add(clickList)

            y+=30
        }
    }

    override fun render(width: Int, height: Int, mouseX: Int, mouseY: Int, partialTicks: Float) {
        var x = 0
        while (x < (width+50)) {
            var y = 0
            while (y < (height+50)){
                ClickGuiUtils.rainbowRect(x.toDouble(),y.toDouble(),x+50.0,y+50.0,100)
                y += 50
            }
            x += 50
        }

        clickLists.forEach { it.render() }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}