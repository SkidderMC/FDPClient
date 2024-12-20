/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter

object MiscUtils : MinecraftInstance() {
    fun showErrorPopup(title: String, message: String) =
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)

    fun Throwable.showErrorPopup() = showErrorPopup(javaClass.simpleName, localizedMessage)

    fun showURL(url: String) =
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            e.showErrorPopup()
        } catch (e: URISyntaxException) {
            e.showErrorPopup()
        }

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