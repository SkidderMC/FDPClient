package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.EntityPlayer
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.charset.StandardCharsets

@ModuleInfo(name = "KillInsults", category = ModuleCategory.MISC)
object KillInsults : Module() {
    var insultWords = mutableListOf<String>()
    private var target: EntityPlayer? = null

    val modeValue = ListValue(
        "Mode", arrayOf(
            "Clear",
            "WithWords",
            "RawWords"
        ), "RawWords"
    )
    private val waterMarkValue = BoolValue("WaterMark", true)
    val insultFile=File(LiquidBounce.fileManager.dir, "insult.json")

    init {
        loadFile()
    }

    fun loadFile(){
        fun convertJson(){
            insultWords.clear()
            insultWords.addAll(IOUtils.toString(FileInputStream(insultFile),"utf-8").split("\n")
                .filter { it.isNotBlank() })
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(insultFile), StandardCharsets.UTF_8))
            val json=JsonArray()
            insultWords.map { JsonPrimitive(it) }.forEach(json::add)
            writer.write(FileManager.PRETTY_GSON.toJson(json))
            writer.close()
        }

        try {
            //check file exists
            if(!insultFile.exists()){
                val fos = FileOutputStream(insultFile)
                IOUtils.copy(KillInsults::class.java.classLoader.getResourceAsStream("assets/minecraft/fdpclient/misc/insult.json"), fos)
                fos.close()
            }
            //read it
            val json=JsonParser().parse(IOUtils.toString(FileInputStream(insultFile),"utf-8"))
            if(json.isJsonArray){
                insultWords.clear()
                json.asJsonArray.forEach{
                    insultWords.add(it.asString)
                }
            }else{
                // not jsonArray convert it to jsonArray
                convertJson()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            convertJson()
        }
    }

    override fun onEnable() {
        target = null
    }

    override val tag: String
        get() = modeValue.get()

    fun getRandomOne():String{
        return insultWords[RandomUtils.nextInt(0, insultWords.size-1)]
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityPlayer) {
            target = event.targetEntity
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        if (target != null && target!!.isDead) {
            val name=target!!.name
            LiquidBounce.hud.addNotification(Notification("Killed","Killed $name.", NotifyType.INFO))
            when (modeValue.get().toLowerCase()) {
                "clear" -> {
                    sendInsultWords("L $name",name)
                }
                "withwords" -> {
                    sendInsultWords("L $name " + getRandomOne(),name)
                }
                "rawwords" -> {
                    sendInsultWords(getRandomOne(),name)
                }
            }
            target = null
        }
    }

    private fun sendInsultWords(msg: String, name: String) {
        var message = msg.replace("%name%",name)
        if (waterMarkValue.get()) {
            message = "[FDPClient] $message"
        }
        mc.thePlayer.sendChatMessage(message)
    }
}