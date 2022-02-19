package net.ccbluex.liquidbounce.ui.cef.view

import net.ccbluex.liquidbounce.ui.cef.page.Page

open class DynamicGuiView(page: Page) : GuiView(page) {
    override fun initGui() {
        init()
    }

    override fun onGuiClosed() {
        destroy()
    }
}