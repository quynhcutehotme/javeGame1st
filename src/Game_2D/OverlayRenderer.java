package Game_2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class OverlayRenderer {

    private final gamePanel gp;

    public OverlayRenderer(gamePanel gp) {
        this.gp = gp;
    }

    public void drawPlayerLife(Graphics2D g2) {
        if (gp.heartIcon != null) {
            int x = 10;
            int y = 10;
            int i = 0;
            while (i < gp.playerHp) {
                g2.drawImage(gp.heartIcon, x, y, null);
                x += gp.tileSize;
                i++;
            }
        } else {
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
            g2.drawString("HP: " + gp.playerHp, 60, 144);
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.width, gp.height);

        int menuWidth = 400;
        int menuHeight = 250;
        int menuX = (gp.width - menuWidth) / 2;
        int menuY = (gp.height - menuHeight) / 2;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        g2.setColor(Color.RED);
        String loseText = "YOU DIED";
        int textWidth = g2.getFontMetrics().stringWidth(loseText);
        g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 100);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18f));
        g2.setColor(Color.BLACK);
        String deathText = "You can not find your Chi Pheo 💔";
        int deathWidth = g2.getFontMetrics().stringWidth(deathText);
        g2.drawString(deathText, menuX + (menuWidth - deathWidth) / 2, menuY + 140);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 170);
    }

    public void drawWinScreen(Graphics2D g2) {
        long winTimeElapsed = System.currentTimeMillis() - gp.winStartTime;

        if (winTimeElapsed < 3000) {
            drawWinBoard(g2, winTimeElapsed);
            return;
        }

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.width, gp.height);

        int menuWidth = 400;
        int menuHeight = 300;
        int menuX = (gp.width - menuWidth) / 2;
        int menuY = (gp.height - menuHeight) / 2;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        g2.setColor(new Color(0, 150, 0));
        String winText = "YOU WIN!";
        int textWidth = g2.getFontMetrics().stringWidth(winText);
        g2.drawString(winText, menuX + (menuWidth - textWidth) / 2, menuY + 100);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24f));
        g2.setColor(Color.BLACK);
        String timeText = "Survival Time: " + gp.currentTimeElapsed + "s";
        int timeWidth = g2.getFontMetrics().stringWidth(timeText);
        g2.drawString(timeText, menuX + (menuWidth - timeWidth) / 2, menuY + 150);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 200);
    }

    private void drawWinBoard(Graphics2D g2, long elapsedTime) {
        try {
            BufferedImage winBoardImage = ImageIO.read(gp.getClass().getResourceAsStream("/res/ui/winboard.png"));

            int imgWidth = winBoardImage.getWidth();
            int imgHeight = winBoardImage.getHeight();

            int targetWidth = Math.min(gp.width - 100, imgWidth);
            int targetHeight = (int) (targetWidth * ((float) imgHeight / imgWidth));

            if (targetHeight > gp.height - 100) {
                targetHeight = gp.height - 100;
                targetWidth = (int) (targetHeight * ((float) imgWidth / imgHeight));
            }

            int x = (gp.width - targetWidth) / 2;
            int y = (gp.height - targetHeight) / 2;

            float alpha = 1.0f;
            if (elapsedTime < 500) {
                alpha = elapsedTime / 500.0f;
            }

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g2.setColor(new Color(0, 0, 0, (int) (150 * alpha)));
            g2.fillRect(0, 0, gp.width, gp.height);

            g2.drawImage(winBoardImage, x, y, targetWidth, targetHeight, null);

            g2.setComposite(originalComposite);
        } catch (Exception e) {
            e.printStackTrace();
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, gp.width, gp.height);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
            g2.setColor(Color.GREEN);
            g2.drawString("VICTORY!", gp.width / 2 - 150, gp.height / 2);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30f));
            g2.setColor(Color.WHITE);
            g2.drawString("Loading win screen...", gp.width / 2 - 120, gp.height / 2 + 60);
        }
    }

    public void drawClouds(Graphics2D g2) {
        int camX = gp.camera.worldX;
        int camY = gp.camera.worldY;
        float parallax = 0.25f;

        int[][] clouds = new int[][]{
                {50, 200, 120, 90},
                {380, 220, 130, 100},
                {720, 240, 160, 140},
                {1040, 210, 180, 110}
        };

        int wrapW = gp.width + 200;
        int wrapH = gp.height + 200;

        for (int[] c : clouds) {
            int baseX = c[0];
            int baseY = c[1];
            int w = c[2];
            int h = c[3];

            int drawX = baseX - (int) (camX * parallax);
            int drawY = baseY - (int) (camY * parallax);

            drawX = ((drawX % wrapW) + wrapW) % wrapW - 100;
            drawY = ((drawY % wrapH) + wrapH) % wrapH - 100;

            g2.drawImage(gp.cloudImage, drawX, drawY, w, h, gp);
        }
    }
}
