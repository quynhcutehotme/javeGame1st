package entity;

import Game_2D.gamePanel;
import Game_2D.keyHander;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class player extends entity{

    gamePanel gp;
    keyHander keyH ;

    public int screenX;
    public final int screenY;
    public int camX;      // camera X
    public int lastCamX;


    public player(gamePanel gp, keyHander keyH){
        this.gp =gp ;
        this.keyH = keyH;


        // vị trí nhân vật trong màn hình
        screenX = gp.width / 2 - (gp.tileSize / 2);
        screenY = gp.height * 2 / 3 +(gp.tileSize);


        // vị trí collusion trên player
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y =16;
        solidArea.width = 40;
        solidArea.height = 40;

        setDefaultValue();
        getPlayerImage();
    }

    public void setDefaultValue(){
         worldX= gp.tileSize*6;
         worldY=gp.tileSize*11;
         speed = 4;
         direction ="down";

    }

    public void getPlayerImage(){
        try {
            up1 = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/player/up1-2.png.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/player/back-6.png.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/player/back-7.png.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/player/right-3.png.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/player/right-4.png.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/player/left.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/player/left2.png"));

        }

        catch(IOException e){
            e.printStackTrace();
        }

    }


    public void update(){

        if (keyH.upPress == true ||keyH.downPress == true ||keyH.leftPress == true ||keyH.rightPress == true){
            if (keyH.upPress == true){
                direction ="up";

            }
            else if (keyH.downPress == true){
                direction ="down";

            }
            else if (keyH.leftPress == true){
                direction ="left";

            }
            else if (keyH.rightPress == true){
                direction ="right";


            }

            collisionOn = false;
            gp.cChecker.checkTile(this);
            if(collisionOn==false){
                switch (direction){
                    case "up":
                        worldY -= speed ;
                        break;
                    case "down":
                        worldY += speed ;
                        break;
                    case "left":
                        worldX -= speed ;
                        break;
                    case "right":
                        worldX += speed ;

                        break;
                }
            }


            gp.cChecker.checkTile(this);
            spriteCounter++;
            if (spriteCounter>10){
                if (spriteNum==1) {
                    spriteNum=2;
                }
                else if  (spriteNum==2) {
                    spriteNum=1;
                }
                spriteCounter=0;
            }
        }

        }



    public void draw(Graphics2D g2){


        BufferedImage image = null;

        switch (direction) {
            case "down":
                if (spriteNum==1){
                    image=up1;}
                if (spriteNum==2) {
                    image=up2;
                }
                break;
            case "up":
                if (spriteNum==1){
                    image=down1;}
                if (spriteNum==2) {
                    image=down2;
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



        g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);


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
//    public int camX;   // Camera X (tọa độ thế giới)
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
