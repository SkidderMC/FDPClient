/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.spotify

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Zero-setup Spotify source. Reads the currently playing track straight from the Windows media
 * session (SMTC), so it works with the Spotify desktop app OR the web player without any account
 * authorization — nothing to configure, it just reads whatever Spotify is already playing. Playback
 * control is issued through the same media session.
 */
object SpotifyLocalSource {

    private val isWindows = System.getProperty("os.name", "").contains("Windows", ignoreCase = true)

    // PowerShell prologue: load the WinRT media-session API and resolve the active Spotify session.
    private val PREFIX = """
        ${'$'}ProgressPreference='SilentlyContinue'
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
        return runCatching {
            val process = ProcessBuilder(
                "powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-EncodedCommand", encodedCommand,
            ).redirectErrorStream(false).start()

            val output = if (readOutput) process.inputStream.bufferedReader(Charsets.UTF_8).readText() else null
            if (!process.waitFor(8, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return null
            }
            output
        }.onFailure { LOGGER.warn("[Spotify][Local] media session call failed: ${it.message}") }.getOrNull()
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
        val track = SpotifyTrack(
            id = "local",
            title = title,
            artists = parts[1],
            album = "",
            coverUrl = null,
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

    private fun encode(script: String): String =
        Base64.getEncoder().encodeToString(script.toByteArray(Charsets.UTF_16LE))
}
