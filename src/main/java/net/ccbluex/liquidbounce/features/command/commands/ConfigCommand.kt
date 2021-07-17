package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command

class ConfigCommand : Command("config", arrayOf("cfg")) {
    override fun execute(args: Array<String>) {
        if(args.size>1){
            when(args[1].toLowerCase()){
                "load","forceload" -> {
                    if(args.size>2){
                        LiquidBounce.configManager.load(args[2],args[1].equals("load",true))
                        chat("Loaded config ${args[2]}")
                    }else{
                        chatSyntax("${args[1]} <configName>")
                    }
                }
                "save" -> {
                    LiquidBounce.configManager.save(true,true)
                    chat("Saved config ${LiquidBounce.configManager.nowConfig}")
                }
            }
        }else{
            chatSyntax(arrayOf("load <configName>",
                "forceload <configName>",
                "save"))
        }
    }
}