/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.ccbluex.liquidbounce.features.value.BoolValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.TextValue
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S1DPacketEntityEffect

@ModuleInfo(name = "AntiStaff", category = ModuleCategory.MISC)
class AntiStaff : Module() {

    private val serverValue = ListValue("Server", arrayOf("BlocksMC", "Jartex", "Pika", "Minebox", "Minemora", "Zonecraft", "Hycraft", "Librecraft","Custom"),"BlocksMC")
    private val notifyValue = BoolValue("Notification",true)
    private val chatValue = BoolValue("SendChatMessage",false)
    private val messageValue = TextValue("Message", "%staff% was detected as a staff member!").displayable { chatValue.get() }
    private val customURLValue = TextValue("CustomURL", "https://raw.githubusercontent.com/fdpweb/fdpweb.github.io/main/test").displayable { serverValue.equals("Custom") }

    private val leaveValue = BoolValue("Leave",true)
    private val leaveMessageValue = TextValue("LeaveCommand","/hub").displayable { leaveValue.get() }
    
    private var bmcStaff : String = " 1Mhmmd mohmad_q8 1Brhom Aliiyah Rylq Jinaaan xImTaiG_ comsterr xIBerryPlayz M4rwaan iiRaivy oNeLaB 1Sweet Ev2n TheMercyWasGone xMz7 1Dayk zA aL5zA3y EyesO_Diamond FernandoEscobar Bl2ck Sadlly Luvaa bestleso 1Ghost_ 1HeyImHasson_ 1BeSo Thenvra MK_F16 M7mmd SadlyNight e9_ qM7_ 420kinaka Refolt yBarbie Firas iS3od_ 3Mmr BoMshary KaaReeeM 2SoulsOnFire MilitaryDeath 1RealFadi loovq SHEFFOO Mmohameed Hestix Yqhya 1DeVilz AFG_progamer92 Sanfoor_J Rq3core A_hmd ILuvMellon 1Ivy_ 1Sweleh_ 1DrRep_ Watchdog BagmaTwT Hmosh eDuel 1Sqnta Im_aNoS F2v Evandeer Maybe_meee OnlyK1nq Bo3loosh B6re80 Barfi Sxlverr _utka Jarxay SuchPro10 MilitaryAhMeD iiReze Mazyadov 1Flick 420Lalilala _Reevox_ 1Logie shuzi_ NotThem Hxmza13 Hmoxd JustLove_It Dio_q ZerNow_YT wstre ixstorm_ Webmasters 1Mawja_ O9R_ DecentPing ii_3zX "
    private var jartexStaff : String = "voodootje0 Max Rodagave Wrath JustThiemo Andeh Nirahz stupxd Botervrij Viclyn_  DrogonMC ovq Flexier NotLoLo1818 SabitTSDM07 ItzCqldFxld Laux  bene_e  iFlyYT          HeadsBreker       AX79       Technostein          Djim       Serpentsalamce       Almostlikeaboss       JustAtaman       ZoneRGH       naranbaatr       louiekeys       Difficulted       FuzniX       xHasey       sammyxt       CR7811149       Xerrainrin       toastt_x          UpperGround       Swervinng       SquareWings928       Yanique1       pakitonia     Stxrs".toString()
    private var pikaStaff : String = "Max voodootje0 MrFrenco JustThiemo Wrath Andeh Nirahz stupxd Botervrij Subvalent Apo2xd Arrly Minecraft_leg CaptainGeoGR Thijme01 ChickenDinnr Crni_ MrGownz Outscale MrEpiko Crveni_Marlboro zMqrcc _Stella_xD Stormidity TryToHitMe Alparo_ CandyOP Astrospeh TinCanL TheTrueNova FIKOZ DarkVenom7 caila5 Lpkfvip i9BAR "
    private var mineboxStaff : String = "xSp3ctro_  SaF3rC  Sagui  TheSuperXD_YT  xAnibal  xTheKillex25x  HankWalter  JavierFenix  inothayami  ChaosSoleil  ElChamo300  Robert TO1010  itachi_uchiha_s  roku__  rynne_ sushi dashi Vicky_21"
    private var zonecraftStaff : String = "002Aren asiessoydecono donerreMC elMagnificPvP ErCris fernxndx gourd Gudaa ImAle ImMarvolo ismq nicoxrm pacorro rapheos MrBara MrMonkey57 uploadedhh trifeyy 002Aren Agu5 augusmaster BetTD d411 dunshbey85 ElMaGnific Pv ErCris Eugene FelmaxMC Gudaa ¡Enux ImMarvolo sleepless ismq ItzOmar16 joescam LuisPoMC Nicoxrm pacorro "
    private var hycraftStaff : String = "Alexander245 arqui Blandih Chony_15 jac0mc Ragen06 TheBryaan TMT_131 Yapecito MartynaGamer830 archeriam"
    private var librecraftStaff : String = "Kudos  H0DKIER  Iker_XD9  acreate  iJeanSC  acreate  Janet  Rosse_RM  aldoum23neko_  DERGO  MJKINGPAND"
    private var minemoraStaff : String = "Ruficraft MariSG iSebaas MaxyMC LuhGleh Esmorall SrLucchel_ ninjagod98 DarkFumado iDrecs CuencaDeDiamante PainSex"
    
    
    private var detected = false
    private var staffs = ""
    
    
    @EventTarget
    fun onWorld(event: WorldEvent) {
        when (serverValue.get().lowercase()) {
            "blocksmc" -> staffs = bmcStaff

            "jartex" -> staffs = jartexStaff

            "pika" -> staffs = pikaStaff

            "minebox" -> staffs = mineboxStaff

            "minemora" -> staffs = minemoraStaff
            
            "zonecraft" -> staffs = zonecraftStaff
            
            "hycraft" -> staffs = hycraftStaff
            
            "librecraft" -> staffs = librecraftStaff

            "custom" -> {
                try {
                    staffs = HttpUtils.get(customURLValue.get())

                    LiquidBounce.hud.addNotification(Notification("AntiStaff", "SuccessFully Loaded URL", NotifyType.SUCCESS, 1000))
                } catch (err: Throwable) {
                    LiquidBounce.hud.addNotification(Notification("AntiStaff", "Error when loading URL", NotifyType.ERROR, 1000))
                    println(err)
                }
            }
        }
            
        detected = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        if (mc.theWorld == null || mc.thePlayer == null) return

        val packet = event.packet // smart convert
        if (packet is S1DPacketEntityEffect) {
            val entity = mc.theWorld.getEntityByID(packet.entityId)
            if (entity != null && (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText))) {
                if (!detected) {
                    if (notifyValue.get()){
                        LiquidBounce.hud.addNotification(Notification(name, "Detected staff members with invis. You should quit ASAP.", NotifyType.WARNING, 8000))
                    }
                    
                    if (chatValue.get()) {
                        mc.thePlayer.sendChatMessage((messageValue.get()).replace("%staff%", entity.name))
                    }
                    if (leaveValue.get()) {
                        mc.thePlayer.sendChatMessage(leaveMessageValue.get())
                    }
                    
                    detected = true
                }
            }
        }
        if (packet is S14PacketEntity) {
            val entity = packet.getEntity(mc.theWorld)

            if (entity != null && (staffs.contains(entity.name) || staffs.contains(entity.displayName.unformattedText))) {
                if (!detected) {
                    if (notifyValue.get()){
                    LiquidBounce.hud.addNotification(Notification(name, "Detected staff members. You should quit ASAP.", NotifyType.WARNING,8000))
                    }
                    
                    if (chatValue.get()) {
                        ClientUtils.displayChatMessage((messageValue.get()).replace("%staff%", entity.name))
                    }
                    
                    if (leaveValue.get()) {
                        mc.thePlayer.sendChatMessage(leaveMessageValue.get())
                    }
                    
                    detected = true
                }
            }
        }
    }
}
