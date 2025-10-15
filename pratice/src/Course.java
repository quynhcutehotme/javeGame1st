import javax.swing.*;
import java.awt.*;

public class Course {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Smiley Face");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new SmileyPanel());
            frame.setSize(400, 420);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

class SmileyPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Face diameter (chiếm 80% kích thước nhỏ hơn)
        int diameter = (int) (Math.min(w, h) * 0.8);
        int cx = w / 2;
        int cy = h / 2;
        int faceX = cx - diameter / 2;
        int faceY = cy - diameter / 2;

        // Face (yellow circle)
        g2.setColor(new Color(0xFFEA00)); // vàng
        g2.fillOval(faceX, faceY, diameter, diameter);

        // Face border
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(faceX, faceY, diameter, diameter);

        // Eyes
        int eyeW = diameter / 8;
        int eyeH = diameter / 8;
        int eyeYOffset = diameter / 8;
        int eyeXOffset = diameter / 6;
        // left eye
        g2.fillOval(cx - eyeXOffset - eyeW/2, cy - eyeYOffset - eyeH/2, eyeW, eyeH);
        // right eye
        g2.fillOval(cx + eyeXOffset - eyeW/2, cy - eyeYOffset - eyeH/2, eyeW, eyeH);

        // Mouth (arc) - smile
        int mouthW = (int)(diameter * 0.6);
        int mouthH = (int)(diameter * 0.35);
        int mouthX = cx - mouthW/2;
        int mouthY = cy; // vị trí miệng bắt đầu từ giữa mặt
        g2.setStroke(new BasicStroke(diameter / 25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(mouthX, mouthY, mouthW, mouthH, 200, 140);

        // Optional: rosy cheeks
        int cheekW = diameter / 7;
        int cheekH = diameter / 12;
        g2.setColor(new Color(0xFF6B6B, true));
        g2.fillOval(cx - mouthW/2 - cheekW/4, cy - cheekH/2, cheekW, cheekH);
        g2.fillOval(cx + mouthW/2 - cheekW + cheekW/4, cy - cheekH/2, cheekW, cheekH);

        g2.dispose();
    }
}
