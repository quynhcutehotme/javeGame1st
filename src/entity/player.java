package entity;

import Game_2D.gamePanel;
import Game_2D.keyHander;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import Game_2D.utiltityTool;

public class player extends entity{

    gamePanel gp;
    keyHander keyH ;

    public int screenX;
    public final int screenY;

    private float velocityY = 0f;
    private float gravity = 0.6f;
    private float jumpStrength = -15f;
    private boolean isGrounded = false;


    public player(gamePanel gp, keyHander keyH){
        this.gp =gp ;
        this.keyH = keyH;


        // vị trí nhân vật trong màn hình
        screenX = gp.width / 2 - (gp.tileSize / 2);
        screenY = gp.height * 2 / 4 +(gp.tileSize);


        // vị trí collusion trên player
        solidArea = new Rectangle();
        solidArea.x = 45;
        solidArea.y =35;
        solidArea.width = 42;
        solidArea.height = 90;

        setDefaultValue();
        getPlayerImage();
    }

    //xuất hiện trên máp
    public void setDefaultValue(){
        worldX= gp.tileSize*15;
        worldY=gp.tileSize*11;
        speed = 4;
        direction ="right";

    }

    public void getPlayerImage(){
        right1 = setup("ThiNo-1.png");
        right2 = setup("ThiNo-2.png");
        left1 = setup("ThiNo-3.png");
        left2 = setup("ThiNo-4.png");

    }

    public BufferedImage setup(String imagePath){
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;

        try{
            image = ImageIO.read(getClass().getResourceAsStream("/res/player/"+imagePath+".png"));
            // image = uTool.scaleImage(image, gp.tileSize,gp.tileSize);
            image = uTool.scaleImage(image, 128, 128);
        }
        catch (IOException e ){
            e.printStackTrace();
        }
        return  image;
    }


    public void update() {

        // ===================== HORIZONTAL MOVEMENT =======================
        if (keyH.leftPress || keyH.rightPress) {

            if (keyH.leftPress) direction = "left";
            if (keyH.rightPress) direction = "right";

            collisionOn = false;
            gp.cChecker.checkTile(this);

            if (!collisionOn) {
                if (direction.equals("left")) worldX -= speed;
                if (direction.equals("right")) worldX += speed;
            }

            // ANIMATION
            spriteCounter++;
            if (spriteCounter > 10) {
                spriteNum = (spriteNum == 1 ? 2 : 1);
                spriteCounter = 0;
            }
        }

        // ========================= JUMP INPUT ============================
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // ======================== GRAVITY APPLY ==========================

        velocityY += gravity;
        float newY = worldY + velocityY;

        // check va chạm theo Y
        if (gp.cChecker.checkCollisionY(this, newY)) {
            // nếu đang FALL xuống platform → đứng lại
            if (velocityY > 0) {
                isGrounded = true;
            }

            // reset rơi để không xuyên platform
            velocityY = 0;
        } else {
            worldY = (int)newY;
            isGrounded = false;
        }

    }


    public void draw(Graphics2D g2) {

        BufferedImage image = null;

        switch (direction) {
            case "right":
                image = (spriteNum == 1 ? right1 : right2);
                break;
            case "left":
                image = (spriteNum == 1 ? left1 : left2);
                break;
        }

        g2.drawImage(image, screenX, screenY, 128, 128, null);
    }
}






