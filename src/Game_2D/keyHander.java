package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyHander implements KeyListener {
    public boolean upPress, downPress, leftPress, rightPress;
    public boolean restartPress, exitPress;
    public boolean jumpPress;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W) {
            upPress = true;
        }
        if (code == KeyEvent.VK_A) {
            leftPress = true;
        }
        if (code == KeyEvent.VK_SPACE){ jumpPress = true; }
        if (code == KeyEvent.VK_S) {
            downPress = true;
        }

        if (code == KeyEvent.VK_D) {
            rightPress = true;
        }
        if (code == KeyEvent.VK_UP) {
            upPress = true;
        }
        if (code == KeyEvent.VK_LEFT) {
            leftPress = true;
        }
        if (code == KeyEvent.VK_DOWN) {
            downPress = true;
        }
        if (code == KeyEvent.VK_RIGHT) {
            rightPress = true;
        }
        if (code == KeyEvent.VK_R) {
            restartPress = true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            exitPress = true;
        }
    }



    @Override
    public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP){
                upPress = false;
            }
            if (code == KeyEvent.VK_SPACE){
                jumpPress = false;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT){
                leftPress = false;
            }
            if (code == KeyEvent.VK_S|| code == KeyEvent.VK_DOWN){
                downPress = false;
            }
            if (code == KeyEvent.VK_D|| code == KeyEvent.VK_RIGHT){
                rightPress = false;
            }
            if (code == KeyEvent.VK_R) {
                restartPress = false;
            }
            if (code == KeyEvent.VK_ESCAPE) {
                exitPress = false;
            }

    }
}