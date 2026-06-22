/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import com.google.gson.JsonParser
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.io.applyBypassHttps
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Zero-setup Spotify source. Reads the currently playing track straight from the Windows media
 * session (SMTC), so it works with the Spotify desktop app OR the web player without any account
 * authorization — nothing to configure, it just reads whatever Spotify is already playing. Playback
 * control is issued through the same media session.
 */
object SpotifyLocalSource {

    private val isWindows = System.getProperty("os.name", "").contains("Windows", ignoreCase = true)

    // Album art for the zero-setup source: the OS media session does not expose a readable thumbnail
    // from PowerShell, so we look the cover up by artist + title and embed it as a data URI (which
    // always renders in the in-game browser, unlike an external URL). Fetched off-thread, cached per track.
    private val coverCache = ConcurrentHashMap<String, String>()
    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .applyBypassHttps()
            .build()
    }

    // PowerShell prologue: load the WinRT media-session API and resolve the active Spotify session.
    private val PREFIX = """
        ${'$'}ProgressPreference='SilentlyContinue'
        [Console]::OutputEncoding=[System.Text.Encoding]::UTF8
        Add-Type -AssemblyName System.Runtime.WindowsRuntime
        ${'$'}g=([System.WindowsRuntimeSystemExtensions].GetMethods()|?{${'$'}_.Name -eq 'AsTask' -and ${'$'}_.GetParameters().Count -eq 1 -and ${'$'}_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1'})[0]
        function A(${'$'}t,${'$'}rt){${'$'}x=${'$'}g.MakeGenericMethod(${'$'}rt).Invoke(${'$'}null,@(${'$'}t));${'$'}x.Wait(-1)|Out-Null;${'$'}x.Result}
        [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager,Windows.Media.Control,ContentType=WindowsRuntime]|Out-Null
        ${'$'}m=A ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
        ${'$'}s=${'$'}m.GetSessions()|?{${'$'}_.SourceAppUserModelId -match 'Spotify'}|Select-Object -First 1
        if(-not ${'$'}s){${'$'}s=${'$'}m.GetCurrentSession()}
    """.trimIndent()

    private val READ_BODY = """
        if(${'$'}s){
          ${'$'}p=${'$'}s.GetPlaybackInfo()
          ${'$'}info=A (${'$'}s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
          ${'$'}tl=${'$'}s.GetTimelineProperties()
          ("RESULT`t{0}`t{1}`t{2}`t{3}`t{4}" -f ${'$'}p.PlaybackStatus,${'$'}info.Artist,${'$'}info.Title,[int]${'$'}tl.Position.TotalMilliseconds,[int]${'$'}tl.EndTime.TotalMilliseconds)
        } else { "RESULT`tNONE" }
    """.trimIndent()

    private val encodedRead: String by lazy { encode("$PREFIX\n$READ_BODY") }

    fun isAvailable(): Boolean = isWindows

    /** Reads the current Spotify playback straight from the OS media session. Blocking; call off-thread. */
    fun fetchNowPlaying(): SpotifyState? {
        if (!isWindows) return null
        val output = runPowerShell(encodedRead, readOutput = true) ?: return null
        return parse(output)
    }

    fun playPause() = control("TryTogglePlayPauseAsync")
    fun next() = control("TrySkipNextAsync")
    fun previous() = control("TrySkipPreviousAsync")

    private fun control(action: String) {
        if (!isWindows) return
        val body = "if(${'$'}s){ A (${'$'}s.$action()) ([bool])|Out-Null }"
        runPowerShell(encode("$PREFIX\n$body"), readOutput = false)
    }

    private fun runPowerShell(encodedCommand: String, readOutput: Boolean): String? {
        var process: Process? = null
        return try {
            val proc = ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encodedCommand,
            ).redirectErrorStream(true).start()
            process = proc

            // Drain stdout (stderr merged in) on a daemon thread so a misbehaving PowerShell can never
            // block us past the hard timeout below — the worker must never hang on this call.
            val sb = StringBuilder()
            val reader = Thread {
                runCatching {
                    proc.inputStream.bufferedReader(Charsets.UTF_8).use { r ->
                        val buf = CharArray(4096)
                        while (true) {
                            val n = r.read(buf)
                            if (n < 0) break
                            sb.append(buf, 0, n)
                        }
                    }
                }
            }
            reader.isDaemon = true
            reader.start()

            if (!proc.waitFor(8, TimeUnit.SECONDS)) {
                proc.destroyForcibly()
            }
            reader.join(1500)
            if (readOutput) sb.toString() else null
        } catch (t: Throwable) {
            LOGGER.warn("[Spotify][Local] media session call failed: ${t.message}")
            null
        } finally {
            process?.let { if (it.isAlive) it.destroyForcibly() }
        }
    }

    private fun parse(output: String): SpotifyState? {
        val line = output.lineSequence().map { it.trim() }.lastOrNull { it.startsWith("RESULT\t") } ?: return null
        val parts = line.removePrefix("RESULT\t").split('\t')
        if (parts.isEmpty() || parts[0] == "NONE" || parts.size < 5) {
            return null
        }
        val title = parts[2]
        if (title.isBlank()) {
            return null
        }
        val cover = resolveCover(parts[1], title)
        val track = SpotifyTrack(
            id = "local:${parts[1]}:$title",
            title = title,
            artists = parts[1],
            album = "",
            coverUrl = cover,
            durationMs = parts[4].toIntOrNull() ?: 0,
        )
        return SpotifyState(
            track = track,
            isPlaying = parts[0].equals("Playing", ignoreCase = true),
            progressMs = parts[3].toIntOrNull() ?: 0,
            shuffleEnabled = false,
            repeatMode = SpotifyRepeatMode.OFF,
        )
    }

    private fun resolveCover(artist: String, title: String): String? {
        if (title.isBlank()) return null
        val key = "$artist|$title".lowercase()
        val cached = coverCache[key]
        if (cached != null) return cached.ifBlank { null }
        // Not cached: fetch off-thread so we never delay the now-playing text. Mark in-flight (empty)
        // to avoid duplicate fetches; the next poll picks up the result once it lands.
        coverCache[key] = ""
        Thread {
            val uri = fetchCoverDataUri(artist, title) ?: ""
            if (coverCache.size > 48) coverCache.clear()
            coverCache[key] = uri
        }.apply { isDaemon = true; name = "spotify-cover" }.start()
        return null
    }

    private fun fetchCoverDataUri(artist: String, title: String): String? = runCatching {
        val term = URLEncoder.encode(
            listOf(artist, title).filter { it.isNotBlank() }.joinToString(" "), "UTF-8",
        )
        val artUrl = http.newCall(
            Request.Builder()
                .url("https://itunes.apple.com/search?term=$term&entity=song&limit=1")
                .header("User-Agent", "Mozilla/5.0")
                .build(),
        ).execute().use { resp ->
            if (!resp.isSuccessful) return@runCatching null
            val body = resp.body?.string()
            if (body.isNullOrBlank()) return@runCatching null
            val results = JsonParser().parse(body).asJsonObject.getAsJsonArray("results")
            if (results == null || results.size() == 0) return@runCatching null
            val raw = results[0].asJsonObject.get("artworkUrl100")?.asString ?: return@runCatching null
            raw.replace("100x100bb", "300x300bb")
        }
        http.newCall(Request.Builder().url(artUrl).build()).execute().use { resp ->
            if (!resp.isSuccessful) return@runCatching null
            val bytes = resp.body?.bytes() ?: return@runCatching null
            "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes)
        }
    }.getOrNull()

    private fun encode(script: String): String =
        Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))
}
