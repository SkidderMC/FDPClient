/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager

import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.handler.lang.translationButton
import net.ccbluex.liquidbounce.handler.lang.translationMenu
import net.ccbluex.liquidbounce.handler.lang.translationText
import net.ccbluex.liquidbounce.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.ccbluex.liquidbounce.ui.font.CustomFontInfo
import net.ccbluex.liquidbounce.ui.font.FontInfo
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.ui.font.drawCenteredString
import net.ccbluex.liquidbounce.utils.io.FileFilters
import net.ccbluex.liquidbounce.utils.io.MiscUtils
import net.ccbluex.liquidbounce.utils.ui.AbstractScreen
import net.minecraft.client.gui.*
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File

private const val BACK_BTN_ID = 0
private const val ADD_BTN_ID = 10
private const val REMOVE_BTN_ID = 11
private const val EDIT_BTN_ID = 12

/**
 * @author MukjepScarlet
 */
class GuiFontManager(private val prevGui: GuiScreen) : AbstractScreen() {

    private enum class Status(val text: String) {
        IDLE("§7Idle..."),
        FAILED_TO_LOAD("§cFailed to load font file!"),
        FAILED_TO_REMOVE("§cFailed to remove font info!")
    }

    private var status = Status.IDLE

    private lateinit var fontListView: GuiList
    private lateinit var addButton: GuiButton
    private lateinit var removeButton: GuiButton
    private lateinit var textField: GuiTextField
    private lateinit var nameField: GuiTextField
    private lateinit var sizeField: GuiTextField

    override fun initGui() {
        buttonList.clear()
        textFields.clear()

        val startPositionY = 22
        val leftStartX = 5
        val rightStartX = width - 80

        val textFieldWidth = (width / 8).coerceAtLeast(70)
        textField = textField(2, mc.fontRendererObj, width - textFieldWidth - 10, 10, textFieldWidth, 20) {
            maxStringLength = Int.MAX_VALUE
        }
        nameField = textField(3, mc.fontRendererObj, leftStartX, startPositionY + 24 * 1, textFieldWidth, 20) {
            maxStringLength = Int.MAX_VALUE
        }
        sizeField = textField(4, mc.fontRendererObj, leftStartX, startPositionY + 24 * 2, textFieldWidth, 20) {
            setValidator {
                it.isNullOrBlank() || it.toIntOrNull()?.takeIf { i -> i in 1..500 } != null
            }
            maxStringLength = 3
        }
        +GuiButton(EDIT_BTN_ID, leftStartX, startPositionY + 24 * 3, 70, 20, translationButton("fontManager.edit"))

        addButton = +GuiButton(ADD_BTN_ID, rightStartX, startPositionY + 24 * 1, 70, 20, translationButton("add"))
        removeButton = +GuiButton(REMOVE_BTN_ID, rightStartX, startPositionY + 24 * 2, 70, 20, translationButton("remove"))

        +GuiButton(BACK_BTN_ID, rightStartX, height - 65, 70, 20, translationButton("back"))

        fontListView = GuiList(this).apply {
            registerScrollButtons(7, 8)
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        fontListView.handleMouseInput()
    }

    public override fun keyTyped(typedChar: Char, keyCode: Int) {
        this.textFields.forEach {
            if (it.isFocused) {
                it.textboxKeyTyped(typedChar, keyCode)
            }
        }

        when (keyCode) {
            // Go back
            Keyboard.KEY_ESCAPE -> mc.displayGuiScreen(prevGui)

            // Go one up in account list
            Keyboard.KEY_UP -> fontListView.selectedSlot -= 1

            // Go one down in account list
            Keyboard.KEY_DOWN -> fontListView.selectedSlot += 1

            // Go up or down in account list
            Keyboard.KEY_TAB -> fontListView.selectedSlot += if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) -1 else 1

            // Login into account
            Keyboard.KEY_RETURN -> fontListView.elementClicked(fontListView.selectedSlot, true, 0, 0)

            // Scroll account list
            Keyboard.KEY_NEXT -> fontListView.scrollBy(height - 100)

            // Scroll account list
            Keyboard.KEY_PRIOR -> fontListView.scrollBy(-height + 100)

            // Add account
            Keyboard.KEY_ADD -> actionPerformed(addButton)

            // Remove account
            Keyboard.KEY_DELETE, Keyboard.KEY_MINUS -> actionPerformed(removeButton)

            else -> super.keyTyped(typedChar, keyCode)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        assumeNonVolatile {
            drawBackground(0)
            fontListView.drawScreen(mouseX, mouseY, partialTicks)
            Fonts.fontSemibold40.drawCenteredString(translationMenu("fontManager"), width / 2f, 6f, 0xffffff)
            val count = Fonts.customFonts.size
            val text = if (count == 1) {
                translationText("fontManager.customFonts", count)
            } else {
                translationText("fontManager.customFonts.plural", count)
            }
            Fonts.fontSemibold35.drawCenteredString(
                text,
                width / 2f,
                18f,
                0xffffff
            )
            Fonts.fontSemibold35.drawCenteredString(status.text, width / 2f, 32f, 0xffffff)

            this.textFields.forEach { it.drawTextBox() }
            if (nameField.text.isEmpty() && !nameField.isFocused) Fonts.fontSemibold40.drawStringWithShadow(
                translationText("fontManager.name") + "...", nameField.xPosition + 4f, nameField.yPosition + 7f, Color.GRAY.rgb
            )
            if (sizeField.text.isEmpty() && !sizeField.isFocused) Fonts.fontSemibold40.drawStringWithShadow(
                translationText("fontManager.size") + "...", sizeField.xPosition + 4f, sizeField.yPosition + 7f, Color.GRAY.rgb
            )
            if (textField.text.isEmpty() && !textField.isFocused) Fonts.fontSemibold40.drawStringWithShadow(
                translationText("fontManager.preview") + "...", textField.xPosition + 4f, 17f, Color.GRAY.rgb
            ) else {
                val font = fontListView.selectedEntry.value
                font.drawCenteredString(
                    textField.text,
                    x = width * 0.5f,
                    y = height - 40f + font.FONT_HEIGHT * 0.5f,
                    color = Color.WHITE.rgb,
                )
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun CustomFontInfo.save() = Fonts.registerCustomAWTFont(this, save = true) ?: run {
        status = Status.FAILED_TO_LOAD
    }

    private fun editFontInfo(fontInfo: FontInfo) {
        val newName = nameField.text.takeIf { it.isNotBlank() }
        val newSize = sizeField.text.toIntOrNull()?.coerceIn(1, 500)

        if (newName == null && newSize == null) {
            return
        }

        val origin = Fonts.removeCustomFont(fontInfo) ?: run {
            status = Status.FAILED_TO_REMOVE
            return
        }

        var edited = origin
        if (newName != null) {
            edited = edited.copy(name = newName)
        }
        if (newSize != null) {
            edited = edited.copy(fontSize = newSize)
        }

        edited.save()
    }

    public override fun actionPerformed(button: GuiButton) {
        // Not enabled buttons should be ignored
        if (!button.enabled) return

        when (button.id) {
            BACK_BTN_ID -> mc.displayGuiScreen(prevGui)
            ADD_BTN_ID -> {
                val file = MiscUtils.openFileChooser(FileFilters.FONT, acceptAll = false)?.takeIf { it.isFile } ?: run {
                    status = Status.FAILED_TO_LOAD
                    return
                }

                val directory = FileManager.fontsDir

                // Copy font file
                val targetFile = File(directory, file.name)
                if (!targetFile.exists()) {
                    file.copyTo(targetFile, overwrite = true)
                }

                val fontFile = targetFile.relativeTo(directory).path
                val defaultInfo = CustomFontInfo(name = file.name, fontFile = fontFile, fontSize = 20)
                defaultInfo.save()
            }
            REMOVE_BTN_ID -> {
                val fontInfo = fontListView.selectedEntry.key.takeIf { it.isCustom } ?: return
                Fonts.removeCustomFont(fontInfo)
            }
            EDIT_BTN_ID -> {
                val fontInfo = fontListView.selectedEntry.key.takeIf { it.isCustom } ?: return
                editFontInfo(fontInfo)
            }
        }
    }

    private inner class GuiList(prevGui: GuiScreen) :
        GuiSlot(mc, prevGui.width, prevGui.height, 40, prevGui.height - 40, 30) {

        override fun getSize(): Int = Fonts.customFonts.size

        var selectedSlot = -1
            set(value) {
                field = if (size == 0) {
                    -1
                } else {
                    (value + size) % size
                }
            }

        private val defaultEntry = object : Map.Entry<FontInfo, FontRenderer> {
            override val key: FontInfo
                get() = Fonts.minecraftFontInfo

            override val value: FontRenderer
                get() = mc.fontRendererObj
        }

        val selectedEntry: Map.Entry<FontInfo, FontRenderer>
            get() = Fonts.customFonts.entries.elementAtOrElse(selectedSlot) { defaultEntry }

        public override fun elementClicked(clickedElement: Int, doubleClick: Boolean, var3: Int, var4: Int) {
            selectedSlot = clickedElement
        }

        override fun isSelected(p0: Int): Boolean = p0 == selectedSlot

        override fun drawBackground() {}

        override fun drawSlot(id: Int, x: Int, y: Int, var4: Int, var5: Int, var6: Int) {
            val (fontInfo, _) = Fonts.customFonts.entries.elementAt(id)

            Fonts.minecraftFont.drawCenteredString("${fontInfo.name} - ${fontInfo.size}", width / 2f, y + 2f, Color.WHITE.rgb, true)
        }

    }

}