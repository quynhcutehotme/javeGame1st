package Game_2D;

import entity.ChiPheo;
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
    public final int winState = 3;

    tileManager tileM = new tileManager(this);
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
    
    // Timer for scaling difficulty
    private long gameStartTime;
    private long currentTimeElapsed = 0;
    private long survivalTimeSeconds = 15;
    
    // THÊM BIẾN MỚI để tracking spawn ban đầu
    private int initialBotsSpawned = 0;
    private final int maxInitialBots = 3;
    private long lastInitialBotSpawnTime = 0;
    private final long initialSpawnInterval = 3500; // 3.5 giây
    
    // Spawn pacing
    private final long maxSpawnIntervalMs = 5000;
    private final long minSpawnIntervalMs = 3500;
    private final long spawnRampDurationSec = 30;
    private long lastBotSpawnTime = System.currentTimeMillis();
    private int botsKilled = 0;
    private final int killsToWin = 5;
    private final int maxBotsOnField = 6;
    
    // Bot speed scaling
    public final int baseBotSpeed = 3;
    private final float maxSpeedMultiplier = 2.2f;

    // Attack handling (giữ lại cho tùy chọn)
    private boolean attackRequested = false;
    private int attackCooldownCounter = 0;
    private final int attackCooldownFrames = 10;
    private final int attackWindupFrames = 6;
    private int attackWindupCounter = 0;
    private String queuedAttackDirection = null;
    private final int attackRange = tileSize * 2;

    // BOT SPAWN SETTINGS - NHƯ DINO GAME
    private final int spawnYPosition = player.worldY  + tileSize * 1;
    private final int spawnXPosition = -tileSize * 2; // Bên ngoài màn hình bên trái
    private final int offscreenDespawnX = width + tileSize * 2; // Bên ngoài màn hình bên phải
    
    // STOMP MECHANISM VARIABLES
    private boolean stompActive = false;
    private int stompDamage = 1;
    private final int stompBounceForce = 7; // Lực bật lại khi đạp trúng
    
    private Rectangle buildAttackHitbox(String direction) {
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
            backgroundImage = null;
        }

        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(new Color(92, 201, 141));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(this);
        this.setFocusable(true);
        
        // Thêm focus listener để reset keys
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                keyH.resetAllKeys();
            }
        });

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

        gameState = titleState;
        gameStartTime = 0;
        lastBotSpawnTime = System.currentTimeMillis();
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

    // SỬA LẠI: Chỉ spawn ChiPheo, không spawn bot nào
    private void spawnBots() {
        bots.clear();
        
        // Chỉ spawn ChiPheo ở vị trí phía dưới player
        spawnChiPheo();
        
        // Reset tracking variables
        initialBotsSpawned = 0;
        lastInitialBotSpawnTime = 0;
    }

    // THÊM MỚI: Spawn ChiPheo ở vị trí riêng
    private void spawnChiPheo() {
        int chiPheoSpawnX = player.worldX;
        int chiPheoSpawnY = player.worldY + tileSize * 5;
        
        // Đảm bảo trong giới hạn map
        chiPheoSpawnX = Math.max(tileSize, Math.min(chiPheoSpawnX, worldWidth - tileSize * 2));
        chiPheoSpawnY = Math.max(tileSize, Math.min(chiPheoSpawnY, worldHeight - tileSize * 2));
        
        chiPheo = new ChiPheo(this, chiPheoSpawnX, chiPheoSpawnY);
    }

    // THÊM MỚI: Phương thức spawn bot ban đầu từng con
    private void spawnInitialBot() {
        if (initialBotsSpawned >= maxInitialBots) return;
        
        bot.BotType type;
        int index = initialBotsSpawned;
        
        switch (index) {
            case 0:
                type = bot.BotType.GREEN;
                break;
            case 1:
                type = bot.BotType.PURPLE;
                break;
            case 2:
                type = bot.BotType.YELLOW;
                break;
            default:
                return;
        }
        
        // SPAWN TỪ BÊN TRÁI SANG, ĐỘ CAO CỐ ĐỊNH - NHƯ DINO GAME
        int spawnX = spawnXPosition;
        int spawnY = spawnYPosition;
        
        bot newBot = new bot(this, spawnX, spawnY, type);
        
        // Đặt hướng di chuyển sang phải
        newBot.direction = "right";
        newBot.isMovingRight = true;
        
        bots.add(newBot);
        initialBotsSpawned++;
        lastInitialBotSpawnTime = System.currentTimeMillis();
    }

    // THÊM MỚI: Spawn bot từ bên trái sang phải - NHƯ DINO GAME
    private void spawnBotAtRandomPosition() {
        if (bots.size() >= maxBotsOnField) return;
        
        // SPAWN TỪ BÊN TRÁI SANG
        int spawnX = spawnXPosition;
        
        // SỬA Ở ĐÂY: CÙNG ĐỘ CAO với 3 con đầu
        int baseSpawnY = spawnYPosition;
        
        // Chọn loại bot ngẫu nhiên
        bot.BotType[] types = bot.BotType.values();
        bot.BotType type = types[random.nextInt(types.length)];
        
        bot newBot = new bot(this, spawnX, baseSpawnY, type);
        
        // Đặt hướng di chuyển sang phải
        newBot.direction = "right";
        newBot.isMovingRight = true;
        
        bots.add(newBot);
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

    // PHƯƠNG THỨC MỚI: Xử lý đạp bot (stomp)
    private void handleStompMechanic() {
        // Chỉ kiểm tra khi player đang rơi xuống (velocityY > 0)
        if (!player.isJumping || player.velocityY <= 0) {
            stompActive = false;
            return;
        }
        
        // Tạo vùng dưới chân player để kiểm tra đạp
        Rectangle stompArea = new Rectangle(
            player.worldX + player.solidArea.x,
            player.worldY + player.solidArea.y + player.solidArea.height - 10,
            player.solidArea.width,
            15
        );
        
        java.util.Iterator<bot> iterator = bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();
            
            // Tạo vùng trên đầu bot
            Rectangle botHeadArea = new Rectangle(
                b.worldX + b.solidArea.x,
                b.worldY + b.solidArea.y - 5,
                b.solidArea.width,
                10
            );
            
            if (stompArea.intersects(botHeadArea)) {
                // ĐẠP TRÚNG ĐẦU BOT!
                stompActive = true;
                
                // Gây sát thương
                boolean dead = b.applyDamage(stompDamage);
                
                if (dead) {
                    // Thêm hiệu ứng chết
                    deathEffects.add(new DeathEffect(
                        b.worldX + b.solidArea.width / 2, 
                        b.worldY + b.solidArea.height / 2
                    ));
                    
                    // Xóa bot
                    iterator.remove();
                    botsKilled++;
                    
                    // Hiệu ứng điểm
                    int effectScreenX = b.worldX - player.worldX + player.screenX + b.solidArea.width / 2;
                    int effectScreenY = b.worldY - player.worldY + player.screenY;
                    damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "STOMP!", new Color(255, 200, 0)));
                    
                    // Kiểm tra thắng
                    if (botsKilled >= killsToWin && !gameWon) {
                        triggerGameWin();
                    }
                }
                
                // BẬT PLAYER LÊN KHI ĐẠP TRÚNG
                player.velocityY = -stompBounceForce; // Âm vì đi lên
                player.isGrounded = false;
                
                // Thêm hiệu ứng visual
                attackEffects.add(new AttackEffect(stompArea, 10, false));
                
                break; // Chỉ đạp 1 bot mỗi lần
            }
        }
    }
    
    // PHƯƠNG THỨC MỚI: Kiểm tra va chạm thông thường (tránh mất máu khi đang đạp)
    private void checkPlayerBotCollision() {
        if (playerInvincible) {
            invincibleCounter++;
            if (invincibleCounter > invincibleTime) {
                invincibleCounter = 0;
                playerInvincible = false;
            }
            return;
        }
        
        // Nếu đang đạp bot thì không mất máu
        if (stompActive) {
            return;
        }
        
        // Kiểm tra va chạm với từng bot
        for (bot b : bots) {
            if (cChecker.entitiesIntersect(player, b)) {
                // Kiểm tra xem có phải va chạm từ trên xuống không (có thể là đạp)
                if (player.isJumping && player.velocityY > 0) {
                    // Player đang rơi xuống, có thể là đạp
                    // Kiểm tra xem player có ở trên bot không
                    Rectangle playerBottom = new Rectangle(
                        player.worldX + player.solidArea.x,
                        player.worldY + player.solidArea.y + player.solidArea.height - 5,
                        player.solidArea.width,
                        10
                    );
                    
                    Rectangle botTop = new Rectangle(
                        b.worldX + b.solidArea.x,
                        b.worldY + b.solidArea.y - 10,
                        b.solidArea.width,
                        15
                    );
                    
                    if (playerBottom.intersects(botTop)) {
                        // Đây là đạp, không mất máu
                        continue;
                    }
                }
                
                // Va chạm thông thường - mất máu
                playerHp = Math.max(0, playerHp - 1);
                damageEffects.add(new damageEffect(player.screenX + tileSize / 2, player.screenY, "-1"));
                playerInvincible = true;
                invincibleCounter = 0;
                
                // Knockback effect
                int knockbackDirection = (b.worldX < player.worldX) ? 1 : -1;
                player.worldX += knockbackDirection * player.speed * 3;
                
                break;
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

    if (gameWon) {
        if (keyH.restartPress) restartGame();
        if (keyH.exitPress) System.exit(0);
        return;
    }

    if (gameState == playState) {
        // Initialize game start time on first update
        if (gameStartTime == 0) {
            gameStartTime = System.currentTimeMillis();
            lastInitialBotSpawnTime = System.currentTimeMillis();
            lastBotSpawnTime = System.currentTimeMillis(); // Reset luôn
        }
        
        long currentTime = System.currentTimeMillis();
        
        // SPAWN BOT BAN ĐẦU TỪNG CON - NHƯ DINO GAME
        if (initialBotsSpawned < maxInitialBots) {
            // Spawn bot đầu tiên ngay lập tức
            if (initialBotsSpawned == 0) {
                spawnInitialBot();
            } 
            // Spawn các bot tiếp theo sau mỗi 3.5 giây
            else if (currentTime - lastInitialBotSpawnTime >= initialSpawnInterval) {
                spawnInitialBot();
                
                // QUAN TRỌNG: KHI SPAWN BOT CUỐI CÙNG, RESET THỜI GIAN SPAWN BOT THƯỜNG
                if (initialBotsSpawned >= maxInitialBots) {
                    lastBotSpawnTime = System.currentTimeMillis();
                }
            }
        }
        
        // Update survival timer - CHUYỂN RA NGOÀI ĐIỀU KIỆN
        currentTimeElapsed = (currentTime - gameStartTime) / 1000;
        
        // Update bot speeds - CHUYỂN RA NGOÀI ĐIỀU KIỆN
        updateBotSpeeds();
        
        // Spawn new bots (CHỈ sau khi đã spawn đủ 3 bot ban đầu)
        // THÊM ĐIỀU KIỆN: Phải đợi thêm ít nhất 1 giây sau khi spawn bot cuối cùng
        long now = System.currentTimeMillis();
        long currentSpawnInterval = getCurrentSpawnIntervalMs();
        
        // Tính thời gian kể từ khi spawn bot cuối cùng ban đầu
        long timeSinceLastInitialBot = now - lastInitialBotSpawnTime;
        long minDelayAfterLastInitialBot = 2000; // Đợi 2 giây sau bot cuối cùng
        
        if (initialBotsSpawned >= maxInitialBots && 
            timeSinceLastInitialBot >= minDelayAfterLastInitialBot &&
            now - lastBotSpawnTime >= currentSpawnInterval && 
            bots.size() < maxBotsOnField) {
            spawnBotAtRandomPosition();
            lastBotSpawnTime = now;
        }

        // Handle attack cooldown (giữ lại cho tùy chọn)
        if (attackCooldownCounter > 0) {
            attackCooldownCounter--;
        }

        if (attackRequested && attackCooldownCounter == 0 && attackWindupCounter == 0) {
            queuedAttackDirection = player.direction;
            attackWindupCounter = attackWindupFrames;
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
        if (chiPheo != null) chiPheo.update();

        // XỬ LÝ ĐẠP BOT - THÊM DÒNG NÀY
        handleStompMechanic();
        
        // UPDATE BOTS - DI CHUYỂN SANG PHẢI NHƯ DINO GAME
        for (bot b : bots) {
            b.updateAI(player.worldX, player.worldY);
        }
        
        // Xóa bots đã đi ra khỏi màn hình bên phải
        java.util.Iterator<bot> iterator = bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();
            if (b.worldX > offscreenDespawnX) {
                iterator.remove();
            }
        }

        // KIỂM TRA VA CHẠM PLAYER-BOT (đã sửa)
        checkPlayerBotCollision();

        // Update effects
        damageEffects.removeIf(effect -> {
            effect.update();
            return !effect.isAlive();
        });
        attackEffects.removeIf(effect -> !effect.update());
        deathEffects.removeIf(effect -> !effect.update());

        // Kiểm tra game over
        if (playerHp <= 0 && !gameOver) {
            triggerGameOver();
        }
    }
}
    
    private void updateBotSpeeds() {
        float progress = Math.min(1.0f, (float)currentTimeElapsed / survivalTimeSeconds);
        float speedMultiplier = 1.0f + (maxSpeedMultiplier - 1.0f) * progress;
        
        int newSpeed = Math.round(baseBotSpeed * speedMultiplier);
        for (bot b : bots) {
            b.speed = newSpeed;
            if (b.botType == bot.BotType.YELLOW) {
                b.baseSpeed = newSpeed;
            }
        }
    }

    private long getCurrentSpawnIntervalMs() {
        float progress = Math.min(1.0f, (float) currentTimeElapsed / spawnRampDurationSec);
        return (long) (maxSpawnIntervalMs - (maxSpawnIntervalMs - minSpawnIntervalMs) * progress);
    }

    // PUBLIC METHODS FOR EXTERNAL ACCESS
    public void performRestart() {
        restartGame();
    }
    
    public void performGameOver() {
        triggerGameOver();
    }
    
    public void performGameWin() {
        triggerGameWin();
    }
    
    public boolean isGameOverMenuVisible() {
        return showGameOverMenu;
    }
    
    public boolean isWinMenuVisible() {
        return showWinMenu;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public boolean isGameWon() {
        return gameWon;
    }
    
    public int getCurrentGameState() {
        return gameState;
    }
    
    public void setGameState(int state) {
        gameState = state;
    }
    
    public int getKillsToWin() {
        return killsToWin;
    }
    
    public int getBotsKilled() {
        return botsKilled;
    }
    
    public long getSurvivalTime() {
        return currentTimeElapsed;
    }
    
    // PRIVATE METHODS (keep them private)
    private void restartGame() {
        playerHp = 3;
        playerInvincible = false;
        invincibleCounter = 0;
        player.setDefaultValue();
        keyH.resetAllKeys(); // RESET KEYS
        spawnBots();
        lastBotSpawnTime = System.currentTimeMillis();
        botsKilled = 0;
        damageEffects.clear();
        attackEffects.clear();
        deathEffects.clear();
        gameOver = false;
        showGameOverMenu = false;
        gameWon = false;
        showWinMenu = false;
        gameStartTime = 0;
        currentTimeElapsed = 0;
        stompActive = false;
        
        // RESET BIẾN TRACKING SPAWN BAN ĐẦU
        initialBotsSpawned = 0;
        lastInitialBotSpawnTime = 0;

        if (loseMusic != null) loseMusic.stop();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.playLoop();
        }

        gameState = playState;
    }

    public void queueAttack() {
        attackRequested = true;
    }

    private void performAttack(String directionSnapshot) {
        Rectangle hitbox = buildAttackHitbox(directionSnapshot);
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
                int effectScreenX = b.worldX - player.worldX + player.screenX + b.solidArea.width / 2;
                int effectScreenY = b.worldY - player.worldY + player.screenY;
                damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "+1", new Color(50, 200, 50)));
            }
        }
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
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, width, height, null);
            } else {
                g2.setColor(new Color(92, 201, 141));
                g2.fillRect(0, 0, width, height);
            }

            tileM.draw(g2);

            for (bot b : bots) {
                b.draw(g2);
            }

            player.draw(g2);

            for (AttackEffect effect : attackEffects) {
                effect.draw(g2, player.worldX, player.worldY, player.screenX, player.screenY);
            }

            for (DeathEffect effect : deathEffects) {
                effect.draw(g2, player.worldX, player.worldY, player.screenX, player.screenY);
            }

            if (chiPheo != null) {
                chiPheo.draw(g2);
            }

            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            drawPlayerLife(g2);
            drawKillCounter(g2);
            
            // Vẽ hướng dẫn stomp
            // drawStompInstruction(g2);
            
            if (cloudImage != null) {
                drawClouds(g2);
            }

            if (showGameOverMenu) {
                drawGameOverScreen(g2);
            }
            
            if (showWinMenu) {
                drawWinScreen(g2);
            }
        }

        g2.dispose();
    }
    
    // PHƯƠNG THỨC MỚI: Vẽ hướng dẫn stomp
    // private void drawStompInstruction(Graphics2D g2) {
//     if (gameState != playState) return;
//     
//     g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
//     String text = "JUMP on bots to STOMP them! (SPACE/W/↑)";
//     
//     int padding = 8;
//     int x = width / 2 - 150;
//     int y = height - 30;
//     
//     int textWidth = g2.getFontMetrics().stringWidth(text);
//     int textHeight = g2.getFontMetrics().getHeight();
//     
//     g2.setColor(new Color(0, 0, 0, 140));
//     g2.fillRoundRect(x - padding, y - textHeight, textWidth + padding * 2, textHeight + padding, 5, 5);
//     
//     g2.setColor(Color.WHITE);
//     g2.drawString(text, x, y);
// }

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

        int menuWidth = 400;
        int menuHeight = 250;
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
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
    
    public void drawWinScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);

        int menuWidth = 400;
        int menuHeight = 300;
        int menuX = (width - menuWidth) / 2;
        int menuY = (height - menuHeight) / 2;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
        g2.setColor(new Color(0, 150, 0));
        String winText = "YOU WIN!";
        int textWidth = g2.getFontMetrics().stringWidth(winText);
        g2.drawString(winText, menuX + (menuWidth - textWidth) / 2, menuY + 100);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24f));
        g2.setColor(Color.BLACK);
        String timeText = "Survival Time: " + currentTimeElapsed + "s";
        int timeWidth = g2.getFontMetrics().stringWidth(timeText);
        g2.drawString(timeText, menuX + (menuWidth - timeWidth) / 2, menuY + 150);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        g2.setColor(Color.BLACK);
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 200);
    }
    
    private void drawKillCounter(Graphics2D g2) {
        if (gameState != playState) return;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
        String text = "Killed: " + botsKilled + "/" + killsToWin;

        int padding = 10;
        int x = 10;
        int y = 10 + tileSize + 20;

        int textWidth = g2.getFontMetrics().stringWidth(text);
        int textHeight = g2.getFontMetrics().getHeight();

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(x - padding, y - textHeight, textWidth + padding * 2, textHeight + padding / 2, 8, 8);

        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
    }

    private void drawClouds(Graphics2D g2) {
        int camX = player.worldX - player.screenX;
        int camY = player.worldY - player.screenY;
        float parallax = 0.25f;

        int[][] clouds = new int[][] {
                {50, 200, 120, 90},
                {380, 220, 130, 100},
                {720, 240, 160, 140},
                {1040, 210, 180, 110}
        };

        int wrapW = width + 200;
        int wrapH = height + 200;

        for (int[] c : clouds) {
            int baseX = c[0];
            int baseY = c[1];
            int w = c[2];
            int h = c[3];

            int drawX = baseX - (int)(camX * parallax);
            int drawY = baseY - (int)(camY * parallax);

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

    public void triggerGameOver() {
        gameOver = true;
        showGameOverMenu = true;
        if (bgMusic != null) bgMusic.stop();
        if (loseMusic != null) loseMusic.playOnce();
        System.out.println("Game over menu should be visible now");
    }
    
    private void triggerGameWin() {
        gameWon = true;
        showWinMenu = true;
        gameState = winState;
        if (bgMusic != null) bgMusic.stop();
        System.out.println("Game won! Survival time: " + currentTimeElapsed + " seconds");
    }

    private static class AttackEffect {
        private final Rectangle hitboxWorld;
        private int life;
        private final boolean windup;

        AttackEffect(Rectangle hitboxWorld, int lifeFrames, boolean windup) {
            this.hitboxWorld = new Rectangle(hitboxWorld);
            this.life = lifeFrames;
            this.windup = windup;
        }

        boolean update() {
            life--;
            return life > 0;
        }

        void draw(Graphics2D g2, int playerWorldX, int playerWorldY, int playerScreenX, int playerScreenY) {
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
    }

    private static class DeathEffect {
        private final int worldX;
        private final int worldY;
        private int life = 18;

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
}