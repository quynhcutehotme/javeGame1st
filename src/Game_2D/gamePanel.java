package Game_2D;

import entity.player;
import tile.tileManager;
import entity.bot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class gamePanel extends JPanel implements Runnable {
    final int orgsSize = 16;
    final int scale = 2;
    public final int tileSize = orgsSize * scale *2; // = 64
//set size màn hình mặt định
    public int maxColumn = 18;
    public int maxRow = 9;

    public final int width = tileSize * maxColumn;   // 64 * 16 = 1024
    public final int height = tileSize * maxRow;

    public final int maxWorldCol=34;
    public final int maxWorldRow=30;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize* maxWorldRow ;


    int FPS = 60;

    tileManager tileM = new tileManager(this);

    keyHander keyH = new keyHander() ;

    public collisionChecker cChecker = new collisionChecker(this);

    Thread gameThread;
    public  player player = new player(this, keyH);
    public java.util.List<bot> bots = new java.util.ArrayList<>();
    public java.util.List<damageEffect> damageEffects = new java.util.ArrayList<>();

    public int playerHp = 3;
    private boolean playerInvincible = false;
    private int invincibleCounter = 0;
    private final int invincibleTime = 60; // frames

    private boolean gameOver = false;
    private boolean showGameOverMenu = false;

    public gamePanel (){
        this.setPreferredSize(new Dimension(width,height));
        this.setBackground(new Color(92, 201, 141));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        // spawn some bots
        spawnBots();
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

    public void run(){

        double drawInterval = 1000000000/FPS ;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null){
//            System.out.println("game running");
            update();
            repaint();

            try {
                double remaniningTime = nextDrawTime - System.nanoTime();

                if (remaniningTime < 0) {
                    remaniningTime = 0;
                }
                Thread.sleep((long) remaniningTime / 1000000);
                nextDrawTime+=drawInterval;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            }

        }

    public void update(){
        if (gameOver) {
            // Handle game over menu input
            if (keyH.restartPress) {
                restartGame();
            }
            if (keyH.exitPress) {
                System.exit(0);
            }
            return;
        }

        player.update();
        for (bot b : bots) {
            b.updateAI(player.worldX, player.worldY);
        }
        // handle player-bot collisions
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
                    System.out.println("Player hit! HP: " + playerHp);
                    
                    // Create damage effect at player position
                    int screenX = player.screenX;
                    int screenY = player.screenY;
                    damageEffects.add(new damageEffect(screenX + tileSize/2, screenY, "-1"));
                    
                    playerInvincible = true;
                    break;
                }
            }
        }
        
        // Update damage effects
        damageEffects.removeIf(effect -> {
            effect.update();
            return !effect.isAlive();
        });

        if (playerHp <= 0 && !gameOver) {
            System.out.println("Game Over triggered! HP: " + playerHp);
            triggerGameOver();
        }
    }

    private void triggerGameOver() {
        System.out.println("triggerGameOver() called");
        gameOver = true;
        showGameOverMenu = true;
        System.out.println("Game over menu should be visible now");
    }

    private void restartGame() {
        // Reset state
        playerHp = 3;
        playerInvincible = false;
        invincibleCounter = 0;
        player.setDefaultValue();
        spawnBots();
        damageEffects.clear(); // Clear any existing damage effects
        gameOver = false;
        showGameOverMenu = false;
        this.revalidate();
        this.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;



        tileM.draw(g2);
        for (bot b : bots) {
            b.draw(g2);
        }
        player.draw(g2);
        
        // Draw damage effects
        for (damageEffect effect : damageEffects) {
            effect.draw(g2);
        }
        
        // HUD
        g2.setColor(Color.BLACK);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 20f));
        g2.drawString("HP: " + playerHp, 10, 24);
        
        // Game Over Menu
        if (showGameOverMenu) {
            // Semi-transparent overlay
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, width, height);
            
            // Menu background
            int menuWidth = 300;
            int menuHeight = 200;
            int menuX = (width - menuWidth) / 2;
            int menuY = (height - menuHeight) / 2;
            
            g2.setColor(new Color(255, 255, 255, 230));
            g2.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(menuX, menuY, menuWidth, menuHeight, 20, 20);
            
            // "YOU LOSE" text
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
            g2.setColor(Color.RED);
            String loseText = "YOU LOSE";
            int textWidth = g2.getFontMetrics().stringWidth(loseText);
            g2.drawString(loseText, menuX + (menuWidth - textWidth) / 2, menuY + 60);
            
            // Instructions
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 16f));
            g2.setColor(Color.BLACK);
            String instructionText = "Press R to Restart or ESC to Exit";
            int instWidth = g2.getFontMetrics().stringWidth(instructionText);
            g2.drawString(instructionText, menuX + (menuWidth - instWidth) / 2, menuY + 100);
            
            // Button backgrounds
            int buttonWidth = 80;
            int buttonHeight = 30;
            int buttonY = menuY + 130;
            
            // Restart button
            int restartX = menuX + 50;
            g2.setColor(new Color(200, 255, 200));
            g2.fillRoundRect(restartX, buttonY, buttonWidth, buttonHeight, 5, 5);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(restartX, buttonY, buttonWidth, buttonHeight, 5, 5);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 14f));
            g2.drawString("Restart", restartX + 15, buttonY + 20);
            
            // Exit button
            int exitX = menuX + 170;
            g2.setColor(new Color(255, 200, 200));
            g2.fillRoundRect(exitX, buttonY, buttonWidth, buttonHeight, 5, 5);
            g2.setColor(Color.BLACK);
            g2.drawRoundRect(exitX, buttonY, buttonWidth, buttonHeight, 5, 5);
            g2.drawString("Exit", exitX + 25, buttonY + 20);
        }
        
        g2.dispose();

    }

}
