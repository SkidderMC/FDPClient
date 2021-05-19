package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.BoolValue
import net.ccbluex.liquidbounce.features.ListValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.minecraft.entity.player.EntityPlayer
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.math.roundToInt

@ModuleInfo(name = "AutoAbuse", description = "Automatically abuse peoples you killed.", category = ModuleCategory.MISC)
object AutoAbuse : Module() {
    var abuseWords: JsonArray? = null
    private var target: EntityPlayer? = null

    val modeValue = ListValue(
        "Mode", arrayOf(
            "Clear",
            "WithWords",
            "RawWords"
        ), "RawWords"
    )
    private val waterMarkValue = BoolValue("WaterMark", true)

    init {
        try {
            //check file exists
            val abuseFile=File(LiquidBounce.fileManager.dir, "abuse.json")
            if(!abuseFile.exists()){
                val fos = FileOutputStream(abuseFile)
                IOUtils.copy(
                    AutoAbuse::class.java.classLoader.getResourceAsStream("abuse.json"),
                    fos
                )
                fos.close()
            }
            //read it
            abuseWords = JsonParser().parse(IOUtils.toString(FileInputStream(abuseFile),"utf-8")).asJsonArray
        } catch (e: Exception) {
            e.printStackTrace()
            abuseWords = JsonArray()
            abuseWords!!.add("Support ur local client!")
            abuseWords!!.add("请支持国人的客户端!")
        }
    }

    override fun onEnable() {
        target = null
    }

    override val tag: String
        get() = modeValue.get()

    fun getRandomOne():String{
        return abuseWords!![(Math.random() * (abuseWords!!.size()-1)).roundToInt()].asString
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
                    sendAbuseWords("L $name",name)
                }
                "withwords" -> {
                    sendAbuseWords("L $name " + getRandomOne(),name)
                }
                "rawwords" -> {
                    sendAbuseWords(getRandomOne(),name)
                }
            }
            target = null
        }
    }

    private fun sendAbuseWords(msg: String,name: String) {
        var message = msg.replace("%name%",name)
        if (waterMarkValue.get()) {
            message = "[FDPClient] $message"
        }
        mc.thePlayer.sendChatMessage(message)
    }
}