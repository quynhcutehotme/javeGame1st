package entity;

import Game_2D.gamePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class bot extends entity {
    private final gamePanel gp;
    private boolean dying = false;
    private int dyingCounter = 0;

    // --- 1. THÊM BIẾN ĐỂ GHI NHỚ VỊ TRÍ XUẤT PHÁT ---
    private int startX;

    public bot(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = 1;
        this.direction = "left";

        // Ghi nhớ vị trí ban đầu ngay khi tạo bot
        this.startX = worldX;

        // Hitbox to gấp đôi (như bạn đã chỉnh)
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 80;
        solidArea.height = 80;

        // Mở comment nếu cần
        // solidAreaDefaultX = solidArea.x;
        // solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        try {
            left1 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_left1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_left2.png"));
            left3 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_left3.png"));

            right1 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_right1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_right2.png"));
            right3 = ImageIO.read(getClass().getResourceAsStream("/res/monster/bot_right3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (dying) return;

        // Xử lý hướng đi (Check va chạm + Check khoảng cách)
        setAction();

        collisionOn = false;
        gp.cChecker.checkTile(this);

        // Di chuyển
        if (!collisionOn) {
            switch (direction) {
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }

        // Animation
        spriteCounter++;
        if (spriteCounter > 10) {
            if (spriteNum == 1) spriteNum = 2;
            else if (spriteNum == 2) spriteNum = 3;
            else if (spriteNum == 3) spriteNum = 1;
            spriteCounter = 0;
        }
    }

    // --- 2. CHỈNH SỬA LOGIC DI CHUYỂN Ở ĐÂY ---
    public void setAction() {
        // Ưu tiên 1: Đụng tường thì phải quay đầu ngay
        if (collisionOn) {
            direction = direction.equals("left") ? "right" : "left";
        }

        // Ưu tiên 2: Giới hạn phạm vi di chuyển trong 2 ô
        // Tính khoảng cách hiện tại so với lúc xuất phát
        int distanceFromStart = worldX - startX;

        // Nếu đi quá 2 ô về bên phải (Dương) -> Quay về trái
        if (distanceFromStart > gp.tileSize * 2) {
            direction = "left";
        }
        // Nếu đi quá 2 ô về bên trái (Âm) -> Quay về phải
        else if (distanceFromStart < -gp.tileSize * 2) {
            direction = "right";
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + gp.tileSize * 2 > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize * 2 < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize * 2 > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize * 2 < gp.player.worldY + gp.player.screenY) {

            BufferedImage image = null;
            switch (direction) {
                case "left":
                    if (spriteNum == 1) image = left1;
                    if (spriteNum == 2) image = left2;
                    if (spriteNum == 3) image = left3;
                    break;
                case "right":
                    if (spriteNum == 1) image = right1;
                    if (spriteNum == 2) image = right2;
                    if (spriteNum == 3) image = right3;
                    break;
            }

            // Kích thước vẽ to gấp đôi
            int drawWidth = gp.tileSize * 3;
            int drawHeight = gp.tileSize * 2;

            if (dying) {
                dyingCounter++;
                int squashH = gp.tileSize / 3;
                int squashY = screenY + drawHeight - squashH;
                if (image != null) {
                    g2.drawImage(image, screenX, squashY, drawWidth, squashH, null);
                }
                if (dyingCounter > 30) worldX = -1000;
                return;
            }

            if (image != null) {
                g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect(screenX, screenY, drawWidth, drawHeight);
            }
        }
    }
}