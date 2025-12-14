package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyHander implements KeyListener {

    gamePanel gp;

    // Movement keys
    public boolean upPress, downPress, leftPress, rightPress;
    
    // Jump key
    public boolean jumpPress;
    public boolean jumpHeld = false; // Track if jump is being held
    
    // Action keys
    public boolean attackPress;
    
    // Menu/System keys
    public boolean restartPress, exitPress;
    public boolean enterPress;
    
    // Debug key (optional)
    public boolean debugPress = false;

    public keyHander(gamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // --- TITLE STATE (MENU) ---
        if (gp.getCurrentGameState() == gp.titleState) {
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 2;
                }
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 2) {
                    gp.ui.commandNum = 0;
                }
            }
            if (code == KeyEvent.VK_ENTER) {
                enterPress = true;
                if (gp.ui.commandNum == 0) {
                    gp.setGameState(gp.playState);
                }
                if (gp.ui.commandNum == 1) {
                    gp.setGameState(gp.guideState);
                }
                if (gp.ui.commandNum == 2) {
                    System.exit(0);
                }
            }
            // Allow ESC to exit from title screen too
            if (code == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }

        // --- GUIDE STATE ---
        else if (gp.getCurrentGameState() == gp.guideState) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_SPACE) {
                gp.setGameState(gp.titleState);
                gp.ui.commandNum = 0; // Reset to first option
            }
        }

        // --- PLAY STATE ---
        else if (gp.getCurrentGameState() == gp.playState) {
            // MOVEMENT KEYS
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPress = true;
            }
            if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPress = true;
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPress = true;
            }
            if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPress = true;
            }
            
            // JUMP KEYS - Multiple jump controls
            if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                if (!jumpHeld) { // Only register jump once per press
                    jumpPress = true;
                    jumpHeld = true;
                }
            }
            
            // ATTACK KEY (Keep for backward compatibility or remove if not needed)
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_J) {
                attackPress = true;
                gp.queueAttack();
            }
            
            // MOUSE CLICK ALSO TRIGGERS ATTACK (if you want to keep both)
            // Attack on mouse is handled separately in MouseListener
            
            // SYSTEM KEYS
            if (code == KeyEvent.VK_R) {
                restartPress = true;
                gp.performRestart();
            }
            if (code == KeyEvent.VK_ESCAPE) {
                exitPress = true;
                System.exit(0);
            }
            if (code == KeyEvent.VK_ENTER) {
                enterPress = true;
            }
            
            // DEBUG KEY (optional)
            if (code == KeyEvent.VK_F3) {
                debugPress = !debugPress;
            }
            
            // QUICK RESTART (for testing)
            if (code == KeyEvent.VK_F5) {
                gp.performRestart();
            }
        }
        
        // --- HANDLE GAME OVER/WIN STATES THROUGH MENU VISIBILITY ---
        // Kiß╗âm tra xem c├│ menu n├áo ─æang hiß╗ân thß╗ï kh├┤ng
        else if (gp.isGameOverMenuVisible() || gp.isWinMenuVisible()) {
            if (code == KeyEvent.VK_R) {
                restartPress = true;
                gp.performRestart();
            }
            if (code == KeyEvent.VK_ESCAPE) {
                exitPress = true;
                System.exit(0);
            }
            if (code == KeyEvent.VK_ENTER) {
                enterPress = true;
                // Could go back to main menu
                gp.setGameState(gp.titleState);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // MOVEMENT KEYS
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPress = false;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPress = false;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPress = false;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPress = false;
        }
        
        // JUMP KEYS
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            jumpPress = false;
            jumpHeld = false; // Reset held state
        }
        
        // ATTACK KEYS
        if (code == KeyEvent.VK_F || code == KeyEvent.VK_J) {
            attackPress = false;
        }
        
        // SYSTEM KEYS
        if (code == KeyEvent.VK_R) {
            restartPress = false;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            exitPress = false;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPress = false;
        }
    }
    
    // Helper method to reset all keys (useful when changing game states)
    public void resetAllKeys() {
        upPress = false;
        downPress = false;
        leftPress = false;
        rightPress = false;
        jumpPress = false;
        jumpHeld = false;
        attackPress = false;
        restartPress = false;
        exitPress = false;
        enterPress = false;
    }
    
    // Check if any movement key is pressed
    public boolean isAnyMovementKeyPressed() {
        return upPress || downPress || leftPress || rightPress;
    }
    
    // Check if player is trying to move horizontally
    public boolean isMovingHorizontally() {
        return leftPress || rightPress;
    }
    
    // Check if player is trying to move vertically (not including jump)
    public boolean isMovingVertically() {
        return upPress || downPress;
    }
}
