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
    
    // Thuộc tính HP và Miễn nhiễm
    public int maxLife;
    public int life;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60; // 1 second

    // Jump/Gravity state - SỬA THÀNH PUBLIC
    public float velocityY = 0f; // ĐỔI TỪ private THÀNH public
    public float gravity = 0.6f;
    public float jumpStrength = -13f; // Tăng lực nhảy
    public boolean isGrounded = false;
    public boolean isJumping = false;
    public int jumpVisualOffset = 0;
    public int camX;
    public int lastCamX;

    // Thêm cho stomp mechanism
    public boolean isStomping = false;
    public int stompDamage = 1;
    public float stompBounceStrength = -8f;

    // Ground check variables
    private int groundLevel = 0; // Mức đất

    public player(gamePanel gp, keyHander keyH) {
        this.gp = gp;
        this.keyH = keyH;

        // vị trí nhân vật trong màn hình
        screenX = gp.width / 2 - (gp.tileSize / 2);
        screenY = gp.height * 2 / 4 + (gp.tileSize);

        // vị trí collusion trên player
        solidArea = new Rectangle();
        solidArea.x = 45;
        solidArea.y = 35;
        solidArea.width = 42;
        solidArea.height = 90;

        setDefaultValue();
        getPlayerImage();
        
        // Xác định ground level
        groundLevel = gp.tileSize * 11;
    }

    // xuất hiện trên máp
    public void setDefaultValue() {
        worldX = gp.tileSize * 10;
        worldY = gp.tileSize * 11;
        speed = 4;
        direction = "right";
        
        maxLife = 3;
        life = maxLife;
        invincible = false;
        
        // Reset jump state
        velocityY = 0f;
        isGrounded = true; // Bắt đầu trên đất
        isJumping = false;
        isStomping = false;
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

    // Phương thức mới: Kiểm tra đạp bot (được gọi từ gamePanel)
    public Rectangle getStompArea() {
        return new Rectangle(
            worldX + solidArea.x,
            worldY + solidArea.y + solidArea.height - 10,
            solidArea.width,
            15
        );
    }
    
    // Getter cho kiểm tra đang rơi
    public boolean isFalling() {
        return isJumping && velocityY > 0;
    }
    
    // Getter cho kiểm tra đang nhảy lên
    public boolean isRising() {
        return isJumping && velocityY < 0;
    }
    
    // Phương thức để gamePanel kiểm tra stomp
    public boolean checkStompOnBot(Rectangle botHeadArea) {
        Rectangle stompArea = getStompArea();
        return stompArea.intersects(botHeadArea);
    }
    
    // Phương thức để bật lên khi đạp trúng bot
    public void bounceFromStomp() {
        velocityY = stompBounceStrength;
        isGrounded = false;
        isStomping = true;
        isJumping = true;
    }

    public void update() {
        // 1. Horizontal movement
        boolean moving = false;
        
        if (keyH.leftPress || keyH.rightPress) {
            moving = true;
            if (keyH.leftPress) {
                direction = "left";
            } else if (keyH.rightPress) {
                direction = "right";
            }
            
            collisionOn = false;
            gp.cChecker.checkTile(this);
            
            if (!collisionOn) {
                if (keyH.leftPress) {
                    worldX -= speed;
                } else if (keyH.rightPress) {
                    worldX += speed;
                }
            }
        }
        
        // 2. Jump/Gravity system
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
            isJumping = true;
            keyH.jumpPress = false;
        }
        
        // Áp dụng gravity khi không đứng trên đất
        if (!isGrounded) {
            velocityY += gravity;
            worldY += (int) Math.round(velocityY);
            
            // Kiểm tra chạm đất
            if (worldY >= groundLevel) {
                worldY = groundLevel;
                velocityY = 0f;
                isGrounded = true;
                isJumping = false;
                isStomping = false;
            }
        }
        
        // 3. Kiểm tra nếu đứng trên đất nhưng không có đất thật sự
        if (isGrounded) {
            // Kiểm tra dưới chân
            int checkY = worldY + 5;
            int tempWorldY = worldY;
            worldY = checkY;
            collisionOn = false;
            gp.cChecker.checkTile(this);
            worldY = tempWorldY;
            
            if (!collisionOn) {
                // Không có đất, bắt đầu rơi
                isGrounded = false;
                isJumping = true;
            }
        }
        
        // 4. Animation
        if (moving) {
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }
        
        // 5. Invincibility frames
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > INVINCIBLE_DURATION) {
                invincible = false;
                invincibleCounter = 0;
            }
        }
        
        // 6. Giới hạn biên
        int minX = 1 * gp.tileSize;
        int minY = 1 * gp.tileSize;
        int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
        int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

        if (worldX < minX) worldX = minX;
        if (worldY < minY) worldY = minY;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;
        
        // 7. Cập nhật jump visual offset
        if (isJumping) {
            jumpVisualOffset = (int) (-velocityY * 0.5);
        } else {
            jumpVisualOffset = 0;
        }
        
        // 8. Reset stomp flag sau một thời gian
        if (isStomping) {
            // Sau khi đạp, reset sau 5 frames
            if (velocityY >= 0) {
                isStomping = false;
            }
        }
    }
    
    // Phương thức gây sát thương
    public void takeDamage(entity attacker) {
        if (!invincible) {
            life -= 1;
            invincible = true;
            invincibleCounter = 0;
            
            // Knockback effect
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
        BufferedImage image = null;

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
        
        // Hiệu ứng nhấp nháy khi invincible
        if (invincible && invincibleCounter % 10 < 5) {
            return; // Bỏ qua frame này
        }
        
        // Tính vị trí vẽ với offset nhảy
        int drawY = screenY + jumpVisualOffset;
        
        // Hiệu ứng visual khi đang đạp
        if (isStomping) {
            g2.setColor(new Color(255, 200, 0, 100));
            g2.fillRect(screenX, drawY, 128, 128);
        }
        
        g2.drawImage(image, screenX, drawY, 128, 128, null);
        
        // Vẽ debug stomp area (tùy chọn)
        if (gp.gameState == gp.playState && gp.keyH.debugPress) {
            g2.setColor(Color.RED);
            Rectangle stompArea = getStompArea();
            int screenStompX = stompArea.x - gp.player.worldX + gp.player.screenX;
            int screenStompY = stompArea.y - gp.player.worldY + drawY + solidArea.y;
            g2.drawRect(screenStompX, screenStompY, stompArea.width, stompArea.height);
        }
    }
    
    // Getter cho ground level
    public int getGroundLevel() {
        return groundLevel;
    }
    
    // Setter cho ground level (nếu cần thay đổi)
    public void setGroundLevel(int level) {
        groundLevel = level;
    }
    
    // Phương thức kiểm tra có đang trên không không
    public boolean isInAir() {
        return !isGrounded;
    }
}