/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.chat
import java.io.File

/**
 * Module Notebot
 *
 * Plays a note-block song entirely on the client side. Songs are simple text files
 * placed in the FDPCLIENT/songs folder. Each line describes one note:
 *
 *     <tick> <note> [instrument]
 *
 *   - tick:       absolute tick index (20 ticks per second), integer >= 0
 *   - note:       note pitch 0..24 (the note-block range)
 *   - instrument: optional sound name (harp, bass, basedrum, snare, hat,
 *                 guitar, bell, flute, chime, xylophone, pling) - defaults to harp
 *
 * Lines starting with '#' and blank lines are ignored. A simple example:
 *
 *     # twinkle twinkle
 *     0 9 harp
 *     0 9
 *     10 16
 *     10 16
 */
object Notebot : Module("Notebot", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val song by text("Song", "song.txt")
        .describe("File name of the song to play from the songs folder.")
    private val speed by float("Speed", 1.0f, 0.1f..5.0f)
        .describe("Playback speed multiplier for the song.")
    private val volume by float("Volume", 1.0f, 0.1f..2.0f)
        .describe("Volume of the played note sounds.")
    private val loop by boolean("Loop", false)
        .describe("Restart the song from the beginning when it ends.")

    private val instrumentSounds = mapOf(
        "harp" to "note.harp",
        "piano" to "note.harp",
        "bass" to "note.bass",
        "bassguitar" to "note.bass",
        "basedrum" to "note.bd",
        "bd" to "note.bd",
        "kick" to "note.bd",
        "snare" to "note.snare",
        "hat" to "note.hat",
        "click" to "note.hat",
        "guitar" to "note.bassattack",
        "bell" to "note.harp",
        "flute" to "note.harp",
        "chime" to "note.harp",
        "xylophone" to "note.harp",
        "pling" to "note.pling"
    )

    private data class Note(val tick: Int, val pitch: Float, val sound: String)

    private val notes = ArrayList<Note>()
    private var noteIndex = 0
    private var startTime = 0L
    private var songLengthTicks = 0

    override fun onEnable() {
        notes.clear()
        noteIndex = 0
        songLengthTicks = 0

        val file = resolveSongFile()

        if (file == null || !file.isFile) {
            chat("§cNotebot: song not found. Place '$song' in the songs folder: ${songsDir().absolutePath}")
            state = false
            return
        }

        loadSong(file)

        if (notes.isEmpty()) {
            chat("§cNotebot: '$song' has no playable notes.")
            state = false
            return
        }

        startTime = System.currentTimeMillis()
        chat("§aNotebot: playing '${file.name}' (${notes.size} notes).")
    }

    override fun onDisable() {
        notes.clear()
        noteIndex = 0
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (notes.isEmpty()) {
            return@handler
        }

        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
        val currentTick = elapsedSeconds * 20.0 * speed.toDouble()

        while (noteIndex < notes.size && notes[noteIndex].tick <= currentTick) {
            val note = notes[noteIndex]
            player.playSound(note.sound, volume, note.pitch)
            noteIndex++
        }

        if (noteIndex >= notes.size) {
            if (loop) {
                noteIndex = 0
                startTime = System.currentTimeMillis()
            } else {
                chat("§aNotebot: finished.")
                state = false
            }
        }
    }

    private fun loadSong(file: File) {
        try {
            file.forEachLine { rawLine ->
                val line = rawLine.substringBefore('#').trim()

                if (line.isEmpty()) {
                    return@forEachLine
                }

                val parts = line.split(Regex("\\s+"))

                val tick = parts.getOrNull(0)?.toIntOrNull() ?: return@forEachLine
                val rawNote = parts.getOrNull(1)?.toIntOrNull() ?: return@forEachLine
                val note = rawNote.coerceIn(0, 24)

                val instrument = parts.getOrNull(2)?.lowercase() ?: "harp"
                val sound = instrumentSounds[instrument] ?: "note.harp"

                // Note-block pitch: 2^((note - 12) / 12), note 0..24 maps to F#3..F#5.
                val pitch = Math.pow(2.0, (note - 12) / 12.0).toFloat()

                notes.add(Note(tick.coerceAtLeast(0), pitch, sound))
            }

            notes.sortBy { it.tick }
            songLengthTicks = notes.lastOrNull()?.tick ?: 0
        } catch (throwable: Throwable) {
            LOGGER.error("Notebot failed to load song ${file.name}", throwable)
            notes.clear()
        }
    }

    private fun resolveSongFile(): File? {
        val dir = songsDir()

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val direct = File(dir, song)

        if (direct.isFile) {
            return direct
        }

        // Fall back to the first available song file if the configured name is missing.
        return dir.listFiles { f -> f.isFile && f.extension.equals("txt", ignoreCase = true) }
            ?.minByOrNull { it.name.lowercase() }
    }

    private fun songsDir() = File(FileManager.dir, "songs")
}
