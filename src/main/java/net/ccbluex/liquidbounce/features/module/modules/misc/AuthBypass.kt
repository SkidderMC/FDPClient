package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.IntegerValue
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2FPacketSetSlot
import java.util.*

@ModuleInfo(name = "AuthBypass", description = "Bypass auth when join server.", category = ModuleCategory.MISC)
class AuthBypass : Module(){
    //redesky add a authbypass check :(
    private val delayValue= IntegerValue("Delay",1500,100,5000)

    private var skull:String?=null
    private var thePacket:Packet<INetHandlerPlayServer>?=null
    private val timer=MSTimer()
    private val jsonParser=JsonParser()

    @EventTarget
    fun onUpdate(event: UpdateEvent){
        if(thePacket!=null&&timer.hasTimePassed(delayValue.get().toLong())){
            mc.netHandler.addToSendQueue(thePacket)
            LiquidBounce.hud.addNotification(Notification(name,"Authenticate bypassed.", NotifyType.INFO))
            thePacket=null
        }
    }

    // now bypass REDESKY,MUSH
    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet=event.packet
        if(packet is S2FPacketSetSlot){
            if(packet.func_149175_c()==0){
                return
            }
            val item=packet.func_149174_e()
            if(item!=null&&item.unlocalizedName.contains("item.skull.char",ignoreCase = true)){
                val nbt=item.tagCompound ?: return
                // val uuid=nbt.get<CompoundTag>("SkullOwner").get<CompoundTag>("Properties").get<ListTag>("textures").get<CompoundTag>(0).get<StringTag>("Value").value
                val data=process(nbt.getCompoundTag("SkullOwner").getCompoundTag("Properties")
                    .getTagList("textures",NBTTagCompound.NBT_TYPES.indexOf("COMPOUND"))
                    .getCompoundTagAt(0).getString("Value"))
                if(skull==null){
                    skull=data
                }else if(skull!=data) {
                    skull = null
                    timer.reset()
                    thePacket=C0EPacketClickWindow(packet.func_149175_c(),packet.func_149173_d(),0,0,item,1919)
                }
            }
        }
        //silent auth xd
        if(packet is S2DPacketOpenWindow){
            if(packet.slotCount==27 && packet.guiId.contains("container",ignoreCase = true)
                && packet.windowTitle.unformattedText.contains("Clique no bloco",ignoreCase = true)){
                event.cancelEvent()
            }
        }
    }

    private fun process(data: String):String{
        val jsonObject=jsonParser.parse(String(Base64.getDecoder().decode(data))).asJsonObject
        return jsonObject
            .getAsJsonObject("textures")
            .getAsJsonObject("SKIN")
            .get("url").asString
    }
}