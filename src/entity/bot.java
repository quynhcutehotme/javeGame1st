package entity;

import Game_2D.gamePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class bot extends entity {
    private gamePanel gp;
    private final Random random = new Random();
    private BufferedImage sprite;
    private Color bodyColor;
    private Color outlineColor;
    private int health = 1;
    private int aiCounter = 0;
    private int changeDirInterval = 60; // frames (1 second)
    private int stuckCounter = 0;
    private final int stuckThreshold = 15;
    public boolean isMovingRight = false;

    
    // Bot type for different movement patterns
    public enum BotType {
        GREEN,   // Zig-zag vertically while moving horizontally
        PURPLE,  // Sine-wave horizontal movement
        YELLOW   // Pause randomly for 1-2 seconds, then sprint
    }
    
    public BotType botType;
    
    // Behavioral states
    public enum BotState {
        PATROL,     // Random movement
        ALERT,      // Detected player, preparing to chase
        CHASE,      // Actively pursuing player
        SEARCH      // Lost player, searching last known location
    }
    
    private BotState currentState = BotState.PATROL;
    private int lastPlayerX, lastPlayerY;
    private int searchCounter = 0;
    private int alertCounter = 0;
    private final int searchDuration = 120; // frames to search (2 seconds)
    private final int alertDuration = 30;   // frames before chasing (0.5 seconds)
    
    // Detection range constants (will be initialized in constructor)
    private int DETECTION_RANGE;
    private int LOSS_RANGE;
    
    // Randomness for direction changes
    private int nextRandomChangeFrame = 0;
    private final double randomFlipChance = 0.05; // 5% chance per frame (reduced from 20% to prevent shaking)
    private int lastAvoidanceFrame = 0; // Track when we last applied avoidance
    private String lastAvoidanceDirection = null; // Smooth avoidance direction
    
    // Pattern-specific variables
    private int zigZagCounter = 0; // For green bots
    private int sineWaveCounter = 0; // For purple bots
    private int pauseCounter = 0; // For yellow bots
    private boolean isPaused = false;
    private int pauseDuration = 0;
    private int sprintCounter = 0;
    public int baseSpeed = 2; // Store base speed for yellow bot sprint calculation
    


    public bot(gamePanel gp, int worldX, int worldY, BotType type) {
        this.gp = gp;
        this.botType = type;
        // Initialize detection ranges after gp is set
        this.DETECTION_RANGE = gp.tileSize * 10;
        this.LOSS_RANGE = gp.tileSize * 15;
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
        
        // Set color based on bot type
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
        
        // Initialize random change frame (between 60-180 frames = 1-3 seconds at 60 FPS)
        nextRandomChangeFrame = random.nextInt(120) + 60;
        
        // Keep sprite null to use vector draw; can be loaded if you prefer images
        sprite = null;
    }

    /**
     * Convenience factory: create a bot at camera/screen coordinates (no randomness).
     * Callers can use `bot.createAtScreen(gp, screenX, screenY, type)` to spawn
     * a bot relative to the current camera view.
     */
    public static bot createAtScreen(gamePanel gp, int screenX, int screenY, BotType type) {
        int worldX = gp.player.worldX - gp.player.screenX + screenX;
        int worldY = gp.player.worldY - gp.player.screenY + screenY;
        return new bot(gp, worldX, worldY, type);
    }
    

    private void loadSprite() { /* no-op; using vector drawing now */ }
    
    // Phương thức update chính
    public void update() {
        aiCounter++;
        
        int dx = gp.player.worldX - worldX;
        int dy = gp.player.worldY - worldY;
        int distSq = dx * dx + dy * dy;

        boolean playerInRange = distSq < DETECTION_RANGE * DETECTION_RANGE;
        
        // 1. Bot-to-bot collision avoidance will be applied after movement
        // Store avoidance direction for later use
        String avoidanceDirection = null;
        if (currentState != BotState.CHASE && currentState != BotState.ALERT) {
            avoidanceDirection = calculateAvoidanceDirection();
        }
        
        // 2. Logic Trạng thái (FSM)
        switch (currentState) {
            case PATROL:
                if (playerInRange) {
                    currentState = BotState.ALERT;
                    alertCounter = 0;
                    break;
                }
                // Apply pattern-specific movement
                applyMovementPattern();
                break;

            case ALERT:
                alertCounter++;
                if (alertCounter >= alertDuration) {
                    currentState = BotState.CHASE;
                }
                // Hướng về phía người chơi (chỉ theo trục X để không bay lên trời)
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
                // Chase only horizontally (avoid vertical movement)
                setDirectionTowardHorizontal(dx);
                // Add occasional horizontal flip to vary behavior
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
                // Di chuyển về vị trí cuối cùng của player
                // Move toward last known player X only (horizontal search)
                setDirectionTowardHorizontal(lastPlayerX - worldX);
                break;
        }

        // 3. Apply random direction changes (every 1-3 seconds)
        if (aiCounter >= nextRandomChangeFrame) {
            if (currentState == BotState.PATROL && random.nextDouble() < 0.3) {
                // 30% chance to change direction when timer hits
                int r = random.nextInt(4);
                direction = (r == 0 ? "up" : r == 1 ? "down" : r == 2 ? "left" : "right");
            }
            // Reset timer for next random change (1-3 seconds)
            nextRandomChangeFrame = aiCounter + random.nextInt(120) + 60;
        }
        
        // 4. Random direction flip (reduced frequency to prevent shaking)
        // Only apply random flips occasionally, not every frame
        if (currentState == BotState.PATROL && aiCounter % 10 == 0 && random.nextDouble() < randomFlipChance) {
            // Only check every 10 frames and with reduced chance
            if (botType == BotType.GREEN || botType == BotType.PURPLE) {
                // Very low chance for pattern bots to preserve their patterns
                if (random.nextDouble() < 0.1) {
                    flipDirection();
                }
            } else {
                flipDirection();
            }
        }

        // 5. Apply sprint speed multiplier for yellow bots
        int currentSpeed = speed;
        if (botType == BotType.YELLOW && sprintCounter > 0 && !isPaused) {
            currentSpeed = (int)Math.ceil(baseSpeed * 2.0f); // gentler sprint
        }
        
        // 6. Kiểm tra va chạm và di chuyển
        collisionOn = false;
        gp.cChecker.checkTile(this);
        
        if (!collisionOn && !isPaused) {
            stuckCounter = 0;
            // Apply base movement
            switch (direction) {
                case "up": worldY -= currentSpeed; break;
                case "down": worldY += currentSpeed; break;
                case "left": worldX -= currentSpeed; break;
                case "right": worldX += currentSpeed; break;
            }
            
            // Apply bot-to-bot avoidance AFTER base movement (so it doesn't get overwritten)
            // Only apply avoidance every few frames to prevent jittering
            if (avoidanceDirection != null && (aiCounter - lastAvoidanceFrame) >= 2) {
                int avoidSpeed = Math.max(1, currentSpeed / 3); // Reduced to 1/3 speed for smoother avoidance
                switch (avoidanceDirection) {
                    case "up": worldY -= avoidSpeed; break;
                    case "down": worldY += avoidSpeed; break;
                    case "left": worldX -= avoidSpeed; break;
                    case "right": worldX += avoidSpeed; break;
                }
                lastAvoidanceFrame = aiCounter;
            }
            
            // Apply purple bot sine wave offset (applied after base movement)
            if (botType == BotType.PURPLE && currentState == BotState.PATROL) {
                double sineValue = Math.sin(sineWaveCounter * 0.1);
                int verticalOffset = (int)(sineValue * 1.5); // Amplitude of 1.5 pixels per frame
                worldY += verticalOffset;
                // Clamp to world boundaries
                worldY = Math.max(0, Math.min(gp.maxWorldRow * gp.tileSize - solidArea.height, worldY));
            }
        } else if (collisionOn) {
            stuckCounter++;
            // Khi va chạm, chuyển hướng ngẫu nhiên để không bị kẹt
            if (currentState != BotState.CHASE) {
                patrolMovement(); 
            }
            // If stuck for too long, force flip to escape
            if (stuckCounter >= stuckThreshold) {
                flipDirection();
                stuckCounter = 0;
            }
        }
        
        // 6. Kiểm tra va chạm với người chơi và gây sát thương
        checkPlayerCollision();
    }
    
    private void patrolMovement() {
        if (aiCounter % changeDirInterval == 0) {
            int r = random.nextInt(4);
            direction = (r == 0 ? "up" : r == 1 ? "down" : r == 2 ? "left" : "right");
        }
    }
    
    private void setDirectionToward(int dx, int dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? "right" : "left";
        } else {
            direction = (dy > 0) ? "down" : "up";
        }
    }

    /**
     * Set direction only horizontally (left/right) based on dx.
     * Useful to prevent bots from trying to fly vertically when chasing.
     */
    private void setDirectionTowardHorizontal(int dx) {
        direction = (dx > 0) ? "right" : "left";
    }

    /**
     * Flip only horizontally (left <-> right). Keeps vertical directions unchanged
     * only when appropriate callers use this helper.
     */
    private void flipHorizontal() {
        if ("left".equals(direction)) direction = "right";
        else if ("right".equals(direction)) direction = "left";
        else direction = "left"; // default to left if currently up/down
    }
    
    private void flipDirection() {
        switch (direction) {
            case "up": direction = "down"; break;
            case "down": direction = "up"; break;
            case "left": direction = "right"; break;
            case "right": direction = "left"; break;
        }
    }
    
    /**
     * Lightweight horizontal collision avoidance for side-scrolling game.
     * Only checks horizontal collisions (same Y-level), applies 50% nudge to each bot.
     * Uses squared distance checks for performance (avoids sqrt until needed).
     * 
     * @param allBots List of all bots to check against
     */
    public static void avoidClumping(java.util.List<bot> allBots) {
        if (allBots.size() < 2) return; // Need at least 2 bots
        
        final int AVOIDANCE_DISTANCE = 32; // 2 tiles (16px * 2)
        final int AVOIDANCE_DISTANCE_SQ = AVOIDANCE_DISTANCE * AVOIDANCE_DISTANCE;
        final int Y_TOLERANCE = 16; // Allow 1 tile vertical difference (same ground level)
        final int Y_TOLERANCE_SQ = Y_TOLERANCE * Y_TOLERANCE;
        final int MAX_NUDGE = 8; // Maximum nudge per frame to prevent jerky movement
        
        // Process each pair of bots once (O(n²) but lightweight)
        for (int i = 0; i < allBots.size(); i++) {
            bot botA = allBots.get(i);
            
            for (int j = i + 1; j < allBots.size(); j++) {
                bot botB = allBots.get(j);
                
                // Calculate horizontal and vertical differences
                int dx = botB.worldX - botA.worldX;
                int dy = botB.worldY - botA.worldY;
                
                // Use squared distance for performance (avoid sqrt)
                int distX_Sq = dx * dx;
                int distY_Sq = dy * dy;
                
                // Only check if bots are on similar Y-level (same ground level)
                if (distY_Sq > Y_TOLERANCE_SQ) {
                    continue; // Skip if too far vertically
                }
                
                // Check if bots are too close horizontally
                if (distX_Sq < AVOIDANCE_DISTANCE_SQ && distX_Sq > 0) {
                    // Calculate nudge amount (50% to each bot)
                    // Nudge strength based on how close they are (closer = stronger)
                    // Only calculate sqrt when we know we need to nudge
                    int distance = (int)Math.sqrt(distX_Sq);
                    int nudgeAmount = (AVOIDANCE_DISTANCE - distance) / 2; // Split between two bots
                    
                    // Limit max nudge to prevent jerky movement
                    nudgeAmount = Math.min(nudgeAmount, MAX_NUDGE);
                    nudgeAmount = Math.max(1, nudgeAmount); // At least 1px to ensure separation
                    
                    // Apply 50% nudge to each bot (push them apart horizontally)
                    if (dx > 0) {
                        // botB is to the right of botA
                        // Push botA left, botB right
                        botA.worldX -= nudgeAmount;
                        botB.worldX += nudgeAmount;
                    } else if (dx < 0) {
                        // botB is to the left of botA
                        // Push botA right, botB left
                        botA.worldX += nudgeAmount;
                        botB.worldX -= nudgeAmount;
                    }
                    // If dx == 0, bots are on same X, skip (rare edge case)
                }
            }
        }
    }
    
    private String calculateAvoidanceDirection() {
        int avoidanceRange = gp.tileSize * 1; // Reduced to 1 tile (only avoid when very close)
        int avoidanceRangeSq = avoidanceRange * avoidanceRange;
        
        // Track avoidance vector
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
            
            // Only avoid if very close (within 1 tile) to prevent constant jittering
            if (distSq < avoidanceRangeSq && distSq > 0) {
                nearbyBots++;
                if (distance < closestDist) {
                    closestDist = distance;
                }
                
                // Calculate direction away from other bot
                if (distance > 0) {
                    // Accumulate avoidance vector (away from other bot)
                    // Use inverse square for smoother falloff
                    double weight = 1.0 / (distance + 1); // Heavier weight for closer bots
                    avoidX -= (int)((dx / distance) * speed * weight);
                    avoidY -= (int)((dy / distance) * speed * weight);
                }
            }
        }
        
        // Only return avoidance direction if bots are very close and we need strong avoidance
        if (nearbyBots > 0 && closestDist < gp.tileSize * 0.8) {
            // Determine primary avoidance direction with threshold to prevent jittering
            int threshold = speed; // Only change if the avoidance is significant
            if (Math.abs(avoidX) > threshold && Math.abs(avoidX) > Math.abs(avoidY)) {
                // Avoid horizontally
                String newDir = (avoidX < 0) ? "left" : "right";
                // Smooth the direction change - only change if it's different from last frame
                if (newDir.equals(lastAvoidanceDirection) || lastAvoidanceDirection == null) {
                    lastAvoidanceDirection = newDir;
                    return newDir;
                } else {
                    // Keep previous direction to prevent rapid switching
                    return lastAvoidanceDirection;
                }
            } else if (Math.abs(avoidY) > threshold) {
                // Avoid vertically
                String newDir = (avoidY < 0) ? "up" : "down";
                // Smooth the direction change
                if (newDir.equals(lastAvoidanceDirection) || lastAvoidanceDirection == null) {
                    lastAvoidanceDirection = newDir;
                    return newDir;
                } else {
                    return lastAvoidanceDirection;
                }
            }
        }
        
        // Reset last direction if no avoidance needed
        if (nearbyBots == 0) {
            lastAvoidanceDirection = null;
        }
        
        return null; // No avoidance needed
    }
    
    private void applyMovementPattern() {
        switch (botType) {
            case GREEN:
                // Zig-zag: Move horizontally, but alternate vertical direction periodically
                zigZagCounter++;
                
                // Every 40 frames (~0.67 seconds), switch between horizontal and vertical
                if (zigZagCounter % 40 == 0) {
                    // Alternate between horizontal and vertical movement
                    if (direction.equals("left") || direction.equals("right")) {
                        // Switch to vertical (zig)
                        direction = (zigZagCounter / 40) % 2 == 0 ? "up" : "down";
                    } else {
                        // Switch back to horizontal (zag)
                        direction = random.nextBoolean() ? "left" : "right";
                    }
                }
                
                // Periodically change horizontal direction when moving horizontally
                if ((direction.equals("left") || direction.equals("right")) && zigZagCounter % 80 == 0) {
                    direction = direction.equals("left") ? "right" : "left";
                }
                break;
                
            case PURPLE:
                // Sine-wave: Move horizontally with vertical sine wave offset
                sineWaveCounter++;
                
                // Ensure horizontal movement
                if (!direction.equals("left") && !direction.equals("right")) {
                    direction = random.nextBoolean() ? "left" : "right";
                }
                
                // Sine wave vertical offset is applied in movement section (after base movement)
                // Periodically reverse horizontal direction
                if (sineWaveCounter % 120 == 0) {
                    direction = direction.equals("left") ? "right" : "left";
                }
                break;
                
            case YELLOW:
                // Pause randomly for 1-2 seconds, then sprint
                if (isPaused) {
                    pauseCounter++;
                    if (pauseCounter >= pauseDuration) {
                        isPaused = false;
                        sprintCounter = 90; // Sprint for 1.5 seconds
                    }
                } else if (sprintCounter > 0) {
                    sprintCounter--;
                } else {
                    // Random chance to pause (0.5% chance per frame = ~30% chance per second)
                    if (random.nextDouble() < 0.005) {
                        isPaused = true;
                        pauseCounter = 0;
                        pauseDuration = random.nextInt(60) + 60; // 1-2 seconds
                    }
                }
                // Normal patrol movement when not paused
                if (!isPaused && sprintCounter == 0) {
                    patrolMovement();
                }
                break;
        }
    }
    
    private void checkPlayerCollision() {
        // Tạo vùng va chạm tạm thời để kiểm tra
        Rectangle botHitbox = new Rectangle(worldX + solidArea.x, worldY + solidArea.y, solidArea.width, solidArea.height);
        Rectangle playerHitbox = new Rectangle(gp.player.worldX + gp.player.solidArea.x, 
                                               gp.player.worldY + gp.player.solidArea.y, 
                                               gp.player.solidArea.width, 
                                               gp.player.solidArea.height);

        // Kiểm tra xem bot có chạm vào player không
        if (botHitbox.intersects(playerHitbox)) {
            gp.player.takeDamage(this); // Gây sát thương
        }
    }

    /**
     * Apply damage to this bot. Returns true if the bot is dead and should be removed.
     */
    public boolean applyDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    // Phương thức draw (giữ nguyên)
    public void draw(Graphics2D g2) {
        // ... (Giữ nguyên phương thức draw)
        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
            worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
            worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
            worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {
            
            // Enable antialiasing for nicer shapes
            Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = gp.tileSize;
            int padding = Math.max(2, size / 12);

            // Shadow
            g2.setColor(new Color(0, 0, 0, 70));
            int shadowW = (int)(size * 0.8);
            int shadowH = (int)(size * 0.25);
            int shadowX = screenX + (size - shadowW) / 2;
            int shadowY = screenY + size - shadowH / 2;
            g2.fillOval(shadowX, shadowY, shadowW, shadowH);

            // Body
            int bodyX = screenX + padding;
            int bodyY = screenY + padding;
            int bodyW = size - padding * 2;
            int bodyH = size - padding * 2;
            g2.setColor(bodyColor);
            g2.fillRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);
            g2.setColor(outlineColor);
            g2.drawRoundRect(bodyX, bodyY + padding, bodyW, bodyH - padding, size / 3, size / 3);

            // Eyes
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

            // Mouth
            g2.setColor(outlineColor.darker());
            int mouthW = bodyW / 3;
            int mouthH = Math.max(2, size / 20);
            int mouthX = screenX + (size - mouthW) / 2;
            int mouthY = screenY + size / 2 + size / 8;
            g2.fillRoundRect(mouthX, mouthY, mouthW, mouthH, mouthH, mouthH);

            // Restore AA
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        }
    }

	public void updateAI(int playerX, int playerY) {
        // Nếu đang ở chế độ di chuyển sang phải (như Dinosaur game)
        if (isMovingRight) {
            direction = "right";
            worldX += speed;
            
            // Kiểm tra nếu bot đã đi quá xa bên phải thì xóa
            if (worldX > gp.player.worldX + gp.width) {
                gp.bots.remove(this);
            }
        } else {
            // AI cũ theo player (giữ lại cho các tính năng khác)
            // ... existing AI code ...
        }
        
        // Update animation
        spriteCounter++;
        if (spriteCounter > 12) {
            if (spriteNum == 1) spriteNum = 2;
            else if (spriteNum == 2) spriteNum = 1;
            spriteCounter = 0;
        }
    }
}