package Game_2D;

import java.awt.*;

public class DeathEffect {
    private final int worldX;
    private final int worldY;
    private int life = 18;

    public DeathEffect(int worldX, int worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public boolean update() {
        life--;
        return life > 0;
    }

    public void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int playerScreenX, int playerScreenY) {
        int screenX = worldX - playerWorldX + playerScreenX;
        int screenY = worldY - playerWorldY + playerScreenY;

        float progress = 1f - (life / 18f);
        int radius = (int) (10 + 44 * progress);
        int alpha = Math.max(80, 230 - (int) (progress * 230));

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

        g2.setColor(new Color(255, 70, 70, alpha));
        g2.setStroke(new BasicStroke(5f));
        g2.drawOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

        g2.setColor(new Color(255, 140, 140, Math.max(90, alpha - 30)));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(screenX - radius / 2, screenY - radius / 2, radius, radius);

        int coreSize = 18;
        g2.setColor(new Color(255, 200, 200, Math.min(240, alpha + 40)));
        g2.fillOval(screenX - coreSize / 2, screenY - coreSize / 2, coreSize, coreSize);

        g2.setColor(new Color(255, 255, 255, Math.min(220, alpha)));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(screenX - radius, screenY, screenX + radius, screenY);
        g2.drawLine(screenX, screenY - radius, screenX, screenY + radius);

        g2.setComposite(old);
    }
}
