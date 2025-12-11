package Game_2D;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundMusic {
    private Clip clip;
    public BackgroundMusic(String path) {
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    System.err.println("Lỗi: Không tìm thấy resource tại đường dẫn: ");
                    return;
                }
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
    }


    public void playLoop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public void playOnce() {
        if (clip != null) {
            clip.setFramePosition(0); // quay về đầu file
            clip.start();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void close() {
        if (clip != null) {
            clip.close();
        }
    }
}
