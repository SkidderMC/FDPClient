package net.ccbluex.liquidbounce.ui.sound

import java.io.File
import javax.sound.sampled.AudioSystem

class TipSoundPlayer(private val file: File) {
    fun asyncPlay() {
        Thread { playSound() }.start()
    }

    fun playSound() {
        try {
            val audioInputStream = AudioSystem.getAudioInputStream(file)
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.start()
        } catch (ex: Exception) {
            println("Error with playing sound.")
            ex.printStackTrace()
        }
    }
}