package tile;

import Game_2D.gamePanel;
import Game_2D.utiltityTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class tileManager {
    gamePanel gp;
    public tile[] tile;
    public int mapTileNum[][];

    public tileManager(gamePanel gp) {
        this.gp = gp;

        // --- SỬA 1: Tăng size lên 10 để chứa được số 5 ---
        tile = new tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/map/map1.txt");
    }

    public void getTileImage() {

        // setup(1, "water", false);
        // setup(2, "stone", true);
        //setup(3, "wood", false);

        // --- SỬA 2: Khai báo tile số 5 (Bầu trời) ---
        tile[5] = new tile();
        tile[5].collision = false; // Đi xuyên qua được
        // Không cần load ảnh vì mình sẽ không vẽ nó
        setup(0, "grass", false);
    }

    public void setup(int index, String imagePath, boolean collision) {
        utiltityTool uTool = new utiltityTool();

        try {
            tile[index] = new tile();
            // Thử bỏ chữ /res đi xem sao
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/res/tile/" + imagePath + ".png"));
            tile[index].image = uTool.scaleImage(tile[index].image, gp.tileSize, gp.tileSize);
            tile[index].collision = collision;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMap(String filePath) {
        // 1. Phần đọc file (Giữ nguyên code cũ để tránh lỗi logic nếu sau này cần dùng lại)
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while (br.readLine() != null) {
                } // Đọc giả để đóng file an toàn
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. --- TẠO MAP THỦ CÔNG ---
        System.out.println("ĐANG TẠO MAP TEST: 1 DÒNG CỎ");

        // Bước A: Reset toàn bộ bản đồ thành số 5 (Bầu trời/Rỗng)
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                mapTileNum[col][row] = 5;
            }
        }

        // Bước B: Chỉ vẽ 1 hàng đất duy nhất tại hàng số 22
        // Lưu ý: Đảm bảo nhân vật của bạn spawn ở worldY < 22 * gp.tileSize (ví dụ hàng 20)
        for (int col = 0; col < gp.maxWorldCol; col++) {
            mapTileNum[col][30] = 0; //
        }
    }
    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {

            int tileNum = mapTileNum[worldCol][worldRow];

            // Tính toán vị trí trên màn hình so với Camera (Player)
            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            int buffer = gp.tileSize * 2;
            // Tối ưu: Chỉ vẽ những ô nằm trong khung hình camera
            if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                    worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                    worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

                // QUAN TRỌNG: Chỉ vẽ nếu tile đó không phải là bầu trời (số 5)
                // Vì tile 5 không có ảnh, nếu cố vẽ sẽ bị lỗi
                if (tileNum != 5 && tile[tileNum].image != null) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, null);
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
