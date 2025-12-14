package entity;

import Game_2D.gamePanel;
import Game_2D.keyHander;
import Game_2D.utiltityTool;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class player extends entity {

    gamePanel gp;
    keyHander keyH;

    public int screenX;
    public final int screenY;

    public int maxLife;
    public int life;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60;

    // Jump/Gravity state
    public float velocityY = 0f;
    public float gravity = 0.6f;
    public float jumpStrength = -13f;
    public boolean isGrounded = false;
    public boolean isJumping = false;
    public int jumpVisualOffset = 0;
    public int camX;
    public int lastCamX;

    // Stomp
    public boolean isStomping = false;
    public int stompDamage = 1;
    public float stompBounceStrength = -8f;

    public boolean canMove = true;
    public int rotationAngle = 0;

    // Ground
    private int groundLevel = 0;

    // >>>>> KEY FIX: trạng thái rơi xuống hố
    private boolean fallingInHole = false;

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

        groundLevel = gp.tileSize * 10;
    }

    public void setDefaultValue() {
        worldX = gp.tileSize * 10;
        groundLevel = gp.tileSize * 10;
        worldY = groundLevel;
        speed = 4;
        direction = "right";

        maxLife = 3;
        life = maxLife;
        invincible = false;

        velocityY = 0f;
        isGrounded = true;
        isJumping = false;
        isStomping = false;

        fallingInHole = false;
        canMove = true;
    }

    public void getPlayerImage() {
        right1 = setup("ThiNo-1");
        right2 = setup("ThiNo-2");
        left1 = setup("ThiNo-3");
        left2 = setup("ThiNo-4");
    }

    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;

        try {
            InputStream is = getClass().getResourceAsStream("/res/player/" + imagePath + ".png.png");
            if (is == null) {
                is = getClass().getResourceAsStream("/res/player/" + imagePath + ".png");
            }
            if (is == null) {
                File f = new File("src/res/player/" + imagePath + ".png.png");
                if (!f.exists()) {
                    f = new File("src/res/player/" + imagePath + ".png");
                }
                if (f.exists()) {
                    is = new FileInputStream(f);
                }
            }
            if (is != null) {
                image = ImageIO.read(is);
                image = uTool.scaleImage(image, 128, 128);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // ===== HOLE FALL API (gamePanel gọi) =====
    public void beginHoleFall() {
        fallingInHole = true;
        canMove = false;      // khóa điều khiển khi rơi
        isGrounded = false;
        isJumping = true;

        // nếu đang bay lên mà rơi hố thì reset cho rơi xuống luôn
        if (velocityY < 0) velocityY = 0f;
    }

    public void resetHoleFall() {
        fallingInHole = false;
        canMove = true;
    }

    public boolean isFallingInHole() {
        return fallingInHole;
    }

    // Stomp area
    public Rectangle getStompArea() {
        return new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y + solidArea.height - 10,
                solidArea.width,
                15
        );
    }

    public boolean isFalling() {
        return isJumping && velocityY > 0;
    }

    public boolean isRising() {
        return isJumping && velocityY < 0;
    }

    public boolean checkStompOnBot(Rectangle botHeadArea) {
        Rectangle stompArea = getStompArea();
        return stompArea.intersects(botHeadArea);
    }

    public void bounceFromStomp() {
        velocityY = stompBounceStrength;
        isGrounded = false;
        isStomping = true;
        isJumping = true;
    }

    public void update() {
        // >>>>> KEY FIX: nếu đang rơi hố, chỉ gravity rơi xuống (không snap ground, không clamp Y)
        if (fallingInHole) {
            velocityY += gravity;
            worldY += (int) Math.round(velocityY);

            // vẫn cho X đứng yên (hoặc bạn muốn trượt cũng được)
            jumpVisualOffset = (int) (-velocityY * 0.5);
            return;
        }

        // 1) Horizontal movement
        if (canMove && (keyH.leftPress || keyH.rightPress)) {
            if (keyH.leftPress) direction = "left";
            else if (keyH.rightPress) direction = "right";

            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn) {
                if (keyH.leftPress) worldX -= speed;
                else if (keyH.rightPress) worldX += speed;
            }
        }

        // 2) Jump
        if (canMove && keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
            isJumping = true;
            keyH.jumpPress = false;
        }

        // 3) Gravity
        if (!isGrounded) {
            velocityY += gravity;
            worldY += (int) Math.round(velocityY);

            if (worldY >= groundLevel) {
                worldY = groundLevel;
                velocityY = 0f;
                isGrounded = true;
                isJumping = false;
                isStomping = false;
            }
        }

        // 4) Clamp world bounds (BỎ clamp Y khi rơi hố đã return phía trên)
        int minX = 1 * gp.tileSize;
        int minY = 1 * gp.tileSize;
        int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
        int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

        if (worldX < minX) worldX = minX;
        if (worldY < minY) worldY = minY;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;

        // 5) Jump visual offset
        if (isJumping) {
            jumpVisualOffset = (int) (-velocityY * 0.5);
        } else {
            jumpVisualOffset = 0;
        }

        // 6) Reset stomp flag
        if (isStomping) {
            if (velocityY >= 0) {
                isStomping = false;
            }
        }
    }

    public void takeDamage(entity attacker) {
        if (!invincible) {
            life -= 1;
            invincible = true;
            invincibleCounter = 0;

            if (attacker != null) {
                int knockbackDirection = (attacker.worldX < worldX) ? 1 : -1;
                worldX += knockbackDirection * speed * 5;
            }

            if (life <= 0) {
                gp.triggerGameOver();
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image;

        switch (direction) {
            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;
            default:
                image = right1;
        }

        if (invincible && invincibleCounter % 10 < 5) {
            return;
        }

        int drawY = screenY + jumpVisualOffset;

        if (isStomping) {
            g2.setColor(new Color(255, 200, 0, 100));
            g2.fillRect(screenX, drawY, 128, 128);
        }

        g2.drawImage(image, screenX, drawY, 128, 128, null);

        if (gp.gameState == gp.playState && gp.keyH.debugPress) {
            g2.setColor(Color.RED);
            Rectangle stompArea = getStompArea();
            int screenStompX = stompArea.x - gp.player.worldX + gp.player.screenX;
            int screenStompY = stompArea.y - gp.player.worldY + gp.player.screenY;
            g2.drawRect(screenStompX, screenStompY, stompArea.width, stompArea.height);
        }
    }

    public int getGroundLevel() {
        return groundLevel;
    }

    public void setGroundLevel(int level) {
        groundLevel = level;
    }

    public boolean isInAir() {
        return !isGrounded;
    }
}
