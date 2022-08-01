package net.ccbluex.liquidbounce.ui.sound

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound: TipSoundPlayer
    var disableSound: TipSoundPlayer

    init {
        val enableSoundFile = File(FDPClient.fileManager.soundsDir, "enable.wav")
        val disableSoundFile = File(FDPClient.fileManager.soundsDir, "disable.wav")

        if (!enableSoundFile.exists()) {
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/fdpclient/sound/enable.wav")
        }
        if (!disableSoundFile.exists()) {
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/fdpclient/sound/disable.wav")
        }

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
    }
}