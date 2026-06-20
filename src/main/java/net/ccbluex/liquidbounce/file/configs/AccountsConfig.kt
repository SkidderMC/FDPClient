/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.configs

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.account.MojangAccount
import me.liuli.elixir.manage.AccountSerializer.fromJson
import me.liuli.elixir.manage.AccountSerializer.toJson
import net.ccbluex.liquidbounce.file.FileConfig
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import net.ccbluex.liquidbounce.utils.io.readJson
import net.ccbluex.liquidbounce.utils.io.writeTextAtomic
import java.io.*

class AccountsConfig(file: File) : FileConfig(file) {

    val accounts = mutableListOf<MinecraftAccount>()

    /**
     * Load config from file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun loadConfig() {
        clearAccounts()
        val json = file.readJson() as? JsonArray ?: return

        for (accountElement in json) {
            val accountObject = accountElement.asJsonObject
            try {
                // Import Elixir account format
                accounts += fromJson(accountElement.asJsonObject)
            } catch (e: JsonSyntaxException) {
                importLegacyAccount(accountObject)?.let(accounts::add)
            } catch (e: IllegalStateException) {
                importLegacyAccount(accountObject)?.let(accounts::add)
            }
        }
    }

    /**
     * Save config to file
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    override fun saveConfig() {
        val jsonArray = JsonArray()

        for (minecraftAccount in accounts)
            jsonArray.add(toJson(minecraftAccount))

        file.writeTextAtomic(PRETTY_GSON.toJson(jsonArray))
        restrictAccountFilePermissions()
    }

    private fun importLegacyAccount(accountObject: JsonObject): MinecraftAccount? {
        val name = accountObject.getNullableString("name") ?: return null
        val password = accountObject.getNullableString("password")
        val inGameName = accountObject.getNullableString("inGameName")

        return if (password.isNullOrEmpty()) {
            CrackedAccount().apply {
                this.name = inGameName ?: name
            }
        } else {
            MojangAccount().apply {
                email = name
                this.name = inGameName ?: name
                this.password = password
            }
        }
    }

    private fun JsonObject.getNullableString(memberName: String): String? {
        val element = get(memberName) ?: return null
        return if (element.isJsonNull) null else element.asString
    }

    private fun restrictAccountFilePermissions() {
        runCatching {
            file.setReadable(false, false)
            file.setWritable(false, false)
            file.setExecutable(false, false)
            file.setReadable(true, true)
            file.setWritable(true, true)
        }
    }

    /**
     * Add cracked account to config
     *
     * @param name of account
     */
    fun addCrackedAccount(name: String) {
        val crackedAccount = CrackedAccount()
        crackedAccount.name = name

        if (!accountExists(crackedAccount)) accounts += crackedAccount
    }

    /**
     * Add account to config
     *
     * @param name     of account
     * @param password of password
     */
    fun addMojangAccount(name: String, password: String) {
        val mojangAccount = MojangAccount()
        mojangAccount.name = name
        mojangAccount.password = password

        if (!accountExists(mojangAccount)) accounts += mojangAccount
    }

    /**
     * Add account to config
     */
    fun addAccount(account: MinecraftAccount) = accounts.add(account)

    /**
     * Remove account from config
     *
     * @param selectedSlot of the account
     */
    fun removeAccount(selectedSlot: Int) = accounts.removeAt(selectedSlot)

    /**
     * Removed an account from the config
     *
     * @param account the account
     */
    fun removeAccount(account: MinecraftAccount) = accounts.remove(account)

    /**
     * Check if the account is already added
     */
    fun accountExists(newAccount: MinecraftAccount) =
        accounts.any { it::class == newAccount::class && it.name == newAccount.name }

    /**
     * Clear all minecraft accounts from alt array
     */
    fun clearAccounts() = accounts.clear()
}
