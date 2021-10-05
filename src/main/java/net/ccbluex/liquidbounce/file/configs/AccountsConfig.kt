/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.Gson
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.login.MinecraftAccount
import java.io.File

class AccountsConfig(file: File) : FileConfig(file) {
    val altManagerMinecraftAccounts: MutableList<MinecraftAccount> = ArrayList()
    override fun loadConfig(config: String) {
        val accountList = Gson().fromJson<List<*>>(config, MutableList::class.java) as MutableList<String>?  ?: return
        altManagerMinecraftAccounts.clear()
        for (account in accountList) {
            val information = account.split(":").toTypedArray()
            if (information.size >= 3)
                altManagerMinecraftAccounts.add(MinecraftAccount(information[0], information[1], information[2]))
            else if (information.size == 2)
                altManagerMinecraftAccounts.add(MinecraftAccount(information[0], information[1]))
            else
                altManagerMinecraftAccounts.add(MinecraftAccount(information[0]))
        }
    }

    override fun saveConfig(): String {
        val accountList: MutableList<String> = ArrayList()

        for (minecraftAccount in altManagerMinecraftAccounts) {
            accountList.add(minecraftAccount.name + ":"
                    + (if (minecraftAccount.password == null) { "" } else { minecraftAccount.password }) + ":"
                    + if (minecraftAccount.accountName == null) { "" } else { minecraftAccount.accountName })
        }

        return FileManager.PRETTY_GSON.toJson(accountList)
    }
}