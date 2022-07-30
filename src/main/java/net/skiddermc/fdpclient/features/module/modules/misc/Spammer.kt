/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.features.module.modules.misc

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.event.EventTarget
import net.skiddermc.fdpclient.event.UpdateEvent
import net.skiddermc.fdpclient.features.module.Module
import net.skiddermc.fdpclient.features.module.ModuleCategory
import net.skiddermc.fdpclient.features.module.ModuleInfo
import net.skiddermc.fdpclient.utils.misc.RandomUtils
import net.skiddermc.fdpclient.utils.timer.MSTimer
import net.skiddermc.fdpclient.utils.timer.TimeUtils
import net.skiddermc.fdpclient.value.IntegerValue
import net.skiddermc.fdpclient.value.ListValue
import net.skiddermc.fdpclient.value.TextValue
import net.minecraft.client.gui.GuiChat

@ModuleInfo(name = "Spammer", category = ModuleCategory.MISC)
class Spammer : Module() {
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 1000, 0, 5000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelayValueObject = minDelayValue.get()
            if (minDelayValueObject > newValue) set(minDelayValueObject)
            delay = TimeUtils.randomDelay(minDelayValue.get(), this.get())
        }
    }
    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 500, 0, 5000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelayValueObject = maxDelayValue.get()
            if (maxDelayValueObject < newValue) set(maxDelayValueObject)
            delay = TimeUtils.randomDelay(this.get(), maxDelayValue.get())
        }
    }

    private val modeValue = ListValue("Mode", arrayOf("Single", "Insult", "OrderInsult"), "Single")
    private val endingChars = IntegerValue("EndingRandomChars",5,0,30)
    private val messageValue = TextValue("Message", "Buy %r Minecraft %r Legit %r and %r stop %r using %r cracked %r servers %r%r")
        .displayable { !modeValue.contains("insult") }
    private val insultMessageValue = TextValue("InsultMessage", "[%s] %w [%s]")
        .displayable { modeValue.contains("insult") }

    private val msTimer = MSTimer()
    private var delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    private var lastIndex = -1

    override fun onEnable() {
        lastIndex = -1
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen != null && mc.currentScreen is GuiChat) {
            return
        }

        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(when (modeValue.get().lowercase()) {
                "insult" -> {
                    replaceAbuse(KillInsults.getRandomOne())
                }
                "orderinsult" -> {
                    lastIndex++
                    if (lastIndex >= (KillInsults.insultWords.size - 1)) {
                        lastIndex = 0
                    }
                    replaceAbuse(KillInsults.insultWords[lastIndex])
                }
                else -> replace(messageValue.get())
            })
            msTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    private fun replaceAbuse(str: String): String {
        return replace(insultMessageValue.get().replace("%w", str))
    }

    private fun replace(str: String): String {
        return str.replace("%r", RandomUtils.nextInt(0, 99).toString())
                    .replace("%s", RandomUtils.randomString(3))
                    .replace("%c", RandomUtils.randomString(1))
                    .replace("%name%", if (FDPClient.combatManager.target != null) { FDPClient.combatManager.target!!.name } else { "You" }) + (RandomUtils.randomString(endingChars.get().toInt()).toString())
    }
}
