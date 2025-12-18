package entity;

import Game_2D.gamePanel;
import java.awt.*;
import java.util.Random;

public class bot extends entity {
    private gamePanel gp;
    private final Random random = new Random();
    private Color bodyColor;
    private Color outlineColor;
    private int health = 1;
    private int aiCounter = 0;
    private int changeDirInterval = 60; 
    private int stuckCounter = 0;
    private final int stuckThreshold = 15;
    public boolean isMovingRight = false;

    
    public enum BotType {
        GREEN,   // Zig-zag vertically while moving horizontally
        PURPLE,  // Sine-wave horizontal movement
        YELLOW   // Pause randomly for 1-2 seconds, then sprint
    }

    public BotType botType;

   
    public enum BotState {
        PATROL,    // Random movement
        ALERT,     // Detected player, preparing to chase
        CHASE,     // Actively pursuing player
        SEARCH     // Lost player, searching last known location
    }

    private BotState currentState = BotState.PATROL;
    private int lastPlayerX, lastPlayerY;
    private int searchCounter = 0;
    private int alertCounter = 0;
    private final int searchDuration = 120; 
    private final int alertDuration = 30;   
    // Detection range constants
    private int DETECTION_RANGE;
    private int LOSS_RANGE;

   
    private int nextRandomChangeFrame = 0;
    private final double randomFlipChance = 0.05;
    private int lastAvoidanceFrame = 0;
    private String lastAvoidanceDirection = null;

    
    private int zigZagCounter = 0;
    private int sineWaveCounter = 0;
    private int pauseCounter = 0;
    private boolean isPaused = false;
    private int pauseDuration = 0;
    private int sprintCounter = 0;
    public int baseSpeed = 2;

   
    private boolean inDeathZone = false;

   
    private int patrolCenterX;
    private int patrolMinX;
    private int patrolMaxX;
    private int patrolRangePx = 0; 
    public bot(gamePanel gp, int worldX, int worldY, BotType type) {
        this.gp = gp;
        this.botType = type;

    
        if (gp != null) {
            this.DETECTION_RANGE = gp.tileSize * 10;
            this.LOSS_RANGE = gp.tileSize * 15;
        } else {
            this.DETECTION_RANGE = 640;
            this.LOSS_RANGE = 960;
        }

        this.worldX = worldX;
        this.worldY = worldY;
        this.speed = 2;
        this.baseSpeed = 2;
        this.direction = "left";

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidArea.width = 40;
        solidArea.height = 40;

      
        switch (type) {
            case GREEN:
                bodyColor = new Color(50, 200, 50);
                outlineColor = new Color(30, 150, 30);
                break;
            case PURPLE:
                bodyColor = new Color(200, 50, 200);
                outlineColor = new Color(150, 30, 150);
                break;
            case YELLOW:
                bodyColor = new Color(255, 220, 50);
                outlineColor = new Color(200, 170, 30);
                break;
        }

        nextRandomChangeFrame = random.nextInt(120) + 60;

      
        this.patrolCenterX = worldX;
        int defaultRange = (gp != null) ? gp.tileSize * 2 : 128; // mặc định ±2 tile
        this.patrolRangePx = (this.patrolRangePx > 0) ? this.patrolRangePx : defaultRange;
        this.patrolMinX = patrolCenterX - patrolRangePx;
        this.patrolMaxX = patrolCenterX + patrolRangePx;
    }


    public bot(gamePanel gp, int worldX, int worldY) {
        this(gp, worldX, worldY, BotType.GREEN);
    }

 
    public void setPatrolRangePx(int rangePx) {
        this.patrolRangePx = Math.max(1, rangePx);
        this.patrolMinX = patrolCenterX - this.patrolRangePx;
        this.patrolMaxX = patrolCenterX + this.patrolRangePx;
    }

   
    public boolean updateAI(int playerX, int playerY) {

        if (direction == null) direction = "left";


        if ("right".equals(direction) && worldX >= patrolMaxX) {
            direction = "left";
        } else if ("left".equals(direction) && worldX <= patrolMinX) {
            direction = "right";
        }

       
        if ("right".equals(direction)) worldX += speed;
        else worldX -= speed;

     
        spriteCounter++;
        if (spriteCounter > 12) {
            if (spriteNum == 1) spriteNum = 2;
            else if (spriteNum == 2) spriteNum = 1;
            spriteCounter = 0;
        }

        return false; 
    }

  
    private boolean checkDeathZone() {
        if (gp == null || gp.deathZones == null) return false;

        Rectangle botRect = new Rectangle(
                worldX + solidArea.x,
                worldY + solidArea.y,
                solidArea.width,
                solidArea.height
        );

        for (Rectangle deathZone : gp.deathZones) {
            if (botRect.intersects(deathZone)) {
                inDeathZone = true;
                return true;
            }
        }

        return false;
    }

   
        if (inDeathZone) return;
        if (gp == null || gp.player == null) return;

        aiCounter++;

        int dx = gp.player.worldX - worldX;
        int dy = gp.player.worldY - worldY;
        int distSq = dx * dx + dy * dy;

        boolean playerInRange = distSq < DETECTION_RANGE * DETECTION_RANGE;

        String avoidanceDirection = null;
        if (currentState != BotState.CHASE && currentState != BotState.ALERT) {
            avoidanceDirection = calculateAvoidanceDirection();
        }

        switch (currentState) {
            case PATROL:
                if (playerInRange) {
                    currentState = BotState.ALERT;
                    alertCounter = 0;
                    break;
                }
                applyMovementPattern();
                break;

            case ALERT:
                alertCounter++;
                if (alertCounter >= alertDuration) {
                    currentState = BotState.CHASE;
                }
                setDirectionTowardHorizontal(dx);
                break;

            case CHASE:
                if (!playerInRange && distSq > LOSS_RANGE * LOSS_RANGE) {
                    currentState = BotState.SEARCH;
                    lastPlayerX = gp.player.worldX;
                    lastPlayerY = gp.player.worldY;
                    searchCounter = 0;
                    break;
                }
                setDirectionTowardHorizontal(dx);
                if (random.nextDouble() < randomFlipChance) {
                    flipHorizontal();
                }
                break;

            case SEARCH:
                searchCounter++;
                if (searchCounter >= searchDuration) {
                    currentState = BotState.PATROL;
                    break;
                }
                setDirectionTowardHorizontal(lastPlayerX - worldX);
                break;
        }

        if (aiCounter >= nextRandomChangeFrame) {
            if (currentState == BotState.PATROL && random.nextDouble() < 0.3) {
                int r = random.nextInt(4);
                direction = (r == 0 ? "up" : r == 1 ? "down" : r == 2 ? "left" : "right");
            }
            nextRandomChangeFrame = aiCounter + random.nextInt(120) + 60;
        }

        if (currentState == BotState.PATROL && aiCounter % 10 == 0 && random.nextDouble() < randomFlipChance) {
            if (botType == BotType.GREEN || botType == BotType.PURPLE) {
                if (random.nextDouble() < 0.1) {
                    flipDirection();
                }
            } else {
                flipDirection();
            }
        }

        int currentSpeed = speed;
        if (botType == BotType.YELLOW && sprintCounter > 0 && !isPaused) {
            currentSpeed = (int) Math.ceil(baseSpeed * 2.0f);
        }

        if (!isPaused) {
            switch (direction) {
                case "up": worldY -= currentSpeed; break;
                case "down": worldY += currentSpeed; break;
                case "left": worldX -= currentSpeed; break;
                case "right": worldX += currentSpeed; break;
            }

            if (avoidanceDirection != null && (aiCounter - lastAvoidanceFrame) >= 2) {
                int avoidSpeed = Math.max(1, currentSpeed / 3);
                switch (avoidanceDirection) {
                    case "up": worldY -= avoidSpeed; break;
                    case "down": worldY += avoidSpeed; break;
                    case "left": worldX -= avoidSpeed; break;
                    case "right": worldX += avoidSpeed; break;
                }
                lastAvoidanceFrame = aiCounter;
            }

            if (botType == BotType.PURPLE && currentState == BotState.PATROL) {
                double sineValue = Math.sin(sineWaveCounter * 0.1);
                int verticalOffset = (int) (sineValue * 1.5);
                worldY += verticalOffset;
                worldY = Math.max(0, Math.min(gp.maxWorldRow * gp.tileSize - solidArea.height, worldY));
            }
        }

        int minX = 1 * gp.tileSize;
        int minY = 1 * gp.tileSize;
        int maxX = (gp.maxWorldCol - 2) * gp.tileSize;
        int maxY = (gp.maxWorldRow - 2) * gp.tileSize;

        if (worldX < minX) worldX = minX;
        if (worldY < minY) worldY = minY;
        if (worldX > maxX) worldX = maxX;
        if (worldY > maxY) worldY = maxY;

        checkDeathZone();
    }

    private void patrolMovement() {
        if (aiCounter % changeDirInterval == 0) {
            int r = random.nextInt(4);
            direction = (r == 0 ? "up" : r == 1 ? "down" : r == 2 ? "left" : "right");
        }
    }

    private void setDirectionTowardHorizontal(int dx) {
        direction = (dx > 0) ? "right" : "left";
    }

    private void flipHorizontal() {
        if ("left".equals(direction)) direction = "right";
        else if ("right".equals(direction)) direction = "left";
        else direction = "left";
    }

    private void flipDirection() {
        switch (direction) {
            case "up": direction = "down"; break;
            case "down": direction = "up"; break;
            case "left": direction = "right"; break;
            case "right": direction = "left"; break;
        }
    }

    private String calculateAvoidanceDirection() {
        if (gp == null || gp.bots == null) return null;

        int avoidanceRange = gp.tileSize * 1;
        int avoidanceRangeSq = avoidanceRange * avoidanceRange;

        int avoidX = 0;
        int avoidY = 0;
        int nearbyBots = 0;
        double closestDist = Double.MAX_VALUE;

        for (bot other : gp.bots) {
            if (other == this) continue;

            int dx = other.worldX - worldX;
            int dy = other.worldY - worldY;
            int distSq = dx * dx + dy * dy;
            double distance = Math.sqrt(distSq);

            if (distSq < avoidanceRangeSq && distSq > 0) {
                nearbyBots++;
                if (distance < closestDist) closestDist = distance;

                if (distance > 0) {
                    double weight = 1.0 / (distance + 1);
                    avoidX -= (int) ((dx / distance) * speed * weight);
                    avoidY -= (int) ((dy / distance) * speed * weight);
                }
            }
        }

        if (nearbyBots > 0 && closestDist < gp.tileSize * 0.8) {
            int threshold = speed;

            if (Math.abs(avoidX) > threshold && Math.abs(avoidX) > Math.abs(avoidY)) {
                String newDir = (avoidX < 0) ? "left" : "right";
                if (newDir.equals(lastAvoidanceDirection) || lastAvoidanceDirection == null) {
                    lastAvoidanceDirection = newDir;
                    return newDir;
                } else return lastAvoidanceDirection;

            } else if (Math.abs(avoidY) > threshold) {
                String newDir = (avoidY < 0) ? "up" : "down";
                if (newDir.equals(lastAvoidanceDirection) || lastAvoidanceDirection == null) {
                    lastAvoidanceDirection = newDir;
                    return newDir;
                } else return lastAvoidanceDirection;
            }
        }

        if (nearbyBots == 0) lastAvoidanceDirection = null;
        return null;
    }

    private void applyMovementPattern() {
        switch (botType) {
            case GREEN:
                zigZagCounter++;
                if (zigZagCounter % 40 == 0) {
                    if (direction.equals("left") || direction.equals("right")) {
                        direction = (zigZagCounter / 40) % 2 == 0 ? "up" : "down";
                    } else {
                        direction = random.nextBoolean() ? "left" : "right";
                    }
                }
                if ((direction.equals("left") || direction.equals("right")) && zigZagCounter % 80 == 0) {
                    direction = direction.equals("left") ? "right" : "left";
                }
                break;

            case PURPLE:
                sineWaveCounter++;
                if (!direction.equals("left") && !direction.equals("right")) {
                    direction = random.nextBoolean() ? "left" : "right";
                }
                if (sineWaveCounter % 120 == 0) {
                    direction = direction.equals("left") ? "right" : "left";
                }
                break;

            case YELLOW:
                if (isPaused) {
                    pauseCounter++;
                    if (pauseCounter >= pauseDuration) {
                        isPaused = false;
                        sprintCounter = 90;
                    }
                } else if (sprintCounter > 0) {
                    sprintCounter--;
                } else {
                    if (random.nextDouble() < 0.005) {
                        isPaused = true;
                        pauseCounter = 0;
                        pauseDuration = random.nextInt(60) + 60;
                    }
                }
                if (!isPaused && sprintCounter == 0) {
                    patrolMovement();
                }
                break;
        }
    }

    public boolean applyDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    // Draw có camera support (giữ nguyên)
    public void draw(Graphics2D g2, int screenX, int screenY) {
        if (!isInCameraView(screenX, screenY)) return;

        Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = gp.tileSize;
        int padding = Math.max(2, size / 12);

        g2.setColor(new Color(0, 0, 0, 70));
        int shadowW = (int) (size * 0.8);
        int shadowH = (int) (size * 0.25);
        int shadowX = screenX + (size - shadowW) / 2;
        int shadowY = screenY + size - shadowH / 2;
        g2.fillOval(shadowX, shadowY, shadowW, shadowH);

        int bodyX = screenX + padding;
        int bodyY = screenY + padding;
        int bodyW = size - padding * 2;
        int bodyH = size - padding * 2;

        if (inDeathZone) {
            g2.setColor(new Color(255, 100, 100, 150));
            g2.fillRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);

            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3));
            g2.drawLine(bodyX, bodyY, bodyX + bodyW, bodyY + bodyH);
            g2.drawLine(bodyX + bodyW, bodyY, bodyX, bodyY + bodyH);
            return;
        }

        g2.setColor(bodyColor);
        g2.fillRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);
        g2.setColor(outlineColor);
        g2.drawRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);

        int eyeW = Math.max(4, size / 8);
        int eyeH = Math.max(5, size / 6);
        int eyeY = bodyY + size / 4;
        int eyeLX = bodyX + size / 5;
        int eyeRX = bodyX + bodyW - size / 5 - eyeW;

        g2.setColor(Color.WHITE);
        g2.fillOval(eyeLX, eyeY, eyeW, eyeH);
        g2.fillOval(eyeRX, eyeY, eyeW, eyeH);

        g2.setColor(Color.BLACK);
        int pupilW = Math.max(3, eyeW / 2);
        int pupilH = Math.max(3, eyeH / 3);
        int pupilOffsetX = direction.equals("left") ? -eyeW / 6 : direction.equals("right") ? eyeW / 6 : 0;
        int pupilOffsetY = direction.equals("up") ? -eyeH / 6 : direction.equals("down") ? eyeH / 6 : 0;

        g2.fillOval(eyeLX + (eyeW - pupilW) / 2 + pupilOffsetX, eyeY + (eyeH - pupilH) / 2 + pupilOffsetY, pupilW, pupilH);
        g2.fillOval(eyeRX + (eyeW - pupilW) / 2 + pupilOffsetX, eyeY + (eyeH - pupilH) / 2 + pupilOffsetY, pupilW, pupilH);

        g2.setColor(outlineColor.darker());
        int mouthW = bodyW / 3;
        int mouthH = Math.max(2, size / 20);
        int mouthX = screenX + (size - mouthW) / 2;
        int mouthY = screenY + size / 2 + size / 8;
        g2.fillRoundRect(mouthX, mouthY, mouthW, mouthH, mouthH, mouthH);

        if (gp.keyH != null && (gp.keyH.debugPress || gp.keyH.showHitbox)) {
            g2.setColor(inDeathZone ? Color.RED : Color.BLUE);
            g2.drawRect(screenX + solidArea.x, screenY + solidArea.y, solidArea.width, solidArea.height);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    private boolean isInCameraView(int screenX, int screenY) {
        if (gp == null) return false;

        return screenX + gp.tileSize > 0 &&
                screenX < gp.width &&
                screenY + gp.tileSize > 0 &&
                screenY < gp.height;
    }

    public void draw(Graphics2D g2) {
        if (gp == null) return;

        int screenX, screenY;

        if (gp.camera != null) {
            screenX = worldX - gp.camera.worldX;
            screenY = worldY - gp.camera.worldY;
        } else {
            screenX = worldX - gp.player.worldX + gp.player.screenX;
            screenY = worldY - gp.player.worldY + gp.player.screenY;
        }

        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            draw(g2, screenX, screenY);
        }
    }

    public boolean isInDeathZone() {
        return inDeathZone;
    }

    public void setInDeathZone(boolean inDeathZone) {
        this.inDeathZone = inDeathZone;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }
}
