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

  
    private int width;
    private int height;

    public ChiPheo(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = gp.tileSize * 4;   
        this.height = gp.tileSize * 2; 

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
                    
                    walkFrames[i] = createPlaceholderImage();
                } else {
                    System.out.println("[ChiPheo] OK found: " + url);
                    walkFrames[i] = ImageIO.read(url);
                }

                if (walkFrames[i] == null) {
                    System.out.println("[ChiPheo] >>> FAILED TO READ IMAGE: " + path);
                    walkFrames[i] = createPlaceholderImage();
                } else {
                    System.out.println("[ChiPheo] Loaded frame " + i);
                }
            }
        } catch (Exception e) {
            System.out.println("[ChiPheo] Exception in loadFrames:");
            e.printStackTrace();

          
            for (int i = 0; i < 4; i++) {
                walkFrames[i] = createPlaceholderImage();
            }
        }
    }

    private BufferedImage createPlaceholderImage() {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        return img;
    }

    public void update() {
        frameCounter++;
        if (frameCounter > 3) {
            frameIndex = (frameIndex + 1) % walkFrames.length;
            frameCounter = 0;
        }
    }

    public void draw(Graphics2D g2, int screenX, int screenY) {
        if (walkFrames[frameIndex] != null) {
            g2.drawImage(walkFrames[frameIndex],
                    screenX, screenY,
                    width, height, null);

          
            if (gp.keyH != null && gp.keyH.showHitbox) {
                g2.setColor(Color.GREEN);
                g2.drawRect(screenX, screenY, width, height);
            }
        }
    }

  
    public void draw(Graphics2D g2) {
        if (gp.camera != null) {
            int screenX = worldX - gp.camera.worldX;
            int screenY = worldY - gp.camera.worldY;
            draw(g2, screenX, screenY);
        } else {
          
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            draw(g2, screenX, screenY);
        }
    }

   
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
