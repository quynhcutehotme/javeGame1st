package Game_2D;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class keyHander implements KeyListener {

    // 1. KHAI BÁO BIẾN gamePanel ĐỂ KẾT NỐI
    gamePanel gp;

    public boolean upPress, downPress, leftPress, rightPress;
    public boolean restartPress, exitPress;
    public boolean jumpPress;

    // 2. CONSTRUCTOR (Quan trọng: Nhận gp từ Main)
    public keyHander(gamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // --- TRƯỜNG HỢP 1: ĐANG Ở MENU (LOBBY) ---
        if (gp.gameState == gp.titleState) {

            // Dùng W hoặc Mũi tên Lên để chọn lên
            if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 2; // Quay vòng xuống dưới cùng
                }
            }

            // Dùng S hoặc Mũi tên Xuống để chọn xuống
            if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 2) {
                    gp.ui.commandNum = 0; // Quay vòng lên trên cùng
                }
            }

            // Dùng ENTER để chọn
            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) { // Nút START
                    gp.gameState = gp.playState; // Vào game
                }
                if (gp.ui.commandNum == 1) { // Nút GUIDE
                    gp.gameState = gp.guideState; // Vào hướng dẫn
                }
                if (gp.ui.commandNum == 2) { // Nút QUIT
                    System.exit(0); // Thoát game
                }
            }
        }

        // --- TRƯỜNG HỢP 2: ĐANG XEM GUIDE ---
        else if (gp.gameState == gp.guideState) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
                gp.gameState = gp.titleState; // Quay về menu chính
            }
        }

        // --- TRƯỜNG HỢP 3: ĐANG CHƠI GAME (Code cũ của bạn) ---
        else if (gp.gameState == gp.playState) {

            if (code == KeyEvent.VK_W) upPress = true;
            if (code == KeyEvent.VK_A) leftPress = true;
            if (code == KeyEvent.VK_SPACE) jumpPress = true;
            if (code == KeyEvent.VK_S) downPress = true;
            if (code == KeyEvent.VK_D) rightPress = true;
            if (code == KeyEvent.VK_F) gp.queueAttack();

            // Hỗ trợ thêm phím mũi tên
            if (code == KeyEvent.VK_UP) upPress = true;
            if (code == KeyEvent.VK_LEFT) leftPress = true;
            if (code == KeyEvent.VK_DOWN) downPress = true;
            if (code == KeyEvent.VK_RIGHT) rightPress = true;

            // Phím chức năng
            if (code == KeyEvent.VK_R) restartPress = true;
            if (code == KeyEvent.VK_ESCAPE) exitPress = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        // Chỉ cần xử lý nhả phím khi đang chơi
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPress = false;
        }
        if (code == KeyEvent.VK_SPACE) {
            jumpPress = false;
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
        if (code == KeyEvent.VK_R) {
            restartPress = false;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            exitPress = false;
        }
    }
}