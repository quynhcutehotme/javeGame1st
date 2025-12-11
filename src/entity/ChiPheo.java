package entity;

import Game_2D.gamePanel;
import Game_2D.utiltityTool;
import java.awt.*;

import javax.swing.ImageIcon;

public class ChiPheo {
    gamePanel gp;
    public int worldX, worldY;
    public Image image; // đổi BufferedImage thành Image

    public ChiPheo(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;

        ImageIcon icon = new ImageIcon(getClass().getResource("/res/ui/CPheo.gif"));
        image = icon.getImage(); // GIF động sẽ tự chạy
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        g2.drawImage(image, screenX, screenY, gp.tileSize + 250, gp.tileSize + 60, null);
    }
}
