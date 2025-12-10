package entity;

import Game_2D.gamePanel;
import Game_2D.keyHander;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import Game_2D.utiltityTool;

public class player extends entity {

    gamePanel gp;
    keyHander keyH;

    public int screenX;
    public final int screenY;
    
    // Jump/Gravity state
    private float velocityY = 0f;
    private float gravity = 0.6f;
    private float jumpStrength = -10f;
    private boolean isGrounded = true;
    private int jumpVisualOffset = 0;

    public player(gamePanel gp, keyHander keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.width / 2 - (gp.tileSize / 2);
        screenY = gp.height * 2 / 4 + (gp.tileSize);

        solidArea = new Rectangle();
        solidArea.x = 45;
        solidArea.y = 35;
        solidArea.width = 42;
        solidArea.height = 90;

        setDefaultValue();
        getPlayerImage();
    }

    public void setDefaultValue() {
        worldX = gp.tileSize * 10;
        worldY = gp.tileSize * 11;
        speed = 4;
        direction = "right";
    }

    public void getPlayerImage() {
        right1 = setup("ThiNo-1");
        right2 = setup("ThiNo-2");
        left1 = setup("ThiNo-3");
        left2 = setup("ThiNo-4");
        
        // Try to load up/down sprites, but don't fail if they're missing
        up1 = setup("up1");
        up2 = setup("up2");
        down1 = setup("down1");
        down2 = setup("down2");
    }

    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;

        try {
            InputStream is = getClass().getResourceAsStream("/res/player/" + imagePath + ".png");
            if (is == null) {
                System.err.println("WARNING: Cannot find /res/player/" + imagePath + ".png");
                return null;
            }
            image = ImageIO.read(is);
            image = uTool.scaleImage(image, 128, 128);
        } catch (IOException e) {
            System.err.println("ERROR loading image: " + imagePath);
            e.printStackTrace();
        }
        return image;
    }

    public void update() {
        boolean moving = false;
        String newDirection = direction;

        // FIXED: Determine direction based on key combinations (including diagonals)
        if (keyH.upPress && keyH.leftPress) {
            newDirection = "up-left";
            moving = true;
        } else if (keyH.upPress && keyH.rightPress) {
            newDirection = "up-right";
            moving = true;
        } else if (keyH.downPress && keyH.leftPress) {
            newDirection = "down-left";
            moving = true;
        } else if (keyH.downPress && keyH.rightPress) {
            newDirection = "down-right";
            moving = true;
        } else if (keyH.upPress) {
            newDirection = "up";
            moving = true;
        } else if (keyH.downPress) {
            newDirection = "down";
            moving = true;
        } else if (keyH.leftPress) {
            newDirection = "left";
            moving = true;
        } else if (keyH.rightPress) {
            newDirection = "right";
            moving = true;
        }

        if (moving) {
            direction = newDirection;
            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn) {
                // FIXED: Handle all 8 directions including diagonals
                switch (direction) {
                    case "up":
                        worldY -= speed;
                        break;
                    case "down":
                        worldY += speed;
                        break;
                    case "left":
                        worldX -= speed;
                        break;
                    case "right":
                        worldX += speed;
                        break;
                    case "up-left":
                        worldY -= speed;
                        worldX -= speed;
                        break;
                    case "up-right":
                        worldY -= speed;
                        worldX += speed;
                        break;
                    case "down-left":
                        worldY += speed;
                        worldX -= speed;
                        break;
                    case "down-right":
                        worldY += speed;
                        worldX += speed;
                        break;
                }

                // FIXED: Prevent reaching the world border
                int minX = 1 * gp.tileSize;
                int minY = 1 * gp.tileSize;
                int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
                int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

                worldX = Math.max(minX, Math.min(worldX, maxX));
                worldY = Math.max(minY, Math.min(worldY, maxY));
            }

            // Animation
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }

        // Jump logic
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // Apply gravity when not grounded
        if (!isGrounded) {
            velocityY += gravity;
            jumpVisualOffset += (int) Math.round(velocityY);

            if (jumpVisualOffset > 0) {
                jumpVisualOffset = 0;
                velocityY = 0f;
                isGrounded = true;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        // FIXED: Handle all directions with proper fallbacks
        switch (direction) {
            case "down":
                image = (spriteNum == 1) ? down1 : down2;
                if (image == null) image = (spriteNum == 1) ? left1 : left2; // Fallback
                break;
            case "down-left":
                image = (spriteNum == 1) ? down1 : down2;
                if (image == null) image = (spriteNum == 1) ? left1 : left2;
                break;
            case "down-right":
                image = (spriteNum == 1) ? down1 : down2;
                if (image == null) image = (spriteNum == 1) ? right1 : right2;
                break;
            case "up":
                image = (spriteNum == 1) ? up1 : up2;
                if (image == null) image = (spriteNum == 1) ? left1 : left2; // Fallback
                break;
            case "up-left":
                image = (spriteNum == 1) ? up1 : up2;
                if (image == null) image = (spriteNum == 1) ? left1 : left2;
                break;
            case "up-right":
                image = (spriteNum == 1) ? up1 : up2;
                if (image == null) image = (spriteNum == 1) ? right1 : right2;
                break;
            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;
            default:
                image = right1; // Default fallback
        }

        // FIXED: Null check before drawing
        if (image != null) {
            g2.drawImage(image, screenX, screenY + jumpVisualOffset, 128, 128, null);
        } else {
            // Emergency fallback: draw a colored rectangle
            g2.setColor(Color.BLUE);
            g2.fillRect(screenX, screenY + jumpVisualOffset, 128, 128);
        }

        g2.setColor(Color.white);
    }
}
