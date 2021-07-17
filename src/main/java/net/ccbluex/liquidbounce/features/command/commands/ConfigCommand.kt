package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import java.io.File

class ConfigCommand : Command("config", arrayOf("cfg")) {
    override fun execute(args: Array<String>) {
        if(args.size>1){
            when(args[1].toLowerCase()){
                "create" -> {
                    if(args.size>2){
                        val file=File(LiquidBounce.fileManager.configsDir,"${args[2]}.json")
                        if(!file.exists()){
                            LiquidBounce.configManager.load(args[2],true)
                            chat("Create config ${args[2]}")
                        }else{
                            chat("Config ${args[2]} already exists")
                        }
                    }else{
                        chatSyntax("${args[1]} <configName>")
                    }
                }

                "load","forceload" -> {
                    if(args.size>2){
                        val file=File(LiquidBounce.fileManager.configsDir,"${args[2]}.json")
                        if(file.exists()){
                            LiquidBounce.configManager.load(args[2],args[1].equals("load",true))
                            chat("Loaded config ${args[2]}")
                        }else{
                            chat("Config ${args[2]} not exists")
                        }
                    }else{
                        chatSyntax("${args[1]} <configName>")
                    }
                }

                "delete" -> {
                    if(args.size>2){
                        val file=File(LiquidBounce.fileManager.configsDir,"${args[2]}.json")
                        if(file.exists())
                            chat("Successfully deleted config ${args[2]}")
                        else
                            chat("Config ${args[2]} not exist")
                    }else{
                        chatSyntax("${args[1]} <configName>")
                    }
                }

                "list" -> {
                    val list=(LiquidBounce.fileManager.configsDir.listFiles() ?: return)
                        .filter { it.isFile }
                        .map {
                            val name=it.name
                            if(name.endsWith(".json"))
                                name.substring(0,name.length-5)
                            else
                                name
                        }

                    chat("Configs(${list.size}):")

                    for (file in list)
                        chat("> $file")
                }

                "save" -> {
                    LiquidBounce.configManager.save(true,true)
                    chat("Saved config ${LiquidBounce.configManager.nowConfig}")
                }

                "reload" -> {
                    LiquidBounce.configManager.load(LiquidBounce.configManager.nowConfig,false)
                    chat("Reloaded config ${LiquidBounce.configManager.nowConfig}")
                }
            }
        }else{
            chatSyntax(arrayOf("create <configName>",
                "load <configName>",
                "forceload <configName>",
                "delete <configName>",
                "reload",
                "list",
                "save"))
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("create", "load", "forceload", "delete", "reload", "list", "save").filter { it.startsWith(args[0], true) }
            2 -> when (args[0].toLowerCase()) {
                    "delete", "load", "forceload" -> {
                        (LiquidBounce.fileManager.configsDir.listFiles() ?: return emptyList())
                            .filter { it.isFile }
                            .map {
                                val name=it.name
                                if(name.endsWith(".json"))
                                    name.substring(0,name.length-5)
                                else
                                    name
                            }
                            .filter { it.startsWith(args[1], true) }
                    }
                    else -> emptyList()
                }
            else -> emptyList()
        }
    }
}