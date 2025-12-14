package Game_2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class UI {

    gamePanel gp;
    Graphics2D g2;




    public Rectangle startButtonBounds;
    public Rectangle guideButtonBounds;
    public Rectangle quitButtonBounds;

    BufferedImage titleBackground;
    BufferedImage btnStart, btnGuide, btnQuit;
    BufferedImage guideBackground;

    public int commandNum = 0;

    public UI(gamePanel gp) {
        this.gp = gp;


        try {

            if (getClass().getResourceAsStream("/res/ui/background.png") != null) {
                titleBackground = ImageIO.read(getClass().getResourceAsStream("/res/ui/background.png"));
            } else {
                System.err.println("!!! THIẾU ẢNH NỀN: /res/ui/background.png");
            }

            // Load các nút bấm (Code cũ)
            if (getClass().getResourceAsStream("/res/ui/start_button.png") != null)
                btnStart = ImageIO.read(getClass().getResourceAsStream("/res/ui/start_button.png"));

            if (getClass().getResourceAsStream("/res/ui/guide_button.png") != null)
                btnGuide = ImageIO.read(getClass().getResourceAsStream("/res/ui/guide_button.png"));

            if (getClass().getResourceAsStream("/res/ui/3.png") != null) {
                btnQuit = ImageIO.read(getClass().getResourceAsStream("/res/ui/3.png"));
            } else {
                btnQuit = btnStart; // Dùng tạm nếu thiếu
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;
        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }
        if (gp.gameState == gp.guideState) {
            drawGuideScreen();
        }
    }

    public void drawTitleScreen() {
        // --- 3. VẼ ẢNH NỀN (Thay thế cho màu đen) ---
        if (titleBackground != null) {
            // Nếu có ảnh thì vẽ ảnh
            g2.drawImage(titleBackground, 0, 0, gp.width, gp.height, null);
        } else {
            // Nếu không có ảnh (hoặc quên bỏ vào) thì vẽ màu đen chống cháy
            g2.setColor(Color.black);
            g2.fillRect(0, 0, gp.width, gp.height);
        }

        // Vẽ Tên Game
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
        String text = "BÁT CHÁO HÀNH";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;

        // Vẽ bóng chữ (Màu đen cho dễ đọc trên nền ảnh)
        g2.setColor(Color.black);
        g2.drawString(text, x + 6, y + 6);
        // Vẽ chữ chính (Màu trắng)
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Vẽ Nút Bấm
        int btnWidth = gp.tileSize * 4;
        int btnHeight = gp.tileSize;
        int btnX = (gp.width / 2) - (btnWidth / 2);
        int btnY = gp.tileSize * 5;

        // Nút START
        if (btnStart != null) g2.drawImage(btnStart, btnX, btnY, btnWidth, btnHeight, null);
        if (commandNum == 0) {
            g2.setColor(Color.white); // Mũi tên màu trắng
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }

        // Nút GUIDE
        btnY += gp.tileSize * 1.5;
        if (btnGuide != null) g2.drawImage(btnGuide, btnX, btnY, btnWidth, btnHeight, null);
        if (commandNum == 1) {
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }

        // Nút QUIT
        btnY += gp.tileSize * 1.5;
        if (btnQuit != null) g2.drawImage(btnQuit, btnX, btnY, btnWidth, btnHeight, null);
        if (commandNum == 2) {
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }
    }

    public void drawGuideScreen() {
        // Màn hình hướng dẫn cũng có thể dùng nền ảnh nếu thích
        // Ở đây mình để nền đen mờ cho dễ đọc chữ
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, gp.width, gp.height);

        g2.setColor(Color.white);
        Font arial_40 = null;
        g2.setFont(arial_40);

        String text = "GUIDE";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(20F));

        String[] lines = {
                "Press W, A, S, D or Arrow Keys to move.",
                "Jump on monsters to defeat them.",
                "Avoid monsters and find Chi Pheo.",
                "Touch Chi Pheo to win the game.",
                "Drop 3 bowls and the game ends."
        };

        x = gp.tileSize;
        y += gp.tileSize * 3;

        for (String line : lines) {
            g2.drawString(line, x, y);
            y += gp.tileSize; // khoảng cách giữa các dòng
        }

        text = "Nhấn ENTER hoặc ESC để quay lại.";
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
    }

    public int getXforCenteredText(String text) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (gp.width / 2) - (length / 2);
    }
}
