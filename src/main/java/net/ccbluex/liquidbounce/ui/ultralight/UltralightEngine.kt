package net.ccbluex.liquidbounce.ui.ultralight

import com.labymedia.ultralight.UltralightJava
import com.labymedia.ultralight.UltralightPlatform
import com.labymedia.ultralight.UltralightRenderer
import com.labymedia.ultralight.config.FontHinting
import com.labymedia.ultralight.config.UltralightConfig
import com.labymedia.ultralight.plugin.logging.UltralightLogLevel
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.ui.ultralight.support.ClipboardAdapter
import net.ccbluex.liquidbounce.ui.ultralight.support.FileSystemAdapter
import net.ccbluex.liquidbounce.ui.ultralight.view.View
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.FileUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.Display
import java.io.File
import java.net.URL

object UltralightEngine : Listenable {
    lateinit var platform: UltralightPlatform
    lateinit var renderer: UltralightRenderer

    val logger = LogManager.getLogger("Ultralight")

    private val ultralightPath = File(LiquidBounce.fileManager.cacheDir, "ultralight")
    private val resourcePath = File(ultralightPath, "resources")
    private val pagesPath = File(ultralightPath, "pages")
    private val cachePath = File(ultralightPath, "cache")

    private const val ULTRALIGHT_NATIVE_VERSION = "0.4.6"

    var width=0
    var height=0
    var scaledWidth=0
    var scaledHeight=0
    var factor=1

    private val views=mutableListOf<View>()

    init {
        if(!pagesPath.exists())
            pagesPath.mkdirs()

        if(!cachePath.exists())
            cachePath.mkdirs()
    }

    fun init(){
        // download ultralight natives and resources from web
        checkResources()

        // then load it
        UltralightJava.load(resourcePath.toPath())

        platform = UltralightPlatform.instance()
        platform.setConfig(
            UltralightConfig()
                .forceRepaint(false)
                .resourcePath(resourcePath.absolutePath.toString())
                .cachePath(cachePath.absolutePath.toString())
                .fontHinting(FontHinting.SMOOTH)
        )
        platform.usePlatformFontLoader()
        platform.setFileSystem(FileSystemAdapter())
        platform.setClipboard(ClipboardAdapter())
        platform.setLogger { level, message ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (level) {
                UltralightLogLevel.ERROR -> logger.debug("[Ultralight/ERR] $message")
                UltralightLogLevel.WARNING -> logger.debug("[Ultralight/WARN] $message")
                UltralightLogLevel.INFO -> logger.debug("[Ultralight/INFO] $message")
            }
        }

        renderer = UltralightRenderer.create()
        renderer.logMemoryUsage()

        LiquidBounce.eventManager.registerListener(this)
    }

    private fun checkResources(){
        val versionsFile = File(resourcePath, "VERSION")

        // Check if library version is matching the resources version
        if (versionsFile.exists() && versionsFile.readText() == ULTRALIGHT_NATIVE_VERSION)
            return

        if(resourcePath.exists())
            resourcePath.deleteRecursively()

        resourcePath.mkdirs()

        // download the natives
        val resourcesZip = File(resourcePath, "resources.zip")
        FileUtils.downloadFile(resourcesZip, URL("${LiquidBounce.CLIENT_STORAGE}ultralight/$ULTRALIGHT_NATIVE_VERSION/${ClientUtils.osType.friendlyName}-x64.zip"))
        FileUtils.extractZip(resourcesZip, resourcePath)
        resourcesZip.delete()

        versionsFile.createNewFile()
        versionsFile.writeText(ULTRALIGHT_NATIVE_VERSION)
    }

    fun registerView(view: View){
        views.add(view)
    }

    fun unregisterView(view: View){
        views.remove(view)
        view.close()
    }

    @EventTarget
    fun onRender2d(event: Render2DEvent){
        var resized=false
        if(width!=Display.getWidth()) {
            width = Display.getWidth()
            resized=true
        }
        if(height!=Display.getHeight()) {
            height = Display.getHeight()
            resized=true
        }
        val sr=ScaledResolution(Minecraft.getMinecraft())
        scaledWidth=sr.scaledWidth
        scaledHeight=sr.scaledHeight
        factor=sr.scaleFactor

        if(resized){
            views.forEach { it.resize(width, height) }
        }

        this.renderer.update()
        this.renderer.render()
    }

    override fun handleEvents() = true
}