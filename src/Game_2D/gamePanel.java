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
    public Image cloudImage;

    // ✅ tách audio
    public AudioManager audio;

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

    // ✅ overlay needs access
    public long winStartTime = 0;
    public boolean winBoardShown = false;

    public UI ui = new UI(this);
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int guideState = 2;
    public final int winState = 3;

    // ✅ HazardSystem cần đọc mapTileNum -> để public
    public tileManager tileM = new tileManager(this);

    public keyHander keyH = new keyHander(this);
    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public player player = new player(this, keyH);

    public java.util.List<bot> bots = new java.util.ArrayList<>();
    public java.util.List<damageEffect> damageEffects = new java.util.ArrayList<>();

    // ✅ moved out
    public final java.util.List<AttackEffect> attackEffects = new java.util.ArrayList<>();
    public final java.util.List<DeathEffect> deathEffects = new java.util.ArrayList<>();

    private final Random random = new Random();

    public int playerHp = 3;
    public BufferedImage heartIcon;

    // ✅ CombatSystem needs these
    public boolean playerInvincible = false;
    public int invincibleCounter = 0;
    public final int invincibleTime = 60;

    private boolean gameOver = false;
    private boolean showGameOverMenu = false;
    private boolean gameWon = false;
    private boolean showWinMenu = false;

    private long gameStartTime;
    public long currentTimeElapsed = 0;
    private long survivalTimeSeconds = 15;

    // tracking spawn ban đầu (giữ biến nhưng không dùng spawn theo thời gian nữa)
    public int initialBotsSpawned = 0;
    public final int maxInitialBots = 3;
    public long lastInitialBotSpawnTime = 0;
    public final long initialSpawnInterval = 6500;

    private final long maxSpawnIntervalMs = 10000;
    private final long minSpawnIntervalMs = 7000;
    private final long spawnRampDurationSec = 30;
    private long lastBotSpawnTime = System.currentTimeMillis();

    public int botsKilled = 0;
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

    // ✅ CombatSystem uses this
    public final int attackRange = tileSize * 2;

    private final int spawnXPosition = -tileSize * 2;
    private final int offscreenDespawnX = worldWidth + tileSize * 2;

    // ✅ CombatSystem uses these
    public boolean stompActive = false;
    public int stompDamage = 1;
    public final int stompBounceForce = 7;

    // ✅ Bot đang dùng gp.deathZones -> giữ field này
    // và cho nó trỏ vào hazardSystem.deathZones để không sửa bot.java
    public java.util.List<Rectangle> deathZones = new java.util.ArrayList<>();
    public final int deathZoneTileId = 8;

    private mouseHandler mouseH;

    // ✅ NEW systems
    public HazardSystem hazardSystem;
    public BotSpawner botSpawner;
    public CombatSystem combatSystem;
    public OverlayRenderer overlayRenderer;

    public gamePanel() {

        // ✅ Audio
        audio = new AudioManager();
        audio.playBackgroundLoop();

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

        // ✅ init systems
        hazardSystem = new HazardSystem(this, deathZoneTileId);
        botSpawner = new BotSpawner(this);
        combatSystem = new CombatSystem(this);
        overlayRenderer = new OverlayRenderer(this);

        // ✅ IMPORTANT: gp.deathZones trỏ vào hazardSystem.deathZones (bot.java vẫn dùng gp.deathZones)
        this.deathZones = hazardSystem.deathZones;

        getPlayerImage();
        hazardSystem.detectDeathZones();

        // ✅ spawn cố định qua BotSpawner
        botSpawner.spawnBotsFixed();

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

    // ✅ BotSpawner cần gọi -> public
    public int getBotSpawnY() {
        return player.getGroundLevel() + tileSize * 1;
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

                // ✅ moved: hitbox build in CombatSystem
                Rectangle telegraphHitbox = combatSystem.buildAttackHitbox(queuedAttackDirection);
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

            // ✅ moved: death zone check in HazardSystem
            if (hazardSystem.isPlayerInDeathZone()) {
                System.out.println("Player touched death zone! Game over!");
                triggerGameOver();
                return;
            }

            player.update();

            // ✅ moved: bot death zone handling in HazardSystem
            hazardSystem.checkBotDeathZone();

            if (chiPheo != null) chiPheo.update();
            checkChiPheoCollision();

            // ✅ moved: stomp mechanic in CombatSystem
            combatSystem.handleStompMechanic();

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

            // ✅ moved: collision damage in CombatSystem
            combatSystem.checkPlayerBotCollision();

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

        // ✅ reset bots cố định (qua spawner)
        botSpawner.spawnBotsFixed();
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

        // ✅ reset audio
        if (audio != null) audio.resetToBackgroundLoop();

        gameState = playState;
    }

    public void queueAttack() {
        attackRequested = true;
    }

    // ✅ moved: body logic stays same but delegated
    private void performAttack(String directionSnapshot) {
        combatSystem.performAttack(directionSnapshot);
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

            // ✅ moved: overlays
            overlayRenderer.drawPlayerLife(g2);

            if (cloudImage != null) overlayRenderer.drawClouds(g2);

            if (showGameOverMenu) overlayRenderer.drawGameOverScreen(g2);
            if (showWinMenu) overlayRenderer.drawWinScreen(g2);
        }

        g2.dispose();
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

        // ✅ audio moved
        if (audio != null) {
            audio.stopBackground();
            audio.stopWin();
            audio.playLoseOnce();
        }

        System.out.println("Game over - Death zone!");
    }

    private void triggerGameWin() {
        gameWon = true;
        showWinMenu = true;
        gameState = winState;
        winStartTime = System.currentTimeMillis();
        winBoardShown = false;

        // ✅ audio moved
        if (audio != null) {
            audio.stopBackground();
            audio.stopLose();
            audio.playWinOnce();
        }

        System.out.println("Game won! Survival time: " + currentTimeElapsed + " seconds");
    }
}
