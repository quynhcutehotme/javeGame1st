package Game_2D;

import entity.bot;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HazardSystem {

    private final gamePanel gp;
    public final List<Rectangle> deathZones = new ArrayList<>();
    public final int deathZoneTileId;

    public HazardSystem(gamePanel gp, int deathZoneTileId) {
        this.gp = gp;
        this.deathZoneTileId = deathZoneTileId;
    }

    public void detectDeathZones() {
        deathZones.clear();

        int[][] mapTileNum = gp.tileM.mapTileNum;

        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                if (mapTileNum[col][row] == deathZoneTileId) {
                    Rectangle deathZone = new Rectangle(
                            col * gp.tileSize,
                            row * gp.tileSize,
                            gp.tileSize,
                            gp.tileSize
                    );
                    deathZones.add(deathZone);
                }
            }
        }

        System.out.println("Detected " + deathZones.size() + " death zones");
    }

    public boolean isPlayerInDeathZone() {
        if (gp.isGameOver() || gp.isGameWon()) return false;

        Rectangle playerRect = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y,
                gp.player.solidArea.width,
                gp.player.solidArea.height
        );

        for (Rectangle deathZone : deathZones) {
            if (playerRect.intersects(deathZone)) return true;
        }
        return false;
    }

    public boolean isBotInDeathZone(bot b) {
        Rectangle botRect = new Rectangle(
                b.worldX + b.solidArea.x,
                b.worldY + b.solidArea.y,
                b.solidArea.width,
                b.solidArea.height
        );

        for (Rectangle deathZone : deathZones) {
            if (botRect.intersects(deathZone)) return true;
        }
        return false;
    }

    public void checkBotDeathZone() {
        java.util.Iterator<bot> iterator = gp.bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();

            if (isBotInDeathZone(b)) {
                gp.deathEffects.add(new DeathEffect(
                        b.worldX + b.solidArea.width / 2,
                        b.worldY + b.solidArea.height / 2
                ));

                iterator.remove();

                int effectScreenX = gp.camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                int effectScreenY = gp.camera.worldYToScreenY(b.worldY);
                gp.damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "DEATH ZONE!", new Color(255, 50, 50)));
            }
        }
    }
}
