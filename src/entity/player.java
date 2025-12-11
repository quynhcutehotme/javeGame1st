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


    public void update(){

        // Horizontal movement (existing)
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

                // Prevent reaching the world border: block exactly the outermost row/column
                // Allow movement only within tile indices [1 .. max-2]
                int minX = 1 * gp.tileSize;
                int minY = 1 * gp.tileSize;
                int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
                int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

                if (worldX < minX) worldX = minX;
                if (worldY < minY) worldY = minY;
                if (worldX > maxX) worldX = maxX;
                if (worldY > maxY) worldY = maxY;
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

        // Jump input: start jump if grounded
        if (keyH.jumpPress && isGrounded) {
            velocityY = jumpStrength;
            isGrounded = false;
        }

        // Apply gravity when not grounded
        if (!isGrounded) {
            velocityY += gravity;
            jumpVisualOffset += (int) Math.round(velocityY);

            // Simple ground at base position (offset back to 0)
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

        g2.drawImage(image, screenX, screenY + jumpVisualOffset, 128, 128, null);;


        g2.setColor(Color.white);
    }
}




