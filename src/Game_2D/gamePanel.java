package Game_2D;

import entity.bot;
import entity.player;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import tile.tileManager;

public class gamePanel extends JPanel implements Runnable, MouseListener {
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
    public final int winState = 3;   // Trạng thái thắng

    tileManager tileM = new tileManager(this);

    // --- 2. SỬA LẠI KEYHANDLER (QUAN TRỌNG: Phải có 'this') ---
    public keyHander keyH = new keyHander(this);

    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public player player = new player(this, keyH);
    public java.util.List<bot> bots = new java.util.ArrayList<>();
    public java.util.List<damageEffect> damageEffects = new java.util.ArrayList<>();
    private final java.util.List<AttackEffect> attackEffects = new java.util.ArrayList<>();
    private final java.util.List<DeathEffect> deathEffects = new java.util.ArrayList<>();
    private final Random random = new Random();

    // PLAYER SETTINGS
    public int playerHp = 3;
    public BufferedImage heartIcon;
    private boolean playerInvincible = false;
    private int invincibleCounter = 0;
    private final int invincibleTime = 60;

    private boolean gameOver = false;
    private boolean showGameOverMenu = false;
    private boolean gameWon = false;
    private boolean showWinMenu = false;
    
    // Timer for scaling difficulty (no longer used for win condition)
    private long gameStartTime;
    private long currentTimeElapsed = 0;
    private long survivalTimeSeconds = 15; // used for difficulty scaling only
    // Spawn pacing: ramps from slower to faster
    private final long maxSpawnIntervalMs = 1000; // start slower
    private final long minSpawnIntervalMs = 350;  // faster floor
    private final long spawnRampDurationSec = 30; // reach min over 30s
    private long lastBotSpawnTime = System.currentTimeMillis();
    private int botsKilled = 0;
    private final int killsToWin = 5;
    private final int maxBotsOnField = 6;
    
    // Bot speed scaling
    public final int baseBotSpeed = 2; // Initial bot speed
    private final float maxSpeedMultiplier = 1.8f; // keep bots reasonable

    // Attack handling
    private boolean attackRequested = false;
    private int attackCooldownCounter = 0;
    private final int attackCooldownFrames = 10;
    private final int attackWindupFrames = 6;
    private int attackWindupCounter = 0;
    private String queuedAttackDirection = null;
    // Attack range is forward-only; player no longer needs to overlap bot
    private final int attackRange = tileSize * 2; // two tiles reach
    private Rectangle buildAttackHitbox(String direction) {
        // Build a forward-only attack box so player cannot damage while overlapping
        int baseX = player.worldX + player.solidArea.x;
        int baseY = player.worldY + player.solidArea.y;
        int boxWidth = player.solidArea.width;
        int boxHeight = player.solidArea.height;

        Rectangle hitbox;
        switch (direction) {
            case "left":
                hitbox = new Rectangle(baseX - attackRange, baseY, attackRange, boxHeight);
                break;
            case "right":
                hitbox = new Rectangle(baseX + boxWidth, baseY, attackRange, boxHeight);
                break;
            case "up":
                hitbox = new Rectangle(baseX, baseY - attackRange, boxWidth, attackRange);
                break;
            case "down":
                hitbox = new Rectangle(baseX, baseY + boxHeight, boxWidth, attackRange);
                break;
            default:
                hitbox = new Rectangle(baseX + boxWidth, baseY, attackRange, boxHeight);
                break;
        }
        return hitbox;
    }

    // Constructor
    public gamePanel() {
        bgMusic = new BackgroundMusic("/music/MusicBackground.wav");
        loseMusic = new BackgroundMusic("/music/over_ending.wav");
        bgMusic.playLoop();
        try {
            InputStream bgStream = getClass().getResourceAsStream("/res/map/background.png");
            if (bgStream == null) {
                // Try file system fallback
                File bgFile = new File("src/res/map/background.png");
                if (!bgFile.exists()) {
                    bgFile = new File("out/res/map/background.png");
                }
                if (bgFile.exists()) {
                    bgStream = new FileInputStream(bgFile);
                }
            }
            if (bgStream != null) {
                backgroundImage = ImageIO.read(bgStream);
                bgStream.close();
            }
        } catch (Exception e) {
            // Background image is optional, just use solid color if not found
            backgroundImage = null;
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(92, 201, 141));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(this);
        this.setFocusable(true);

        getPlayerImage();
       spawnBots();
        try {
            java.net.URL imageUrl = getClass().getResource("/res/tile/clound1.png");
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
        gameStartTime = 0;
        lastBotSpawnTime = System.currentTimeMillis();

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
        // Spawn different bot types with diverse behaviors
        bots.add(new bot(this, tileSize * 10, tileSize * 10, bot.BotType.GREEN));
        bots.add(new bot(this, tileSize * 20, tileSize * 8, bot.BotType.PURPLE));
        bots.add(new bot(this, tileSize * 26, tileSize * 14, bot.BotType.YELLOW));
    }

    private void spawnBotAtRandomPosition() {
        if (bots.size() >= maxBotsOnField) return;
        int attempts = 30;
        int spawnX = tileSize * 5;
        int spawnY = tileSize * 5;

        // Require some distance from player and avoid blocking tiles/other bots
        int minDistanceSq = (tileSize * 4) * (tileSize * 4);

        for (int i = 0; i < attempts; i++) {
            int col = random.nextInt(Math.max(2, maxWorldCol - 3)) + 1;

            // Find ground at this column: first solid tile from bottom
            int groundRow = -1;
            for (int r = maxWorldRow - 2; r >= 1; r--) {
                int tileNum = tileM.mapTileNum[col][r];
                if (tileM.tile[tileNum].collision) {
                    groundRow = r;
                    break;
                }
            }
            if (groundRow <= 1) continue; // no ground found, skip

            int row = groundRow - 1; // stand on top of ground
            int tileNum = tileM.mapTileNum[col][row];
            if (tileM.tile[tileNum].collision) continue; // space must be free to stand in

            int candidateX = col * tileSize;
            int candidateY = row * tileSize;

            int dx = candidateX - player.worldX;
            int dy = candidateY - player.worldY;
            if (dx * dx + dy * dy <= minDistanceSq) continue;

            // Keep spawns outside the current camera view (with small margin)
            int cameraLeft = player.worldX - player.screenX - tileSize;
            int cameraTop = player.worldY - player.screenY - tileSize;
            int cameraRight = cameraLeft + width + tileSize * 2;
            int cameraBottom = cameraTop + height + tileSize * 2;
            if (candidateX > cameraLeft && candidateX < cameraRight &&
                candidateY > cameraTop && candidateY < cameraBottom) {
                continue;
            }

            Rectangle candidateBox = new Rectangle(candidateX + 8, candidateY + 16, 40, 40);
            boolean overlaps = false;
            for (bot other : bots) {
                Rectangle otherBox = new Rectangle(
                        other.worldX + other.solidArea.x,
                        other.worldY + other.solidArea.y,
                        other.solidArea.width,
                        other.solidArea.height);
                if (candidateBox.intersects(otherBox)) {
                    overlaps = true;
                    break;
                }
            }
            if (overlaps) continue;

            spawnX = candidateX;
            spawnY = candidateY;
            break;
        }

        bot.BotType[] types = bot.BotType.values();
        bot.BotType type = types[random.nextInt(types.length)];
        bots.add(new bot(this, spawnX, spawnY, type));
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

        // Nếu Game Won
        if (gameWon) {
            if (keyH.restartPress) restartGame();
            if (keyH.exitPress) System.exit(0);
            return;
        }

        // Nếu đang chơi (PlayState) thì mới chạy logic dưới đây
        if (gameState == playState) {
            // Initialize game start time on first update
            if (gameStartTime == 0) {
                gameStartTime = System.currentTimeMillis();
            }
            
            // Update survival timer
            long currentTime = System.currentTimeMillis();
            currentTimeElapsed = (currentTime - gameStartTime) / 1000; // Convert to seconds
            
            // Update bot speeds based on elapsed time (gradual increase)
            updateBotSpeeds();
            // Spawn new bots periodically with dynamic interval
            long now = System.currentTimeMillis();
            long currentSpawnInterval = getCurrentSpawnIntervalMs();
            if (now - lastBotSpawnTime >= currentSpawnInterval && bots.size() < maxBotsOnField) {
                spawnBotAtRandomPosition();
                lastBotSpawnTime = now;
            }

            // Handle attack cooldown and queued input
            if (attackCooldownCounter > 0) {
                attackCooldownCounter--;
            }

            // Handle attack wind-up then resolve hit
            if (attackRequested && attackCooldownCounter == 0 && attackWindupCounter == 0) {
                queuedAttackDirection = player.direction;
                attackWindupCounter = attackWindupFrames;
                // Pre-swing visual hint
                Rectangle telegraphHitbox = buildAttackHitbox(queuedAttackDirection);
                attackEffects.add(new AttackEffect(telegraphHitbox, attackWindupFrames, true));
            }
            attackRequested = false;

            if (attackWindupCounter > 0) {
                attackWindupCounter--;
                if (attackWindupCounter == 0 && queuedAttackDirection != null) {
                    performAttack(queuedAttackDirection);
                    attackCooldownCounter = attackCooldownFrames;
                    queuedAttackDirection = null;
                }
            }

            player.update();

            for (bot b : bots) {
                b.updateAI(player.worldX, player.worldY);
            }
            
            // Apply lightweight horizontal collision avoidance after all bots have updated
            bot.avoidClumping(bots);

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
            attackEffects.removeIf(effect -> !effect.update());
            deathEffects.removeIf(effect -> !effect.update());

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
    
    private void triggerGameWin() {
        gameWon = true;
        showWinMenu = true;
        if (bgMusic != null) bgMusic.stop();
        // You can add win music here if you have one
        System.out.println("Game won! Survival time: " + currentTimeElapsed + " seconds");
    }
    
    private void updateBotSpeeds() {
        // Calculate speed multiplier based on elapsed time
        // Speed increases linearly from 1.0x at start to maxSpeedMultiplier at end
        float progress = Math.min(1.0f, (float)currentTimeElapsed / survivalTimeSeconds);
        float speedMultiplier = 1.0f + (maxSpeedMultiplier - 1.0f) * progress;
        
        // Update speed for all bots (yellow bots handle sprint speed separately)
        int newSpeed = Math.round(baseBotSpeed * speedMultiplier);
        for (bot b : bots) {
            // Update base speed, but yellow bots will apply sprint multiplier in their update
            b.speed = newSpeed;
            // Update baseSpeed for yellow bot sprint calculation
            if (b.botType == bot.BotType.YELLOW) {
                b.baseSpeed = newSpeed;
            }
        }
    }

    private long getCurrentSpawnIntervalMs() {
        float progress = Math.min(1.0f, (float) currentTimeElapsed / spawnRampDurationSec);
        return (long) (maxSpawnIntervalMs - (maxSpawnIntervalMs - minSpawnIntervalMs) * progress);
    }

    private void restartGame() {
        playerHp = 3;
        playerInvincible = false;
        invincibleCounter = 0;
        player.setDefaultValue();
        spawnBots();
        lastBotSpawnTime = System.currentTimeMillis();
        botsKilled = 0;
        damageEffects.clear();
        gameOver = false;
        showGameOverMenu = false;
        gameWon = false;
        showWinMenu = false;
        gameStartTime = 0;
        currentTimeElapsed = 0;

        if (loseMusic != null) loseMusic.stop();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.playLoop();
        }

        // Reset lại vào game luôn (hoặc về menu nếu muốn: gameState = titleState)
        gameState = playState;
    }

    public void queueAttack() {
        attackRequested = true;
    }

    private void performAttack(String directionSnapshot) {
        Rectangle hitbox = buildAttackHitbox(directionSnapshot);

        // Add a brief flash effect for the attack area
        attackEffects.add(new AttackEffect(hitbox, 8, false));

        java.util.Iterator<bot> iterator = bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();
            Rectangle botHitbox = new Rectangle(
                    b.worldX + b.solidArea.x,
                    b.worldY + b.solidArea.y,
                    b.solidArea.width,
                    b.solidArea.height
            );
            if (hitbox.intersects(botHitbox)) {
                boolean dead = b.applyDamage(1);
                if (dead) {
                    deathEffects.add(new DeathEffect(b.worldX + b.solidArea.width / 2, b.worldY + b.solidArea.height / 2));
                    iterator.remove();
                    botsKilled++;
                    if (botsKilled >= killsToWin && !gameWon) {
                        triggerGameWin();
                    }
                }
                // Add a floating hit effect at the bot position (screen space)
                int effectScreenX = b.worldX - player.worldX + player.screenX + b.solidArea.width / 2;
                int effectScreenY = b.worldY - player.worldY + player.screenY;
                damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "+1", new Color(50, 200, 50)));
            }
        }
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
                g2.setColor(new Color(92, 201, 141));
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

            // 5. Vẽ hiệu ứng tấn công (flash vùng đánh)
            for (AttackEffect effect : attackEffects) {
                effect.draw(g2, player.worldX, player.worldY, player.screenX, player.screenY);
            }

            // 6. Vẽ hiệu ứng hạ gục bot
            for (DeathEffect effect : deathEffects) {
                effect.draw(g2, player.worldX, player.worldY, player.screenX, player.screenY);
            }

            // 7. Vẽ Hiệu ứng damage
            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            // 8. Vẽ Máu (HUD)
            drawPlayerLife(g2);

            // 8b. Vẽ số bot đã tiêu diệt
            drawKillCounter(g2);
            
            // 9. Vẽ đám mây trang trí (parallax theo camera)
            if (cloudImage != null) {
                drawClouds(g2);
            }

            // 10. Vẽ Menu Game Over (Nếu thua)
            if (showGameOverMenu) {
                drawGameOverScreen(g2);
            }
            
            // 11. Vẽ Menu Win (Nếu thắng)
            if (showWinMenu) {
                drawWinScreen(g2);
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
    
    public void drawWinScreen(Graphics2D g2) {
        // 1. Làm tối màn hình
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);

        // 2. Kích thước khung menu
        int menuWidth = 400;
        int menuHeight = 300;
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;

        // Vẽ nền bảng
        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        // 3. Vẽ chữ "YOU WIN!"
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        g2.setColor(new Color(0, 150, 0)); // Green color for win
        String winText = "YOU WIN!";
        int textWidth = g2.getFontMetrics().stringWidth(winText);
        g2.drawString(winText, menuX + (menuWidth - textWidth) / 2, menuY + 100);

        // 4. Vẽ thời gian sống sót
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24f));
        g2.setColor(Color.BLACK);
        String timeText = "Survival Time: " + currentTimeElapsed + "s";
        int timeWidth = g2.getFontMetrics().stringWidth(timeText);
        g2.drawString(timeText, menuX + (menuWidth - timeWidth) / 2, menuY + 150);

        // 5. Vẽ dòng hướng dẫn
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 200);
    }
    
    public void drawSurvivalTimer(Graphics2D g2) {
        if (gameState == playState && !gameOver && !gameWon) {
            // Calculate remaining time
            long remainingTime = Math.max(0, survivalTimeSeconds - currentTimeElapsed);
            long minutes = remainingTime / 60;
            long seconds = remainingTime % 60;
            
            // Draw timer in top right corner
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
            g2.setColor(Color.WHITE);
            
            // Draw background for better visibility
            String timeText = String.format("Time: %02d:%02d", minutes, seconds);
            int textWidth = g2.getFontMetrics().stringWidth(timeText);
            int textX = width - textWidth - 20;
            int textY = 40;
            
            // Draw shadow/background
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(textX - 10, textY - 25, textWidth + 20, 35, 5, 5);
            
            // Draw text
            g2.setColor(Color.WHITE);
            g2.drawString(timeText, textX, textY);
            
            // Draw target time
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            String targetText = "Target: " + survivalTimeSeconds + "s";
            int targetWidth = g2.getFontMetrics().stringWidth(targetText);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.drawString(targetText, width - targetWidth - 20, textY + 25);
        }
    }

    private void drawKillCounter(Graphics2D g2) {
        if (gameState != playState) return;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        String text = "Killed: " + botsKilled + "/" + killsToWin;

        int padding = 10;
        int x = 10;
        int y = 10 + tileSize + 20; // below hearts

        int textWidth = g2.getFontMetrics().stringWidth(text);
        int textHeight = g2.getFontMetrics().getHeight();

        // Background for readability
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x - padding, y - textHeight, textWidth + padding * 2, textHeight + padding / 2, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawClouds(Graphics2D g2) {
        // Simple parallax clouds that move with the camera
        int camX = player.worldX - player.screenX;
        int camY = player.worldY - player.screenY;
        float parallax = 0.25f; // move slower than camera

        int[][] clouds = new int[][] {
                {50, 200, 120, 90},
                {380, 220, 130, 100},
                {720, 240, 160, 140},
                {1040, 210, 180, 110}
        };

        int wrapW = width + 200;  // allow some overdraw for wrapping
        int wrapH = height + 200;

        for (int[] c : clouds) {
            int baseX = c[0];
            int baseY = c[1];
            int w = c[2];
            int h = c[3];

            int drawX = baseX - (int)(camX * parallax);
            int drawY = baseY - (int)(camY * parallax);

            // Wrap horizontally/vertically so clouds stay present
            drawX = ((drawX % wrapW) + wrapW) % wrapW - 100;
            drawY = ((drawY % wrapH) + wrapH) % wrapH - 100;

            g2.drawImage(cloudImage, drawX, drawY, w, h, this);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && gameState == playState && !gameOver && !gameWon) {
            queueAttack();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    /**
     * Quick flash overlay to show the attack area.
     */
    private static class AttackEffect {
        private final Rectangle hitboxWorld;
        private int life;
        private final boolean windup;

        AttackEffect(Rectangle hitboxWorld, int lifeFrames, boolean windup) {
            this.hitboxWorld = new Rectangle(hitboxWorld);
            this.life = lifeFrames;
            this.windup = windup;
        }

        /**
         * @return false when effect has finished
         */
        boolean update() {
            life--;
            return life > 0;
        }

        void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int playerScreenX, int playerScreenY) {
            int screenX = hitboxWorld.x - playerWorldX + playerScreenX;
            int screenY = hitboxWorld.y - playerWorldY + playerScreenY;
            int alpha = windup
                    ? Math.max(25, Math.min(160, life * 22))
                    : Math.max(40, Math.min(200, life * 25)); // brighter and punchier

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

            // Vivid multi-layer flash; cooler palette for windup, hotter for impact
            int arc = Math.min(hitboxWorld.width, hitboxWorld.height) / 3;

            // Inner fill
            g2.setColor(windup
                    ? new Color(180, 220, 255, Math.min(200, alpha))
                    : new Color(255, 235, 130, Math.min(220, alpha)));
            g2.fillRoundRect(screenX, screenY, hitboxWorld.width, hitboxWorld.height, arc, arc);

            // Middle stroke (hot)
            g2.setColor(windup
                    ? new Color(120, 180, 255, Math.min(210, alpha))
                    : new Color(255, 120, 60, Math.min(230, alpha)));
            g2.setStroke(new BasicStroke(windup ? 3f : 4f));
            g2.drawRoundRect(screenX - 1, screenY - 1, hitboxWorld.width + 2, hitboxWorld.height + 2, arc + 4, arc + 4);

            // Outer stroke (cool contrast)
            g2.setColor(windup
                    ? new Color(90, 200, 255, Math.min(170, alpha))
                    : new Color(80, 180, 255, Math.min(180, alpha)));
            g2.setStroke(new BasicStroke(windup ? 5f : 7f));
            g2.drawRoundRect(screenX - 3, screenY - 3, hitboxWorld.width + 6, hitboxWorld.height + 6, arc + 10, arc + 10);

            // Spark line accent
            g2.setColor(new Color(255, 255, 255, Math.min(200, alpha)));
            g2.setStroke(new BasicStroke(windup ? 1.5f : 2f));
            g2.drawLine(screenX, screenY, screenX + hitboxWorld.width, screenY + hitboxWorld.height);
            g2.drawLine(screenX + hitboxWorld.width, screenY, screenX, screenY + hitboxWorld.height);

            g2.setComposite(old);
        }
    }

    /**
     * Simple radial burst when a bot is eliminated.
     */
    private static class DeathEffect {
        private final int worldX;
        private final int worldY;
        private int life = 18; // longer for visibility

        DeathEffect(int worldX, int worldY) {
            this.worldX = worldX;
            this.worldY = worldY;
        }

        boolean update() {
            life--;
            return life > 0;
        }

        void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int playerScreenX, int playerScreenY) {
            int screenX = worldX - playerWorldX + playerScreenX;
            int screenY = worldY - playerWorldY + playerScreenY;

            float progress = 1f - (life / 18f);
            int radius = (int)(10 + 44 * progress);
            int alpha = Math.max(80, 230 - (int)(progress * 230));

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

            // Outer ring (thicker)
            g2.setColor(new Color(255, 70, 70, alpha));
            g2.setStroke(new BasicStroke(5f));
            g2.drawOval(screenX - radius, screenY - radius, radius * 2, radius * 2);

            // Inner ring
            g2.setColor(new Color(255, 140, 140, Math.max(90, alpha - 30)));
            g2.setStroke(new BasicStroke(3f));
            g2.drawOval(screenX - radius / 2, screenY - radius / 2, radius, radius);

            // Core fill glow
            int coreSize = 18;
            g2.setColor(new Color(255, 200, 200, Math.min(240, alpha + 40)));
            g2.fillOval(screenX - coreSize / 2, screenY - coreSize / 2, coreSize, coreSize);

            // Cross flash
            g2.setColor(new Color(255, 255, 255, Math.min(220, alpha)));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(screenX - radius, screenY, screenX + radius, screenY);
            g2.drawLine(screenX, screenY - radius, screenX, screenY + radius);

            g2.setComposite(old);
        }
    }
}
