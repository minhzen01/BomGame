package bomberman.musics;

import javax.sound.sampled.*;
import java.net.URL;

public class Sound {
    private Clip clip;
    private final int time;

    public void play() {
        clip.start();
        clip.loop(time == -1 ? Clip.LOOP_CONTINUOUSLY : time);
    }
    public void stop() {
        clip.stop();
    }

    public Sound(String url1, int time1){
        URL url = this.getClass().getClassLoader().getResource(url1);
        time = time1;
        try {
            assert url != null;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        }
        catch (Exception ignored) {

        }
    }
}
