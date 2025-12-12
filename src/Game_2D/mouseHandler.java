package Game_2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class mouseHandler extends MouseAdapter {

    gamePanel gp;

    public mouseHandler(gamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        if (gp.gameState == gp.titleState) {
            if (gp.ui.startButtonBounds != null && gp.ui.startButtonBounds.contains(mx, my)) {
                gp.gameState = gp.playState; // vào game
            } else if (gp.ui.guideButtonBounds != null && gp.ui.guideButtonBounds.contains(mx, my)) {
                gp.gameState = gp.guideState; // vào hướng dẫn
            } else if (gp.ui.quitButtonBounds != null && gp.ui.quitButtonBounds.contains(mx, my)) {
                System.exit(0); // thoát game
            }
        } else if (gp.gameState == gp.guideState) {
            gp.gameState = gp.titleState; // click bất kỳ quay lại menu
        }
        gp.repaint();
    }
}
