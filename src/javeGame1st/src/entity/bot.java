package entity;

import Game_2D.gamePanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class bot extends entity {
    private final gamePanel gp;
    private final Random random = new Random();
    private BufferedImage sprite;
    private Color bodyColor;
    private Color outlineColor;
    private int aiCounter = 0;
    private int changeDirInterval = 60; // frames
    
    // Behavioral states
    public enum BotState {
        PATROL,    // Random movement
        ALERT,     // Detected player, preparing to chase
        CHASE,     // Actively pursuing player
        SEARCH     // Lost player, searching last known location
    }
    
    private BotState currentState = BotState.PATROL;
    private int lastPlayerX, lastPlayerY;
    private int searchCounter = 0;
    private int alertCounter = 0;
    private final int searchDuration = 120; // frames to search
    private final int alertDuration = 30;   // frames before chasing

    public bot(gamePanel gp, int worldX, int worldY) {
        this.gp = gp;
        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = 2;
        this.direction = "left";
        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 40;
        solidArea.height = 40;
        // Pick a pleasant random body color per bot
        float hue = random.nextFloat();
        float sat = 0.6f + random.nextFloat() * 0.3f;
        float bri = 0.8f;
        bodyColor = Color.getHSBColor(hue, sat, bri);
        outlineColor = bodyColor.darker();
        // Keep sprite null to use vector draw; can be loaded if you prefer images
        sprite = null;
    }

    private void loadSprite() { /* no-op; using vector drawing now */ }

    public void updateAI(int playerWorldX, int playerWorldY) {
        aiCounter++;

        int dx = playerWorldX - worldX;
        int dy = playerWorldY - worldY;
        int distSq = dx*dx + dy*dy;

        boolean chase = distSq < (gp.tileSize * 10) * (gp.tileSize * 10);

        if (chase) {
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = (dx > 0) ? "right" : "left";
            } else {
                direction = (dy > 0) ? "down" : "up";
            }
        } else if (aiCounter % changeDirInterval == 0) {
            int r = random.nextInt(4);
            direction = (r==0?"up": r==1?"down": r==2?"left":"right");
        }

        collisionOn = false;
        gp.cChecker.checkTile(this);
        if (!collisionOn) {
            switch (direction) {
                case "up": worldY -= speed; break;
                case "down": worldY += speed; break;
                case "left": worldX -= speed; break;
                case "right": worldX += speed; break;
            }
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            // Enable antialiasing for nicer shapes
            Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = gp.tileSize;
            int padding = Math.max(2, size / 12);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 70));
            int shadowW = (int)(size * 0.8);
            int shadowH = (int)(size * 0.25);
            int shadowX = screenX + (size - shadowW) / 2;
            int shadowY = screenY + size - shadowH / 2;
            g2.fillOval(shadowX, shadowY, shadowW, shadowH);

            // Body
            int bodyX = screenX + padding;
            int bodyY = screenY + padding;
            int bodyW = size - padding * 2;
            int bodyH = size - padding * 2;
            g2.setColor(bodyColor);
            g2.fillRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);
            g2.setColor(outlineColor);
            g2.drawRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);

            // Eyes
            int eyeW = Math.max(4, size / 8);
            int eyeH = Math.max(5, size / 6);
            int eyeY = bodyY + size / 4;
            int eyeLX = bodyX + size / 5;
            int eyeRX = bodyX + bodyW - size / 5 - eyeW;
            g2.setColor(Color.WHITE);
            g2.fillOval(eyeLX, eyeY, eyeW, eyeH);
            g2.fillOval(eyeRX, eyeY, eyeW, eyeH);
            g2.setColor(Color.BLACK);
            int pupilW = Math.max(3, eyeW / 2);
            int pupilH = Math.max(3, eyeH / 3);
            int pupilOffsetX = direction.equals("left") ? -eyeW/6 : direction.equals("right") ? eyeW/6 : 0;
            int pupilOffsetY = direction.equals("up") ? -eyeH/6 : direction.equals("down") ? eyeH/6 : 0;
            g2.fillOval(eyeLX + (eyeW - pupilW)/2 + pupilOffsetX, eyeY + (eyeH - pupilH)/2 + pupilOffsetY, pupilW, pupilH);
            g2.fillOval(eyeRX + (eyeW - pupilW)/2 + pupilOffsetX, eyeY + (eyeH - pupilH)/2 + pupilOffsetY, pupilW, pupilH);

            // Mouth
            g2.setColor(outlineColor.darker());
            int mouthW = bodyW / 3;
            int mouthH = Math.max(2, size / 20);
            int mouthX = screenX + (size - mouthW) / 2;
            int mouthY = screenY + size / 2 + size / 8;
            g2.fillRoundRect(mouthX, mouthY, mouthW, mouthH, mouthH, mouthH);

            // Restore AA
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }
}



