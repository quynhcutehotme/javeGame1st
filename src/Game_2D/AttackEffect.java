package Game_2D;

import java.awt.*;

public class AttackEffect {
    private final Rectangle hitboxWorld;
    private int life;
    private final boolean windup;

    public AttackEffect(Rectangle hitboxWorld, int lifeFrames, boolean windup) {
        this.hitboxWorld = new Rectangle(hitboxWorld);
        this.life = lifeFrames;
        this.windup = windup;
    }

    public boolean update() {
        life--;
        return life > 0;
    }

    public void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int playerScreenX, int playerScreenY) {
        int screenX = hitboxWorld.x - playerWorldX + playerScreenX;
        int screenY = hitboxWorld.y - playerWorldY + playerScreenY;
        int alpha = windup
                ? Math.max(25, Math.min(160, life * 22))
                : Math.max(40, Math.min(200, life * 25));

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

        int arc = Math.min(hitboxWorld.width, hitboxWorld.height) / 3;

        g2.setColor(windup
                ? new Color(180, 220, 255, Math.min(200, alpha))
                : new Color(255, 235, 130, Math.min(220, alpha)));
        g2.fillRoundRect(screenX, screenY, hitboxWorld.width, hitboxWorld.height, arc, arc);

        g2.setColor(windup
                ? new Color(120, 180, 255, Math.min(210, alpha))
                : new Color(255, 120, 60, Math.min(230, alpha)));
        g2.setStroke(new BasicStroke(windup ? 3f : 4f));
        g2.drawRoundRect(screenX - 1, screenY - 1, hitboxWorld.width + 2, hitboxWorld.height + 2, arc + 4, arc + 4);

        g2.setColor(windup
                ? new Color(90, 200, 255, Math.min(170, alpha))
                : new Color(80, 180, 255, Math.min(180, alpha)));
        g2.setStroke(new BasicStroke(windup ? 5f : 7f));
        g2.drawRoundRect(screenX - 3, screenY - 3, hitboxWorld.width + 6, hitboxWorld.height + 6, arc + 10, arc + 10);

        g2.setColor(new Color(255, 255, 255, Math.min(200, alpha)));
        g2.setStroke(new BasicStroke(windup ? 1.5f : 2f));
        g2.drawLine(screenX, screenY, screenX + hitboxWorld.width, screenY + hitboxWorld.height);
        g2.drawLine(screenX + hitboxWorld.width, screenY, screenX, screenY + hitboxWorld.height);

        g2.setComposite(old);
    }

    public Rectangle getHitboxWorld() {
        return new Rectangle(hitboxWorld);
    }
}
