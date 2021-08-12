package net.ccbluex.liquidbounce.script.kotlin

import net.minecraft.client.Minecraft
import java.io.File
import javax.script.ScriptEngineManager

/**
 * @author Liulihaocai
 * Load kotlin scripts dynamically
 */
class KotlinScript(val scriptFile: File) {

    private val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
    private val scriptText = scriptFile.readText(Charsets.UTF_8)

    init {
        scriptEngine.put("mc", Minecraft.getMinecraft())

        scriptEngine.eval(scriptText)
    }
}