package Game_2D;

public class Camera {
    private gamePanel gp;

    public int worldX;  // Vị trí camera trong world
    public int worldY;

    // Vùng chết (dead zone) - player có thể di chuyển tự do trong vùng này
    private int deadZoneLeft;    // 1/3 màn hình
    private int deadZoneRight;   // 2/3 màn hình
    private int deadZoneTop;     // 1/3 màn hình
    private int deadZoneBottom;  // 2/3 màn hình

    public Camera(gamePanel gp) {
        this.gp = gp;
        this.worldX = 0;
        this.worldY = 0;

        // Tính toán vùng chết dựa trên kích thước màn hình
        deadZoneLeft = gp.width / 3;
        deadZoneRight = gp.width * 2 / 3;
        deadZoneTop = gp.height / 3;
        deadZoneBottom = gp.height * 2 / 3;
    }

    public void update() {
        if (gp.player == null) return;

        // Tính toán vị trí player trên màn hình
        int playerScreenX = gp.player.worldX - worldX;
        int playerScreenY = gp.player.worldY - worldY;

        // === LOGIC CAMERA KIỂU MARIO ===

        // 1. TRỤC X: Camera luôn theo player khi ra khỏi vùng chết
        if (playerScreenX > deadZoneRight) {
            worldX += playerScreenX - deadZoneRight;
        } else if (playerScreenX < deadZoneLeft) {
            worldX += playerScreenX - deadZoneLeft;
        }

        // 2. TRỤC Y: Camera chỉ di chuyển khi player rơi xuống hoặc nhảy lên quá cao
        // Camera Y gần như cố định (kiểu Mario cổ điển)
        if (playerScreenY > deadZoneBottom) {
            // Khi player rơi xuống quá thấp
            worldY += (playerScreenY - deadZoneBottom) * 0.5f; // Di chuyển chậm
        } else if (playerScreenY < deadZoneTop) {
            // Khi player nhảy lên quá cao
            worldY += (playerScreenY - deadZoneTop) * 0.5f; // Di chuyển chậm
        }

        // Giới hạn camera trong phạm vi world
        clampToWorldBounds();
    }

    private void clampToWorldBounds() {
        // Giới hạn X
        if (worldX < 0) {
            worldX = 0;
        } else if (worldX > gp.worldWidth - gp.width) {
            worldX = gp.worldWidth - gp.width;
        }

        // Giới hạn Y
        if (worldY < 0) {
            worldY = 0;
        } else if (worldY > gp.worldHeight - gp.height) {
            worldY = gp.worldHeight - gp.height;
        }
    }

    public int worldXToScreenX(int worldX) {
        return worldX - this.worldX;
    }

    public int worldYToScreenY(int worldY) {
        return worldY - this.worldY;
    }

    // Reset camera về vị trí player
    public void centerOnPlayer() {
        if (gp.player != null) {
            worldX = gp.player.worldX - gp.width / 2;
            worldY = gp.player.worldY - gp.height / 2;
            clampToWorldBounds();
        }
    }

    // Getter cho vùng chết (debug)
    public int[] getDeadZone() {
        return new int[]{deadZoneLeft, deadZoneRight, deadZoneTop, deadZoneBottom};
    }
}