package Game_2D;

import entity.ChiPheo;
import entity.House;
import entity.bot;
import entity.player;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import tile.tileManager;

public class gamePanel extends JPanel implements Runnable, MouseListener {

    public ChiPheo chiPheo;
    public House house;
    Image cloudImage;
    BackgroundMusic bgMusic;
    BackgroundMusic loseMusic;
    BackgroundMusic winMusic;

    // Camera từ gamePanel thứ hai
    public Camera camera;

    final int orgsSize = 16;
    final int scale = 2;
    public final int tileSize = orgsSize * scale * 2; // = 64

    public int maxColumn = 18;
    public int maxRow = 9;

    public final int width = tileSize * maxColumn;   // 1024
    public final int height = tileSize * maxRow;

    // Kích thước map thực tế
    public final int maxWorldCol = 200;
    public final int maxWorldRow = 200;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    public BufferedImage backgroundImage;
    int FPS = 60;

    private long winStartTime = 0; // Thời điểm bắt đầu thắng
    private boolean winBoardShown = false;

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

    // tracking spawn ban đầu
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

    // Attack handling
    private boolean attackRequested = false;
    private int attackCooldownCounter = 0;
    private final int attackCooldownFrames = 10;
    private final int attackWindupFrames = 6;
    private int attackWindupCounter = 0;
    private String queuedAttackDirection = null;
    private final int attackRange = tileSize * 2;

    // BOT SPAWN SETTINGS - NHƯ DINO GAME
    private final int spawnXPosition = -tileSize * 2; // start a bit before the map
    private final int offscreenDespawnX = worldWidth + tileSize * 2; // let bots run entire map

    // STOMP MECHANISM VARIABLES
    private boolean stompActive = false;
    private int stompDamage = 1;
    private final int stompBounceForce = 7;

    // VÙNG CHẾT THAY VÌ HỐ - CHẠM VÀO LÀ CHẾT
    public final java.util.List<Rectangle> deathZones = new java.util.ArrayList<>();
    public final int deathZoneTileId = 8; // ID tile vùng chết
//    public final Color deathZoneColor = new Color(255, 0, 0, 150); // Màu đỏ trong suốt

    // Mouse handler từ gamePanel thứ hai
    private mouseHandler mouseH;

    // Constructor
    public gamePanel() {
        bgMusic = new BackgroundMusic("/music/MusicBackground.wav");
        loseMusic = new BackgroundMusic("/music/over_ending.wav");
        winMusic = new BackgroundMusic("/music/winMusic.wav");
        bgMusic.playLoop();

        try {
            // Thử cả hai cách load background
            InputStream bgStream = getClass().getResourceAsStream("/res/map/background.png");
            if (bgStream == null) {
                bgStream = getClass().getResourceAsStream("/res/ui/chongchay.png");
            }
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
        this.setBackground(new Color(37, 150, 190)); // Màu từ gamePanel thứ hai
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.addMouseListener(this);

        // Thêm mouseHandler từ gamePanel thứ hai
        mouseH = new mouseHandler(this);
        this.addMouseListener(mouseH);

        this.setFocusable(true);

        // focus listener để reset keys
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                keyH.resetAllKeys();
            }
        });

        gameState = titleState;

        // KHỞI TẠO CAMERA từ gamePanel thứ hai
        camera = new Camera(this);

        getPlayerImage();
        detectDeathZones(); // PHÁT HIỆN VÙNG CHẾT
       //spawnBots();
        spawnChiPheo();

        try {
            java.net.URL imageUrl = getClass().getResource("/res/tile/clound1.png");
            if (imageUrl == null) {
                imageUrl = getClass().getResource("/tile/clound1.png");
            }
            if (imageUrl != null) {
                cloudImage = new ImageIcon(imageUrl).getImage();
            } else {
                System.err.println("Lỗi: Không tìm thấy tài nguyên cloud.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameStartTime = 0;
        lastBotSpawnTime = System.currentTimeMillis();
    }

    // Phương thức camera từ gamePanel thứ hai
    public int getScreenX(int worldX) {
        return camera.worldXToScreenX(worldX);
    }

    public int getScreenY(int worldY) {
        return camera.worldYToScreenY(worldY);
    }

    private int getBotSpawnY() {
        // spawn ngang mặt đất của player + 1 tile
        return player.getGroundLevel() + tileSize * 1;
    }

    // PHÁT HIỆN VÙNG CHẾT TRÊN MAP - CHẠM VÀO LÀ DIE
    private void detectDeathZones() {
        deathZones.clear();

        // Dùng trực tiếp mapTileNum (public) để không cần sửa tileManager
        int[][] mapTileNum = tileM.mapTileNum;

        for (int row = 0; row < maxWorldRow; row++) {
            for (int col = 0; col < maxWorldCol; col++) {
                if (mapTileNum[col][row] == deathZoneTileId) {
                    Rectangle deathZone = new Rectangle(
                            col * tileSize,
                            row * tileSize,
                            tileSize,
                            tileSize
                    );
                    deathZones.add(deathZone);
                }
            }
        }

        System.out.println("Detected " + deathZones.size() + " death zones");
    }

    // KIỂM TRA PLAYER CÓ CHẠM VÀO VÙNG CHẾT KHÔNG
    private boolean isPlayerInDeathZone() {
        if (gameOver || gameWon) return false;

        Rectangle playerRect = new Rectangle(
                player.worldX + player.solidArea.x,
                player.worldY + player.solidArea.y,
                player.solidArea.width,
                player.solidArea.height
        );

        for (Rectangle deathZone : deathZones) {
            if (playerRect.intersects(deathZone)) {
                return true;
            }
        }
        return false;
    }

    // KIỂM TRA BOT CÓ CHẠM VÀO VÙNG CHẾT KHÔNG
    private boolean isBotInDeathZone(bot b) {
        Rectangle botRect = new Rectangle(
                b.worldX + b.solidArea.x,
                b.worldY + b.solidArea.y,
                b.solidArea.width,
                b.solidArea.height
        );

        for (Rectangle deathZone : deathZones) {
            if (botRect.intersects(deathZone)) {
                return true;
            }
        }
        return false;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    private void spawnBots() {
        bots.clear();




        // Thêm house từ gamePanel thứ hai
        house = new House(this, tileSize * 6, tileSize * 8 + 20);

        initialBotsSpawned = 0;
        lastInitialBotSpawnTime = 0;
    }

    private void spawnChiPheo() {
        int chiPheoSpawnX =  tileSize * 50;
        int chiPheoSpawnY = tileSize * 9 +15 ;

        chiPheo = new ChiPheo(this, chiPheoSpawnX, chiPheoSpawnY);
    }





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

        int spawnX = spawnXPosition;
        int spawnY = getBotSpawnY();

        // Đảm bảo không spawn vào vùng chết
        for (Rectangle deathZone : deathZones) {
            Rectangle spawnRect = new Rectangle(spawnX, spawnY, tileSize, tileSize);
            if (spawnRect.intersects(deathZone)) {
                spawnY -= tileSize;
                break;
            }
        }

        bot newBot = new bot(this, spawnX, spawnY, type);
        newBot.direction = "right";
        newBot.isMovingRight = true;

        bots.add(newBot);
        initialBotsSpawned++;
        lastInitialBotSpawnTime = System.currentTimeMillis();
    }

    private void spawnBotAtRandomPosition() {
        if (bots.size() >= maxBotsOnField) return;

        int spawnX = spawnXPosition;
        int spawnY = getBotSpawnY();

        // Đảm bảo không spawn vào vùng chết
        for (Rectangle deathZone : deathZones) {
            Rectangle spawnRect = new Rectangle(spawnX, spawnY, tileSize, tileSize);
            if (spawnRect.intersects(deathZone)) {
                spawnY -= tileSize;
                break;
            }
        }

        bot.BotType[] types = bot.BotType.values();
        bot.BotType type = types[random.nextInt(types.length)];

        bot newBot = new bot(this, spawnX, spawnY, type);
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
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null) {
            update();
            repaint();
            try {
                double remaining = nextDrawTime - System.nanoTime();
                if (remaining < 0) remaining = 0;
                Thread.sleep((long) (remaining / 1000000));
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleStompMechanic() {
        if (!player.isJumping || player.velocityY <= 0) {
            stompActive = false;
            return;
        }

        Rectangle stompArea = new Rectangle(
                player.worldX + player.solidArea.x,
                player.worldY + player.solidArea.y + player.solidArea.height - 10,
                player.solidArea.width,
                15
        );

        java.util.Iterator<bot> iterator = bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();

            Rectangle botHeadArea = new Rectangle(
                    b.worldX + b.solidArea.x,
                    b.worldY + b.solidArea.y - 5,
                    b.solidArea.width,
                    10
            );

            if (stompArea.intersects(botHeadArea)) {
                stompActive = true;

                boolean dead = b.applyDamage(stompDamage);

                if (dead) {
                    deathEffects.add(new DeathEffect(
                            b.worldX + b.solidArea.width / 2,
                            b.worldY + b.solidArea.height / 2
                    ));

                    iterator.remove();
                    botsKilled++;

                    int effectScreenX = camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                    int effectScreenY = camera.worldYToScreenY(b.worldY);
                    damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "STOMP!", new Color(255, 200, 0)));
                }

                player.velocityY = -stompBounceForce;
                player.isGrounded = false;

                attackEffects.add(new AttackEffect(stompArea, 10, false));
                break;
            }
        }
    }

    private void checkPlayerBotCollision() {
        if (playerInvincible) {
            invincibleCounter++;
            if (invincibleCounter > invincibleTime) {
                invincibleCounter = 0;
                playerInvincible = false;
            }
            return;
        }

        if (stompActive) return;

        for (bot b : bots) {
            if (cChecker.entitiesIntersect(player, b)) {

                if (player.isJumping && player.velocityY > 0) {
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
                        continue;
                    }
                }

                playerHp = Math.max(0, playerHp - 1);
                damageEffects.add(new damageEffect(
                        camera.worldXToScreenX(player.worldX) + tileSize / 2,
                        camera.worldYToScreenY(player.worldY),
                        "-1"
                ));
                playerInvincible = true;
                invincibleCounter = 0;

                int knockbackDirection = (b.worldX < player.worldX) ? 1 : -1;
                player.worldX += knockbackDirection * player.speed * 3;

                break;
            }
        }
    }

    private void checkChiPheoCollision() {
        if (chiPheo == null || gameWon || gameOver) return;

        Rectangle playerBox = new Rectangle(
                player.worldX + player.solidArea.x,
                player.worldY + player.solidArea.y,
                player.solidArea.width,
                player.solidArea.height
        );

        int chiMarginX = tileSize * 3;
        int chiMarginY = tileSize * 3 / 2;
        int chiWidth = Math.max(tileSize / 2, tileSize * 4 - chiMarginX);
        int chiHeight = Math.max(tileSize / 2, tileSize * 2 - chiMarginY);
        Rectangle chiPheoBox = new Rectangle(
                chiPheo.worldX + (tileSize * 4 - chiWidth) / 2,
                chiPheo.worldY + (tileSize * 2 - chiHeight) / 2,
                chiWidth,
                chiHeight
        );

        if (playerBox.intersects(chiPheoBox)) {
            triggerGameWin();
        }
    }

    public void update() {
        if (gameState == titleState || gameState == guideState) return;

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

            if (gameStartTime == 0) {
                gameStartTime = System.currentTimeMillis();
                lastInitialBotSpawnTime = System.currentTimeMillis();
                lastBotSpawnTime = System.currentTimeMillis();
            }

            // CẬP NHẬT CAMERA TRƯỚC
            camera.update();

            long currentTime = System.currentTimeMillis();

            // Spawn initial bots
            if (initialBotsSpawned < maxInitialBots) {
                if (initialBotsSpawned == 0) {
                    spawnInitialBot();
                } else if (currentTime - lastInitialBotSpawnTime >= initialSpawnInterval) {
                    spawnInitialBot();
                    if (initialBotsSpawned >= maxInitialBots) {
                        lastBotSpawnTime = System.currentTimeMillis();
                    }
                }
            }

            currentTimeElapsed = (currentTime - gameStartTime) / 1000;

            updateBotSpeeds();

            // Spawn new bots after initial
            long now = System.currentTimeMillis();
            long currentSpawnInterval = getCurrentSpawnIntervalMs();
            long timeSinceLastInitialBot = now - lastInitialBotSpawnTime;
            long minDelayAfterLastInitialBot = 2000;

            if (initialBotsSpawned >= maxInitialBots
                    && timeSinceLastInitialBot >= minDelayAfterLastInitialBot
                    && now - lastBotSpawnTime >= currentSpawnInterval
                    && bots.size() < maxBotsOnField) {
                spawnBotAtRandomPosition();
                lastBotSpawnTime = now;
            }

            if (attackCooldownCounter > 0) attackCooldownCounter--;

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

            // >>>>> LOGIC MỚI: KIỂM TRA CHẠM VÙNG CHẾT TRƯỚC
            if (isPlayerInDeathZone()) {
                System.out.println("Player touched death zone! Game over!");
                triggerGameOver();
                return; // Không cần update tiếp
            }

            player.update();

            // Kiểm tra bot chạm vùng chết
            checkBotDeathZone();

            if (chiPheo != null) chiPheo.update();
            checkChiPheoCollision();

            handleStompMechanic();

            java.util.Iterator<bot> iterator = bots.iterator();
            while (iterator.hasNext()) {
                bot b = iterator.next();

                boolean shouldDespawn = b.updateAI(player.worldX, player.worldY);
                if (shouldDespawn) {
                    iterator.remove();
                    continue;
                }

                // fallback: nếu vẫn muốn remove khi ra quá xa
                if (b.worldX > offscreenDespawnX) {
                    iterator.remove();
                }
            }

            checkPlayerBotCollision();

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

    // KIỂM TRA BOT CHẠM VÙNG CHẾT
    private void checkBotDeathZone() {
        java.util.Iterator<bot> iterator = bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();

            if (isBotInDeathZone(b)) {
                deathEffects.add(new DeathEffect(
                        b.worldX + b.solidArea.width / 2,
                        b.worldY + b.solidArea.height / 2
                ));

                iterator.remove();

                int effectScreenX = camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                int effectScreenY = camera.worldYToScreenY(b.worldY);
                damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "DEATH ZONE!", new Color(255, 50, 50)));
            }
        }
    }

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

    private void updateBotSpeeds() {
        float progress = Math.min(1.0f, (float) currentTimeElapsed / survivalTimeSeconds);
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

    private void restartGame() {
        playerHp = 3;
        playerInvincible = false;
        invincibleCounter = 0;

        player.setDefaultValue();
        player.canMove = true;

        keyH.resetAllKeys();

        winStartTime = 0;
        winBoardShown = false;

//        spawnBots();
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

        initialBotsSpawned = 0;
        lastInitialBotSpawnTime = 0;

        // Reset camera về vị trí player từ gamePanel thứ hai
        if (camera != null) {
            camera.centerOnPlayer();
        }

        if (loseMusic != null) loseMusic.stop();
        if (winMusic != null) winMusic.stop();
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
                }
                int effectScreenX = camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                int effectScreenY = camera.worldYToScreenY(b.worldY);
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
                g2.setColor(new Color(37, 150, 190));
                g2.fillRect(0, 0, width, height);
            }

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

            for (AttackEffect effect : attackEffects) {
                effect.draw(g2, player.worldX, player.worldY, camera.worldXToScreenX(0), camera.worldYToScreenY(0));
            }

            for (DeathEffect effect : deathEffects) {
                effect.draw(g2, player.worldX, player.worldY, camera.worldXToScreenX(0), camera.worldYToScreenY(0));
            }

            // Vẽ chí phèo với camera offset
            if (chiPheo != null) chiPheo.draw(g2);

            // Vẽ house
            if (house != null) {
                house.draw(g2);
            }

            // Vẽ Hiệu ứng damage
            for (damageEffect effect : damageEffects) {
                effect.draw(g2);
            }

            drawPlayerLife(g2);
//            drawKillCounter(g2);

            if (cloudImage != null) drawClouds(g2);

            if (showGameOverMenu) drawGameOverScreen(g2);
            if (showWinMenu) drawWinScreen(g2);
        }

        g2.dispose();
    }

    // VẼ VÙNG CHẾT VỚI HIỆU ỨNG
    private void drawDeathZones(Graphics2D g2) {
        Composite originalComposite = g2.getComposite();

        // Đặt độ trong suốt
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        for (Rectangle deathZone : deathZones) {
            int screenX = deathZone.x - camera.worldX;
            int screenY = deathZone.y - camera.worldY;

            // Chỉ vẽ nếu trong tầm nhìn
            if (screenX + tileSize > 0 && screenX < width &&
                    screenY + tileSize > 0 && screenY < height) {

                // Vẽ màu đỏ với gradient
                GradientPaint gradient = new GradientPaint(
                        screenX, screenY, new Color(255, 0, 0, 200),
                        screenX + tileSize, screenY + tileSize, new Color(200, 0, 0, 150)
                );
                g2.setPaint(gradient);
                g2.fillRect(screenX, screenY, tileSize, tileSize);

                // Vẽ viền
                g2.setColor(new Color(150, 0, 0, 200));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(screenX, screenY, tileSize, tileSize);

                // Vẽ dấu X
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(screenX + 5, screenY + 5, screenX + tileSize - 5, screenY + tileSize - 5);
                g2.drawLine(screenX + tileSize - 5, screenY + 5, screenX + 5, screenY + tileSize - 5);
            }
        }

        // Khôi phục composite
        g2.setComposite(originalComposite);
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
        String loseText = "YOU DIED";
        int textWidth = g2.getFontMetrics().stringWidth(loseText);
        g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 100);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18f));
        g2.setColor(Color.BLACK);
        String deathText = "You can not find your Chi Pheo 💔";
        int deathWidth = g2.getFontMetrics().stringWidth(deathText);
        g2.drawString(deathText, menuX + (menuWidth - deathWidth) / 2, menuY + 140);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20f));
        String instructionText = "Press R to Restart or ESC to Exit";
        int instWidth = g2.getFontMetrics().stringWidth(instructionText);
        g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 170);
    }

    public void drawWinScreen(Graphics2D g2) {
        // Tính thời gian đã trôi qua kể từ khi thắng
        long winTimeElapsed = System.currentTimeMillis() - winStartTime;

        // Nếu chưa đủ 3 giây (3000ms), hiển thị ảnh winboard.png
        if (winTimeElapsed < 3000) {
            // Hiển thị ảnh winboard.png
            drawWinBoard(g2, winTimeElapsed);
            return; // Chưa hiển thị màn hình "YOU WIN!"
        }

        // Sau 3 giây, hiển thị màn hình "YOU WIN!" bình thường
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

    // Phương thức vẽ ảnh winboard.png
    private void drawWinBoard(Graphics2D g2, long elapsedTime) {
        try {
            // Load ảnh winboard.png
            BufferedImage winBoardImage = ImageIO.read(getClass().getResourceAsStream("/res/ui/winboard.png"));

            // Tính toán scale để ảnh vừa với màn hình
            int imgWidth = winBoardImage.getWidth();
            int imgHeight = winBoardImage.getHeight();

            // Scale ảnh (có thể điều chỉnh theo ý muốn)
            int targetWidth = Math.min(width - 100, imgWidth);
            int targetHeight = (int) (targetWidth * ((float)imgHeight / imgWidth));

            if (targetHeight > height - 100) {
                targetHeight = height - 100;
                targetWidth = (int) (targetHeight * ((float)imgWidth / imgHeight));
            }

            int x = (width - targetWidth) / 2;
            int y = (height - targetHeight) / 2;

            // Thêm hiệu ứng fade in trong 0.5s đầu
            float alpha = 1.0f;
            if (elapsedTime < 500) {
                alpha = elapsedTime / 500.0f; // Từ 0 đến 1
            }

            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Vẽ nền đen mờ
            g2.setColor(new Color(0, 0, 0, (int)(150 * alpha)));
            g2.fillRect(0, 0, width, height);

            // Vẽ ảnh winboard
            g2.drawImage(winBoardImage, x, y, targetWidth, targetHeight, null);

            g2.setComposite(originalComposite);
        } catch (Exception e) {
            // Nếu không tìm thấy ảnh, vẽ màn hình đơn giản
            e.printStackTrace();
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, width, height);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
            g2.setColor(Color.GREEN);
            g2.drawString("VICTORY!", width/2 - 150, height/2);

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30f));
            g2.setColor(Color.WHITE);
            g2.drawString("Loading win screen...", width/2 - 120, height/2 + 60);
        }
    }
    private void drawClouds(Graphics2D g2) {
        int camX = camera.worldX;
        int camY = camera.worldY;
        float parallax = 0.25f;

        int[][] clouds = new int[][]{
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

            int drawX = baseX - (int) (camX * parallax);
            int drawY = baseY - (int) (camY * parallax);

            drawX = ((drawX % wrapW) + wrapW) % wrapW - 100;
            drawY = ((drawY % wrapH) + wrapH) % wrapH - 100;

            g2.drawImage(cloudImage, drawX, drawY, w, h, this);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && gameState == playState && !gameOver && !gameWon) {
            queueAttack();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void triggerGameOver() {
        gameOver = true;
        showGameOverMenu = true;
        if (bgMusic != null) bgMusic.stop();
        if (winMusic != null) winMusic.stop();
        if (loseMusic != null) loseMusic.playOnce();
        System.out.println("Game over - Death zone!");
    }

    private void triggerGameWin() {
        gameWon = true;
        showWinMenu = true;
        gameState = winState;
        winStartTime = System.currentTimeMillis(); // Ghi lại thời điểm thắng
        winBoardShown = false;

        if (bgMusic != null) bgMusic.stop();
        if (loseMusic != null) loseMusic.stop();
        if (winMusic != null) winMusic.playOnce();
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
            int radius = (int) (10 + 44 * progress);
            int alpha = Math.max(80, 230 - (int) (progress * 230));

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