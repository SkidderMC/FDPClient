package net.ccbluex.liquidbounce.features.module.modules.`fun`

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.EntityPlayer
import sun.misc.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

@ModuleInfo(name = "AutoAbuse", description = "Automatically abuse peoples you killed.", category = ModuleCategory.FUN)
class AutoAbuse : Module() {
    private var abuseWords: JsonArray? = null
    private var target: EntityPlayer? = null

    val modeValue = ListValue(
        "Mode", arrayOf(
            "Clear",
            "WithWords",
            "RawWords"
        ), "WithWords"
    )
    private val waterMarkValue = BoolValue("WaterMark", true)

    init {
        try {
            //check file exists
            val abuseFile=File(LiquidBounce.fileManager.dir, "abuse.json")
            if(!abuseFile.exists()){
                val fos = FileOutputStream(abuseFile)
                org.apache.commons.io.IOUtils.copy(
                    AutoAbuse::class.java.classLoader.getResourceAsStream("abuse.json"),
                    fos
                )
                fos.close()
            }
            //read it
            abuseWords = JsonParser()
                .parse(
                    String(
                        IOUtils.readAllBytes(FileInputStream(abuseFile)),
                        StandardCharsets.UTF_8
                    )
                ).asJsonArray
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
            LiquidBounce.hud.addNotification(Notification("§cKilled §a$name",NotifyType.INFO))
            when (modeValue.get().toLowerCase()) {
                "clear" -> {
                    sendAbuseWords("L $name")
                }
                "withwords" -> {
                    sendAbuseWords(
                        "L $name " + abuseWords!![(Math.random() * abuseWords!!.size()).roundToInt()
                            .toInt()].asString
                    )
                }
                "rawwords" -> {
                    sendAbuseWords(abuseWords!![(Math.random() * abuseWords!!.size()).roundToInt()
                        .toInt()].asString)
                }
            }
            target = null
        }
    }

    private fun sendAbuseWords(msg: String) {
        var msg = msg
        if (waterMarkValue.get()) {
            msg = "[FDPClient] $msg"
        }
        mc.thePlayer.sendChatMessage(msg)
    }
}