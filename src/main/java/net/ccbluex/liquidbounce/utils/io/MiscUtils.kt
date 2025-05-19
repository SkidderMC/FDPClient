/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.awt.Desktop
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.net.ssl.SSLException
import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

object MiscUtils : MinecraftInstance {

    @JvmStatic
    fun copy(content: String) {
        val selection = StringSelection(content)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
    }

    @JvmStatic
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
    fun generateCrashInfo(): String {
        var base = """
            --- Game crash info ---
              Client: ${FDPClient.CLIENT_NAME} ${FDPClient.clientVersionText} (${FDPClient.clientCommit})
            Time: ${LocalDateTime.now()}
            OS: ${System.getProperty("os.name")} (Version: ${System.getProperty("os.version")}, Arch: ${System.getProperty("os.arch")})
            Java: ${System.getProperty("java.version")} (Vendor: ${System.getProperty("java.vendor")})
            Available Processors: ${Runtime.getRuntime().availableProcessors()}
            Max Memory: ${Runtime.getRuntime().maxMemory() / (1024 * 1024)} MB
            Free Memory: ${Runtime.getRuntime().freeMemory() / (1024 * 1024)} MB
            Total Memory: ${Runtime.getRuntime().totalMemory() / (1024 * 1024)} MB
        """.trimIndent()

        if (mc.currentServerData != null) {
            val serverData = mc.currentServerData
            base += """
                Server address: ${serverData.serverIP}
                Server version: ${serverData.gameVersion}
            """.trimIndent()
        }

        return base
    }

    @JvmStatic
    fun showMessageDialog(title: String, message: Any, messageType: Int = JOptionPane.ERROR_MESSAGE) =
        JOptionPane.showMessageDialog(null, message, title, messageType)

    private fun Throwable.possibleTips(): String? {
        return when (this) {
            // Network issue
            is SSLException, is SocketTimeoutException, is SocketException -> """
                It looks like your network connection is experiencing problems, and some HTTP requests are failing.
                1. Check your network, make sure you are online.
                2. Try to restart the client. This might be a temporary issue.
                3. Try to use a VPN. Notice: You should make sure JVM applications are applied.
            """.trimIndent()
            // TODO
            is NullPointerException -> null
            is NoClassDefFoundError -> null
            else -> null
        }
    }

    @JvmStatic
    fun Throwable.showErrorPopup(
        titlePrefix: String = "Exception occurred: ",
        extraContent: String = LocalDateTime.now().toString()
    ) {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val exceptionType = javaClass.simpleName

        val title = titlePrefix + exceptionType

        val tips = possibleTips()

        val content = buildString {
            append(extraContent)
            if (isNotEmpty()) {
                append('\n')
            }
            possibleTips()?.let {
                append("-- Possible Tips ---\n")
                append(it)
            }
            append("--- Stacktrace ---\n")
            append(stackTraceToString())
        }

        val textArea = JTextArea(content).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            font = Font("Consolas", Font.PLAIN, 12)
            adjustTextAreaSize()
        }

        val scrollPane = JScrollPane(textArea).apply {
            preferredSize = java.awt.Dimension(800, 600)
        }

        val copyButton = JButton("Copy Text").apply {
            addActionListener {
                copy(content)
                JOptionPane.showMessageDialog(null, "Text copied to clipboard!", "Info", JOptionPane.INFORMATION_MESSAGE)
            }
        }

        val openIssueButton = JButton("Open GitHub Issue").apply {
            addActionListener {
                showURL("${FDPClient.CLIENT_GITHUB}/issues/new?template=bug_report.yml&title=%5BBUG%5D+Game+crashed+$exceptionType")
            }
        }

        val buttonPanel = JPanel().apply {
            add(copyButton)
            add(openIssueButton)
        }

        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(scrollPane)
            add(buttonPanel)
        }

        showMessageDialog(title, mainPanel)
    }

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
    private inline fun fileChooserAction(
        fileFilers: Array<out FileFilter>,
        isAcceptAllFileFilterUsed: Boolean,
        action: JFileChooser.(JFrame) -> Int
    ): File? {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val fileChooser = JFileChooser()
        fileChooser.currentDirectory = FileManager.dir
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        fileChooser.isAcceptAllFileFilterUsed = isAcceptAllFileFilterUsed || fileFilers.isEmpty()
        fileFilers.forEach(fileChooser::addChoosableFileFilter)

        val frame = JFrame()
        frame.isVisible = true
        frame.toFront()
        frame.isVisible = false

        val actionResult = fileChooser.action(frame)
        frame.dispose()

        return if (actionResult == JFileChooser.APPROVE_OPTION)
            fileChooser.selectedFile.takeIf { f -> fileFilers.any { it.accept(f) } }
        else null
    }

    @JvmStatic
    fun openFileChooser(
        vararg fileFilers: FileFilter,
        acceptAll: Boolean = true,
    ): File? = fileChooserAction(fileFilers, acceptAll, action = JFileChooser::showOpenDialog)

    @JvmStatic
    fun saveFileChooser(
        vararg fileFilers: FileFilter,
        acceptAll: Boolean = true,
    ): File? = fileChooserAction(fileFilers, acceptAll, action = JFileChooser::showSaveDialog)

}

object FileFilters {
    @JvmField
    val JAVASCRIPT = FileNameExtensionFilter("JavaScript Files", "js")

    @JvmField
    val TEXT = FileNameExtensionFilter("Text Files", "txt")

    @JvmField
    val IMAGE = FileNameExtensionFilter("Image Files (png)", "png")

    /**
     * Based on runtime ImageIO
     */
    @JvmField
    val ALL_IMAGES = ImageIO.getReaderFormatNames().mapTo(sortedSetOf(), String::lowercase).let {
        FileNameExtensionFilter("Image Files (${it.joinToString()}", *it.toTypedArray())
    }

    @JvmField
    val SHADER = FileNameExtensionFilter("Shader Files (frag, glsl, shader)", "frag", "glsl", "shader")

    @JvmField
    val ARCHIVE = FileNameExtensionFilter("Archive Files (zip)", "zip")

    @JvmField
    val FONT = FileNameExtensionFilter("Font Files (ttf, otf)", "ttf", "otf")
}