package tile;

import Game_2D.gamePanel;
import Game_2D.utiltityTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class tileManager {
    gamePanel gp;
    public tile[] tile;
    public int mapTileNum[][];

    public tileManager(Game_2D.gamePanel gp) {
        this.gp = gp;

        tile = new tile[7];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/res/map/map.txt");
    }

    public void getTileImage() {
        setup(0, "grass2", false);
        setup(1, "sky", false);
        setup(2, "stone", true);
        setup(3, "solid", false);
        setup(4, "wood", true);
    }

    public void setup(int index, String imagePath, boolean collision) {
        utiltityTool uTool = new utiltityTool();

        try {
            tile[index] = new tile();
            InputStream is = getClass().getResourceAsStream("/res/tile/" + imagePath + ".png");
            
            if (is == null) {
                System.err.println("ERROR: Cannot find /res/tile/" + imagePath + ".png");
                // Create a colored tile as fallback
                tile[index].image = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = tile[index].image.createGraphics();
                g2.setColor(new Color(200, 200, 200));
                g2.fillRect(0, 0, gp.tileSize, gp.tileSize);
                g2.dispose();
            } else {
                tile[index].image = ImageIO.read(is);
                tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
            }
            
            tile[index].collision = collision;

        } catch (IOException e) {
            System.err.println("ERROR setting up tile: " + imagePath);
            e.printStackTrace();
        }
    }

    // FIXED: Better map loading with proper error handling
    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            
            if (is == null) {
                System.err.println("ERROR: Map file not found at " + filePath);
                return;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int row = 0;
            String line;

            while ((line = br.readLine()) != null && row < gp.maxWorldRow) {
                line = line.trim();
                
                // FIXED: Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                
                // FIXED: Ensure we don't exceed array bounds
                for (int col = 0; col < gp.maxWorldCol && col < parts.length; col++) {
                    try {
                        int value = Integer.parseInt(parts[col]);
                        mapTileNum[col][row] = value;
                    } catch (NumberFormatException e) {
                        System.err.println("WARNING: Invalid number at row " + row + ", col " + col);
                        mapTileNum[col][row] = 0; // Default to 0
                    }
                }
                
                // Fill remaining columns with 0 if line is short
                for (int col = parts.length; col < gp.maxWorldCol; col++) {
                    mapTileNum[col][row] = 0;
                }
                
                row++;
            }
            
            br.close();
            System.out.println("Map loaded successfully: " + row + " rows read.");
            
        } catch (Exception e) {
            System.err.println("ERROR loading map from " + filePath);
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        // Fill background to avoid showing panel background at edges
        g2.setColor(new Color(92, 201, 141));
        g2.fillRect(0, 0, gp.width, gp.height);

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            // Only draw tiles that are visible on screen
            if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                    worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                    worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
                
                // FIXED: Add bounds checking for tileNum
                if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null && tile[tileNum].image != null) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, null);
                } else {
                    // Draw error tile
                    g2.setColor(Color.MAGENTA);
                    g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                }
            }

            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}
