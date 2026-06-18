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
import java.lang.reflect.Method

/**
 * Boots the embedded browser runtime on demand and drives its per-frame work so the web
 * ClickGUI can render inside the game. The native runtime is fetched once and cached in the
 * game directory; everything here is reflection-based so the client compiles without the
 * embedded-browser native classes on the build classpath.
 *
 * The embedded runtime is single-threaded with a manual message loop: it MUST be initialized,
 * pumped and have its texture uploaded all on the same render thread. We therefore run the whole
 * setup on the client/render thread (the first-time native download briefly blocks it; the runtime
 * is cached afterwards so subsequent boots are instant).
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

        Minecraft.getMinecraft().addScheduledTask {
            try {
                val proxyClass = Class.forName(PROXY_CLASS)
                val instance = proxyClass.getDeclaredConstructor().newInstance()

                MCEF.SKIP_UPDATES = false
                MCEF.WARN_UPDATES = false
                MCEF::class.java.getField("PROXY").set(null, instance)

                proxyClass.getMethod("onInit").invoke(instance)

                val virtual = proxyClass.getField("VIRTUAL").getBoolean(null)
                if (virtual) {
                    state = State.FAILED
                    detail = "In-game browser unavailable (virtual mode)."
                    return@addScheduledTask
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
