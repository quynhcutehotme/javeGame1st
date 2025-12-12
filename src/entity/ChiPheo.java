package entity;

import Game_2D.gamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ChiPheo {

    gamePanel gp;

    public int worldX, worldY;

    BufferedImage[] walkFrames;
    int frameIndex = 0;
    int frameCounter = 0;

    public ChiPheo(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;

        loadFrames();
    }

    private void loadFrames() {
        try {
            walkFrames = new BufferedImage[4];
            for (int i = 0; i < 4; i++) {
                String path = "/res/chipheo/chipheo" + (i + 1) + ".png";
                System.out.println("[ChiPheo] Try load frame: " + path);

                java.net.URL url = getClass().getResource(path);
                if (url == null) {
                    System.out.println("[ChiPheo] >>> NOT FOUND: " + path);
                    continue;
                } else {
                    System.out.println("[ChiPheo] OK found: " + url);
                }

                walkFrames[i] = ImageIO.read(url);
                if (walkFrames[i] == null) {
                    System.out.println("[ChiPheo] >>> FAILED TO READ IMAGE: " + path);
                } else {
                    System.out.println("[ChiPheo] Loaded frame " + i);
                }
            }
        } catch (Exception e) {
            System.out.println("[ChiPheo] Exception in loadFrames:");
            e.printStackTrace();
        }
    }


    public void update() {
        frameCounter++;
        if (frameCounter > 3) {
            frameIndex = (frameIndex + 1) % walkFrames.length;
            frameCounter = 0;
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        g2.drawImage(walkFrames[frameIndex],
                screenX, screenY,
                gp.tileSize * 4, gp.tileSize * 2, null);
    }
}
