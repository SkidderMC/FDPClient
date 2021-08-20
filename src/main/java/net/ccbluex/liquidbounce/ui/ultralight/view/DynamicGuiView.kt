package net.ccbluex.liquidbounce.ui.ultralight.view

class DynamicGuiView(page: Page) : GuiView(page) {
    override fun initGui() {
        init()
    }

    override fun onGuiClosed() {
        destroy()
    }
}