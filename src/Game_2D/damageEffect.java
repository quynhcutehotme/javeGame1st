package Game_2D;

import java.awt.*;

public class damageEffect {
    private int x, y;
    private String text;
    private int life;
    private int maxLife;
    private Color color;
    private float alpha;
    private int offsetY; // Để di chuyển hiệu ứng lên trên

    public damageEffect(int x, int y, String text) {
        this(x, y, text, Color.RED);
    }

    public damageEffect(int x, int y, String text, Color color) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.maxLife = 60; // frames to display
        this.life = maxLife;
        this.color = color;
        this.alpha = 1.0f;
        this.offsetY = 0;
    }

    public void update() {
        life--;
        if (life > 0) {
            // Di chuyển lên trên
            offsetY -= 2;
            // Move upward
            y -= 2;
            // Fade out
            alpha = (float) life / maxLife;
        }
    }

    public boolean isAlive() {
        return life > 0;
    }

    public void draw(Graphics2D g2) {
        if (life <= 0) return;

        // Enable alpha blending
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Set font and color
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        g2.setColor(color);

        // Draw text with outline for better visibility
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 1, y + offsetY + 1); // Sửa: y + offsetY

        g2.setColor(color);
        g2.drawString(text, x, y + offsetY); // Sửa: y + offsetY

        // Reset composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

}