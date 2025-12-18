package entity;

import Game_2D.gamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public class entity {
    public gamePanel gp;
    public int worldX, worldY;
    public int speed;

  
    public int screenX = 0;
    public int screenY = 0;

    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2, backgr;
    public String direction;

    public int spriteCounter =0;
    public int spriteNum = 1;

    public Rectangle solidArea;
    public boolean collisionOn = false;
}


