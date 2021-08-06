package net.ccbluex.liquidbounce.ui.other;

import javax.sound.sampled.*;
import java.io.File;

public class TipSoundPlayer {
    private final File file;

    public TipSoundPlayer(File file){
        this.file=file;
    }

    public void asyncPlay(){
        new Thread(this::playSound).start();
    }

    public void playSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }
}
