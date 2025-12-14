package tile;

import Game_2D.gamePanel;
import Game_2D.utiltityTool;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;

public class tileManager {
    gamePanel gp;
    public tile[] tile;
    public int[][] mapTileNum;

    public tileManager(gamePanel gp) {
        this.gp = gp;

        tile = new tile[9];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/res/map/map.txt");
    }

    public void getTileImage() {
        setup(0, "grass2", true);
        setup(1, "sky", false);
        setup(2, "stone", true);
        setup(3, "solid", true); // đất
        setup(4, "wood", true);
        setup(5, "grassCollision", true); // cỏ collision
        setup(6, "solidCollision", true); // đá collision
        setup(7, "skyCollision", true);
        setup(8, "sky", false); // hole tile
    }

    public void setup(int index, String imagePath, boolean collision) {
        utiltityTool uTool = new utiltityTool();

        try {
            tile[index] = new tile();
            InputStream is = getClass().getResourceAsStream("/res/tile/" + imagePath + ".png");
            if (is == null) {
                File f = new File("src/res/tile/" + imagePath + ".png");
                if (!f.exists()) {
                    f = new File("out/res/tile/" + imagePath + ".png");
                }
                if (f.exists()) {
                    is = new java.io.FileInputStream(f);
                }
            }

            if (is != null) {
                tile[index].image = ImageIO.read(is);
                is.close();
            } else {
                tile[index].image = new java.awt.image.BufferedImage(gp.tileSize, gp.tileSize, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }
            tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
            tile[index].collision = collision;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = null;
            File f = new File("src" + filePath);
            if (!f.exists()) {
                f = new File("out" + filePath);
            }
            if (f.exists()) {
                is = new java.io.FileInputStream(f);
            } else {
                is = getClass().getResourceAsStream(filePath);
            }
            if (is == null) {
                throw new IOException("Map not found: " + filePath);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int row = 0;

            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                for (int col = 0; col < gp.maxWorldCol; col++) {
                    int value = 0;
                    if (col < parts.length && !parts[col].isEmpty()) {
                        try {
                            value = Integer.parseInt(parts[col]);
                        } catch (NumberFormatException ignore) {
                            value = 0;
                        }
                    }
                    mapTileNum[col][row] = value;
                }
                row++;
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[][] getMapTileNum() {
        return mapTileNum;
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        g2.setColor(new Color(92, 201, 141));
        g2.fillRect(0, 0, gp.width, gp.height);

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow];

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                    worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                    worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
                g2.drawImage(tile[tileNum].image, screenX, screenY, null);
            }

            worldCol++;
            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}
