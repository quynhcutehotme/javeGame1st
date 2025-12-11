package Game_2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
        if (titleBackground != null) {
            g2.drawImage(titleBackground, 0, 0, gp.width, gp.height, null);
        } else {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, gp.width, gp.height);
        }


        int btnWidth = gp.tileSize * 4;
        int btnHeight = gp.tileSize *3;
        int btnX = (gp.width / 2) - (btnWidth / 2);
        int btnY = gp.tileSize * 5;

        // Nút START
        if (btnStart != null) g2.drawImage(btnStart, btnX, btnY-100, btnWidth, btnHeight, null);
        startButtonBounds = new Rectangle(btnX+20, btnY-40, btnWidth-40, btnHeight-120);

        // Nút GUIDE
        btnY += gp.tileSize * 1.5;
        if (btnGuide != null) g2.drawImage(btnGuide, btnX-18, btnY-110, btnWidth+40, btnHeight+40, null); // Kích thước và vị trí chưa đồng bộ
        guideButtonBounds = new Rectangle(btnX+27, btnY-32, btnWidth-55, btnHeight-130);

        // Nút QUIT
        btnY += gp.tileSize * 1.5;
        if (btnQuit != null) g2.drawImage(btnQuit, btnX-40, btnY-120, btnWidth+80, btnHeight+55, null);
        quitButtonBounds = new Rectangle(btnX+27, btnY-40, btnWidth-55, btnHeight-130);

    }

    public void drawGuideScreen() {
        if (guideBackground == null) { // chỉ load 1 lần
            try {
                guideBackground = ImageIO.read(getClass().getResourceAsStream("/res/ui/guildBackGr.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (guideBackground != null) {
            g2.drawImage(guideBackground, 0, 0, gp.width, gp.height, null);
        } else {
            // fallback nếu thiếu ảnh
            g2.setColor(Color.black);
            g2.fillRect(0, 0, gp.width, gp.height);
        }
    }

    public int getXforCenteredText(String text) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (gp.width / 2) - (length / 2);
    }
}
