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
    // Jump/Gravity state
    private float velocityY = 0f;
    private float gravity = 0.6f;
    private float jumpStrength = -10f;
    private boolean isGrounded = true;
    private int jumpVisualOffset = 0; // pixels above screenY for drawing
    public int camX;      // camera X
    public int lastCamX;


    public player(gamePanel gp, keyHander keyH){
        this.gp =gp ;
        this.keyH = keyH;

        final int PLAYER_SIZE = 128;

        // vị trí nhân vật trong màn hình
        screenX = gp.width / 2 - (128 / 2);
        screenY = gp.height * 2 / 3 + (gp.tileSize)-50;

        // vị trí collusion trên player
        solidArea = new Rectangle();
        solidArea.x = 6;
        solidArea.y = 6;
        solidArea.width = 60;
        solidArea.height = 60;

        setDefaultValue();
        getPlayerImage();
    }

    public void setDefaultValue(){
         worldX= gp.tileSize*20;
         worldY=gp.tileSize*28;
         speed = 4;
         direction ="right";

    }


    public void getPlayerImage(){
        //up1 = setup("up1");
        //up2 = setup("up1-2.png");
        //down1 = setup("back-6.png");
       // down2 = setup("back-7.png");
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

        // 1. Chỉ kiểm tra phím TRÁI và PHẢI
        if (keyH.leftPress || keyH.rightPress) {

            if (keyH.leftPress) {
                direction = "left";
            } else if (keyH.rightPress) {
                direction = "right";
            }

            // Kiểm tra va chạm
            collisionOn = false;
            gp.cChecker.checkTile(this);

            // Nếu không va chạm thì mới di chuyển
            if (!collisionOn) {
                switch (direction) {
                    case "left":
                        worldX -= speed;
                        break;
                    case "right":
                        worldX += speed;
                        break;
                    // BỎ case "up" và "down" để không thay đổi worldY bằng phím di chuyển
                }
            }

            // Animation bước chân
            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        } else {
            // Nếu không bấm phím nào, đứng yên (có thể reset sprite về 1 nếu muốn)
        }

        // 2. Xử lý nhảy (JUMP)
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // 3. Áp dụng trọng lực (Gravity) - Đây là thứ duy nhất thay đổi Y
        if (!isGrounded) {
            velocityY += gravity;
            // Cập nhật vị trí nhảy ảo để vẽ (hoặc cập nhật worldY nếu muốn map cuộn theo chiều dọc)
            jumpVisualOffset += (int) Math.round(velocityY);

            // Giả lập mặt đất đơn giản (khi chạm đất thì reset)
            // Lưu ý: Logic này chỉ đúng nếu bạn muốn mặt đất luôn phẳng
            if (jumpVisualOffset > 0) {
                jumpVisualOffset = 0;
                velocityY = 0f;
                isGrounded = true;
            }
        }
    }



    public void draw(Graphics2D g2){


        BufferedImage image = null;

        switch (direction) {
            case "down":
                if (spriteNum==1){
                    image=down1;}
                if (spriteNum==2) {
                    image=down2;
                }
                break;
            case "up":
                if (spriteNum==1){
                    image=up1;}
                if (spriteNum==2) {
                    image=up2;
                }
                break;
            case "right":
                if (spriteNum==1){
                    image=right1;}
                if (spriteNum==2) {
                    image=right2;
                }
                break;
            case "left":
                if (spriteNum==1){
                    image=left1;}
                if (spriteNum==2) {
                    image=left2;
                }
                break;

        }



		// Draw with vertical offset to visualize jump
		//g2.drawImage(image, screenX, screenY + jumpVisualOffset, gp.tileSize, gp.tileSize, null);
        g2.drawImage(image, screenX, screenY + jumpVisualOffset, 128, 128, null);

        g2.setColor(Color.white);
        }
    }




//package entity;
//
//import Game_2D.gamePanel;
//import Game_2D.keyHander;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.IOException;
//
//
//
//public class player extends entity {
//
//    gamePanel gp;
//    keyHander keyH;
//
//    public int camX;   // Camera X (world coordinates)
//    public final int screenY;
//    public int drawX;
//
//    public player(gamePanel gp, keyHander keyH) {
//        this.gp = gp;
//        this.keyH = keyH;
//
//        // Nhân vật luôn ở 1/2 màn hình (trừ lúc ở sát biên map)
//        screenY = gp.height * 2 / 3 + (gp.tileSize);
//
//        solidArea = new Rectangle();
//        solidArea.x = 8;
//        solidArea.y = 16;
//        solidArea.width = 40;
//        solidArea.height = 40;
//
//        setDefaultValue();
//        getPlayerImage();
//    }
//
//    public void setDefaultValue() {
//        worldX = gp.tileSize * 6;
//        worldY = gp.tileSize * 11;
//        speed = 4;
//        direction = "down";
//        camX = 0;
//    }
//
//    public void getPlayerImage(){
//        try {
//            up1 = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));
//            up2 = ImageIO.read(getClass().getResourceAsStream("/player/up1-2.png.png"));
//            down1 = ImageIO.read(getClass().getResourceAsStream("/player/back-6.png.png"));
//            down2 = ImageIO.read(getClass().getResourceAsStream("/player/back-7.png.png"));
//            right1 = ImageIO.read(getClass().getResourceAsStream("/player/right-3.png.png"));
//            right2 = ImageIO.read(getClass().getResourceAsStream("/player/right-4.png.png"));
//            left1 = ImageIO.read(getClass().getResourceAsStream("/player/left.png"));
//            left2 = ImageIO.read(getClass().getResourceAsStream("/player/left2.png"));
//
//        }
//
//        catch(IOException e){
//            e.printStackTrace();
//        }
//
//    }
//
//    public void update() {
//        boolean moving = (keyH.upPress || keyH.downPress || keyH.leftPress || keyH.rightPress);
//
//        if (moving) {
//            if (keyH.upPress) direction = "up";
//            else if (keyH.downPress) direction = "down";
//            else if (keyH.leftPress) direction = "left";
//            else if (keyH.rightPress) direction = "right";
//
//            collisionOn = false;
//            gp.cChecker.checkTile(this);
//
//            int nextWorldX = worldX, nextWorldY = worldY;
//            if (!collisionOn) {
//                switch (direction) {
//                    case "up":
//                        nextWorldY -= speed;
//                        break;
//                    case "down":
//                        nextWorldY += speed;
//                        break;
//                    case "left":
//                        // Không cho lùi quá biên trái camera
//                        if (worldX > camX) nextWorldX -= speed;
//                        break;
//                    case "right":
//                        nextWorldX += speed;
//                        break;
//                }
//            }
//
//            // Cập nhật vị trí mới
//            worldX = nextWorldX;
//            worldY = nextWorldY;
//
//            // Camera chỉ chạy tới khi nhân vật tiến lên
//            int halfScreen = gp.width / 2 - gp.tileSize / 2;
//
//            if (worldX < halfScreen) {
//                // Ở nửa trái màn hình: camera đứng yên, Mario tự chạy
//                camX = 0;
//                drawX = worldX;
//            } else {
//                // Qua nửa màn hình: camera chạy theo, Mario đứng yên giữa màn hình
//                camX = worldX - halfScreen;
//                drawX = halfScreen;
//            }
//
//            // Animation
//            spriteCounter++;
//            if (spriteCounter > 10) {
//                spriteNum = (spriteNum == 1) ? 2 : 1;
//                spriteCounter = 0;
//            }
//        }
//    }
//
//    public void draw(Graphics2D g2) {
//        BufferedImage image = null;
//        switch (direction) {
//            case "down":
//                image = (spriteNum == 1) ? up1 : up2;
//                break;
//            case "up":
//                image = (spriteNum == 1) ? down1 : down2;
//                break;
//            case "right":
//                image = (spriteNum == 1) ? right1 : right2;
//                break;
//            case "left":
//                image = (spriteNum == 1) ? left1 : left2;
//                break;
//        }
//
//        // Vẽ Mario tại vị trí màn hình: worldX - camX
//        drawX = worldX - camX;
//        g2.drawImage(image, drawX, screenY, gp.tileSize, gp.tileSize, null);
//    }
//}
//
//
