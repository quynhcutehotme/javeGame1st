package Game_2D;

import entity.player;
import tile.tileManager;
import entity.bot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class gamePanel extends JPanel implements Runnable {
    Image cloudImage;
    BackgroundMusic bgMusic;
    BackgroundMusic loseMusic;
    
    final int orgsSize = 16;
    final int scale = 2;
    public final int tileSize = orgsSize * scale * 2; // = 64

    public int maxColumn = 18;
    public int maxRow = 9;

    public final int width = tileSize * maxColumn;   // 1152
    public final int height = tileSize * maxRow;     // 576

    public final int maxWorldCol = 200;
    public final int maxWorldRow = 200;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public BufferedImage backgroundImage;
    int FPS = 60;

    // Game states
    public UI ui = new UI(this);
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int guideState = 2;

    tileManager tileM = new tileManager(this);
    public keyHander keyH = new keyHander(this);
    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public player player = new player(this, keyH);
    public java.util.List<bot> bots = new java.util.ArrayList<>();
    public java.util.List<damageEffect> damageEffects = new java.util.ArrayList<>();

    // Player settings
    public int playerHp = 3;
    public BufferedImage heartIcon;
    private boolean playerInvincible = false;
    private int invincibleCounter = 0;
    private final int invincibleTime = 60;

    private boolean gameOver = false;
    private boolean showGameOverMenu = false;

    public gamePanel() {
        // FIXED: Add /res/ prefix to music paths
        bgMusic = new BackgroundMusic("/res/music/MusicBackground.wav");
        loseMusic = new BackgroundMusic("/res/music/over_ending.wav");
        
        if (bgMusic != null && bgMusic.clip != null) {
            bgMusic.playLoop();
        } else {
            System.err.println("WARNING: Background music failed to load");
        }
        
        try {
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/res/map/background.png"));
        } catch (Exception e) {
            System.err.println("WARNING: Background image failed to load");
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(92, 201, 141));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        getPlayerImage();
        spawnBots();
        
        try {
            java.net.URL imageUrl = getClass().getResource("/res/tile/clound1.png");
            if (imageUrl != null) {
                cloudImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("WARNING: Cloud image not found at /res/tile/clound1.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameState = titleState;
    }

    public void getPlayerImage() {
        try {
            heartIcon = setup("chao_hanh");
            if (heartIcon == null) {
                System.err.println("WARNING: Heart icon failed to load");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResourceAsStream("/res/player/" + imagePath + ".png"));
            image = uTool.scaleImage(image, tileSize, tileSize);
        } catch (IOException e) {
            System.err.println("ERROR loading: /res/player/" + imagePath + ".png");
            e.printStackTrace();
        }
        return image;
    }

    private void spawnBots() {
        bots.clear();
        bots.add(new bot(this, tileSize * 10, tileSize * 10));
        bots.add(new bot(this, tileSize * 20, tileSize * 8));
        bots.add(new bot(this, tileSize * 26, tileSize * 14));
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        
        while (gameThread != null) {
            update();
            repaint();
            
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime / 1000000);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // Don't update game if in menu or guide
        if (gameState == titleState || gameState == guideState) {
            return;
        }

        // Handle game over
        if (gameOver) {
            if (keyH.restartPress) restartGame();
            if (keyH.exitPress) System.exit(0);
            return;
        }

        // Update game when playing
        if (gameState == playState) {
            player.update();

            for (bot b : bots) {
                b.updateAI(player.worldX, player.worldY);
            }

            // Check bot collision
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
                        damageEffects.add(new damageEffect(player.screenX + tileSize / 2, player.screenY, "-1"));
                        playerInvincible = true;
                        break;
                    }
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
        
        System.out.println("Game Over!");
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

        gameState = playState;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw menu screens
        if (gameState == titleState || gameState == guideState) {
            ui.draw(g2);
        }
        // Draw game
        else {
            // 1. Background
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, width, height, null);
            } else {
                g2.setColor(new Color(92, 201, 141));
                g2.fillRect(0, 0, width, height);
            }

            // 2. Tiles
            tileM.draw(g2);

            // 3. Bots
            for (bot b : bots) {
                b.draw(g2);
            }

            // 4. Player
            player.draw(g2);

            // 5. Damage effects
            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            // 6. HP display
            drawPlayerLife(g2);

            // 7. Clouds
            if (cloudImage != null) {
                g2.drawImage(cloudImage, 50, 50, 100, 100, this);
                g2.drawImage(cloudImage, 400, 70, 100, 80, this);
                g2.drawImage(cloudImage, 700, 80, 120, 150, this);
                g2.drawImage(cloudImage, 1000, 55, 150, 80, this);
            }

            // 8. Game over menu
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
            for (int i = 0; i < playerHp; i++) {
                g2.drawImage(heartIcon, x, y, null);
                x += tileSize;
            }
        } else {
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
            g2.drawString("HP: " + playerHp, 60, 144);
        }
    }

    public void drawGameOverScreen(Graphics2D g2) {
        // Darken screen
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);

        // FIXED: Larger menu box
        int menuWidth = 600;
        int menuHeight = 350;
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;

        // Draw menu background
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        // Draw "YOU LOSE" text
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        g2.setColor(Color.RED);
        String loseText = "YOU LOSE";
        int textWidth = g2.getFontMetrics().stringWidth(loseText);
        g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 130);

        // Draw instructions
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 170);
    }
}
