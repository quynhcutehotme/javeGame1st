package entity;

import Game_2D.gamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class House {

    gamePanel gp;

    public int worldX, worldY;
    public int width, height;

    BufferedImage image;

    public House(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;

        this.width = gp.tileSize * 6;
        this.height = gp.tileSize * 6;

        loadImage();
    }

    private void loadImage() {
        try {
            image = ImageIO.read(
                    getClass().getResourceAsStream("/res/ui/house.png")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {

        int screenX = gp.getScreenX(worldX);
        int screenY = gp.getScreenY(worldY);


        if (worldX + width > gp.player.worldX - gp.player.screenX &&
                worldX - width < gp.player.worldX + gp.player.screenX &&
                worldY + height > gp.player.worldY - gp.player.screenY &&
                worldY - height < gp.player.worldY + gp.player.screenY) {

            g2.drawImage(image, screenX, screenY, width, height, null);
        }
    }}


