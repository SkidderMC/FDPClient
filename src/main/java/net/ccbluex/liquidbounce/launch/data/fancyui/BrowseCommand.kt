package net.ccbluex.liquidbounce.launch.data.fancyui

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.cef.view.DynamicGuiView
import net.ccbluex.liquidbounce.ui.cef.page.Page

class BrowseCommand : Command("browse", emptyArray()) {

    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            mc.displayGuiScreen(DynamicGuiView(Page(args[1])))
            return
        }
        chatSyntax("browse <url>")
    }
}