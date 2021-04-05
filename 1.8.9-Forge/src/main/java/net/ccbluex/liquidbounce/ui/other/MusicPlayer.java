package net.ccbluex.liquidbounce.ui.other;

import javax.sound.sampled.*;
import java.io.File;

public class MusicPlayer {
    private final File file;

    public MusicPlayer(File file){
        this.file=file;
    }

    public void asyncPlay(){
        new Thread(this::play).start();
    }

    public void play() {
        try {
            AudioInputStream audioInputStream=AudioSystem.getAudioInputStream(file);
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info dataLine_info = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLine_info);
            byte[] b = new byte[1024];
            int len = 0;
            sourceDataLine.open(audioFormat, 1024);
            sourceDataLine.start();
            while ((len = audioInputStream.read(b)) > 0) {
                sourceDataLine.write(b, 0, len);
            }
            audioInputStream.close();
            sourceDataLine.drain();
            sourceDataLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
