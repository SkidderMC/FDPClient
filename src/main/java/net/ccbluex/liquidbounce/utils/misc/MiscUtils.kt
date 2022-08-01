/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.misc

import net.ccbluex.liquidbounce.utils.MinecraftInstance
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.function.Consumer
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane

object MiscUtils : MinecraftInstance() {
    fun showErrorPopup(message: String) {
        JOptionPane.showMessageDialog(null, message, "Alert", JOptionPane.ERROR_MESSAGE)
    }

    fun showErrorPopup(title: String, message: String) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE)
    }

    fun showURL(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun openFileChooser(): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()
        val fileChooser = JFileChooser()
        val frame = JFrame()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false
        val action = fileChooser.showOpenDialog(frame)
        frame.dispose()
        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }

    fun saveFileChooser(): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()
        val fileChooser = JFileChooser()
        val frame = JFrame()
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false
        val action = fileChooser.showSaveDialog(frame)
        frame.dispose()
        return if (action == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile else null
    }

    fun <T> make(`object`: T, consumer: Consumer<T>): T {
        consumer.accept(`object`)
        return `object`
    }
}