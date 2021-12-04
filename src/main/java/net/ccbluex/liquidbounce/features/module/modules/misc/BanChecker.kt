/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 * 
 * This code belongs to WYSI-Foundation. Please give credits when using this in your repository.
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.misc.HttpUtils.get
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue

@ModuleInfo(name = "BanChecker", category = ModuleCategory.MISC)
class BanChecker : Module() {
    val alertValue = BoolValue("Alert", true)
    val serverCheckValue = BoolValue("ServerCheck", true)
    val alertTimeValue = IntegerValue("Alert-Time", 10, 1, 50)
    override var tag = "Idle..."
    val isOnHypixel: Boolean
        get() = !mc.isIntegratedServerRunning && mc.currentServerData.serverIP.contains("hypixel.net")

    companion object {
        // no u
        private val API_PUNISHMENT =
            aB("68747470733a2f2f6170692e706c616e636b652e696f2f6879706978656c2f76312f70756e6973686d656e745374617473")
        var WATCHDOG_BAN_LAST_MIN = 0
        var LAST_TOTAL_STAFF = -1
        var STAFF_BAN_LAST_MIN = 0
        @JvmStatic
        fun aB(str: String): String { // :trole:
            var result = String()
            val charArray = str.toCharArray()
            var i = 0
            while (i < charArray.size) {
                val st = "" + charArray[i] + "" + charArray[i + 1]
                val ch = st.toInt(16).toChar()
                result += ch
                i += 2
            }
            return result
        }
    }

    init {
        object : Thread("banCheckHypixel") {
            override fun run() {
                val checkTimer = MSTimer()
                while (true) {
                    if (checkTimer.hasTimePassed(60000L)) {
                        if (LiquidBounce.moduleManager.getModule(BanChecker::class.java)!!.state) {
                            try {
                                val apiContent = get(API_PUNISHMENT)
                                val jsonObject = JsonParser().parse(apiContent).asJsonObject
                                if (jsonObject["success"].asBoolean && jsonObject.has("record")) {
                                    val objectAPI = jsonObject["record"].asJsonObject
                                    WATCHDOG_BAN_LAST_MIN = objectAPI["watchdog_lastMinute"].asInt
                                    var staffBanTotal = objectAPI["staff_total"].asInt
                                    if (staffBanTotal < LAST_TOTAL_STAFF) staffBanTotal = LAST_TOTAL_STAFF
                                    if (LAST_TOTAL_STAFF == -1) LAST_TOTAL_STAFF = staffBanTotal else {
                                        STAFF_BAN_LAST_MIN = staffBanTotal - LAST_TOTAL_STAFF
                                        LAST_TOTAL_STAFF = staffBanTotal
                                    }
                                    tag = "Staff:${STAFF_BAN_LAST_MIN} WatchDog:${WATCHDOG_BAN_LAST_MIN}"
                                    if (LiquidBounce.moduleManager.getModule(BanChecker::class.java)!!.state && alertValue.get() && mc.thePlayer != null && (!serverCheckValue.get() || isOnHypixel)) if (STAFF_BAN_LAST_MIN > 0) LiquidBounce.hud.addNotification(
                                        Notification("BanCheck",
                                            "$STAFF_BAN_LAST_MIN player(s) got banned by staffs in the last minute!",
                                            if (STAFF_BAN_LAST_MIN > 3) NotifyType.ERROR else NotifyType.WARNING,
                                            alertTimeValue.get() * 1000,
                                            500)) else LiquidBounce.hud.addNotification(
                                        Notification("BanCheck",
                                            "Staffs didn't ban any player in the last minute.",
                                            NotifyType.SUCCESS,
                                            alertTimeValue.get() * 1000,
                                            500))

                                    // watchdog ban doesn't matter, open an issue if you want to add it.
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        checkTimer.reset()
                    }
                }
            }
        }.start()
    }
}