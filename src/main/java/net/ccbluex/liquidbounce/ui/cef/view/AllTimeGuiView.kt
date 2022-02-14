package net.ccbluex.liquidbounce.ui.cef.view

import net.ccbluex.liquidbounce.ui.cef.page.Page

open class AllTimeGuiView(page: Page) : GuiView(page) {
    init {
        init()
    }

    fun finalize() {
        destroy() // destroy the renderer when the view is destroyed
    }
}