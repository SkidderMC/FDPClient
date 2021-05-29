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

    private var clickedList:ClickList?=null

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

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        clickedList=handleClick(mouseX, mouseY)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun click(mouseX: Int, mouseY: Int) {
        clickedList?:return
    }

    override fun drag(moveX: Int, moveY: Int, mouseX: Int, mouseY: Int, startX: Int, startY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        clickedList?:return

        clickedList!!.x=clickedList!!.x+moveX
        clickedList!!.y=clickedList!!.y+moveY
    }

    private fun handleClick(mouseX: Int, mouseY: Int):ClickList? {
        var clicked: ClickList? =null
        for(clickList in clickLists){
            if(clickList.inTitleArea(mouseX, mouseY)){
                clicked=clickList
                break
            }
        }
        clicked?:return null
        if(clickLists.indexOf(clicked)!=0){
            clickLists.sortBy { if(it == clicked){114514}else{clickLists.indexOf(it)} }
        }
        return clicked
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }
}