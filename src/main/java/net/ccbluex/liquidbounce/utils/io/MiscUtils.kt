/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.awt.Desktop
import java.awt.Font
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JTextArea
import javax.swing.filechooser.FileFilter

object MiscUtils : MinecraftInstance() {

    private fun JTextArea.adjustTextAreaSize() {
        val fontMetrics = getFontMetrics(font)
        val lineSequence = text.lineSequence()
        val lines = lineSequence.count()
        val maxLineWidth = lineSequence.maxOfOrNull { fontMetrics.stringWidth(it) } ?: 0
        val columns = maxLineWidth / fontMetrics.charWidth('m')
        this.rows = lines + 1
        this.columns = columns + 1
    }

    @JvmStatic
    fun showErrorPopup(title: String, message: Any) =
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)

    fun Throwable.showErrorPopup() = showErrorPopup(javaClass.simpleName, localizedMessage)

    @JvmStatic
    fun showURL(url: String) =
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            e.showErrorPopup()
        } catch (e: URISyntaxException) {
            e.showErrorPopup()
        }

    @JvmStatic
    fun Throwable.showErrorPopup(
        titlePrefix: String = "Exception occurred: ",
        extraContent: String = LocalDateTime.now().toString() + '\n'
    ) {
        val title = titlePrefix + javaClass.simpleName
        val content = extraContent + "--- Stacktrace ---\n" + stackTraceToString()
        val textArea = JTextArea(content).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font("Consolas", Font.PLAIN, 12)
            adjustTextAreaSize()
        }
        showErrorPopup(title, textArea)
    }

    @JvmStatic
    fun openFileChooser(fileFiler: FileFilter? = null): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileFiler?.let { fileChooser.fileFilter = it }

        val frame = JFrame()
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false

        val action = fileChooser.showOpenDialog(frame)
        frame.dispose()

        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }

    @JvmStatic
    fun saveFileChooser(fileFiler: FileFilter? = null): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val fileChooser = JFileChooser()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileFiler?.let { fileChooser.fileFilter = it }

        val frame = JFrame()
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false

        val action = fileChooser.showSaveDialog(frame)
        frame.dispose()

        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }
}