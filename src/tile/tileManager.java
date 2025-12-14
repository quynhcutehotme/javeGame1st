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

        tile = new tile[9];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/map/map.txt");

    }

    public void getTileImage() {
        setup(0,"grass2",true );
        setup(1,"sky",false);
        setup(2,"stone",true);
        setup(3,"solid",true); //đất
        setup(4,"wood",true);
        setup(5,"grassCollision",true); //cỏ C
        setup(6,"solidCollision",true); //đát c
        setup(7,"skyCollision",true);
        setup(8,"solidDie",false);

    }

    public void setup(int index, String imagePath, boolean collision){
        utiltityTool uTool = new utiltityTool();

        try{
            tile[index]= new tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/tile/" + imagePath +".png"));
            tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
            tile[index].collision = collision;

        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int row = 0;

            while (row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) break;

                line = line.trim();
                if (line.isEmpty()) {
                    // Skip blank lines
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

    // PHƯƠNG THỨC DRAW MỚI - NHẬN CAMERA OFFSET
    public void draw(Graphics2D g2, int cameraX, int cameraY) {

        // Fill background
        g2.setColor(new Color(92, 201, 141));
        g2.fillRect(0, 0, gp.width, gp.height);

        // Tính toán các tile cần vẽ dựa trên camera
        int startCol = cameraX / gp.tileSize;
        int startRow = cameraY / gp.tileSize;

        // +2 để đảm bảo không bị khoảng trống
        int endCol = startCol + (gp.width / gp.tileSize) + 2;
        int endRow = startRow + (gp.height / gp.tileSize) + 2;

        // Giới hạn trong map
        startCol = Math.max(startCol, 0);
        startRow = Math.max(startRow, 0);
        endCol = Math.min(endCol, gp.maxWorldCol);
        endRow = Math.min(endRow, gp.maxWorldRow);

        for (int row = startRow; row < endRow; row++) {
            for (int col = startCol; col < endCol; col++) {

                int tileNum = mapTileNum[col][row];

                int worldX = col * gp.tileSize;
                int worldY = row * gp.tileSize;
                int screenX = worldX - cameraX;
                int screenY = worldY - cameraY;

                // Chỉ vẽ tile nằm trong màn hình
                if (screenX + gp.tileSize > 0 && screenX < gp.width &&
                        screenY + gp.tileSize > 0 && screenY < gp.height) {

                    if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null) {
                        g2.drawImage(tile[tileNum].image, screenX, screenY, null);
                    }
                }
            }
        }
    }

    // Giữ phương thức cũ để tương thích
    public void draw(Graphics2D g2) {
        // Sử dụng camera từ gamePanel
        if (gp.camera != null) {
            draw(g2, gp.camera.worldX, gp.camera.worldY);
        } else {
            // Fallback: dùng player position
            draw(g2, gp.player.worldX - gp.player.screenX, gp.player.worldY - gp.player.screenY);
        }
    }
}