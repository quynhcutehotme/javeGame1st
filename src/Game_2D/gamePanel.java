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

    public final int width = tileSize * maxColumn;   // 1024
    public final int height = tileSize * maxRow;

    public final int maxWorldCol = 200;
    public final int maxWorldRow = 200;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public BufferedImage backgroundImage;
    int FPS = 60;

    // --- 1. THÊM UI VÀ TRẠNG THÁI GAME ---
    public UI ui = new UI(this); // Khởi tạo giao diện Menu
    public int gameState;
    public final int titleState = 0; // Trạng thái ở Lobby
    public final int playState = 1;  // Trạng thái đang chơi
    public final int guideState = 2; // Trạng thái xem hướng dẫn

    tileManager tileM = new tileManager(this);

    // --- 2. SỬA LẠI KEYHANDLER (QUAN TRỌNG: Phải có 'this') ---
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
            backgroundImage = ImageIO.read(getClass().getResourceAsStream("/res/map/background.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(37, 150, 190));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

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

        // --- 3. THIẾT LẬP TRẠNG THÁI BAN ĐẦU LÀ MENU ---
        gameState = titleState;

    }

    public void getPlayerImage() {
        heartIcon = setup("chao_hanh"); // Đảm bảo bạn có ảnh chao_hanh.png trong folder player
    }

    public BufferedImage setup(String imagePath) {
        utiltityTool uTool = new utiltityTool();
        BufferedImage image = null;
        try {
            // Lưu ý: Đảm bảo đường dẫn ảnh đúng
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
        // --- 4. LOGIC UPDATE DỰA TRÊN TRẠNG THÁI ---

        // Nếu đang ở Lobby hoặc Guide thì KHÔNG update game (nhân vật đứng yên)
        if (gameState == titleState || gameState == guideState) {
            return;
        }

        // Nếu Game Over
        if (gameOver) {
            if (keyH.restartPress) restartGame();
            if (keyH.exitPress) System.exit(0);
            return;
        }

        // Nếu đang chơi (PlayState) thì mới chạy logic dưới đây
        if (gameState == playState) {
            player.update();

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
        // ★ PHÁT NHẠC THUA (1 lần)
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

        // Reset lại vào game luôn (hoặc về menu nếu muốn: gameState = titleState)
        gameState = playState;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // --- 5. LOGIC VẼ MÀN HÌNH ---

        // A. NẾU ĐANG Ở MENU (Lobby/Guide)
        if (gameState == titleState || gameState == guideState) {
            ui.draw(g2); // Chỉ vẽ UI Menu
        }

        // B. NẾU ĐANG CHƠI
        else {
            // 1. Vẽ Background
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, width, height, null);
            } else {
                g2.setColor(new Color(37, 150, 190));
                g2.fillRect(0, 0, width, height);
            }

            // 2. Vẽ Map
            tileM.draw(g2);

            // 3. Vẽ Bot
            for (bot b : bots) {
                b.draw(g2);
            }

            // 4. Vẽ Player
            player.draw(g2);

            // 5. Vẽ Hiệu ứng damage
            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            // 6. Vẽ Máu (HUD)
            drawPlayerLife(g2);

            if (cloudImage != null) {
                g2.drawImage(cloudImage, 50, 50, 100, 100, this);
                g2.drawImage(cloudImage, 400, 70, 100, 80, this);
                g2.drawImage(cloudImage, 700, 80, 120, 150, this);
                g2.drawImage(cloudImage, 1000, 55, 150, 80, this);

            }

            // 7. Vẽ Menu Game Over (Nếu thua)
            if (showGameOverMenu) {
                drawGameOverScreen(g2);
            }
        }
//Check collusion
//        g2.setColor(Color.RED); // màu đỏ để dễ nhìn
//        g2.drawRect(
//                player.screenX + player.solidArea.x,
//                player.screenY + player.solidArea.y,
//                player.solidArea.width,
//                player.solidArea.height
//        );

        g2.dispose();
    }

    // Tách hàm vẽ máu cho gọn
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
        // 1. Làm tối màn hình
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);

        // 2. --- SỬA: TĂNG KÍCH THƯỚC KHUNG MENU ---
        int menuWidth = 400;  // Cũ là 400 -> Tăng lên 600
        int menuHeight = 250; // Cũ là 250 -> Tăng lên 350
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;

        // Vẽ nền bảng
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30); // Bo góc tròn hơn (30)
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3)); // Viền dày hơn chút
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        // 3. Vẽ chữ "YOU LOSE" (Cho to hơn nữa)
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f)); // Font size 60
        g2.setColor(Color.RED);
        String loseText = "YOU LOSE";
        int textWidth = g2.getFontMetrics().stringWidth(loseText);
        g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 130);

        // 4. Vẽ dòng hướng dẫn
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 170);

    }
}
