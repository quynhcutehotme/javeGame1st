package Game_2D;

import entity.ChiPheo;
import entity.player;
import tile.tileManager;
import entity.bot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class gamePanel extends JPanel implements Runnable {
    public ChiPheo chiPheo;
    Image cloudImage;
    BackgroundMusic bgMusic;
    BackgroundMusic loseMusic;
    final int orgsSize = 16;
    final int scale = 2;
    public final int tileSize = orgsSize * scale * 2; // = 64

    public int maxColumn = 18;
    public int maxRow = 9;

    public final int width = tileSize * maxColumn;   // 1024
    public final int height = tileSize * maxRow;

    public final int maxWorldCol = 200;
    public final int maxWorldRow = 200;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public BufferedImage backgroundImage;
    int FPS = 60;

    public UI ui = new UI(this);
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int guideState = 2;

    tileManager tileM = new tileManager(this);
    public Camera camera;
    public keyHander keyH = new keyHander(this);
    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public player player = new player(this, keyH);
    public java.util.List<bot> bots = new java.util.ArrayList<>();
    public java.util.List<damageEffect> damageEffects = new java.util.ArrayList<>();

    // PLAYER SETTINGS
    public int playerHp = 3;
    public BufferedImage heartIcon;
    private boolean playerInvincible = false;
    private int invincibleCounter = 0;
    private final int invincibleTime = 60;

    private boolean gameOver = false;
    private boolean showGameOverMenu = false;

    // Constructor
    public gamePanel() {
        bgMusic = new BackgroundMusic("/music/MusicBackground.wav");
        loseMusic = new BackgroundMusic("/music/over_ending.wav");
        bgMusic.playLoop();

        try {
            backgroundImage = ImageIO.read(
                    getClass().getResourceAsStream("/res/ui/chongchay.png")
            );
            if (backgroundImage == null){
                System.out.println("lỗi nền");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setOpaque(true);
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(37, 150, 190));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        this.addMouseListener(new mouseHandler(this));

        gameState = titleState;

        // KHỞI TẠO CAMERA
        camera = new Camera(this);

        getPlayerImage();
        spawnBots();

        try {
            java.net.URL imageUrl = getClass().getResource("/tile/clound1.png");
            if (imageUrl != null) {
                cloudImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("Lỗi: Không tìm thấy tài nguyên /tile/clound1.png.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameState = titleState;
    }

    public void getPlayerImage() {
        heartIcon = setup("chao_hanh");
    }

    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/player/" + imagePath + ".png"));
            image = uTool.scaleImage(image, tileSize, tileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void spawnBots() {
        bots.clear();
        bots.add(new bot(this, tileSize * 10, tileSize * 10));
        bots.add(new bot(this, tileSize * 20, tileSize * 8));
        bots.add(new bot(this, tileSize * 26, tileSize * 14));
        // Vị trí chí phèo
        chiPheo = new ChiPheo(this, tileSize * 17, tileSize * 11 + 15);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null) {
            update();
            repaint();
            try {
                double remaniningTime = nextDrawTime - System.nanoTime();
                if (remaniningTime < 0) remaniningTime = 0;
                Thread.sleep((long) remaniningTime / 1000000);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        if (gameState == titleState || gameState == guideState) {
            return;
        }

        if (gameOver) {
            if (keyH.restartPress) restartGame();
            if (keyH.exitPress) System.exit(0);
            return;
        }

        if (gameState == playState) {
            // CẬP NHẬT CAMERA TRƯỚC
            camera.update();
            player.update();



            if (chiPheo != null) chiPheo.update();

            for (bot b : bots) {
                b.updateAI(player.worldX, player.worldY);
            }

            // Check va chạm Bot
            if (playerInvincible) {
                invincibleCounter++;
                if (invincibleCounter > invincibleTime) {
                    invincibleCounter = 0;
                    playerInvincible = false;
                }
            } else {
                for (bot b : bots) {
                    if (cChecker.entitiesIntersect(player, b)) {
                        playerHp = Math.max(0, playerHp - 1);
                        // Tính toán vị trí màn hình cho damage effect
                        int screenX = camera.worldXToScreenX(player.worldX) + tileSize / 2;
                        int screenY = camera.worldYToScreenY(player.worldY);
                        damageEffects.add(new damageEffect(screenX, screenY, "-1"));
                        playerInvincible = true;
                        break;
                    }
                }
            }

            int playerTileCol = player.worldX / tileSize;
            int playerTileRow = player.worldY / tileSize;

            // Đảm bảo không vượt quá giới hạn map
            if (playerTileCol >= 0 && playerTileCol < maxWorldCol &&
                    playerTileRow >= 0 && playerTileRow < maxWorldRow) {

                int tileIndex = tileM.mapTileNum[playerTileCol][playerTileRow];


                if (tileIndex == 8 && !gameOver) {

                    triggerGameOver();
                }
            }

            damageEffects.removeIf(effect -> {
                effect.update();
                return !effect.isAlive();
            });

            if (playerHp <= 0 && !gameOver) {
                triggerGameOver();
            }
        }
    }


    private void triggerGameOver() {
        gameOver = true;
        showGameOverMenu = true;
        if (bgMusic != null) bgMusic.stop();
        if (loseMusic != null) loseMusic.playOnce();
        System.out.println("Game over menu should be visible now");
    }

    private void restartGame() {
        playerHp = 3;
        playerInvincible = false;
        invincibleCounter = 0;
        player.setDefaultValue();
        spawnBots();
        damageEffects.clear();
        gameOver = false;
        showGameOverMenu = false;

        if (loseMusic != null) loseMusic.stop();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.playLoop();
        }

        // Reset camera về vị trí player
        if (camera != null) {
            camera.centerOnPlayer();
        }

        gameState = playState;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, width, height, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, width, height);
        }

        if (gameState == titleState || gameState == guideState) {
            ui.draw(g2);
        } else {
            // Vẽ tile map với camera offset
            tileM.draw(g2, camera.worldX, camera.worldY);

            // Vẽ bot với camera offset
            for (bot b : bots) {
                int screenX = camera.worldXToScreenX(b.worldX);
                int screenY = camera.worldYToScreenY(b.worldY);
                b.draw(g2, screenX, screenY);
            }

            // Vẽ player với camera offset
            int playerScreenX = camera.worldXToScreenX(player.worldX);
            int playerScreenY = camera.worldYToScreenY(player.worldY);
            player.draw(g2, playerScreenX, playerScreenY);

            // Lưu screen position cho damageEffect


            // Vẽ chí phèo với camera offset
            if (chiPheo != null) {
                int chiPheoScreenX = camera.worldXToScreenX(chiPheo.worldX);
                int chiPheoScreenY = camera.worldYToScreenY(chiPheo.worldY);
                chiPheo.draw(g2, chiPheoScreenX+tileSize*32, chiPheoScreenY-tileSize*2);
            }

            // Vẽ Hiệu ứng damage (đã là screen coordinates)
            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            // Vẽ Máu (HUD)
            drawPlayerLife(g2);

            // Vẽ mây
            if (cloudImage != null) {
                g2.drawImage(cloudImage, 50, 50, 100, 100, this);
                g2.drawImage(cloudImage, 400, 70, 100, 80, this);
                g2.drawImage(cloudImage, 700, 80, 120, 150, this);
                g2.drawImage(cloudImage, 1000, 55, 150, 80, this);
            }

            if (showGameOverMenu) {
                drawGameOverScreen(g2);
            }
        }

        g2.dispose();
    }


    public void drawPlayerLife(Graphics2D g2) {
        if (heartIcon != null) {
            int x = 10;
            int y = 10;
            int i = 0;
            while (i < playerHp) {
                g2.drawImage(heartIcon, x, y, null);
                x += tileSize;
                i++;
            }
        } else {
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
            g2.drawString("HP: " + playerHp, 60, 144);
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);

        int menuWidth = 400;  // Cũ là 400 -> Tăng lên 600
        int menuHeight = 250; // Cũ là 250 -> Tăng lên 350
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;


        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30); // Bo góc tròn hơn (30)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3)); // Viền dày hơn chút
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);


        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f)); // Font size 60
        g2.setColor(Color.RED);
        String loseText = "YOU LOSE";
        int textWidth = g2.getFontMetrics().stringWidth(loseText);
        g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 130);


        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 170);

    }

    public int getScreenX(int worldX) {
        return camera.worldXToScreenX(worldX);
    }

    public int getScreenY(int worldY) {
        return camera.worldYToScreenY(worldY);
    }

}


