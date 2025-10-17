package Game_2D;

import entity.entity;
import entity.player;
import entity.bot;

public class collisionChecker {
    gamePanel gp;
    public collisionChecker(gamePanel gp){
        this.gp = gp;
    }
    public void checkTile(entity entity){
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;

        int entityLeftCol = entityLeftWorldX/gp.tileSize;
        int entityRightCol = entityRightWorldX/gp.tileSize;
        int entityTopRow = entityTopWorldY/gp.tileSize;
        int entityBottomRow = entityBottomWorldY/gp.tileSize;

        // Clamp indices to prevent ArrayIndexOutOfBounds
        entityLeftCol = Math.max(0, Math.min(entityLeftCol, gp.maxWorldCol - 1));
        entityRightCol = Math.max(0, Math.min(entityRightCol, gp.maxWorldCol - 1));
        entityTopRow = Math.max(0, Math.min(entityTopRow, gp.maxWorldRow - 1));
        entityBottomRow = Math.max(0, Math.min(entityBottomRow, gp.maxWorldRow - 1));

        int tileNum1, tileNum2;

        switch (entity.direction) {
            case "up":
                entityTopRow= (entityTopWorldY-entity.speed)/gp.tileSize;
                entityTopRow = Math.max(0, Math.min(entityTopRow, gp.maxWorldRow - 1));
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "down":
                entityBottomRow= (entityBottomWorldY+entity.speed)/gp.tileSize;
                entityBottomRow = Math.max(0, Math.min(entityBottomRow, gp.maxWorldRow - 1));
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "left":
                entityLeftCol= (entityLeftWorldX-entity.speed)/gp.tileSize;
                entityLeftCol = Math.max(0, Math.min(entityLeftCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "right":
                entityRightCol= (entityRightWorldX+entity.speed)/gp.tileSize;
                entityRightCol = Math.max(0, Math.min(entityRightCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true){
                    entity.collisionOn = true;
                }
                break;
            // Handle diagonal directions
            case "up-left":
                entityTopRow= (entityTopWorldY-entity.speed)/gp.tileSize;
                entityLeftCol= (entityLeftWorldX-entity.speed)/gp.tileSize;
                entityTopRow = Math.max(0, Math.min(entityTopRow, gp.maxWorldRow - 1));
                entityLeftCol = Math.max(0, Math.min(entityLeftCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                int tileNum3 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true || gp.tileM.tile[tileNum3].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "up-right":
                entityTopRow= (entityTopWorldY-entity.speed)/gp.tileSize;
                entityRightCol= (entityRightWorldX+entity.speed)/gp.tileSize;
                entityTopRow = Math.max(0, Math.min(entityTopRow, gp.maxWorldRow - 1));
                entityRightCol = Math.max(0, Math.min(entityRightCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                tileNum3 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true || gp.tileM.tile[tileNum3].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "down-left":
                entityBottomRow= (entityBottomWorldY+entity.speed)/gp.tileSize;
                entityLeftCol= (entityLeftWorldX-entity.speed)/gp.tileSize;
                entityBottomRow = Math.max(0, Math.min(entityBottomRow, gp.maxWorldRow - 1));
                entityLeftCol = Math.max(0, Math.min(entityLeftCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityLeftCol][entityTopRow];
                tileNum3 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true || gp.tileM.tile[tileNum3].collision == true){
                    entity.collisionOn = true;
                }
                break;
            case "down-right":
                entityBottomRow= (entityBottomWorldY+entity.speed)/gp.tileSize;
                entityRightCol= (entityRightWorldX+entity.speed)/gp.tileSize;
                entityBottomRow = Math.max(0, Math.min(entityBottomRow, gp.maxWorldRow - 1));
                entityRightCol = Math.max(0, Math.min(entityRightCol, gp.maxWorldCol - 1));
                tileNum1 = gp.tileM.mapTileNum[entityRightCol][entityBottomRow];
                tileNum2 = gp.tileM.mapTileNum[entityRightCol][entityTopRow];
                tileNum3 = gp.tileM.mapTileNum[entityLeftCol][entityBottomRow];
                if (gp.tileM.tile[tileNum1].collision == true || gp.tileM.tile[tileNum2].collision == true || gp.tileM.tile[tileNum3].collision == true){
                    entity.collisionOn = true;
                }
                break;
        }

        // Check map boundaries
        checkMapBoundaries(entity);
    }
    
    private void checkMapBoundaries(entity entity) {
        int entityLeftWorldX = entity.worldX + entity.solidArea.x;
        int entityRightWorldX = entity.worldX + entity.solidArea.x + entity.solidArea.width;
        int entityTopWorldY = entity.worldY + entity.solidArea.y;
        int entityBottomWorldY = entity.worldY + entity.solidArea.y + entity.solidArea.height;
        
        // Calculate future position based on direction and speed
        int futureLeftX = entityLeftWorldX;
        int futureRightX = entityRightWorldX;
        int futureTopY = entityTopWorldY;
        int futureBottomY = entityBottomWorldY;
        
        switch (entity.direction) {
            case "up":
                futureTopY -= entity.speed;
                break;
            case "down":
                futureBottomY += entity.speed;
                break;
            case "left":
                futureLeftX -= entity.speed;
                break;
            case "right":
                futureRightX += entity.speed;
                break;
            case "up-left":
                futureTopY -= entity.speed;
                futureLeftX -= entity.speed;
                break;
            case "up-right":
                futureTopY -= entity.speed;
                futureRightX += entity.speed;
                break;
            case "down-left":
                futureBottomY += entity.speed;
                futureLeftX -= entity.speed;
                break;
            case "down-right":
                futureBottomY += entity.speed;
                futureRightX += entity.speed;
                break;
        }
        
        // Check if future position would be outside map boundaries
        int mapLeftBoundary = 0;
        int mapRightBoundary = gp.maxWorldCol * gp.tileSize;
        int mapTopBoundary = 0;
        int mapBottomBoundary = gp.maxWorldRow * gp.tileSize;
        
        if (futureLeftX < mapLeftBoundary || 
            futureRightX > mapRightBoundary || 
            futureTopY < mapTopBoundary || 
            futureBottomY > mapBottomBoundary) {
            entity.collisionOn = true;
        }
    }

    public boolean entitiesIntersect(entity a, entity b) {
        int ax1 = a.worldX + a.solidArea.x;
        int ay1 = a.worldY + a.solidArea.y;
        int ax2 = ax1 + a.solidArea.width;
        int ay2 = ay1 + a.solidArea.height;

        int bx1 = b.worldX + b.solidArea.x;
        int by1 = b.worldY + b.solidArea.y;
        int bx2 = bx1 + b.solidArea.width;
        int by2 = by1 + b.solidArea.height;

        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
    }

}
