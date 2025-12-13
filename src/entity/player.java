package entity;

import Game_2D.gamePanel;
import Game_2D.keyHander;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import Game_2D.utiltityTool;

public class player extends entity {

    gamePanel gp;
    keyHander keyH;

    public int screenX;
    public final int screenY;

    // ===== PHYSICS =====
    private float velocityY = 0f;
    private final float gravity = 0.6f;

    private final float jumpLow = -13f;
    private final float jumpHigh = -17f;

    private boolean isGrounded = false;

    // jump control
    private boolean jumpCharging = false;
    private int jumpChargeCounter = 0;
    private final int JUMP_CHARGE_LIMIT = 25; // giữ bao lâu thì nhảy cao

    public player(gamePanel gp, keyHander keyH) {
        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.width / 2 - (gp.tileSize / 2);
        screenY = gp.height * 2 / 4 + gp.tileSize;

        solidArea = new Rectangle();
        solidArea.x = 45;
        solidArea.y = 35;
        solidArea.width = 42;
        solidArea.height = 90;

        setDefaultValue();
        getPlayerImage();
    }

    public void setDefaultValue() {
        worldX = gp.tileSize * 11;
        worldY = gp.tileSize * 11;
        speed = 4;
        direction = "right";
    }

    public void getPlayerImage() {
        right1 = setup("ThiNo-1.png");
        right2 = setup("ThiNo-2.png");
        left1  = setup("ThiNo-3.png");
        left2  = setup("ThiNo-4.png");
    }


    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;

        try {
            image = ImageIO.read(
                    getClass().getResourceAsStream("/res/player/" + imagePath+".png")
            );
            image = uTool.scaleImage(image, 128, 128);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public void update() {

        // ===================== HORIZONTAL =====================
        if (keyH.leftPress || keyH.rightPress) {

            if (keyH.leftPress) direction = "left";
            if (keyH.rightPress) direction = "right";

            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn) {
                if (direction.equals("left")) worldX -= speed;
                if (direction.equals("right")) worldX += speed;
            }

            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1 ? 2 : 1);
                spriteCounter = 0;
            }
        }

        // ===================== JUMP INPUT =====================
        if (isGrounded) {

            if (keyH.jumpPress && !jumpCharging) {
                // bắt đầu giữ
                jumpCharging = true;
                jumpChargeCounter = 0;
            }

            if (jumpCharging && keyH.jumpPress) {
                jumpChargeCounter++;

                // giữ đủ lâu → nhảy cao
                if (jumpChargeCounter >= JUMP_CHARGE_LIMIT) {
                    velocityY = jumpHigh;
                    isGrounded = false;
                    jumpCharging = false;
                }
            }

            // thả sớm → nhảy thấp
            if (jumpCharging && !keyH.jumpPress) {
                velocityY = jumpLow;
                isGrounded = false;
                jumpCharging = false;
            }
        }

        // ===================== GRAVITY =====================
        velocityY += gravity;
        float newY = worldY + velocityY;

        // chỉ check collision khi rơi xuống
        if (velocityY >= 0 && gp.cChecker.checkCollisionY(this, newY)) {
            velocityY = 0;
            isGrounded = true;
        } else {
            worldY = (int) newY;
        }
    }

    public void draw(Graphics2D g2, int screenX, int screenY) {
        BufferedImage image = null;

        switch (direction) {
            case "left":
                image = (spriteNum == 1 ? left1 : left2);
                break;
            case "right":
                image = (spriteNum == 1 ? right1 : right2);
                break;
        }

        if (image != null) {
            g2.drawImage(image, screenX, screenY, 128, 128, null);

            if (gp.keyH.showHitbox) {
                g2.setColor(Color.RED);
                g2.drawRect(
                        screenX + solidArea.x,
                        screenY + solidArea.y,
                        solidArea.width,
                        solidArea.height
                );
            }
        }
    }

    public void draw(Graphics2D g2) {
        draw(g2, worldX, worldY);
    }
}
