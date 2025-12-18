package Game_2D;

import entity.bot;
import java.awt.*;

public class CombatSystem {

    private final gamePanel gp;

    public CombatSystem(gamePanel gp) {
        this.gp = gp;
    }

    public void handleStompMechanic() {
        if (!gp.player.isJumping || gp.player.velocityY <= 0) {
            gp.stompActive = false;
            return;
        }

        Rectangle stompArea = new Rectangle(
                gp.player.worldX + gp.player.solidArea.x,
                gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height - 10,
                gp.player.solidArea.width,
                15
        );

        java.util.Iterator<bot> iterator = gp.bots.iterator();
        while (iterator.hasNext()) {
            bot b = iterator.next();

            Rectangle botHeadArea = new Rectangle(
                    b.worldX + b.solidArea.x,
                    b.worldY + b.solidArea.y - 5,
                    b.solidArea.width,
                    10
            );

            if (stompArea.intersects(botHeadArea)) {
                gp.stompActive = true;

                boolean dead = b.applyDamage(gp.stompDamage);

                if (dead) {
                    gp.deathEffects.add(new DeathEffect(
                            b.worldX + b.solidArea.width / 2,
                            b.worldY + b.solidArea.height / 2
                    ));

                    iterator.remove();
                    gp.botsKilled++;

                    int effectScreenX = gp.camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                    int effectScreenY = gp.camera.worldYToScreenY(b.worldY);
                    gp.damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "STOMP!", new Color(255, 200, 0)));
                }

                gp.player.velocityY = -gp.stompBounceForce;
                gp.player.isGrounded = false;

                gp.attackEffects.add(new AttackEffect(stompArea, 10, false));
                break;
            }
        }
    }

    public void checkPlayerBotCollision() {
        if (gp.playerInvincible) {
            gp.invincibleCounter++;
            if (gp.invincibleCounter > gp.invincibleTime) {
                gp.invincibleCounter = 0;
                gp.playerInvincible = false;
            }
            return;
        }

        if (gp.stompActive) return;

        for (bot b : gp.bots) {
            if (gp.cChecker.entitiesIntersect(gp.player, b)) {

                if (gp.player.isJumping && gp.player.velocityY > 0) {
                    Rectangle playerBottom = new Rectangle(
                            gp.player.worldX + gp.player.solidArea.x,
                            gp.player.worldY + gp.player.solidArea.y + gp.player.solidArea.height - 5,
                            gp.player.solidArea.width,
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

                gp.playerHp = Math.max(0, gp.playerHp - 1);
                gp.damageEffects.add(new damageEffect(
                        gp.camera.worldXToScreenX(gp.player.worldX) + gp.tileSize / 2,
                        gp.camera.worldYToScreenY(gp.player.worldY),
                        "-1"
                ));
                gp.playerInvincible = true;
                gp.invincibleCounter = 0;

                int knockbackDirection = (b.worldX < gp.player.worldX) ? 1 : -1;
                gp.player.worldX += knockbackDirection * gp.player.speed * 3;

                break;
            }
        }
    }

    public Rectangle buildAttackHitbox(String direction) {
        int baseX = gp.player.worldX + gp.player.solidArea.x;
        int baseY = gp.player.worldY + gp.player.solidArea.y;
        int boxWidth = gp.player.solidArea.width;
        int boxHeight = gp.player.solidArea.height;

        Rectangle hitbox;
        switch (direction) {
            case "left":
                hitbox = new Rectangle(baseX - gp.attackRange, baseY, gp.attackRange, boxHeight);
                break;
            case "right":
                hitbox = new Rectangle(baseX + boxWidth, baseY, gp.attackRange, boxHeight);
                break;
            case "up":
                hitbox = new Rectangle(baseX, baseY - gp.attackRange, boxWidth, gp.attackRange);
                break;
            case "down":
                hitbox = new Rectangle(baseX, baseY + boxHeight, boxWidth, gp.attackRange);
                break;
            default:
                hitbox = new Rectangle(baseX + boxWidth, baseY, gp.attackRange, boxHeight);
                break;
        }
        return hitbox;
    }

    public void performAttack(String directionSnapshot) {
        Rectangle hitbox = buildAttackHitbox(directionSnapshot);
        gp.attackEffects.add(new AttackEffect(hitbox, 8, false));

        java.util.Iterator<bot> iterator = gp.bots.iterator();
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
                    gp.deathEffects.add(new DeathEffect(b.worldX + b.solidArea.width / 2, b.worldY + b.solidArea.height / 2));
                    iterator.remove();
                    gp.botsKilled++;
                }
                int effectScreenX = gp.camera.worldXToScreenX(b.worldX + b.solidArea.width / 2);
                int effectScreenY = gp.camera.worldYToScreenY(b.worldY);
                gp.damageEffects.add(new damageEffect(effectScreenX, effectScreenY, "+1", new Color(50, 200, 50)));
            }
        }
    }
}
