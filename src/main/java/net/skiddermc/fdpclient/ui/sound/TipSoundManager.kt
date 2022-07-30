package net.skiddermc.fdpclient.ui.sound

import net.skiddermc.fdpclient.FDPClient
import net.skiddermc.fdpclient.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound: TipSoundPlayer
    var disableSound: TipSoundPlayer
    var littlesound: TipSoundPlayer

    init {
        val enableSoundFile = File(FDPClient.fileManager.soundsDir, "enable.wav")
        val disableSoundFile = File(FDPClient.fileManager.soundsDir, "disable.wav")
        val littlebutterflyFile = File(FDPClient.fileManager.soundsDir, "littlebutterfly.wav")

        if (!enableSoundFile.exists()) {
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/fdpclient/sound/enable.wav")
        }
        if (!disableSoundFile.exists()) {
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/fdpclient/sound/disable.wav")
        }

        if(!littlebutterflyFile.exists()) {
            FileUtils.unpackFile(littlebutterflyFile, "assets/minecraft/fdpclient/sound/littlebutterfly.wav")
        }

        enableSound = TipSoundPlayer(enableSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
        littlesound = TipSoundPlayer(littlebutterflyFile)
    }
}