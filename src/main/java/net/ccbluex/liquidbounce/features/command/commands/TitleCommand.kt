package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import org.lwjgl.opengl.Display

class TitleCommand : Command("Title", emptyArray()){
    override fun execute(args: Array<String>) {
        var i = 1
        var title = ""
        while(i < args.size) {
            title += args[i] + " "
            i++
        }
        chat(Display.getTitle() + " was changed to " + title)
        Display.setTitle(title)
        if(args.isEmpty()) {
            chatSyntax("Title <Title>")
        }
    }

}
