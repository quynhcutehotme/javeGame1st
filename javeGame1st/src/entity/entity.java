package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class entity {
    public int worldX, worldY;
    public int speed;
    public BufferedImage up1, up2, down1, down2, left1, left2, left3, right1, right2, right3;
    public String direction;
    public int spriteCounter = 0;
    public int spriteNum = 1;

    // CHỈ GIỮ 1 DÒNG NÀY THÔI:
    public boolean collisionOn = false;

    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public int solidAreaDefaultX, solidAreaDefaultY;

    // Các hàm rỗng để tránh lỗi @Override
    public void update() {}
    public void draw(java.awt.Graphics2D g2) {}
}