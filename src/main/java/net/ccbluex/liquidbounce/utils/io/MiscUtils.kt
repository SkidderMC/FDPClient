/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.io

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.awt.Desktop
import java.awt.Font
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

object MiscUtils : MinecraftInstance {

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

        return base + '\n'
    }

    @JvmStatic
    fun showMessageDialog(title: String, message: Any, messageType: Int = JOptionPane.ERROR_MESSAGE) =
        JOptionPane.showMessageDialog(null, message, title, messageType)

    @JvmStatic
    fun Throwable.showErrorPopup(
        titlePrefix: String = "Exception occurred: ",
        extraContent: String = LocalDateTime.now().toString() + '\n'
    ) {
        if (mc.isFullScreen) mc.toggleFullscreen()

        val exceptionType = javaClass.simpleName

        val title = titlePrefix + exceptionType

        val content = extraContent + "--- Stacktrace ---\n" + stackTraceToString()

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
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                clipboard.setContents(StringSelection(content), null)
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
            fileChooser.selectedFile.takeIf { f -> fileFilers.all { it.accept(f) } }
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