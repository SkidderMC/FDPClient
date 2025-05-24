/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.altmanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.account.MojangAccount
import net.ccbluex.liquidbounce.FDPClient.CLIENT_CLOUD
import net.ccbluex.liquidbounce.event.EventManager
import net.ccbluex.liquidbounce.event.SessionUpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.client.HUDModule.guiColor
import net.ccbluex.liquidbounce.file.FileManager.accountsConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.handler.lang.translationButton
import net.ccbluex.liquidbounce.handler.lang.translationMenu
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiLoginIntoAccount
import net.ccbluex.liquidbounce.ui.client.altmanager.menus.GuiSessionLogin
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.io.*
import net.ccbluex.liquidbounce.utils.kotlin.swap
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.randomAccount
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBloom
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiSlot
import net.minecraft.client.gui.GuiTextField
import net.minecraft.util.Session
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*

class GuiAltManager(private val prevGui: GuiScreen) : AbstractScreen() {

    var status = "§7Idle..."

    private lateinit var loginButton: GuiButton
    private lateinit var randomAltButton: GuiButton
    private lateinit var randomNameButton: GuiButton
    private lateinit var addButton: GuiButton
    private lateinit var removeButton: GuiButton
    private lateinit var copyButton: GuiButton
    private lateinit var altsList: GuiList
    private lateinit var searchField: GuiTextField

    override fun initGui() {
        val textFieldWidth = (width / 8).coerceAtLeast(70)
        searchField = GuiTextField(2, mc.fontRendererObj, width - textFieldWidth - 10, 10, textFieldWidth, 20)
        searchField.maxStringLength = Int.MAX_VALUE

        altsList = GuiList(this).apply {
            registerScrollButtons(7, 8)

            val mightBeTheCurrentAccount = accountsConfig.accounts.indexOfFirst { it.name == mc.session.username }
            elementClicked(mightBeTheCurrentAccount, false, 0, 0)

            scrollBy(mightBeTheCurrentAccount * this.getSlotHeight())
        }

        // Setup buttons

        val startPositionY = 22
        addButton = +GuiButton(1, width - 80, startPositionY + 24, 70, 20, translationButton("add"))
        removeButton = +GuiButton(2, width - 80, startPositionY + 24 * 2, 70, 20, translationButton("remove"))
        +GuiButton(13, width - 80, startPositionY + 24 * 3, 70, 20, translationButton("moveUp"))
        +GuiButton(14, width - 80, startPositionY + 24 * 4, 70, 20, translationButton("moveDown"))
        +GuiButton(7, width - 80, startPositionY + 24 * 5, 70, 20, translationButton("import"))
        +GuiButton(12, width - 80, startPositionY + 24 * 6, 70, 20, translationButton("export"))
        copyButton = +GuiButton(8, width - 80, startPositionY + 24 * 7, 70, 20, translationButton("altManager.copy"))

        +GuiButton(0, width - 80, height - 65, 70, 20, translationButton("back"))
        loginButton = +GuiButton(3, 5, startPositionY + 24, 90, 20, translationButton("altManager.login"))
        randomAltButton = +GuiButton(4, 5, startPositionY + 24 * 2, 90, 20, translationButton("altManager.randomAlt"))
        randomNameButton = +GuiButton(5, 5, startPositionY + 24 * 3, 90, 20, translationButton("altManager.randomName"))
        +GuiButton(6, 5, startPositionY + 24 * 4, 90, 20, translationButton("altManager.directLogin"))
        +GuiButton(10, 5, startPositionY + 24 * 5, 90, 20, translationButton("altManager.sessionLogin"))
            +GuiButton(11, 5, startPositionY + 24 * 7, 90, 20, "Reload")
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile {
            drawBackground(0)
            altsList.drawScreen(mouseX, mouseY, partialTicks)
            Fonts.fontSemibold40.drawCenteredStringWithShadow(translationMenu("altManager"), width / 2f, 6f, 0xffffff)
            Fonts.fontSemibold35.drawCenteredStringWithShadow(
                if (searchField.text.isEmpty()) "${accountsConfig.accounts.size} Alts" else altsList.accounts.size.toString() + " Search Results",
                width / 2f,
                18f,
                0xffffff
            )
            Fonts.fontSemibold35.drawCenteredString(status, width / 2f, 32f, 0xffffff)
            Fonts.fontSemibold35.drawStringWithShadow(
                "§7User: §a${mc.getSession().username}", 6f, 6f, 0xffffff
            )

            searchField.drawTextBox()
            if (searchField.text.isEmpty() && !searchField.isFocused) Fonts.fontSemibold40.drawStringWithShadow(
                "§7Search...", searchField.xPosition + 4f, 17f, 0xffffff
            )
        }

        drawBloom(mouseX - 5, mouseY - 5, 10, 10, 16, Color(guiColor))

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    public override fun actionPerformed(button: GuiButton) {
        // Not enabled buttons should be ignored
        if (!button.enabled) return

        when (button.id) {
            0 -> mc.displayGuiScreen(prevGui)
            1 -> mc.displayGuiScreen(GuiLoginIntoAccount(this))
            2 -> { // Delete button
                status = if (altsList.selectedSlot != -1 && altsList.selectedSlot < altsList.size) {
                    accountsConfig.removeAccount(altsList.accounts[altsList.selectedSlot])
                    saveConfig(accountsConfig)
                    "§aThe account has been removed."
                } else {
                    "§cSelect an account."
                }
            }

            3 -> { // Login button
                status = altsList.selectedAccount?.let {
                    loginButton.enabled = false
                    randomAltButton.enabled = false
                    randomNameButton.enabled = false

                    login(it, {
                        status = "§aLogged into §f§l${mc.session.username}§a."
                    }, { exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    }, {
                        loginButton.enabled = true
                        randomAltButton.enabled = true
                        randomNameButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cSelect an account."
            }

            4 -> { // Random alt button
                status = altsList.accounts.randomOrNull()?.let {
                    loginButton.enabled = false
                    randomAltButton.enabled = false
                    randomNameButton.enabled = false

                    login(it, {
                        status = "§aLogged into §f§l${mc.session.username}§a."
                    }, { exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    }, {
                        loginButton.enabled = true
                        randomAltButton.enabled = true
                        randomNameButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cYou do not have any accounts."
            }

            5 -> { // Random name button
                status = "§aLogged into §f§l${randomAccount().name}§a."
            }

            6 -> { // Direct login button
                mc.displayGuiScreen(GuiLoginIntoAccount(this, directLogin = true))
            }

            7 -> { // Import button
                val file = MiscUtils.openFileChooser(FileFilters.TEXT) ?: return

                file.forEachLine {
                    val accountData = it.split(':', limit = 2)
                    if (accountData.size > 1) {
                        // Most likely a mojang account
                        accountsConfig.addMojangAccount(accountData[0], accountData[1])
                    } else if (accountData[0].length < 16) {
                        // Might be cracked account
                        accountsConfig.addCrackedAccount(accountData[0])
                    } // skip account
                }

                saveConfig(accountsConfig)
                status = "§aThe accounts were imported successfully."
            }

            12 -> { // Export button
                if (accountsConfig.accounts.isEmpty()) {
                    status = "§cYou do not have any accounts to export."
                    return
                }

                val file = MiscUtils.saveFileChooser()
                if (file == null || file.isDirectory) {
                    return
                }

                try {
                    if (!file.exists()) {
                        file.createNewFile()
                    }

                    val accounts = accountsConfig.accounts.joinToString(separator = "\n") { account ->
                        when (account) {
                            is MojangAccount -> "${account.email}:${account.password}" // EMAIL:PASSWORD
                            is MicrosoftAccount -> "${account.name}:${account.session.token}" // NAME:SESSION
                            else -> account.name
                        }
                    }
                    file.writeText(accounts)

                    status = "§aExported successfully!"
                } catch (e: Exception) {
                    status = "§cUnable to export due to error: ${e.message}"
                }
            }

            8 -> {
                val currentAccount = altsList.selectedAccount

                if (currentAccount == null) {
                    status = "§cSelect an account."
                    return
                }

                try {
                    // Format data for other tools
                    val formattedData = when (currentAccount) {
                        is MojangAccount -> "${currentAccount.email}:${currentAccount.password}" // EMAIL:PASSWORD
                        is MicrosoftAccount -> "${currentAccount.name}:${currentAccount.session.token}" // NAME:SESSION
                        else -> currentAccount.name
                    }

                    // Copy to clipboard
                    MiscUtils.copy(formattedData)
                    status = "§aCopied account into your clipboard."
                } catch (any: Exception) {
                    any.printStackTrace()
                }
            }

            10 -> { // Session Login Button
                mc.displayGuiScreen(GuiSessionLogin(this))
            }

            13 -> { // Move Up Button
                val currentAccount = altsList.selectedAccount
                if (currentAccount == null) {
                    status = "§cSelect an account."
                    return
                }
                val currentIndex = altsList.accounts.indexOf(currentAccount)
                if (currentIndex == 0) {
                    return
                }
                val prevElement = altsList.accounts[currentIndex - 1]
                val prevIndex = accountsConfig.accounts.indexOf(prevElement)
                val currentOriginalIndex = accountsConfig.accounts.indexOf(currentAccount)
                // Move currentAccount
                accountsConfig.accounts.swap(prevIndex, currentOriginalIndex)
                accountsConfig.saveConfig()
                altsList.selectedSlot--
            }
            14 -> { // Move Down Button
                val currentAccount = altsList.selectedAccount
                if (currentAccount == null) {
                    status = "§cSelect an account."
                    return
                }
                val currentIndex = altsList.accounts.indexOf(currentAccount)
                if (currentIndex == altsList.accounts.lastIndex) {
                    return
                }
                val nextElement = altsList.accounts[currentIndex + 1]
                val nextIndex = accountsConfig.accounts.indexOf(nextElement)
                val currentOriginalIndex = accountsConfig.accounts.indexOf(currentAccount)
                // Move currentAccount
                accountsConfig.accounts.swap(nextIndex, currentOriginalIndex)
                accountsConfig.saveConfig()
                altsList.selectedSlot++
            }
        }
    }

    public override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (searchField.isFocused) {
            searchField.textboxKeyTyped(typedChar, keyCode)
        }

        when (keyCode) {
            // Go back
            Keyboard.KEY_ESCAPE -> mc.displayGuiScreen(prevGui)

            // Go one up in account list
            Keyboard.KEY_UP -> altsList.selectedSlot -= 1

            // Go one down in account list
            Keyboard.KEY_DOWN -> altsList.selectedSlot += 1

            // Go up or down in account list
            Keyboard.KEY_TAB -> altsList.selectedSlot += if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) -1 else 1

            // Login into account
            Keyboard.KEY_RETURN -> altsList.elementClicked(altsList.selectedSlot, true, 0, 0)

            // Scroll account list
            Keyboard.KEY_NEXT -> altsList.scrollBy(height - 100)

            // Scroll account list
            Keyboard.KEY_PRIOR -> altsList.scrollBy(-height + 100)

            // Add account
            Keyboard.KEY_ADD -> actionPerformed(addButton)

            // Remove account
            Keyboard.KEY_DELETE, Keyboard.KEY_MINUS -> actionPerformed(removeButton)

            // Copy when CTRL+C gets pressed
            Keyboard.KEY_C -> {
                if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) actionPerformed(copyButton)
                else super.keyTyped(typedChar, keyCode)
            }

            else -> super.keyTyped(typedChar, keyCode)
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        altsList.handleMouseInput()
    }

    public override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        searchField.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun updateScreen() = searchField.updateCursorCounter()

    private inner class GuiList(prevGui: GuiScreen) :
        GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {

        val accounts: List<MinecraftAccount>
            get() {
                var search = searchField.text
                if (search == null || search.isEmpty()) {
                    return accountsConfig.accounts
                }
                search = search.lowercase(Locale.getDefault())

                return accountsConfig.accounts.filter {
                    it.name.contains(
                        search, ignoreCase = true
                    ) || (it is MojangAccount && it.email.contains(search, ignoreCase = true))
                }
            }

        var selectedSlot = 0
            set(value) {
                if (accounts.isEmpty()) return
                field = (value + accounts.size) % accounts.size
            }
            get() {
                return if (field >= accounts.size) -1
                else field
            }

        val selectedAccount
            get() = accounts.getOrNull(selectedSlot)

        override fun isSelected(id: Int) = selectedSlot == id

        public override fun getSize() = accounts.size

        public override fun elementClicked(clickedElement: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = clickedElement

            if (doubleClick) {
                status = altsList.selectedAccount?.let {
                    loginButton.enabled = false
                    randomAltButton.enabled = false
                    randomNameButton.enabled = false

                    login(it, {
                        status = "§aLogged into §f§l${mc.session.username}§a."
                    }, { exception ->
                        status = "§cLogin failed due to '${exception.message}'."
                    }, {
                        loginButton.enabled = true
                        randomAltButton.enabled = true
                        randomNameButton.enabled = true
                    })

                    "§aLogging in..."
                } ?: "§cSelect an account."
            }
        }

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val minecraftAccount = accounts[id]
            val accountName = if (minecraftAccount is MojangAccount && minecraftAccount.name.isEmpty()) {
                minecraftAccount.email
            } else {
                minecraftAccount.name
            }

            Fonts.minecraftFont.drawStringWithShadow(accountName, width / 2f - 40, y + 2f, Color.WHITE.rgb)
            Fonts.minecraftFont.drawStringWithShadow(
                if (minecraftAccount is CrackedAccount) "Cracked" else if (minecraftAccount is MicrosoftAccount) "Microsoft" else if (minecraftAccount is MojangAccount) "Mojang" else "Something else",
                width / 2f,
                y + 15f,
                if (minecraftAccount is CrackedAccount) Color.GRAY.rgb else Color(118, 255, 95).rgb
            )
        }

        override fun drawBackground() {}
    }

    companion object {

        private val activeGenerators = mutableMapOf<String, Boolean>()

        fun loadActiveGenerators() {
            try {
                // Read versions json from cloud
                activeGenerators += HttpClient.get("$CLIENT_CLOUD/generators.json").jsonBody<Map<String, Boolean>>()!!

            } catch (throwable: Throwable) {
                // Print throwable to console
                LOGGER.error("Failed to load enabled generators.", throwable)
            }
        }

        fun AbstractScreen.login(
            minecraftAccount: MinecraftAccount,
            success: () -> Unit,
            error: (Exception) -> Unit,
            done: () -> Unit
        ) {
            screenScope.launch(Dispatchers.IO) {
                try {
                    minecraftAccount.update()
                    mc.session = Session(
                        minecraftAccount.session.username,
                        minecraftAccount.session.uuid,
                        minecraftAccount.session.token,
                        "microsoft"
                    )
                    EventManager.call(SessionUpdateEvent)

                    success()
                } catch (exception: Exception) {
                    error(exception)
                }
                done()
            }
        }
    }
}