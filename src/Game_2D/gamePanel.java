package Game_2D;

import javax.swing.*;
import java.awt.*;

public class gamePanel extends JPanel implements Runnable {
    final int orgsSize = 16;
    final int scale = 2;
    final int tileSize = orgsSize * scale; // = 64

    final int maxColumn = 16;
    final int maxRow = 12;

    final int width = tileSize * maxColumn;   // 64 * 16 = 1024
    final int height = tileSize * maxRow;

    int playerX =100;
    int playerY = 100;
    int speed = 3;

    int FPS = 60;

    keyHander keyH = new keyHander() ;

    Thread gameThread;

    public gamePanel (){
        this.setPreferredSize(new Dimension(width,height));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();

    }

//    public void run(){
//
//        double drawInterval = 1000000000/FPS ;
//        double nextDrawTime = System.nanoTime() + drawInterval;
//        while (gameThread != null){
////            System.out.println("game running");
//            update();
//            repaint();
//
//            try {
//                double remaniningTime = nextDrawTime - System.nanoTime();
//
//                if (remaniningTime < 0) {
//                    remaniningTime = 0;
//                }
//                Thread.sleep((long) remaniningTime / 1000000);
//                nextDrawTime+=drawInterval;
//            }
//            catch (InterruptedException e){
//                e.printStackTrace();
//            }
//            }
//
//        }

public void run() {
    // lấy thời điểm bắt đầu
    double drawInterval = 1000000000.0 / FPS; // thời gian 1 frame lý tưởng (ns)
    double delta = 0;
    long lastTime = System.nanoTime();
    long currentTime;

    while (gameThread != null) {
        currentTime = System.nanoTime();

        // cộng dồn thời gian đã trôi qua
        delta += (currentTime - lastTime) / drawInterval;
        lastTime = currentTime;

        // nếu đã đủ thời gian cho 1 frame
        if (delta >= 1) {
            update();
            repaint();
            delta--;
        }
    }
}

    public void update(){
        if (keyH.upPress == true){
            playerY -= speed ;
    }
        else if (keyH.downPress == true){
            playerY += speed ;
    }
        else if (keyH.leftPress == true){
            playerX -= speed ;
        }
        else if (keyH.rightPress == true){
            playerX += speed ;
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        g2.setColor(Color.white);
        g2.fillRect(playerX,playerY,tileSize,tileSize);
        g2.dispose();

    }

}
