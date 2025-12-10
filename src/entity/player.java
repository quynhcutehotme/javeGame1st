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

public class player extends entity{

    gamePanel gp;
    keyHander keyH ;

    public int screenX;
    public final int screenY;
    
    // Thuộc tính HP và Miễn nhiễm
    public int maxLife;
    public int life;
    public boolean invincible = false;
    public int invincibleCounter = 0;
    private final int INVINCIBLE_DURATION = 60; // 1 second

    // Jump/Gravity state
    private float velocityY = 0f;
    private float gravity = 0.6f;
    private float jumpStrength = -10f;
    public boolean isGrounded = true; 
    public int jumpVisualOffset = 0; // pixels above screenY for drawing
    public int camX;       // camera X
    public int lastCamX;


    public player(gamePanel gp, keyHander keyH){
        this.gp =gp ;
        this.keyH = keyH;

        // vị trí nhân vật trong màn hình (Camera stays centered horizontally)
        screenX = gp.width / 2 - (gp.tileSize / 2);
        // Đặt screenY cố định gần đáy màn hình cho game Platformer
        screenY = gp.height * 2 / 4 +(gp.tileSize); 


        // vị trí collusion trên player
        solidArea = new Rectangle();
        solidArea.x = 45;
        solidArea.y = 35;
        solidArea.width = 42;
        solidArea.height = 90;

        setDefaultValue();
        getPlayerImage();
        // setup();
    }

    // xuất hiện trên máp
    public void setDefaultValue(){
        worldX= gp.tileSize*10;
        worldY=gp.tileSize*11;
        speed = 4;
        direction ="right";
        
        maxLife = 3; 
        life = maxLife;
        invincible = false;
    }

    public void getPlayerImage(){
        // up1, up2, down1, down2: Nếu game là Platformer, có thể chỉ cần Right/Left/Jump
        // ... (Giữ nguyên logic load ảnh)
        right1 = setup("ThiNo-1");
        right2 = setup("ThiNo-2");
        left1 = setup("ThiNo-3");
        left2 = setup("ThiNo-4");
    }

    public BufferedImage setup(String imagePath){
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;

        try{
            // Try with .png.png first (actual file extension), then fallback to .png
            InputStream is = getClass().getResourceAsStream("/res/player/"+imagePath+".png.png");
            if (is == null) {
                is = getClass().getResourceAsStream("/res/player/"+imagePath+".png");
            }
            if (is == null) {
                // Fallback: try loading from file system
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
        }
        catch (IOException e ){
            e.printStackTrace();
        }
        return image;
    }

    private void setup() {
        try {
            // try loading from classpath first (put images under src/main/resources/images/...)
            String resourcePath = "/images/player.png"; // <-- adjust to the actual path inside resources/jar
            InputStream is = getClass().getResourceAsStream(resourcePath);

            // fallback: try project filesystem (useful during development in IDE)
            if (is == null) {
                File f = new File("src/main/resources" + resourcePath); // or "resources" depending on your layout
                if (f.exists()) {
                    is = new FileInputStream(f);
                }
            }

            if (is == null) {
                throw new IllegalArgumentException("Image resource not found: " + resourcePath);
            }

            BufferedImage img = ImageIO.read(is);
            // ... assign img to your player sprite fields ...
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Phương thức gây sát thương
    public void takeDamage(entity attacker) {
        if (!invincible) {
            life -= 1; // Mất 1 máu
            invincible = true;
            invincibleCounter = 0;
            
            // Thêm logic hiệu ứng knockback (đẩy lùi) nhẹ
            // worldX += (attacker.worldX < worldX) ? speed * 10 : -speed * 10;
            
            if (life <= 0) {
                // Xử lý Game Over
                // Giả sử có trạng thái game: gp.gameState = gp.gameOverState;
                System.out.println("YOU LOSE!");
            }
        }
    }

    public void update(){
        
        // 1. Horizontal and vertical movement
        boolean moving = false;
        
        // Handle horizontal movement
        if (keyH.leftPress || keyH.rightPress) {
            moving = true;
            if (keyH.leftPress) {
                direction = "left";
            } else if (keyH.rightPress) {
                direction = "right";
            }
            
            // Check collision for horizontal movement
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
        
        // Handle vertical movement (down only, up is handled by jump)
        if (keyH.downPress) {
            moving = true;
            direction = "down";
            
            // Check collision for vertical movement
            collisionOn = false;
            gp.cChecker.checkTile(this);
            
            if (!collisionOn) {
                worldY += speed;
            }
        }
        
        // Handle up movement (separate from jump for non-platformer movement)
        if (keyH.upPress && !keyH.jumpPress) {
            moving = true;
            direction = "up";
            
            // Check collision for vertical movement
            collisionOn = false;
            gp.cChecker.checkTile(this);
            
            if (!collisionOn) {
                worldY -= speed;
            }
        }
        
        // 2. Vertical movement (Jump/Gravity)
        
        // Check if collision checker finds ground (cần cập nhật trong cChecker)
        // Hiện tại dùng kiểm tra đơn giản:
        
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
            keyH.jumpPress = false; // Xử lý input nhảy
        }

        // Apply gravity when not grounded
        if (!isGrounded) {
            velocityY += gravity;
            
            // Cập nhật worldY (di chuyển thật)
            worldY += (int) Math.round(velocityY); 
            
            // Xử lý va chạm khi rơi/nhảy (cần được xử lý trong cChecker, nhưng ta sẽ dùng logic đơn giản)
            // Nếu va chạm với vật thể cứng bên dưới, đặt lại isGrounded = true và worldY = vị trí trên nền
            
            // Simple ground check (cho đến khi cChecker được cập nhật)
            if (worldY >= gp.tileSize * 11) { // Giả sử 11 là hàng nền cứng
                worldY = gp.tileSize * 11;
                velocityY = 0f;
                isGrounded = true;
                jumpVisualOffset = 0;
            }
        }
        
        // 3. Xử lý hoạt hình (chỉ khi đang di chuyển ngang)
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
        
        // 4. Xử lý trạng thái miễn nhiễm
        if (invincible) {
            invincibleCounter++;
            if (invincibleCounter > INVINCIBLE_DURATION) {
                invincible = false;
            }
        }
        
        // 5. Ngăn chặn vượt biên giới (giữ nguyên)
        int minX = 1 * gp.tileSize;
        int minY = 1 * gp.tileSize;
        int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
        int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

        if (worldX < minX) worldX = minX;
        if (worldY < minY) worldY = minY;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;

    }


    public void draw(Graphics2D g2){


        BufferedImage image = null;

        switch (direction) {
            // ... (Giữ nguyên case down/up - nếu không có ảnh, sẽ không vẽ gì)
            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;
            default:
                 // Vẫn nên vẽ một hình ảnh mặc định khi không di chuyển
                 image = right1;
        }

        // Tạo hiệu ứng nhấp nháy khi đang miễn nhiễm
        if (invincible && invincibleCounter % 10 < 5) { // Nhấp nháy 5 frames on / 5 frames off
             // Không vẽ (bỏ qua frame vẽ)
        } else {
             // Vị trí vẽ là screenY (cố định trên màn hình) trừ đi jumpVisualOffset
             // Lưu ý: Nếu logic Platformer áp dụng vào worldY, bạn không cần jumpVisualOffset nữa.
             // Nếu bạn vẫn muốn giữ Player tại screenY, thì phải áp dụng thế này:
             int drawY = screenY + jumpVisualOffset;
             g2.drawImage(image, screenX, drawY, 128, 128, null);
        }
    }
}

        // Vẽ HP (Ví dụ đơn giản)

