package Game_2D;

public class AudioManager {

    private final BackgroundMusic bgMusic;
    private final BackgroundMusic loseMusic;
    private final BackgroundMusic winMusic;

    public AudioManager() {
        bgMusic = new BackgroundMusic("/music/MusicBackground.wav");
        loseMusic = new BackgroundMusic("/music/over_ending.wav");
        winMusic = new BackgroundMusic("/music/winMusic.wav");
    }

    public void playBackgroundLoop() {
        if (bgMusic != null) bgMusic.playLoop();
    }

    public void stopBackground() {
        if (bgMusic != null) bgMusic.stop();
    }

    public void stopWin() {
        if (winMusic != null) winMusic.stop();
    }

    public void stopLose() {
        if (loseMusic != null) loseMusic.stop();
    }

    public void playLoseOnce() {
        if (loseMusic != null) loseMusic.playOnce();
    }

    public void playWinOnce() {
        if (winMusic != null) winMusic.playOnce();
    }

    public void resetToBackgroundLoop() {
        if (loseMusic != null) loseMusic.stop();
        if (winMusic != null) winMusic.stop();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.playLoop();
        }
    }
}
