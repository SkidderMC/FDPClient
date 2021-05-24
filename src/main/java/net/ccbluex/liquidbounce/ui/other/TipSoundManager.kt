package net.ccbluex.liquidbounce.ui.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound : TipSoundPlayer
    var disableSound : TipSoundPlayer

    init {
        val enableSoundFile=File(LiquidBounce.fileManager.soundsDir,"enable.wav")
        val disableSoundFile=File(LiquidBounce.fileManager.soundsDir,"disable.wav")

        FileUtils.unpackFile(enableSoundFile,"sounds/enable.wav")
        FileUtils.unpackFile(disableSoundFile,"sounds/disable.wav")

        enableSound= TipSoundPlayer(enableSoundFile)
        disableSound= TipSoundPlayer(disableSoundFile)
    }
}