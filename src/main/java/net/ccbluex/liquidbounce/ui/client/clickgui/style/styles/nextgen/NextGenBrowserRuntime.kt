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
import java.util.concurrent.CopyOnWriteArrayList
import java.nio.file.Path

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

    /** Reason for the most recent FAILED state (message + exception chain). Shown by the fallback screen. */
    @Volatile
    var lastErrorLog: String = ""
        private set

    /** Completed download packages, in order, for the progressive step list shown on the loading screen. */
    private val downloadStepsInternal = CopyOnWriteArrayList<String>()
    val downloadSteps: List<String> get() = downloadStepsInternal

    /** Package currently downloading (the in-progress step), or empty when none. */
    @Volatile
    var currentDownload: String = ""
        private set

    /** Total asset package count for this download, or 0 when unknown (used for the N/total counter). */
    @Volatile
    var downloadTotal: Int = 0
        private set

    private const val PROXY_CLASS = "net.montoyo.mcef.client.ClientProxy"
    private const val REMOTE_CONFIG_CLASS = "net.montoyo.mcef.remote.RemoteConfig"
    private const val PROGRESS_LISTENER_CLASS = "net.montoyo.mcef.utilities.IProgressListener"

    /** Set while a deliberate (re)install runs so MixinMcefUtil never short-circuits a resource fetch.
     *  Keep in sync with the same constant in MixinMcefUtil. */
    const val MCEF_INSTALLING_PROPERTY = "fdp.mcef.installing"

    private var setFocus: Method? = null
    private var initializationAttempted = false

    /** Bumped on every (re)start so a stale background attempt cannot initialize or report after a retry. */
    @Volatile
    private var attemptId = 0

    @Volatile
    private var forceRedownloadOnNextStart = false

    private val nativeDownloadLock = Any()

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
        NextGenClickGuiServer.stop()
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
        val forceRedownload = forceRedownloadOnNextStart
        forceRedownloadOnNextStart = false

        if (!forceRedownload && adoptExistingRuntime()) {
            return
        }
        if (state == State.FAILED || initializationAttempted) {
            return
        }

        state = State.INITIALIZING
        detail = "Preparing in-game browser (one-time setup)..."
        lastErrorLog = ""
        resetDownloadSteps()
        val attempt = ++attemptId

        Thread({
            try {
                synchronized(nativeDownloadLock) {
                    if (attempt != attemptId) {
                        return@Thread
                    }

                    // Tell MixinMcefUtil a deliberate (re)install is running so it never short-circuits a
                    // resource fetch to "already on disk" - it would otherwise null out every native after
                    // libcef.dll lands and leave a broken half-install. Cleared in finally so steady-state
                    // launches keep skipping the dead mirror. Property is JVM-global; all MCEF openStream
                    // calls for this download happen on this thread inside the lock.
                    System.setProperty(MCEF_INSTALLING_PROPERTY, "true")
                    try {
                        resetMcefVirtualState()
                        configureMcefDownload()

                        if (forceRedownload) {
                            purgeNativeRuntime()
                        }

                        val nativesPresent = hasNativeRuntime()
                        MCEF.ENABLE_EXAMPLE = false
                        // The montoyo mirror is frequently unreachable, and a failed remote update check forces
                        // MCEF into virtual mode even when valid native files are already on disk. When the
                        // runtime is present locally and we are not explicitly re-downloading, skip the remote
                        // check entirely and load it directly.
                        MCEF.SKIP_UPDATES = nativesPresent && !forceRedownload
                        MCEF.WARN_UPDATES = false
                        MCEF.USE_FORGE_SPLASH = false

                        if (nativesPresent && !forceRedownload) {
                            detail = "Starting in-game browser..."
                        } else {
                            downloadNatives()
                            Thread.sleep(250L)
                        }
                    } finally {
                        System.clearProperty(MCEF_INSTALLING_PROPERTY)
                    }
                }

                if (attempt != attemptId) {
                    return@Thread
                }

                Minecraft.getMinecraft().addScheduledTask {
                    if (attempt == attemptId && state == State.INITIALIZING) {
                        initOnRenderThread()
                    }
                }
            } catch (throwable: Throwable) {
                if (attempt != attemptId) {
                    return@Thread
                }
                fail("In-game browser failed to download.", throwable)
                Minecraft.getMinecraft().addScheduledTask {
                    HUD.addNotification(
                        Notification("NextGen ClickGUI", "In-game browser download failed", Type.ERROR)
                    )
                }
            }
        }, "NextGen-Browser-Init").apply { isDaemon = true }.start()
    }

    /**
     * Throw away a failed or stuck preparation and run the whole thing again from scratch: re-download
     * the natives when they are missing and re-initialize. Any in-flight background attempt is orphaned
     * through [attemptId] so it cannot report or initialize after this point. Safe to call from a UI
     * action (the ClickGUI module option and the fallback screen button both route here).
     */
    @Synchronized
    fun retry(redownloadAssets: Boolean = false) {
        attemptId++
        closePersistentBrowser()
        resetMcefVirtualState()
        initializationAttempted = false
        forceRedownloadOnNextStart = redownloadAssets
        progress = -1.0
        lastErrorLog = ""
        resetDownloadSteps()
        detail = if (redownloadAssets) {
            "Reinstalling all in-game browser assets..."
        } else {
            "Resuming in-game browser download (fetching missing assets)..."
        }
        state = State.IDLE
        ensureStarted()
    }

    /** Mark the runtime FAILED, recording a readable reason (message + exception chain) for the fallback UI. */
    private fun fail(message: String, throwable: Throwable? = null) {
        state = State.FAILED
        detail = message
        lastErrorLog = formatFailure(message, throwable)
        LOGGER.error("[NextGen] $message", throwable)
    }

    private fun formatFailure(message: String, throwable: Throwable?): String {
        if (throwable == null) {
            return message
        }
        val builder = StringBuilder(message)
        var cause: Throwable? = throwable
        var depth = 0
        while (cause != null && depth < 4) {
            builder.append('\n').append(cause.javaClass.simpleName)
            cause.message?.let { builder.append(": ").append(it) }
            cause = cause.cause
            depth++
        }
        return builder.toString()
    }

    private fun resetDownloadSteps() {
        downloadStepsInternal.clear()
        currentDownload = ""
        downloadTotal = 0
    }

    /** Move the in-progress package into the completed step list (when a task ends or the next begins). */
    private fun finishCurrentStep() {
        val name = currentDownload
        if (name.isNotEmpty() && downloadStepsInternal.lastOrNull() != name) {
            downloadStepsInternal.add(name)
        }
        currentDownload = ""
    }

    /** Strip any directory part so a step reads as the package/file name only. */
    private fun cleanTaskName(raw: String?): String {
        val trimmed = raw?.trim().orEmpty()
        return trimmed.substringAfterLast('/').substringAfterLast('\\').ifEmpty { trimmed }
    }

    /** Compact one-line status for the in-game overlay: current package, completed count and percent. */
    private fun downloadDetailLine(): String = buildString {
        append("Downloading browser assets")
        if (currentDownload.isNotEmpty()) {
            append(": ").append(currentDownload)
        }
        val done = downloadStepsInternal.size
        when {
            downloadTotal > 0 -> append(" (").append(done).append('/').append(downloadTotal).append(')')
            done > 0 -> append(" (").append(done).append(" done)")
        }
        progress.takeIf { it in 0.0..100.0 }?.let { append(" · ").append(it.toInt()).append('%') }
    }

    private fun hasNativeRuntime(): Boolean =
        REQUIRED_NATIVE_FILES.all { fileName ->
            File(mc.mcDataDir, fileName).let { it.isFile && it.length() > 0L }
        }

    /**
     * Routes MCEF's downloader through montoyo's plain-HTTP mirror. We boot the browser by calling
     * the proxy directly, bypassing the mod pre-init that would set up HTTPS; the mirror's HTTPS
     * certificate chains to a root (ISRG) that the old Java 8 bundled by most launchers does not
     * trust, so HTTPS downloads fail the handshake and a fresh install falls into virtual mode. The
     * mirror serves the exact same files over HTTP, which needs no TLS and works on every runtime,
     * so we force it.
     *
     * We deliberately do NOT touch any process-wide SSL state here: a previous attempt overrode the
     * JVM default HTTPS factory and broke Minecraft's own Mojang authentication ("Cannot contact
     * authentication server"), which blocked multiplayer. This stays scoped to MCEF only.
     */
    private fun configureMcefDownload() {
        MCEF.SECURE_MIRRORS_ONLY = false
        MCEF.FORCE_MIRROR = HTTP_MIRROR
        LOGGER.info("[NextGen] In-game browser download routed through the HTTP mirror ($HTTP_MIRROR).")
    }

    private fun resetMcefVirtualState() {
        runCatching {
            Class.forName(PROXY_CLASS).getField("VIRTUAL").setBoolean(null, false)
        }.onFailure {
            LOGGER.warn("[NextGen] Could not reset MCEF virtual-mode flag before retry.", it)
        }

        val proxy = runCatching { MCEF.PROXY }.getOrNull() ?: return
        val virtual = runCatching {
            proxy.javaClass.getMethod("isVirtual").invoke(proxy) as? Boolean
        }.getOrNull() == true

        if (virtual) {
            runCatching { MCEF.PROXY = null }
                .onFailure { LOGGER.warn("[NextGen] Could not discard the virtual MCEF proxy before retry.", it) }
        }
    }

    private fun purgeNativeRuntime() {
        detail = "Removing cached in-game browser assets..."
        val rootDir = mc.mcDataDir
        val rootPath = rootDir.absolutePath.replace("\\", "/").trimEnd('.', '/')
        LOGGER.info("[NextGen] Removing cached in-game browser assets before re-download.")

        runCatching {
            Class.forName(PROXY_CLASS).getField("ROOT").set(null, rootPath)
        }.onFailure {
            LOGGER.warn("[NextGen] Could not point MCEF at the game directory before cleanup.", it)
        }

        val targets = linkedSetOf<File>()
        targets += readInstalledNativeListing(rootDir)
        targets += remoteResourceFiles()
        REQUIRED_NATIVE_FILES.mapTo(targets) { File(rootDir, it) }
        EXTRA_NATIVE_TARGETS.mapTo(targets) { File(rootDir, it) }
        // Drop the cached config too, so a clean reinstall re-fetches a fresh manifest instead of
        // letting a stale mcef2.json be served back as a "success".
        targets += File(rootDir, "mcef2.new")
        targets += File(rootDir, "mcef2.json")

        targets.forEach { deleteNativeTarget(rootDir, it) }
        deleteNativeTarget(rootDir, File(File(rootDir, "config"), "mcefFiles.lst"))

        progress = -1.0
        detail = "Downloading in-game browser assets..."
    }

    private fun remoteResourceFiles(): List<File> = runCatching {
        val remoteConfigClass = Class.forName(REMOTE_CONFIG_CLASS)
        val remoteConfig = remoteConfigClass.getDeclaredConstructor().newInstance()
        remoteConfigClass.getMethod("load").invoke(remoteConfig)
        val resources = remoteConfigClass.getMethod("getResourceArray").invoke(remoteConfig) as? Array<*>
        resources?.filterIsInstance<File>().orEmpty()
    }.onFailure {
        LOGGER.warn("[NextGen] Could not read MCEF remote resource list; cleaning known native files only.", it)
    }.getOrDefault(emptyList())

    private fun readInstalledNativeListing(rootDir: File): List<File> {
        val listing = File(File(rootDir, "config"), "mcefFiles.lst")
        if (!listing.isFile) {
            return emptyList()
        }

        return runCatching {
            listing.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && !it.startsWith(".") && !it.startsWith("/") && !it.startsWith("\\") }
                .map { File(rootDir, it) }
                .asReversed()
        }.onFailure {
            LOGGER.warn("[NextGen] Could not read MCEF installed-file list.", it)
        }.getOrDefault(emptyList())
    }

    private fun deleteNativeTarget(rootDir: File, target: File) {
        if (!target.exists()) {
            return
        }

        val rootPath = canonicalPath(rootDir)
        val targetPath = canonicalPath(target)
        if (rootPath == null || targetPath == null || targetPath == rootPath || !targetPath.startsWith(rootPath)) {
            LOGGER.warn("[NextGen] Refusing to delete non-MCEF path during asset cleanup: ${target.absolutePath}")
            return
        }

        if (!target.deleteRecursively()) {
            target.deleteOnExit()
            LOGGER.warn("[NextGen] Could not delete ${target.absolutePath}; scheduled it for deletion on exit.")
        }
    }

    private fun canonicalPath(file: File): Path? =
        runCatching { file.canonicalFile.toPath() }.getOrNull()

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

        downloadTotal = runCatching {
            (remoteConfigClass.getMethod("getResourceArray").invoke(remoteConfig) as? Array<*>)?.size ?: 0
        }.getOrDefault(0)

        val listenerClass = Class.forName(PROGRESS_LISTENER_CLASS)
        val listener = Proxy.newProxyInstance(listenerClass.classLoader, arrayOf(listenerClass)) { _, method, args ->
            when (method.name) {
                "onTaskChanged" -> {
                    finishCurrentStep()
                    currentDownload = cleanTaskName(args?.getOrNull(0)?.toString())
                    progress = 0.0
                    detail = downloadDetailLine()
                }
                "onProgressed" -> {
                    val pct = (args?.getOrNull(0) as? Double)?.takeIf { it in 0.0..100.0 }
                    if (pct != null) {
                        progress = pct
                        detail = downloadDetailLine()
                    }
                }
                "onProgressEnd" -> {
                    finishCurrentStep()
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
            fail("In-game browser initialization was already attempted.")
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
                fail(
                    if (virtual) {
                        "In-game browser unavailable (virtual mode) - native assets missing or blocked."
                    } else {
                        "In-game browser failed to start."
                    }
                )
                return
            }

            markRuntimeReady("initialized")
        } catch (throwable: Throwable) {
            fail("In-game browser failed to start.", throwable)
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

    private val EXTRA_NATIVE_TARGETS = arrayOf(
        "chrome_elf.dll",
        "d3dcompiler_47.dll",
        "libEGL.dll",
        "libGLESv2.dll",
        "cef_100_percent.pak",
        "cef_200_percent.pak",
        "cef_extensions.pak",
        "devtools_resources.pak",
        "v8_context_snapshot.bin",
        "jcef_helper.exe",
        "jcef_helper",
        "libcef.so",
        "libjcef.so",
        "MCEFLocales",
        "MCEFCache",
        "swiftshader"
    )

    private const val HTTP_MIRROR = "http://montoyo.net/jcef"

    private const val BROWSER_WARMUP_MILLIS = 450L
    private const val BROWSER_WARMUP_FRAMES = 3
    private const val HIDDEN_PUMP_INTERVAL_MILLIS = 250L
}
