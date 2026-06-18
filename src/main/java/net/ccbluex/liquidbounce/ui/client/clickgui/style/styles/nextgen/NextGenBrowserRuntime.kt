/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.minecraft.client.Minecraft
import net.montoyo.mcef.MCEF
import net.montoyo.mcef.api.API
import net.montoyo.mcef.api.IBrowser
import net.montoyo.mcef.api.MCEFApi
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Boots the embedded browser runtime on demand and drives its per-frame work so the web
 * ClickGUI can render inside the game. Everything here is reflection-based so the client compiles
 * without the embedded-browser native classes on the build classpath.
 *
 * The native runtime (~160 MB) is fetched once and cached in the game directory. The download runs
 * on a background thread so a fresh install never freezes the render thread; the browser itself is
 * single-threaded with a manual message loop, so it is initialized, pumped and uploaded all on the
 * render thread.
 */
object NextGenBrowserRuntime {

    enum class State { IDLE, INITIALIZING, READY, FAILED }

    @Volatile
    var state: State = State.IDLE
        private set

    @Volatile
    var detail: String = ""
        private set

    private const val PROXY_CLASS = "net.montoyo.mcef.client.ClientProxy"
    private const val REMOTE_CONFIG_CLASS = "net.montoyo.mcef.remote.RemoteConfig"
    private const val PROGRESS_LISTENER_CLASS = "net.montoyo.mcef.utilities.IProgressListener"

    private var cefApp: Any? = null
    private var doMessageLoopWork: Method? = null
    private var mcefUpdate: Method? = null
    private var setFocus: Method? = null

    @Synchronized
    fun ensureStarted() {
        if (state != State.IDLE) {
            return
        }

        state = State.INITIALIZING
        detail = "Preparing in-game browser (one-time setup)..."

        Thread({
            try {
                MCEF.SKIP_UPDATES = false
                MCEF.WARN_UPDATES = false

                // Phase 1 (background): fetch the native runtime so the render thread never blocks.
                downloadNatives()

                // Phase 2 (render thread): CEF must be initialized on the same thread that later
                // pumps the message loop and uploads its texture.
                Minecraft.getMinecraft().addScheduledTask { initOnRenderThread() }
            } catch (throwable: Throwable) {
                state = State.FAILED
                detail = "In-game browser failed to download."
                LOGGER.error("[NextGen] Failed to prepare the in-game browser runtime", throwable)
            }
        }, "NextGen-Browser-Init").apply { isDaemon = true }.start()
    }

    /** Replicates the proxy's native-download step (load manifest, mark missing, download). Background only. */
    private fun downloadNatives() {
        val remoteConfigClass = Class.forName(REMOTE_CONFIG_CLASS)
        val remoteConfig = remoteConfigClass.getDeclaredConstructor().newInstance()
        remoteConfigClass.getMethod("load").invoke(remoteConfig)

        val updateFileListing =
            remoteConfigClass.getMethod("updateFileListing", File::class.java, Boolean::class.javaPrimitiveType)
        (remoteConfigClass.getMethod("getResourceArray").invoke(remoteConfig) as? Array<*>)?.forEach { resource ->
            (resource as? File)?.let { updateFileListing.invoke(remoteConfig, it, false) }
        }

        val listenerClass = Class.forName(PROGRESS_LISTENER_CLASS)
        val listener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass)) { _, method, args ->
            when (method.name) {
                "onTaskChanged" -> detail = "Downloading in-game browser: ${args?.getOrNull(0)}"
                "onProgressed" -> {
                    val pct = (args?.getOrNull(0) as? Double)?.takeIf { it in 0.0..100.0 }
                    if (pct != null) detail = "Downloading in-game browser: ${pct.toInt()}%"
                }
                "onProgressEnd" -> detail = "Starting in-game browser..."
            }
            null
        }
        remoteConfigClass.getMethod("downloadMissing", listenerClass).invoke(remoteConfig, listener)
    }

    private fun initOnRenderThread() {
        try {
            val proxyClass = Class.forName(PROXY_CLASS)
            val instance = proxyClass.getDeclaredConstructor().newInstance()

            MCEF::class.java.getField("PROXY").set(null, instance)
            proxyClass.getMethod("onInit").invoke(instance)

            val virtual = proxyClass.getField("VIRTUAL").getBoolean(null)
            if (virtual) {
                state = State.FAILED
                detail = "In-game browser unavailable (virtual mode)."
                return
            }

            val app = proxyClass.getMethod("getCefApp").invoke(instance)
            cefApp = app
            doMessageLoopWork = app?.javaClass?.getMethod("doMessageLoopWork", Long::class.javaPrimitiveType)

            state = State.READY
            detail = "In-game browser ready."
            LOGGER.info("[NextGen] In-game browser runtime ready (render thread).")
        } catch (throwable: Throwable) {
            state = State.FAILED
            detail = "In-game browser failed to start."
            LOGGER.error("[NextGen] Failed to initialize the in-game browser runtime", throwable)
        }
    }

    fun readyApi(): API? =
        if (state == State.READY) runCatching { MCEFApi.getAPI() }.getOrNull() else null

    /** Drive the CEF message loop one frame. MUST run on the render thread. No-op until ready. */
    fun pump() {
        if (state != State.READY) {
            return
        }
        val app = cefApp ?: return
        val method = doMessageLoopWork ?: return
        runCatching { method.invoke(app, 0L) }
            .onFailure { LOGGER.error("[NextGen] Browser message-loop pump failed", it) }
    }

    /** Upload the latest painted frame of [browser] into its OpenGL texture. Render thread only. */
    fun updateBrowser(browser: IBrowser) {
        if (state != State.READY) {
            return
        }
        val method = mcefUpdate ?: runCatching { browser.javaClass.getMethod("mcefUpdate") }.getOrNull()
            ?.also { mcefUpdate = it } ?: return
        runCatching { method.invoke(browser) }
            .onFailure { LOGGER.error("[NextGen] Browser texture update failed", it) }
    }

    /** Give (or remove) keyboard focus from the browser. The off-screen browser ignores typed keys
     *  until it is focused; [IBrowser] doesn't expose this, so reach the backing browser reflectively. */
    fun focus(browser: IBrowser, focused: Boolean) {
        val method = setFocus
            ?: runCatching { browser.javaClass.getMethod("setFocus", Boolean::class.javaPrimitiveType) }.getOrNull()
                ?.also { setFocus = it } ?: return
        runCatching { method.invoke(browser, focused) }
            .onFailure { LOGGER.error("[NextGen] Browser focus failed", it) }
    }
}
