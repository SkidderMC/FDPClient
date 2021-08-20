package net.ccbluex.liquidbounce.ui.ultralight.view

class DynamicGuiView(url: String) : GuiView(url) {
    override fun initGui() {
        init()
    }

    override fun onGuiClosed() {
        destroy()
    }
}