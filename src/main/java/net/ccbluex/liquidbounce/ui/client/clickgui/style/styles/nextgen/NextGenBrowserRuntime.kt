/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nextgen

import net.ccbluex.liquidbounce.event.ClientShutdownEvent
import net.ccbluex.liquidbounce.event.GameLoopEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.ui.client.hud.HUD
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Type
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.client.Minecraft
import net.montoyo.mcef.MCEF
import net.montoyo.mcef.api.API
import net.montoyo.mcef.api.IBrowser
import net.montoyo.mcef.api.MCEFApi
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

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
object NextGenBrowserRuntime : MinecraftInstance, Listenable {

    enum class State { IDLE, INITIALIZING, READY, FAILED }

    @Volatile
    var state: State = State.IDLE
        private set

    @Volatile
    var detail: String = ""
        private set

    /** Download progress in 0..100, or -1 when no measurable download is in progress. */
    @Volatile
    var progress: Double = -1.0
        private set

    private const val PROXY_CLASS = "net.montoyo.mcef.client.ClientProxy"
    private const val REMOTE_CONFIG_CLASS = "net.montoyo.mcef.remote.RemoteConfig"
    private const val PROGRESS_LISTENER_CLASS = "net.montoyo.mcef.utilities.IProgressListener"

    private var setFocus: Method? = null
    private var initializationAttempted = false

    private var persistentBrowser: IBrowser? = null
    private var persistentUrl = ""
    private var persistentRequested = false
    private var persistentVisible = false
    private var persistentFocused = false
    private var browserCreatedAt = 0L
    private var browserTextureFrames = 0
    private var browserReady = false
    private var lastTextureId = 0
    private var lastResizeWidth = 0
    private var lastResizeHeight = 0
    private var lastHiddenPump = 0L
    private var loggedCreateFailure = false

    private val onGameLoop = handler<GameLoopEvent>(always = true, priority = Byte.MIN_VALUE) {
        tickPersistentBrowser()
    }

    private val onShutdown = handler<ClientShutdownEvent>(always = true) {
        closePersistentBrowser()
    }

    /**
     * Persistent on-screen download status. Stays visible the whole time the browser runtime is
     * being prepared/downloaded (does NOT fade like a notification) and disappears only once the
     * download finishes (state leaves INITIALIZING).
     */
    private val onRenderOverlay = handler<Render2DEvent>(always = true) {
        if (state != State.INITIALIZING) {
            return@handler
        }

        val font = mc.fontRendererObj ?: return@handler
        val sr = ScaledResolution(mc)
        val text = detail.ifEmpty { "Preparing in-game browser (one-time ~160MB download)..." }
        val pct = progress
        val hasBar = pct in 0.0..100.0

        val boxW = (font.getStringWidth(text) + 16).coerceAtLeast(200)
        val boxH = if (hasBar) 30 else 20
        val x = (sr.scaledWidth - boxW) / 2
        val y = 6

        RenderUtils.drawRect(x.toFloat(), y.toFloat(), (x + boxW).toFloat(), (y + boxH).toFloat(), 0xC8101014.toInt())
        RenderUtils.drawRect(x.toFloat(), y.toFloat(), (x + boxW).toFloat(), (y + 1).toFloat(), 0x40FFFFFF)
        font.drawStringWithShadow(text, (x + 8).toFloat(), (y + 6).toFloat(), 0xFFFFFF)

        if (hasBar) {
            val barX = x + 8
            val barY = y + 20
            val barW = boxW - 16
            RenderUtils.drawRect(barX.toFloat(), barY.toFloat(), (barX + barW).toFloat(), (barY + 4).toFloat(), 0xC0202024.toInt())
            val filled = (barW * (pct / 100.0)).toInt()
            if (filled > 0) {
                RenderUtils.drawRect(barX.toFloat(), barY.toFloat(), (barX + filled).toFloat(), (barY + 4).toFloat(), 0xFF3A8BFF.toInt())
            }
        }
    }

    @Synchronized
    fun ensureStarted() {
        if (state == State.READY || state == State.INITIALIZING) {
            return
        }
        if (adoptExistingRuntime()) {
            return
        }
        if (state == State.FAILED || initializationAttempted) {
            return
        }

        state = State.INITIALIZING
        detail = "Preparing in-game browser (one-time setup)..."

        Thread({
            try {
                MCEF.ENABLE_EXAMPLE = false
                MCEF.SKIP_UPDATES = false
                MCEF.WARN_UPDATES = false
                MCEF.USE_FORGE_SPLASH = false
                installDownloadTrustStore()
                if (hasNativeRuntime()) {
                    detail = "Starting in-game browser..."
                } else {
                    downloadNatives()
                    Thread.sleep(250L)
                }

                Minecraft.getMinecraft().addScheduledTask {
                    if (state == State.INITIALIZING) {
                        initOnRenderThread()
                    }
                }
            } catch (throwable: Throwable) {
                state = State.FAILED
                detail = "In-game browser failed to download."
                LOGGER.error("[NextGen] Failed to prepare the in-game browser runtime", throwable)
                Minecraft.getMinecraft().addScheduledTask {
                    HUD.addNotification(
                        Notification("NextGen ClickGUI", "In-game browser download failed", Type.ERROR)
                    )
                }
            }
        }, "NextGen-Browser-Init").apply { isDaemon = true }.start()
    }

    private fun hasNativeRuntime(): Boolean =
        REQUIRED_NATIVE_FILES.all { fileName -> File(mc.mcDataDir, fileName).isFile }

    /**
     * Makes the native download work on the JVMs that ship with most launchers. We boot the browser
     * by calling the proxy directly, so the mod's normal pre-init (which installs an SSL factory)
     * never runs and the factory stays null. The download mirror serves a Let's Encrypt certificate
     * that chains to ISRG Root X1, which the old Java 8 runtimes bundled by launchers do not trust,
     * so the HTTPS handshake fails and a fresh install falls into virtual mode.
     *
     * Two layers fix this, in order of preference:
     *  1. Install a trust store of the JVM defaults plus the ISRG roots (embedded inline so it can
     *     never be lost to classpath/repackaging quirks). HTTPS then works and stays secure.
     *  2. If HTTPS still cannot connect (truly ancient runtime, exotic TLS, broken cert path), probe
     *     the mirror and, on failure, force the plain-HTTP mirror so the download always succeeds.
     * Both [MCEF.SECURE_MIRRORS_ONLY] is cleared and the probe runs before any download. On a modern
     * JDK the roots are already trusted, so HTTPS is used and the fallback never triggers.
     */
    private fun installDownloadTrustStore() {
        MCEF.SECURE_MIRRORS_ONLY = false

        if (MCEF.SSL_SOCKET_FACTORY == null) {
            runCatching {
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
                var index = 0

                val systemTrustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                    .apply { init(null as KeyStore?) }
                    .trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()
                systemTrustManager?.acceptedIssuers?.forEach { keyStore.setCertificateEntry("system-${index++}", it) }

                val certificateFactory = CertificateFactory.getInstance("X.509")
                var roots = 0
                BUNDLED_ROOTS.forEach { pem ->
                    runCatching {
                        pem.byteInputStream().use { keyStore.setCertificateEntry("root-${index++}", certificateFactory.generateCertificate(it)) }
                        roots++
                    }.onFailure { LOGGER.warn("[NextGen] Could not parse a bundled download root certificate.", it) }
                }

                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                    .apply { init(keyStore) }
                MCEF.SSL_SOCKET_FACTORY = SSLContext.getInstance("TLS")
                    .apply { init(null, trustManagerFactory.trustManagers, null) }
                    .socketFactory
                LOGGER.info("[NextGen] Installed in-game browser download trust store ($roots bundled roots).")
            }.onFailure {
                LOGGER.warn("[NextGen] Could not install the in-game browser download trust store; HTTPS may fail on old runtimes.", it)
            }
        }

        if (isHttpsMirrorReachable()) {
            LOGGER.info("[NextGen] HTTPS download mirror reachable; using secure download.")
        } else {
            MCEF.FORCE_MIRROR = HTTP_MIRROR
            LOGGER.warn("[NextGen] HTTPS download mirror unreachable; falling back to the HTTP mirror.")
        }
    }

    /** Quick liveness check against the HTTPS mirror using the trust store we just installed. */
    private fun isHttpsMirrorReachable(): Boolean = runCatching {
        val connection = (java.net.URL("$HTTPS_MIRROR/config2.json").openConnection() as javax.net.ssl.HttpsURLConnection).apply {
            MCEF.SSL_SOCKET_FACTORY?.let { sslSocketFactory = it }
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "MCEF")
        }
        try {
            connection.responseCode in 200..399
        } finally {
            connection.disconnect()
        }
    }.getOrDefault(false)

    /** Replicates the proxy's native-download step (load manifest, mark missing, download). Background only. */
    private fun downloadNatives() {
        val proxyClass = Class.forName(PROXY_CLASS)
        val root = mc.mcDataDir.absolutePath.replace("\\", "/").trimEnd('.', '/')
        proxyClass.getField("ROOT").set(null, root)

        val remoteConfigClass = Class.forName(REMOTE_CONFIG_CLASS)
        val remoteConfig = remoteConfigClass.getDeclaredConstructor().newInstance()
        remoteConfigClass.getMethod("load").invoke(remoteConfig)

        val updateFileListing =
            remoteConfigClass.getMethod("updateFileListing", File::class.java, Boolean::class.javaPrimitiveType)
        val configDirectory = File(File(root), "config")
        if (!configDirectory.exists()) {
            configDirectory.mkdirs()
        }

        if (updateFileListing.invoke(remoteConfig, configDirectory, false) != true) {
            LOGGER.warn("[NextGen] MCEF file listing could not be established; continuing with resource check.")
        }

        val listenerClass = Class.forName(PROGRESS_LISTENER_CLASS)
        val listener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass)) { _, method, args ->
            when (method.name) {
                "onTaskChanged" -> detail = "Downloading in-game browser: ${args?.getOrNull(0)}"
                "onProgressed" -> {
                    val pct = (args?.getOrNull(0) as? Double)?.takeIf { it in 0.0..100.0 }
                    if (pct != null) {
                        progress = pct
                        detail = "Downloading in-game browser: ${pct.toInt()}%"
                    }
                }
                "onProgressEnd" -> {
                    progress = 100.0
                    detail = "Starting in-game browser..."
                }
            }
            null
        }
        remoteConfigClass.getMethod("downloadMissing", listenerClass).invoke(remoteConfig, listener)
        updateFileListing.invoke(remoteConfig, configDirectory, true)
    }

    private fun initOnRenderThread() {
        if (state != State.INITIALIZING || adoptExistingRuntime()) {
            return
        }
        if (initializationAttempted) {
            state = State.FAILED
            detail = "In-game browser initialization was already attempted."
            return
        }

        initializationAttempted = true

        try {
            val proxyClass = Class.forName(PROXY_CLASS)
            val instance = proxyClass.getDeclaredConstructor().newInstance()

            MCEF::class.java.getField("PROXY").set(null, instance)

            // Late initialization still needs a temporary active Forge container.
            invokeMcefOnInit(proxyClass, instance)

            val virtual = proxyClass.getField("VIRTUAL").getBoolean(null)
            val app = proxyClass.getMethod("getCefApp").invoke(instance)
            if (virtual || app == null) {
                state = State.FAILED
                detail = if (virtual) {
                    "In-game browser unavailable (virtual mode)."
                } else {
                    "In-game browser failed to start."
                }
                return
            }

            markRuntimeReady("initialized")
        } catch (throwable: Throwable) {
            state = State.FAILED
            detail = "In-game browser failed to start."
            LOGGER.error("[NextGen] Failed to initialize the in-game browser runtime", throwable)
        }
    }

    private fun adoptExistingRuntime(): Boolean {
        val proxy = runCatching { MCEF.PROXY }.getOrNull() ?: return false
        val virtual = runCatching {
            proxy.javaClass.getMethod("isVirtual").invoke(proxy) as? Boolean
        }.getOrNull() == true
        if (virtual) {
            return false
        }

        runCatching {
            proxy.javaClass.getMethod("getCefApp").invoke(proxy)
        }.getOrNull() ?: return false

        markRuntimeReady("adopted")
        return true
    }

    private fun markRuntimeReady(source: String) {
        state = State.READY
        detail = "In-game browser ready."
        LOGGER.info("[NextGen] In-game browser runtime ready ($source).")
        if (progress >= 0.0) {
            progress = -1.0
            Minecraft.getMinecraft().addScheduledTask {
                HUD.addNotification(
                    Notification("NextGen ClickGUI", "In-game browser ready", Type.SUCCESS)
                )
            }
        }
    }
    private fun invokeMcefOnInit(proxyClass: Class<*>, instance: Any) {
        val loaderClass = Class.forName("net.minecraftforge.fml.common.Loader")
        val loadControllerClass = Class.forName("net.minecraftforge.fml.common.LoadController")
        val loader = loaderClass.getMethod("instance").invoke(null)
        val controller = loaderClass.getDeclaredField("modController").apply { isAccessible = true }.get(loader)
        val activeContainer = loadControllerClass.getDeclaredField("activeContainer").apply { isAccessible = true }
        val previousContainer = controller?.let { activeContainer.get(it) }

        if (controller != null && previousContainer == null) {
            activeContainer.set(controller, loaderClass.getMethod("getMinecraftModContainer").invoke(loader))
        }

        try {
            proxyClass.getMethod("onInit").invoke(instance)
        } finally {
            if (controller != null && previousContainer == null) {
                activeContainer.set(controller, null)
            }
        }
    }

    fun preload(url: String = NextGenClickGuiServer.start()) {
        if (url.isBlank()) {
            return
        }

        persistentRequested = true
        persistentUrl = url
        ensureStarted()
    }

    fun attach(url: String) {
        preload(url)
        persistentVisible = true
        resizePersistent(mc.displayWidth, mc.displayHeight)
    }

    fun detach() {
        persistentVisible = false
        persistentFocused = false
        persistentBrowser?.let { focus(it, false) }
    }

    fun browser(): IBrowser? = persistentBrowser

    fun isBrowserReady(): Boolean {
        val browser = persistentBrowser ?: return false
        return isBrowserReady(browser)
    }

    fun resizePersistent(width: Int, height: Int) {
        if (width <= 0 || height <= 0) {
            return
        }

        lastResizeWidth = width
        lastResizeHeight = height
        persistentBrowser?.let { browser ->
            runCatching { browser.resize(width, height) }
                .onFailure { LOGGER.error("[NextGen] Browser resize failed", it) }
        }
    }

    fun ensureFocused() {
        val browser = persistentBrowser ?: return
        if (!persistentFocused) {
            focus(browser, true)
            persistentFocused = true
        }
    }

    fun releasePersistentBrowser() {
        persistentRequested = false
        closePersistentBrowser()
    }

    fun readyApi(): API? =
        if (state == State.READY) runCatching { MCEFApi.getAPI() }.getOrNull() else null

    /** Give (or remove) keyboard focus from the browser. The off-screen browser ignores typed keys
     *  until it is focused; [IBrowser] doesn't expose this, so reach the backing browser reflectively. */
    fun focus(browser: IBrowser, focused: Boolean) {
        val method = setFocus
            ?: runCatching { browser.javaClass.getMethod("setFocus", Boolean::class.javaPrimitiveType) }.getOrNull()
                ?.also { setFocus = it } ?: return
        runCatching { method.invoke(browser, focused) }
            .onFailure { LOGGER.error("[NextGen] Browser focus failed", it) }
    }

    private fun tickPersistentBrowser() {
        if (!persistentRequested || state != State.READY) {
            return
        }

        val now = System.currentTimeMillis()
        if (!persistentVisible && browserReady && now - lastHiddenPump < HIDDEN_PUMP_INTERVAL_MILLIS) {
            return
        }
        lastHiddenPump = now

        ensurePersistentBrowser()

        val browser = persistentBrowser ?: return
        updateReadyState(browser)
    }

    private fun ensurePersistentBrowser() {
        if (persistentUrl.isBlank() || persistentBrowser != null) {
            return
        }

        val api = readyApi() ?: return
        persistentBrowser = runCatching {
            api.createBrowser(persistentUrl, true).also { browser ->
                browserCreatedAt = System.currentTimeMillis()
                browserTextureFrames = 0
                browserReady = false
                lastTextureId = 0
                loggedCreateFailure = false

                val resizeWidth = lastResizeWidth.takeIf { it > 0 } ?: mc.displayWidth
                val resizeHeight = lastResizeHeight.takeIf { it > 0 } ?: mc.displayHeight
                browser.resize(resizeWidth, resizeHeight)
            }
        }.getOrElse {
            if (!loggedCreateFailure) {
                LOGGER.error("[NextGen] Could not create the persistent in-game browser", it)
                loggedCreateFailure = true
            }
            null
        }
    }

    private fun updateReadyState(browser: IBrowser) {
        val textureId = runCatching { browser.getTextureID() }.getOrElse {
            LOGGER.error("[NextGen] Browser texture query failed", it)
            closePersistentBrowser()
            return
        }

        if (textureId <= 0) {
            return
        }

        if (textureId != lastTextureId) {
            lastTextureId = textureId
            browserTextureFrames = 0
            browserReady = false
        }

        browserTextureFrames++
        if (!browserReady && isBrowserReady(browser)) {
            browserReady = true
            LOGGER.info("[NextGen] Persistent browser warmed up.")
        }
    }

    private fun isBrowserReady(browser: IBrowser): Boolean {
        val textureReady = runCatching { browser.getTextureID() > 0 }.getOrDefault(false)
        if (!textureReady) {
            return false
        }

        val warmEnough = System.currentTimeMillis() - browserCreatedAt >= BROWSER_WARMUP_MILLIS &&
            browserTextureFrames >= BROWSER_WARMUP_FRAMES
        if (!warmEnough) {
            return false
        }

        return !runCatching { browser.isPageLoading }.getOrDefault(false)
    }

    private fun closePersistentBrowser() {
        persistentFocused = false
        persistentBrowser?.let { browser ->
            runCatching { browser.close() }
                .onFailure { LOGGER.error("[NextGen] Browser close failed", it) }
        }
        persistentBrowser = null
        browserCreatedAt = 0L
        browserTextureFrames = 0
        browserReady = false
        lastTextureId = 0
    }

    private val REQUIRED_NATIVE_FILES = arrayOf(
        "jcef.dll",
        "libcef.dll",
        "icudtl.dat",
        "cef.pak",
        "natives_blob.bin",
        "snapshot_blob.bin"
    )

    private const val HTTPS_MIRROR = "https://montoyo.net/jcef"
    private const val HTTP_MIRROR = "http://montoyo.net/jcef"

    /**
     * ISRG Root X1 / X2 (the Let's Encrypt roots, valid through 2035 / 2040). Embedded inline rather
     * than read from a resource so repackaging or classpath quirks can never strip them. These are
     * the trust anchors the HTTPS download mirror's certificate chains to.
     */
    private val BUNDLED_ROOTS = arrayOf(
        """
-----BEGIN CERTIFICATE-----
MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC
B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv
KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbW
nOlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynT
njh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrb
wqHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53C
IrU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qx
q4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TP
AmRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57
demyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
-----END CERTIFICATE-----
""".trimIndent(),
        """
-----BEGIN CERTIFICATE-----
MIICGzCCAaGgAwIBAgIQQdKd0XLq7qeAwSxs6S+HUjAKBggqhkjOPQQDAzBPMQsw
CQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJuZXQgU2VjdXJpdHkgUmVzZWFyY2gg
R3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBYMjAeFw0yMDA5MDQwMDAwMDBaFw00
MDA5MTcxNjAwMDBaME8xCzAJBgNVBAYTAlVTMSkwJwYDVQQKEyBJbnRlcm5ldCBT
ZWN1cml0eSBSZXNlYXJjaCBHcm91cDEVMBMGA1UEAxMMSVNSRyBSb290IFgyMHYw
EAYHKoZIzj0CAQYFK4EEACIDYgAEzZvVn4CDCuwJSvMWSj5cz3es3mcFDR0HttwW
+1qLFNvicWDEukWVEYmO6gbf9yoWHKS5xcUy4APgHoIYOIvXRdgKam7mAHf7AlF9
ItgKbppbd9/w+kHsOdx1ymgHDB/qo0IwQDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0T
AQH/BAUwAwEB/zAdBgNVHQ4EFgQUfEKWrt5LSDv6kviejM9ti6lyN5UwCgYIKoZI
zj0EAwMDaAAwZQIwe3lORlCEwkSHRhtFcP9Ymd70/aTSVaYgLXTWNLxBo1BfASdW
tL4ndQavEi51mI38AjEAi/V3bNTIZargCyzuFJ0nN6T5U6VR5CmD1/iQMVtCnwr1
/q4AaOeMSQ+2b1tbFfLn
-----END CERTIFICATE-----
""".trimIndent()
    )

    private const val BROWSER_WARMUP_MILLIS = 450L
    private const val BROWSER_WARMUP_FRAMES = 3
    private const val HIDDEN_PUMP_INTERVAL_MILLIS = 250L
}
