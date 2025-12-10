package Game_2D;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class BackgroundMusic {
    public Clip clip; // FIXED: Changed to public for null checking in gamePanel
    
    public BackgroundMusic(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            
            // FIXED: Better error message with path
            if (is == null) {
                System.err.println("ERROR: Cannot find audio resource at path: " + path);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(is);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            System.out.println("Audio loaded successfully: " + path);
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("ERROR: Unsupported audio file format: " + path);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("ERROR: IOException while loading audio: " + path);
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("ERROR: Audio line unavailable for: " + path);
            e.printStackTrace();
        }
    }

    public void playLoop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } else {
            System.err.println("WARNING: Cannot play loop - clip is null");
        }
    }

    public void playOnce() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset to beginning
            clip.start();
        } else {
            System.err.println("WARNING: Cannot play once - clip is null");
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
