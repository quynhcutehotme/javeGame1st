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

        public Camera camera;

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

        private long winStartTime = 0;
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

        public int playerHp = 3;
        public BufferedImage heartIcon;
        private boolean playerInvincible = false;
        private int invincibleCounter = 0;
        private final int invincibleTime = 60;

        private boolean gameOver = false;
        private boolean showGameOverMenu = false;
        private boolean gameWon = false;
        private boolean showWinMenu = false;

        private long gameStartTime;
        private long currentTimeElapsed = 0;
        private long survivalTimeSeconds = 15;

        // tracking spawn ban đầu (giữ biến nhưng không dùng spawn theo thời gian nữa)
        private int initialBotsSpawned = 0;
        private final int maxInitialBots = 3;
        private long lastInitialBotSpawnTime = 0;
        private final long initialSpawnInterval = 6500;

        private final long maxSpawnIntervalMs = 10000;
        private final long minSpawnIntervalMs = 7000;
        private final long spawnRampDurationSec = 30;
        private long lastBotSpawnTime = System.currentTimeMillis();
        private int botsKilled = 0;
        private final int killsToWin = 5;
        private final int maxBotsOnField = 6;

        // (3)(4)(5) áp dụng: bỏ spawn theo thời gian nên các biến speed scaling không cần dùng
        public final int baseBotSpeed = 3;
        private final float maxSpeedMultiplier = 2.2f;

        private boolean attackRequested = false;
        private int attackCooldownCounter = 0;
        private final int attackCooldownFrames = 10;
        private final int attackWindupFrames = 6;
        private int attackWindupCounter = 0;
        private String queuedAttackDirection = null;
        private final int attackRange = tileSize * 2;

        private final int spawnXPosition = -tileSize * 2;
        private final int offscreenDespawnX = worldWidth + tileSize * 2;

        private boolean stompActive = false;
        private int stompDamage = 1;
        private final int stompBounceForce = 7;

        public final java.util.List<Rectangle> deathZones = new java.util.ArrayList<>();
        public final int deathZoneTileId = 8;

        private mouseHandler mouseH;

        public gamePanel() {
            bgMusic = new BackgroundMusic("/music/MusicBackground.wav");
            loseMusic = new BackgroundMusic("/music/over_ending.wav");
            winMusic = new BackgroundMusic("/music/winMusic.wav");
            bgMusic.playLoop();

            try {
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
            this.setBackground(new Color(37, 150, 190));
            this.setDoubleBuffered(true);
            this.addKeyListener(keyH);
            this.addMouseListener(this);

            mouseH = new mouseHandler(this);
            this.addMouseListener(mouseH);

            this.setFocusable(true);

            this.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    keyH.resetAllKeys();
                }
            });

            gameState = titleState;

            camera = new Camera(this);

            getPlayerImage();
            detectDeathZones();

            // ✅ (3): spawn đúng 3 bot cố định, không spawn thêm
            spawnBots();

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

        public int getScreenX(int worldX) {
            return camera.worldXToScreenX(worldX);
        }

        public int getScreenY(int worldY) {
            return camera.worldYToScreenY(worldY);
        }

        private int getBotSpawnY() {
            return player.getGroundLevel() + tileSize * 1;
        }

        private void detectDeathZones() {
            deathZones.clear();

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

        // ✅ (3)(4)(5): Spawn đúng 3 bot cố định + mỗi con đi qua-lại trong zone riêng
        private void spawnBots() {
            bots.clear();

            // house giữ nguyên
            house = new House(this, tileSize * 1, tileSize * 8 + 20);

            int y = getBotSpawnY();

            bot b1 = new bot(this, tileSize * 10, y, bot.BotType.GREEN);
            b1.direction = "left";
            b1.setPatrolRangePx(tileSize * 4);   // đi ±1 tile

            bot b2 = new bot(this, tileSize * 25, y, bot.BotType.PURPLE);
            b2.direction = "right";
            b2.setPatrolRangePx(tileSize * 3);   // đi ±3 tile

            bot b3 = new bot(this, tileSize * 38, y, bot.BotType.YELLOW);
            b3.direction = "left";
            b3.setPatrolRangePx(tileSize * 1);   // đi ±1 tile

            bot b4 = new bot(this, tileSize * 44, y, bot.BotType.GREEN); // 👈 vị trí spawn
            b4.direction = "right";
            b4.setPatrolRangePx(tileSize * 2);                           // 👈 quãng đường đi

            bot b5 = new bot(this, tileSize * 60, y, bot.BotType.PURPLE); // 👈 vị trí spawn
            b5.direction = "right";
            b5.setPatrolRangePx(tileSize * 4);

            bot b6 = new bot(this, tileSize * 70, y, bot.BotType.YELLOW); // 👈 vị trí spawn
            b6.direction = "left";
            b6.setPatrolRangePx(tileSize * 4);

            bots.add(b1);
            bots.add(b2);
            bots.add(b3);
            bots.add(b4);
            bots.add(b5);
            bots.add(b6);

            // chặn spawn khác
            initialBotsSpawned = 3;
            lastInitialBotSpawnTime = 0;
        }

        private void spawnChiPheo() {
            int chiPheoSpawnX = tileSize * 50;
            int chiPheoSpawnY = tileSize * 9 + 15;

            chiPheo = new ChiPheo(this, chiPheoSpawnX, chiPheoSpawnY);
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

                camera.update();

                currentTimeElapsed = (System.currentTimeMillis() - gameStartTime) / 1000L;

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

                if (isPlayerInDeathZone()) {
                    System.out.println("Player touched death zone! Game over!");
                    triggerGameOver();
                    return;
                }

                player.update();

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

            // ✅ reset bots cố định
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

            initialBotsSpawned = 3;
            lastInitialBotSpawnTime = 0;

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

                tileM.draw(g2, camera.worldX, camera.worldY);

                for (bot b : bots) {
                    int screenX = camera.worldXToScreenX(b.worldX);
                    int screenY = camera.worldYToScreenY(b.worldY);
                    b.draw(g2, screenX, screenY);
                }

                int playerScreenX = camera.worldXToScreenX(player.worldX);
                int playerScreenY = camera.worldYToScreenY(player.worldY);
                player.draw(g2, playerScreenX, playerScreenY);

                for (AttackEffect effect : attackEffects) {
                    effect.draw(g2, player.worldX, player.worldY, camera.worldXToScreenX(0), camera.worldYToScreenY(0));
                }

                for (DeathEffect effect : deathEffects) {
                    effect.draw(g2, player.worldX, player.worldY, camera.worldXToScreenX(0), camera.worldYToScreenY(0));
                }

                if (chiPheo != null) chiPheo.draw(g2);

                if (house != null) {
                    house.draw(g2);
                }

                for (damageEffect effect : damageEffects) {
                    effect.draw(g2);
                }

                drawPlayerLife(g2);

                if (cloudImage != null) drawClouds(g2);

                if (showGameOverMenu) drawGameOverScreen(g2);
                if (showWinMenu) drawWinScreen(g2);
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
            long winTimeElapsed = System.currentTimeMillis() - winStartTime;

            if (winTimeElapsed < 3000) {
                drawWinBoard(g2, winTimeElapsed);
                return;
            }

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

        private void drawWinBoard(Graphics2D g2, long elapsedTime) {
            try {
                BufferedImage winBoardImage = ImageIO.read(getClass().getResourceAsStream("/res/ui/winboard.png"));

                int imgWidth = winBoardImage.getWidth();
                int imgHeight = winBoardImage.getHeight();

                int targetWidth = Math.min(width - 100, imgWidth);
                int targetHeight = (int) (targetWidth * ((float) imgHeight / imgWidth));

                if (targetHeight > height - 100) {
                    targetHeight = height - 100;
                    targetWidth = (int) (targetHeight * ((float) imgWidth / imgHeight));
                }

                int x = (width - targetWidth) / 2;
                int y = (height - targetHeight) / 2;

                float alpha = 1.0f;
                if (elapsedTime < 500) {
                    alpha = elapsedTime / 500.0f;
                }

                Composite originalComposite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

                g2.setColor(new Color(0, 0, 0, (int) (150 * alpha)));
                g2.fillRect(0, 0, width, height);

                g2.drawImage(winBoardImage, x, y, targetWidth, targetHeight, null);

                g2.setComposite(originalComposite);
            } catch (Exception e) {
                e.printStackTrace();
                g2.setColor(new Color(0, 0, 0, 200));
                g2.fillRect(0, 0, width, height);

                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 60f));
                g2.setColor(Color.GREEN);
                g2.drawString("VICTORY!", width / 2 - 150, height / 2);

                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 30f));
                g2.setColor(Color.WHITE);
                g2.drawString("Loading win screen...", width / 2 - 120, height / 2 + 60);
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

        @Override public void mouseClicked(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && gameState == playState && !gameOver && !gameWon) {
                queueAttack();
            }
        }
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}

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
            winStartTime = System.currentTimeMillis();
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
