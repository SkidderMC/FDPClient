/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/Project-EZ4H/FDPClient/
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
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue

@ModuleInfo(name = "Spammer", description = "Spams the chat with a given message.", category = ModuleCategory.MISC)
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

    private val modeValue = ListValue("Mode", arrayOf("Abuse","Single"),"Single")
    private val messageValue = TextValue("Message", "Buy %r Minecraft %r Legit %r and %r stop %r using %r cracked %r servers %r%r")
    private val randomValue = BoolValue("RandomAbuse",true)

    private val msTimer = MSTimer()
    private var delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    private var lastIndex=-1

    override fun onEnable() {
        lastIndex=-1
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(when(modeValue.get().toLowerCase()){
                "abuse" -> {
                    replace(if(randomValue.get()){
                        AutoAbuse.getRandomOne()
                    }else{
                        lastIndex++
                        if(lastIndex>(AutoAbuse.abuseWords!!.size()-1)){
                            lastIndex=0
                        }
                        AutoAbuse.abuseWords!![lastIndex].asString
                    })
                }
                else -> replace(messageValue.get())
            })
            msTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    private fun replace(str: String): String {
        return str.replace("%r", RandomUtils.nextInt(0, 99).toString())
                    .replace("%c", RandomUtils.randomString(1))
                    .replace("%name%",if(LiquidBounce.combatManager.target!=null){ LiquidBounce.combatManager.target!!.name }else{ "You" })
    }
}