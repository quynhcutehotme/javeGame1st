package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyHander implements KeyListener {

    gamePanel gp;

    // Movement keys
    public boolean upPress, downPress, leftPress, rightPress;

    // Jump keys
    public boolean jumpPress;
    public boolean jumpHeld = false; // Track if jump is being held

    // Action keys
    public boolean attackPress;

    // Menu/System keys
    public boolean restartPress, exitPress;
    public boolean enterPress;

    // Debug key (optional)
    public boolean showHitbox = false; // Từ gamePanel đầu tiên
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
            // Dùng W/S hoặc Mũi tên Lên/Xuống để chọn
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 2; // Quay vòng xuống dưới cùng
                }
            }
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 2) {
                    gp.ui.commandNum = 0; // Quay vòng lên trên cùng
                }
            }

            // Dùng ENTER để chọn
            if (code == KeyEvent.VK_ENTER) {
                enterPress = true;
                if (gp.ui.commandNum == 0) { // Nút START
                    gp.setGameState(gp.playState); // Vào game
                }
                if (gp.ui.commandNum == 1) { // Nút GUIDE
                    gp.setGameState(gp.guideState); // Vào hướng dẫn
                }
                if (gp.ui.commandNum == 2) { // Nút QUIT
                    System.exit(0); // Thoát game
                }
            }

            // Cho phép ESC thoát từ màn hình title
            if (code == KeyEvent.VK_ESCAPE) {
                System.exit(0);
            }
        }

        // --- GUIDE STATE ---
        else if (gp.getCurrentGameState() == gp.guideState) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_SPACE) {
                gp.setGameState(gp.titleState);
                gp.ui.commandNum = 0; // Reset về lựa chọn đầu tiên
            }
        }

        // --- PLAY STATE ---
        else if (gp.getCurrentGameState() == gp.playState) {
            // MOVEMENT KEYS - Hỗ trợ cả WASD và mũi tên
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
            if (code == KeyEvent.VK_SPACE) {
                if (!jumpHeld) { // Chỉ đăng ký nhảy một lần mỗi lần nhấn
                    jumpPress = true;
                    jumpHeld = true;
                }
            }

            // ATTACK KEYS - Cả bàn phím và chuột đều có thể tấn công
            if (code == KeyEvent.VK_F || code == KeyEvent.VK_J || code == KeyEvent.VK_CONTROL) {
                attackPress = true;
                gp.queueAttack();
            }

            // SYSTEM KEYS
            if (code == KeyEvent.VK_R) {
                restartPress = true;
                // Gọi restart ngay lập tức để phản hồi nhanh
                gp.performRestart();
            }
            if (code == KeyEvent.VK_ESCAPE) {
                exitPress = true;
                System.exit(0);
            }
            if (code == KeyEvent.VK_ENTER) {
                enterPress = true;
            }

            // DEBUG KEYS (tùy chọn)
            if (code == KeyEvent.VK_F3) {
                debugPress = !debugPress;
                showHitbox = debugPress; // Đồng bộ showHitbox với debug mode
            }

            // QUICK RESTART (for testing)
            if (code == KeyEvent.VK_F5) {
                gp.performRestart();
            }
        }

        // --- GAME OVER / WIN STATES ---
        // Kiểm tra xem có menu nào đang hiển thị không
        if (gp.isGameOverMenuVisible() || gp.isWinMenuVisible()) {
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
                // Có thể quay về main menu
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
        if (code == KeyEvent.VK_SPACE) {
            jumpPress = false;
            jumpHeld = false; // Reset trạng thái giữ phím
        }

        // ATTACK KEYS
        if (code == KeyEvent.VK_F || code == KeyEvent.VK_J || code == KeyEvent.VK_CONTROL) {
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

        // DEBUG KEY
        if (code == KeyEvent.VK_F3) {
            // Không cần làm gì ở đây vì F3 là toggle
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

    // Check if player is trying to jump
    public boolean isJumping() {
        return jumpPress;
    }
}