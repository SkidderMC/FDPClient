/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.features.value.IntegerValue
import net.ccbluex.liquidbounce.features.value.ListValue
import net.ccbluex.liquidbounce.features.value.TextValue
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
    private val endingCharsValue = IntegerValue("EndingRandomChars",5,0,30)
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
        if (modeValue.equals("Single") && messageValue.get().startsWith(".")) {
            LiquidBounce.commandManager.executeCommands(messageValue.get()) 
            return
        }

        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(when (modeValue.get().lowercase()) {
                "insult" -> {
                    replaceAbuse(Insult.getRandomOne())
                }
                "orderinsult" -> {
                    lastIndex++
                    if (lastIndex >= (Insult.insultWords.size - 1)) {
                        lastIndex = 0
                    }
                    replaceAbuse(Insult.insultWords[lastIndex])
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
                    .replace("%name%", if (LiquidBounce.combatManager.target != null) { LiquidBounce.combatManager.target!!.name } else { "You" }) + (RandomUtils.randomString(endingCharsValue.get().toInt()).toString())
    }
}
