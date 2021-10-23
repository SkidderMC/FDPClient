package net.ccbluex.liquidbounce.ui.ultralight

import com.labymedia.ultralight.UltralightJava
import com.labymedia.ultralight.UltralightPlatform
import com.labymedia.ultralight.UltralightRenderer
import com.labymedia.ultralight.config.FontHinting
import com.labymedia.ultralight.config.UltralightConfig
import com.labymedia.ultralight.plugin.logging.UltralightLogLevel
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.ultralight.support.ClipboardAdapter
import net.ccbluex.liquidbounce.ui.ultralight.support.FileSystemAdapter
import net.ccbluex.liquidbounce.ui.ultralight.view.View
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.FileUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import org.apache.logging.log4j.LogManager
import java.io.File

object UltralightEngine {
    lateinit var platform: UltralightPlatform
    lateinit var renderer: UltralightRenderer

    val logger = LogManager.getLogger("Ultralight")

    val ultralightPath = File(LiquidBounce.fileManager.cacheDir, "ultralight")
    val resourcePath = File(ultralightPath, "resources")
    val pagesPath = File(ultralightPath, "pages")
    val cachePath = File(ultralightPath, "cache")

    const val ULTRALIGHT_NATIVE_VERSION = "0.4.6"

    val views = mutableListOf<View>()

    init {
        if (!pagesPath.exists()) {
            pagesPath.mkdirs()
        }

        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
    }

    fun initEngine() {
        platform = UltralightPlatform.instance()
        platform.setConfig(
            UltralightConfig()
                .forceRepaint(false)
                .animationTimerDelay(1.0 / 60)
                .scrollTimerDelay(1.0 / 60)
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
                UltralightLogLevel.ERROR -> logger.error("[Ultralight/ERR] $message")
                UltralightLogLevel.WARNING -> logger.warn("[Ultralight/WARN] $message")
                UltralightLogLevel.INFO -> logger.info("[Ultralight/INFO] $message")
            }
        }

        renderer = UltralightRenderer.create()
        renderer.logMemoryUsage()
    }

    fun initResources() {
        // download ultralight natives and resources from web
        checkNativeResources()
        checkPageResources()

        // then load it
        UltralightJava.load(resourcePath.toPath())
    }

    private fun checkNativeResources() {
        val versionFile = File(resourcePath, "VERSION")

        // Check if library version is matching the resources version
        if (versionFile.exists() && versionFile.readText() == ULTRALIGHT_NATIVE_VERSION) {
            return
        }

        if (resourcePath.exists()) {
            resourcePath.deleteRecursively()
        }

        resourcePath.mkdirs()

        // download the natives
        val resourcesZip = File(resourcePath, "resources.zip")
        HttpUtils.download("${LiquidBounce.CLIENT_STORAGE}ultralight/$ULTRALIGHT_NATIVE_VERSION/${ClientUtils.osType.friendlyName}-x64.zip", resourcesZip)
        FileUtils.extractZip(resourcesZip, resourcePath)
        resourcesZip.delete()

        versionFile.createNewFile()
        versionFile.writeText(ULTRALIGHT_NATIVE_VERSION)
    }

    private fun checkPageResources() {
        if (File(pagesPath, "NO_UPDATE").exists()) {
            logger.warn("PASSED RESOURCE CHECK BY \"NO_UPDATE\" FILE")
            return
        }

        if (ClientUtils.inDevMode) {
            if (pagesPath.exists()) {
                pagesPath.deleteRecursively()
            }

            pagesPath.mkdirs()

            val projectDir = File(File("./").canonicalFile.parentFile, "ui")
            val srcDir = File(projectDir, "src") // this should not have issues
            val depsDir = File(projectDir, "deps")

            if (!depsDir.exists()) {
                throw NullPointerException("deps dir not exists, please run \"./gradlew ui:build\" first!")
            }

            FileUtils.copyDir(srcDir, pagesPath)
            FileUtils.copyDir(depsDir, File(pagesPath, "lib"))

            return
        }

        val versionFile = File(pagesPath, "VERSION")

        if (versionFile.exists() && versionFile.readText() == LiquidBounce.CLIENT_VERSION) {
            return
        }

        if (pagesPath.exists()) {
            pagesPath.deleteRecursively()
        }

        pagesPath.mkdirs()

        // packaged file in project "ui"
        FileUtils.extractZip(UltralightEngine::class.java.classLoader.getResourceAsStream("ui_resources.zip"), pagesPath)

        versionFile.createNewFile()
        versionFile.writeText(LiquidBounce.CLIENT_VERSION)
    }

    fun registerView(view: View) {
        views.add(view)
    }

    fun unregisterView(view: View) {
        views.remove(view)
        view.close()
    }
}
