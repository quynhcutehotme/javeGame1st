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

    // ===== PHYSICS =====
    public float velocityY = 0f;
    public float gravity = 0.6f;

    public final float jumpLow = -13f;
    public final float jumpHigh = -17f;

    public boolean isGrounded = false;
    public boolean isJumping = false;

    // Jump control
    private boolean jumpCharging = false;
    private int jumpChargeCounter = 0;
    private final int JUMP_CHARGE_LIMIT = 5; // giữ bao lâu thì nhảy cao

    // Player stats
    public int maxLife = 3;
    public int life = 3;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60;

    // Stomp mechanic
    public boolean isStomping = false;
    public int stompDamage = 1;
    public float stompBounceStrength = -8f;

    // Movement control
    public boolean canMove = true;

    // Ground level (từ player merged trước)
    private int groundLevel = 0;

    // Hole falling (từ player merged trước)
    private boolean fallingInHole = false;

    // Visual offset for jumping
    public int jumpVisualOffset = 0;

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

        groundLevel = gp.tileSize * 11; // Khởi tạo ground level
    }

    public void setDefaultValue() {
        worldX = gp.tileSize * 11;
        worldY = gp.tileSize * 11;
        groundLevel = gp.tileSize * 11;
        speed = 4;
        direction = "right";

        life = maxLife;
        invincible = false;
        invincibleCounter = 0;

        velocityY = 0f;
        isGrounded = true;
        isJumping = false;
        isStomping = false;
        jumpCharging = false;
        jumpChargeCounter = 0;

        fallingInHole = false;
        canMove = true;
        jumpVisualOffset = 0;
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
            // Thử nhiều cách load ảnh
            InputStream is = getClass().getResourceAsStream("/res/player/" + imagePath + ".png");
            if (is == null) {
                // Thử với .png.png
                is = getClass().getResourceAsStream("/res/player/" + imagePath + ".png.png");
            }
            if (is == null) {
                // Thử từ file system
                File f = new File("src/res/player/" + imagePath + ".png");
                if (!f.exists()) {
                    f = new File("out/res/player/" + imagePath + ".png");
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

    // ===== HOLE FALL API (từ player merged trước) =====
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

    // Stomp area (từ player merged trước)
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
        // >>>>> KEY FIX: nếu đang rơi hố, chỉ gravity rơi xuống
        if (fallingInHole) {
            velocityY += gravity;
            worldY += (int) Math.round(velocityY);

            // vẫn cho X đứng yên
            jumpVisualOffset = (int) (-velocityY * 0.5);
            return;
        }

        // ===================== HORIZONTAL MOVEMENT =====================
        if (canMove && (keyH.leftPress || keyH.rightPress)) {

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
                    isJumping = true;
                    jumpCharging = false;
                    keyH.jumpPress = false; // Reset jump press
                }
            }

            // thả sớm → nhảy thấp
            if (jumpCharging && !keyH.jumpPress) {
                velocityY = jumpLow;
                isGrounded = false;
                isJumping = true;
                jumpCharging = false;
            }
        }

        // ===================== GRAVITY (GIỮ NGUYÊN TỪ PLAYER CÓ GRAVITY CHUẨN) =====================
        velocityY += gravity;
        float newY = worldY + velocityY;

        // chỉ check collision khi rơi xuống
        if (velocityY >= 0 && gp.cChecker.checkCollisionY(this, newY)) {
            velocityY = 0;
            isGrounded = true;
            isJumping = false;
            isStomping = false;
        } else {
            worldY = (int) newY;
            isGrounded = false;
        }

        // ===================== BOUNDS CHECKING =====================
        int minX = 1 * gp.tileSize;
        int minY = 1 * gp.tileSize;
        int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
        int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

        if (worldX < minX) worldX = minX;
        if (worldY < minY) worldY = minY;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;

        // ===================== JUMP VISUAL OFFSET =====================
        if (isJumping) {
            jumpVisualOffset = (int) (-velocityY * 0.5);
        } else {
            jumpVisualOffset = 0;
        }

        // ===================== RESET STOMP FLAG =====================
        if (isStomping) {
            if (velocityY >= 0) {
                isStomping = false;
            }
        }

        // ===================== INVINCIBILITY FRAMES =====================
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter >= INVINCIBLE_DURATION) {
                invincible = false;
                invincibleCounter = 0;
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

    // Draw method với camera support
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

        // Invincibility blink effect
        if (invincible && invincibleCounter % 10 < 5) {
            // Hiển thị trong suốt
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        }

        int drawY = screenY + jumpVisualOffset;

        // Vẽ shadow khi nhảy
        if (!isGrounded) {
            g2.setColor(new Color(0, 0, 0, 70));
            int shadowW = 64;
            int shadowH = 16;
            int shadowX = screenX + (128 - shadowW) / 2;
            int shadowY = screenY + 128 - shadowH / 2 + jumpVisualOffset * 2;
            g2.fillOval(shadowX, shadowY, shadowW, shadowH);
        }

        // Vẽ hiệu ứng stomp
        if (isStomping) {
            g2.setColor(new Color(255, 200, 0, 100));
            g2.fillRect(screenX, drawY, 128, 128);
        }

        // Vẽ player
        if (image != null) {
            g2.drawImage(image, screenX, drawY, 128, 128, null);
        }

        // Reset composite nếu đang blink
        if (invincible && invincibleCounter % 10 < 5) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // Debug hitbox
        if (gp.gameState == gp.playState && (gp.keyH.debugPress || gp.keyH.showHitbox)) {
            g2.setColor(Color.RED);
            g2.drawRect(
                    screenX + solidArea.x,
                    drawY + solidArea.y,
                    solidArea.width,
                    solidArea.height
            );

            // Vẽ stomp area
            Rectangle stompArea = getStompArea();
            int screenStompX = stompArea.x - gp.player.worldX + screenX;
            int screenStompY = stompArea.y - gp.player.worldY + drawY;
            g2.setColor(Color.YELLOW);
            g2.drawRect(screenStompX, screenStompY, stompArea.width, stompArea.height);

            // Vẽ ground level line (debug)
            g2.setColor(Color.GREEN);
            g2.drawLine(screenX - 50, screenY + 128, screenX + 178, screenY + 128);
        }
    }

    // Overload cho backward compatibility
    public void draw(Graphics2D g2) {
        if (gp.camera != null) {
            int screenX = worldX - gp.camera.worldX;
            int screenY = worldY - gp.camera.worldY;
            draw(g2, screenX, screenY);
        } else {
            // Fallback: dùng player position
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            draw(g2, screenX, screenY);
        }
    }

    // ===================== GETTERS & SETTERS =====================
    public int getGroundLevel() {
        return groundLevel;
    }

    public void setGroundLevel(int level) {
        groundLevel = level;
    }

    public boolean isInAir() {
        return !isGrounded;
    }

    public int getScreenX() {
        return screenX;
    }

    public int getScreenY() {
        return screenY + jumpVisualOffset;
    }

    // Thêm method để kiểm tra xem player có đang trên hố không
    public Rectangle getPlayerFeetRect() {
        return new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y + solidArea.height - 6,
                solidArea.width,
                12
        );
    }
}