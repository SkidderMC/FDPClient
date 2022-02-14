package net.ccbluex.liquidbounce.ui.cef

import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.IProgressHandler
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.launch.options.FancyUiLaunchOption
import net.ccbluex.liquidbounce.ui.cef.page.ResourceScheme
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.misc.HttpUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiChat
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.cef.browser.CefBrowserCustom
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.browser.scheme.SchemeResourceHandler
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import java.io.File
import kotlin.concurrent.thread


object CefRenderManager : Listenable {

    lateinit var cefApp: CefApp
        private set
    lateinit var cefClient: CefClient
        private set
    lateinit var cefMessageRouter: CefMessageRouter

    private val dataDir = File(LiquidBounce.fileManager.cacheDir, "cef")
    private val cacheDir = File(LiquidBounce.fileManager.cacheDir, "cef_cache")
    private val cefArgs = arrayOf<String>()

    val browsers = mutableListOf<CefBrowserCustom>()

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    fun initializeAsync(progressHandler: IProgressHandler?) {
        thread {
            initialize(progressHandler)
        }
    }

    fun initialize(progressHandler: IProgressHandler?) {
        // data dir will created by CefAppBuilder
        if(!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val gameSettings = Minecraft.getMinecraft().gameSettings

        // use jcef maven CefAppBuilder, it can download resources automatically
        val builder = CefAppBuilder()

        builder.setInstallDir(dataDir)
        progressHandler?.let { builder.setProgressHandler(it) }
        builder.addJcefArgs(*cefArgs)
        builder.cefSettings.windowless_rendering_enabled = true
        builder.cefSettings.locale = gameSettings.language
        builder.cefSettings.cache_path = cacheDir.absolutePath
        builder.cefSettings.user_agent = HttpUtils.DEFAULT_AGENT

        cefApp = builder.build()
        cefClient = cefApp.createClient()

        cefMessageRouter = CefMessageRouter.create()
        cefMessageRouter.addHandler(object : CefMessageRouterHandlerAdapter() {
            /**
             * cef query can be used to contact browser and client
             */
            override fun onQuery(browser: CefBrowser, frame: CefFrame, queryId: Long, request: String, persistent: Boolean, callback: CefQueryCallback): Boolean {
                println("onQuery: $queryId $request")
                callback.success("OK")
                return super.onQuery(browser, frame, queryId, request, persistent, callback)
            }
        }, true)
        cefClient.addMessageRouter(cefMessageRouter)

        configureCefClient(cefClient)

        cefApp.registerSchemeHandlerFactory("resource", "", SchemeResourceHandler.build(ResourceScheme()))

        val version = cefApp.version
        ClientUtils.logInfo("Cef Loaded (jcefVersion=${version.jcefVersion}, cefVersion=${version.cefVersion}, chromeVersion=${version.chromeVersion})")
    }

    private fun configureCefClient(cefClient: CefClient) {
        // TODO
    }

    fun stop() {
        cefApp.dispose()
    }

    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        cefApp.doMessageLoopWork(0L)
        browsers.forEach(CefBrowserCustom::mcefUpdate)
        if(Minecraft.getMinecraft().currentScreen !is GuiChat) {
            FancyUiLaunchOption.render(false)
        }
    }

    override fun handleEvents() = true
}