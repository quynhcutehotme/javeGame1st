package Game_2D;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UI {

    gamePanel gp;
    Graphics2D g2;
    Font arial_40;

    BufferedImage titleBackground;
    BufferedImage btnStart, btnGuide, btnQuit;

    public int commandNum = 0;

    public UI(gamePanel gp) {
        this.gp = gp;
        arial_40 = new Font("Times New Roman", Font.BOLD, 40);

        try {
            // Load background image
            if (getClass().getResourceAsStream("/res/ui/background.png") != null) {
                titleBackground = ImageIO.read(getClass().getResourceAsStream("/res/ui/background.png"));
            } else {
                System.err.println("!!! MISSING BACKGROUND IMAGE: /res/ui/background.png");
            }

            // Load button images
            if (getClass().getResourceAsStream("/res/ui/start_button.png") != null) {
                btnStart = ImageIO.read(getClass().getResourceAsStream("/res/ui/start_button.png"));
            } else {
                System.err.println("!!! MISSING: /res/ui/start_button.png");
            }

            if (getClass().getResourceAsStream("/res/ui/guide_button.png") != null) {
                btnGuide = ImageIO.read(getClass().getResourceAsStream("/res/ui/guide_button.png"));
            } else {
                System.err.println("!!! MISSING: /res/ui/guide_button.png");
            }

            // FIXED: Load correct quit button image
            if (getClass().getResourceAsStream("/res/ui/quit_button.png") != null) {
                btnQuit = ImageIO.read(getClass().getResourceAsStream("/res/ui/quit_button.png"));
            } else {
                System.err.println("!!! MISSING: /res/ui/quit_button.png - Using start button as fallback");
                btnQuit = btnStart; // Fallback to start button
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
        // Draw background image or black fallback
        if (titleBackground != null) {
            g2.drawImage(titleBackground, 0, 0, gp.width, gp.height, null);
        } else {
            g2.setColor(Color.black);
            g2.fillRect(0, 0, gp.width, gp.height);
        }

        // Draw game title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 80F));
        String text = "BÁT CHÁO HÀNH";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;

        // Draw shadow
        g2.setColor(Color.black);
        g2.drawString(text, x + 6, y + 6);
        // Draw main text
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Draw buttons
        int btnWidth = gp.tileSize * 4;
        int btnHeight = gp.tileSize;
        int btnX = (gp.width / 2) - (btnWidth / 2);
        int btnY = gp.tileSize * 5;

        // START button
        if (btnStart != null) {
            g2.drawImage(btnStart, btnX, btnY, btnWidth, btnHeight, null);
        }
        if (commandNum == 0) {
            g2.setColor(Color.white);
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }

        // GUIDE button
        btnY += gp.tileSize * 1.5;
        if (btnGuide != null) {
            g2.drawImage(btnGuide, btnX, btnY, btnWidth, btnHeight, null);
        }
        if (commandNum == 1) {
            g2.setColor(Color.white);
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }

        // QUIT button
        btnY += gp.tileSize * 1.5;
        if (btnQuit != null) {
            g2.drawImage(btnQuit, btnX, btnY, btnWidth, btnHeight, null);
        }
        if (commandNum == 2) {
            g2.setColor(Color.white);
            g2.drawString(">", btnX - gp.tileSize, btnY + 52);
        }
    }

    public void drawGuideScreen() {
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, gp.width, gp.height);

        g2.setColor(Color.white);
        g2.setFont(arial_40);

        String text = "GUIDE";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(20F));

        String[] lines = {
                "Press W, A, S, D or Arrow Keys to move.",
                "Deliver the porridge to Chi Pheo while avoiding monsters.",
                "Drop 3 bowls and the game ends."
        };

        x = gp.tileSize;
        y += gp.tileSize * 3;

        for (String line : lines) {
            g2.drawString(line, x, y);
            y += gp.tileSize;
        }

        text = "Press ENTER or ESC to return.";
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
    }

    public int getXforCenteredText(String text) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return (gp.width / 2) - (length / 2);
    }
}
