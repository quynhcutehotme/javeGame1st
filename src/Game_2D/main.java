package Game_2D;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setLocationRelativeTo(null);

        gamePanel gPanel = new gamePanel();
        window.add(gPanel);

        window.pack();
        
        window.setTitle("My game");
        window.setVisible(true);

        gPanel.startGameThread();


    }
}
